const express = require('express');
const cors = require('cors');
const fs = require('fs');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 5000;

app.use(cors());
app.use(express.json());

// Load initial stations from database
const DATA_DIR = path.join(__dirname, 'data');
try {
  if (!fs.existsSync(DATA_DIR)) {
    fs.mkdirSync(DATA_DIR, { recursive: true });
  }
} catch (err) {
  console.warn('Warning: Could not create data directory. File persistence might fail.', err.message);
}

const STATIONS_FILE = path.join(DATA_DIR, 'stations.json');
const BOOKINGS_FILE = path.join(DATA_DIR, 'bookings.json');
const WALLET_FILE = path.join(DATA_DIR, 'wallet.json');
let stations = [];
let bookings = [];

// Default stations data fallback in case database files are missing/read-only on live hosting
const DEFAULT_STATIONS = [
  {
    "id": "station-1",
    "name": "Tata Power EZ Charge - Nellore Bypass",
    "address": "NH16 Nellore Bypass, Nellore, Andhra Pradesh 524004",
    "lat": 14.4605,
    "lng": 79.9592,
    "operator": "Tata Power",
    "rating": 4.7,
    "reliability": 92,
    "gridStatus": "Solar Power Backup",
    "openingHours": "24/7",
    "queueLength": 1,
    "peakHours": [15, 25, 45, 75, 90, 98, 85, 70, 65, 55, 35, 20],
    "amenities": ["WiFi", "Coffee", "Restrooms", "Shopping"],
    "connectors": [
      { "type": "CCS", "power": 150, "price": 17.5, "status": "Available", "count": 6 },
      { "type": "Type 2", "power": 22, "price": 17.5, "status": "Available", "count": 4 }
    ],
    "status": "Available",
    "isFavorite": true,
    "reviews": [
      {
        "username": "Aero_Bolt",
        "rating": 5,
        "comment": "Super fast charger near the bypass. Very convenient on the highway.",
        "date": "2026-06-12"
      }
    ]
  },
  {
    "id": "station-2",
    "name": "Jio-bp pulse - Nellore Railway Station",
    "address": "Station Road, Nellore, Andhra Pradesh 524001",
    "lat": 14.4468,
    "lng": 79.9811,
    "operator": "Jio-bp",
    "rating": 4.5,
    "reliability": 94,
    "gridStatus": "Stable Grid",
    "openingHours": "24/7",
    "queueLength": 2,
    "peakHours": [30, 45, 60, 85, 95, 90, 80, 85, 75, 60, 40, 20],
    "amenities": ["Dining", "Coffee", "Restrooms"],
    "connectors": [
      { "type": "CCS", "power": 120, "price": 19.5, "status": "Available", "count": 4 },
      { "type": "CHAdeMO", "power": 50, "price": 16, "status": "Available", "count": 2 }
    ],
    "status": "Available",
    "isFavorite": false,
    "reviews": [
      {
        "username": "Ravi_K",
        "rating": 5,
        "comment": "Right outside the railway station. Easy to locate.",
        "date": "2026-06-15"
      }
    ]
  },
  {
    "id": "station-3",
    "name": "Ather Grid - Dargamitta",
    "address": "Mini Bypass Road, Dargamitta, Nellore, Andhra Pradesh 524003",
    "lat": 14.4324,
    "lng": 79.9721,
    "operator": "Ather",
    "rating": 4.8,
    "reliability": 96,
    "gridStatus": "Stable Grid",
    "openingHours": "08:00 - 22:00",
    "queueLength": 0,
    "peakHours": [20, 35, 55, 80, 95, 96, 92, 85, 80, 70, 50, 25],
    "amenities": ["Shopping", "Dining", "Restrooms"],
    "connectors": [
      { "type": "Type 2", "power": 22, "price": 15, "status": "Available", "count": 8 }
    ],
    "status": "Available",
    "isFavorite": true,
    "reviews": [
      {
        "username": "Nellore_Scooter",
        "rating": 5,
        "comment": "Perfect place to charge two-wheelers. Fast charging works well.",
        "date": "2026-06-20"
      }
    ]
  },
  {
    "id": "station-4",
    "name": "Zeon Charging - VRC Centre",
    "address": "VRC Centre, Trunk Road, Nellore, Andhra Pradesh 524001",
    "lat": 14.4452,
    "lng": 79.9868,
    "operator": "Zeon Charging",
    "rating": 4.8,
    "reliability": 97,
    "gridStatus": "Stable Grid",
    "openingHours": "24/7",
    "queueLength": 0,
    "peakHours": [10, 15, 30, 50, 75, 80, 70, 65, 85, 90, 60, 30],
    "amenities": ["Shopping", "WiFi", "Dining"],
    "connectors": [
      { "type": "CCS", "power": 240, "price": 22, "status": "Available", "count": 4 }
    ],
    "status": "Available",
    "isFavorite": false,
    "reviews": [
      {
        "username": "Travel_Nellore",
        "rating": 5,
        "comment": "240 kW charger at the heart of the city. Charged within 15 minutes.",
        "date": "2026-06-21"
      }
    ]
  },
  {
    "id": "station-5",
    "name": "Statcon Energiaa - Podalakur Road",
    "address": "Podalakur Road, Nellore, Andhra Pradesh 524004",
    "lat": 14.4285,
    "lng": 79.9605,
    "operator": "Statcon",
    "rating": 4.1,
    "reliability": 82,
    "gridStatus": "Reduced Load - Peak Demand",
    "openingHours": "09:00 - 21:00",
    "queueLength": 1,
    "peakHours": [40, 50, 65, 75, 80, 85, 75, 70, 82, 80, 60, 45],
    "amenities": ["WiFi", "Dining"],
    "connectors": [
      { "type": "CCS", "power": 60, "price": 17, "status": "Available", "count": 2 }
    ],
    "status": "Available",
    "isFavorite": false,
    "reviews": [
      {
        "username": "Local_EV",
        "rating": 3,
        "comment": "Speeds drop slightly in the evening. Decent otherwise.",
        "date": "2026-06-11"
      }
    ]
  },
  {
    "id": "station-6",
    "name": "Fortum Charge & Drive - Magunta Layout",
    "address": "Magunta Layout, Nellore, Andhra Pradesh 524003",
    "lat": 14.4371,
    "lng": 79.9798,
    "operator": "Fortum",
    "rating": 4.4,
    "reliability": 90,
    "gridStatus": "Stable Grid",
    "openingHours": "06:00 - 23:00",
    "queueLength": 2,
    "peakHours": [25, 40, 60, 75, 85, 90, 80, 82, 75, 70, 50, 30],
    "amenities": ["Shopping", "Restrooms", "Coffee"],
    "connectors": [
      { "type": "CCS", "power": 100, "price": 19, "status": "In Use", "count": 3 },
      { "type": "Type 2", "power": 22, "price": 13, "status": "Available", "count": 2 }
    ],
    "status": "Available",
    "isFavorite": false,
    "reviews": [
      {
        "username": "Subbu_AP",
        "rating": 4,
        "comment": "Nice location. Coffee shop is right across the street.",
        "date": "2026-06-18"
      }
    ]
  },
  {
    "id": "station-7",
    "name": "Tata Power EZ Charge - Haranathapuram",
    "address": "Haranathapuram Main Road, Nellore, Andhra Pradesh 524003",
    "lat": 14.4552,
    "lng": 79.9754,
    "operator": "Tata Power",
    "rating": 4.6,
    "reliability": 95,
    "gridStatus": "Solar Power Backup",
    "openingHours": "24/7",
    "queueLength": 0,
    "peakHours": [10, 20, 40, 60, 75, 90, 80, 75, 70, 60, 45, 20],
    "amenities": ["Park", "Coffee", "Restrooms"],
    "connectors": [
      { "type": "CCS", "power": 150, "price": 18.5, "status": "Available", "count": 4 }
    ],
    "status": "Available",
    "isFavorite": false,
    "reviews": [
      {
        "username": "Kiran_EV",
        "rating": 5,
        "comment": "Excellent solar-backed grid power. Very green setup.",
        "date": "2026-06-20"
      }
    ]
  },
  {
    "id": "station-ap-6",
    "name": "Statiq Charging Station - Nellore Central",
    "address": "Grand Trunk Road, Nellore, Andhra Pradesh 524001",
    "lat": 14.449,
    "lng": 79.982,
    "operator": "Statiq",
    "rating": 4.5,
    "reliability": 92,
    "gridStatus": "Stable Grid",
    "openingHours": "08:00 - 22:00",
    "queueLength": 0,
    "peakHours": [10, 25, 45, 60, 80, 90, 85, 75, 65, 55, 35, 10],
    "amenities": ["Shopping", "Dining", "Restrooms"],
    "connectors": [
      { "type": "CCS", "power": 120, "price": 18.5, "status": "Available", "count": 2 }
    ],
    "status": "Available",
    "isFavorite": false,
    "reviews": [
      {
        "username": "Nellore_EV",
        "rating": 5,
        "comment": "Nice facility, charging got connected instantly.",
        "date": "2026-06-21"
      }
    ]
  }
];

