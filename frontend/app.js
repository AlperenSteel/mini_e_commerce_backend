// ============================================================
//  MiniCommerce — Full App Logic
//  Store (customer) + Admin panel
// ============================================================

const BASE = 'http://localhost:8080';

// ─── State ───────────────────────────────────────────────────
const S = {
  mode: 'store',           // 'store' | 'admin'
  storePage: 'home',       // 'home' | 'shop' | 'detail' | 'orders'
  adminPage: 'dashboard',

  cart: [],
  detailProduct: null,
  detailQty: 1,

  allCategories: [],
  shopFilter: { categoryId: null, search: '', page: 0 },
  shopProducts: { items: [], totalPages: 0 },

  adminProducts: { items: [], page: 0, totalPages: 0 },
  adminOrders:   { items: [], page: 0, totalPages: 0 },
  adminUsers:    [],
};

// ─── Utilities ────────────────────────────────────────────────
const $  = (s, ctx = document) => ctx.querySelector(s);
const $$ = (s, ctx = document) => [...ctx.querySelectorAll(s)];
const esc = s => String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
const fmtPrice = p => `₺${(+p || 0).toFixed(2)}`;
const fmtDate = dt => { try { return new Intl.DateTimeFormat('tr-TR',{day:'2-digit',month:'short',year:'numeric',hour:'2-digit',minute:'2-digit'}).format(new Date(dt)); } catch { return dt || '-'; } };
const statusTr = s => ({PENDING:'Bekliyor',CONFIRMED:'Onaylandı',SHIPPED:'Kargoda',DELIVERED:'Teslim Edildi',CANCELLED:'İptal'})[s] || s;

function emoji(cat) {
  if (!cat) return '📦';
  const n = cat.toLowerCase();
  if (n.includes('elektronik')||n.includes('tech')||n.includes('bilgisayar')) return '💻';
  if (n.includes('telefon')||n.includes('mobile')||n.includes('phone')) return '📱';
  if (n.includes('giyim')||n.includes('kıyafet')||n.includes('moda')||n.includes('fashion')) return '👕';
  if (n.includes('ayakkabı')||n.includes('shoe')) return '👟';
  if (n.includes('kitap')||n.includes('book')) return '📚';
  if (n.includes('yiyecek')||n.includes('gıda')||n.includes('food')) return '🍕';
  if (n.includes('ev')||n.includes('home')||n.includes('mobilya')) return '🛋️';
  if (n.includes('oyun')||n.includes('game')) return '🎮';
  if (n.includes('müzik')||n.includes('music')) return '🎵';
  if (n.includes('spor')||n.includes('sport')) return '⚽';
  if (n.includes('güzellik')||n.includes('beauty')||n.includes('kozmetik')) return '💄';
  if (n.includes('araç')||n.includes('oto')) return '🚗';
  if (n.includes('sağlık')||n.includes('health')) return '💊';
  return '🛍️';
}

// ─── API ─────────────────────────────────────────────────────
async function api(method, path, body = null) {
  const opts = { method, headers: { 'Content-Type': 'application/json' } };
  if (body !== null) opts.body = JSON.stringify(body);
  const res = await fetch(`${BASE}${path}`, opts);
  if (!res.ok) {
    let msg = `HTTP ${res.status}`;
    try { msg = await res.text(); } catch {}
    throw new Error(msg);
  }
  if (res.status === 204 || res.headers.get('content-length') === '0') return null;
  const ct = res.headers.get('content-type') || '';
  if (ct.includes('json')) return res.json();
  return res.text();
}

const GET    = p       => api('GET',   p);
const POST   = (p, b)  => api('POST',  p, b);
const DELETE = p       => api('DELETE', p);
const PATCH  = (p, b)  => api('PATCH', p, b);

// ─── Toast ────────────────────────────────────────────────────
function toast(msg, type = 'info') {
  const icons = { success: '✅', error: '❌', info: 'ℹ️' };
  const el = document.createElement('div');
  el.className = `toast ${type}`;
  el.innerHTML = `<span class="toast-ico">${icons[type]}</span><span class="toast-txt">${esc(msg)}</span>`;
  $('#toast-stack').appendChild(el);
  setTimeout(() => { el.classList.add('out'); setTimeout(() => el.remove(), 250); }, 3200);
}

// ─── Mode Switch ─────────────────────────────────────────────
function goAdmin() {
  S.mode = 'admin';
  $('#store-wrapper').style.display = 'none';
  $('#admin-wrapper').style.display = 'block';
  adminNavigate('dashboard');
  checkHealth();
}

function goStore() {
  S.mode = 'store';
  $('#store-wrapper').style.display = 'block';
  $('#admin-wrapper').style.display = 'none';
  storeNavigate(S.storePage || 'home');
}

// ═══════════════════════════════════════════════════════════════
// STORE
// ═══════════════════════════════════════════════════════════════

function storeNavigate(page, data = null) {
  S.storePage = page;
  $$('.store-page').forEach(p => p.classList.remove('active'));
  $$('.nav-link').forEach(n => n.classList.remove('active'));
  const el = $(`#sp-${page}`);
  if (el) el.classList.add('active');
  const nl = $(`[data-sp="${page}"]`);
  if (nl) nl.classList.add('active');

  if (page === 'home')   loadHome();
  if (page === 'shop')   loadShop();
  if (page === 'detail' && data) openDetail(data);
  if (page === 'myorders') loadMyOrders();
}

