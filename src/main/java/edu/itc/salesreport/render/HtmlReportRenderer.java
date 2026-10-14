package edu.itc.salesreport.render;

import java.nio.charset.StandardCharsets;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.MonthlyReport;

public final class HtmlReportRenderer implements ReportRenderer {

    @Override
    public String extension() {
        return "html";
    }

    @Override
    public byte[] render(MonthlyReport report) {
        var sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html><head><meta charset=\"UTF-8\"><title>")
          .append("Angkor Mart ").append(report.month())
          .append("</title></head><body>\n<h1>Angkor Mart monthly sales ")
          .append(report.month()).append("</h1>\n<table>\n")
          .append("<tr><th>Branch</th><th>Revenue</th><th>Receipts</th>")
          .append("<th>Avg basket</th></tr>\n");
        for (BranchSummary b : report.branches()) {
            row(sb, b);
        }
        row(sb, report.chain());
        sb.append("</table>\n</body></html>\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void row(StringBuilder sb, BranchSummary b) {
        sb.append("<tr><td>").append(escape(b.branch()))
          .append("</td><td>").append(b.revenue())
          .append("</td><td>").append(b.receipts())
          .append("</td><td>").append(b.averageBasket())
          .append("</td></tr>\n");
    }

    static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}