// Default wallet data
let walletData = {
  balance: 1500.00,
  withdrawnAmount: 0,
  transactions: [
    { id: 'topup-init', type: 'Wallet Topup', amount: 1500, date: '2026-05-20', funding: 'Visa ****5678' }
  ],
  savedCards: [
    { id: 'card-1', number: '4532789012345678', expiry: '12/28', cvv: '123', holder: 'Bangalore Rider', brand: 'Visa' },
    { id: 'card-2', number: '6071829304152637', expiry: '06/29', cvv: '987', holder: 'Bangalore Rider', brand: 'RuPay' }
  ],
  savedUpis: [
    { id: 'upi-1', address: 'rider@okaxis' },
    { id: 'upi-2', address: '9876543210@paytm' }
  ]
};

// Fail-safe loaders
try {
  if (fs.existsSync(STATIONS_FILE)) {
    const data = fs.readFileSync(STATIONS_FILE, 'utf8');
    stations = JSON.parse(data);
    console.log(`Loaded ${stations.length} stations from data store.`);
  } else {
    stations = DEFAULT_STATIONS;
    console.log("Database file stations.json not found. Using default in-memory stations.");
  }
} catch (error) {
  console.warn('Warning: Failed to load stations data. Using default in-memory stations fallback.', error.message);
  stations = DEFAULT_STATIONS;
}