// ─── HOME ─────────────────────────────────────────────────────
async function loadHome() {
  try {
    const [prodRes, cats] = await Promise.all([
      GET('/api/products?size=1'),
      GET('/api/categories'),
    ]);
    animCount('home-prod-count', prodRes.totalElements || 0);
    animCount('home-cat-count', cats.length || 0);

    // Featured products
    const featured = await GET('/api/products?size=8');
    renderFeatured(featured.content || []);
    renderHomeCategories(cats);
  } catch(e) {
    console.error('Home yüklenemedi:', e);
  }
}

function animCount(id, target) {
  const el = $(`#${id}`);
  if (!el) return;
  let n = 0, step = Math.ceil(target / 28);
  const t = setInterval(() => {
    n = Math.min(n + step, target);
    el.textContent = n.toLocaleString('tr-TR') + '+';
    if (n >= target) clearInterval(t);
  }, 28);
}

function renderFeatured(products) {
  const el = $('#featured-grid');
  if (!el) return;
  if (!products.length) {
    el.innerHTML = '<p style="color:var(--text-2);text-align:center;grid-column:1/-1;padding:40px">Ürün bulunamadı</p>';
    return;
  }
  el.innerHTML = products.map(p => storePCard(p)).join('');
}

function renderHomeCategories(cats) {
  const el = $('#home-cat-chips');
  if (!el) return;
  el.innerHTML = `<div class="chip active" onclick="storeNavigate('shop')">🛍️ Tümü</div>` +
    cats.slice(0,8).map(c =>
      `<div class="chip" onclick="shopByCategory(${c.id},'${esc(c.name)}')">${emoji(c.name)} ${esc(c.name)}</div>`
    ).join('');
}

function shopByCategory(id, name) {
  S.shopFilter = { categoryId: id, search: '', page: 0 };
  storeNavigate('shop');
}

// ─── SHOP ─────────────────────────────────────────────────────
async function loadShop(page = S.shopFilter.page) {
  S.shopFilter.page = page;
  const grid = $('#shop-grid');
  grid.innerHTML = `<div style="grid-column:1/-1;display:grid;grid-template-columns:repeat(auto-fill,minmax(260px,1fr));gap:22px">
    ${Array(6).fill('<div class="skeleton sk-card"></div>').join('')}
  </div>`;

  try {
    const cats = S.allCategories.length ? S.allCategories : await GET('/api/categories');
    if (!S.allCategories.length) {
      S.allCategories = cats;
      renderShopCategoryChips(cats);
    }

    const data = await GET(`/api/products?page=${page}&size=12`);
    let items = data.content || [];
    S.shopProducts = { items, totalPages: data.totalPages || 0 };

    // filter client-side (backend has no search/category query param in this impl)
    if (S.shopFilter.categoryId) {
      items = items.filter(p => p.category?.id === S.shopFilter.categoryId);
    }
    if (S.shopFilter.search) {
      const q = S.shopFilter.search.toLowerCase();
      items = items.filter(p =>
        p.name.toLowerCase().includes(q) || p.description?.toLowerCase().includes(q)
      );
    }

    $('#shop-count').textContent = `${items.length} ürün`;

    if (!items.length) {
      grid.innerHTML = `<div class="empty" style="grid-column:1/-1">
        <div class="empty-ico">🔍</div>
        <div class="empty-title">Ürün bulunamadı</div>
        <div class="empty-sub">Arama veya filtreyi değiştirmeyi deneyin</div>
      </div>`;
    } else {
      grid.innerHTML = items.map(p => storePCard(p)).join('');
    }

    renderShopPagination(page, data.totalPages || 0);

  } catch(e) {
    grid.innerHTML = `<div class="empty" style="grid-column:1/-1"><div class="empty-ico">⚠️</div><div class="empty-title">${e.message}</div></div>`;
    toast('Ürünler yüklenemedi: ' + e.message, 'error');
  }
}

function renderShopCategoryChips(cats) {
  const el = $('#shop-cats');
  if (!el) return;
  el.innerHTML = `<div class="chip ${!S.shopFilter.categoryId ? 'active' : ''}" onclick="setShopCategory(null)">🛍️ Tümü</div>` +
    cats.map(c =>
      `<div class="chip ${S.shopFilter.categoryId === c.id ? 'active' : ''}" onclick="setShopCategory(${c.id},'${esc(c.name)}')">${emoji(c.name)} ${esc(c.name)}</div>`
    ).join('');
}

function setShopCategory(id, name) {
  S.shopFilter.categoryId = id;
  S.shopFilter.page = 0;
  renderShopCategoryChips(S.allCategories);
  loadShop(0);
}

let searchDebounce;
function onShopSearch(val) {
  S.shopFilter.search = val;
  clearTimeout(searchDebounce);
  searchDebounce = setTimeout(() => loadShop(0), 320);
}

function renderShopPagination(page, total) {
  const el = $('#shop-pagination');
  if (!el || total <= 1) { if(el) el.innerHTML = ''; return; }
  let html = `<button class="pg-btn" onclick="loadShop(${page-1})" ${page===0?'disabled':''}>‹</button>`;
  for (let i = 0; i < total; i++) {
    html += `<button class="pg-btn ${i===page?'active':''}" onclick="loadShop(${i})">${i+1}</button>`;
  }
  html += `<button class="pg-btn" onclick="loadShop(${page+1})" ${page>=total-1?'disabled':''}>›</button>`;
  el.innerHTML = html;
}

