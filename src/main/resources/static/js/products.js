/**
 * JD Mart - Products Catalog Controller
 */

let currentFilters = {
  keyword: '',
  categoryId: null,
  minPrice: null,
  maxPrice: null,
  minRating: null,
  sort: 'default',
  page: 0,
  size: 20
};

document.addEventListener('DOMContentLoaded', () => {
  parseUrlParams();
  loadCategories();
  loadProducts();
  setupFilterListeners();
});

function parseUrlParams() {
  const params = new URLSearchParams(window.location.search);
  if (params.get('keyword')) currentFilters.keyword = params.get('keyword');
  if (params.get('categoryId')) currentFilters.categoryId = Number(params.get('categoryId'));
  if (params.get('minPrice')) currentFilters.minPrice = Number(params.get('minPrice'));
  if (params.get('maxPrice')) currentFilters.maxPrice = Number(params.get('maxPrice'));
  if (params.get('minRating')) currentFilters.minRating = Number(params.get('minRating'));
  if (params.get('sort')) currentFilters.sort = params.get('sort');

  const searchInput = document.getElementById('navSearchInput');
  if (searchInput && currentFilters.keyword) {
    searchInput.value = currentFilters.keyword;
  }

  const sortSelect = document.getElementById('catalogSortSelect');
  if (sortSelect && currentFilters.sort) {
    sortSelect.value = currentFilters.sort;
  }
}

async function loadCategories() {
  const container = document.getElementById('categoryFilterList');
  if (!container) return;

  try {
    const res = await apiRequest('/api/categories');
    if (res.success && res.data) {
      let html = `
        <label class="filter-option">
          <input type="radio" name="categoryFilter" value="" ${!currentFilters.categoryId ? 'checked' : ''} onchange="onCategoryFilterChange(null)">
          <span>All Categories</span>
        </label>
      `;

      res.data.forEach(cat => {
        const isChecked = currentFilters.categoryId === cat.id ? 'checked' : '';
        html += `
          <label class="filter-option">
            <input type="radio" name="categoryFilter" value="${cat.id}" ${isChecked} onchange="onCategoryFilterChange(${cat.id})">
            <span>${escapeHtml(cat.name)}</span>
          </label>
        `;
      });

      container.innerHTML = html;
    }
  } catch (err) {
    console.error('Failed to load categories', err);
  }
}

async function loadProducts() {
  const container = document.getElementById('productGridContainer');
  const countEl = document.getElementById('productCountDisplay');
  if (!container) return;

  container.innerHTML = `
    <div style="grid-column: 1 / -1; text-align: center; padding: 3rem;">
      <div style="font-size: 1.1rem; color: #64748b;">Loading products...</div>
    </div>
  `;

  try {
    let url = '/api/products?';
    const queryParts = [];

    if (currentFilters.keyword) queryParts.push(`keyword=${encodeURIComponent(currentFilters.keyword)}`);
    if (currentFilters.categoryId) queryParts.push(`categoryId=${currentFilters.categoryId}`);
    if (currentFilters.minPrice !== null) queryParts.push(`minPrice=${currentFilters.minPrice}`);
    if (currentFilters.maxPrice !== null) queryParts.push(`maxPrice=${currentFilters.maxPrice}`);
    if (currentFilters.minRating !== null) queryParts.push(`minRating=${currentFilters.minRating}`);
    if (currentFilters.sort && currentFilters.sort !== 'default') queryParts.push(`sort=${currentFilters.sort}`);
    queryParts.push(`page=${currentFilters.page}&size=${currentFilters.size}`);

    url += queryParts.join('&');

    const res = await apiRequest(url);
    let products = [];

    if (res.success && res.data) {
      if (Array.isArray(res.data)) {
        products = res.data;
      } else if (res.data.content && Array.isArray(res.data.content)) {
        products = res.data.content;
      }
    }

    if (countEl) {
      countEl.textContent = `Showing ${products.length} products`;
    }

    if (products.length === 0) {
      container.innerHTML = `
        <div class="empty-state" style="grid-column: 1 / -1;">
          <h3>No products found</h3>
          <p>Try clearing some filters or searching with different keywords.</p>
          <button class="btn btn-primary" onclick="clearAllFilters()">Reset Filters</button>
        </div>
      `;
      return;
    }

    container.innerHTML = products.map(p => renderProductCard(p)).join('');
  } catch (err) {
    container.innerHTML = `
      <div class="empty-state" style="grid-column: 1 / -1;">
        <h3>Unable to load products</h3>
        <p>${escapeHtml(err.message)}</p>
      </div>
    `;
  }
}