try {
  if (fs.existsSync(BOOKINGS_FILE)) {
    const data = fs.readFileSync(BOOKINGS_FILE, 'utf8');
    bookings = JSON.parse(data);
    console.log(`Loaded ${bookings.length} bookings from data store.`);
  } else {
    bookings = [];
    try {
      fs.writeFileSync(BOOKINGS_FILE, JSON.stringify([], null, 2), 'utf8');
    } catch (e) {
      console.warn("Warning: Read-only filesystem detected. Bookings will persist in memory only.");
    }
  }
} catch (error) {
  console.warn('Warning: Failed to load bookings data. Running bookings in-memory fallback.', error.message);
  bookings = [];
}

try {
  if (fs.existsSync(WALLET_FILE)) {
    const data = fs.readFileSync(WALLET_FILE, 'utf8');
    walletData = JSON.parse(data);
    console.log("Loaded wallet data from data store.");
  } else {
    try {
      fs.writeFileSync(WALLET_FILE, JSON.stringify(walletData, null, 2), 'utf8');
    } catch (e) {
      console.warn("Warning: Read-only filesystem detected. Wallet will persist in memory only.");
    }
  }
} catch (error) {
  console.warn('Warning: Failed to load wallet data. Running wallet in-memory fallback.', error.message);
}