function storePCard(p) {
  const stockClass = p.stock===0?'stock-empty':p.stock<5?'stock-low':'stock-ok';
  const stockLbl   = p.stock===0?'Tükendi':p.stock<5?`Son ${p.stock}`:p.stock+' adet';
  const emj = emoji(p.category?.name);
  const pJson = JSON.stringify(p).replace(/'/g,"\\'").replace(/"/g,'&quot;');
  return `
    <div class="pcard" onclick="openDetail(${pJson.replace(/&quot;/g,"'")})" id="pcard-${p.id}">
      <div class="pcard-img">
        ${emj}
        <div class="pcard-img-overlay"></div>
        ${p.stock>0?`<button class="btn btn-primary btn-sm pcard-quick-add" onclick="event.stopPropagation();quickAdd(${pJson.replace(/&quot;/g,"'")})">🛒 Sepete Ekle</button>`:''}
      </div>
      <div class="pcard-body">
        <div class="pcard-cat">${esc(p.category?.name||'Kategorisiz')}</div>
        <div class="pcard-name">${esc(p.name)}</div>
        <div class="pcard-desc">${esc(p.description||'')}</div>
      </div>
      <div class="pcard-foot">
        <div class="pcard-price">${fmtPrice(p.price)}</div>
        <span class="pcard-stock-pill ${stockClass}">${stockLbl}</span>
      </div>
    </div>`;
}

// ─── PRODUCT DETAIL ───────────────────────────────────────────
function openDetail(product) {
  S.detailProduct = product;
  S.detailQty = 1;
  storeNavigate('detail');
  renderDetailPage(product);
}

function renderDetailPage(p) {
  const el = $('#sp-detail');
  if (!el) return;
  const stockClass = p.stock===0?'stock-empty':p.stock<5?'stock-low':'stock-ok';
  const stockLbl   = p.stock===0?'Tükendi':p.stock<5?`Son ${p.stock} adet`:`${p.stock} adet stokta`;
  el.innerHTML = `
    <div style="padding:60px clamp(16px,5vw,80px)">
      <div class="detail-back" onclick="storeNavigate('shop')">← Alışverişe dön</div>
      <div class="detail-layout">
        <div class="detail-image">
          <span style="position:relative;z-index:1;filter:drop-shadow(0 0 30px rgba(124,92,252,.3))">${emoji(p.category?.name)}</span>
        </div>
        <div class="detail-info">
          <div class="detail-cat">${esc(p.category?.name||'Kategorisiz')}</div>
          <h1 class="detail-name">${esc(p.name)}</h1>
          <div class="detail-price">${fmtPrice(p.price)}</div>
          <p class="detail-desc">${esc(p.description||'Bu ürün için açıklama bulunmamaktadır.')}</p>
          <div class="detail-meta">
            <div class="detail-meta-item">
              <div class="detail-meta-label">Stok</div>
              <div class="detail-meta-value"><span class="pcard-stock-pill ${stockClass}">${stockLbl}</span></div>
            </div>
            <div class="detail-meta-item">
              <div class="detail-meta-label">Birim Fiyat</div>
              <div class="detail-meta-value">${fmtPrice(p.price)}</div>
            </div>
          </div>
          ${p.stock > 0 ? `
          <div class="qty-row">
            <div class="qty-control-lg">
              <button class="qty-btn-lg" onclick="changeDetailQty(-1)">−</button>
              <span class="qty-num" id="detail-qty">1</span>
              <button class="qty-btn-lg" onclick="changeDetailQty(1)">+</button>
            </div>
            <span style="font-size:14px;color:var(--text-2)">adet</span>
          </div>
          <button class="btn btn-primary btn-lg btn-full" onclick="addDetailToCart()">
            🛒 Sepete Ekle — <span id="detail-total">${fmtPrice(p.price)}</span>
          </button>` : `
          <button class="btn btn-outline btn-lg btn-full" disabled style="opacity:.5;cursor:not-allowed">
            ❌ Stokta Yok
          </button>`}
        </div>
      </div>
    </div>`;
}

function changeDetailQty(delta) {
  const p = S.detailProduct;
  S.detailQty = Math.max(1, Math.min(S.detailQty + delta, p?.stock || 1));
  const qEl = $('#detail-qty');
  const tEl = $('#detail-total');
  if (qEl) qEl.textContent = S.detailQty;
  if (tEl && p) tEl.textContent = fmtPrice(p.price * S.detailQty);
}

function addDetailToCart() {
  const p = S.detailProduct;
  if (!p) return;
  addToCart(p, S.detailQty);
}

// ─── MY ORDERS ───────────────────────────────────────────────
async function loadMyOrders() {
  const el = $('#myorders-content');
  el.innerHTML = '<div class="spinner"></div>';
  try {
    const data = await GET('/api/orders?size=20');
    const orders = data.content || [];
    if (!orders.length) {
      el.innerHTML = `<div class="empty"><div class="empty-ico">🧾</div><div class="empty-title">Henüz siparişiniz yok</div><div class="empty-sub">Alışveriş yaparak başlayın!</div><br><button class="btn btn-primary" onclick="storeNavigate('shop')">🛍️ Alışverişe Başla</button></div>`;
      return;
    }
    el.innerHTML = orders.map(o => `
      <div style="background:var(--bg-card);border:1px solid var(--border);border-radius:var(--r-lg);padding:22px;margin-bottom:16px;transition:all .18s"
           onmouseenter="this.style.borderColor='var(--border-accent)'" onmouseleave="this.style.borderColor='var(--border)'">
        <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;flex-wrap:wrap;gap:10px">
          <div>
            <div style="font-size:16px;font-weight:800">Sipariş #${o.id}</div>
            <div style="font-size:12px;color:var(--text-2);margin-top:2px">${fmtDate(o.createdAt)}</div>
          </div>
          <div style="display:flex;align-items:center;gap:12px">
            <span class="badge badge-${(o.status||'').toLowerCase()}">${statusTr(o.status)}</span>
            <span style="font-size:18px;font-weight:900;color:var(--accent-2)">${fmtPrice(o.totalPrice)}</span>
          </div>
        </div>
        <div style="display:flex;flex-direction:column;gap:8px">
          ${(o.items||[]).map(item => `
            <div style="display:flex;align-items:center;justify-content:space-between;padding:10px 14px;background:var(--bg-elevated);border-radius:var(--r-sm);border:1px solid var(--border)">
              <div style="display:flex;align-items:center;gap:10px">
                <span style="font-size:22px">${emoji(item.product?.category?.name)}</span>
                <div>
                  <div style="font-size:14px;font-weight:700">${esc(item.product?.name||'Ürün')}</div>
                  <div style="font-size:12px;color:var(--text-2)">x${item.quantity}</div>
                </div>
              </div>
              <span style="font-weight:800">${fmtPrice(item.orderPrice)}</span>
            </div>
          `).join('')}
        </div>
      </div>
    `).join('');
  } catch(e) {
    el.innerHTML = `<div class="empty"><div class="empty-ico">⚠️</div><div class="empty-title">${e.message}</div></div>`;
    toast('Siparişler yüklenemedi', 'error');
  }
}

// ─── CART ────────────────────────────────────────────────────
function addToCart(product, qty = 1) {
  const existing = S.cart.find(i => i.product.id === product.id);
  if (existing) {
    const newQty = existing.quantity + qty;
    if (newQty > product.stock) { toast('Stok yetmez!', 'error'); return; }
    existing.quantity = newQty;
  } else {
    if (qty > product.stock) { toast('Stok yetmez!', 'error'); return; }
    S.cart.push({ product, quantity: qty });
  }
  updateCartUI();
  toast(`${product.name} sepete eklendi 🛒`, 'success');
}

function quickAdd(product) {
  addToCart(product, 1);
}

function removeCartItem(id) {
  S.cart = S.cart.filter(i => i.product.id !== id);
  updateCartUI();
}

function changeCartQty(id, delta) {
  const item = S.cart.find(i => i.product.id === id);
  if (!item) return;
  item.quantity += delta;
  if (item.quantity <= 0) removeCartItem(id);
  else if (item.quantity > item.product.stock) {
    item.quantity = item.product.stock;
    toast('Stok sınırına ulaşıldı!', 'error');
  }
  updateCartUI();
}

function updateCartUI() {
  const count = S.cart.reduce((s,i) => s + i.quantity, 0);
  const subtotal = S.cart.reduce((s,i) => s + i.product.price * i.quantity, 0);

  // badge
  const badge = $('#cart-badge');
  if (badge) { badge.textContent = count; badge.classList.toggle('show', count > 0); }

  // drawer body
  const body = $('#cart-body');
  if (body) {
    if (!S.cart.length) {
      body.innerHTML = `<div class="empty" style="padding:60px 20px">
        <div class="empty-ico">🛒</div>
        <div class="empty-title">Sepet boş</div>
        <div class="empty-sub">Ürün eklemek için kataloğa göz atın</div>
      </div>`;
    } else {
      body.innerHTML = S.cart.map(item => `
        <div class="cart-item-card">
          <div class="cart-item-emoji">${emoji(item.product.category?.name)}</div>
          <div class="cart-item-info">
            <div class="cart-item-name">${esc(item.product.name)}</div>
            <div class="cart-item-unit">${fmtPrice(item.product.price)} / adet</div>
          </div>
          <div class="cart-qty-row">
            <button class="cqb" onclick="changeCartQty(${item.product.id},-1)">−</button>
            <span class="cqn">${item.quantity}</span>
            <button class="cqb" onclick="changeCartQty(${item.product.id},1)">+</button>
          </div>
          <button class="rm-btn" onclick="removeCartItem(${item.product.id})">✕</button>
        </div>
      `).join('');
    }
  }

  // totals
  const tot = $('#cart-total-val');
  if (tot) tot.textContent = fmtPrice(subtotal);

  const cb = $('#checkout-btn');
  if (cb) cb.disabled = !S.cart.length;
}

function toggleCart() {
  const d = $('#cart-drawer');
  const o = $('#overlay');
  const open = d.classList.toggle('open');
  o.classList.toggle('show', open);
  if (open) updateCartUI();
}

function closeCart() {
  $('#cart-drawer').classList.remove('open');
  $('#overlay').classList.remove('show');
}

async function checkout() {
  if (!S.cart.length) return;
  const body = { items: S.cart.map(i => ({ productId: i.product.id, quantity: i.quantity })) };
  try {
    const order = await POST('/api/orders', body);
    S.cart = [];
    updateCartUI();
    closeCart();
    toast(`Sipariş #${order.id} oluşturuldu! 🎉`, 'success');
  } catch(e) {
    toast('Sipariş verilemedi: ' + e.message, 'error');
  }
}

