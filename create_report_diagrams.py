import os
from PIL import Image, ImageDraw, ImageFont

# Set output directory for diagram assets
assets_dir = os.path.join(os.path.dirname(__file__), "report_assets")
os.makedirs(assets_dir, exist_ok=True)

# Helper function to get default PIL font
def get_font(size=14, bold=False):
    try:
        # Standard Windows fonts
        font_name = "arialbd.ttf" if bold else "arial.ttf"
        return ImageFont.truetype(font_name, size)
    except Exception:
        return ImageFont.load_default()

def draw_rounded_rect(draw, xy, corner_radius, fill=None, outline=None, width=1):
    draw.rounded_rectangle(xy, radius=corner_radius, fill=fill, outline=outline, width=width)

# Colors
C_GOLD = (255, 179, 0)
C_TEAL = (15, 118, 110)
C_DARK = (11, 15, 25)
C_BG = (248, 250, 252)
C_WHITE = (255, 255, 255)
C_BORDER = (203, 213, 225)
C_TEXT_DARK = (15, 23, 42)
C_TEXT_MUTED = (100, 116, 139)

# 1. Figure 2.1: Existing vs Proposed System
def make_fig_2_1():
    img = Image.new("RGB", (900, 420), C_BG)
    draw = ImageDraw.Draw(img)
    f_title = get_font(18, True)
    f_box = get_font(14, True)
    f_sub = get_font(11, False)

    # Title
    draw.text((450, 25), "Existing EV Systems vs. Sup Charged Smart Grid", fill=C_TEXT_DARK, font=f_title, anchor="mm")

    # Box 1: Legacy
    draw_rounded_rect(draw, [50, 70, 420, 360], 12, fill=C_WHITE, outline=(239, 68, 68), width=2)
    draw.text((235, 95), "Traditional Static App", fill=(220, 38, 38), font=f_box, anchor="mm")
    draw.line([(70, 115), (400, 115)], fill=(252, 165, 165), width=1)
    items_old = [
        "• Static map pins without live status",
        "• No battery SoC estimation during trip",
        "• Missing dynamic surge / solar pricing",
        "• Unverified, open login security",
        "• No real-time retailer payout dashboard"
    ]
    for i, item in enumerate(items_old):
        draw.text((80, 140 + i * 40), item, fill=C_TEXT_DARK, font=f_sub)

    # Box 2: Proposed Sup Charged
    draw_rounded_rect(draw, [480, 70, 850, 360], 12, fill=C_WHITE, outline=C_TEAL, width=2)
    draw.text((665, 95), "Sup Charged Smart Grid Hub", fill=C_TEAL, font=f_box, anchor="mm")
    draw.line([(500, 115), (830, 115)], fill=(153, 246, 228), width=1)
    items_new = [
        "• Real-time connector availability & map pins",
        "• Live Battery SoC charging simulator HUD",
        "• Dynamic Solar & Peak surge price calculator",
        "• Secure 6-digit OTP verification state machine",
        "• Full operator portal with wallet payouts"
    ]
    for i, item in enumerate(items_new):
        draw.text((500, 140 + i * 40), item, fill=C_TEXT_DARK, font=f_sub)

    path = os.path.join(assets_dir, "fig_2_1.png")
    img.save(path)

