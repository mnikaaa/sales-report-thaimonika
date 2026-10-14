package edu.itc.salesreport.render;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import edu.itc.salesreport.model.MonthlyReport;

class ReportRendererTest {

    private static String text(ReportRenderer r, MonthlyReport rep) {
        return new String(r.render(rep), StandardCharsets.UTF_8);
    }

    @ParameterizedTest
    @ValueSource(strings = {"txt", "html", "csv"})
    void containsBranchesAndChainTotal(String ext) {
        String out = text(ReportRenderer.forExtension(ext), TestReports.september());
        for (String code : List.of("PNH", "REP", "BTB")) {
            assertTrue(out.contains(code), code);
        }
        assertTrue(out.contains("2300.50"));
    }

    @Test
    void csvHasExactLine() {
        String out = text(new CsvReportRenderer(), TestReports.september());
        assertTrue(out.contains("2026-09,REP,800.50,0.00,50,16.01"));
    }

    @Test
    void unknownExtensionThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> ReportRenderer.forExtension("pdf"));
    }

    @Test
    void htmlEscapesSpecialCharacters() {
        var r = new MonthlyReport(YearMonth.of(2026, 9),
                List.of(TestReports.branch("R&D<x>", "1.00", 1)),
                TestReports.branch("ALL", "1.00", 1), List.of());
        String out = text(new HtmlReportRenderer(), r);
        assertTrue(out.contains("R&amp;D&lt;x&gt;"));
    }

    @Test
    void exhaustiveSwitchWithoutDefault() {
        ReportRenderer r = ReportRenderer.forExtension("html");
        String name = switch (r) {
            case TextReportRenderer t -> "text";
            case HtmlReportRenderer h -> "html";
            case CsvReportRenderer c -> "csv";
        };
        assertEquals("html", name);
    }
}