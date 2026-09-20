/**
 * JD Mart - Core API & Utility Module
 */

const API_BASE = '';

const Auth = {
  getToken() {
    return localStorage.getItem('jd_mart_token');
  },
  setSession(authResponse) {
    localStorage.setItem('jd_mart_token', authResponse.token);
    localStorage.setItem('jd_mart_user', JSON.stringify({
      id: authResponse.id,
      fullName: authResponse.fullName,
      email: authResponse.email,
      mobile: authResponse.mobile,
      roles: authResponse.roles || []
    }));
  },
  getUser() {
    const userJson = localStorage.getItem('jd_mart_user');
    try {
      return userJson ? JSON.parse(userJson) : null;
    } catch (e) {
      return null;
    }
  },
  isLoggedIn() {
    return !!this.getToken();
  },
  isAdmin() {
    const user = this.getUser();
    return user && user.roles && user.roles.includes('ROLE_ADMIN');
  },
  logout() {
    localStorage.removeItem('jd_mart_token');
    localStorage.removeItem('jd_mart_user');
    window.location.href = '/login.html';
  }
};

async function apiRequest(endpoint, method = 'GET', data = null) {
  const headers = {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  };

  const token = Auth.getToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const options = {
    method,
    headers
  };

  if (data && (method === 'POST' || method === 'PUT' || method === 'PATCH')) {
    options.body = JSON.stringify(data);
  }

  try {
    const response = await fetch(`${API_BASE}${endpoint}`, options);
    
    // Handle 401 Unauthorized
    if (response.status === 401) {
      if (Auth.isLoggedIn()) {
        Auth.logout();
      }
    }

    const json = await response.json();
    if (!response.ok) {
      let errMsg = json.message || 'An error occurred';
      if (json.data && typeof json.data === 'object') {
        const fieldErrors = Object.values(json.data).join(', ');
        if (fieldErrors) errMsg = fieldErrors;
      }
      throw new Error(errMsg);
    }

    return json;
  } catch (err) {
    console.error(`API Error on [${method}] ${endpoint}:`, err);
    throw err;
  }
}

function formatINR(amount) {
  if (amount === null || amount === undefined) return '₹0';
  return '₹' + Number(amount).toLocaleString('en-IN', {
    maximumFractionDigits: 2,
    minimumFractionDigits: 0
  });
}

function showToast(message, type = 'success') {
  let container = document.getElementById('toastContainer');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toastContainer';
    container.className = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;

  const icon = type === 'success' ? '✓' : (type === 'error' ? '✕' : 'ℹ');
  toast.innerHTML = `
    <span style="font-weight:bold; font-size:1.1rem;">${icon}</span>
    <span>${message}</span>
  `;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}
