package com.smartev.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.smartev.entity.Bill;
import com.smartev.entity.User;
import com.smartev.repository.BillRepository;
import com.smartev.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfInvoiceService {

    private final BillRepository billRepository;
    private final BillingService billingService;

    public PdfInvoiceService(BillRepository billRepository, BillingService billingService) {
        this.billRepository = billRepository;
        this.billingService = billingService;
    }

    @Transactional(readOnly = true)
    public byte[] generateInvoicePdf(Long billId, User currentUser) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + billId));

        billingService.validateBillAccess(bill, currentUser);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 40, 40);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Font Definitions
            Font brandFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(13, 110, 253));
            Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font footnoteFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);

            // 1. Header Section
            Paragraph brand = new Paragraph("SMART EV CHARGING SYSTEM", brandFont);
            brand.setAlignment(Element.ALIGN_CENTER);
            document.add(brand);

            Paragraph subtitle = new Paragraph("Centralized Urban & Highway EV Infrastructure Network", subHeaderFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);

            Paragraph demoBanner = new Paragraph("DEMO / SIMULATED INVOICE -- FOR DEMONSTRATION PURPOSES ONLY", boldFont);
            demoBanner.setAlignment(Element.ALIGN_CENTER);
            demoBanner.setSpacingBefore(8);
            document.add(demoBanner);

            Paragraph title = new Paragraph("DEMO / SIMULATED INVOICE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(6);
            title.setSpacingAfter(15);
            document.add(title);

            // 2. Invoice Meta Table
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingAfter(15);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

            PdfPCell cellLeft = new PdfPCell();
            cellLeft.setBorder(Rectangle.NO_BORDER);
            cellLeft.addElement(new Paragraph("Invoice Number: " + bill.getInvoiceNumber(), boldFont));
            cellLeft.addElement(new Paragraph("Issue Date: " + bill.getCreatedAt().format(dtf), normalFont));
            cellLeft.addElement(new Paragraph("Booking Reference: " + bill.getBooking().getBookingCode(), normalFont));
            cellLeft.addElement(new Paragraph("Session Code: " + bill.getSession().getSessionCode(), normalFont));
            metaTable.addCell(cellLeft);

            PdfPCell cellRight = new PdfPCell();
            cellRight.setBorder(Rectangle.NO_BORDER);
            cellRight.addElement(new Paragraph("Payment Status: " + bill.getPaymentStatus(), boldFont));
            cellRight.addElement(new Paragraph("Charging Company: " + bill.getCompany().getName(), normalFont));
            cellRight.addElement(new Paragraph("Station: " + bill.getStation().getName(), normalFont));
            cellRight.addElement(new Paragraph("Location: " + bill.getStation().getCity() + ", " + bill.getStation().getState(), normalFont));
            metaTable.addCell(cellRight);

            document.add(metaTable);

            // 3. Driver & Vehicle Info
            PdfPTable partyTable = new PdfPTable(2);
            partyTable.setWidthPercentage(100);
            partyTable.setSpacingAfter(15);

            PdfPCell driverCell = new PdfPCell();
            driverCell.setBackgroundColor(new Color(245, 247, 250));
            driverCell.setPadding(8);
            driverCell.addElement(new Paragraph("BILLED TO (DRIVER):", boldFont));
            driverCell.addElement(new Paragraph(bill.getDriver().getFullName(), normalFont));
            driverCell.addElement(new Paragraph("Email: " + bill.getDriver().getEmail(), normalFont));
            if (bill.getDriver().getPhone() != null) {
                driverCell.addElement(new Paragraph("Phone: " + bill.getDriver().getPhone(), normalFont));
            }
            partyTable.addCell(driverCell);

            PdfPCell chargerCell = new PdfPCell();
            chargerCell.setBackgroundColor(new Color(245, 247, 250));
            chargerCell.setPadding(8);
            chargerCell.addElement(new Paragraph("CHARGER DETAILS:", boldFont));
            chargerCell.addElement(new Paragraph("Post Identifier: " + bill.getCharger().getChargerIdentifier(), normalFont));
            chargerCell.addElement(new Paragraph("Type: " + bill.getCharger().getChargerType() + " (" + bill.getCharger().getConnectorType() + ")", normalFont));
            chargerCell.addElement(new Paragraph("Power Rating: " + bill.getCharger().getPowerRatingKw() + " kW", normalFont));
            partyTable.addCell(chargerCell);

            document.add(partyTable);

            // 4. Itemized Calculation Table
            PdfPTable itemsTable = new PdfPTable(4);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{40, 20, 20, 20});
            itemsTable.setSpacingAfter(15);

            // Header Row
            String[] headers = {"Description", "Rate", "Quantity", "Amount (INR)"};
            for (String h : headers) {
                PdfPCell th = new PdfPCell(new Phrase(h, tableHeaderFont));
                th.setBackgroundColor(new Color(33, 37, 41));
                th.setPadding(6);
                itemsTable.addCell(th);
            }

            // Energy Consumed Row
            itemsTable.addCell(new Phrase("EV Energy Delivered (Simulated)", normalFont));
            itemsTable.addCell(new Phrase("Rs. " + bill.getPricePerKwh() + " / kWh", normalFont));
            itemsTable.addCell(new Phrase(String.format("%.2f kWh", bill.getEnergyConsumedKwh()), normalFont));
            itemsTable.addCell(new Phrase("Rs. " + bill.getEnergyCost().toString(), normalFont));

            // Service Charge Row
            itemsTable.addCell(new Phrase("Grid Connection & Service Fee", normalFont));
            itemsTable.addCell(new Phrase("-", normalFont));
            itemsTable.addCell(new Phrase("1 Session", normalFont));
            itemsTable.addCell(new Phrase("Rs. " + bill.getServiceCharge().toString(), normalFont));

            // Subtotal Row
            PdfPCell subtotalLabel = new PdfPCell(new Phrase("Subtotal:", boldFont));
            subtotalLabel.setColspan(3);
            subtotalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            itemsTable.addCell(subtotalLabel);
            itemsTable.addCell(new Phrase("Rs. " + bill.getEnergyCost().add(bill.getServiceCharge()), boldFont));

            // GST Row
            PdfPCell taxLabel = new PdfPCell(new Phrase(String.format("GST / Tax (%.1f%%):", bill.getTaxRatePct().doubleValue()), normalFont));
            taxLabel.setColspan(3);
            taxLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            itemsTable.addCell(taxLabel);
            itemsTable.addCell(new Phrase("Rs. " + bill.getTaxAmount().toString(), normalFont));

            // Total Row
            PdfPCell totalLabel = new PdfPCell(new Phrase("Total Amount Payable (Settled):", boldFont));
            totalLabel.setColspan(3);
            totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalLabel.setBackgroundColor(new Color(230, 242, 255));
            itemsTable.addCell(totalLabel);

            PdfPCell totalVal = new PdfPCell(new Phrase("Rs. " + bill.getTotalAmount().toString(), boldFont));
            totalVal.setBackgroundColor(new Color(230, 242, 255));
            itemsTable.addCell(totalVal);

            document.add(itemsTable);

            // 5. Environmental Benefit Callout
            double co2SavedKg = bill.getEnergyConsumedKwh() * 0.82; // CEA India grid emission baseline
            Paragraph eco = new Paragraph(
                    String.format("[Environmental Benefit] Clean Energy Delivered: %.2f kWh. Estimated CO2 emission avoided: %.2f kg (based on 0.82 kg CO2/kWh baseline).",
                            bill.getEnergyConsumedKwh(), co2SavedKg), footnoteFont);
            eco.setSpacingAfter(15);
            document.add(eco);

            // 6. Signoff Footer
            Paragraph footer = new Paragraph("DEMO / SIMULATED INVOICE -- Generated for academic project demonstration only. Not a valid tax invoice. No real monetary transaction occurred.", footnoteFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (DocumentException ex) {
            throw new RuntimeException("Failed to generate PDF invoice: " + ex.getMessage(), ex);
        }

        return out.toByteArray();
    }
}
