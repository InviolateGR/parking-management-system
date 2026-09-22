# Multi-Level Parking Management System

A console-based parking management system built in Java, demonstrating strong object-oriented design, SOLID principles, and classic design patterns in a realistic, non-trivial domain.

Unlike typical beginner CRUD projects, this system models the real operational complexity of a multi-floor parking facility — vehicle allocation, slot compatibility, reservations, dynamic pricing, real-time occupancy notifications, and analytics — using a clean, layered architecture with a persistence layer designed to be swapped without touching business logic.

## Why This Project

Most academic/resume projects (student management, library systems) are simple data-entry CRUD apps that don't demonstrate architectural thinking. This project was deliberately scoped to require:

- Real polymorphism and abstraction, not just getters/setters
- Multiple design patterns used for genuine reasons, not shoehorned in
- A persistence layer that was actually migrated mid-project — from flat files to SQLite — with zero changes to business logic, proving the Dependency Inversion design was real, not theoretical
- Proper exception handling with a custom exception hierarchy
- Structured logging throughout, not `System.out.println` debugging

## Tech Stack

- **Language**: Java 17
- **Build tool**: Maven
- **Logging**: SLF4J + Logback (console + rolling file appenders)
- **Persistence**: SQLite via JDBC (migrated from an initial file-based implementation, both of which still exist in the codebase)
- **Console I/O**: `java.util.Scanner`-based interactive menu

## Architecture Overview
com.gowshick.parking
├── model/ → Core domain entities (Vehicle, ParkingSlot, ParkingFloor, ParkingLot, Ticket, Bill, Reservation)
├── enums/ → Type-safe vocabulary (VehicleType, SlotType, SlotStatus, PaymentMethod)
├── state/ → State pattern — slot lifecycle (Free ↔ Occupied ↔ Reserved)
├── strategy/ → Strategy pattern — per-vehicle-type pricing algorithms
├── observer/ → Observer pattern — real-time occupancy notifications
├── factory/ → Factory pattern — vehicle instantiation
├── service/ → Business logic orchestration (ParkingService, ReservationService, AnalyticsService)
├── repository/ → Persistence interfaces, decoupled from storage implementation
│ ├── file/ → File-based implementation (initial version)
│ └── sqlite/ → SQLite implementation (current, via JDBC)
├── exception/ → Custom exception hierarchy for domain-specific error handling
└── util/ → Shared utilities (ID generation)


**Design principle behind this structure**: dependencies point inward. `model/` has zero knowledge of `service/` or `repository/`. Business logic (`service/`) depends only on repository *interfaces* (`TicketRepository`, `BillRepository`, `ReservationRepository`), never on concrete implementations. This is what made the SQLite migration possible with a one-file change in wiring (`App.java`) and zero changes to `ParkingService`, `ReservationService`, or `AnalyticsService`.

## Design Patterns Used

| Pattern | Where | Why |
|---|---|---|
| **State** | `ParkingSlot` delegates behavior to `SlotState` (`FreeState`, `OccupiedState`, `ReservedState`) | Avoids scattered `if/else` on status; each state encapsulates its own valid transitions, including a dedicated `release()` path for expired reservations |
| **Strategy** | `PricingStrategy` implementations per vehicle type | Different vehicle types need genuinely different fare algorithms — trucks use a daily cap, cars/bikes use hourly tiers — not just different constants |
| **Observer** | `ParkingFloor` notifies registered `ParkingObserver`s (`DisplayBoard`, `AdminNotifier`) | Real-time updates on occupancy changes without polling; fires correctly for every state transition, including reservation creation |
| **Factory** | `VehicleFactory` | Centralizes vehicle instantiation; client code depends only on the `Vehicle` abstraction |
| **Singleton** | `ParkingLot` | Exactly one facility instance per running application |

## SOLID Principles in Practice

