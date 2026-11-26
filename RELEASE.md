# Single-instance release plan

The current deployment target is one application process with persistent H2
storage. The following are release gates, in dependency order.

| Next change | Acceptance check |
| --- | --- |
| Version the database schema | Back up a previous release, migrate a copy with real data, verify redirects and counts, then replace automatic schema updates with validation. |
| Automate builds and API compatibility checks | CI on Windows runs the full Maven suite (including recovery); publish a versioned artifact only on success. |
| Add operational metrics | Measure redirect latency, 4xx/5xx rates, throttled writes, and pending reports without URL, token or client identifiers as metric labels. Exercise alerts in staging. |
| Establish production operations | Configure TLS, trusted ingress, moderator secret rotation, off-host encrypted backups, retention and a recurring restore drill. Record recovery time and accepted data loss. |
| Introduce account-based moderation if needed | Replace the shared token with roles and record the responsible moderator in a persistent audit history. |
| Add owner workflows and analytics if needed | Authenticated owners can list their own links and update expiry; support aggregate counts by day under a defined retention policy. |

Multi-instance deployment is a separate milestone: move both quota enforcement
and persistence to shared services and repeat the concurrency and failure tests.

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
