/**
 * JD Mart - Admin Controller (Dashboard, Products, Orders)
 */

document.addEventListener('DOMContentLoaded', () => {
  if (!Auth.isLoggedIn() || !Auth.isAdmin()) {
    showToast('Access denied: Admin privileges required', 'error');
    setTimeout(() => { window.location.href = '/login.html'; }, 1000);
    return;
  }

  const pageType = document.body.dataset.adminPage;
  if (pageType === 'dashboard') {
    loadDashboardStats();
    loadRecentOrders();
  } else if (pageType === 'products') {
    loadAdminProducts();
    loadCategoriesForProductModal();
    setupProductForm();
  } else if (pageType === 'orders') {
    loadAdminOrders();
  }
});

/* ================= Dashboard ================= */
async function loadDashboardStats() {
  try {
    const res = await apiRequest('/api/admin/dashboard');
    if (res.success && res.data) {
      const stats = res.data;
      if (document.getElementById('statTotalUsers')) document.getElementById('statTotalUsers').textContent = stats.totalUsers;
      if (document.getElementById('statTotalProducts')) document.getElementById('statTotalProducts').textContent = stats.totalProducts;
      if (document.getElementById('statTotalOrders')) document.getElementById('statTotalOrders').textContent = stats.totalOrders;
      if (document.getElementById('statTotalSales')) document.getElementById('statTotalSales').textContent = formatINR(stats.totalSales);
    }
  } catch (err) {
    showToast('Failed to load dashboard metrics', 'error');
  }
}

async function loadRecentOrders() {
  const container = document.getElementById('recentOrdersTableBody');
  if (!container) return;

  try {
    const res = await apiRequest('/api/admin/orders?page=0&size=5');
    if (res.success && res.data && res.data.content) {
      container.innerHTML = res.data.content.map(order => `
        <tr>
          <td><strong>#${escapeHtml(order.orderNumber)}</strong></td>
          <td>${escapeHtml(order.shippingFullName)}</td>
          <td>${formatINR(order.totalAmount)}</td>
          <td><span class="status-badge status-${order.status}">${order.status.replace(/_/g, ' ')}</span></td>
          <td>${new Date(order.createdAt).toLocaleDateString('en-IN')}</td>
        </tr>
      `).join('');
    }
  } catch (err) {
    container.innerHTML = '<tr><td colspan="5">Failed to load orders</td></tr>';
  }
}

/* ================= Products ================= */
let adminProductsList = [];

async function loadAdminProducts() {
  const container = document.getElementById('adminProductsTableBody');
  if (!container) return;

  container.innerHTML = '<tr><td colspan="7" style="text-align:center;">Loading products...</td></tr>';

  try {
    const res = await apiRequest('/api/admin/products');
    if (res.success && res.data) {
      adminProductsList = res.data;
      renderAdminProductsTable(adminProductsList);
    }
  } catch (err) {
    container.innerHTML = `<tr><td colspan="7" style="text-align:center; color:red;">${escapeHtml(err.message)}</td></tr>`;
  }
}

