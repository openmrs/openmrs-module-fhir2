<img src="https://repository-images.githubusercontent.com/232160993/36a03500-3221-11ea-9176-0786b70e4a13"  alt="OpenMS on FHIR"/>

openmrs-module-fhir2
==========================
[![Build Status](https://travis-ci.com/openmrs/openmrs-module-fhir2.svg?branch=master)](https://travis-ci.com/openmrs/openmrs-module-fhir2)
[![codecov](https://codecov.io/gh/openmrs/openmrs-module-fhir2/branch/master/graph/badge.svg)](https://codecov.io/gh/openmrs/openmrs-module-fhir2)

Description
-----------
This is intended to be a replacement for the current OpenMRS FHIR module,
initially using FHIR R4.

Development Principles
----------------------

There are a couple of things that are not standard practice for other OpenMRS
modules that should be borne in mind while developing this module.

1. The OpenMRS service layer, e.g. `PatientService`, `EncounterService` etc.
should only be used inside DAO objects.
1. Avoid using `org.openmrs.Context` except as a last resort. We should favour
injecting the appropriate service rather than relying on `Context` to load it
for us.

Technology Stack
----------------

Broadly speaking, we should try to use the tools and utilities common to the
OpenMRS environment, such as [the Spring framework](https://spring.io/projects/spring-framework),
[Hibernate](https://hibernate.org/), [Liquibase](https://www.liquibase.org/),
[Slf4j](http://www.slf4j.org/), [JUnit](https://junit.org/junit4/), and
[Mockito](https://site.mockito.org/). Libraries not included in the OpenMRS platform
should be included sparingly; the more libraries bundled in this module, the
more likely it is to interfere with other modules.

That said, we should attempt to use up-to-date methods for using these libraries,
which will sometimes differ from how these technologies are used in the OpenMRS core.
For example, we should favour annotation-based configuration for
[Spring](https://www.baeldung.com/spring-core-annotations),
[Hibernate](https://docs.jboss.org/hibernate/stable/annotations/reference/en/html_single/#entity-mapping),
and [JUnit](https://www.mkyong.com/unittest/junit-4-tutorial-1-basic-usage/) rather
than their XML equivalents, where possible.

In addition to the default libraries, we should use [Lombok](https://projectlombok.org/)
where feasible to avoid having to write so much repetitive code.

Unit Testing
------------

We should aim to have as much of this modules' code as possible tested with automated
unit tests. Unit tests are tests which cover a single functional unit of code. More
specifically, unit tests should aim to cover the behaviour of any methods that are marked
as public. [This blog post](https://phauer.com/2019/modern-best-practices-testing-java/)
has good pointers on writing unit tests and on how to write code that is testable.
However, for the purposes of this module, ignore the sections under "Test Close To The Reality".
That is, we should write tests that make judicious use of mocks and tests that require
a database should use an in-memory database.

In general, to test the output of any method, we should favour using
[JUnit's `assertThat()` method](http://junit.sourceforge.net/javadoc/org/junit/Assert.html#assertThat(T,%20org.hamcrest.Matcher)).
This will tend to lead to more "readable" assertions. For example compare this:

```java
Concept expected = getExpectedConcept();

Concept result = getConcept("expectedConcept");

assertThat(result, equalTo(expected));
```

To this:

```java
Concept expected = getExpectedConcept();

Concept result = getConcept("expectedConcept");

assertEquals(result, expected);
``` 

While the latter is slightly shorter, the assertion in the former reads closer to
an English sentence.

Each unit test should be thought of as having three sections:

1. The preconditions (given)
1. The code being tested (when)
1. Checking the expected result (then)

For example:
```java
// given
// here we setup whatever needs to be setup to test our code
Concept expected = getExpectedConcept();

// when
// this is the code we are testing
Concept result = getConcept("expectedConcept");

// then
// here we verify that what we expect happens.
assertThat(result, equalTo(expected));
```

Class Naming Conventions
------------------------

* Prefer to create an interface rather than directly creating a concrete type. This allows modules and implementers to swap
out these classes with minimal effort.
* All interfaces should be the name of a class without additional text.  For example, favour `GreatClass` as an interface
name over `IGreatClass` or `GreatClassInterface`.
* All implementation classes should be the name of the class with `Impl` to distinguish them from the interface. For example,
`GreatClassImpl`.
* All abstract classes should start with `Base` and not end with `Impl`. For example, `BaseGreatClass`.
* These conventions can be waved for `ResourceProvider`s, as they all implement HAPI's `IResourceProvider` interface and are
not expected to be overwritten by implementations. Instead, they should be named `GreatClassFhirResourceProvider`.

License
-------

The license for this project is included in the [LICENSE](https://github.com/openmrs/openmrs-module-fhir2/blob/master/LICENSE)
file. However, in addition to the full license included here, each file should contain the text found in
[license-header.txt](https://github.com/openmrs/openmrs-module-fhir2/blob/master/license-header.txt)
at the start of the file.

For Java files, we should have the following message:

```java
/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * 
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
```

For XML files, we should use the following:

```xml
<!--
    This Source Code Form is subject to the terms of the Mozilla Public License,
    v. 2.0. If a copy of the MPL was not distributed with this file, You can
    obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
    the terms of the Healthcare Disclaimer located at http://openmrs.org/license.

    Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
    graphic logo is a trademark of OpenMRS Inc.
-->
```

For properties files, we should use the following:

```properties
#
# This Source Code Form is subject to the terms of the Mozilla Public License,
# v. 2.0. If a copy of the MPL was not distributed with this file, You can
# obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
# the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
#
# Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
# graphic logo is a trademark of OpenMRS Inc.
#
```

In any case, the appropriate license header can easily be added to any existing file that needs it by running:

```shell script
mvn com.mycila:license-maven-plugin:format
```
