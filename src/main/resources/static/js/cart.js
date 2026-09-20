/**
 * JD Mart - Shopping Cart Controller
 */

document.addEventListener('DOMContentLoaded', () => {
  if (!Auth.isLoggedIn()) {
    renderLoginRequired();
    return;
  }
  loadCart();
});

function renderLoginRequired() {
  const container = document.getElementById('cartMainContainer');
  if (!container) return;

  container.innerHTML = `
    <div class="empty-state">
      <div style="font-size: 3rem; margin-bottom: 1rem;">🛒</div>
      <h3>Missing Cart items?</h3>
      <p>Login to see the items you previously added to your JD Mart cart.</p>
      <a href="/login.html?redirect=/cart.html" class="btn btn-primary" style="padding: 0.75rem 2rem;">Login to Your Account</a>
    </div>
  `;
}

async function loadCart() {
  const container = document.getElementById('cartMainContainer');
  if (!container) return;

  try {
    const res = await apiRequest('/api/cart');
    if (!res.success || !res.data) {
      throw new Error('Unable to retrieve cart');
    }

    const cart = res.data;
    renderCart(cart);
  } catch (err) {
    container.innerHTML = `
      <div class="empty-state">
        <h3>Error loading cart</h3>
        <p>${escapeHtml(err.message)}</p>
        <button class="btn btn-primary" onclick="loadCart()">Retry</button>
      </div>
    `;
  }
}

function renderCart(cart) {
  const container = document.getElementById('cartMainContainer');
  if (!cart.items || cart.items.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <div style="font-size: 3.5rem; margin-bottom: 1rem;">🛍️</div>
        <h3>Your Shopping Cart is Empty!</h3>
        <p>Explore thousands of great products with exclusive discounts on JD Mart.</p>
        <a href="/products.html" class="btn btn-primary" style="padding: 0.75rem 2rem;">Shop Now</a>
      </div>
    `;
    updateCartBadge();
    return;
  }

  const fallbackImg = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&auto=format&fit=crop&q=60";

  const itemsHtml = cart.items.map(item => `
    <div class="cart-item">
      <img src="${item.productImage || fallbackImg}" alt="${escapeHtml(item.productName)}" class="cart-item-img" onerror="this.onerror=null; this.src='${fallbackImg}';">
      
      <div class="cart-item-info">
        <a href="/product-details.html?id=${item.productId}" style="text-decoration:none;">
          <h4>${escapeHtml(item.productName)}</h4>
        </a>
        <div style="display:flex; align-items:baseline; gap:0.5rem; margin-bottom: 0.4rem;">
          <span class="cart-item-price">${formatINR(item.price)}</span>
          ${item.originalPrice > item.price ? `
            <span style="font-size:0.85rem; color:#94a3b8; text-decoration:line-through;">${formatINR(item.originalPrice)}</span>
            <span style="font-size:0.8rem; color:#10b981; font-weight:700;">${item.discountPercentage}% off</span>
          ` : ''}
        </div>
        <div style="font-size:0.8rem; color:#64748b;">Subtotal: <strong style="color:#0f172a;">${formatINR(item.subtotal)}</strong></div>

        <div class="cart-item-actions">
          <div style="display:flex; align-items:center; gap:0.5rem;">
            <button class="qty-btn" style="width:28px; height:28px; font-size:0.9rem;" onclick="updateItemQuantity(${item.id}, ${item.quantity - 1})">-</button>
            <span style="font-weight:700; font-size:0.95rem; min-width:20px; text-align:center;">${item.quantity}</span>
            <button class="qty-btn" style="width:28px; height:28px; font-size:0.9rem;" onclick="updateItemQuantity(${item.id}, ${item.quantity + 1})">+</button>
          </div>
          <button class="btn-remove-item" onclick="removeItem(${item.id})">Remove</button>
        </div>
      </div>
    </div>
  `).join('');

  container.innerHTML = `
    <div class="cart-layout">
      <div>
        <div class="cart-card">
          <div style="display:flex; justify-content:space-between; align-items:center; padding-bottom:1rem; border-bottom:1px solid #e2e8f0; margin-bottom:1rem;">
            <h2 style="font-size:1.25rem; font-weight:800;">My Cart (${cart.totalQuantity} items)</h2>
            <button onclick="clearCart()" style="background:none; border:none; color:#ef4444; font-size:0.85rem; font-weight:600; cursor:pointer;">
              Clear All
            </button>
          </div>
          ${itemsHtml}
          <div style="margin-top: 1.5rem; display: flex; justify-content: space-between; align-items: center;">
            <a href="/products.html" class="btn btn-outline">
              ← Continue Shopping
            </a>
          </div>
        </div>
      </div>

      <div>
        <div class="price-summary-card">
          <h3>PRICE DETAILS</h3>
          
          <div class="price-row-item">
            <span>Price (${cart.totalQuantity} items)</span>
            <span>${formatINR(cart.originalTotal)}</span>
          </div>

          <div class="price-row-item" style="color: #10b981;">
            <span>Discount</span>
            <span>- ${formatINR(cart.discount)}</span>
          </div>

          <div class="price-row-item">
            <span>Delivery Charges</span>
            <span>${cart.deliveryCharge === 0 ? '<strong style="color:#10b981;">FREE</strong>' : formatINR(cart.deliveryCharge)}</span>
          </div>

          ${cart.subtotal < 500 ? `
            <div style="font-size:0.75rem; color:#f59e0b; margin-top:-0.3rem; margin-bottom:0.75rem;">
              Add ${formatINR(500 - cart.subtotal)} more to get FREE Delivery!
            </div>
          ` : ''}

          <div class="price-total-row">
            <span>Total Amount</span>
            <span>${formatINR(cart.totalAmount)}</span>
          </div>

          ${cart.discount > 0 ? `
            <div class="savings-banner">
              You will save ${formatINR(cart.discount)} on this order
            </div>
          ` : ''}

          <a href="/checkout.html" class="btn btn-secondary btn-block" style="padding:0.9rem; font-size:1rem; font-weight:800; margin-top:1.25rem;">
            PROCEED TO CHECKOUT
          </a>

          <div style="display:flex; align-items:center; gap:0.5rem; justify-content:center; margin-top:1.25rem; font-size:0.8rem; color:#64748b;">
            <span>🔒</span> Safe and Secure Payments
          </div>
        </div>
      </div>
    </div>
  `;

  updateCartBadge();
}

async function updateItemQuantity(itemId, newQty) {
  try {
    const res = await apiRequest(`/api/cart/items/${itemId}`, 'PUT', { quantity: newQty });
    if (res.success && res.data) {
      renderCart(res.data);
      showToast('Cart updated', 'success');
    }
  } catch (err) {
    showToast(err.message || 'Failed to update quantity', 'error');
  }
}

async function removeItem(itemId) {
  try {
    const res = await apiRequest(`/api/cart/items/${itemId}`, 'DELETE');
    if (res.success && res.data) {
      renderCart(res.data);
      showToast('Item removed from cart', 'info');
    }
  } catch (err) {
    showToast(err.message || 'Failed to remove item', 'error');
  }
}

async function clearCart() {
  if (!confirm('Are you sure you want to empty your cart?')) return;
  try {
    const res = await apiRequest('/api/cart/clear', 'DELETE');
    if (res.success) {
      loadCart();
      showToast('Cart cleared', 'info');
    }
  } catch (err) {
    showToast(err.message || 'Failed to clear cart', 'error');
  }
}
