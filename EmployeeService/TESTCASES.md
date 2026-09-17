# EmployeeService - Test Cases & Expected Outputs

Project: `/mnt/E/microservices/EmployeeService` (Spring Boot 4.1.1, Java 21, Spring Data JPA + MySQL/H2, MapStruct, Eureka client).

## 1. Service Layer (`EmployeeServiceImplTest`)

Service depends on `EmployeeRepository` and `EmployeeMapper` (both mocked). All query/CRUD flows and exception paths are covered.

| # | Method / Test Case | Input / Stub | Expected Output / Behaviour |
|---|--------------------|--------------|------------------------------|
| 1 | `createEmployee` success | email not taken, repo.save returns entity | returns mapped `EmployeeResponseDTO`; `existsByEmpEmail`, `save`, `toResponse` invoked |
| 2 | `createEmployee` duplicate email | `existsByEmpEmail` returns `true` | throws `EmailAlreadyExistsException` with message = email; `save` never called |
| 3 | `getEmployeeById` found | `findById` returns entity | returns mapped DTO |
| 4 | `getEmployeeById` missing | `findById` empty | throws `EmployeeNotFoundException` ("Employee not found") |
| 5 | `getEmployeeByEmail` found | `findByEmpEmail` returns entity | returns mapped DTO |
| 6 | `getEmployeeByEmail` missing | `findByEmpEmail` empty | throws `EmployeeNotFoundException` |
| 7 | `getAllEmployees` empty DB | `findAll` empty | returns empty list |
| 8 | `getAllEmployees` populated | `findAll` 2 entities | returns list of 2 DTOs |
| 9 | `getEmployeesByDepartment` matches | repo returns entities | returns mapped list |
| 10 | `getEmployeesByDepartment` no matches | repo returns empty | returns empty list |
| 11 | `getEmployeesByCompany` matches | repo returns entities | returns mapped list |
| 12 | `getEmployeesByCompany` no matches | repo returns empty | returns empty list |
| 13 | `getEmployeesByStatus` matches | repo returns entities | returns mapped list |
| 14 | `getEmployeesByStatus` no matches | repo returns empty | returns empty list |
| 15 | `getEmployeesByDepartmentAndStatus` matches | repo returns entities | returns mapped list |
| 16 | `getEmployeesByDepartmentAndStatus` no matches | repo returns empty | returns empty list |
| 17 | `existsById` existing | repo returns `true` | returns `true` |
| 18 | `existsById` missing | repo returns `false` | returns `false` |
| 19 | `updateEmployee` success (same email) | `findById` present, email unchanged | `updateEntityFromDto` + `save` called; returns mapped DTO; `existsByEmpEmail` NOT called |
| 20 | `updateEmployee` email taken by another | email differs, `existsByEmpEmail` true | throws `EmailAlreadyExistsException`; `save` never called |
| 21 | `updateEmployee` missing | `findById` empty | throws `EmployeeNotFoundException` |
| 22 | `updateEmployeeStatus` success | `findById` present | entity status set, saved; returned DTO has new status |
| 23 | `updateEmployeeStatus` missing | `findById` empty | throws `EmployeeNotFoundException` |
| 24 | `deleteEmployee` existing | `existsById` true | `deleteById` invoked; returns normally |
| 25 | `deleteEmployee` missing | `existsById` false | throws `EmployeeNotFoundException`; `deleteById` never called |

## 2. Controller / Web Layer (`employeeControllerTest`, `@WebMvcTest` + mocked service)

### POST `/employee`
| # | Test Case | Input | Expected HTTP + Body |
|---|-----------|-------|----------------------|
| 26 | Valid create | full valid JSON | `201 Created`; body = created employee JSON (id, empName, empEmail, department, companyName, status) |
| 27 | Duplicate email | service throws `EmailAlreadyExistsException` | `400 Bad Request`; body `{"message":"<email>","status":"400 BAD_REQUEST","timestamp":...}` |
| 28 | Blank required field | `empName`/`designation`/`department`/`companyName` = `"  "` | `400 Bad Request` (validation) |
| 29 | Invalid email | `empEmail: "not-an-email"` | `400 Bad Request` |
| 30 | Missing status | status omitted | `400 Bad Request` |
| 31 | Invalid enum | `status: "BOGUS"` | `400 Bad Request` (`HttpMessageNotReadableException`) |
| 32 | Empty body | `{}` | `400 Bad Request` |

### GET `/employee/{id}`
| # | Test case | Expected |
|---|-----------|----------|
| 33 | Existing id `1` | `200 OK`, employee JSON |
| 34 | Missing id `99` | `404 Not Found`, `{"message":"Employee not found","status":"404 NOT_FOUND",...}` |
| 35 | Non-numeric id `not-a-number` | `400 Bad Request` (type mismatch) |

