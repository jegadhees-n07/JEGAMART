/**
 * JD Mart - Product Details Controller
 */

let currentProduct = null;
let selectedQuantity = 1;

document.addEventListener('DOMContentLoaded', () => {
  const params = new URLSearchParams(window.location.search);
  const productId = params.get('id');

  if (!productId) {
    window.location.href = '/products.html';
    return;
  }

  loadProductDetails(productId);
});

async function loadProductDetails(id) {
  const container = document.getElementById('productDetailsContent');
  if (!container) return;

  try {
    const res = await apiRequest(`/api/products/${id}`);
    if (!res.success || !res.data) {
      throw new Error('Product not found');
    }

    currentProduct = res.data;
    renderDetails(currentProduct);
    loadRelatedProducts(currentProduct.categoryId, currentProduct.id);
  } catch (err) {
    container.innerHTML = `
      <div class="empty-state">
        <h3>Product Not Found</h3>
        <p>${escapeHtml(err.message)}</p>
        <a href="/products.html" class="btn btn-primary">Browse All Products</a>
      </div>
    `;
  }
}

function renderDetails(p) {
  const container = document.getElementById('productDetailsContent');
  const fallbackImg = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&auto=format&fit=crop&q=60";
  const inStock = p.stockQuantity > 0;
  const savings = p.originalPrice > p.sellingPrice ? (p.originalPrice - p.sellingPrice) : 0;

  // Format specifications table
  let specsHtml = '';
  if (p.specifications) {
    const specsList = p.specifications.split('|');
    specsHtml = `
      <h3 style="margin-top: 2rem; font-size: 1.15rem; font-weight: 800;">Product Specifications</h3>
      <table class="specs-table">
        <tbody>
          ${specsList.map(item => {
            const parts = item.split(':');
            const key = parts[0] ? parts[0].trim() : 'Detail';
            const val = parts[1] ? parts.slice(1).join(':').trim() : '';
            return `
              <tr>
                <th>${escapeHtml(key)}</th>
                <td>${escapeHtml(val || key)}</td>
              </tr>
            `;
          }).join('')}
        </tbody>
      </table>
    `;
  }

  container.innerHTML = `
    <div class="product-details-container">
      <div class="product-gallery">
        <div class="main-image-wrap">
          <img src="${p.imageUrl || fallbackImg}" alt="${escapeHtml(p.name)}" onerror="this.onerror=null; this.src='${fallbackImg}';">
        </div>
        <div class="action-buttons-row">
          <button class="btn btn-secondary" onclick="addToCartFromDetails()" ${!inStock ? 'disabled' : ''}>
            <span>🛒</span> ADD TO CART
          </button>
          <button class="btn btn-primary" onclick="buyNowFromDetails()" ${!inStock ? 'disabled' : ''}>
            <span>⚡</span> BUY NOW
          </button>
        </div>
      </div>

      <div class="product-info-col">
        <div class="product-category-tag">${escapeHtml(p.categoryName || 'General')}</div>
        <h1>${escapeHtml(p.name)}</h1>

        <div class="product-meta-row">
          <div class="rating-badge" style="font-size: 0.9rem; padding: 0.25rem 0.6rem;">
            <span>★</span>
            <span>${Number(p.rating || 4.2).toFixed(1)}</span>
          </div>
          <span style="font-size: 0.9rem; color: #64748b; font-weight: 600;">
            ${Number(p.reviewCount || 10).toLocaleString('en-IN')} Ratings & Reviews
          </span>
        </div>

        <div class="detail-price-box">
          <div style="display: flex; align-items: baseline;">
            <span class="detail-selling-price">${formatINR(p.sellingPrice)}</span>
            ${p.originalPrice > p.sellingPrice ? `
              <span class="detail-original-price">${formatINR(p.originalPrice)}</span>
              <span class="detail-discount">${p.discountPercentage}% off</span>
            ` : ''}
          </div>
          ${savings > 0 ? `
            <div style="font-size: 0.85rem; color: #10b981; font-weight: 700; margin-top: 0.35rem;">
              You save ${formatINR(savings)} on this purchase!
            </div>
          ` : ''}
          <div style="font-size: 0.8rem; color: #64748b; margin-top: 0.25rem;">
            Inclusive of all taxes
          </div>
        </div>

        <div>
          <span class="stock-status-badge ${inStock ? 'in-stock' : 'out-of-stock'}">
            ${inStock ? `✓ In Stock (${p.stockQuantity} available)` : '✕ Out of Stock'}
          </span>
        </div>

        ${inStock ? `
          <div class="qty-selector">
            <span style="font-weight: 700; font-size: 0.95rem;">Quantity:</span>
            <button class="qty-btn" onclick="changeQuantity(-1)">-</button>
            <span class="qty-val" id="detailQtyDisplay">1</span>
            <button class="qty-btn" onclick="changeQuantity(1)">+</button>
          </div>
        ` : ''}

        <div style="margin: 1.5rem 0; border-top: 1px solid #e2e8f0; border-bottom: 1px solid #e2e8f0; padding: 1.25rem 0;">
          <h3 style="font-size: 1.05rem; font-weight: 800; margin-bottom: 0.6rem;">Description</h3>
          <p style="color: #475569; font-size: 0.95rem; line-height: 1.6;">
            ${escapeHtml(p.description || p.shortDescription || 'No description available for this product.')}
          </p>
        </div>

        <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 1rem; margin: 1.5rem 0; text-align: center;">
          <div style="background: #f8fafc; padding: 0.9rem; border-radius: 8px; border: 1px solid #e2e8f0;">
            <div style="font-size: 1.4rem;">🚚</div>
            <div style="font-weight: 700; font-size: 0.85rem; margin-top: 0.3rem;">Free Delivery</div>
            <div style="font-size: 0.75rem; color: #64748b;">On orders above ₹499</div>
          </div>
          <div style="background: #f8fafc; padding: 0.9rem; border-radius: 8px; border: 1px solid #e2e8f0;">
            <div style="font-size: 1.4rem;">🔄</div>
            <div style="font-weight: 700; font-size: 0.85rem; margin-top: 0.3rem;">7 Days Return</div>
            <div style="font-size: 0.75rem; color: #64748b;">Hassle-free replacement</div>
          </div>
          <div style="background: #f8fafc; padding: 0.9rem; border-radius: 8px; border: 1px solid #e2e8f0;">
            <div style="font-size: 1.4rem;">🛡️</div>
            <div style="font-weight: 700; font-size: 0.85rem; margin-top: 0.3rem;">100% Genuine</div>
            <div style="font-size: 0.75rem; color: #64748b;">Brand warranty assured</div>
          </div>
        </div>

        ${specsHtml}
      </div>
    </div>
  `;
}

