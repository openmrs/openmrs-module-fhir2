# Vaccination Module Features Implementation

This document describes the vaccination management features that have been implemented in the OpenMRS FHIR2 module.

## Overview

The vaccination module provides comprehensive functionality for managing vaccination records, including CRUD operations, patient profile integration, reminders for upcoming vaccinations, and reporting features for vaccination coverage.

## Features Implemented

### 1. Vaccination Record Management (CRUD Operations)

The module already had basic CRUD operations implemented. These have been enhanced with:

- **Create**: Add new vaccination records via `POST /fhir2Servlet/Immunization`
- **Read**: Retrieve vaccination records via `GET /fhir2Servlet/Immunization/{id}`
- **Update**: Update existing vaccination records via `PUT /fhir2Servlet/Immunization/{id}`
- **Delete**: Remove vaccination records via `DELETE /fhir2Servlet/Immunization/{id}`
- **Search**: Search vaccinations with patient and date filters via `GET /fhir2Servlet/Immunization?patient={patientId}&date={dateRange}`

### 2. Integration with Patient Profile

**New Endpoint**: `$patient-immunizations` operation

- **URL**: `GET /fhir2Servlet/Immunization/$patient-immunizations?patient={patientUuid}`
- **Description**: Retrieves all immunizations for a specific patient
- **Parameters**:
  - `patient` (required): Patient UUID
- **Returns**: Bundle containing all immunization resources for the patient

**Example Request**:
```
GET /fhir2Servlet/Immunization/$patient-immunizations?patient=123e4567-e89b-12d3-a456-426614174000
```

### 3. Reminders for Upcoming Vaccinations

**New Endpoint**: `$upcoming-vaccinations` operation

- **URL**: `GET /fhir2Servlet/Immunization/$upcoming-vaccinations?days={days}&patient={patientUuid}`
- **Description**: Retrieves vaccinations that are due within the specified number of days
- **Parameters**:
  - `days` (optional): Number of days to look ahead (default: 30)
  - `patient` (optional): Patient UUID to filter by specific patient
- **Returns**: Bundle containing upcoming immunization resources

**Example Requests**:
```
# Get all vaccinations due in next 30 days
GET /fhir2Servlet/Immunization/$upcoming-vaccinations

# Get vaccinations due in next 7 days for a specific patient
GET /fhir2Servlet/Immunization/$upcoming-vaccinations?days=7&patient=123e4567-e89b-12d3-a456-426614174000
```

**New Endpoint**: `$missed-vaccinations` operation

- **URL**: `GET /fhir2Servlet/Immunization/$missed-vaccinations?patient={patientUuid}`
- **Description**: Retrieves vaccinations that are past their due date
- **Parameters**:
  - `patient` (optional): Patient UUID to filter by specific patient
- **Returns**: Bundle containing missed immunization resources

**Example Requests**:
```
# Get all missed vaccinations
GET /fhir2Servlet/Immunization/$missed-vaccinations

# Get missed vaccinations for a specific patient
GET /fhir2Servlet/Immunization/$missed-vaccinations?patient=123e4567-e89b-12d3-a456-426614174000
```

### 4. Reporting Feature for Vaccination Coverage

**New Endpoint**: `$vaccination-coverage` operation

- **URL**: `GET /fhir2Servlet/Immunization/$vaccination-coverage?startDate={date}&endDate={date}&patient={patientUuid}`
- **Description**: Generates vaccination coverage statistics and reports
- **Parameters**:
  - `startDate` (optional): Start date for coverage period
  - `endDate` (optional): End date for coverage period
  - `patient` (optional): Patient UUID to filter by specific patient
- **Returns**: Parameters resource containing:
  - `totalPatients`: Total number of patients
  - `vaccinatedPatients`: Number of patients who received vaccinations
  - `totalVaccinations`: Total number of vaccinations given
  - `coveragePercentage`: Percentage of patients vaccinated
  - `typeCoverage`: Array of coverage statistics by vaccine type:
    - `vaccineType`: Display name of the vaccine
    - `vaccineCode`: Code of the vaccine
    - `totalGiven`: Total number of this vaccine given
    - `patientsVaccinated`: Number of patients who received this vaccine

