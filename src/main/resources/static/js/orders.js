/**
 * JD Mart - My Orders Controller
 */

document.addEventListener('DOMContentLoaded', () => {
  if (!Auth.isLoggedIn()) {
    window.location.href = '/login.html?redirect=/orders.html';
    return;
  }

  loadOrders();
});

async function loadOrders() {
  const container = document.getElementById('ordersListContainer');
  if (!container) return;

  try {
    const res = await apiRequest('/api/orders');
    if (!res.success || !res.data) {
      throw new Error('Unable to fetch orders');
    }

    const orders = res.data;
    renderOrders(orders);
  } catch (err) {
    container.innerHTML = `
      <div class="empty-state">
        <h3>Error loading orders</h3>
        <p>${escapeHtml(err.message)}</p>
      </div>
    `;
  }
}

function renderOrders(orders) {
  const container = document.getElementById('ordersListContainer');
  if (!orders || orders.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <div style="font-size: 3.5rem; margin-bottom: 1rem;">📦</div>
        <h3>No Orders Found</h3>
        <p>You haven't placed any orders yet. Discover our best deals and shop now!</p>
        <a href="/products.html" class="btn btn-primary" style="padding: 0.75rem 2rem;">Start Shopping</a>
      </div>
    `;
    return;
  }

  const fallbackImg = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&auto=format&fit=crop&q=60";

  container.innerHTML = orders.map(order => {
    const orderDate = new Date(order.createdAt).toLocaleDateString('en-IN', {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });

    const itemsHtml = order.items.map(item => `
      <div style="display: flex; gap: 1rem; padding: 0.85rem 0; border-bottom: 1px solid #f1f5f9; align-items: center;">
        <img src="${item.productImage || fallbackImg}" alt="${escapeHtml(item.productName)}" style="width:60px; height:60px; object-fit:contain; border:1px solid #e2e8f0; border-radius:6px; padding:0.25rem;" onerror="this.onerror=null; this.src='${fallbackImg}';">
        <div style="flex:1;">
          <a href="/product-details.html?id=${item.productId || ''}" style="font-weight:600; font-size:0.95rem; color:#0f172a; text-decoration:none;">
            ${escapeHtml(item.productName)}
          </a>
          <div style="font-size:0.8rem; color:#64748b; margin-top:0.2rem;">
            Qty: ${item.quantity} | Price: ${formatINR(item.price)} each
          </div>
        </div>
        <div style="font-weight:800; font-size:1rem; color:#0f172a;">
          ${formatINR(item.subtotal)}
        </div>
      </div>
    `).join('');

    return `
      <div class="order-card">
        <div class="order-header">
          <div>
            <span class="order-id">Order #${escapeHtml(order.orderNumber)}</span>
            <span class="order-date" style="margin-left:0.75rem;">Placed on ${orderDate}</span>
          </div>
          <div>
            <span class="status-badge status-${order.status}">${order.status.replace(/_/g, ' ')}</span>
          </div>
        </div>

        <div>
          ${itemsHtml}
        </div>

        <div style="display:flex; justify-content:space-between; align-items:flex-end; margin-top:1rem; padding-top:0.75rem; border-top:1px dashed #e2e8f0; flex-wrap:wrap; gap:1rem;">
          <div>
            <div style="font-size:0.8rem; color:#64748b;">Shipping To:</div>
            <div style="font-size:0.85rem; font-weight:600; color:#334155;">${escapeHtml(order.shippingFullName)} (${escapeHtml(order.shippingMobile)})</div>
            <div style="font-size:0.8rem; color:#64748b;">${escapeHtml(order.shippingAddress)}</div>
            <div style="font-size:0.8rem; color:#64748b; margin-top:0.2rem;">Payment: <strong>${escapeHtml(order.paymentMethod)}</strong> (${escapeHtml(order.paymentStatus)})</div>
          </div>
          <div style="text-align:right;">
            <div style="font-size:0.8rem; color:#64748b;">Total Paid:</div>
            <div style="font-size:1.35rem; font-weight:800; color:#0f172a;">${formatINR(order.totalAmount)}</div>
          </div>
        </div>
      </div>
    `;
  }).join('');
}
