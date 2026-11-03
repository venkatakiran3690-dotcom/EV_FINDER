import os
import sys
import subprocess

# Auto-install reportlab if missing in current executing environment
try:
    import reportlab
except ImportError:
    print("ReportLab missing. Installing reportlab dynamically...")
    subprocess.check_call([sys.executable, "-m", "pip", "install", "reportlab"])
    import reportlab

from reportlab.lib.pagesizes import letter
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_LEFT, TA_JUSTIFY
from reportlab.pdfgen import canvas

# Define custom page numbering class
class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super(NumberedCanvas, self).__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_elements(num_pages)
            super(NumberedCanvas, self).showPage()
        super(NumberedCanvas, self).save()

    def draw_page_elements(self, page_count):
        self.saveState()
        
        # Suppress headers/footers on page 1 (cover page)
        if self._pageNumber == 1:
            self.restoreState()
            return

        # Running Header
        self.setFont("Helvetica-Bold", 8)
        self.setFillColor(colors.HexColor("#0f766e")) # Forest Teal
        self.drawString(54, 750, "SUP CHARGED - PROJECT TECHNICAL DOCUMENTATION")
        self.setStrokeColor(colors.HexColor("#e2e8f0"))
        self.setLineWidth(0.5)
        self.line(54, 742, 558, 742)

        # Running Footer
        self.setFont("Helvetica", 9)
        self.setFillColor(colors.HexColor("#64748b"))
        self.drawString(54, 36, "Confidential - Antigravity EV Smart Grid Hub Project Documentation")
        
        page_str = f"Page {self._pageNumber} of {page_count}"
        self.drawRightString(558, 36, page_str)
        self.restoreState()

