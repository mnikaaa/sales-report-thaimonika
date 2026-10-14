# C4 notes

1. **Not shown:** classes and packages, deployment hosts, error paths (quarantine of bad files), and authentication details; the diagrams stay at system and container level. The finance director has no box of their own because they are reached through the e-mail arrow.
2. **Containers and scenarios:** the ingest job and landing zone serve QA-3 (a late file is handled per file with a checksum, so other branches are not blocked); the web report page serves QA-5 (it enforces own-branch-only access); the report batch serves QA-1 (3 M rows in 60 s or less).
3. **Open question for the client:** how late may a branch file arrive before head office must publish a partial report?