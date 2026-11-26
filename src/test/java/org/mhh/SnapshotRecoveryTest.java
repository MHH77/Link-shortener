package org.mhh;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.sql.DriverManager;
import static org.junit.jupiter.api.Assertions.*;

@EnabledOnOs(OS.WINDOWS)
class SnapshotRecoveryTest {
    @TempDir Path directory;

    @Test void restoresClosedH2DatabaseAndRejectsCorruptOrExistingTarget() throws Exception {
        try (var db = DriverManager.getConnection("jdbc:h2:file:" + directory.resolve("live"), "sa", "");
             var statement = db.createStatement()) {
            statement.execute("create table recovery_check (clicks integer)");
            statement.execute("insert into recovery_check values (17)");
        }
        String digest = copy("Backup", "live.mv.db", "backup.mv.db", null, 0);
        copy("Restore", "backup.mv.db", "rejected.mv.db", "0".repeat(64), 1);
        assertFalse(directory.resolve("rejected.mv.db").toFile().exists());
        copy("Restore", "backup.mv.db", "restored.mv.db", digest, 0);
        copy("Restore", "backup.mv.db", "restored.mv.db", digest, 1);
        try (var db = DriverManager.getConnection("jdbc:h2:file:" + directory.resolve("restored") + ";IFEXISTS=TRUE", "sa", "");
             var statement = db.createStatement(); var result = statement.executeQuery("select clicks from recovery_check")) {
            assertTrue(result.next());
            assertEquals(17, result.getInt(1));
        }
    }

    private String copy(String mode, String source, String destination, String digest, int expected) throws Exception {
        var builder = new ProcessBuilder("powershell", "-NoProfile", "-NonInteractive", "-Command",
                "$ErrorActionPreference='Stop'; ( & ./ops/Copy-H2Snapshot.ps1 -Mode $env:SNAP_MODE " +
                "-Source $env:SNAP_SOURCE -Destination $env:SNAP_DEST " +
                (digest == null ? "" : "-ExpectedSha256 $env:SNAP_HASH") + " ).SHA256");
        builder.environment().put("SNAP_MODE", mode);
        builder.environment().put("SNAP_SOURCE", directory.resolve(source).toString());
        builder.environment().put("SNAP_DEST", directory.resolve(destination).toString());
        if (digest != null) builder.environment().put("SNAP_HASH", digest);
        var process = builder.redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes()).trim();
        assertEquals(expected, process.waitFor(), output);
        return output;
    }
}
