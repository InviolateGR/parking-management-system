# Multi-Level Parking Management System

A console-based parking management system built in Java, designed to demonstrate strong object-oriented design, SOLID principles, and classic design patterns in a realistic, non-trivial domain.

Unlike typical beginner CRUD projects, this system models the real operational complexity of a multi-floor parking facility — vehicle allocation, slot compatibility, reservations, dynamic pricing, and real-time occupancy notifications — using a clean, layered architecture.

## Why This Project

Most academic/resume projects (student management, library systems) are simple data-entry CRUD apps that don't demonstrate architectural thinking. This project was deliberately scoped to require:

- Real polymorphism and abstraction (not just getters/setters)
- Multiple design patterns used for genuine reasons, not shoehorned in
- A persistence layer designed to be swapped (file-based → SQLite) without touching business logic
- Proper exception handling and structured logging throughout

## Tech Stack

- **Language**: Java 17
- **Build tool**: Maven
- **Logging**: SLF4J + Logback (rolling file + console appenders)
- **Persistence**: File-based (current) → SQLite via JDBC (planned)
- **Testing**: JUnit (planned)

## Architecture Overview

The codebase is organized by architectural role rather than dumping all classes into one package:

com.gowshick.parking
├── model/ → Core domain entities (Vehicle, ParkingSlot, ParkingFloor, ParkingLot, Ticket, Bill, Reservation)
├── enums/ → Type-safe vocabulary (VehicleType, SlotType, SlotStatus, PaymentMethod)
├── state/ → State pattern — slot lifecycle (Free → Occupied / Reserved)
├── strategy/ → Strategy pattern — per-vehicle-type pricing algorithms
├── observer/ → Observer pattern — real-time occupancy notifications
├── factory/ → Factory pattern — vehicle instantiation
├── service/ → Business logic orchestration (ParkingService, and upcoming ReservationService/AnalyticsService)
├── repository/ → Persistence interfaces, decoupled from storage implementation
│ └── file/ → Current file-based implementation
├── exception/ → Custom exception hierarchy for domain-specific error handling
└── util/ → Shared utilities (ID generation)



**Design principle behind this structure**: dependencies point inward. `model/` has zero knowledge of `service/` or `repository/`. Business logic (`service/`) depends only on repository *interfaces*, never concrete implementations — this is what allows the underlying storage mechanism to change without touching business logic.

## Design Patterns Used

| Pattern | Where | Why |
|---|---|---|
| **State** | `ParkingSlot` delegates behavior to `SlotState` (`FreeState`, `OccupiedState`, `ReservedState`) | Avoids scattered `if/else` on status; each state encapsulates its own valid transitions |
| **Strategy** | `PricingStrategy` implementations per vehicle type | Different vehicle types need genuinely different fare algorithms (not just different constants) — trucks use a daily cap, cars/bikes use hourly tiers |
| **Observer** | `ParkingFloor` notifies registered `ParkingObserver`s (`DisplayBoard`, `AdminNotifier`) | Real-time updates on occupancy changes without polling |
| **Factory** | `VehicleFactory` | Centralizes vehicle instantiation; client code depends only on the `Vehicle` abstraction |
| **Singleton** | `ParkingLot` | Exactly one facility instance per running application |

## SOLID Principles in Practice

- **Single Responsibility**: `ParkingService` orchestrates; it doesn't know *how* fares are calculated or *how* tickets are persisted — those concerns live elsewhere.
- **Open/Closed**: Adding a new vehicle type means adding one class to `factory/` and one to `strategy/` — no existing code is modified.
- **Liskov Substitution**: Any `Vehicle` subtype (`Bike`, `Car`, `Truck`) can be used wherever `Vehicle` is expected.
- **Interface Segregation**: `SlotState`, `PricingStrategy`, `ParkingObserver`, and repository interfaces each expose only the methods relevant to their specific role.
- **Dependency Inversion**: `ParkingService` depends on `TicketRepository`/`BillRepository` interfaces, never on `FileTicketRepository` directly — enabling the planned SQLite migration with zero changes to business logic.

## Current Status

✅ Core domain model (Vehicle hierarchy, ParkingSlot, ParkingFloor, ParkingLot)
✅ State pattern (slot lifecycle)
✅ Observer pattern (real-time notifications)
✅ Strategy pattern (per-vehicle pricing)
✅ Factory pattern (vehicle creation)
✅ File-based persistence for tickets and bills
✅ Structured logging (console + rolling file)
✅ End-to-end park → exit flow verified

🔶 In progress:
- Reservation workflow (`ReservationService`, wiring `ReservedState` into active use)
- Analytics module (revenue, occupancy trends, derived from persisted data)
- Interactive console menu (currently a hardcoded demo flow in `App.java`)
- SQLite persistence layer (swapping file-based repositories via the existing interface, no business logic changes required)
- Unit test coverage

## Running the Project

**Prerequisites**: JDK 17+, Maven 3.6+

```bash
mvn clean compile
mvn exec:java
```

This currently runs a demonstration flow: initializes a 2-floor parking lot, parks a car, and immediately processes its exit — printing the issued ticket and generated bill, with full log output to both console and `logs/parking-system.log`.

## Sample Output

16:11:01 INFO DisplayBoard - Slot F1-S2 on floor 1 is now OccupiedState
16:11:01 INFO AdminNotifier - Alert: Slot F1-S2 on floor 1 changed status to OccupiedState
16:11:01 INFO ParkingService - Vehicle parked successfully: Ticket[TICKET-235A768A, vehicle=CAR [KA01AB1234], slot=F1-S2, entry=2026-08-20T16:11:01]
16:11:01 INFO ParkingService - Vehicle exited successfully: Bill[BILL-801AF967, ticket=TICKET-235A768A, amount=50.0, exit=2026-08-20T16:11:01, payment=UPI]


## Author

Gowshick — B.E. Electronics and Communication Engineering graduate, building toward Software Engineer / Java Developer roles.