# 2. Figure 3.1: System Architecture Diagram
def make_fig_3_1():
    img = Image.new("RGB", (900, 440), C_BG)
    draw = ImageDraw.Draw(img)
    f_title = get_font(18, True)
    f_box = get_font(13, True)
    f_sub = get_font(10, False)

    draw.text((450, 25), "System Architecture of Sup Charged EV Locator", fill=C_TEXT_DARK, font=f_title, anchor="mm")

    layers = [
        ("Data Sources", ["GPS Location", "Mobile App UI", "Station Devices"], 50, C_TEAL),
        ("Preprocessing Layer", ["Haversine Distance", "Grid Pricing", "Input Validation"], 260, C_TEAL),
        ("Core Engines", ["OTP Auth Service", "Live Battery Simulator", "Routing Engine"], 470, C_TEAL),
        ("Presentation Layer", ["Driver Map Dashboard", "Operator Command", "Wallet & Payouts"], 680, C_GOLD)
    ]

    for title, items, x, color in layers:
        draw_rounded_rect(draw, [x, 70, x + 170, 320], 10, fill=C_WHITE, outline=color, width=2)
        draw.text((x + 85, 90), title, fill=color, font=f_box, anchor="mm")
        draw.line([(x + 10, 110), (x + 160, 110)], fill=C_BORDER, width=1)
        for j, item in enumerate(items):
            draw_rounded_rect(draw, [x + 12, 130 + j * 55, x + 158, 172 + j * 55], 6, fill=(241, 245, 249), outline=C_BORDER, width=1)
            draw.text((x + 85, 151 + j * 55), item, fill=C_TEXT_DARK, font=f_sub, anchor="mm")

        # Arrow right
        if x < 680:
            draw.polygon([(x + 180, 195), (x + 245, 195), (x + 245, 190), (x + 255, 200), (x + 245, 210), (x + 245, 205), (x + 180, 205)], fill=C_TEAL)

    # Cloud DB at bottom
    draw_rounded_rect(draw, [200, 350, 700, 410], 10, fill=C_DARK, outline=C_GOLD, width=2)
    draw.text((450, 380), "Express REST API & Persistent JSON Data Store (stations.json, bookings.json, wallet.json)", fill=C_GOLD, font=get_font(12, True), anchor="mm")

    path = os.path.join(assets_dir, "fig_3_1.png")
    img.save(path)

# 3. Figure 4.1: Live Battery SOC Simulation Engine Workflow
def make_fig_4_1():
    img = Image.new("RGB", (900, 260), C_BG)
    draw = ImageDraw.Draw(img)
    f_title = get_font(16, True)
    f_box = get_font(12, True)
    f_sub = get_font(10, False)

    draw.text((450, 20), "Live Battery SOC Simulation Engine Workflow", fill=C_TEXT_DARK, font=f_title, anchor="mm")

    steps = [
        ("1. Select Plug", "Station & Power (kW)"),
        ("2. Initialize HUD", "Set initial SoC %"),
        ("3. Charging Loop", "Increment SoC & Time"),
        ("4. Tariff Compute", "Apply Solar/Peak Rates"),
        ("5. Session Finish", "Deduct Wallet & Receipt")
    ]

    for i, (title, desc) in enumerate(steps):
        x = 30 + i * 170
        draw_rounded_rect(draw, [x, 60, x + 140, 210], 10, fill=C_WHITE, outline=C_TEAL, width=2)
        draw_rounded_rect(draw, [x, 60, x + 140, 95], 10, fill=C_TEAL)
        draw.text((x + 70, 77), title, fill=C_WHITE, font=f_box, anchor="mm")
        draw.text((x + 70, 140), desc, fill=C_TEXT_DARK, font=f_sub, anchor="mm")

        if i < 4:
            draw.polygon([(x + 145, 130), (x + 162, 130), (x + 162, 126), (x + 168, 135), (x + 162, 144), (x + 162, 140), (x + 145, 140)], fill=C_GOLD)

    path = os.path.join(assets_dir, "fig_4_1.png")
    img.save(path)