// Helper to save wallet to file
const saveWalletToFile = () => {
  try {
    fs.writeFileSync(WALLET_FILE, JSON.stringify(walletData, null, 2), 'utf8');
  } catch (error) {
    console.error('Error writing to wallet database:', error);
  }
};

// Helper to save stations to file
const saveStationsToFile = () => {
  try {
    fs.writeFileSync(STATIONS_FILE, JSON.stringify(stations, null, 2), 'utf8');
  } catch (error) {
    console.error('Error writing to stations database:', error);
  }
};

// Helper to save bookings to file
const saveBookingsToFile = () => {
  try {
    fs.writeFileSync(BOOKINGS_FILE, JSON.stringify(bookings, null, 2), 'utf8');
  } catch (error) {
    console.error('Error writing to bookings database:', error);
  }
};

// Users mock database
// Users mock database
let users = [
  { username: 'Rider Bangalore', email: 'driver@sup.in', password: 'password', role: 'driver', phone: '+91 9876543210' },
  { username: 'Tata Operator', email: 'operator@sup.in', password: 'password', role: 'retailer', phone: '+91 9123456780' }
];

// Active OTP session storage: email -> { code, expiresAt, type, payload }
let activeOtps = {};

// Helper to generate a random 6-digit OTP
function generateOTP() {
  return Math.floor(100000 + Math.random() * 900000).toString();
}

// Auth Endpoints
app.post('/api/auth/register', (req, res) => {
  const { username, email, password, role, phone } = req.body;
  if (!username || !email || !password || !role || !phone) {
    return res.status(400).json({ message: 'All registration fields are required.' });
  }
  if (users.some(u => u.email === email)) {
    return res.status(400).json({ message: 'An account with this email already exists.' });
  }
  
  const code = generateOTP();
  const expiresAt = Date.now() + 5 * 60 * 1000; // 5 mins validity
  
  activeOtps[email] = {
    code,
    expiresAt,
    type: 'register',
    payload: { username, email, password, role, phone }
  };
  
  console.log(`\n==============================================`);
  console.log(`[OTP SERVICE] Registration OTP for ${email} is ${code}`);
  console.log(`==============================================\n`);
  
  res.status(200).json({ otpRequired: true, email, phone, code });
});

app.post('/api/auth/login', (req, res) => {
  const { email, password, role } = req.body;
  if (!email || !password || !role) {
    return res.status(400).json({ message: 'Credentials and role type required.' });
  }
  const user = users.find(u => u.email === email);
  if (!user) {
    return res.status(401).json({ message: 'No account associated with this email.' });
  }
  if (user.password !== password) {
    return res.status(401).json({ message: 'Incorrect password.' });
  }
  if (user.role !== role) {
    const label = user.role === 'driver' ? 'Driver' : 'Operator';
    return res.status(403).json({ message: `Access Denied: This account is registered as a ${label}. Please use the ${label} login portal.` });
  }

  const code = generateOTP();
  const expiresAt = Date.now() + 5 * 60 * 1000;

  activeOtps[email] = {
    code,
    expiresAt,
    type: 'login',
    payload: { email, role }
  };

  console.log(`\n==============================================`);
  console.log(`[OTP SERVICE] Login OTP for ${email} is ${code}`);
  console.log(`==============================================\n`);

  res.status(200).json({ otpRequired: true, email, phone: user.phone, code });
});