function renderAdminProductsTable(products) {
  const container = document.getElementById('adminProductsTableBody');
  if (!container) return;

  if (products.length === 0) {
    container.innerHTML = '<tr><td colspan="7" style="text-align:center;">No products found</td></tr>';
    return;
  }

  const fallbackImg = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&auto=format&fit=crop&q=60";

  container.innerHTML = products.map(p => `
    <tr>
      <td>
        <img src="${p.imageUrl || fallbackImg}" style="width:48px; height:48px; object-fit:contain; border:1px solid #e2e8f0; border-radius:4px;" onerror="this.onerror=null; this.src='${fallbackImg}';">
      </td>
      <td>
        <div style="font-weight:700; max-width:260px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" title="${escapeHtml(p.name)}">
          ${escapeHtml(p.name)}
        </div>
        <div style="font-size:0.75rem; color:#64748b;">ID: ${p.id}</div>
      </td>
      <td>${escapeHtml(p.categoryName || 'General')}</td>
      <td>
        <div><strong>${formatINR(p.sellingPrice)}</strong></div>
        <div style="font-size:0.75rem; color:#94a3b8; text-decoration:line-through;">${formatINR(p.originalPrice)}</div>
      </td>
      <td>
        <div style="display:flex; align-items:center; gap:0.4rem;">
          <input type="number" min="0" value="${p.stockQuantity}" style="width:60px; padding:0.25rem 0.4rem; border:1px solid #cbd5e1; border-radius:4px;" onchange="updateStockQuick(${p.id}, this.value)">
        </div>
      </td>
      <td>
        <span class="stock-status-badge ${p.stockQuantity > 0 ? 'in-stock' : 'out-of-stock'}">
          ${p.stockQuantity > 0 ? 'Active' : 'Out'}
        </span>
      </td>
      <td>
        <div style="display:flex; gap:0.5rem;">
          <button class="btn btn-outline" style="padding:0.3rem 0.6rem; font-size:0.8rem;" onclick="openEditProductModal(${p.id})">Edit</button>
          <button class="btn btn-danger" style="padding:0.3rem 0.6rem; font-size:0.8rem;" onclick="deleteProduct(${p.id})">Delete</button>
        </div>
      </td>
    </tr>
  `).join('');
}

async function updateStockQuick(productId, newStock) {
  try {
    const res = await apiRequest(`/api/admin/products/${productId}/stock`, 'PATCH', { stockQuantity: Number(newStock) });
    if (res.success) {
      showToast('Stock quantity updated', 'success');
    }
  } catch (err) {
    showToast(err.message || 'Failed to update stock', 'error');
  }
}

async function deleteProduct(productId) {
  if (!confirm('Are you sure you want to deactivate/delete this product?')) return;
  try {
    const res = await apiRequest(`/api/admin/products/${productId}`, 'DELETE');
    if (res.success) {
      showToast('Product removed', 'info');
      loadAdminProducts();
    }
  } catch (err) {
    showToast(err.message || 'Failed to delete product', 'error');
  }
}

async function loadCategoriesForProductModal() {
  const select = document.getElementById('prodCategorySelect');
  if (!select) return;

  try {
    const res = await apiRequest('/api/categories');
    if (res.success && res.data) {
      select.innerHTML = res.data.map(c => `
        <option value="${c.id}">${escapeHtml(c.name)}</option>
      `).join('');
    }
  } catch (err) {
    console.error(err);
  }
}

function openAddProductModal() {
  document.getElementById('productModalTitle').textContent = 'Add New Product';
  document.getElementById('adminProductForm').reset();
  document.getElementById('editProdId').value = '';
  document.getElementById('productModal').classList.add('show');
}

function openEditProductModal(id) {
  const p = adminProductsList.find(item => item.id === id);
  if (!p) return;

  document.getElementById('productModalTitle').textContent = 'Edit Product';
  document.getElementById('editProdId').value = p.id;
  document.getElementById('prodName').value = p.name;
  document.getElementById('prodCategorySelect').value = p.categoryId;
  document.getElementById('prodOriginalPrice').value = p.originalPrice;
  document.getElementById('prodDiscountPct').value = p.discountPercentage || 0;
  document.getElementById('prodStock').value = p.stockQuantity;
  document.getElementById('prodImageUrl').value = p.imageUrl || '';
  document.getElementById('prodShortDesc').value = p.shortDescription || '';
  document.getElementById('prodDesc').value = p.description || '';
  document.getElementById('prodSpecs').value = p.specifications || '';
  document.getElementById('prodFeatured').checked = !!p.featured;
  document.getElementById('prodTrending').checked = !!p.trending;

  document.getElementById('productModal').classList.add('show');
}

function closeProductModal() {
  document.getElementById('productModal').classList.remove('show');
}

