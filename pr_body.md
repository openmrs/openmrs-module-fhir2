## What I changed
- Added `FhirNormalizationUtils` to trim `CodeableConcept` codings and decode HL7 v2 escapes (\F\ → |, \S\ → ^, \T\ → &, \R\ → ~, \E\ → \).
- Added unit test `FhirNormalizationUtilsTest`.

## Why
- Prevents stray whitespace and HL7 escape sequences from leaking into FHIR R4 resources.

## Tests
- `FhirNormalizationUtilsTest` passes locally.

## Notes
- Java 17 used. Added `--add-opens` JVM flags where needed for local runs.