app.post('/api/auth/verify-otp', (req, res) => {
  const { email, otp, role } = req.body;
  if (!email || !otp || !role) {
    return res.status(400).json({ message: 'Email, OTP code, and role type are required.' });
  }

  const record = activeOtps[email];
  if (!record) {
    return res.status(400).json({ message: 'No active OTP verification session found. Please request a new code.' });
  }

  if (Date.now() > record.expiresAt) {
    delete activeOtps[email];
    return res.status(400).json({ message: 'OTP code has expired. Please request a new code.' });
  }

  if (record.code !== otp.trim()) {
    return res.status(400).json({ message: 'Incorrect OTP code. Please verify and try again.' });
  }

  // OTP validated successfully
  if (record.type === 'register') {
    const { username, email: regEmail, password, role: regRole, phone } = record.payload;
    if (users.some(u => u.email === regEmail)) {
      delete activeOtps[email];
      return res.status(400).json({ message: 'An account with this email was registered during verification.' });
    }
    const newUser = { username, email: regEmail, password, role: regRole, phone };
    users.push(newUser);
    delete activeOtps[email];
    return res.json({ username, email: regEmail, role: regRole, phone });
  } else {
    // Login path
    const user = users.find(u => u.email === email);
    if (!user) {
      delete activeOtps[email];
      return res.status(400).json({ message: 'User account not found.' });
    }
    delete activeOtps[email];
    return res.json({ username: user.username, email: user.email, role: user.role, phone: user.phone });
  }
});

app.post('/api/auth/resend-otp', (req, res) => {
  const { email } = req.body;
  if (!email) {
    return res.status(400).json({ message: 'Email address is required.' });
  }

  const record = activeOtps[email];
  if (!record) {
    return res.status(400).json({ message: 'No active verification session found. Please go back and try again.' });
  }

  const newCode = generateOTP();
  record.code = newCode;
  record.expiresAt = Date.now() + 5 * 60 * 1000;

  console.log(`\n==============================================`);
  console.log(`[OTP SERVICE] Resent OTP for ${email} is ${newCode}`);
  console.log(`==============================================\n`);

  res.json({ message: 'OTP code has been resent successfully.', code: newCode });
});

app.post('/api/auth/change-password', (req, res) => {
  const { email, currentPassword, newPassword } = req.body;
  if (!email || !currentPassword || !newPassword) {
    return res.status(400).json({ message: 'All password fields are required.' });
  }
  const user = users.find(u => u.email === email);
  if (!user) {
    return res.status(404).json({ message: 'User not found.' });
  }
  if (user.password !== currentPassword) {
    return res.status(400).json({ message: 'Incorrect current password.' });
  }
  user.password = newPassword;
  res.json({ message: 'Password updated successfully.' });
});

// Endpoints

// 1. GET /api/stations - Fetch all stations
app.get('/api/stations', (req, res) => {
  res.json(stations);
});

// 2. GET /api/stations/:id - Fetch single station
app.get('/api/stations/:id', (req, res) => {
  const station = stations.find(s => s.id === req.params.id);
  if (station) {
    res.json(station);
  } else {
    res.status(404).json({ message: 'Station not found' });
  }
});

// 3. POST /api/stations/:id/favorite - Toggle favorite
app.post('/api/stations/:id/favorite', (req, res) => {
  const station = stations.find(s => s.id === req.params.id);
  if (station) {
    station.isFavorite = !station.isFavorite;
    saveStationsToFile();
    res.json(station);
  } else {
    res.status(404).json({ message: 'Station not found' });
  }
});

// 4. POST /api/stations/:id/rate - Rate a station (Legacy/Quick rate)
app.post('/api/stations/:id/rate', (req, res) => {
  const { rating } = req.body;
  if (typeof rating !== 'number' || rating < 1 || rating > 5) {
    return res.status(400).json({ message: 'Rating must be a number between 1 and 5' });
  }

  const station = stations.find(s => s.id === req.params.id);
  if (station) {
    const currentRating = station.rating || 4.0;
    station.rating = Math.round(((currentRating * 9 + rating) / 10) * 10) / 10;
    saveStationsToFile();
    res.json(station);
  } else {
    res.status(404).json({ message: 'Station not found' });
  }
});

