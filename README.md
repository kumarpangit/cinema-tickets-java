# Cinema Tickets Booking Service

A Java 21 Maven project for a cinema ticket booking system.

## Prerequisites

- Java 17 or later
- Maven

## Build

```bash
mvn clean compile
```

## Run Tests

```bash
mvn test
```

## Run a Single Test

```bash
mvn test -Dtest=TicketServiceImplTest#shouldThrowExceptionForInvalidAccountId
```

## Package

```bash
mvn package
```

## Project Structure

```
src/main/java/uk/gov/dwp/uc/pairtest/
├── TicketService.java          # Interface
├── TicketServiceImpl.java  # Implementation
├── TicketValidator.java   # Validation
├── domain/
│   └── TicketTypeRequest.java
└── exception/
    └── InvalidPurchaseException.java
```

## Ticket Types

- **ADULT**: £25
- **CHILD**: £15
- **INFANT**: Free (must be accompanied by an adult)

## Validation Rules

1. Account ID must be positive
2. At least one ticket required
3. Child/infant tickets require at least one adult ticket
4. Infants cannot exceed adults
5. Maximum 25 tickets per transaction