function changeQuantity(delta) {
  if (!currentProduct) return;
  const newQty = selectedQuantity + delta;
  if (newQty >= 1 && newQty <= Math.min(currentProduct.stockQuantity, 10)) {
    selectedQuantity = newQty;
    const el = document.getElementById('detailQtyDisplay');
    if (el) el.textContent = selectedQuantity;
  }
}

async function addToCartFromDetails() {
  if (!Auth.isLoggedIn()) {
    showToast('Please login to add products to your cart', 'warning');
    setTimeout(() => {
      window.location.href = `/login.html?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`;
    }, 1200);
    return;
  }

  try {
    const res = await apiRequest('/api/cart/items', 'POST', {
      productId: currentProduct.id,
      quantity: selectedQuantity
    });

    if (res.success) {
      showToast(`${selectedQuantity} item(s) added to cart!`, 'success');
      updateCartBadge();
    }
  } catch (err) {
    showToast(err.message || 'Failed to add item to cart', 'error');
  }
}

async function buyNowFromDetails() {
  if (!Auth.isLoggedIn()) {
    showToast('Please login to proceed to checkout', 'warning');
    setTimeout(() => {
      window.location.href = `/login.html?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`;
    }, 1200);
    return;
  }

  try {
    const res = await apiRequest('/api/cart/items', 'POST', {
      productId: currentProduct.id,
      quantity: selectedQuantity
    });

    if (res.success) {
      window.location.href = '/checkout.html';
    }
  } catch (err) {
    showToast(err.message || 'Failed to proceed', 'error');
  }
}

async function loadRelatedProducts(categoryId, excludeId) {
  const container = document.getElementById('relatedProductsGrid');
  if (!container || !categoryId) return;

  try {
    const res = await apiRequest(`/api/products/category/${categoryId}`);
    if (res.success && res.data) {
      const related = res.data.filter(p => p.id !== excludeId).slice(0, 4);
      if (related.length > 0) {
        document.getElementById('relatedSection').style.display = 'block';
        container.innerHTML = related.map(p => renderProductCard(p)).join('');
      }
    }
  } catch (err) {
    console.error('Failed to load related products', err);
  }
}
