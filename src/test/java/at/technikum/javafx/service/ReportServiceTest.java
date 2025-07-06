package at.technikum.javafx.service;

import at.technikum.javafx.entity.TourLog;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReportServiceHelperTest {

    private static ReportService svc;
    private static Method safeParse;
    private static Method formatDur;
    private static Method computeCF;

    @BeforeAll
    static void init() throws Exception {
        svc = new ReportService(null);

        Class<?> cls = ReportService.class;
        safeParse = cls.getDeclaredMethod("safeParseSeconds", String.class);
        formatDur = cls.getDeclaredMethod("formatDuration", long.class);
        computeCF = cls.getDeclaredMethod("computeChildFriendliness", List.class);

        safeParse.setAccessible(true);
        formatDur.setAccessible(true);
        computeCF.setAccessible(true);
    }

    @Test
    void safeParseSeconds_validAndInvalid() throws Exception {
        assertEquals(0L, safeParse.invoke(svc, "00:00:00"));
        assertEquals(3661L, safeParse.invoke(svc, "01:01:01"));
        assertEquals(0L, safeParse.invoke(svc, "bad"));
        assertEquals(0L, safeParse.invoke(svc, (Object) null));
    }

    @Test
    void formatDuration_minutesAndHours() throws Exception {
        assertEquals("0 m", formatDur.invoke(svc, 0L));
        assertEquals("0 m", formatDur.invoke(svc, 59L));
        assertEquals("1 h 1 m", formatDur.invoke(svc, 3660L));
    }

    @Test
    void computeChildFriendliness_edgeCases() throws Exception {
        double cf0 = (double) computeCF.invoke(svc, List.of());
        assertEquals(0.0, cf0, 1e-6);

        TourLog log = new TourLog();
        log.setDifficulty("3");
        log.setTotalTime("00:00:00");
        log.setTotalDistance(0.0);
        double cf1 = (double) computeCF.invoke(svc, List.of(log));
        assertTrue(cf1 > 0.8 && cf1 < 0.9);
    }
}