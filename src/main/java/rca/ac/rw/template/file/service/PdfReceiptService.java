package rca.ac.rw.template.file.service;


import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rca.ac.rw.template.payslip.entity.Payslip;
import rca.ac.rw.template.employee.entity.Employee;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class PdfReceiptService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
    private static final DateTimeFormatter MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");


    /**
     * Generates a PDF payslip for a given Payslip entity.
     *
     * @param payslip The {@link Payslip} for which to generate a receipt.
     * @return A byte array containing the PDF data.
     * @throws IOException If an error occurs during PDF generation.
     */
    public byte[] generatePayslipPdf(Payslip payslip) throws IOException {
        log.info("Generating PDF Payslip for Employee: {}, Period: {}/{}",
                payslip.getEmployee().getEmployeeCode(), payslip.getMonth(), payslip.getYear());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(byteArrayOutputStream);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument, PageSize.A4);
        document.setMargins(30, 30, 30, 30);

        PdfFont font = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
        PdfFont boldFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);

        Paragraph header = new Paragraph("Employee Payslip")
                .setFont(boldFont).setFontSize(20).setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(header);

        Paragraph companyName = new Paragraph("Your Institution Name Here")
                .setFont(font).setFontSize(14).setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(companyName);

        Paragraph period = new Paragraph("For the Period: " + String.format("%02d/%d", payslip.getMonth(), payslip.getYear()))
                .setFont(boldFont).setFontSize(12).setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(period);


        Employee employee = payslip.getEmployee();
        Table empDetailsTable = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1, 2})).useAllAvailableWidth().setMarginBottom(15);
        empDetailsTable.addCell(new Paragraph("Employee Code:").setFont(boldFont));
        empDetailsTable.addCell(new Paragraph(employee.getEmployeeCode()).setFont(font));
        empDetailsTable.addCell(new Paragraph("Employee Name:").setFont(boldFont));
        empDetailsTable.addCell(new Paragraph(employee.getFirstName() + " " + employee.getLastName()).setFont(font));
        document.add(empDetailsTable);


        Table earningsDeductionsTable = new Table(UnitValue.createPercentArray(new float[]{2, 1, 1})).useAllAvailableWidth().setMarginBottom(15);
        earningsDeductionsTable.addHeaderCell(new Paragraph("Description").setFont(boldFont));
        earningsDeductionsTable.addHeaderCell(new Paragraph("Earnings (RWF)").setFont(boldFont).setTextAlignment(TextAlignment.RIGHT));
        earningsDeductionsTable.addHeaderCell(new Paragraph("Deductions (RWF)").setFont(boldFont).setTextAlignment(TextAlignment.RIGHT));

        earningsDeductionsTable.addCell(new Paragraph("Base Salary").setFont(font));
        earningsDeductionsTable.addCell(new Paragraph(payslip.getBaseSalarySnapshot().setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()).setFont(font).setTextAlignment(TextAlignment.RIGHT));
        earningsDeductionsTable.addCell(new Paragraph("").setFont(font));

        earningsDeductionsTable.addCell(new Paragraph("Housing Allowance").setFont(font));
        earningsDeductionsTable.addCell(new Paragraph(payslip.getHouseAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()).setFont(font).setTextAlignment(TextAlignment.RIGHT));
        earningsDeductionsTable.addCell(new Paragraph("").setFont(font));

        earningsDeductionsTable.addCell(new Paragraph("Transport Allowance").setFont(font));
        earningsDeductionsTable.addCell(new Paragraph(payslip.getTransportAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()).setFont(font).setTextAlignment(TextAlignment.RIGHT));
        earningsDeductionsTable.addCell(new Paragraph("").setFont(font));

        earningsDeductionsTable.addCell(new Paragraph("GROSS SALARY").setFont(boldFont));
        earningsDeductionsTable.addCell(new Paragraph(payslip.getGrossSalary().setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()).setFont(boldFont).setTextAlignment(TextAlignment.RIGHT));
        earningsDeductionsTable.addCell(new Paragraph("").setFont(boldFont));


        earningsDeductionsTable.addCell(new Paragraph("Employee Tax").setFont(font));
        earningsDeductionsTable.addCell(new Paragraph("").setFont(font));
        earningsDeductionsTable.addCell(new Paragraph(payslip.getEmployeeTaxedAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()).setFont(font).setTextAlignment(TextAlignment.RIGHT));

        earningsDeductionsTable.addCell(new Paragraph("Pension (6%)").setFont(font));
        earningsDeductionsTable.addCell(new Paragraph("").setFont(font));
        earningsDeductionsTable.addCell(new Paragraph(payslip.getPensionAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()).setFont(font).setTextAlignment(TextAlignment.RIGHT));

        earningsDeductionsTable.addCell(new Paragraph("Medical Insurance").setFont(font));
        earningsDeductionsTable.addCell(new Paragraph("").setFont(font));
        earningsDeductionsTable.addCell(new Paragraph(payslip.getMedicalInsuranceAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()).setFont(font).setTextAlignment(TextAlignment.RIGHT));

        earningsDeductionsTable.addCell(new Paragraph("Other Deductions").setFont(font));
        earningsDeductionsTable.addCell(new Paragraph("").setFont(font));
        earningsDeductionsTable.addCell(new Paragraph(payslip.getOtherTaxedAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()).setFont(font).setTextAlignment(TextAlignment.RIGHT));

        BigDecimal totalDeductions = payslip.getEmployeeTaxedAmount()
                .add(payslip.getPensionAmount())
                .add(payslip.getMedicalInsuranceAmount())
                .add(payslip.getOtherTaxedAmount());
        earningsDeductionsTable.addCell(new Paragraph("TOTAL DEDUCTIONS").setFont(boldFont));
        earningsDeductionsTable.addCell(new Paragraph("").setFont(boldFont));
        earningsDeductionsTable.addCell(new Paragraph(totalDeductions.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()).setFont(boldFont).setTextAlignment(TextAlignment.RIGHT));

        document.add(earningsDeductionsTable);


        Table netSalaryTable = new Table(UnitValue.createPercentArray(new float[]{2, 1})).useAllAvailableWidth().setMarginTop(15);
        netSalaryTable.addCell(new Paragraph("NET SALARY PAYABLE").setFont(boldFont).setFontSize(14));
        netSalaryTable.addCell(new Paragraph(payslip.getNetSalary().setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + " RWF").setFont(boldFont).setFontSize(14).setTextAlignment(TextAlignment.RIGHT));
        document.add(netSalaryTable);


        Paragraph footer = new Paragraph("This is a system-generated payslip. " + payslip.getStatus())
                .setFont(font).setFontSize(10).setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(50);
        document.add(footer);

        document.close();
        log.info("PDF Payslip generated for Employee: {}, Period: {}/{}",
                payslip.getEmployee().getEmployeeCode(), payslip.getMonth(), payslip.getYear());
        return byteArrayOutputStream.toByteArray();
    }
}