// ─── MODAL ────────────────────────────────────────────────────
function showModal(title, content) {
  $('#modal-title').textContent = title;
  $('#modal-body').innerHTML = content;
  $('#modal-backdrop').classList.add('open');
}

function closeModal() {
  $('#modal-backdrop').classList.remove('open');
}

// ═══════════════════════════════════════════════════════════════
// ADMIN
// ═══════════════════════════════════════════════════════════════

function adminNavigate(page) {
  S.adminPage = page;
  $$('.admin-page').forEach(p => p.classList.remove('active'));
  $$('.sb-item').forEach(n => n.classList.remove('active'));
  const el = $(`#ap-${page}`);
  if (el) el.classList.add('active');
  const nav = $(`[data-ap="${page}"]`);
  if (nav) nav.classList.add('active');
  const titles = { dashboard:'Dashboard', products:'Ürünler', categories:'Kategoriler', orders:'Siparişler', users:'Kullanıcılar' };
  $('#admin-topbar-title').textContent = titles[page] || page;
  if (page === 'dashboard')  loadAdminDashboard();
  if (page === 'products')   loadAdminProducts(0);
  if (page === 'categories') loadAdminCategories();
  if (page === 'orders')     loadAdminOrders(0);
  if (page === 'users')      loadAdminUsers();
}

