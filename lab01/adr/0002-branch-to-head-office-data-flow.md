# ADR-0002: Branches push one CSV file per day by SFTP; partial reports allowed

Status: Accepted
Date: 2026-10-14 · Deciders: <Thai Monika>

## Context
Each POS exports one CSV per day, available by 23:00, and branch links fail now and then (QA-3). Head office needs the report on the morning of day 2. Managers must see only their own branch (QA-5). Branch IT capacity is small.

## Decision
Each branch uploads `BRANCH-yyyy-MM-dd.csv` by SFTP to the head-office landing zone. The ingest job validates the file and stores its SHA-256 checksum.

Failure policy (QA-3):
- Head office waits until 06:00 on day 2 of M+1 for missing files.
- At 06:00 it publishes a report for the branches whose files are complete, marked "PARTIAL: missing REP 2026-09-30".
- A late file triggers a re-run for that month, and the new report replaces the partial one.
- Idempotency: the key is file name plus SHA-256. The same key again is ignored. The same name with a different checksum replaces the old file and is never appended, so rows are never duplicated.

## Alternatives considered
- Head office pulls from each branch: central control of timing, but needs inbound access to branch networks and branch uptime at pull time.
- POS publishes one event per receipt to a broker: near-real-time with no batch window, but needs POS changes, an operated broker and about 3 M messages per month, which is too much for a monthly report.

## Consequences
+ QA-3: a failed link delays only one branch, and the partial report still meets the deadline.
+ Branches only upload one file and retry on failure, so their side stays simple.
+ Re-runs are safe and auditable through checksums.
- QA-3: the first published report can be incomplete and must be clearly marked.
- QA-5: per-branch SFTP credentials must be managed and rotated.
- No intraday data; reports are only as fresh as the last upload.

Revisit when: management needs intraday reports, or branches get reliable links and want streaming.