// 5. POST /api/stations/:id/reviews - Submit review comments
app.post('/api/stations/:id/reviews', (req, res) => {
  const { username, rating, comment } = req.body;
  const ratingNum = parseFloat(rating);
  
  if (isNaN(ratingNum) || ratingNum < 1 || ratingNum > 5) {
    return res.status(400).json({ message: 'Rating must be between 1 and 5' });
  }

  const station = stations.find(s => s.id === req.params.id);
  if (station) {
    const newReview = {
      username: username || 'EV Driver',
      rating: ratingNum,
      comment: comment || '',
      date: new Date().toISOString().split('T')[0]
    };
    
    if (!station.reviews) station.reviews = [];
    station.reviews.unshift(newReview);
    
    // Recalculate average rating
    const total = station.reviews.reduce((acc, r) => acc + r.rating, 0);
    station.rating = parseFloat((total / station.reviews.length).toFixed(1));
    
    saveStationsToFile();
    res.json(station);
  } else {
    res.status(404).json({ message: 'Station not found' });
  }
});

// 6. POST /api/stations/:id/report - Report plug status (Reliability index)
app.post('/api/stations/:id/report', (req, res) => {
  const { type } = req.body; // 'working' or 'broken'
  const station = stations.find(s => s.id === req.params.id);
  
  if (station) {
    if (type === 'broken') {
      station.reliability = Math.max(10, (station.reliability || 90) - 8);
    } else {
      station.reliability = Math.min(100, (station.reliability || 90) + 3);
    }
    saveStationsToFile();
    res.json(station);
  } else {
    res.status(404).json({ message: 'Station not found' });
  }
});

// 6b. POST /api/stations - Add a new station
app.post('/api/stations', (req, res) => {
  const { name, address, lat, lng, operator, gridStatus, openingHours, amenities, connectors } = req.body;
  
  if (!name || !address || !operator || !connectors || !connectors.length) {
    return res.status(400).json({ message: 'Station name, address, operator, and at least one connector details are required.' });
  }

  const newStation = {
    id: `station-${Date.now()}`,
    name,
    address,
    lat: parseFloat(lat) || (14.4426 + (Math.random() * 0.1 - 0.05)),
    lng: parseFloat(lng) || (79.9865 + (Math.random() * 0.1 - 0.05)),
    operator,
    rating: 5.0,
    reliability: 100,
    gridStatus: gridStatus || 'Stable Grid',
    queueLength: 0,
    peakHours: [10, 15, 20, 45, 60, 80, 95, 90, 75, 50, 30, 10],
    amenities: amenities || ['WiFi'],
    connectors: connectors.map(c => ({
      type: c.type,
      power: parseInt(c.power) || 50,
      price: parseFloat(c.price) || 18.0,
      status: c.status || 'Available',
      count: parseInt(c.count) || 1
    })),
    status: 'Available',
    isFavorite: false,
    reviews: []
  };

  if (openingHours) {
    newStation.openingHours = openingHours;
  }

  stations.push(newStation);
  saveStationsToFile();
  
  res.status(201).json(newStation);
});