// ─── Admin Dashboard ──────────────────────────────────────────
async function loadAdminDashboard() {
  try {
    const [pr, cats, or, us] = await Promise.all([
      GET('/api/products?size=1'),
      GET('/api/categories'),
      GET('/api/orders?size=1'),
      GET('/api/users'),
    ]);
    animCount2('adstat-prod', pr.totalElements || 0);
    animCount2('adstat-cats', cats.length || 0);
    animCount2('adstat-orders', or.totalElements || 0);
    animCount2('adstat-users', us.length || 0);
    const recOrd = await GET('/api/orders?size=6');
    renderAdminRecentOrders(recOrd.content || []);
  } catch(e) { console.error(e); }
}

function animCount2(id, target) {
  const el = $(`#${id}`);
  if (!el) return;
  let n = 0, step = Math.ceil(target / 28) || 1;
  const t = setInterval(() => {
    n = Math.min(n + step, target);
    el.textContent = n;
    if (n >= target) clearInterval(t);
  }, 30);
}

function renderAdminRecentOrders(orders) {
  const el = $('#admin-recent-orders');
  if (!el) return;
  if (!orders.length) {
    el.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:30px;color:var(--text-2)">Sipariş yok</td></tr>`;
    return;
  }
  el.innerHTML = orders.map(o => `
    <tr>
      <td><strong>#${o.id}</strong></td>
      <td>${fmtDate(o.createdAt)}</td>
      <td><span class="badge badge-${(o.status||'').toLowerCase()}">${statusTr(o.status)}</span></td>
      <td><strong>${fmtPrice(o.totalPrice)}</strong></td>
      <td>${(o.items||[]).length} ürün</td>
    </tr>
  `).join('');
}

// ─── Admin Products ───────────────────────────────────────────
async function loadAdminProducts(page = 0) {
  const grid = $('#admin-prod-grid');
  grid.innerHTML = `<div style="grid-column:1/-1;display:grid;grid-template-columns:repeat(auto-fill,minmax(240px,1fr));gap:18px">
    ${Array(6).fill('<div class="skeleton" style="height:260px;border-radius:20px"></div>').join('')}
  </div>`;

  try {
    const data = await GET(`/api/products?page=${page}&size=12`);
    S.adminProducts = { items: data.content || [], page, totalPages: data.totalPages || 0 };

    if (!data.content.length) {
      grid.innerHTML = `<div class="empty" style="grid-column:1/-1"><div class="empty-ico">📦</div><div class="empty-title">Ürün yok</div></div>`;
    } else {
      grid.innerHTML = data.content.map(p => `
        <div class="apc" id="apc-${p.id}">
          <div class="apc-img">${emoji(p.category?.name)}</div>
          <div class="apc-body">
            <div class="apc-cat">${esc(p.category?.name||'Kategorisiz')}</div>
            <div class="apc-name">${esc(p.name)}</div>
            <div style="font-size:12px;color:var(--text-2);margin:4px 0 8px;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden">${esc(p.description||'')}</div>
            <div class="apc-price">${fmtPrice(p.price)}</div>
          </div>
          <div class="apc-foot">
            <button class="btn btn-ghost btn-sm" onclick="adminViewProduct(${p.id})">👁 Detay</button>
            <button class="btn btn-danger-soft btn-sm" onclick="adminDeleteProduct(${p.id})">🗑</button>
          </div>
        </div>
      `).join('');
    }

    renderAdminPagination('admin-prod-pg', page, data.totalPages || 0, p => loadAdminProducts(p));

  } catch(e) {
    grid.innerHTML = `<div class="empty" style="grid-column:1/-1"><div class="empty-ico">⚠️</div><div class="empty-title">${e.message}</div></div>`;
    toast('Yüklenemedi', 'error');
  }
}

async function adminViewProduct(id) {
  try {
    const p = await GET(`/api/products/${id}`);
    showModal('Ürün Detayı', `
      <div style="text-align:center;margin-bottom:22px">
        <div style="font-size:72px;margin-bottom:12px">${emoji(p.category?.name)}</div>
        <div style="font-size:20px;font-weight:800">${esc(p.name)}</div>
        <div style="font-size:13px;color:var(--text-2);margin-top:4px">${esc(p.description||'')}</div>
      </div>
      <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px">
        <div style="background:var(--bg-elevated);border:1px solid var(--border);border-radius:var(--r-sm);padding:14px">
          <div style="font-size:11px;color:var(--text-3);text-transform:uppercase;letter-spacing:1px;margin-bottom:4px">Fiyat</div>
          <div style="font-size:22px;font-weight:900;color:var(--accent-2)">${fmtPrice(p.price)}</div>
        </div>
        <div style="background:var(--bg-elevated);border:1px solid var(--border);border-radius:var(--r-sm);padding:14px">
          <div style="font-size:11px;color:var(--text-3);text-transform:uppercase;letter-spacing:1px;margin-bottom:4px">Stok</div>
          <div style="font-size:22px;font-weight:900">${p.stock}</div>
        </div>
        <div style="background:var(--bg-elevated);border:1px solid var(--border);border-radius:var(--r-sm);padding:14px;grid-column:1/-1">
          <div style="font-size:11px;color:var(--text-3);text-transform:uppercase;letter-spacing:1px;margin-bottom:4px">Kategori</div>
          <div style="font-size:16px;font-weight:700">${esc(p.category?.name||'Kategorisiz')}</div>
        </div>
      </div>
    `);
  } catch(e) { toast(e.message, 'error'); }
}

