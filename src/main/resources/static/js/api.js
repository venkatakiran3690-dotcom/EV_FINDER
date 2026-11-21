/**
 * Smart EV Client API Helper & Authentication Utility
 * Includes Theme Management (Dark/Light), Animated Counters, Button Loading States, and Toasts
 */
const API_BASE = (window.location.protocol === 'file:') ? 'http://localhost:8080/api' : '/api';

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
        showToast('Signing out of Smart EV session...', 'info');
        setTimeout(() => {
            localStorage.removeItem('smartev_token');
            localStorage.removeItem('smartev_user');
            window.location.href = '/login.html';
        }, 300);
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
 * Theme Switcher (Dark / Light Theme with LocalStorage Persistence)
 */
const theme = {
    getTheme() {
        const saved = localStorage.getItem('smartev_theme');
        if (saved) return saved;
        return (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) ? 'dark' : 'light';
    },

    setTheme(newTheme) {
        document.documentElement.setAttribute('data-bs-theme', newTheme);
        document.documentElement.setAttribute('data-theme', newTheme);
        document.body.setAttribute('data-bs-theme', newTheme);
        localStorage.setItem('smartev_theme', newTheme);

        // Update toggle icons on page if they exist
        const toggleBtns = document.querySelectorAll('.theme-toggle-btn, #themeToggleBtn');
        toggleBtns.forEach(btn => {
            if (newTheme === 'dark') {
                btn.innerHTML = '<i class="bi bi-sun-fill text-warning"></i>';
                btn.setAttribute('title', 'Switch to Light Theme');
                btn.setAttribute('aria-label', 'Switch to Light Theme');
            } else {
                btn.innerHTML = '<i class="bi bi-moon-stars-fill text-info"></i>';
                btn.setAttribute('title', 'Switch to Dark Theme');
                btn.setAttribute('aria-label', 'Switch to Dark Theme');
            }
        });

        window.dispatchEvent(new CustomEvent('themeChanged', { detail: { theme: newTheme } }));
    },

    toggle() {
        const current = this.getTheme();
        const next = current === 'dark' ? 'light' : 'dark';
        this.setTheme(next);
        showToast(`Theme switched to ${next === 'dark' ? 'Dark' : 'Light'} mode`, 'info');
    },

    init() {
        this.setTheme(this.getTheme());
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
 * Button Loading States (Async Feedback & Duplicate Click Prevention)
 */
function setButtonLoading(btn, loadingText = 'Processing...', spinnerClass = 'spinner-border spinner-border-sm') {
    if (!btn) return;
    btn.dataset.origHtml = btn.innerHTML;
    btn.disabled = true;
    btn.classList.add('btn-loading');
    btn.innerHTML = `<span class="${spinnerClass} me-2" role="status" aria-hidden="true"></span> ${loadingText}`;
}

function restoreButton(btn) {
    if (!btn) return;
    if (btn.dataset.origHtml) {
        btn.innerHTML = btn.dataset.origHtml;
    }
    btn.disabled = false;
    btn.classList.remove('btn-loading');
}

/**
 * Smooth Animated Counter for Numbers
 */
function animateCounter(elOrId, targetValue, duration = 1200, prefix = '', suffix = '', decimals = 0) {
    const el = (typeof elOrId === 'string') ? document.getElementById(elOrId) : elOrId;
    if (!el) return;

    const start = 0;
    const end = parseFloat(targetValue) || 0;
    const startTime = performance.now();

    function updateCounter(currentTime) {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);

        // Ease out cubic: 1 - Math.pow(1 - progress, 3)
        const easeProgress = 1 - Math.pow(1 - progress, 3);
        const currentVal = start + (end - start) * easeProgress;

        el.innerText = `${prefix}${currentVal.toFixed(decimals)}${suffix}`;

        if (progress < 1) {
            requestAnimationFrame(updateCounter);
        } else {
            el.innerText = `${prefix}${end.toFixed(decimals)}${suffix}`;
        }
    }

    requestAnimationFrame(updateCounter);
}

/**
 * Shared UI Alert Helper
 */
function showAlert(message, type = 'success', containerId = 'alert-container') {
    const container = document.getElementById(containerId);
    if (!container) {
        showToast(message, type);
        return;
    }
    const iconClass = type === 'success' ? 'bi-check-circle-fill' :
                      type === 'danger' ? 'bi-exclamation-triangle-fill' :
                      type === 'warning' ? 'bi-exclamation-circle-fill' : 'bi-info-circle-fill';

    const alertHtml = `
        <div class="alert alert-${type} alert-dismissible fade show d-flex align-items-center rounded-3 shadow-sm" role="alert">
            <i class="bi ${iconClass} me-2 fs-5"></i>
            <div class="flex-grow-1">${message}</div>
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
 * Floating Toast Notification
 */
function showToast(message, type = 'success') {
    let container = document.getElementById('smartev-toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'smartev-toast-container';
        container.className = 'toast-container-custom';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = 'toast-custom';
    const icon = type === 'success' ? '<i class="bi bi-check-circle-fill text-success fs-5"></i>' :
                 type === 'danger' ? '<i class="bi bi-x-circle-fill text-danger fs-5"></i>' :
                 type === 'warning' ? '<i class="bi bi-exclamation-triangle-fill text-warning fs-5"></i>' :
                 '<i class="bi bi-info-circle-fill text-info fs-5"></i>';

    toast.innerHTML = `
        ${icon}
        <div class="flex-grow-1 small fw-semibold">${message}</div>
    `;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(-10px)';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
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

/**
 * Format time only
 */
function formatTime(dtStr) {
    if (!dtStr) return '-';
    const d = new Date(dtStr);
    return d.toLocaleTimeString('en-IN', {
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Auto-initialize theme on script evaluation
document.addEventListener('DOMContentLoaded', () => {
    theme.init();
});
