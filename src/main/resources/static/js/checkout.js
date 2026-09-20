/**
 * JD Mart - Checkout Controller
 */

let checkoutCart = null;
let selectedPaymentMethod = 'Cash on Delivery';

document.addEventListener('DOMContentLoaded', () => {
  if (!Auth.isLoggedIn()) {
    window.location.href = '/login.html?redirect=/checkout.html';
    return;
  }

  loadCheckoutData();
  setupPaymentMethodHandlers();
});

async function loadCheckoutData() {
  try {
    // 1. Fetch Cart
    const cartRes = await apiRequest('/api/cart');
    if (!cartRes.success || !cartRes.data || !cartRes.data.items || cartRes.data.items.length === 0) {
      showToast('Your cart is empty. Please add products first.', 'warning');
      window.location.href = '/products.html';
      return;
    }
    checkoutCart = cartRes.data;
    renderOrderSummary(checkoutCart);

    // 2. Fetch User Profile to autofill address
    const profileRes = await apiRequest('/api/profile');
    if (profileRes.success && profileRes.data) {
      populateAddressFields(profileRes.data);
    }
  } catch (err) {
    showToast(err.message || 'Error loading checkout data', 'error');
  }
}

function populateAddressFields(user) {
  if (user.fullName) document.getElementById('shippingFullName').value = user.fullName;
  if (user.mobile) document.getElementById('shippingMobile').value = user.mobile;

  if (user.addresses && user.addresses.length > 0) {
    const defaultAddr = user.addresses.find(a => a.default) || user.addresses[0];
    if (defaultAddr.houseBuilding) document.getElementById('shippingHouse').value = defaultAddr.houseBuilding;
    if (defaultAddr.street) document.getElementById('shippingStreet').value = defaultAddr.street;
    if (defaultAddr.city) document.getElementById('shippingCity').value = defaultAddr.city;
    if (defaultAddr.state) document.getElementById('shippingState').value = defaultAddr.state;
    if (defaultAddr.pincode) document.getElementById('shippingPincode').value = defaultAddr.pincode;
  }
}

function renderOrderSummary(cart) {
  const container = document.getElementById('checkoutSummaryContainer');
  if (!container) return;

  const itemsListHtml = cart.items.map(i => `
    <div style="display:flex; justify-content:space-between; margin-bottom:0.6rem; font-size:0.875rem;">
      <span style="color:#334155; max-width:220px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">
        ${escapeHtml(i.productName)} (x${i.quantity})
      </span>
      <strong>${formatINR(i.subtotal)}</strong>
    </div>
  `).join('');

  container.innerHTML = `
    <div class="price-summary-card" style="position:static;">
      <h3>ORDER SUMMARY</h3>
      
      <div style="margin-bottom:1rem; padding-bottom:1rem; border-bottom:1px solid #e2e8f0; max-height:200px; overflow-y:auto;">
        ${itemsListHtml}
      </div>

      <div class="price-row-item">
        <span>Items (${cart.totalQuantity})</span>
        <span>${formatINR(cart.originalTotal)}</span>
      </div>

      <div class="price-row-item" style="color:#10b981;">
        <span>Discount</span>
        <span>- ${formatINR(cart.discount)}</span>
      </div>

      <div class="price-row-item">
        <span>Delivery Fee</span>
        <span>${cart.deliveryCharge === 0 ? '<strong style="color:#10b981;">FREE</strong>' : formatINR(cart.deliveryCharge)}</span>
      </div>

      <div class="price-total-row">
        <span>Total Payable</span>
        <span>${formatINR(cart.totalAmount)}</span>
      </div>

      <button id="btnPlaceOrder" class="btn btn-secondary btn-block" style="padding:0.9rem; font-size:1.05rem; font-weight:800; margin-top:1.5rem;" onclick="placeOrder()">
        PLACE ORDER
      </button>

      <div style="margin-top:1rem; text-align:center; font-size:0.75rem; color:#64748b;">
        By placing this order, you agree to JD Mart's Terms of Use & Privacy Policy.
      </div>
    </div>
  `;
}

function setupPaymentMethodHandlers() {
  const options = document.querySelectorAll('input[name="paymentMethod"]');
  options.forEach(opt => {
    opt.addEventListener('change', (e) => {
      selectedPaymentMethod = e.target.value;
      updatePaymentFields(selectedPaymentMethod);
    });
  });
}

function updatePaymentFields(method) {
  const upiBox = document.getElementById('upiPaymentFields');
  const cardBox = document.getElementById('cardPaymentFields');

  if (upiBox) upiBox.style.display = method === 'UPI Demo' ? 'block' : 'none';
  if (cardBox) cardBox.style.display = method === 'Card Demo' ? 'block' : 'none';
}

async function placeOrder() {
  const fullName = document.getElementById('shippingFullName').value.trim();
  const mobile = document.getElementById('shippingMobile').value.trim();
  const houseBuilding = document.getElementById('shippingHouse').value.trim();
  const street = document.getElementById('shippingStreet').value.trim();
  const city = document.getElementById('shippingCity').value.trim();
  const state = document.getElementById('shippingState').value.trim();
  const pincode = document.getElementById('shippingPincode').value.trim();

  // Basic Validation
  if (!fullName || !mobile || !houseBuilding || !street || !city || !state || !pincode) {
    showToast('Please fill in all address fields', 'error');
    return;
  }

  if (!/^[0-9]{10}$/.test(mobile)) {
    showToast('Please enter a valid 10-digit mobile number', 'error');
    return;
  }

  if (!/^[0-9]{6}$/.test(pincode)) {
    showToast('Please enter a valid 6-digit pincode', 'error');
    return;
  }

  const btn = document.getElementById('btnPlaceOrder');
  if (btn) {
    btn.disabled = true;
    btn.textContent = 'Placing Order...';
  }

  try {
    const payload = {
      fullName,
      mobile,
      houseBuilding,
      street,
      city,
      state,
      pincode,
      paymentMethod: selectedPaymentMethod
    };

    const res = await apiRequest('/api/orders', 'POST', payload);

    if (res.success && res.data) {
      showToast('Order placed successfully! Redirecting...', 'success');
      setTimeout(() => {
        window.location.href = `/orders.html?newOrder=${encodeURIComponent(res.data.orderNumber)}`;
      }, 1200);
    }
  } catch (err) {
    showToast(err.message || 'Failed to place order', 'error');
    if (btn) {
      btn.disabled = false;
      btn.textContent = 'PLACE ORDER';
    }
  }
}