async function adminDeleteProduct(id) {
  if (!confirm('Bu ürünü silmek istediğinize emin misiniz?')) return;
  try {
    await DELETE(`/api/products/${id}`);
    toast('Ürün silindi', 'success');
    loadAdminProducts(S.adminProducts.page);
  } catch(e) { toast('Silinemedi: ' + e.message, 'error'); }
}

async function openNewProductModal() {
  const cats = S.allCategories.length ? S.allCategories : await GET('/api/categories').catch(() => []);
  if (!S.allCategories.length) S.allCategories = cats;

  showModal('Yeni Ürün Ekle', `
    <form id="prod-form" onsubmit="submitNewProduct(event)">
      <div class="form-group">
        <label class="form-label">Ürün Adı *</label>
        <input name="name" class="form-input" placeholder="Ürün adı..." required autofocus>
      </div>
      <div class="form-group">
        <label class="form-label">Açıklama *</label>
        <textarea name="description" class="form-input" rows="3" style="resize:vertical" placeholder="Ürün açıklaması..." required></textarea>
      </div>
      <div class="form-grid-2">
        <div class="form-group">
          <label class="form-label">Fiyat (₺) *</label>
          <input name="price" type="number" step="0.01" min="0.01" class="form-input" placeholder="0.00" required>
        </div>
        <div class="form-group">
          <label class="form-label">Stok *</label>
          <input name="stock" type="number" min="0" class="form-input" placeholder="0" required>
        </div>
      </div>
      <div class="form-group">
        <label class="form-label">Kategori *</label>
        <select name="categoryId" class="form-input" required>
          <option value="">Seçin...</option>
          ${cats.map(c => `<option value="${c.id}">${esc(c.name)}</option>`).join('')}
        </select>
      </div>
      <div class="modal-foot" style="padding:0;border:none;margin-top:8px">
        <button type="button" class="btn btn-ghost" onclick="closeModal()">İptal</button>
        <button type="submit" class="btn btn-primary">✨ Oluştur</button>
      </div>
    </form>
  `);
}

async function submitNewProduct(e) {
  e.preventDefault();
  const f = e.target;
  try {
    await POST('/api/products', {
      name: f.name.value.trim(),
      description: f.description.value.trim(),
      price: parseFloat(f.price.value),
      stock: parseInt(f.stock.value),
      categoryId: parseInt(f.categoryId.value),
    });
    toast('Ürün oluşturuldu! 🎉', 'success');
    closeModal();
    loadAdminProducts(0);
  } catch(e) { toast('Hata: ' + e.message, 'error'); }
}

// ─── Admin Categories ─────────────────────────────────────────
async function loadAdminCategories() {
  const el = $('#admin-cats-grid');
  el.innerHTML = '<div class="spinner"></div>';
  try {
    const data = await GET('/api/categories');
    S.allCategories = data;
    if (!data.length) {
      el.innerHTML = `<div class="empty" style="grid-column:1/-1"><div class="empty-ico">🏷️</div><div class="empty-title">Kategori yok</div></div>`;
    } else {
      el.innerHTML = data.map(c => `
        <div class="cat-card" id="cat-${c.id}">
          <div>
            <div class="cat-name">${esc(c.name)}</div>
            <div class="cat-id">ID: ${c.id}</div>
          </div>
          <div style="display:flex;gap:8px">
            <button class="btn btn-ghost btn-sm" onclick="adminViewCategory(${c.id})">👁</button>
            <button class="btn btn-danger-soft btn-sm" onclick="adminDeleteCategory(${c.id})">🗑</button>
          </div>
        </div>
      `).join('');
    }
  } catch(e) {
    el.innerHTML = `<div class="empty"><div class="empty-ico">⚠️</div><div class="empty-title">${e.message}</div></div>`;
    toast('Kategoriler yüklenemedi', 'error');
  }
}

async function adminViewCategory(id) {
  try {
    const c = await GET(`/api/categories/${id}`);
    showModal('Kategori Detayı', `
      <div style="text-align:center;padding:20px 0">
        <div style="font-size:52px;margin-bottom:14px">🏷️</div>
        <div style="font-size:24px;font-weight:900">${esc(c.name)}</div>
        <div style="font-size:13px;color:var(--text-2);margin-top:6px">ID: ${c.id}</div>
      </div>
    `);
  } catch(e) { toast(e.message, 'error'); }
}

async function adminDeleteCategory(id) {
  if (!confirm('Bu kategoriyi silmek istediğinize emin misiniz?')) return;
  try {
    await DELETE(`/api/categories/${id}`);
    toast('Kategori silindi', 'success');
    loadAdminCategories();
  } catch(e) { toast('Silinemedi: ' + e.message, 'error'); }
}