- **Single Responsibility**: `ParkingService` orchestrates; it doesn't know *how* fares are calculated or *how* tickets are persisted.
- **Open/Closed**: Adding a new vehicle type means adding one class to `factory/` and one to `strategy/` — no existing code is modified.
- **Liskov Substitution**: Any `Vehicle` subtype (`Bike`, `Car`, `Truck`) can be used wherever `Vehicle` is expected.
- **Interface Segregation**: `SlotState`, `PricingStrategy`, `ParkingObserver`, and repository interfaces each expose only the methods relevant to their specific role.
- **Dependency Inversion**: `ParkingService`, `ReservationService`, and `AnalyticsService` depend on repository *interfaces*, never on `FileTicketRepository` or `SQLiteTicketRepository` directly. This is proven, not just claimed — the persistence layer was fully migrated from files to SQLite mid-project with no changes to these classes.

## Features

- **Vehicle allocation** — nearest compatible slot found automatically based on vehicle size (bikes fit any slot; cars need medium/large; trucks need large)
- **Multi-floor support** — configurable floors, each with independently tracked occupancy
- **Reservations** — reserve a slot ahead of time; a 30-minute grace period auto-releases no-shows back to the pool
- **Dynamic billing** — per-vehicle-type pricing (flat first-hour + hourly rate for cars/bikes, daily-capped hourly rate for trucks)
- **Real-time notifications** — console-logged observer updates on every slot state change
- **Analytics** — total vehicles parked, total revenue, average parking duration, and peak-occupancy floor, all derived live from persisted data (no separate, driftable analytics store)
- **Persistent storage** — SQLite database, survives application restarts
- **Interactive console menu** — park, exit, reserve, view floor status, view analytics, all through a guided menu with input validation and friendly error handling

## Running the Project

**Prerequisites**: JDK 17+, Maven 3.6+

```bash
mvn clean compile
mvn exec:java
```

This launches an interactive menu:

===== Parking Management System =====

Park a vehicle
Exit a vehicle
Reserve a slot
View floor status
View analytics summary
Quit

Data persists in `data/parking.db` (SQLite) across restarts — analytics will reflect cumulative history, not just the current session.

## Sample Output

Choose an option: 1
Enter vehicle type (BIKE/CAR/TRUCK): CAR
Enter registration number: KA01AB1234
INFO DisplayBoard - Slot F1-S2 on floor 1 is now OccupiedState
INFO ParkingService - Vehicle parked successfully: Ticket[TICKET-5B941C4D, vehicle=CAR [KA01AB1234], slot=F1-S2, entry=2026-09-22T11:44:28]
Vehicle parked successfully!

Choose an option: 5
INFO AnalyticsService - === Analytics Summary ===
INFO AnalyticsService - Total vehicles parked: 3
INFO AnalyticsService - Total revenue: 20.0
INFO AnalyticsService - Vehicle count by floor: {1=3}
INFO AnalyticsService - Peak floor: 1


## Known Limitations & Possible Extensions

Being upfront about scope boundaries — these were deliberate decisions, not oversights:

- **Floor/slot layout is hardcoded at startup**, not persisted or configurable via file. Tickets, bills, and reservations persist across restarts; the physical layout does not yet.
- **No unit test suite yet** — the codebase was designed for testability (constructor-injected dependencies throughout), but tests haven't been written. This is a natural next step given the current architecture.
- **No concurrency handling** — a real parking garage has simultaneous entries; this system assumes single-threaded, sequential console interaction. `ParkingLot`'s Singleton uses synchronized lazy initialization, but slot allocation itself isn't guarded against concurrent access.
- **Vehicle registration numbers aren't format-validated** — only checked for null/blank, not matched against a real-world plate format.
- **File-based repositories (`repository/file/`) are kept in the codebase intentionally** alongside the SQLite ones, to visibly demonstrate the migration — in a production codebase, the unused implementation would typically be removed.

## Author

Gowshick — B.E. Electronics and Communication Engineering graduate, building toward Software Engineer / Java Developer roles.

