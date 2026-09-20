/**
 * JD Mart - Global App & Navbar Controller
 */

document.addEventListener('DOMContentLoaded', () => {
  initNavbar();
  updateCartBadge();
  initSearch();
});

function initNavbar() {
  const userActionsEl = document.getElementById('navUserActions');
  if (!userActionsEl) return;

  const user = Auth.getUser();

  if (Auth.isLoggedIn() && user) {
    const isAdmin = Auth.isAdmin();
    userActionsEl.innerHTML = `
      <div class="user-menu-container">
        <button class="user-btn" id="userMenuBtn" onclick="toggleUserDropdown(event)">
          <div class="user-avatar">${escapeHtml(user.fullName ? user.fullName.charAt(0).toUpperCase() : 'U')}</div>
          <span>${escapeHtml(user.fullName.split(' ')[0])}</span>
          <span style="font-size:0.75rem;">▼</span>
        </button>
        <div class="dropdown-menu" id="userDropdown">
          <div style="padding: 0.6rem 1rem; border-bottom: 1px solid #e2e8f0;">
            <div style="font-weight: 700; font-size: 0.9rem;">${escapeHtml(user.fullName)}</div>
            <div style="font-size: 0.75rem; color: #64748b;">${escapeHtml(user.email)}</div>
          </div>
          ${isAdmin ? `
            <a href="/admin-dashboard.html">
              <span>📊</span> Admin Dashboard
            </a>
            <a href="/admin-products.html">
              <span>📦</span> Manage Products
            </a>
            <a href="/admin-orders.html">
              <span>📑</span> Manage Orders
            </a>
            <div class="dropdown-divider"></div>
          ` : ''}
          <a href="/profile.html">
            <span>👤</span> My Profile
          </a>
          <a href="/orders.html">
            <span>📦</span> My Orders
          </a>
          <div class="dropdown-divider"></div>
          <button onclick="Auth.logout()">
            <span>🚪</span> Logout
          </button>
        </div>
      </div>
    `;
  } else {
    userActionsEl.innerHTML = `
      <a href="/login.html" class="nav-link-item">
        <span>👤</span> Login
      </a>
      <a href="/register.html" class="btn btn-primary" style="padding: 0.45rem 0.9rem; font-size: 0.85rem;">
        Sign Up
      </a>
    `;
  }
}

function toggleUserDropdown(event) {
  event.stopPropagation();
  const dropdown = document.getElementById('userDropdown');
  if (dropdown) {
    dropdown.classList.toggle('show');
  }
}

document.addEventListener('click', (e) => {
  const dropdown = document.getElementById('userDropdown');
  if (dropdown && !dropdown.contains(e.target) && !e.target.closest('#userMenuBtn')) {
    dropdown.classList.remove('show');
  }
});

async function updateCartBadge() {
  const badge = document.getElementById('navCartBadge');
  if (!badge) return;

  if (!Auth.isLoggedIn()) {
    badge.textContent = '0';
    return;
  }

  try {
    const res = await apiRequest('/api/cart');
    if (res.success && res.data) {
      badge.textContent = res.data.totalQuantity || 0;
    }
  } catch (err) {
    badge.textContent = '0';
  }
}

function initSearch() {
  const form = document.getElementById('navSearchForm');
  if (!form) return;

  form.addEventListener('submit', (e) => {
    e.preventDefault();
    const input = document.getElementById('navSearchInput');
    const query = input ? input.value.trim() : '';
    if (query) {
      window.location.href = `/products.html?keyword=${encodeURIComponent(query)}`;
    }
  });
}

async function quickAddToCart(productId, event) {
  if (event) {
    event.stopPropagation();
  }

  if (!Auth.isLoggedIn()) {
    showToast('Please login to add products to your cart', 'warning');
    setTimeout(() => {
      window.location.href = `/login.html?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`;
    }, 1200);
    return;
  }

  try {
    const res = await apiRequest('/api/cart/items', 'POST', {
      productId: Number(productId),
      quantity: 1
    });

    if (res.success) {
      showToast('Product added to cart successfully!', 'success');
      updateCartBadge();
    }
  } catch (err) {
    showToast(err.message || 'Failed to add item to cart', 'error');
  }
}

function escapeHtml(text) {
  if (!text) return '';
  return text.toString()
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