function openNewCategoryModal() {
  showModal('Yeni Kategori Ekle', `
    <form id="cat-form" onsubmit="submitNewCategory(event)">
      <div class="form-group">
        <label class="form-label">Kategori Adı *</label>
        <input name="catName" class="form-input" placeholder="Kategori adı..." required autofocus>
      </div>
      <div class="modal-foot" style="padding:0;border:none;margin-top:8px">
        <button type="button" class="btn btn-ghost" onclick="closeModal()">İptal</button>
        <button type="submit" class="btn btn-primary">✨ Oluştur</button>
      </div>
    </form>
  `);
}

async function submitNewCategory(e) {
  e.preventDefault();
  try {
    await POST('/api/categories', { name: e.target.catName.value.trim() });
    toast('Kategori oluşturuldu! 🎉', 'success');
    closeModal();
    loadAdminCategories();
  } catch(e) { toast('Hata: ' + e.message, 'error'); }
}

// ─── Admin Orders ─────────────────────────────────────────────
async function loadAdminOrders(page = 0) {
  const tbody = $('#admin-orders-body');
  tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:30px"><div class="spinner" style="margin:0 auto"></div></td></tr>`;
  try {
    const data = await GET(`/api/orders?page=${page}&size=10`);
    S.adminOrders = { items: data.content || [], page, totalPages: data.totalPages || 0 };

    if (!data.content.length) {
      tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:40px;color:var(--text-2)">Sipariş yok</td></tr>`;
    } else {
      tbody.innerHTML = data.content.map(o => `
        <tr>
          <td><strong>#${o.id}</strong></td>
          <td>${fmtDate(o.createdAt)}</td>
          <td><span class="badge badge-${(o.status||'').toLowerCase()}">${statusTr(o.status)}</span></td>
          <td><strong>${fmtPrice(o.totalPrice)}</strong></td>
          <td>
            <div style="display:flex;gap:8px">
              <button class="btn btn-ghost btn-sm" onclick="adminViewOrder(${o.id})">👁</button>
              <button class="btn btn-primary btn-sm" onclick="adminUpdateStatus(${o.id},'${o.status}')">⚡ Durum</button>
            </div>
          </td>
        </tr>
      `).join('');
    }

    renderAdminPagination('admin-orders-pg', page, data.totalPages || 0, p => loadAdminOrders(p));

  } catch(e) {
    tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:30px;color:var(--danger)">${e.message}</td></tr>`;
    toast('Siparişler yüklenemedi', 'error');
  }
}

async function adminViewOrder(id) {
  try {
    const o = await GET(`/api/orders/${id}`);
    showModal(`Sipariş #${o.id}`, `
      <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px;margin-bottom:18px">
        <div style="background:var(--bg-elevated);border:1px solid var(--border);border-radius:var(--r-sm);padding:14px">
          <div style="font-size:11px;color:var(--text-3);text-transform:uppercase;letter-spacing:1px;margin-bottom:6px">Durum</div>
          <span class="badge badge-${(o.status||'').toLowerCase()}">${statusTr(o.status)}</span>
        </div>
        <div style="background:var(--bg-elevated);border:1px solid var(--border);border-radius:var(--r-sm);padding:14px">
          <div style="font-size:11px;color:var(--text-3);text-transform:uppercase;letter-spacing:1px;margin-bottom:4px">Toplam</div>
          <div style="font-size:20px;font-weight:900;color:var(--accent-2)">${fmtPrice(o.totalPrice)}</div>
        </div>
        <div style="background:var(--bg-elevated);border:1px solid var(--border);border-radius:var(--r-sm);padding:14px;grid-column:1/-1">
          <div style="font-size:11px;color:var(--text-3);text-transform:uppercase;letter-spacing:1px;margin-bottom:4px">Tarih</div>
          <div style="font-size:14px;font-weight:700">${fmtDate(o.createdAt)}</div>
        </div>
      </div>
      <div style="font-size:12px;font-weight:700;text-transform:uppercase;letter-spacing:1px;color:var(--text-3);margin-bottom:10px">Kalemler</div>
      ${(o.items||[]).map(item => `
        <div style="display:flex;align-items:center;justify-content:space-between;padding:10px 14px;background:var(--bg-elevated);border-radius:var(--r-sm);border:1px solid var(--border);margin-bottom:8px">
          <div style="display:flex;align-items:center;gap:10px">
            <span style="font-size:20px">${emoji(item.product?.category?.name)}</span>
            <div>
              <div style="font-size:13px;font-weight:700">${esc(item.product?.name||'Ürün')}</div>
              <div style="font-size:12px;color:var(--text-2)">x${item.quantity}</div>
            </div>
          </div>
          <strong>${fmtPrice(item.orderPrice)}</strong>
        </div>
      `).join('')}
    `);
  } catch(e) { toast(e.message, 'error'); }
}

function adminUpdateStatus(id, current) {
  const statuses = ['PENDING','CONFIRMED','SHIPPED','DELIVERED','CANCELLED'];
  showModal(`Sipariş #${id} — Durum Güncelle`, `
    <div class="form-group">
      <label class="form-label">Yeni Durum</label>
      <select id="status-sel" class="form-input">
        ${statuses.map(s => `<option value="${s}" ${s===current?'selected':''}>${statusTr(s)}</option>`).join('')}
      </select>
    </div>
    <div class="modal-foot" style="padding:0;border:none;margin-top:8px">
      <button class="btn btn-ghost" onclick="closeModal()">İptal</button>
      <button class="btn btn-primary" onclick="submitStatusUpdate(${id})">⚡ Güncelle</button>
    </div>
  `);
}