**Example Requests**:
```
# Get overall vaccination coverage
GET /fhir2Servlet/Immunization/$vaccination-coverage

# Get coverage for a specific date range
GET /fhir2Servlet/Immunization/$vaccination-coverage?startDate=2024-01-01&endDate=2024-12-31

# Get coverage for a specific patient
GET /fhir2Servlet/Immunization/$vaccination-coverage?patient=123e4567-e89b-12d3-a456-426614174000
```

## Implementation Details

### Service Layer Enhancements

The `FhirImmunizationService` interface has been extended with the following methods:

1. `searchImmunizations(ReferenceAndListParam, DateRangeParam, SortSpec)` - Enhanced search with date range support
2. `getImmunizationsByPatient(String)` - Get all immunizations for a patient
3. `getUpcomingVaccinations(int, String)` - Get upcoming vaccinations
4. `getMissedVaccinations(String)` - Get missed vaccinations
5. `getVaccinationCoverage(Date, Date, String)` - Get coverage statistics

### Data Models

Two new inner classes have been added to `FhirImmunizationService`:

- `VaccinationCoverageReport`: Contains overall coverage statistics
- `VaccinationTypeCoverage`: Contains per-vaccine-type coverage statistics

### Date Range Search Support

The search functionality now supports date range filtering using the `date` parameter, which allows filtering immunizations by their administration date.

## Technical Notes

1. **Reminder Logic**: The reminder functionality uses the `nextDoseDate` extension (FHIR extension: `http://hl7.eu/fhir/StructureDefinition/immunization-nextDoseDate`) to determine when the next dose is due.

2. **Coverage Calculation**: The coverage report calculates statistics based on:
   - Unique patients who have received vaccinations
   - Total number of vaccinations administered
   - Breakdown by vaccine type

3. **Patient Filtering**: All new endpoints support optional patient filtering to allow both system-wide and patient-specific queries.

## Usage Examples

### Adding a New Vaccination
```http
POST /fhir2Servlet/Immunization
Content-Type: application/json

{
  "resourceType": "Immunization",
  "status": "completed",
  "vaccineCode": {
    "coding": [{
      "system": "http://hl7.org/fhir/sid/cvx",
      "code": "01",
      "display": "DTP"
    }]
  },
  "patient": {
    "reference": "Patient/123e4567-e89b-12d3-a456-426614174000"
  },
  "occurrenceDateTime": "2024-01-15",
  "extension": [{
    "url": "http://hl7.eu/fhir/StructureDefinition/immunization-nextDoseDate",
    "valueDateTime": "2024-02-15"
  }]
}
```

### Getting Patient Vaccination History
```http
GET /fhir2Servlet/Immunization/$patient-immunizations?patient=123e4567-e89b-12d3-a456-426614174000
```

### Getting Upcoming Vaccinations
```http
GET /fhir2Servlet/Immunization/$upcoming-vaccinations?days=14
```

### Getting Coverage Report
```http
GET /fhir2Servlet/Immunization/$vaccination-coverage?startDate=2024-01-01&endDate=2024-12-31
```

## Files Modified

1. `api/src/main/java/org/openmrs/module/fhir2/api/FhirImmunizationService.java` - Added new service methods
2. `api/src/main/java/org/openmrs/module/fhir2/api/impl/FhirImmunizationServiceImpl.java` - Implemented new service methods
3. `api/src/main/java/org/openmrs/module/fhir2/providers/r4/ImmunizationFhirResourceProvider.java` - Added new REST endpoints

## Testing Recommendations

1. Test CRUD operations with various vaccination data
2. Test patient profile integration with multiple patients
3. Test reminder functionality with different date ranges
4. Test coverage reporting with various date ranges and filters
5. Test error handling for invalid parameters
6. Test performance with large datasets

## Future Enhancements

Potential future improvements:
- Scheduled reminder notifications
- Email/SMS integration for reminders
- Advanced reporting with charts and graphs
- Export functionality for coverage reports
- Integration with immunization schedules/calendars
- Support for vaccine lot tracking
- Adverse event reporting