### GET `/employee/email/{empEmail}`
| # | Test case | Expected |
|---|-----------|----------|
| 36 | Existing email | `200 OK`, employee JSON |
| 37 | Missing email | `404 Not Found`, ErrorResponse with message |

### GET `/employee`
| # | Test case | Expected |
|---|-----------|----------|
| 38 | Employees exist (2) | `200 OK`, JSON array of size 2 |
| 39 | No employees | `200 OK`, `[]` |

### GET `/employee/department/{department}`
| # | Test case | Expected |
|---|-----------|----------|
| 40 | Matches | `200 OK`, JSON array |
| 41 | No matches | `200 OK`, `[]` |

### GET `/employee/company/{companyName}`
| # | Test case | Expected |
|---|-----------|----------|
| 42 | Matches | `200 OK`, JSON array |
| 43 | No matches | `200 OK`, `[]` |

### GET `/employee/status/{status}`
| # | Test case | Expected |
|---|-----------|----------|
| 44 | `ACTIVE` | `200 OK`, JSON array |
| 45 | `BOGUS` (invalid enum) | `400 Bad Request` |
| 46 | `active` (lowercase; enum binding is case-sensitive) | `400 Bad Request` |

### GET `/employee/department/{department}/status/{status}`
| # | Test case | Expected |
|---|-----------|----------|
| 47 | `Engineering` + `ACTIVE` | `200 OK`, JSON array |
| 48 | Invalid status segment | `400 Bad Request` |

### GET `/employee/exists/{id}`
| # | Test case | Expected |
|---|-----------|----------|
| 49 | Existing id | `200 OK`, body `true` |
| 50 | Missing id | `200 OK`, body `false` |

### PUT `/employee/{id}`
| # | Test case | Expected |
|---|-----------|----------|
| 51 | Valid body | `200 OK`, updated employee JSON |
| 52 | Blank field | `400 Bad Request` |
| 53 | Email taken by another | `400 Bad Request`, ErrorResponse message = email |
| 54 | Missing id | `404 Not Found`, ErrorResponse |

### PATCH `/employee/{id}/status?status=...`
| # | Test case | Expected |
|---|-----------|----------|
| 55 | `ON_LEAVE` param | `200 OK`, employee JSON with `status:"ON_LEAVE"` |
| 56 | Missing `status` param | `400 Bad Request` (missing request param) |
| 57 | Invalid status value | `400 Bad Request` |
| 58 | Missing id | `404 Not Found`, ErrorResponse |

### DELETE `/employee/{id}`
| # | Test case | Expected |
|---|-----------|----------|
| 59 | Existing id | `204 No Content`, empty body; `deleteEmployee` invoked |
| 60 | Missing id | `404 Not Found`, ErrorResponse |

## 3. Mapper Layer (`EmployeeMapperTest`)
| # | Test case | Expected |
|---|-----------|----------|
| 61 | `toEntity(dto)` | entity fields populated; `department → empDepartment` mapped (bug fix verified); `id/createdAt/updatedAt` null |
| 62 | `toResponse(entity)` | DTO fields populated; `empDepartment → department` mapped |
| 63 | `toResponseList(list)` | list of 2 entities → 2 DTOs |
| 64 | `updateEntityFromDto(dto, existing)` | name/email/designation/department/company/status overwritten; `id/createdAt` preserved |

## 4. Application Context (`EmployeeServiceApplicationTests.contextLoads`)
| # | Test case | Expected |
|---|-----------|----------|
| 65 | Full Spring context loads | no exceptions; H2 (test scope) + Eureka disabled in `src/test/resources/application.properties` |
| 66 | `updateEmployeeStatus` freshness regression | `saveAndFlush` used; returned `updatedAt` reflects the refresh and differs from `createdAt` |

## Bugs found & fixed while testing
1. `EmployeeMapper` did not map DTO `department` ↔ entity `empDepartment` (name mismatch). Department was silently lost on create/update/read. Fixed with explicit `@Mapping` in `EmployeeMapper.java:15-24`.
2. `@SpringBootTest` could not start: no datasource (MySQL) configured and Eureka client enabled. Fixed by adding H2 (test scope) in `pom.xml` and disabling Eureka in `src/test/resources/application.properties`.
3. `updateEmployee` / `updateEmployeeStatus` used `save()`, which returns the entity **before** the flush triggers `@PreUpdate`, so the API response contained a stale `updatedAt` even though the DB row was correct. Fixed in `EmployeeServiceImpl.java` by using `saveAndFlush(existing)`; verified live (PATCH/PUT responses now return a fresh `updatedAt ≠ createdAt`).