// 7. PUT /api/stations/:id - Update station details (Retailer console)
app.put('/api/stations/:id', (req, res) => {
  const { name, address, basePrice, gridStatus, connectorsStatus, openingHours, connectors } = req.body;
  const stationIndex = stations.findIndex(s => s.id === req.params.id);
  
  if (stationIndex !== -1) {
    const station = stations[stationIndex];
    if (name) station.name = name;
    if (address) station.address = address;
    if (gridStatus) station.gridStatus = gridStatus;
    if (openingHours !== undefined) station.openingHours = openingHours;
    
    if (connectors && Array.isArray(connectors)) {
      station.connectors = connectors.map(c => ({
        type: c.type,
        power: parseInt(c.power) || 50,
        price: parseFloat(c.price) || 18.0,
        status: c.status || 'Available',
        count: parseInt(c.count) || 1
      }));
    } else {
      if (basePrice !== undefined) {
        const priceNum = parseFloat(basePrice);
        if (!isNaN(priceNum)) {
          station.connectors = station.connectors.map(c => ({
            ...c,
            price: priceNum
          }));
        }
      }

      if (connectorsStatus) {
        // Expecting array of { type, status }
        connectorsStatus.forEach(statusObj => {
          const connector = station.connectors.find(c => c.type === statusObj.type);
          if (connector) {
            connector.status = statusObj.status;
          }
        });
      }
    }

    // Refresh overall station availability status
    const allStatuses = station.connectors.map(c => c.status);
    if (allStatuses.every(s => s === 'Offline')) {
      station.status = 'Offline';
    } else if (allStatuses.some(s => s === 'Available')) {
      station.status = 'Available';
    } else {
      station.status = 'In Use';
    }

    saveStationsToFile();
    res.json(station);
  } else {
    res.status(404).json({ message: 'Station not found' });
  }
});

// 8. POST /api/stations/:id/connector-status - Update status for simulation
app.post('/api/stations/:id/connector-status', (req, res) => {
  const { connectorType, status } = req.body; // status: 'Available', 'In Use', 'Offline'
  const station = stations.find(s => s.id === req.params.id);
  
  if (station) {
    const connector = station.connectors.find(c => c.type === connectorType);
    if (connector) {
      connector.status = status;
      
      const allStatuses = station.connectors.map(c => c.status);
      if (allStatuses.every(s => s === 'Offline')) {
        station.status = 'Offline';
      } else if (allStatuses.some(s => s === 'Available')) {
        station.status = 'Available';
      } else {
        station.status = 'In Use';
      }
      
      saveStationsToFile();
      res.json(station);
    } else {
      res.status(404).json({ message: 'Connector not found' });
    }
  } else {
    res.status(404).json({ message: 'Station not found' });
  }
});

// 9. GET /api/bookings - Get all bookings
app.get('/api/bookings', (req, res) => {
  res.json(bookings);
});

// 10. POST /api/bookings - Create new booking
app.post('/api/bookings', (req, res) => {
  const { stationId, connectorType, timeSlot } = req.body;
  if (!stationId || !connectorType || !timeSlot) {
    return res.status(400).json({ message: 'Missing booking parameters' });
  }

  const station = stations.find(s => s.id === stationId);
  if (!station) {
    return res.status(404).json({ message: 'Station not found' });
  }

  const connector = station.connectors.find(c => c.type === connectorType);
  if (!connector) {
    return res.status(404).json({ message: 'Connector not found' });
  }

  if (connector.status === 'Offline') {
    return res.status(400).json({ message: 'Connector is currently offline' });
  }

  const newBooking = {
    id: `booking-${Date.now()}`,
    stationId,
    stationName: station.name,
    address: station.address,
    connectorType,
    timeSlot,
    priceRate: connector.price,
    powerKwh: connector.power,
    status: 'Confirmed',
    createdAt: new Date().toISOString()
  };

  bookings.unshift(newBooking);
  saveBookingsToFile();
  res.status(201).json(newBooking);
});

// 11. DELETE /api/bookings/:id - Cancel booking
app.delete('/api/bookings/:id', (req, res) => {
  const bookingIndex = bookings.findIndex(b => b.id === req.params.id);
  if (bookingIndex !== -1) {
    bookings[bookingIndex].status = 'Cancelled';
    saveBookingsToFile();
    res.json({ message: 'Booking cancelled successfully', booking: bookings[bookingIndex] });
  } else {
    res.status(404).json({ message: 'Booking not found' });
  }
});

