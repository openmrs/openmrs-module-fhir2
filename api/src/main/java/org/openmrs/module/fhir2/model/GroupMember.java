/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2.model;

import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.util.ElementUtil;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Configuration;
import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Substance;

@ResourceDef(name = "GroupMember", profile = "http://fhir.openmrs.org/R4/StructureDefinition/GroupMember")
public class GroupMember extends DomainResource {
	
	private static final long serialVersionUID = 1L;
	
	@Child(name = "entity", type = { Patient.class, Practitioner.class, PractitionerRole.class, Device.class,
	        Medication.class, Substance.class, Group.class }, order = 1, min = 1)
	@Description(shortDefinition = "Reference to the group member", formalDefinition = "A reference to the entity that is a member of the group. Must be consistent with Group.type. If the entity is another group, then the type must be the same.")
	protected Reference entity;
	
	protected Resource entityTarget;
	
	@Child(name = "period", type = { Period.class }, order = 2)
	@Description(shortDefinition = "Period member belonged to the group", formalDefinition = "The period that the member was in the group, if known.")
	protected Period period;
	
	@Child(name = "inactive", type = { BooleanType.class }, order = 3)
	@Description(shortDefinition = "If member is no longer in group", formalDefinition = "A flag to indicate that the member is no longer in the group, but previously may have been a member.")
	protected BooleanType inactive;
	
	public GroupMember() {
	}
	
	public GroupMember(Reference entity) {
		this.entity = entity;
	}
	
	public Reference getEntity() {
		if (this.entity == null) {
			if (Configuration.errorOnAutoCreate()) {
				throw new Error("Attempt to auto-create GroupMember.entity");
			}
			
			if (Configuration.doAutoCreate()) {
				this.entity = new Reference();
			}
		}
		
		return this.entity;
	}
	
	public boolean hasEntity() {
		return this.entity != null && !this.entity.isEmpty();
	}
	
	public GroupMember setEntity(Reference value) {
		this.entity = value;
		return this;
	}
	
	public Resource getEntityTarget() {
		return this.entityTarget;
	}
	
	public GroupMember setEntityTarget(Resource value) {
		this.entityTarget = value;
		return this;
	}
	
	public Period getPeriod() {
		if (this.period == null) {
			if (Configuration.errorOnAutoCreate()) {
				throw new Error("Attempt to auto-create GroupMember.period");
			}
			
			if (Configuration.doAutoCreate()) {
				this.period = new Period();
			}
		}
		
		return this.period;
	}
	
	public boolean hasPeriod() {
		return this.period != null && !this.period.isEmpty();
	}
	
	public GroupMember setPeriod(Period value) {
		this.period = value;
		return this;
	}
	
	public BooleanType getInactiveElement() {
		if (this.inactive == null) {
			if (Configuration.errorOnAutoCreate()) {
				throw new Error("Attempt to auto-create GroupMember.inactive");
			}
			
			if (Configuration.doAutoCreate()) {
				this.inactive = new BooleanType();
			}
		}
		
		return this.inactive;
	}
	
	public boolean hasInactiveElement() {
		return this.inactive != null && !this.inactive.isEmpty();
	}
	
	public boolean hasInactive() {
		return this.inactive != null && !this.inactive.isEmpty();
	}
	
	public GroupMember setInactiveElement(BooleanType value) {
		this.inactive = value;
		return this;
	}
	
	public boolean getInactive() {
		return this.inactive != null && !this.inactive.isEmpty() ? this.inactive.getValue() : false;
	}
	
	public GroupMember setInactive(boolean value) {
		if (this.inactive == null) {
			this.inactive = new BooleanType();
		}
		
		this.inactive.setValue(value);
		return this;
	}
	
	@Override
	public DomainResource copy() {
		GroupMember groupMember = new GroupMember();
		this.copyValues(groupMember);
		return groupMember;
	}
	
	public void copyValues(GroupMember gm) {
		super.copyValues(gm);
		gm.entity = this.entity == null ? null : this.entity.copy();
		gm.period = this.period == null ? null : this.period.copy();
		gm.inactive = this.inactive == null ? null : this.inactive.copy();
	}
	
	@Override
	public ResourceType getResourceType() {
		return null;
	}
	
	@Override
	public FhirVersionEnum getStructureFhirVersionEnum() {
		return FhirVersionEnum.R4;
	}
	
	@Override
	public boolean isEmpty() {
		return ElementUtil.isEmpty(entity, entityTarget, inactive, period);
	}
}
