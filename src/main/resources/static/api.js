/**
 * Smart EV Client API Helper & Authentication Utility
 */
const API_BASE = '/api';

const auth = {
    getToken() {
        return localStorage.getItem('smartev_token');
    },

    setSession(token, user) {
        localStorage.setItem('smartev_token', token);
        localStorage.setItem('smartev_user', JSON.stringify(user));
    },

    getUser() {
        const u = localStorage.getItem('smartev_user');
        return u ? JSON.parse(u) : null;
    },

    logout() {
        localStorage.removeItem('smartev_token');
        localStorage.removeItem('smartev_user');
        window.location.href = '/login.html';
    },

    isAuthenticated() {
        return !!this.getToken();
    },

    checkRoleGuard(allowedRoles = []) {
        if (!this.isAuthenticated()) {
            window.location.href = '/login.html';
            return false;
        }
        const user = this.getUser();
        if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
            alert('Access Denied: You do not have permission for this portal.');
            this.redirectDashboard(user.role);
            return false;
        }
        return true;
    },

    redirectDashboard(role) {
        switch (role) {
            case 'PLATFORM_ADMIN':
                window.location.href = '/platform-dashboard.html';
                break;
            case 'COMPANY_ADMIN':
                window.location.href = '/company-dashboard.html';
                break;
            case 'STATION_MANAGER':
                window.location.href = '/manager-dashboard.html';
                break;
            case 'DRIVER':
            default:
                window.location.href = '/driver-dashboard.html';
                break;
        }
    }
};

/**
 * Universal Fetch wrapper that attaches JWT headers and parses JSON
 */
async function apiFetch(endpoint, options = {}) {
    const headers = options.headers || {};
    if (!headers['Content-Type'] && !(options.body instanceof FormData)) {
        headers['Content-Type'] = 'application/json';
    }

    const token = auth.getToken();
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
        ...options,
        headers
    };

    try {
        const response = await fetch(`${API_BASE}${endpoint}`, config);

        if (response.status === 401) {
            auth.logout();
            throw new Error('Session expired. Please log in again.');
        }

        // Handle binary files (like PDF downloads)
        if (response.headers.get('content-type')?.includes('application/pdf')) {
            if (!response.ok) throw new Error('Failed to download document');
            return response.blob();
        }

        const data = await response.json().catch(() => null);

        if (!response.ok) {
            const errorMsg = data?.message || data?.error || `Request failed with status ${response.status}`;
            throw new Error(errorMsg);
        }

        return data;
    } catch (err) {
        console.error('API Error:', err);
        throw err;
    }
}

/**
 * Shared UI Toast / Alert Helper
 */
function showAlert(message, type = 'success', containerId = 'alert-container') {
    const container = document.getElementById(containerId);
    if (!container) {
        alert(message);
        return;
    }
    const alertHtml = `
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;
    container.innerHTML = alertHtml;
    setTimeout(() => {
        const alertEl = container.querySelector('.alert');
        if (alertEl) {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alertEl);
            bsAlert.close();
        }
    }, 5000);
}

/**
 * Format currency in Indian Rupees
 */
function formatCurrency(amount) {
    if (amount === null || amount === undefined) return '₹0.00';
    return '₹' + Number(amount).toFixed(2);
}

/**
 * Format date time
 */
function formatDateTime(dtStr) {
    if (!dtStr) return '-';
    const d = new Date(dtStr);
    return d.toLocaleString('en-IN', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}
