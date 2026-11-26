package org.mhh.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WriteQuotaTest {
    @Test void isolatesClientsAndResetsAtNextMinute() {
        WriteQuota quota = new WriteQuota(1);
        assertEquals(0, quota.retryAfter("first", 65));
        assertEquals(55, quota.retryAfter("first", 65));
        assertEquals(0, quota.retryAfter("second", 65));
        assertEquals(0, quota.retryAfter("first", 120));
    }
}
