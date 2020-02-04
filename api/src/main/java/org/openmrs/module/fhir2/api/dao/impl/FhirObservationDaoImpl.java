/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.api.dao.impl;

import static org.hibernate.criterion.Order.asc;
import static org.hibernate.criterion.Order.desc;
import static org.hibernate.criterion.Projections.property;
import static org.hibernate.criterion.Restrictions.and;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.ilike;
import static org.hibernate.criterion.Restrictions.in;
import static org.hibernate.criterion.Restrictions.or;
import static org.hibernate.criterion.Subqueries.propertyEq;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.FhirConceptSource;
import org.openmrs.module.fhir2.api.dao.FhirObservationDao;
import org.springframework.stereotype.Component;

@Component
public class FhirObservationDaoImpl implements FhirObservationDao {
	
	@Inject
	@Named("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Override
	public Obs getObsByUuid(String uuid) {
		return (Obs) sessionFactory.getCurrentSession().createCriteria(Obs.class).add(eq("uuid", uuid)).uniqueResult();
	}
	
	@Override
	public Collection<Obs> searchForObservations(ReferenceParam encounterReference, ReferenceParam patientReference,
	        TokenAndListParam code, SortSpec sort) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class);
		
		handleEncounterReference(criteria, encounterReference);
		handlePatientReference(criteria, patientReference);
		handleCodedConcept(criteria, code);
		handleSort(criteria, sort);
		
		return criteria.list();
	}
	
	private void handleCodedConcept(Criteria criteria, TokenAndListParam code) {
		if (code != null) {
			criteria.createAlias("concept", "c");
			boolean addedMappingAliases = false;
			
			for (TokenOrListParam tokenList : code.getValuesAsQueryTokens()) {
				List<Criterion> codedConcepts = new ArrayList<>();
				
				List<TokenParam> paramList = tokenList.getValuesAsQueryTokens();
				paramList.sort(
				    Comparator.comparing(tokenParam -> tokenParam.getSystem() == null ? "" : tokenParam.getSystem()));
				
				String previousSystem = null;
				List<String> codes = new ArrayList<>();
				for (TokenParam coding : paramList) {
					if (coding.getSystem() != null) {
						if (!addedMappingAliases) {
							criteria.createAlias("c.conceptMappings", "cm").createAlias("cm.conceptReferenceTerm", "crt");
							addedMappingAliases = true;
						}
						
						if (!coding.getSystem().equals(previousSystem)) {
							if (codes.size() > 0) {
								generateSystemQuery(previousSystem, codes, codedConcepts);
								codes.clear();
							}
							
							previousSystem = coding.getSystem();
						}
						
						codes.add(coding.getValue());
					} else {
						// note that since we sort a null system to an empty string, we should never get coded mappings
						// before the system-specific codes.
						codedConcepts.add(
						    or(eq("c.conceptId", NumberUtils.toInt(coding.getValue())), eq("c.uuid", coding.getValue())));
					}
				}
				
				if (codes.size() > 0) {
					generateSystemQuery(previousSystem, codes, codedConcepts);
				}
				
				criteria.add(or(codedConcepts.toArray(new Criterion[0])));
			}
		}
	}
	
	private void generateSystemQuery(String system, List<String> codes, List<Criterion> codedConcepts) {
		DetachedCriteria conceptSourceCriteria = DetachedCriteria.forClass(FhirConceptSource.class).add(eq("url", system))
		        .setProjection(property("conceptSource"));
		
		if (codes.size() > 1) {
			codedConcepts.add(and(propertyEq("crt.conceptSource", conceptSourceCriteria), in("crt.code", codes)));
		} else {
			codedConcepts.add(and(propertyEq("crt.conceptSource", conceptSourceCriteria), eq("crt.code", codes.get(0))));
		}
	}
	
	private void handleEncounterReference(Criteria criteria, ReferenceParam encounterReference) {
		if (encounterReference != null && encounterReference.getIdPart() != null) {
			criteria.createAlias("encounter", "e").add(eq("e.uuid", encounterReference.getIdPart()));
		}
	}
	
	private void handlePatientReference(Criteria criteria, ReferenceParam patientReference) {
		if (patientReference != null) {
			criteria.createAlias("person", "p");
			
			if (patientReference.getChain() != null) {
				switch (patientReference.getChain()) {
					case Patient.SP_IDENTIFIER:
						criteria.createAlias("p.identifiers", "pi").add(ilike("pi.identifier", patientReference.getValue()));
						break;
					case Patient.SP_GIVEN:
						criteria.createAlias("p.names", "pn")
						        .add(ilike("pn.givenName", patientReference.getValue(), MatchMode.START));
						break;
					case Patient.SP_FAMILY:
						criteria.createAlias("p.names", "pn")
						        .add(ilike("pn.familyName", patientReference.getValue(), MatchMode.START));
						break;
					case Patient.SP_NAME:
						criteria.createAlias("p.names", "pn");
						List<Criterion> criterionList = new ArrayList<>();
						
						for (String token : StringUtils.split(patientReference.getValue(), " \t,")) {
							criterionList.add(ilike("pn.givenName", token, MatchMode.START));
							criterionList.add(ilike("pn.middleName", token, MatchMode.START));
							criterionList.add(ilike("pn.familyName", token, MatchMode.START));
						}
						
						criteria.add(or(criterionList.toArray(new Criterion[0])));
						break;
					case "":
						criteria.add(eq("p.uuid", patientReference.getValue()));
						break;
				}
			}
		}
	}
	
	private void handleSort(Criteria criteria, SortSpec sort) {
		SortSpec sortSpec = sort;
		while (sortSpec != null) {
			String prop = paramToProp(sortSpec.getParamName());
			if (prop != null) {
				switch (sortSpec.getOrder()) {
					case DESC:
						criteria.addOrder(desc(prop));
						break;
					case ASC:
						criteria.addOrder(asc(prop));
						break;
				}
			}
			
			sortSpec = sortSpec.getChain();
		}
	}
	
	private String paramToProp(@NotNull String paramName) {
		switch (paramName) {
			case "date":
				return "obsDatetime";
			default:
				return null;
		}
	}
}
