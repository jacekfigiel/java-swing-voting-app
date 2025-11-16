# Java Swing Voting App

Simple desktop voting application implemented in Java Swing as a recruitment task.

## Features

- Add candidates with auto–incremented IDs.
- Add voters with auto–incremented IDs.
- Display candidates in a table with columns: **ID / Name / Votes**.
- Display voters in a table with columns: **ID / Name / Has voted**.
- Select candidate and voter from dropdowns (combo boxes) to cast a vote.
- Prevent multiple votes from the same voter (voter has a `hasVoted` flag).
- Automatically update tables and dropdowns after each operation.
- Reset election (clear all votes and voting flags) with a single button.

## Architecture

- Domain model: `Candidate`, `Voter`.
- Repositories: `CandidateRepository`, `VoterRepository` with in‑memory implementations.
- Service layer: `VotingService` with business logic (casting votes, reset).
- UI layer: `VotingAppGUI` using Swing (`JTable`, `JComboBox`, `JPanel`, etc.).

This separation makes it easier to extend the application (e.g. replace in‑memory storage with a database or add new validation rules).

## How to run

1. Make sure you have **JDK 17** (or 11+) installed.
2. Clone the repository:
