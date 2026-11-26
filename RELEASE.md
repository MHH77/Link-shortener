# Single-instance release plan

The current deployment target is one application process with persistent H2
storage. The following are release gates, in dependency order.

| Next change | Acceptance check |
| --- | --- |
| Rehearse the upgrade on deployment data | Restore a backup to a separate directory and follow the migration procedure below. |
| Verify hosted CI | The Windows workflow must pass on GitHub before releasing; download the JAR from the successful run for the chosen commit. |
| Add operational metrics | Measure redirect latency, 4xx/5xx rates, throttled writes, and pending reports without URL, token or client identifiers as metric labels. Exercise alerts in staging. |
| Establish production operations | Configure TLS, trusted ingress, moderator secret rotation, off-host encrypted backups, retention and a recurring restore drill. Record recovery time and accepted data loss. |
| Introduce account-based moderation if needed | Replace the shared token with roles and record the responsible moderator in a persistent audit history. |
| Add owner workflows and analytics if needed | Authenticated owners can list their own links and update expiry; support aggregate counts by day under a defined retention policy. |

Multi-instance deployment is a separate milestone: move both quota enforcement
and persistence to shared services and repeat the concurrency and failure tests.

To reproduce CI locally on Windows with Java 17 and Maven, run
`mvn clean verify` followed by `.\ops\Test-PackagedApp.ps1`. The smoke test
starts the executable JAR on a temporary loopback port with in-memory H2.
It checks health, creates a link, follows the local short path without visiting
the destination, verifies one click, and stops its Java process.

## Adopting an existing H2 database

New installations run Flyway automatically. Hibernate validates the result on
startup. Existing installations without Flyway history deliberately refuse
to start until an operator approves adoption:

1. Stop the old application, take a backup, and restore a rehearsal copy.
2. Verify the copy has the expected legacy URL table (identity primary key,
   unique short code, destination, expiration, click count). Check for duplicate
   destinations with `SELECT original_url, COUNT(*) FROM url GROUP BY original_url HAVING COUNT(*) > 1`.
   Resolve any duplicates explicitly before upgrading; no migration deletes links.
3. Point the new application at the copy with `IFEXISTS=TRUE` in its JDBC URL.
   Set `SPRING_FLYWAY_BASELINEONMIGRATE=true` **only for the first start**.
   Version 1 represents the legacy table. Subsequent migrations retain existing
   tokens, moderation state and counts. Remove the environment override.
4. Restart, verify a known destination, redirect, click count and moderation
   state. Repeat on production only after the rehearsal passes. If a migration
   fails, stop and restore the backup before retrying on a fresh copy.

## Offline H2 recovery on Windows

1. Stop the application cleanly. Prepare an existing destination directory
   outside the repository on protected storage. Use a fresh filename each time.
2. Run the following from the repository root. Store the returned SHA256
   separately from the snapshot.

   ```powershell
   .\ops\Copy-H2Snapshot.ps1 -Mode Backup -Source .\data\link-shortener.mv.db -Destination D:\backups\links-001.mv.db
   ```

3. Restore to a fresh, empty recovery directory using the recorded digest.

   ```powershell
   .\ops\Copy-H2Snapshot.ps1 -Mode Restore -Source D:\backups\links-001.mv.db -Destination D:\recovery\link-shortener.mv.db -ExpectedSha256 $recordedDigest
   ```

4. Start an isolated instance of the same application/H2 version with
   `SPRING_DATASOURCE_URL=jdbc:h2:file:D:/recovery/link-shortener;IFEXISTS=TRUE`.
   Verify a known link, its details, and a controlled redirect before switching
   production traffic. Keep the original database until recovery is accepted.
