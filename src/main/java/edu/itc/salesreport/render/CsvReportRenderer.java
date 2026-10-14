package edu.itc.salesreport.render;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.MonthlyReport;

public final class CsvReportRenderer implements ReportRenderer {

    @Override
    public String extension() {
        return "csv";
    }

    @Override
    public byte[] render(MonthlyReport report) {
        var sb = new StringBuilder(
                "month,branch,revenue,discounts,receipts,average_basket\n");
        for (BranchSummary b : report.branches()) {
            line(sb, report.month(), b);
        }
        line(sb, report.month(), report.chain());
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void line(StringBuilder sb, YearMonth m, BranchSummary b) {
        sb.append(String.join(",", m.toString(), b.branch(),
                b.revenue().toString(), b.discounts().toString(),
                String.valueOf(b.receipts()), b.averageBasket().toString()))
          .append('\n');
    }
}