function renderProductCard(p) {
  const fallbackImg = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&auto=format&fit=crop&q=60";
  const discountHtml = p.discountPercentage > 0 ? `<div class="product-card-badge">${p.discountPercentage}% OFF</div>` : '';
  const origPriceHtml = p.originalPrice && p.originalPrice > p.sellingPrice ? `<span class="original-price">${formatINR(p.originalPrice)}</span>` : '';

  return `
    <div class="product-card">
      ${discountHtml}
      <div class="product-img-wrap" onclick="window.location.href='/product-details.html?id=${p.id}'" style="cursor: pointer;">
        <img src="${p.imageUrl || fallbackImg}" alt="${escapeHtml(p.name)}" class="product-img" onerror="this.onerror=null; this.src='${fallbackImg}';">
      </div>
      <div class="product-card-body">
        <div class="product-category-tag">${escapeHtml(p.categoryName || 'General')}</div>
        <a href="/product-details.html?id=${p.id}" class="product-title" title="${escapeHtml(p.name)}">${escapeHtml(p.name)}</a>
        
        <div class="rating-badge">
          <span>★</span>
          <span>${Number(p.rating || 4.2).toFixed(1)}</span>
          <span class="rating-count">(${Number(p.reviewCount || 10).toLocaleString('en-IN')})</span>
        </div>

        <div class="price-row">
          <span class="selling-price">${formatINR(p.sellingPrice)}</span>
          ${origPriceHtml}
        </div>

        <div class="delivery-badge">Free Delivery</div>

        <div class="product-card-actions">
          <button class="btn-add-cart" onclick="quickAddToCart(${p.id}, event)">
            <span>🛒</span> Add
          </button>
          <a href="/product-details.html?id=${p.id}" class="btn-view-details">
            Details
          </a>
        </div>
      </div>
    </div>
  `;
}

function onCategoryFilterChange(catId) {
  currentFilters.categoryId = catId;
  currentFilters.page = 0;
  loadProducts();
}

function setupFilterListeners() {
  const sortSelect = document.getElementById('catalogSortSelect');
  if (sortSelect) {
    sortSelect.addEventListener('change', (e) => {
      currentFilters.sort = e.target.value;
      loadProducts();
    });
  }

  // Price Range Radio listener
  document.querySelectorAll('input[name="priceRange"]').forEach(radio => {
    radio.addEventListener('change', (e) => {
      const val = e.target.value;
      if (!val) {
        currentFilters.minPrice = null;
        currentFilters.maxPrice = null;
      } else {
        const [min, max] = val.split('-').map(Number);
        currentFilters.minPrice = min;
        currentFilters.maxPrice = max || null;
      }
      loadProducts();
    });
  });

  // Rating Filter listener
  document.querySelectorAll('input[name="ratingFilter"]').forEach(radio => {
    radio.addEventListener('change', (e) => {
      const val = e.target.value;
      currentFilters.minRating = val ? Number(val) : null;
      loadProducts();
    });
  });
}

function clearAllFilters() {
  currentFilters = {
    keyword: '',
    categoryId: null,
    minPrice: null,
    maxPrice: null,
    minRating: null,
    sort: 'default',
    page: 0,
    size: 20
  };

  document.querySelectorAll('input[type="radio"]').forEach(r => {
    if (r.value === '') r.checked = true;
    else r.checked = false;
  });

  const sortSelect = document.getElementById('catalogSortSelect');
  if (sortSelect) sortSelect.value = 'default';

  const searchInput = document.getElementById('navSearchInput');
  if (searchInput) searchInput.value = '';

  window.history.replaceState({}, document.title, '/products.html');
  loadProducts();
}
