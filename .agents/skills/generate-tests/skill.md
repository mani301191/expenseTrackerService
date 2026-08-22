---
name: generate-tests
description: Generate high-quality JUnit 5 and Mockito unit tests for Java and Spring Boot applications. Use when asked to create, improve, review, or increase unit test coverage. Inspect the production code and existing tests first, follow project conventions, generate meaningful behavioral tests, and run the tests to verify they pass.
---

# Generate Tests

Generate production-quality unit tests for Java and Spring Boot applications.

The primary goal is to create **meaningful, maintainable, reliable tests**, not merely to increase code coverage.

Always follow this workflow:

**Inspect → Understand → Generate → Run → Fix → Verify**

---

## 1. Inspect Before Generating

Before writing tests:

1. Locate the target production class.
2. Read the complete implementation.
3. Identify all dependencies and collaborators.
4. Locate existing tests for the same class.
5. Locate tests for similar classes in the same package/module.
6. Inspect the project build configuration:
    - `pom.xml`
    - `build.gradle`
    - `build.gradle.kts`
7. Determine:
    - JUnit version
    - Mockito version
    - Assertion library
    - Test naming conventions
    - Mocking conventions
    - Test setup conventions
    - Package structure

Do not generate tests based only on the class name.

Do not introduce a new testing style when the repository already has an established convention.

---

## 2. Understand the Production Code

Before generating tests, analyze:

### Inputs

- Required parameters
- Optional parameters
- Null handling
- Empty values
- Invalid values
- Boundary values

### Outputs

- Return values
- State changes
- Side effects

### Dependencies

Identify:

- Repositories
- Services
- REST clients
- Kafka components
- Mappers
- External APIs
- Configuration
- Utility classes

### Control Flow

Identify:

- `if/else`
- `switch`
- loops
- early returns
- conditional branches
- validation
- null handling
- `Optional`
- feature flags
- exception paths

### Exceptions

Identify:

- Explicitly thrown exceptions
- Business exceptions
- Validation exceptions
- Dependency exceptions
- Exception propagation
- Exception transformation
- Fallback behavior

---

## 3. Inspect Existing Tests

Before creating a new test class, search for:

- Tests for the same class
- Tests for the same service
- Tests for similar methods
- Similar Mockito setups
- Similar assertions
- Existing test helpers
- Existing fixtures/builders

Reuse existing project patterns wherever possible.

If an existing test class already exists, modify it instead of creating a duplicate test class.

Preserve existing working tests.

---

## 4. Choose the Correct Test Type

For normal unit testing, prefer:

- JUnit 5
- Mockito
- Pure Java unit tests

Do not automatically use:

- `@SpringBootTest`
- `@WebMvcTest`
- `@DataJpaTest`
- Testcontainers
- Embedded Kafka
- Real databases
- Real HTTP services

Use integration-test infrastructure only when:

1. The behavior genuinely requires integration testing, or
2. The project already has an established approach that requires it.

For service unit tests, prefer pure Mockito-based tests.

---

## 5. Generate Meaningful Test Scenarios

For every meaningful public method, consider the following.

### Happy Path

Test:

- Normal successful execution
- Correct return value
- Important state changes
- Important dependency interactions

### Negative Path

Where applicable, test:

- Invalid input
- Missing data
- Empty result
- Empty collection
- Validation failure
- Unsupported values

### Exception Path

Where applicable, test:

- Business exception
- Dependency exception
- Exception propagation
- Exception transformation
- Fallback behavior

### Boundary Cases

Only add relevant boundary tests:

- `null`
- Empty string
- Empty collection
- Zero
- Negative values
- Minimum values
- Maximum values
- Boundary dates
- Single-element collections

Do not create irrelevant tests simply to increase test count.

---

## 6. Mockito Rules

Prefer the project's existing Mockito style.

For a typical service:

```java
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;
}