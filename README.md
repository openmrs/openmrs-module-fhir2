<img src="https://repository-images.githubusercontent.com/232160993/36a03500-3221-11ea-9176-0786b70e4a13"  alt="OpenMS on FHIR"/>

openmrs-module-fhir2
==========================
[![Build with Maven](https://github.com/openmrs/openmrs-module-fhir2/actions/workflows/main.yml/badge.svg)](https://github.com/openmrs/openmrs-module-fhir2/actions/workflows/main.yml)
[![codecov](https://codecov.io/gh/openmrs/openmrs-module-fhir2/branch/master/graph/badge.svg)](https://codecov.io/gh/openmrs/openmrs-module-fhir2)

Description
-----------
This is intended to be a replacement for the current OpenMRS FHIR module,
initially using FHIR R4.

Development Principles
----------------------

The FHIR2 module is divided into a series of layers, each responsible for a
single piece of functionality. Each layer and the principles behind it is
described below. Note that the rules given for each layer should be read
as principles rather than hard and fast rules; they can be broken if there
is a good enough explanation for why.

1. `ResourceProvider`s: These are straight-forward HAPI
[`ResourceProviders`](https://hapifhir.io/hapi-fhir/docs/server_plain/resource_providers.html).
They serve to get the necessary parameters for each operation from the HAPI
framework and then pass those parameters along to a `Service` class that
implements the actual operation. Note that `ResourceProvider`s support older
FHIR versions are responsible for translating from newer FHIR resources to
their older equivalents. `ResourceProvider`s should depend only on `Service`
layer interfaces to allow the implementation of a resource to be changed at
runtime based on local customisations. `ResourceProvider`s can be found in
the 
[`org.openmrs.module.fhir2.r4`](https://github.com/openmrs/openmrs-module-fhir2/tree/master/api/src/main/java/org/openmrs/module/fhir2/providers/r4) package and the 
[`org.openmrs.module.fhir2.r3`](https://github.com/openmrs/openmrs-module-fhir2/tree/master/api/src/main/java/org/openmrs/module/fhir2/providers/r3) package.
1. `Service`s: `Service` classes are designed to implement the functionality
needed by `ResourceProviders` in a reusable fashion. This allows us to use a
single code-base to support multiple FHIR versions and allows the OpenMRS-specific
FHIR functionality to be isolated from the HAPI-specific FHIR functionality.
Service classes primarily coordinate between the `Dao` and `Translator`
layers. Generally, service classes are implemented as both an interface and
a concrete implementation class, but we really only need the interface where
the service corresponds directly to a `ResourceProvider`. `Service` classes
should depend only on the interfaces of `Translator`s and `Dao`s, to enable
the implementations of these classes to be swapped at runtime.
`Service` interfaces can be found in the
[`org.openmrs.module.fhir2.api`](https://github.com/openmrs/openmrs-module-fhir2/tree/master/api/src/main/java/org/openmrs/module/fhir2/api) package and the implementations in the
[`org.openmrs.module.fhir2.api.impl`](https://github.com/openmrs/openmrs-module-fhir2/tree/master/api/src/main/java/org/openmrs/module/fhir2/api/impl) package.
1. `Translator`s: `Translator`s are the classes that are primarily responsible
for mapping between FHIR's data model and the OpenMRS data model (though note
that other parts of the code need to be aware of these mappings, for example,
to ensure that searching and translating use the same mappings. Where possible,
`Translator`s should try to implement the full `OpenmrsFhirUpdatableTranslator`
interface, which supports mapping from OpenMRS objects to FHIR objects with
special handling for creating and updating OpenMRS objects. Where that
functionality is not needed, perhaps because we only support translating
something to FHIR and not back to OpenMRS, other interfaces are available to
describe the functionality supported. See the actual translators for examples.
`Translator`s can be found in the
[`org.openmrs.module.fhir2.api.translators`](https://github.com/openmrs/openmrs-module-fhir2/tree/master/api/src/main/java/org/openmrs/module/fhir2/api/translators) package and the corresponding
implementations in the
[`org.openmrs.module.fhir2.api.translators.impl`](https://github.com/openmrs/openmrs-module-fhir2/tree/master/api/src/main/java/org/openmrs/module/fhir2/api/translators/impl) package.
1. `Dao`s: `Dao` classes are responsible for actually interacting with the
OpenMRS database. Normally, we do this relatively directly by using Hibernate,
but this is also the layer than can use default OpenMRS services if there is
special business logic we need to ensure is respected. The reason for using
our own data access layer rather than the standard OpenMRS data access layer
is to enable us to more effectively support the FHIR Search specification.
Accessing the database directly allows us to turn what might be several
calls to the OpenMRS service layer and some manual in-memory filtering into
generally one or two database queries following a standard format.
`Dao`s can be found in the
[`org.openmrs.module.fhir2.api.dao`](https://github.com/openmrs/openmrs-module-fhir2/tree/master/api/src/main/java/org/openmrs/module/fhir2/api/dao) package and the corresponding
implementations in the
[`org.openmrs.module.fhir2.api.dao.impl`](https://github.com/openmrs/openmrs-module-fhir2/tree/master/api/src/main/java/org/openmrs/module/fhir2/api/dao/impl) package.

There are a couple of things that are not standard practice for other OpenMRS
modules that should be borne in mind while developing this module.

1. The OpenMRS service layer, e.g. `PatientService`, `EncounterService` etc.
should only be used inside DAO objects.
1. Avoid using `org.openmrs.Context` except as a last resort. We should favour
injecting the appropriate service rather than relying on `Context` to load it
for us.

### Breaking Development Principles

The principles laid out above are just that: principles and baseline expected
patterns for this module. They are not, however, unbreakable rules, but rather
guidelines. Generally, these principles are designed to ensure that the FHIR
module is maximally customisable without making that customisation a core
feature of the module. The idea is that all of our mapping decisions are
overridable at runtime via other modules without distracting from the ability
to easily determine where any given functionality can be found. By sticking
to common patterns, we make both of these tasks easier as people looking to
implement, override or improve functionality should be able to easily work
out where they need to do so.

Exceptions to the rules above are fine as long as they are in line with enabling
customisation and ease of discovery of implementations. That is to say, we
should have a stronger justification for breaking these principles than just
"it's easier to code this way."

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

Deployment
------------
For performance reasons it is recommended to set **hibernate.cache.use_query_cache=true** for openmrs-core versions lower than 2.6.2, where it is set by default. It is to benefit from caching of result counts. You can set it via [OpenMRS runtime properties](https://wiki.openmrs.org/display/docs/Overriding+OpenMRS+Default+Runtime+Properties).


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
[Hamcrest's `assertThat()` method](http://hamcrest.org/JavaHamcrest/javadoc/2.2/org/hamcrest/MatcherAssert.html#assertThat-T-org.hamcrest.Matcher-).
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