# 4. Figure 4.2: 6-Digit OTP Flowchart
def make_fig_4_2():
    img = Image.new("RGB", (900, 280), C_BG)
    draw = ImageDraw.Draw(img)
    f_title = get_font(16, True)
    f_box = get_font(12, True)
    f_sub = get_font(10, False)

    draw.text((450, 20), "6-Digit OTP Verification State Machine Workflow", fill=C_TEXT_DARK, font=f_title, anchor="mm")

    steps = [
        ("User Auth Request", "Submit Email & Role"),
        ("Generate OTP", "6-Digit Code & 5m Expiry"),
        ("Render OTP Overlay", "6 Auto-Focus Input Boxes"),
        ("Code Validation", "Match with Server Session"),
        ("Access Granted", "Redirect to Dashboard")
    ]

    for i, (title, desc) in enumerate(steps):
        x = 30 + i * 170
        draw_rounded_rect(draw, [x, 65, x + 140, 220], 10, fill=C_WHITE, outline=C_GOLD, width=2)
        draw_rounded_rect(draw, [x, 65, x + 140, 100], 10, fill=C_GOLD)
        draw.text((x + 70, 82.5), title, fill=C_DARK, font=f_box, anchor="mm")
        draw.text((x + 70, 150), desc, fill=C_TEXT_DARK, font=f_sub, anchor="mm")

        if i < 4:
            draw.polygon([(x + 145, 137.5), (x + 162, 137.5), (x + 162, 133.5), (x + 168, 142.5), (x + 162, 151.5), (x + 162, 147.5), (x + 145, 147.5)], fill=C_TEAL)

    path = os.path.join(assets_dir, "fig_4_2.png")
    img.save(path)

# 5. Figure 5.1: UI Mockup Diagram
def make_fig_5_1():
    img = Image.new("RGB", (900, 420), C_DARK)
    draw = ImageDraw.Draw(img)
    f_title = get_font(16, True)
    f_sub = get_font(11, False)

    # Header bar
    draw_rounded_rect(draw, [15, 15, 885, 60], 8, fill=(15, 23, 42), outline=C_GOLD, width=1)
    draw.text((30, 37.5), "⚡ SUP CHARGED - Smart Grid EV Locator Dashboard", fill=C_GOLD, font=f_title, anchor="lm")
    draw.text((860, 37.5), "GPS: Nellore (14.4426, 79.9865)", fill=C_WHITE, font=f_sub, anchor="rm")

    # Left Sidebar
    draw_rounded_rect(draw, [15, 75, 320, 405], 8, fill=(15, 23, 42), outline=C_BORDER, width=1)
    draw.text((30, 95), "Nearby Charging Stations", fill=C_TEAL, font=get_font(13, True))
    
    stations = ["1. Tata Power EZ Charge - Nellore Bypass", "2. Jio-bp pulse - Railway Station", "3. Ather Grid - Dargamitta", "4. Zeon Charging - VRC Centre"]
    for i, st in enumerate(stations):
        draw_rounded_rect(draw, [25, 120 + i * 65, 310, 175 + i * 65], 6, fill=(30, 41, 59), outline=C_GOLD if i==0 else C_BORDER, width=1)
        draw.text((35, 135 + i * 65), st, fill=C_WHITE, font=get_font(10, True))
        draw.text((35, 155 + i * 65), "Speed: 150kW | Status: Available | Rate: ₹17.5/kWh", fill=C_TEXT_MUTED, font=get_font(9, False))

    # Right Map Area
    draw_rounded_rect(draw, [335, 75, 885, 405], 8, fill=(30, 41, 59), outline=C_TEAL, width=1)
    draw.text((610, 240), "[ Interactive Leaflet Map Canvas - Nellore Coordinates ]", fill=C_TEXT_MUTED, font=get_font(14, True), anchor="mm")
    
    # Map Pins
    draw.ellipse([450, 150, 475, 175], fill=C_GOLD, outline=C_WHITE, width=2)
    draw.text((462.5, 162.5), "⚡", fill=C_DARK, font=get_font(10, True), anchor="mm")
    draw.ellipse([600, 280, 625, 305], fill=C_TEAL, outline=C_WHITE, width=2)
    draw.text((612.5, 292.5), "⚡", fill=C_WHITE, font=get_font(10, True), anchor="mm")

    path = os.path.join(assets_dir, "fig_5_1.png")
    img.save(path)

