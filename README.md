openmrs-module-fhir2
==========================
[![Build Status](https://travis-ci.com/openmrs/openmrs-module-fhir2.svg?branch=master)](https://travis-ci.com/ibacher/openmrs-module-fhir2)
[![codecov](https://codecov.io/gh/openmrs/openmrs-module-fhir2/branch/master/graph/badge.svg)](https://codecov.io/gh/ibacher/openmrs-module-fhir2)

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
1. Favour JSR-330 style `@Inject` over `@Autowired` whenever possible. This
will allow us to de-couple from Spring if necessary.
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
