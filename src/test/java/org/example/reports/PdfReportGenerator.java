package org.example.reports;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates a PDF test report by parsing Surefire XML result files.
 *
 * Usage:
 *   PdfReportGenerator <surefire-reports-dir> <output-pdf-path>
 *
 * Called automatically by exec-maven-plugin after the test phase.
 * Output: target/pdf-report/test-report.pdf
 */
public class PdfReportGenerator {

    // ── Colours ───────────────────────────────────────────────────────────────
    private static final DeviceRgb HEADER_BG   = new DeviceRgb(30, 30, 47);
    private static final DeviceRgb PASS_GREEN  = new DeviceRgb(39, 174, 96);
    private static final DeviceRgb FAIL_RED    = new DeviceRgb(192, 57, 43);
    private static final DeviceRgb SKIP_ORANGE = new DeviceRgb(230, 126, 34);
    private static final DeviceRgb ROW_ALT     = new DeviceRgb(245, 245, 250);
    private static final DeviceRgb SECTION_BG  = new DeviceRgb(52, 73, 94);

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: PdfReportGenerator <surefire-dir> <output-pdf>");
            System.exit(1);
        }

        File surefireDir = new File(args[0]);
        File outputPdf   = new File(args[1]);

        if (!surefireDir.exists()) {
            System.out.println("[PDF Report] No surefire-reports directory found — skipping PDF generation.");
            return;
        }

        outputPdf.getParentFile().mkdirs();

        List<TestSuiteResult> suites = parseSurefireXml(surefireDir);
        writePdf(suites, outputPdf);

        System.out.println("[PDF Report] Generated: " + outputPdf.getAbsolutePath());
    }

    // ── XML parsing ───────────────────────────────────────────────────────────

    private static List<TestSuiteResult> parseSurefireXml(File dir) throws Exception {
        List<TestSuiteResult> suites = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        File[] xmlFiles = dir.listFiles((d, name) -> name.endsWith(".xml") && name.startsWith("TEST-"));
        if (xmlFiles == null) return suites;

        for (File xml : xmlFiles) {
            org.w3c.dom.Document doc = builder.parse(xml);
            Element root = doc.getDocumentElement();

            TestSuiteResult suite = new TestSuiteResult();
            suite.name     = root.getAttribute("name");
            suite.tests    = parseInt(root.getAttribute("tests"));
            suite.failures = parseInt(root.getAttribute("failures"));
            suite.errors   = parseInt(root.getAttribute("errors"));
            suite.skipped  = parseInt(root.getAttribute("skipped"));
            suite.time     = root.getAttribute("time");

            NodeList cases = root.getElementsByTagName("testcase");
            for (int i = 0; i < cases.getLength(); i++) {
                Element tc = (Element) cases.item(i);
                TestCaseResult tc2 = new TestCaseResult();
                tc2.name      = tc.getAttribute("name");
                tc2.classname = tc.getAttribute("classname");
                tc2.time      = tc.getAttribute("time");

                NodeList failures = tc.getElementsByTagName("failure");
                NodeList errors   = tc.getElementsByTagName("error");
                NodeList skipped  = tc.getElementsByTagName("skipped");

                if (failures.getLength() > 0) {
                    tc2.status  = "FAILED";
                    tc2.message = ((Element) failures.item(0)).getAttribute("message");
                } else if (errors.getLength() > 0) {
                    tc2.status  = "ERROR";
                    tc2.message = ((Element) errors.item(0)).getAttribute("message");
                } else if (skipped.getLength() > 0) {
                    tc2.status  = "SKIPPED";
                } else {
                    tc2.status  = "PASSED";
                }
                suite.testCases.add(tc2);
            }
            suites.add(suite);
        }
        return suites;
    }

    // ── PDF writing ───────────────────────────────────────────────────────────

    private static void writePdf(List<TestSuiteResult> suites, File output) throws Exception {
        PdfWriter   writer   = new PdfWriter(output);
        PdfDocument pdfDoc   = new PdfDocument(writer);
        Document    document = new Document(pdfDoc);
        document.setMargins(36, 36, 36, 36);

        PdfFont boldFont    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // ── Cover / title ────────────────────────────────────────────────────
        document.add(new Paragraph("WeatherAI API — Test Report")
                .setFont(boldFont).setFontSize(22)
                .setFontColor(HEADER_BG)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(4));

        document.add(new Paragraph("Generated: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss")))
                .setFont(regularFont).setFontSize(9)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        // ── Overall summary ──────────────────────────────────────────────────
        int totalTests = 0, totalPass = 0, totalFail = 0, totalError = 0, totalSkip = 0;
        for (TestSuiteResult s : suites) {
            totalTests += s.tests;
            totalFail  += s.failures;
            totalError += s.errors;
            totalSkip  += s.skipped;
        }
        totalPass = totalTests - totalFail - totalError - totalSkip;

        Table summary = new Table(UnitValue.createPercentArray(new float[]{2, 1, 1, 1, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100));

        addHeaderCell(summary, "Total", boldFont, HEADER_BG);
        addHeaderCell(summary, "Passed", boldFont, PASS_GREEN);
        addHeaderCell(summary, "Failed", boldFont, FAIL_RED);
        addHeaderCell(summary, "Errors", boldFont, FAIL_RED);
        addHeaderCell(summary, "Skipped", boldFont, SKIP_ORANGE);
        addHeaderCell(summary, "Suites", boldFont, SECTION_BG);

        addDataCell(summary, String.valueOf(totalTests), regularFont, ColorConstants.WHITE, HEADER_BG);
        addDataCell(summary, String.valueOf(totalPass),  regularFont, ColorConstants.WHITE, PASS_GREEN);
        addDataCell(summary, String.valueOf(totalFail),  regularFont, ColorConstants.WHITE, totalFail  > 0 ? FAIL_RED    : PASS_GREEN);
        addDataCell(summary, String.valueOf(totalError), regularFont, ColorConstants.WHITE, totalError > 0 ? FAIL_RED    : PASS_GREEN);
        addDataCell(summary, String.valueOf(totalSkip),  regularFont, ColorConstants.WHITE, totalSkip  > 0 ? SKIP_ORANGE : PASS_GREEN);
        addDataCell(summary, String.valueOf(suites.size()), regularFont, ColorConstants.WHITE, SECTION_BG);

        document.add(summary);
        document.add(new Paragraph(" ").setMarginBottom(14));

        // ── Per-suite breakdown ──────────────────────────────────────────────
        for (TestSuiteResult suite : suites) {
            String shortName = suite.name.contains(".") ?
                    suite.name.substring(suite.name.lastIndexOf('.') + 1) : suite.name;

            document.add(new Paragraph(shortName)
                    .setFont(boldFont).setFontSize(11)
                    .setFontColor(ColorConstants.WHITE)
                    .setBackgroundColor(SECTION_BG)
                    .setPadding(5)
                    .setMarginTop(10).setMarginBottom(2));

            document.add(new Paragraph(
                    String.format("Tests: %d   Passed: %d   Failed: %d   Errors: %d   Skipped: %d   Time: %ss",
                            suite.tests,
                            suite.tests - suite.failures - suite.errors - suite.skipped,
                            suite.failures, suite.errors, suite.skipped, suite.time))
                    .setFont(regularFont).setFontSize(8)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setMarginBottom(4));

            // Test case table
            Table table = new Table(UnitValue.createPercentArray(new float[]{4, 1, 1}))
                    .setWidth(UnitValue.createPercentValue(100));

            addHeaderCell(table, "Test Name",  boldFont, HEADER_BG);
            addHeaderCell(table, "Status",     boldFont, HEADER_BG);
            addHeaderCell(table, "Time (s)",   boldFont, HEADER_BG);

            boolean altRow = false;
            for (TestCaseResult tc : suite.testCases) {

                DeviceRgb statusColor = switch (tc.status) {
                    case "PASSED"  -> PASS_GREEN;
                    case "FAILED"  -> FAIL_RED;
                    case "ERROR"   -> FAIL_RED;
                    case "SKIPPED" -> SKIP_ORANGE;
                    default        -> new DeviceRgb(100, 100, 100);
                };

                Cell nameCell = new Cell().add(new Paragraph(tc.name)
                        .setFont(regularFont).setFontSize(8))
                        .setBackgroundColor(altRow ? ROW_ALT : new DeviceRgb(255, 255, 255))
                        .setPadding(3);

                Cell statusCell = new Cell().add(new Paragraph(tc.status)
                        .setFont(boldFont).setFontSize(8).setFontColor(statusColor))
                        .setBackgroundColor(altRow ? ROW_ALT : new DeviceRgb(255, 255, 255))
                        .setPadding(3);

                Cell timeCell = new Cell().add(new Paragraph(tc.time)
                        .setFont(regularFont).setFontSize(8))
                        .setBackgroundColor(altRow ? ROW_ALT : new DeviceRgb(255, 255, 255))
                        .setPadding(3)
                        .setTextAlignment(TextAlignment.RIGHT);

                table.addCell(nameCell);
                table.addCell(statusCell);
                table.addCell(timeCell);

                // If failed/error, add message row
                if (tc.message != null && !tc.message.isBlank()) {
                    String msg = tc.message.length() > 200
                            ? tc.message.substring(0, 200) + "..." : tc.message;
                    Cell msgCell = new Cell(1, 3)
                            .add(new Paragraph("↳ " + msg)
                                    .setFont(regularFont).setFontSize(7)
                                    .setFontColor(FAIL_RED))
                            .setBackgroundColor(new DeviceRgb(255, 245, 245))
                            .setPadding(3);
                    table.addCell(msgCell);
                }

                altRow = !altRow;
            }
            document.add(table);
        }

        document.close();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void addHeaderCell(Table table, String text, PdfFont font, DeviceRgb bg) {
        table.addHeaderCell(new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(9).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(bg)
                .setPadding(5)
                .setTextAlignment(TextAlignment.CENTER));
    }

    private static void addDataCell(Table table, String text, PdfFont font, com.itextpdf.kernel.colors.Color fg, DeviceRgb bg) {
        table.addCell(new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(11).setFontColor(fg))
                .setBackgroundColor(bg)
                .setPadding(6)
                .setTextAlignment(TextAlignment.CENTER));
    }

    private static int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    // ── Data models ───────────────────────────────────────────────────────────

    static class TestSuiteResult {
        String name = "";
        int tests, failures, errors, skipped;
        String time = "0";
        List<TestCaseResult> testCases = new ArrayList<>();
    }

    static class TestCaseResult {
        String name, classname, time = "0", status = "PASSED", message;
    }
}