# 6. Figure 6.1: Testing & Deployment Pipeline
def make_fig_6_1():
    img = Image.new("RGB", (900, 240), C_BG)
    draw = ImageDraw.Draw(img)
    f_title = get_font(16, True)
    f_box = get_font(12, True)
    f_sub = get_font(10, False)

    draw.text((450, 20), "Testing and Production Deployment Pipeline", fill=C_TEXT_DARK, font=f_title, anchor="mm")

    steps = [
        ("Unit Testing", "API & Form Verification"),
        ("Integration Testing", "Auth & OTP Workflows"),
        ("Vite Production Build", "Minify Assets into dist/"),
        ("Public Integration", "Copy to backend/public"),
        ("Live Server Host", "Port 5000 / PM2 Deployment")
    ]

    for i, (title, desc) in enumerate(steps):
        x = 25 + i * 172
        draw_rounded_rect(draw, [x, 60, x + 142, 200], 10, fill=C_WHITE, outline=C_TEAL, width=2)
        draw_rounded_rect(draw, [x, 60, x + 142, 95], 10, fill=C_TEAL)
        draw.text((x + 71, 77.5), title, fill=C_WHITE, font=f_box, anchor="mm")
        draw.text((x + 71, 140), desc, fill=C_TEXT_DARK, font=f_sub, anchor="mm")

        if i < 4:
            draw.polygon([(x + 146, 125), (x + 164, 125), (x + 164, 121), (x + 170, 130), (x + 164, 139), (x + 164, 135), (x + 146, 135)], fill=C_GOLD)

    path = os.path.join(assets_dir, "fig_6_1.png")
    img.save(path)

# 7. Figure 7.1 & 7.2: Dashboards
def make_fig_7_1():
    img = Image.new("RGB", (900, 360), C_BG)
    draw = ImageDraw.Draw(img)
    draw_rounded_rect(draw, [20, 20, 880, 340], 12, fill=C_WHITE, outline=C_TEAL, width=2)
    draw.text((450, 50), "Figure 7.1 Driver Search & Map Locator Dashboard", fill=C_TEAL, font=get_font(18, True), anchor="mm")
    
    # Mock UI Elements
    draw_rounded_rect(draw, [50, 90, 420, 300], 8, fill=(241, 245, 249), outline=C_BORDER, width=1)
    draw.text((235, 115), "Interactive Route Planner & Filter Controls", fill=C_TEXT_DARK, font=get_font(12, True), anchor="mm")
    draw.text((235, 180), "• Live Distance Calculation (Haversine)\n• Dynamic Battery SoC Indicator\n• Connector Compatibility Filters", fill=C_TEXT_MUTED, font=get_font(11, False), anchor="mm")

    draw_rounded_rect(draw, [450, 90, 850, 300], 8, fill=(15, 23, 42), outline=C_GOLD, width=1)
    draw.text((650, 115), "Real-Time Nellore Map Canvas", fill=C_GOLD, font=get_font(12, True), anchor="mm")
    draw.text((650, 180), "• Pulsing Green (Available) & Orange (In-Use) Pins\n• Route Polyline Overlay from GPS to Station\n• Custom Leaflet Infowindow Popups with Nav Action", fill=C_WHITE, font=get_font(11, False), anchor="mm")

    path = os.path.join(assets_dir, "fig_7_1.png")
    img.save(path)