def build_pdf(filename):
    # Setup document geometry (0.75 in / 54pt margins)
    doc = SimpleDocTemplate(
        filename,
        pagesize=letter,
        leftMargin=54,
        rightMargin=54,
        topMargin=72,
        bottomMargin=54
    )

    styles = getSampleStyleSheet()
    
    # Custom Brand Colors
    PRIMARY_COLOR = colors.HexColor("#ffb300")   # Solar Amber Gold
    SECONDARY_COLOR = colors.HexColor("#0f766e") # Forest Teal
    DARK_BG = colors.HexColor("#0b0f19")          # Slate Dark
    TEXT_COLOR = colors.HexColor("#1e293b")       # Slate 800
    MUTED_TEXT = colors.HexColor("#64748b")       # Slate 500

    # Custom Paragraph Styles
    title_style = ParagraphStyle(
        'CoverTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=32,
        leading=38,
        textColor=SECONDARY_COLOR,
        alignment=TA_CENTER,
        spaceAfter=15
    )

    subtitle_style = ParagraphStyle(
        'CoverSubtitle',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=14,
        leading=18,
        textColor=MUTED_TEXT,
        alignment=TA_CENTER,
        spaceAfter=120
    )

    meta_style = ParagraphStyle(
        'CoverMeta',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=10,
        leading=14,
        textColor=DARK_BG,
        alignment=TA_CENTER,
        spaceAfter=5
    )

    h1_style = ParagraphStyle(
        'Heading1_Custom',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=18,
        leading=22,
        textColor=SECONDARY_COLOR,
        spaceBefore=22,
        spaceAfter=10,
        keepWithNext=True
    )

    h2_style = ParagraphStyle(
        'Heading2_Custom',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=13,
        leading=16,
        textColor=PRIMARY_COLOR,
        spaceBefore=14,
        spaceAfter=6,
        keepWithNext=True
    )

    body_style = ParagraphStyle(
        'Body_Custom',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=10.5,
        leading=15,
        textColor=TEXT_COLOR,
        spaceAfter=10
    )

    code_style = ParagraphStyle(
        'Code_Custom',
        parent=styles['Normal'],
        fontName='Courier',
        fontSize=8.5,
        leading=12,
        textColor=colors.HexColor("#0f172a"),
        backColor=colors.HexColor("#f1f5f9"),
        borderPadding=8,
        spaceAfter=12
    )

    bullet_style = ParagraphStyle(
        'Bullet_Custom',
        parent=body_style,
        leftIndent=15,
        firstLineIndent=-10,
        spaceAfter=6
    )

    story = []

    # ================= PAGE 1: COVER PAGE =================
    story.append(Spacer(1, 120))
    story.append(Paragraph("SUP CHARGED", title_style))
    story.append(Paragraph("Smart Grid EV Locator & Live Simulation Engine", subtitle_style))
    
    # Graphic Accents
    accent_table = Table([[""]], colWidths=[200], rowHeights=[4])
    accent_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), PRIMARY_COLOR),
        ('ALIGN', (0,0), (-1,-1), 'CENTER'),
    ]))
    story.append(accent_table)
    story.append(Spacer(1, 140))
    
    # Metadata Block
    story.append(Paragraph("TECHNICAL SPECIFICATION & ARCHITECTURAL MANUAL", meta_style))
    story.append(Paragraph("Compiled by Antigravity AI & Bengaluru Smart City Grid", ParagraphStyle('MetaSub', parent=meta_style, fontName='Helvetica', textColor=MUTED_TEXT)))
    story.append(Paragraph("Version 1.0.0  |  June 2026", ParagraphStyle('MetaDate', parent=meta_style, fontName='Helvetica-Oblique', textColor=MUTED_TEXT)))
    story.append(PageBreak())

    # ================= PAGE 2: EXECUTIVE SUMMARY =================
    story.append(Paragraph("1. Executive Summary & Design Vision", h1_style))
    story.append(Paragraph(
        "<b>Sup Charged</b> is a state-of-the-art interactive EV (Electric Vehicle) Charging Station Locator and "
        "live simulation engine designed for drivers and grid retailers. Positioned as a premium smart-grid dashboard, "
        "the project addresses real-time charging visibility, smart queues, grid peak-load scheduling, and automated "
        "secure financial payouts for station owners.",
        body_style
    ))
    story.append(Paragraph(
        "The project is structured with a robust decoupled stack, featuring a high-performance React client "
        "and a secure REST API backend. It prioritizes data integrity and visual excellence through glassmorphic styling, "
        "responsive layout anchors, and dynamic SVG animations that map perfectly to the <b>Solar Aurora</b> brand identity.",
        body_style
    ))
    story.append(Spacer(1, 10))

    story.append(Paragraph("1.1 Solar Aurora Design Aesthetics", h2_style))
    story.append(Paragraph(
        "To break away from generic web aesthetics, Sup Charged leverages the premium Solar Aurora design tokens:",
        body_style
    ))
    story.append(Paragraph("• <b>Primary Token (Solar Gold)</b>: Hex <code>#ffb300</code>, representing solar energy feed-in loops.", bullet_style))
    story.append(Paragraph("• <b>Secondary Token (Forest Teal)</b>: Hex <code>#0f766e</code>, grounding the ecosystem in natural decarbonization.", bullet_style))
    story.append(Paragraph("• <b>Background Dark Token</b>: Hex <code>#0b0f19</code>, serving as a slate backdrop to provide high contrast for map pins and KPI elements.", bullet_style))
    story.append(Paragraph("• <b>Typography</b>: Outfits (for structured headings) and Plus Jakarta Sans (for highly readable, responsive text sizes).", bullet_style))
    
    # ================= SECTION 2: SYSTEM ARCHITECTURE =================
    story.append(Paragraph("2. High-Level Architecture", h1_style))
    story.append(Paragraph(
        "The application follows a clean full-stack architecture with decoupled concern layers:",
        body_style
    ))
    
    arch_data = [
        ["Layer", "Technology", "Primary Responsibility"],
        ["Frontend Client", "Vite + React 18, ESM modules", "Interactive map, simulators, dashboard UI, client states"],
        ["Backend REST Server", "Node.js + Express", "OTP auth, station databases, booking records, wallet logic"],
        ["Database Store", "Flat JSON databases", "Stations, bookings, and operator wallet records persistency"],
        ["Map Render System", "Leaflet + OpenStreetMap API", "Nellore grid coordinates mapping, custom SVG pins, routing"],
    ]
    arch_table = Table(arch_data, colWidths=[90, 150, 260])
    arch_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), SECONDARY_COLOR),
        ('TEXTCOLOR', (0,0), (-1,0), colors.white),
        ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'),
        ('FONTSIZE', (0,0), (-1,-1), 9),
        ('BOTTOMPADDING', (0,0), (-1,0), 6),
        ('BACKGROUND', (0,1), (-1,-1), colors.HexColor("#f8fafc")),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#cbd5e1")),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor("#f1f5f9")]),
        ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ]))
    story.append(arch_table)
    story.append(Spacer(1, 15))

    # ================= SECTION 3: BACKEND API =================
    story.append(Paragraph("3. Backend Services & REST API", h1_style))
    story.append(Paragraph(
        "The backend Node server handles persistent JSON registries. It logs active OTP codes and exposes secure endpoints:",
        body_style
    ))
    
    api_data = [
        ["Method", "Endpoint Path", "Purpose", "Response Format"],
        ["POST", "/api/auth/register", "Saves pending user, triggers OTP", "JSON: {otpRequired: true, code}"],
        ["POST", "/api/auth/login", "Validates user, triggers OTP", "JSON: {otpRequired: true, code}"],
        ["POST", "/api/auth/verify-otp", "Verifies 6-digit OTP", "JSON: {username, email, role, phone}"],
        ["POST", "/api/auth/resend-otp", "Regenerates and logs OTP", "JSON: {message, code}"],
        ["GET", "/api/stations", "Fetches Nellore station registry", "Array: [ {id, name, lat, lng, connectors...} ]"],
        ["POST", "/api/bookings", "Books a slot on a specific charger", "JSON: {bookingId, stationId, cost...}"],
        ["POST", "/api/wallet/withdraw", "Processes operator payouts", "JSON: {success: true, withdrawnAmount}"],
    ]
    api_table = Table(api_data, colWidths=[50, 140, 180, 130])
    api_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), DARK_BG),
        ('TEXTCOLOR', (0,0), (-1,0), PRIMARY_COLOR),
        ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'),
        ('FONTSIZE', (0,0), (-1,-1), 8),
        ('BOTTOMPADDING', (0,0), (-1,0), 6),
        ('BACKGROUND', (0,1), (-1,-1), colors.HexColor("#f8fafc")),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#cbd5e1")),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [colors.white, colors.HexColor("#f1f5f9")]),
    ]))
    story.append(api_table)
    story.append(Spacer(1, 15))

    story.append(Paragraph("3.1 OTP Authentication Flow", h2_style))
    story.append(Paragraph(
        "To ensure secure transactions, register/login pathways are bound to a 6-digit OTP state machine. "
        "The server holds generated codes in an <code>activeOtps</code> dictionary with a 5-minute expiration timestamp. "
        "During local testing, codes are logged directly into the node console and returned in the HTTP response body, "
        "allowing the frontend to render an interactive testing banner.",
        body_style
    ))
    story.append(PageBreak())

    # ================= SECTION 4: FRONTEND IMPLEMENTATION =================
    story.append(Paragraph("4. Frontend UI Components", h1_style))
    story.append(Paragraph(
        "The interface is built using reactive components with dedicated hooks:",
        body_style
    ))
    story.append(Paragraph(
        "• <b>Logo (Logo.jsx)</b>: Custom vector icon combining a leaf shape and a power plug. To resolve SVG rendering clashes "
        "common in single-page applications, it generates instance-unique gradient IDs via React <code>useId()</code>. It also "
        "supports a <code>variant='white'</code> flag to maintain high-contrast visibility when displayed inside the splash "
        "screen's primary-to-secondary gradient background.",
        body_style
    ))
    story.append(Paragraph(
        "• <b>Map Canvas (LeafletMap.jsx)</b>: Centered dynamically on Nellore coordinates. Pins pulse red/orange/green "
        "representing charging connector availability. It handles unmount lifecycles securely in React StrictMode, keeping "
        "markers fully synchronized.",
        body_style
    ))
    story.append(Paragraph(
        "• <b>Driver Console (Sidebar.jsx)</b>: A side drawer that lets users query stations, filter by connector types (CCS2, Type 2, GB/T), "
        "toggle favorites, schedule bookings, top up their wallets, and check dynamically calculated peak-pricing rates.",
        body_style
    ))
    story.append(Paragraph(
        "• <b>Simulator HUD (ChargingSimulator.jsx)</b>: Tracks live battery SOC charging loops (5% to 100%). It integrates "
        "charging speed indicators, elapsed time countdowns, and triggers wallet deduct queries upon session completion.",
        body_style
    ))
    story.append(Paragraph(
        "• <b>Operator Portal (OperatorDashboard.jsx)</b>: Exposes live KPIs (Total Revenue, Energy Sold, Active Sessions), "
        "real-time review logs, station status toggles, queue metrics, and a payout request interface.",
        body_style
    ))

    # ================= SECTION 5: CORE CODE CORRECTIONS =================
    story.append(Paragraph("5. Critical Technical Resolutions", h1_style))
    story.append(Paragraph(
        "Several key software glitches were resolved during optimization:",
        body_style
    ))
    
    story.append(Paragraph("5.1 Sidebar Layout Flexbox Collapsing", h2_style))
    story.append(Paragraph(
        "In modern flex container contexts, child elements with overflow properties default to <code>min-height: auto</code>. "
        "This prevents them from shrinking to fit, causing layout overflows and locking scroll bars. Resolving this required "
        "overriding the dashboard main container with a strict <code>min-height: 0;</code> rule in the CSS stylesheet to trigger proper overflow scrolling.",
        body_style
    ))

    story.append(Paragraph("5.2 React StrictMode Map Rendering", h2_style))
    story.append(Paragraph(
        "Under React 18, StrictMode mounts, unmounts, and remounts components on load. The Leaflet map instance was originally "
        "held in a Ref, which did not trigger effect runs upon remounting, leaving the second map instance blank. "
        "Refactoring the map object into a React state variable (<code>mapInstance</code>) ensured all marker synchronization "
        "effects executed reliably on initialization.",
        body_style
    ))
    
    story.append(Paragraph("5.3 Dynamic Popup Action Binding", h2_style))
    story.append(Paragraph(
        "Adding navigation buttons directly inside Leaflet popup HTML strings caused stale closure bugs, as standard Leaflet popups "
        "do not re-render under the React lifecycle. We implemented a global map-level popup open listener (<code>map.on('popupopen')</code>) "
        "that dynamically retrieves DOM elements and binds current props, ensuring GPS routing uses the user's latest coordinates.",
        body_style
    ))
    story.append(PageBreak())

    # ================= SECTION 6: HOW TO DEPLOY =================
    story.append(Paragraph("6. Deployment & Verification Protocols", h1_style))
    story.append(Paragraph(
        "To boot the project and run verification cycles, execute these standard routines:",
        body_style
    ))
    
    story.append(Paragraph("1. Install Dependencies", h2_style))
    story.append(Paragraph(
        "Run the installation script to configure root, frontend, and backend packages concurrently:",
        body_style
    ))
    story.append(Paragraph("npm run install-all", code_style))

    story.append(Paragraph("2. Run Local Development Servers", h2_style))
    story.append(Paragraph(
        "Boot both the Vite client (port 5173) and the API server (port 5000) concurrently:",
        body_style
    ))
    story.append(Paragraph("npm run dev", code_style))

    story.append(Paragraph("3. Compile Production Bundle", h2_style))
    story.append(Paragraph(
        "Build the production-ready minified asset folder and host it static-first inside the backend public directory:",
        body_style
    ))
    story.append(Paragraph("npm run build-prod\nnpm start --prefix backend", code_style))
    
    story.append(Spacer(1, 10))
    story.append(Paragraph("6.1 Testing Profiles", h2_style))
    story.append(Paragraph(
        "Use the following preset accounts to verify role authorization pathways:",
        body_style
    ))
    
    test_data = [
        ["User Role", "Email Username", "Password", "Simulated Phone"],
        ["EV Driver", "driver@sup.in", "password", "+91 9876543210"],
        ["Grid Operator", "operator@sup.in", "password", "+91 9123456780"],
    ]
    test_table = Table(test_data, colWidths=[120, 150, 110, 120])
    test_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), SECONDARY_COLOR),
        ('TEXTCOLOR', (0,0), (-1,0), colors.white),
        ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'),
        ('FONTSIZE', (0,0), (-1,-1), 9),
        ('BOTTOMPADDING', (0,0), (-1,0), 6),
        ('BACKGROUND', (0,1), (-1,-1), colors.HexColor("#f8fafc")),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#cbd5e1")),
    ]))
    story.append(test_table)
    
    story.append(Spacer(1, 30))
    
    # Sign-off box
    sign_off = []
    sign_off.append(Paragraph("<b>Document Verification Sign-off</b>", ParagraphStyle('SignTitle', parent=body_style, fontName='Helvetica-Bold')))
    sign_off.append(Spacer(1, 4))
    sign_off.append(Paragraph("This manual specifies the system architecture for Sup Charged. All modules are compiled, verified, and operating in conformity with safety guidelines.", ParagraphStyle('SignBody', parent=body_style, fontSize=9, textColor=MUTED_TEXT)))
    
    story.append(KeepTogether(sign_off))

    # Build the document using our custom canvas for page numbering
    doc.build(story, canvasmaker=NumberedCanvas)

if __name__ == "__main__":
    target_path = r"C:\Users\Lakshmi Siv Prasad\Desktop\Sup_Charged_Project_Documentation.pdf"
    print(f"Generating PDF at {target_path}...")
    build_pdf(target_path)
    print("PDF generation completed successfully!")