async function submitStatusUpdate(id) {
  const status = $('#status-sel').value;
  try {
    await PATCH(`/api/orders/${id}/status`, status);
    toast('Durum güncellendi!', 'success');
    closeModal();
    loadAdminOrders(S.adminOrders.page);
  } catch(e) { toast('Güncellenemedi: ' + e.message, 'error'); }
}

// ─── Admin Users ──────────────────────────────────────────────
async function loadAdminUsers() {
  const tbody = $('#admin-users-body');
  tbody.innerHTML = `<tr><td colspan="4" style="text-align:center;padding:30px"><div class="spinner" style="margin:0 auto"></div></td></tr>`;
  try {
    const data = await GET('/api/users');
    S.adminUsers = data;
    if (!data.length) {
      tbody.innerHTML = `<tr><td colspan="4" style="text-align:center;padding:40px;color:var(--text-2)">Kullanıcı yok</td></tr>`;
    } else {
      tbody.innerHTML = data.map(u => `
        <tr>
          <td><strong>#${u.id}</strong></td>
          <td><strong>${esc(u.username||'-')}</strong></td>
          <td>${esc(u.mail||'-')}</td>
          <td>
            <div style="display:flex;align-items:center;gap:8px">
              <span class="badge badge-${(u.role||'user').toLowerCase()}">${u.role||'USER'}</span>
              <button class="btn btn-ghost btn-sm" onclick="adminViewUser(${u.id})">👁</button>
            </div>
          </td>
        </tr>
      `).join('');
    }
  } catch(e) {
    tbody.innerHTML = `<tr><td colspan="4" style="text-align:center;padding:30px;color:var(--danger)">${e.message}</td></tr>`;
    toast('Kullanıcılar yüklenemedi', 'error');
  }
}

async function adminViewUser(id) {
  try {
    const u = await GET(`/api/users/${id}`);
    showModal('Kullanıcı Detayı', `
      <div style="text-align:center;margin-bottom:22px">
        <div style="width:72px;height:72px;border-radius:50%;background:linear-gradient(135deg,var(--accent),var(--accent-2));display:flex;align-items:center;justify-content:center;font-size:30px;font-weight:900;margin:0 auto 12px;color:#fff">
          ${(u.username||'U')[0].toUpperCase()}
        </div>
        <div style="font-size:20px;font-weight:800">${esc(u.username||'-')}</div>
        <div style="font-size:13px;color:var(--text-2);margin-top:4px">${esc(u.mail||'-')}</div>
      </div>
      <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px">
        <div style="background:var(--bg-elevated);border:1px solid var(--border);border-radius:var(--r-sm);padding:14px">
          <div style="font-size:11px;color:var(--text-3);text-transform:uppercase;letter-spacing:1px;margin-bottom:4px">ID</div>
          <div style="font-size:20px;font-weight:900">#${u.id}</div>
        </div>
        <div style="background:var(--bg-elevated);border:1px solid var(--border);border-radius:var(--r-sm);padding:14px">
          <div style="font-size:11px;color:var(--text-3);text-transform:uppercase;letter-spacing:1px;margin-bottom:6px">Rol</div>
          <span class="badge badge-${(u.role||'user').toLowerCase()}">${u.role||'USER'}</span>
        </div>
      </div>
    `);
  } catch(e) { toast(e.message, 'error'); }
}

// ─── Admin Pagination ─────────────────────────────────────────
function renderAdminPagination(id, page, total, cb) {
  const el = $(`#${id}`);
  if (!el || total <= 1) { if(el) el.innerHTML=''; return; }
  let html = `<button class="pg-btn" onclick="(${cb})(${page-1})" ${page===0?'disabled':''}>‹</button>`;
  for (let i = 0; i < total; i++)
    html += `<button class="pg-btn ${i===page?'active':''}" onclick="(${cb})(${i})">${i+1}</button>`;
  html += `<button class="pg-btn" onclick="(${cb})(${page+1})" ${page>=total-1?'disabled':''}>›</button>`;
  el.innerHTML = html;
}

// ─── Health Check ─────────────────────────────────────────────
async function checkHealth() {
  try {
    await fetch(`${BASE}/api/products?size=1`, { signal: AbortSignal.timeout(3000) });
    const dot  = $('#health-dot');
    const txt  = $('#health-txt');
    if (dot) dot.className = 'hdot on';
    if (txt) txt.textContent = 'Backend çevrimiçi';
  } catch {
    const dot  = $('#health-dot');
    const txt  = $('#health-txt');
    if (dot) dot.className = 'hdot off';
    if (txt) txt.textContent = 'Backend çevrimdışı';
  }
}

// ─── Init ─────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  // store nav clicks
  $$('.nav-link').forEach(el => {
    el.addEventListener('click', () => storeNavigate(el.dataset.sp));
  });

  // admin nav clicks
  $$('.sb-item').forEach(el => {
    el.addEventListener('click', () => adminNavigate(el.dataset.ap));
  });

  // modal close on backdrop click
  const mb = $('#modal-backdrop');
  if (mb) mb.addEventListener('click', e => { if (e.target === mb) closeModal(); });

  // overlay closes cart
  const ov = $('#overlay');
  if (ov) ov.addEventListener('click', closeCart);

  // load categories on boot for store chips
  GET('/api/categories').then(d => {
    S.allCategories = d || [];
  }).catch(() => {});

  // health check loop
  checkHealth();
  setInterval(checkHealth, 30000);

  // boot store
  storeNavigate('home');
});