def make_fig_7_2():
    img = Image.new("RGB", (900, 360), C_BG)
    draw = ImageDraw.Draw(img)
    draw_rounded_rect(draw, [20, 20, 880, 340], 12, fill=C_WHITE, outline=C_GOLD, width=2)
    draw.text((450, 50), "Figure 7.2 Operator Command Center & Real-Time History", fill=C_GOLD, font=get_font(18, True), anchor="mm")

    # Mock UI Elements
    kpis = [("Total Revenue", "₹1,45,280"), ("Energy Sold", "8.4 MWh"), ("Active Plugs", "18 / 24"), ("Avg Reliability", "96.4%")]
    for i, (k, v) in enumerate(kpis):
        x = 50 + i * 200
        draw_rounded_rect(draw, [x, 90, x + 180, 170], 8, fill=(15, 23, 42), outline=C_TEAL, width=1)
        draw.text((x + 90, 115), k, fill=C_TEXT_MUTED, font=get_font(10, True), anchor="mm")
        draw.text((x + 90, 145), v, fill=C_GOLD, font=get_font(16, True), anchor="mm")

    draw_rounded_rect(draw, [50, 190, 850, 310], 8, fill=(241, 245, 249), outline=C_BORDER, width=1)
    draw.text((450, 215), "Live Station Management & Operator Payout Log", fill=C_TEXT_DARK, font=get_font(12, True), anchor="mm")
    draw.text((450, 260), "• Real-Time Queue Length Updates   • Grid Surge Status Controls   • Operator Wallet Withdrawal", fill=C_TEXT_MUTED, font=get_font(11, False), anchor="mm")

    path = os.path.join(assets_dir, "fig_7_2.png")
    img.save(path)

# 8. Figure 8.1 & 8.2: Outcomes & Roadmap
def make_fig_8_1():
    img = Image.new("RGB", (900, 240), C_BG)
    draw = ImageDraw.Draw(img)
    draw.text((450, 25), "Summary of Project Outcomes", fill=C_TEXT_DARK, font=get_font(16, True), anchor="mm")

    outcomes = [
        ("Full-Stack Engine", "Vite + Express Integrated"),
        ("Live SoC Simulator", "Dynamic Battery Tracking"),
        ("6-Digit OTP Security", "Verified User Auth"),
        ("Smart Grid Locator", "Haversine GPS Routing")
    ]
    for i, (title, desc) in enumerate(outcomes):
        x = 40 + i * 210
        draw_rounded_rect(draw, [x, 60, x + 190, 190], 10, fill=C_WHITE, outline=C_TEAL, width=2)
        draw_rounded_rect(draw, [x, 60, x + 190, 95], 10, fill=C_TEAL)
        draw.text((x + 95, 77.5), title, fill=C_WHITE, font=get_font(12, True), anchor="mm")
        draw.text((x + 95, 135), desc, fill=C_TEXT_DARK, font=get_font(10, False), anchor="mm")

    path = os.path.join(assets_dir, "fig_8_1.png")
    img.save(path)

def make_fig_8_2():
    img = Image.new("RGB", (900, 240), C_BG)
    draw = ImageDraw.Draw(img)
    draw.text((450, 25), "Future Scope Roadmap of Sup Charged", fill=C_TEXT_DARK, font=get_font(16, True), anchor="mm")

    roadmap = [
        ("Phase 1: IoT Sensors", "Automated Plug Hardware Sync"),
        ("Phase 2: AI Demand Predict", "Machine Learning Grid Forecast"),
        ("Phase 3: EV Payment SDK", "UPI & Automated Auto-Charge"),
        ("Phase 4: Multi-City Expansion", "Pan-India Smart Grid Network")
    ]
    for i, (title, desc) in enumerate(roadmap):
        x = 40 + i * 210
        draw_rounded_rect(draw, [x, 60, x + 190, 190], 10, fill=C_WHITE, outline=C_GOLD, width=2)
        draw_rounded_rect(draw, [x, 60, x + 190, 95], 10, fill=C_GOLD)
        draw.text((x + 95, 77.5), title, fill=C_DARK, font=get_font(11, True), anchor="mm")
        draw.text((x + 95, 135), desc, fill=C_TEXT_DARK, font=get_font(10, False), anchor="mm")

    path = os.path.join(assets_dir, "fig_8_2.png")
    img.save(path)

if __name__ == "__main__":
    make_fig_2_1()
    make_fig_3_1()
    make_fig_4_1()
    make_fig_4_2()
    make_fig_5_1()
    make_fig_6_1()
    make_fig_7_1()
    make_fig_7_2()
    make_fig_8_1()
    make_fig_8_2()
    print("All 10 diagram asset figures generated successfully!")
