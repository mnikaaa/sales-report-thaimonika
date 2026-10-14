# ADR-0001: Build release 1 as a modular monolith batch job

Status: Accepted
Date: 2026-10-14 · Deciders: <Thai Monika>

## Context
The report for month M is due on the morning of day 2 of M+1 (brief). Architectural drivers from the quality scenarios: QA-1 (3 M rows in 60 s or less), QA-3 (partial report when a branch file is late), QA-5 (manager sees own branch only). The team is one developer with about 8 weeks, using Java 25. About 3 M rows per month is a small batch for a single machine.

## Decision
We will implement ingest, engine and renderers as packages of one Java application, run as a batch job in a single JVM, with parsing parallelised across cores. Packages talk through in-process calls and the canonical model records. Package boundaries are guarded by DependencyRulesTest.

## Alternatives considered
- Process pipeline (coordinator and worker JVMs): scales beyond one machine, but adds IPC, serialization and process management that 3 M rows do not need.
- Event-driven microservices with a message broker: best isolation and buffers late files, but one event per row means about 60 s of broker time alone (QA-1) and an operated broker is too much for one developer.

## Consequences
+ QA-1: no network or IPC hop, so the run stays well inside 60 s.
+ QA-6: the engine is testable in-process with plain records.
+ One artifact to build, deploy and debug, which is cheap to operate.
- QA-2: scaling is vertical only; a tenfold volume would need a redesign.
- QA-3: a crash in one stage stops the whole run, so ingest must checkpoint per file.
- Package boundaries rely on discipline and the dependency test, not on process isolation.

Revisit when: a month exceeds 10 M rows, or a run takes longer than 30 s on the target server.