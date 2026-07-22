/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2;

import javax.sql.DataSource;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@ImportResource({ "classpath:applicationContext-service.xml", "classpath*:moduleApplicationContext.xml" })
public class TestFhirSpringConfiguration {
	
	@Autowired
	private DataSource dataSource;
	
	// core's SchedulerConfig (ShedLock LockProvider) needs this table at context init; core's test base
	// only creates it at @BeforeEach since the test schema comes from hbm2ddl, not liquibase
	@PostConstruct
	public void ensureShedlockTable() {
		new JdbcTemplate(dataSource)
		        .execute("CREATE TABLE IF NOT EXISTS shedlock (name VARCHAR(64) NOT NULL, lock_until TIMESTAMP NOT NULL, "
		                + "locked_at TIMESTAMP NOT NULL, locked_by VARCHAR(255) NOT NULL, PRIMARY KEY (name))");
	}
}