// 11b. PUT /api/bookings/:id - Update booking status and details (payment completion)
app.put('/api/bookings/:id', (req, res) => {
  const bookingIndex = bookings.findIndex(b => b.id === req.params.id);
  if (bookingIndex !== -1) {
    const { status, energyDelivered, accumulatedCost, paymentMethod, completedAt, targetSOC, prepaidAmount, refundAmount, pluggedInAt } = req.body;
    
    if (status) bookings[bookingIndex].status = status;
    if (energyDelivered !== undefined) bookings[bookingIndex].energyDelivered = parseFloat(energyDelivered);
    if (accumulatedCost !== undefined) bookings[bookingIndex].accumulatedCost = parseFloat(accumulatedCost);
    if (paymentMethod !== undefined) bookings[bookingIndex].paymentMethod = paymentMethod;
    if (targetSOC !== undefined) bookings[bookingIndex].targetSOC = parseFloat(targetSOC);
    if (prepaidAmount !== undefined) bookings[bookingIndex].prepaidAmount = parseFloat(prepaidAmount);
    if (refundAmount !== undefined) bookings[bookingIndex].refundAmount = parseFloat(refundAmount);
    if (pluggedInAt !== undefined) bookings[bookingIndex].pluggedInAt = pluggedInAt;
    if (completedAt !== undefined) bookings[bookingIndex].completedAt = completedAt;
    else if (status === 'Completed') bookings[bookingIndex].completedAt = new Date().toISOString();
    
    saveBookingsToFile();
    res.json(bookings[bookingIndex]);
  } else {
    res.status(404).json({ message: 'Booking not found' });
  }
});

// Wallet Simulator API Endpoints
app.get('/api/wallet/data', (req, res) => {
  res.json(walletData);
});

app.post('/api/wallet/pay', (req, res) => {
  const { amount } = req.body;
  const payAmount = parseFloat(amount);
  
  if (isNaN(payAmount) || payAmount < 0) {
    return res.status(400).json({ message: 'Invalid payment amount' });
  }

  if (walletData.balance >= payAmount) {
    walletData.balance = parseFloat((walletData.balance - payAmount).toFixed(2));
    saveWalletToFile();
    res.json({ success: true, balance: walletData.balance });
  } else {
    res.status(400).json({ success: false, message: 'Insufficient balance in SupPay Wallet. Please reload credit.' });
  }
});

app.post('/api/wallet/topup', (req, res) => {
  const { amount } = req.body;
  const addAmount = parseFloat(amount);
  
  if (isNaN(addAmount) || addAmount <= 0) {
    return res.status(400).json({ message: 'Invalid topup amount' });
  }

  walletData.balance = parseFloat((walletData.balance + addAmount).toFixed(2));
  walletData.transactions.unshift({
    id: `topup-${Date.now()}`,
    type: 'Wallet Topup',
    amount: addAmount,
    date: new Date().toISOString().split('T')[0],
    funding: 'Linked Payment Method'
  });
  saveWalletToFile();
  res.json({ success: true, balance: walletData.balance, transactions: walletData.transactions });
});

// Operator Payout API
app.post('/api/wallet/withdraw', (req, res) => {
  const { amount } = req.body;
  const withdrawAmount = parseFloat(amount);

  if (isNaN(withdrawAmount) || withdrawAmount <= 0) {
    return res.status(400).json({ message: 'Invalid withdrawal amount' });
  }

  walletData.withdrawnAmount = parseFloat(((walletData.withdrawnAmount || 0) + withdrawAmount).toFixed(2));
  saveWalletToFile();
  res.json({ success: true, withdrawnAmount: walletData.withdrawnAmount });
});

// Serve static frontend files if they exist in public
const publicPath = path.join(__dirname, 'public');
if (fs.existsSync(publicPath)) {
  app.use(express.static(publicPath));
  app.get('*', (req, res) => {
    if (req.path.startsWith('/api')) {
      return res.status(404).json({ message: 'API route not found' });
    }
    res.sendFile(path.join(publicPath, 'index.html'));
  });
}

// Start Server
app.listen(PORT, () => {
  console.log(`Sup Charged API Server is running on port ${PORT}`);
});