function setupProductForm() {
  const form = document.getElementById('adminProductForm');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('editProdId').value;
    const name = document.getElementById('prodName').value.trim();
    const categoryId = Number(document.getElementById('prodCategorySelect').value);
    const originalPrice = Number(document.getElementById('prodOriginalPrice').value);
    const discountPercentage = Number(document.getElementById('prodDiscountPct').value);
    const stockQuantity = Number(document.getElementById('prodStock').value);
    const imageUrl = document.getElementById('prodImageUrl').value.trim();
    const shortDescription = document.getElementById('prodShortDesc').value.trim();
    const description = document.getElementById('prodDesc').value.trim();
    const specifications = document.getElementById('prodSpecs').value.trim();
    const featured = document.getElementById('prodFeatured').checked;
    const trending = document.getElementById('prodTrending').checked;

    const payload = {
      name,
      categoryId,
      originalPrice,
      discountPercentage,
      stockQuantity,
      imageUrl,
      shortDescription,
      description,
      specifications,
      featured,
      trending,
      active: true
    };

    try {
      if (id) {
        await apiRequest(`/api/admin/products/${id}`, 'PUT', payload);
        showToast('Product updated successfully', 'success');
      } else {
        await apiRequest('/api/admin/products', 'POST', payload);
        showToast('Product created successfully', 'success');
      }
      closeProductModal();
      loadAdminProducts();
    } catch (err) {
      showToast(err.message || 'Error saving product', 'error');
    }
  });
}

/* ================= Orders ================= */
async function loadAdminOrders() {
  const container = document.getElementById('adminOrdersTableBody');
  if (!container) return;

  container.innerHTML = '<tr><td colspan="7" style="text-align:center;">Loading orders...</td></tr>';

  try {
    const res = await apiRequest('/api/admin/orders?page=0&size=50');
    if (res.success && res.data && res.data.content) {
      renderAdminOrdersTable(res.data.content);
    }
  } catch (err) {
    container.innerHTML = `<tr><td colspan="7" style="text-align:center; color:red;">${escapeHtml(err.message)}</td></tr>`;
  }
}

function renderAdminOrdersTable(orders) {
  const container = document.getElementById('adminOrdersTableBody');
  if (!container) return;

  if (orders.length === 0) {
    container.innerHTML = '<tr><td colspan="7" style="text-align:center;">No orders recorded yet</td></tr>';
    return;
  }

  const statuses = ['ORDERED', 'CONFIRMED', 'SHIPPED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED'];

  container.innerHTML = orders.map(order => {
    const optionsHtml = statuses.map(st => `
      <option value="${st}" ${order.status === st ? 'selected' : ''}>${st.replace(/_/g, ' ')}</option>
    `).join('');

    return `
      <tr>
        <td><strong>#${escapeHtml(order.orderNumber)}</strong></td>
        <td>
          <div style="font-weight:600;">${escapeHtml(order.shippingFullName)}</div>
          <div style="font-size:0.75rem; color:#64748b;">${escapeHtml(order.shippingMobile)}</div>
          <div style="font-size:0.75rem; color:#64748b;">${escapeHtml(order.userEmail || '')}</div>
        </td>
        <td>
          <div style="font-size:0.8rem; max-width:200px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">
            ${order.items.map(i => `${escapeHtml(i.productName)} (x${i.quantity})`).join(', ')}
          </div>
        </td>
        <td><strong>${formatINR(order.totalAmount)}</strong></td>
        <td>${escapeHtml(order.paymentMethod)}</td>
        <td>
          <select style="padding:0.35rem 0.6rem; border:1px solid #cbd5e1; border-radius:6px; font-size:0.85rem; font-weight:600;" onchange="updateOrderStatusAdmin(${order.id}, this.value)">
            ${optionsHtml}
          </select>
        </td>
        <td>${new Date(order.createdAt).toLocaleDateString('en-IN')}</td>
      </tr>
    `;
  }).join('');
}

async function updateOrderStatusAdmin(orderId, newStatus) {
  try {
    const res = await apiRequest(`/api/admin/orders/${orderId}/status`, 'PUT', { status: newStatus });
    if (res.success) {
      showToast(`Order status updated to ${newStatus.replace(/_/g, ' ')}`, 'success');
    }
  } catch (err) {
    showToast(err.message || 'Failed to update order status', 'error');
  }
}
