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

import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.in;

import lombok.AccessLevel;
import lombok.Setter;
import org.hibernate.Criteria;
import org.openmrs.Form;
import org.openmrs.module.fhir2.api.dao.FhirQuestionnaireDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

@Component
@Setter(AccessLevel.PACKAGE)
public class FhirQuestionnaireDaoImpl extends BaseFhirDao<Form> implements FhirQuestionnaireDao {

    @Override
    public Form getQuestionnaireById(@Nonnull Integer id) {
        return (Form) getSessionFactory().getCurrentSession().createCriteria(Form.class).add(eq("formId", id))
                .uniqueResult();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Form> getQuestionnairesByIds(@Nonnull Collection<Integer> ids) {
        return getSessionFactory().getCurrentSession().createCriteria(Form.class).add(in("id", ids)).list();
    }

    @Override
    protected void setupSearchParams(Criteria criteria, SearchParameterMap theParams) {
        criteria.add(eq("set", true));
    }

}
