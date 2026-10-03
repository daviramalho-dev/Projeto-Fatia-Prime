const WHATSAPP_NUMBER = '5561993637373';
const CART_STORAGE_KEY = 'fatia-prime-cart';
const ORDER_STORAGE_KEY = 'fatia-prime-last-order';
const ORDER_HISTORY_STORAGE_KEY = 'fatia-prime-orders';
const PRODUCT_STORAGE_KEY = 'fatia-prime-products';
const ORDER_STATUSES = ['Pedido recebido', 'Pedido em andamento', 'Pedido concluído'];
const PRODUCT_CATEGORIES = ['classicas', 'carnes', 'frango', 'queijos'];
const money = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const panel = document.querySelector('.cart-panel');
const checkoutPanel = document.querySelector('.checkout-panel');
const checkoutForm = document.querySelector('#checkout-form');
const checkoutMessage = document.querySelector('.checkout-message');
const checkoutItemsElement = document.querySelector('.checkout-items');
const checkoutTotalElement = document.querySelector('.checkout-total strong');
const checkoutSubtotalElement = document.querySelector('.checkout-subtotal');
const checkoutFreightElement = document.querySelector('.checkout-freight');
const cartSubtotalElement = document.querySelector('.cart-subtotal');
const cartFreightElement = document.querySelector('.cart-freight');
const cartDeliveryCep = document.querySelector('.cart-delivery-cep');
const cartTotalLabel = document.querySelector('.cart-total span');
const checkoutTotalLabel = document.querySelector('.checkout-total span');
const pizzaCustomizer = document.querySelector('.pizza-customizer');
const customizerFirstFlavor = document.querySelector('.customizer-first-flavor');
const customizerSecondFlavor = document.querySelector('.customizer-second-flavor');
const customizerSecondField = document.querySelector('.customizer-second-field');
const customizerBorder = document.querySelector('.customizer-border');
const customizerAddonList = document.querySelector('.customizer-addon-list');
const customizerSauceList = document.querySelector('.customizer-sauce-list');
const customizerQuantityValue = document.querySelector('.customizer-quantity-value');
const customizerAddToCart = document.querySelector('.customizer-add-to-cart');
const confirmationPanel = document.querySelector('.confirmation-panel');
const confirmationSummaryElement = document.querySelector('.confirmation-items');
const confirmationCodeElement = document.querySelector('.confirmation-code');
const confirmationStatusElement = document.querySelector('.confirmation-status');
const confirmationCustomerElement = document.querySelector('.confirmation-customer');
const confirmationTotalElement = document.querySelector('.confirmation-total strong');
const confirmationSubtotalElement = document.querySelector('.confirmation-subtotal');
const confirmationFreightElement = document.querySelector('.confirmation-freight');
const confirmationTrack = document.querySelector('.confirmation-track');
const confirmationWhatsappLink = document.querySelector('.confirmation-whatsapp');
const orderQueryForm = document.querySelector('#order-query-form');
const orderQueryMessage = document.querySelector('.order-query-message');
const orderQueryResult = document.querySelector('.order-query-result');
const orderQueryCode = document.querySelector('#order-query-code');
const orderQueryPhone = document.querySelector('#order-query-phone');
const orderQueryStatusMessage = document.querySelector('.order-query-status-message');
const orderQuerySubtotalElement = document.querySelector('.order-query-subtotal');
const orderQueryFreightElement = document.querySelector('.order-query-freight');
const adminLoginForm = document.querySelector('#admin-login-form');
const adminLoginMessage = document.querySelector('.admin-login-message');
const adminOrderSearch = document.querySelector('#admin-order-search');
const adminOrderStatus = document.querySelector('#admin-order-status');
const adminOrderList = document.querySelector('.admin-order-list');
const adminOrdersMessage = document.querySelector('.admin-orders-message');
const adminOrdersEmpty = document.querySelector('.admin-orders-empty');
const adminSummaryTotal = document.querySelector('#admin-summary-total');
const adminSummaryReceived = document.querySelector('#admin-summary-received');
const adminSummaryProgress = document.querySelector('#admin-summary-progress');
const adminSummaryFinished = document.querySelector('#admin-summary-finished');
const adminOrderDetails = document.querySelector('.admin-order-details');
const adminOrderSubtotalElement = document.querySelector('.admin-order-subtotal strong');
const adminOrderFreightElement = document.querySelector('.admin-order-freight strong');
const adminOrderStatusEditor = document.querySelector('#admin-order-status-editor');
const adminStatusMessage = document.querySelector('.admin-status-message');
const adminStatusSave = document.querySelector('.admin-status-save');
const adminProductForm = document.querySelector('#admin-product-form');
const adminCatalogList = document.querySelector('.admin-catalog-list');
const adminCatalogEmpty = document.querySelector('.admin-catalog-empty');
const adminProductFormMessage = document.querySelector('.admin-product-form-message');
const adminProductFormTitle = document.querySelector('#admin-product-form-title');
const adminProductFormLabel = document.querySelector('#admin-product-form-label');
const adminProductCancel = document.querySelector('.admin-product-cancel');
const adminPizzaOptionList = document.querySelector('.admin-pizza-option-list');
const adminPizzaOptionsEmpty = document.querySelector('.admin-pizza-options-empty');
const adminPizzaOptionForm = document.querySelector('#admin-pizza-option-form');
const adminPizzaOptionFormTitle = document.querySelector('#admin-pizza-option-form-title');
const adminPizzaOptionMessage = document.querySelector('.admin-pizza-option-message');
const adminPizzaOptionCancel = document.querySelector('.admin-pizza-option-cancel');
const adminAccess = document.querySelector('.admin-access');
const adminHeader = document.querySelector('.admin-header');
const adminLogout = document.querySelector('.admin-logout');
const adminOrdersSection = document.querySelector('.admin-orders');
const adminCatalogSection = document.querySelector('.admin-catalog');
const publicHeader = document.querySelector('.public-header');
const publicFooter = document.querySelector('footer');
const publicOrderTracking = document.querySelector('.order-tracking');
const publicWhatsapp = document.querySelector('.whatsapp-float');
const publicMenuFilters = document.querySelector('.menu-filters');
const catalogFeedback = document.querySelector('.catalog-feedback');
const backdrop = document.querySelector('.cart-backdrop');
const itemsElement = document.querySelector('.cart-items');
const totalElement = document.querySelector('.cart-total strong');
let deliveryQuoteRequestId = 0;
let currentDeliveryQuote = null;
let editingProductId = null;
let editingOptionId = null;
let customizerQuantity = 1;
let customizerOptions = [];
let selectedAdminOrderId = null;
let adminOrders = [];
let adminOrdersRequestId = 0;
let publicProducts = [];
let publicCategories = [];
let adminProducts = [];
let adminCategories = [];
let adminPizzaOptions = [];
let adminCatalogRequestId = 0;

function setInterfaceMode(mode) {
    const isPublic = mode === 'public';
    const isLogin = mode === 'admin-login';
    const isWorkspace = mode === 'admin-workspace';
    document.body.classList.toggle('admin-mode', isWorkspace);
    if (adminHeader) adminHeader.hidden = !isWorkspace;
    if (publicHeader) publicHeader.hidden = !isPublic;
    if (adminAccess) adminAccess.hidden = !isLogin;
    if (adminOrdersSection) adminOrdersSection.hidden = !isWorkspace;
    if (adminCatalogSection) adminCatalogSection.hidden = !isWorkspace;
    if (publicFooter) publicFooter.hidden = !isPublic;
    if (publicOrderTracking) publicOrderTracking.hidden = !isPublic;
    if (publicWhatsapp) publicWhatsapp.hidden = !isPublic;
    document.querySelectorAll('main > section').forEach((section) => {
        const isAdminSection = section.classList.contains('admin-access')
            || section.classList.contains('admin-orders')
            || section.classList.contains('admin-catalog');
        section.hidden = isAdminSection ? !((isLogin && section.classList.contains('admin-access')) || (isWorkspace && !section.classList.contains('admin-access'))) : !isPublic;
    });
    if (isPublic) {
        window.history.replaceState({}, '', '#inicio');
        window.scrollTo({ top: 0, behavior: 'smooth' });
    } else {
        closeCart();
        closeCheckout();
        closeConfirmation();
    }
}

function openAdminLogin() {
    setInterfaceMode('admin-login');
    window.location.hash = 'acesso-admin';
    adminAccess?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    adminLoginForm?.elements.namedItem('adminEmail')?.focus();
}

function openAdminWorkspace() {
    setInterfaceMode('admin-workspace');
    window.location.hash = 'painel-pedidos';
    adminOrdersSection?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    loadAdminOrders();
    loadAdminProducts();
}

function readCookie(name) {
    const prefix = `${name}=`;
    const cookie = document.cookie.split('; ').find((item) => item.startsWith(prefix));
    return cookie ? decodeURIComponent(cookie.slice(prefix.length)) : '';
}

async function getCsrfToken() {
    const response = await fetch('/api/auth/csrf', { credentials: 'same-origin' });
    if (!response.ok) throw new Error('csrf');
    const data = await response.json();
    return data.token || readCookie('XSRF-TOKEN');
}

async function authenticateAdmin(email, senha) {
    const csrfToken = await getCsrfToken();
    const response = await fetch('/api/auth/login', {
        method: 'POST',
        credentials: 'same-origin',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-XSRF-TOKEN': csrfToken,
        },
        body: new URLSearchParams({ email, senha }),
    });

    if (!response.ok) {
        let message = 'Não foi possível autenticar. Verifique o e-mail e a palavra-passe.';
        try {
            const data = await response.json();
            if (data.message) message = data.message;
        } catch (error) {
            // Mantém uma mensagem amigável quando o servidor não retorna JSON.
        }
        throw new Error(message);
    }
}

async function logoutAdmin() {
    const csrfToken = await getCsrfToken();
    const response = await fetch('/api/auth/logout', {
        method: 'POST',
        credentials: 'same-origin',
        headers: { 'X-XSRF-TOKEN': csrfToken },
    });
    if (!response.ok) throw new Error('logout');
}

function showAdminApiError(message) {
    if (adminOrdersMessage) {
        adminOrdersMessage.textContent = message;
        adminOrdersMessage.className = 'admin-orders-message error';
    }
}

function renderAdminSummary() {
    const counts = adminOrders.reduce((summary, order) => {
        summary.total += 1;
        if (order.status === ORDER_STATUSES[0]) summary.received += 1;
        if (order.status === ORDER_STATUSES[1]) summary.progress += 1;
        if (order.status === ORDER_STATUSES[2]) summary.finished += 1;
        return summary;
    }, { total: 0, received: 0, progress: 0, finished: 0 });

    if (adminSummaryTotal) adminSummaryTotal.textContent = counts.total;
    if (adminSummaryReceived) adminSummaryReceived.textContent = counts.received;
    if (adminSummaryProgress) adminSummaryProgress.textContent = counts.progress;
    if (adminSummaryFinished) adminSummaryFinished.textContent = counts.finished;
}

function handleAdminApiAuthorization(status) {
    if (status !== 401 && status !== 403) return false;
    adminOrders = [];
    renderAdminSummary();
    setInterfaceMode('admin-login');
    showAdminLoginMessage(
        status === 401
            ? 'Sua sessão expirou. Entre novamente para acessar os pedidos.'
            : 'Seu acesso não está autorizado para consultar os pedidos.'
    );
    return true;
}

async function readAdminApiError(response, fallback) {
    try {
        const data = await response.json();
        return data.message || data.detail || fallback;
    } catch (error) {
        return fallback;
    }
}

function normalizeAdminOrder(order) {
    return {
        id: order.id,
        code: order.codigo,
        status: order.status,
        createdAt: order.dataCriacao,
        customer: order.nomeCliente,
        phone: order.telefone,
        cep: order.cep || '',
        email: order.email,
        address: order.endereco,
        notes: order.observacoes,
        items: Array.isArray(order.itens)
            ? order.itens.map((item) => ({ ...normalizeOrderItem(item), id: item.id }))
            : [],
        subtotal: Number(order.subtotal ?? order.total) || 0,
        freight: Number(order.frete) || 0,
        total: Number(order.total) || 0,
        approximateFreight: order.calculoAproximado === true,
    };
}

function getAdminOrderFilters() {
    const search = String(adminOrderSearch?.value || '').trim();
    const params = new URLSearchParams();
    const normalizedSearch = normalizePhone(search);

    if (search.toUpperCase().startsWith('FP-')) {
        params.set('codigo', search);
    } else if (normalizedSearch.length >= 8 && normalizedSearch.length === search.replace(/\D/g, '').length) {
        params.set('telefone', search);
    } else if (search) {
        params.set('nome', search);
    }

    const status = adminOrderStatus?.value || 'all';
    if (status !== 'all') params.set('status', status);
    return params;
}

async function loadAdminOrders() {
    if (!adminOrderList) return;
    const requestId = ++adminOrdersRequestId;
    if (adminOrdersMessage) {
        adminOrdersMessage.textContent = 'Carregando pedidos...';
        adminOrdersMessage.className = 'admin-orders-message';
    }
    adminOrdersEmpty.hidden = true;

    try {
        const query = getAdminOrderFilters().toString();
        const response = await fetch(`/api/admin/pedidos${query ? `?${query}` : ''}`, {
            credentials: 'same-origin',
        });
        if (handleAdminApiAuthorization(response.status)) return;
        if (!response.ok) {
            throw new Error(await readAdminApiError(response, 'Não foi possível carregar os pedidos.'));
        }
        const data = await response.json();
        if (requestId !== adminOrdersRequestId) return;
        adminOrders = Array.isArray(data) ? data.map(normalizeAdminOrder) : [];
        renderAdminSummary();
        renderAdminOrders();
    } catch (error) {
        if (requestId !== adminOrdersRequestId) return;
        adminOrders = [];
        renderAdminSummary();
        adminOrderList.innerHTML = '';
        adminOrdersEmpty.hidden = false;
        adminOrdersEmpty.querySelector('strong').textContent = 'Não foi possível carregar os pedidos';
        adminOrdersEmpty.querySelector('span').textContent = error.message || 'Tente novamente.';
        showAdminApiError(error.message || 'Não foi possível carregar os pedidos.');
    }
}

function sanitizeCart(items) {
    if (!Array.isArray(items)) return [];

    return items.reduce((accumulator, item) => {
        if (!item || typeof item !== 'object') return accumulator;

        const itemType = item.itemType === 'BEBIDA' ? 'BEBIDA' : 'PIZZA';
        const pizzaType = item.pizzaType === 'MEIO_A_MEIO' ? 'MEIO_A_MEIO' : 'INTEIRA';
        const firstFlavor = item.firstFlavor && typeof item.firstFlavor === 'object'
            ? { id: Number(item.firstFlavor.id), name: String(item.firstFlavor.name || '').trim() }
            : { id: Number(item.productId ?? item.id), name: String(item.name || '').trim() };
        const secondFlavor = item.secondFlavor && typeof item.secondFlavor === 'object'
            ? { id: Number(item.secondFlavor.id), name: String(item.secondFlavor.name || '').trim() }
            : null;
        const price = Number(item.price);
        const quantity = Math.trunc(Number(item.quantity));

        if (!firstFlavor.name || !Number.isFinite(price) || price <= 0 || !Number.isFinite(quantity) || quantity <= 0) {
            return accumulator;
        }
        if (itemType === 'PIZZA' && pizzaType === 'MEIO_A_MEIO'
            && (!Number.isInteger(secondFlavor?.id) || secondFlavor.id <= 0 || !secondFlavor.name)) return accumulator;

        const border = itemType === 'PIZZA' && item.border && Number.isInteger(Number(item.border.id)) && String(item.border.name || '').trim()
            ? { id: Number(item.border.id), name: String(item.border.name).trim(), price: Math.max(0, Number(item.border.price) || 0) }
            : null;
        const normalizeOptions = (options) => Array.isArray(options) ? options
            .filter((addon) => addon && Number.isInteger(Number(addon.id)) && String(addon.name || '').trim())
            .map((addon) => ({
                id: Number(addon.id),
                name: String(addon.name).trim(),
                price: Math.max(0, Number(addon.price) || 0),
            })) : [];
        const addons = itemType === 'PIZZA' ? normalizeOptions(item.addons) : [];
        const sauces = itemType === 'PIZZA' ? normalizeOptions(item.sauces) : [];
        const productId = Number(item.productId ?? firstFlavor.id);
        const normalized = {
            itemType,
            name: itemType === 'BEBIDA'
                ? String(item.name || firstFlavor.name).trim()
                : pizzaType === 'MEIO_A_MEIO' ? 'Pizza meio a meio' : 'Pizza inteira',
            price,
            quantity,
            productId: Number.isInteger(productId) && productId > 0 ? productId : null,
            pizzaType: itemType === 'BEBIDA' ? 'INTEIRA' : pizzaType,
            firstFlavor,
            secondFlavor: itemType === 'PIZZA' && pizzaType === 'MEIO_A_MEIO' ? secondFlavor : null,
            border,
            addons,
            sauces,
        };
        normalized.key = getCartItemKey(normalized);

        const existing = accumulator.find((entry) => entry.key === normalized.key);
        if (existing) {
            existing.quantity += quantity;
            return accumulator;
        }

        accumulator.push(normalized);
        return accumulator;
    }, []);
}

function getCartItemKey(item) {
    return [
        item.itemType || 'PIZZA',
        item.pizzaType,
        item.firstFlavor?.id,
        item.secondFlavor?.id || '',
        item.border?.id || '',
        ...(item.addons || []).map((addon) => addon.id).sort((first, second) => first - second),
        ...(item.sauces || []).map((sauce) => sauce.id).sort((first, second) => first - second),
    ].join(':');
}

function addCartItem(item) {
    item.key = getCartItemKey(item);
    const existing = window.cart.find((entry) => entry.key === item.key);
    if (existing) existing.quantity = Math.min(50, existing.quantity + item.quantity);
    else window.cart.push(item);
    updateCart();
    openCart();
}

function loadCart() {
    try {
        const savedCart = localStorage.getItem(CART_STORAGE_KEY);
        if (!savedCart) return [];
        return sanitizeCart(JSON.parse(savedCart));
    } catch (error) {
        console.warn('Carrinho inválido no localStorage. Iniciando vazio.', error);
        return [];
    }
}

function saveCart() {
    try {
        localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(window.cart));
    } catch (error) {
        console.warn('Não foi possível salvar o carrinho no localStorage.', error);
    }
}

window.cart = loadCart();

function getProductCategoryLabel(category) {
    return { classicas: 'Clássicas', carnes: 'Carnes', frango: 'Frango', queijos: 'Queijos' }[category] || category;
}

function normalizeApiProduct(product, categories) {
    if (!product || typeof product !== 'object') return null;
    const price = Number(product.preco);
    if (!Number.isInteger(Number(product.id)) || !product.nome || !Number.isFinite(price) || price <= 0) return null;
    const category = categories.find((item) => String(item.id) === String(product.categoriaId));
    return {
        id: Number(product.id),
        name: String(product.nome).trim(),
        type: ['SALGADA', 'DOCE', 'BEBIDA'].includes(product.tipo) ? product.tipo : 'SALGADA',
        description: String(product.descricao || '').trim(),
        categoryId: product.categoriaId == null ? '' : String(product.categoriaId),
        categoryName: category?.name || String(product.categoriaNome || '').trim(),
        price,
        image: String(product.imagem || '').trim(),
        active: product.ativo !== false,
    };
}

async function fetchApiJson(url, options = {}) {
    const response = await fetch(url, { credentials: 'same-origin', ...options });
    const data = await response.json().catch(() => null);
    if (!response.ok) {
        const error = new Error(data?.message || 'Não foi possível comunicar com a API.');
        error.status = response.status;
        throw error;
    }
    return data;
}

async function loadPublicCatalog() {
    try {
        const [products, categories] = await Promise.all([
            fetchApiJson('/api/produtos'),
            fetchApiJson('/api/categorias'),
        ]);
        if (!Array.isArray(products) || !Array.isArray(categories)) throw new Error('Resposta inválida do catálogo.');
        publicCategories = categories
            .filter((category) => category && category.id != null && category.nome)
            .map((category) => ({ id: category.id, name: String(category.nome).trim() }));
        publicProducts = products.map((product) => normalizeApiProduct(product, publicCategories)).filter(Boolean);
        renderCategoryFilters(publicCategories, 'public');
        renderPublicCatalog();
    } catch (error) {
        publicProducts = [];
        publicCategories = [];
        renderCategoryFilters([], 'public');
        renderPublicCatalog();
        showCatalogMessage(error.message || 'Não foi possível carregar o cardápio.');
    }
}

function renderCategoryFilters(categories, mode) {
    if (!publicMenuFilters || mode !== 'public') return;
    publicMenuFilters.innerHTML = [
        '<button class="menu-filter-button is-active" type="button" data-filter="all" aria-pressed="true">Todos</button>',
        ...categories.map((category) => `<button class="menu-filter-button" type="button" data-filter="${escapeHtml(category.id)}" aria-pressed="false">${escapeHtml(category.name)}</button>`),
    ].join('');
}

function showCatalogMessage(message) {
    const menuList = document.querySelector('.menu-list');
    if (menuList && !publicProducts.length) menuList.innerHTML = `<div class="catalog-message">${escapeHtml(message)}</div>`;
}

function renderPublicCatalog() {
    const pizzaGrid = document.querySelector('.pizza-grid');
    const menuList = document.querySelector('.menu-list');
    const products = publicProducts.filter((product) => product.active);
    const featuredNames = ['Calabresa Prime', 'Havaiana de Frango', 'Costela com Catupiry'];
    const featuredProducts = featuredNames
        .map((name) => products.find((product) => product.name === name))
        .filter(Boolean);
    const menuProducts = products.filter((product) => !featuredNames.includes(product.name));
    if (pizzaGrid) {
        pizzaGrid.innerHTML = featuredProducts.map((product) => `
            <article class="pizza-card" data-product="${escapeHtml(product.name)}" data-product-id="${product.id}" data-price="${product.price}">
                <div class="pizza-image">
                    ${product.image ? `<img class="pizza-zoom-menor" src="${escapeHtml(product.image)}" alt="Pizza ${escapeHtml(product.name)}">` : '<div class="pizza-image-placeholder" aria-hidden="true">FP</div>'}
                </div>
                <div class="pizza-info"><div><h3>${escapeHtml(product.name)}</h3><p>${escapeHtml(product.description)}</p></div><strong>${money.format(product.price)}</strong></div>
                <button class="btn-card" type="button" data-product-id="${product.id}">ADICIONAR</button>
            </article>`).join('');
    }
    if (menuList) {
        menuList.innerHTML = menuProducts.map((product) => `
            <div class="menu-item" data-category="${escapeHtml(product.categoryId)}">
                <div class="menu-text"><h3>${escapeHtml(product.name)}</h3><p>${escapeHtml(product.description)}</p></div>
                <div class="menu-actions"><strong>${money.format(product.price)}</strong><button class="btn-menu-add" type="button" data-product="${escapeHtml(product.name)}" data-product-id="${product.id}" data-price="${product.price}">ADICIONAR</button></div>
            </div>`).join('');
    }
}

function openCart() {
    if (!panel || !backdrop) return;
    panel.classList.add('is-open');
    backdrop.classList.add('is-open');
    panel.setAttribute('aria-hidden', 'false');
}

function closeCart() {
    if (!panel || !backdrop) return;
    panel.classList.remove('is-open');
    backdrop.classList.remove('is-open');
    panel.setAttribute('aria-hidden', 'true');
}

function closeCheckout() {
    if (!checkoutPanel) return;
    checkoutPanel.classList.remove('is-open');
    checkoutPanel.setAttribute('aria-hidden', 'true');
    if (checkoutMessage) {
        checkoutMessage.textContent = '';
        checkoutMessage.classList.remove('success');
        checkoutMessage.classList.remove('error');
    }
}

function closeConfirmation() {
    if (!confirmationPanel) return;
    confirmationPanel.classList.remove('is-open');
    confirmationPanel.setAttribute('aria-hidden', 'true');
    if (backdrop) {
        backdrop.classList.remove('is-open');
    }
}

function saveRecentOrder(orderData) {
    const normalizedOrder = {
        ...orderData,
        status: orderData.status,
        createdAt: new Date().toISOString(),
    };

    try {
        localStorage.setItem(ORDER_STORAGE_KEY, JSON.stringify(normalizedOrder));
    } catch (error) {
        console.warn('Não foi possível salvar o último pedido no localStorage.', error);
    }

    try {
        const history = readOrderHistory();
        const existingIndex = history.findIndex((item) => item.code === normalizedOrder.code);
        if (existingIndex >= 0) {
            history[existingIndex] = normalizedOrder;
        } else {
            history.push(normalizedOrder);
        }
        localStorage.setItem(ORDER_HISTORY_STORAGE_KEY, JSON.stringify(history));
    } catch (error) {
        console.warn('Não foi possível salvar o histórico de pedidos no localStorage.', error);
    }
}

function readOrderHistory() {
    try {
        const stored = localStorage.getItem(ORDER_HISTORY_STORAGE_KEY);
        if (!stored) {
            const recentOrder = localStorage.getItem(ORDER_STORAGE_KEY);
            if (!recentOrder) return [];
            return [JSON.parse(recentOrder)];
        }
        const parsed = JSON.parse(stored);
        return Array.isArray(parsed) ? parsed : [];
    } catch (error) {
        console.warn('Não foi possível ler o histórico de pedidos.', error);
        return [];
    }
}

function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function normalizeOrderItem(item) {
    const price = Number(item?.precoUnitario);
    const quantity = Number(item?.quantidade);
    const pizzaType = item?.tipoPizza === 'MEIO_A_MEIO' ? 'MEIO_A_MEIO' : 'INTEIRA';
    const itemType = item?.tipoProduto === 'BEBIDA' ? 'BEBIDA' : 'PIZZA';
    const productName = String(item?.nomeProduto || 'Produto não informado');
    const addons = Array.isArray(item?.adicionais) ? item.adicionais.map((addon) => ({
        id: addon.opcaoId,
        name: String(addon.nome || '').trim(),
        price: Number(addon.precoAdicional) || 0,
    })) : [];
    const sauces = Array.isArray(item?.molhos) ? item.molhos.map((sauce) => ({
        id: sauce.opcaoId,
        name: String(sauce.nome || '').trim(),
        price: Number(sauce.precoAdicional) || 0,
    })) : [];
    return {
        itemType,
        productId: item?.produtoId,
        name: itemType === 'BEBIDA' ? productName : pizzaType === 'MEIO_A_MEIO' ? 'Pizza meio a meio' : 'Pizza inteira',
        quantity,
        price,
        subtotal: Number(item?.subtotal) || price * quantity,
        pizzaType,
        firstFlavor: { id: item?.produtoId, name: itemType === 'BEBIDA' ? productName : String(item?.nomeProduto || 'Sabor não informado') },
        secondFlavor: item?.nomeSegundoProduto
            ? { id: item.segundoProdutoId, name: String(item.nomeSegundoProduto) }
            : null,
        border: item?.borda ? { id: item.bordaId, name: String(item.borda), price: Number(item.precoBorda) || 0 } : null,
        addons,
        sauces,
    };
}

function getPizzaPresentation(item) {
    if (item?.itemType === 'BEBIDA') {
        return { title: item.name || item.firstFlavor?.name || 'Bebida', flavors: 'Bebida', border: '', addons: '', sauces: '' };
    }
    const pizzaType = item?.pizzaType === 'MEIO_A_MEIO' ? 'MEIO_A_MEIO' : 'INTEIRA';
    const firstName = item?.firstFlavor?.name || item?.name || 'Sabor não informado';
    const secondName = item?.secondFlavor?.name;
    return {
        title: pizzaType === 'MEIO_A_MEIO' ? 'Pizza meio a meio' : 'Pizza inteira',
        flavors: pizzaType === 'MEIO_A_MEIO'
            ? `1/2 ${firstName} + 1/2 ${secondName || 'Escolha pendente'}`
            : `Sabor: ${firstName}`,
        border: item?.border?.name ? `Borda: ${item.border.name}` : 'Sem borda recheada',
        addons: item?.addons?.length
            ? `Adicionais: ${item.addons.map((addon) => addon.name).join(', ')}`
            : 'Sem adicionais',
        sauces: item?.sauces?.length
            ? `Molhos: ${item.sauces.map((sauce) => sauce.name).join(', ')}`
            : 'Sem molhos adicionais',
    };
}

function pizzaDetailsMarkup(item) {
    const presentation = getPizzaPresentation(item);
    const details = [presentation.flavors, presentation.border, presentation.addons, presentation.sauces]
        .filter(Boolean)
        .map((detail) => `<small>${escapeHtml(detail)}</small>`)
        .join('');
    return `<strong>${escapeHtml(presentation.title)}</strong>${details}`;
}

function pizzaWhatsAppDescription(item) {
    const presentation = getPizzaPresentation(item);
    return [presentation.title, presentation.flavors, presentation.border, presentation.addons, presentation.sauces]
        .filter(Boolean)
        .join(' | ');
}

function getOrderStatusClass(status) {
    return getOrderQueryStatusLabel(status);
}

function getOrderStatusMessage(status) {
    return {
        'Pedido recebido': 'Recebemos seu pedido e ele está aguardando o início do preparo.',
        'Pedido em andamento': 'Seu pedido está sendo preparado pela nossa equipe.',
        'Pedido concluído': 'Seu pedido foi concluído. Obrigado por pedir na Fatia Prime!',
    }[status] || '';
}

function formatOrderDate(value) {
    const date = new Date(value);
    if (!value || Number.isNaN(date.getTime())) return 'Data não disponível';
    return date.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
}

function showAdminStatusMessage(message, type = 'error') {
    if (!adminStatusMessage) return;
    adminStatusMessage.textContent = message;
    adminStatusMessage.className = `admin-status-message ${type}`.trim();
}

function normalizeAdminProduct(product, categories) {
    const normalized = normalizeApiProduct(product, categories);
    if (!normalized || product.id == null || !product.nome) return null;
    return { ...normalized, active: product.ativo !== false };
}

function normalizeAdminPizzaOption(option) {
    const price = Number(option?.precoAdicional);
    if (!option || !Number.isInteger(Number(option.id)) || !option.nome
        || !['BORDA', 'ADICIONAL', 'MOLHO'].includes(option.tipo)
        || !['SALGADA', 'DOCE'].includes(option.tipoProduto)
        || !Number.isFinite(price) || price < 0) return null;
    return {
        id: Number(option.id),
        name: String(option.nome).trim(),
        kind: option.tipo,
        productType: option.tipoProduto,
        price,
        active: option.ativo !== false,
    };
}

function getProductTypeLabel(type) {
    if (type === 'BEBIDA') return 'Bebida';
    return type === 'DOCE' ? 'Doce' : 'Salgada';
}

function renderAdminPizzaOptions() {
    if (!adminPizzaOptionList || !adminPizzaOptionsEmpty) return;
    adminPizzaOptionList.innerHTML = adminPizzaOptions.map((option) => `
        <article class="admin-pizza-option ${option.active ? '' : 'is-inactive'}">
            <div>
                <strong>${escapeHtml(option.name)}</strong>
                <small>${option.kind === 'BORDA' ? 'Borda' : option.kind === 'MOLHO' ? 'Molho' : 'Adicional'} · ${option.productType === 'DOCE' ? 'Doces' : 'Salgadas'} · ${money.format(option.price)}</small>
                <small>${option.active ? 'Disponível' : 'Desativada'}</small>
            </div>
            <div class="admin-pizza-option-actions">
                <button class="btn-secondary" type="button" data-edit-pizza-option="${option.id}">EDITAR</button>
                <button class="btn-secondary" type="button" data-toggle-pizza-option="${option.id}">${option.active ? 'DESATIVAR' : 'REATIVAR'}</button>
            </div>
        </article>`).join('');
    adminPizzaOptionsEmpty.hidden = adminPizzaOptions.length > 0;
}

function renderAdminProducts() {
    if (!adminCatalogList || !adminCatalogEmpty) return;
    adminCatalogList.innerHTML = adminProducts.map((product) => `
        <article class="admin-product-card ${product.active ? '' : 'is-inactive'}">
            <div class="admin-product-card-info">
                <div class="admin-product-card-title">
                    <strong>${escapeHtml(product.name)}</strong>
                    <span class="admin-product-state ${product.active ? 'is-active' : 'is-inactive'}">${product.active ? 'Ativa' : 'Desativada'}</span>
                </div>
                <span>${escapeHtml(product.categoryName || 'Sem categoria')} · ${getProductTypeLabel(product.type)} · ${money.format(product.price)}</span>
                <small>${escapeHtml(product.description || 'Sem descrição')}</small>
            </div>
            <div class="admin-product-card-actions">
                <button class="btn-secondary" type="button" data-edit-product="${escapeHtml(product.id)}">EDITAR</button>
                <button class="${product.active ? 'btn-danger' : 'btn-secondary'}" type="button" data-toggle-product="${escapeHtml(product.id)}">${product.active ? 'DESATIVAR' : 'REATIVAR'}</button>
            </div>
        </article>`).join('');
    adminCatalogEmpty.hidden = adminProducts.length > 0;
}

async function loadAdminProducts() {
    if (!adminCatalogList) return;
    const requestId = ++adminCatalogRequestId;
    try {
        const [products, categories, options] = await Promise.all([
            fetchApiJson('/api/admin/produtos'),
            fetchApiJson('/api/categorias'),
            fetchApiJson('/api/admin/opcoes-pizza'),
        ]);
        if (!Array.isArray(products) || !Array.isArray(categories) || !Array.isArray(options)) throw new Error('Resposta inválida do catálogo.');
        adminCategories = categories
            .filter((category) => category && category.id != null && category.nome)
            .map((category) => ({ id: category.id, name: String(category.nome).trim() }));
        adminProducts = products.map((product) => normalizeAdminProduct(product, adminCategories)).filter(Boolean);
        adminPizzaOptions = options.map(normalizeAdminPizzaOption).filter(Boolean);
        if (requestId !== adminCatalogRequestId) return;
        const categoryField = adminProductForm?.elements.namedItem('productCategory');
        if (categoryField) {
            categoryField.innerHTML = '<option value="">Selecione uma categoria</option>'
                + adminCategories.map((category) => `<option value="${escapeHtml(category.id)}">${escapeHtml(category.name)}</option>`).join('');
        }
        renderAdminProducts();
        renderAdminPizzaOptions();
    } catch (error) {
        if (requestId !== adminCatalogRequestId) return;
        adminProducts = [];
        adminPizzaOptions = [];
        renderAdminProducts();
        renderAdminPizzaOptions();
        showAdminProductMessage(error.message || 'Não foi possível carregar o catálogo.');
    }
}

function showAdminPizzaOptionMessage(message, type = 'error') {
    if (!adminPizzaOptionMessage) return;
    adminPizzaOptionMessage.textContent = message;
    adminPizzaOptionMessage.className = `admin-pizza-option-message ${type}`.trim();
}

function resetAdminPizzaOptionForm() {
    if (!adminPizzaOptionForm) return;
    adminPizzaOptionForm.reset();
    editingOptionId = null;
    adminPizzaOptionFormTitle.textContent = 'Adicionar opção';
    adminPizzaOptionCancel.hidden = true;
    showAdminPizzaOptionMessage('');
}

function editAdminPizzaOption(option) {
    if (!adminPizzaOptionForm || !option) return;
    editingOptionId = option.id;
    adminPizzaOptionForm.elements.namedItem('optionName').value = option.name;
    adminPizzaOptionForm.elements.namedItem('optionKind').value = option.kind;
    adminPizzaOptionForm.elements.namedItem('optionProductType').value = option.productType;
    adminPizzaOptionForm.elements.namedItem('optionPrice').value = option.price;
    adminPizzaOptionFormTitle.textContent = 'Editar opção';
    adminPizzaOptionCancel.hidden = false;
    showAdminPizzaOptionMessage('');
    adminPizzaOptionForm.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

async function toggleAdminPizzaOption(optionIdValue) {
    const option = adminPizzaOptions.find((item) => String(item.id) === String(optionIdValue));
    if (!option) return;
    if (option.active && !window.confirm(`Deseja desativar "${option.name}"?`)) return;
    try {
        const csrfToken = await getCsrfToken();
        const updated = await fetchApiJson(`/api/admin/opcoes-pizza/${encodeURIComponent(option.id)}/status`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': csrfToken },
            body: JSON.stringify({ status: option.active ? 'inativo' : 'ativo' }),
        });
        const normalized = normalizeAdminPizzaOption(updated);
        adminPizzaOptions = adminPizzaOptions.map((item) => item.id === normalized.id ? normalized : item);
        renderAdminPizzaOptions();
        showAdminPizzaOptionMessage(normalized.active ? 'Opção reativada.' : 'Opção desativada.', 'success');
    } catch (error) {
        showAdminPizzaOptionMessage(error.message || 'Não foi possível atualizar a opção.');
    }
}

async function submitAdminPizzaOption(event) {
    event.preventDefault();
    if (!adminPizzaOptionForm) return;
    const formData = new FormData(adminPizzaOptionForm);
    const name = String(formData.get('optionName') || '').trim();
    const price = Number(formData.get('optionPrice'));
    if (!name || !Number.isFinite(price) || price < 0) {
        showAdminPizzaOptionMessage('Informe o nome e um acréscimo igual ou maior que zero.');
        return;
    }

    const existing = adminPizzaOptions.find((option) => option.id === editingOptionId);
    const payload = {
        nome: name,
        tipo: String(formData.get('optionKind')),
        tipoProduto: String(formData.get('optionProductType')),
        precoAdicional: price,
        ativo: existing ? existing.active : true,
    };
    const url = editingOptionId
        ? `/api/admin/opcoes-pizza/${encodeURIComponent(editingOptionId)}`
        : '/api/admin/opcoes-pizza';
    try {
        const csrfToken = await getCsrfToken();
        const saved = await fetchApiJson(url, {
            method: editingOptionId ? 'PUT' : 'POST',
            headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': csrfToken },
            body: JSON.stringify(payload),
        });
        const normalized = normalizeAdminPizzaOption(saved);
        adminPizzaOptions = editingOptionId
            ? adminPizzaOptions.map((option) => option.id === normalized.id ? normalized : option)
            : [...adminPizzaOptions, normalized];
        renderAdminPizzaOptions();
        resetAdminPizzaOptionForm();
        showAdminPizzaOptionMessage('Opção salva com sucesso.', 'success');
    } catch (error) {
        showAdminPizzaOptionMessage(error.message || 'Não foi possível salvar a opção.');
    }
}

function showAdminProductMessage(message, type = 'error') {
    if (!adminProductFormMessage) return;
    adminProductFormMessage.textContent = message;
    adminProductFormMessage.className = `admin-product-form-message ${type}`.trim();
}

function resetAdminProductForm() {
    if (!adminProductForm) return;
    adminProductForm.reset();
    editingProductId = null;
    if (adminProductFormTitle) adminProductFormTitle.textContent = 'Adicionar ao cardápio';
    if (adminProductFormLabel) adminProductFormLabel.textContent = 'NOVO PRODUTO';
    if (adminProductCancel) adminProductCancel.hidden = true;
    showAdminProductMessage('');
}

function editAdminProduct(product) {
    if (!adminProductForm || !product) return;
    editingProductId = product.id;
    adminProductForm.elements.namedItem('productName').value = product.name;
    adminProductForm.elements.namedItem('productDescription').value = product.description;
    adminProductForm.elements.namedItem('productCategory').value = product.categoryId;
    adminProductForm.elements.namedItem('productType').value = product.type;
    adminProductForm.elements.namedItem('productPrice').value = product.price;
    adminProductForm.elements.namedItem('productImage').value = product.image;
    if (adminProductFormTitle) adminProductFormTitle.textContent = 'Editar produto';
    if (adminProductFormLabel) adminProductFormLabel.textContent = `EDIÇÃO DE ${getProductTypeLabel(product.type).toUpperCase()}`;
    if (adminProductCancel) adminProductCancel.hidden = false;
    showAdminProductMessage('');
    adminProductForm.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

async function toggleAdminProduct(productIdValue) {
    const product = adminProducts.find((item) => String(item.id) === String(productIdValue));
    if (!product) return;
    if (product.active && !window.confirm(`Deseja realmente desativar o produto "${product.name}"?`)) return;
    try {
        const csrfToken = await getCsrfToken();
        const updated = await fetchApiJson(`/api/admin/produtos/${encodeURIComponent(product.id)}/status`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': csrfToken },
            body: JSON.stringify({ status: product.active ? 'inativo' : 'ativo' }),
        });
        const normalized = normalizeAdminProduct(updated, adminCategories);
        adminProducts = adminProducts.map((item) => item.id === normalized.id ? normalized : item);
        renderAdminProducts();
        await loadPublicCatalog();
        showAdminProductMessage(normalized.active ? 'Produto reativado com sucesso.' : 'Produto desativado com sucesso.', 'success');
    } catch (error) {
        showAdminProductMessage(error.message || 'Não foi possível atualizar a disponibilidade da pizza.');
    }
}

async function submitAdminProduct(event) {
    event.preventDefault();
    if (!adminProductForm) return;
    const formData = new FormData(adminProductForm);
    const name = String(formData.get('productName') || '').trim();
    const description = String(formData.get('productDescription') || '').trim();
    const categoryId = String(formData.get('productCategory') || '').trim();
    const price = Number(formData.get('productPrice'));
    const image = String(formData.get('productImage') || '').trim();

    if (!name) {
        showAdminProductMessage('Informe o nome do produto.');
        adminProductForm.elements.namedItem('productName').focus();
        return;
    }
    if (!categoryId || !adminCategories.some((category) => String(category.id) === categoryId)) {
        showAdminProductMessage('Selecione uma categoria válida.');
        adminProductForm.elements.namedItem('productCategory').focus();
        return;
    }
    if (!Number.isFinite(price) || price <= 0) {
        showAdminProductMessage('Informe um preço maior que zero.');
        adminProductForm.elements.namedItem('productPrice').focus();
        return;
    }

    if (editingProductId && !adminProducts.some((product) => String(product.id) === String(editingProductId))) {
        showAdminProductMessage('O produto selecionado não foi encontrado.');
        return;
    }
    const payload = {
        nome: name,
        descricao: description,
        preco: price,
        imagem: image,
        categoriaId: Number(categoryId),
        tipo: String(formData.get('productType') || 'SALGADA'),
        ativo: editingProductId
            ? adminProducts.find((product) => String(product.id) === String(editingProductId)).active
            : true,
    };
    const url = editingProductId
        ? `/api/admin/produtos/${encodeURIComponent(editingProductId)}`
        : '/api/admin/produtos';
    const method = editingProductId ? 'PUT' : 'POST';
    try {
        const csrfToken = await getCsrfToken();
        const saved = await fetchApiJson(url, {
            method,
            headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': csrfToken },
            body: JSON.stringify(payload),
        });
        const normalized = normalizeAdminProduct(saved, adminCategories);
        adminProducts = editingProductId
            ? adminProducts.map((product) => product.id === normalized.id ? normalized : product)
            : [...adminProducts, normalized];
        renderAdminProducts();
        await loadPublicCatalog();
        showAdminProductMessage(editingProductId ? 'Produto atualizado com sucesso.' : 'Produto cadastrado com sucesso.', 'success');
        adminProductForm.reset();
        editingProductId = null;
        if (adminProductFormTitle) adminProductFormTitle.textContent = 'Adicionar ao cardápio';
        if (adminProductFormLabel) adminProductFormLabel.textContent = 'NOVA PIZZA';
        if (adminProductCancel) adminProductCancel.hidden = true;
    } catch (error) {
        showAdminProductMessage(error.message || 'Não foi possível salvar a pizza.');
    }
}

function renderAdminOrderDetails(order) {
    if (!adminOrderDetails) return;

    selectedAdminOrderId = order.id;

    const detailsCode = adminOrderDetails.querySelector('.admin-order-details-code');
    const detailsStatus = adminOrderDetails.querySelector('.admin-order-details-status');
    const customerElement = adminOrderDetails.querySelector('.admin-order-customer');
    const productList = adminOrderDetails.querySelector('.admin-order-product-list');
    const totalElement = adminOrderDetails.querySelector('.admin-order-total strong');
    const customerFields = [
        ['Nome', order.customer || 'Não informado'],
        ['Telefone', order.phone ? formatPhone(order.phone) : 'Não informado'],
        ['CEP', order.cep ? formatCep(order.cep) : 'Não informado'],
        ['E-mail', order.email || 'Não informado'],
        ['Endereço', order.address || 'Não informado'],
        ['Observações', order.notes || 'Não informado'],
    ];

    if (detailsCode) detailsCode.textContent = `Pedido ${order.code}`;
    if (detailsStatus) {
        detailsStatus.innerHTML = `<span class="admin-order-status ${getOrderStatusClass(order.status)}">${escapeHtml(order.status)}</span><span>${escapeHtml(formatOrderDate(order.createdAt))}</span>`;
    }
    if (adminOrderStatusEditor) adminOrderStatusEditor.value = ORDER_STATUSES.includes(order.status) ? order.status : ORDER_STATUSES[0];
    showAdminStatusMessage('');
    if (customerElement) {
        customerElement.innerHTML = customerFields.length
            ? customerFields.map(([label, value]) => `<div><strong>${escapeHtml(label)}</strong><span>${escapeHtml(value)}</span></div>`).join('')
            : '<p>Dados do cliente não informados.</p>';
    }

    const items = Array.isArray(order.items) ? order.items : [];
    if (productList) {
        productList.innerHTML = items.length
            ? items.map((item) => `
                <li>
                    <div>${pizzaDetailsMarkup(item)}<small>${Number(item.quantity)}x ${money.format(Number(item.price))} cada</small></div>
                    <strong>${money.format(Number(item.subtotal) || Number(item.price) * Number(item.quantity))}</strong>
                </li>`).join('')
            : '<li><span>Itens não informados.</span></li>';
    }
    if (adminOrderSubtotalElement) adminOrderSubtotalElement.textContent = money.format(Number(order.subtotal) || 0);
    if (adminOrderFreightElement) {
        adminOrderFreightElement.textContent = `${money.format(Number(order.freight) || 0)}${order.approximateFreight ? ' (estimado)' : ''}`;
    }
    if (totalElement) totalElement.textContent = money.format(Number(order.total) || 0);
    adminOrderDetails.hidden = false;
    adminOrderDetails.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

function renderAdminOrders() {
    if (!adminOrderList || !adminOrdersEmpty) return;

    adminOrderList.innerHTML = adminOrders.map((order) => {
        const itemCount = Array.isArray(order.items)
            ? order.items.reduce((sum, item) => sum + Number(item.quantity || 0), 0)
            : 0;
        return `
            <article class="admin-order-card">
                <div class="admin-order-card-main">
                    <div class="admin-order-card-title">
                        <strong>${escapeHtml(order.code)}</strong>
                        <span class="admin-order-status ${getOrderStatusClass(order.status)}">${escapeHtml(order.status)}</span>
                    </div>
                    <div class="admin-order-card-meta">
                        <span>${escapeHtml(order.customer || 'Cliente não informado')}</span>
                        <span>${escapeHtml(order.phone ? formatPhone(order.phone) : 'Telefone não informado')}</span>
                        <span>${itemCount} ${itemCount === 1 ? 'item' : 'itens'}</span>
                        <span>${escapeHtml(formatOrderDate(order.createdAt))}</span>
                    </div>
                </div>
                <div class="admin-order-card-side">
                    <strong>${money.format(Number(order.total) || 0)}</strong>
                    <button class="btn-secondary admin-order-details-button" type="button" data-order-id="${escapeHtml(order.id)}">VER DETALHES</button>
                </div>
            </article>`;
    }).join('');

    const noOrders = adminOrders.length === 0;
    adminOrdersEmpty.hidden = !noOrders;
    if (noOrders) {
        adminOrdersEmpty.querySelector('strong').textContent = 'Nenhum pedido disponível';
        adminOrdersEmpty.querySelector('span').textContent = 'Os pedidos disponíveis no sistema aparecerão aqui.';
    }
    if (adminOrdersMessage) {
        adminOrdersMessage.textContent = noOrders ? '' : `${adminOrders.length} ${adminOrders.length === 1 ? 'pedido encontrado' : 'pedidos encontrados'}.`;
        adminOrdersMessage.className = 'admin-orders-message';
    }
}

function renderConfirmation(orderData) {
    if (!confirmationPanel || !confirmationSummaryElement || !confirmationCodeElement || !confirmationStatusElement || !confirmationTotalElement || !confirmationCustomerElement) {
        return;
    }

    const data = orderData || JSON.parse(localStorage.getItem(ORDER_STORAGE_KEY) || 'null');
    if (!data) return;

    confirmationCodeElement.textContent = `Pedido ${data.code}`;
    confirmationStatusElement.textContent = data.status;
    confirmationStatusElement.className = `confirmation-status ${getOrderQueryStatusLabel(data.status)}`;
    confirmationSummaryElement.innerHTML = data.items.map((item) => `
        <li>
            <div>
                ${pizzaDetailsMarkup(item)}
                <small>${item.quantity}x · ${money.format(Number(item.price))} cada</small>
            </div>
            <strong>${money.format(Number(item.subtotal) || Number(item.price) * Number(item.quantity))}</strong>
        </li>`).join('');
    if (confirmationSubtotalElement) confirmationSubtotalElement.textContent = money.format(Number(data.subtotal ?? data.total) || 0);
    if (confirmationFreightElement) {
        confirmationFreightElement.textContent = `${money.format(Number(data.freight) || 0)}${data.approximateFreight ? ' (estimado)' : ''}`;
    }
    confirmationTotalElement.textContent = money.format(Number(data.total));

    const customerInfo = [
        data.customer ? `Nome: ${data.customer}` : null,
        data.phone ? `Telefone: ${data.phone}` : null,
        data.cep ? `CEP: ${formatCep(data.cep)}` : null,
        data.address ? `Endereço: ${data.address}` : null,
    ].filter(Boolean).join(' • ');

    confirmationCustomerElement.innerHTML = customerInfo
        ? `<strong>Dados do cliente</strong><span>${escapeHtml(customerInfo)}</span>`
        : '<strong>Dados do cliente</strong><span>Não informado.</span>';
}

function openConfirmation(orderData) {
    if (!confirmationPanel) return;
    if (confirmationWhatsappLink) confirmationWhatsappLink.hidden = true;
    renderConfirmation(orderData);
    confirmationPanel.classList.add('is-open');
    confirmationPanel.setAttribute('aria-hidden', 'false');
    if (backdrop) {
        backdrop.classList.add('is-open');
    }
    if (checkoutPanel) {
        checkoutPanel.classList.remove('is-open');
        checkoutPanel.setAttribute('aria-hidden', 'true');
    }
}

function openOrderTracking(orderData) {
    closeConfirmation();
    if (orderQueryCode) orderQueryCode.value = orderData.code || '';
    if (orderQueryPhone) orderQueryPhone.value = orderData.phone ? formatPhone(orderData.phone) : '';
    document.querySelector('#consultar-pedido')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function renderCheckoutSummary() {
    if (!checkoutItemsElement || !checkoutTotalElement) return;

    checkoutItemsElement.innerHTML = window.cart.length
        ? window.cart.map((item) => `
            <li>
                <div>
                    ${pizzaDetailsMarkup(item)}
                    <small>${item.quantity}x ${money.format(item.price)} cada</small>
                </div>
                <strong>${money.format(item.price * item.quantity)}</strong>
            </li>`).join('')
        : '<li><span>Seu carrinho está vazio.</span></li>';

    renderDeliveryBreakdown();
}

function normalizePhone(value) {
    return String(value || '').replace(/\D/g, '');
}

function formatPhone(value) {
    const digits = normalizePhone(value).slice(0, 11);
    if (!digits) return '';
    if (digits.length <= 2) return `(${digits}`;
    if (digits.length <= 6) return `(${digits.slice(0, 2)}) ${digits.slice(2)}`;
    if (digits.length <= 10) return `(${digits.slice(0, 2)}) ${digits.slice(2, 6)}-${digits.slice(6)}`;
    return `(${digits.slice(0, 2)}) ${digits.slice(2, 7)}-${digits.slice(7)}`;
}

function formatCep(value) {
    const digits = String(value || '').replace(/\D/g, '');
    if (digits.length <= 5) return digits;
    return `${digits.slice(0, 5)}-${digits.slice(5)}`;
}

function syncDeliveryCepFields(value) {
    const formatted = formatCep(value);
    if (cartDeliveryCep && cartDeliveryCep.value !== formatted) cartDeliveryCep.value = formatted;
    const checkoutCep = checkoutForm?.elements.namedItem('customerCep');
    if (checkoutCep && checkoutCep.value !== formatted) checkoutCep.value = formatted;
}

function setDeliveryQuoteMessage(form, message, type = '') {
    const element = form?.querySelector('.delivery-quote-message');
    if (!element) return;
    element.textContent = message;
    element.classList.remove('success', 'error');
    if (type) element.classList.add(type);
}

function deliveryQuoteMessage(quote) {
    if (quote.aproximado) {
        return `Frete estimado: ${money.format(quote.frete)}. Valor calculado com base aproximada na região do CEP.`;
    }
    return `Frete: ${money.format(quote.frete)} (${quote.regiao}).`;
}

function renderDeliveryBreakdown() {
    const subtotal = cartTotal();
    const freight = currentDeliveryQuote?.frete;
    const hasFreight = Number.isFinite(freight) && freight >= 0;
    const hasItems = Array.isArray(window.cart) && window.cart.length > 0;
    const awaitingQuote = hasItems && !hasFreight;
    const total = subtotal + (hasFreight ? freight : 0);

    if (cartSubtotalElement) cartSubtotalElement.textContent = money.format(subtotal);
    if (cartFreightElement) cartFreightElement.textContent = hasFreight
        ? money.format(freight)
        : 'Informe o CEP para calcular';
    if (cartTotalLabel) cartTotalLabel.textContent = awaitingQuote ? 'Total com entrega' : 'Total';
    if (totalElement) totalElement.textContent = awaitingQuote ? 'Informe o CEP' : money.format(total);

    if (checkoutSubtotalElement) checkoutSubtotalElement.textContent = money.format(subtotal);
    if (checkoutFreightElement) checkoutFreightElement.textContent = hasFreight
        ? money.format(freight)
        : 'Informe seu CEP';
    if (checkoutTotalLabel) checkoutTotalLabel.textContent = 'Total';
    if (checkoutTotalElement) checkoutTotalElement.textContent = hasFreight
        ? money.format(total)
        : 'Aguardando CEP';
}

async function refreshDeliveryQuote(cep, form, force = false) {
    const normalizedCep = String(cep || '').replace(/\D/g, '');
    if (!/^[0-9]{8}$/.test(normalizedCep)) {
        currentDeliveryQuote = null;
        deliveryQuoteRequestId += 1;
        renderDeliveryBreakdown();
        setDeliveryQuoteMessage(form, normalizedCep ? 'Informe um CEP válido.' : '');
        return false;
    }

    if (!force && currentDeliveryQuote?.cep === normalizedCep) {
        setDeliveryQuoteMessage(
            form,
            deliveryQuoteMessage(currentDeliveryQuote),
            'success'
        );
        return true;
    }

    const requestId = ++deliveryQuoteRequestId;
    currentDeliveryQuote = null;
    renderDeliveryBreakdown();
    setDeliveryQuoteMessage(form, 'Calculando entrega...');

    try {
        const quote = await fetchApiJson(`/api/frete/consulta?cep=${encodeURIComponent(normalizedCep)}`);
        if (requestId !== deliveryQuoteRequestId) return false;
        const freight = Number(quote?.valorFrete);
        if (!Number.isFinite(freight) || freight < 0 || !quote?.regiao) {
            throw new Error('Não foi possível calcular a entrega. Tente novamente.');
        }
        currentDeliveryQuote = {
            cep: normalizedCep,
            regiao: String(quote.regiao),
            frete: freight,
            aproximado: quote?.calculoAproximado === true,
        };
        renderDeliveryBreakdown();
        if (checkoutMessage?.classList.contains('error')) {
            checkoutMessage.textContent = '';
            checkoutMessage.classList.remove('error');
        }
        setDeliveryQuoteMessage(
            form,
            deliveryQuoteMessage(currentDeliveryQuote),
            'success'
        );
        return true;
    } catch (error) {
        if (requestId !== deliveryQuoteRequestId) return false;
        currentDeliveryQuote = null;
        renderDeliveryBreakdown();
        const mensagemForaDaArea = 'Ainda não entregamos nessa região. Em breve abriremos novas unidades mais perto de você!';
        const mensagem = error?.status === 404 && error.message === mensagemForaDaArea
            ? mensagemForaDaArea
            : error?.message === mensagemForaDaArea
                ? 'Não foi possível calcular a entrega. Tente novamente.'
                : error.message || 'Não foi possível calcular a entrega. Tente novamente.';
        setDeliveryQuoteMessage(
            form,
            mensagem,
            'error'
        );
        return false;
    }
}

function composeAddress(formData) {
    const cep = String(formData.get('customerCep') || '').trim();
    const street = String(formData.get('customerStreet') || '').trim();
    const number = String(formData.get('customerNumber') || '').trim();
    const complement = String(formData.get('customerComplement') || '').trim();
    const neighborhood = String(formData.get('customerNeighborhood') || '').trim();
    const city = String(formData.get('customerCity') || '').trim();
    const state = String(formData.get('customerState') || '').trim();
    const streetLine = [street, number].filter(Boolean).join(', ');
    const cityLine = [city, state].filter(Boolean).join(' - ');

    return [streetLine, complement, neighborhood, cityLine, cep && `CEP ${cep}`]
        .filter(Boolean)
        .join(' • ');
}

function isValidPhone(value) {
    const digits = normalizePhone(value);
    return digits.length === 10 || digits.length === 11;
}

function isValidEmail(value) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(value || '').trim());
}

function showAdminLoginMessage(message, type = 'error') {
    if (!adminLoginMessage) return;
    adminLoginMessage.textContent = message;
    adminLoginMessage.classList.toggle('success', type === 'success');
    adminLoginMessage.classList.toggle('error', type === 'error');
}

function clearAdminLoginState() {
    if (!adminLoginForm) return;
    adminLoginForm.querySelectorAll('.is-invalid').forEach((field) => field.classList.remove('is-invalid'));
    if (adminLoginMessage) {
        adminLoginMessage.textContent = '';
        adminLoginMessage.classList.remove('success', 'error');
    }
}

function validateAdminLogin() {
    if (!adminLoginForm) return false;

    const emailField = adminLoginForm.elements.namedItem('adminEmail');
    const passwordField = adminLoginForm.elements.namedItem('adminPassword');
    const email = String(emailField.value || '').trim();
    const password = String(passwordField.value || '');

    clearAdminLoginState();

    if (!email) {
        emailField.classList.add('is-invalid');
        showAdminLoginMessage('Informe o e-mail administrativo.');
        emailField.focus();
        return false;
    }

    if (!isValidEmail(email)) {
        emailField.classList.add('is-invalid');
        showAdminLoginMessage('Informe um e-mail válido.');
        emailField.focus();
        return false;
    }

    if (!password) {
        passwordField.classList.add('is-invalid');
        showAdminLoginMessage('Informe a palavra-passe.');
        passwordField.focus();
        return false;
    }

    return true;
}

function getOrderQueryStatusLabel(status) {
    const mapping = {
        'Pedido recebido': 'status-received',
        'Pedido em andamento': 'status-progress',
        'Pedido concluído': 'status-finished',
    };
    return mapping[status] || 'status-received';
}

function normalizePublicOrder(order) {
    if (!order || typeof order !== 'object' || !order.codigo || !ORDER_STATUSES.includes(order.status) || !Array.isArray(order.itens)) return null;
    const items = order.itens.map((item) => {
        const normalized = normalizeOrderItem(item);
        if (!Number.isFinite(normalized.price) || normalized.price <= 0
            || !Number.isFinite(normalized.quantity) || normalized.quantity <= 0
            || !normalized.firstFlavor.name) return null;
        if (normalized.pizzaType === 'MEIO_A_MEIO' && !normalized.secondFlavor?.name) return null;
        return normalized;
    });
    if (items.some((item) => !item)) return null;
    return {
        code: String(order.codigo),
        status: String(order.status),
        createdAt: order.dataCriacao,
        customer: order.clienteNome || '',
        email: order.clienteEmail || '',
        phone: order.clienteTelefone || '',
        cep: order.cep || '',
        address: order.endereco || '',
        notes: order.observacoes || '',
        items,
        subtotal: Number(order.subtotal ?? order.valorTotal),
        freight: Number(order.frete) || 0,
        total: Number(order.valorTotal),
        approximateFreight: order.calculoAproximado === true,
    };
}

async function queryOrdersByApi(code, phone) {
    const params = new URLSearchParams();
    if (code) params.set('codigo', code);
    else params.set('telefone', normalizePhone(phone));
    const data = await fetchApiJson(`/api/pedidos/consulta?${params.toString()}`);
    if (!Array.isArray(data)) throw new Error('A resposta da consulta está indisponível.');
    const orders = data.map(normalizePublicOrder);
    if (orders.some((order) => !order)) throw new Error('A resposta da consulta está indisponível.');
    return orders;
}

function showOrderQueryMessage(message) {
    if (!orderQueryMessage) return;
    orderQueryMessage.textContent = message;
    orderQueryMessage.classList.remove('success');
    orderQueryMessage.classList.add('error');
}

function renderOrderQueryResult(orderData) {
    if (!orderQueryResult || !orderQueryCode || !orderQueryPhone) return;

    orderQueryResult.hidden = false;
    const status = orderData.status;
    const list = Array.isArray(orderData.items) ? orderData.items : [];
    const total = Number(orderData.total) || 0;

    const codeValue = document.querySelector('.order-query-code-value');
    const phoneValue = document.querySelector('.order-query-phone-value');
    if (codeValue) {
        codeValue.textContent = `Pedido ${orderData.code || 'Não informado'}`;
    }
    if (phoneValue) {
        phoneValue.textContent = orderData.phone ? formatPhone(orderData.phone) : 'Não informado';
    }
    const statusBadge = document.querySelector('.order-query-status');
    if (statusBadge) {
        statusBadge.textContent = status;
        statusBadge.className = `order-query-status ${getOrderQueryStatusLabel(status)}`;
    }
    if (orderQueryStatusMessage) {
        orderQueryStatusMessage.textContent = getOrderStatusMessage(status);
    }
    const currentStep = ORDER_STATUSES.indexOf(status);
    document.querySelectorAll('[data-order-step]').forEach((step, index) => {
        step.classList.toggle('is-complete', index < currentStep);
        step.classList.toggle('is-current', index === currentStep);
        step.classList.toggle('is-upcoming', index > currentStep);
    });
    document.querySelectorAll('.order-progress-line').forEach((line, index) => {
        line.classList.toggle('is-complete', index < currentStep);
    });

    const resultItems = document.querySelector('.order-query-items');
    const resultTotal = document.querySelector('.order-query-total strong');
    if (orderQuerySubtotalElement) orderQuerySubtotalElement.textContent = money.format(Number(orderData.subtotal ?? total) || 0);
    if (orderQueryFreightElement) {
        orderQueryFreightElement.textContent = `${money.format(Number(orderData.freight) || 0)}${orderData.approximateFreight ? ' (estimado)' : ''}`;
    }
    if (resultItems) {
        resultItems.innerHTML = list.length
            ? list.map((item) => `
                <li>
                    <div>
                        ${pizzaDetailsMarkup(item)}
                        <small>${Number(item.quantity)}x · ${money.format(Number(item.price))} cada</small>
                    </div>
                    <strong>${money.format(Number(item.subtotal) || Number(item.price) * Number(item.quantity))}</strong>
                </li>`).join('')
            : '<li><span>Não há itens neste pedido.</span></li>';
    }

    if (resultTotal) {
        resultTotal.textContent = money.format(total);
    }
}

function cartItemsAreValid() {
    return Array.isArray(window.cart)
        && window.cart.length > 0
        && window.cart.every((item) => item
            && typeof item === 'object'
            && typeof item.name === 'string'
            && item.name.trim()
            && Number.isFinite(Number(item.price))
            && Number(item.price) > 0
            && Number.isFinite(Number(item.quantity))
            && Number.isInteger(Number(item.quantity))
            && Number(item.quantity) > 0
            && item.firstFlavor?.name
            && item.firstFlavor.id != null
            && (item.pizzaType !== 'MEIO_A_MEIO'
                || item.secondFlavor?.id != null && item.secondFlavor?.name)
            && (!item.border || Number.isInteger(Number(item.border.id)))
            && Array.isArray(item.addons)
            && item.addons.every((addon) => Number.isInteger(Number(addon.id)))
            && Array.isArray(item.sauces)
            && item.sauces.every((sauce) => Number.isInteger(Number(sauce.id))));
}

function cartItemsHaveProductIds() {
    return Array.isArray(window.cart)
        && window.cart.length > 0
        && window.cart.every((item) => Number.isInteger(Number(item.productId))
            && Number(item.productId) > 0
            && Number(item.firstFlavor?.id) === Number(item.productId)
            && (item.pizzaType !== 'MEIO_A_MEIO'
                || Number.isInteger(Number(item.secondFlavor?.id)) && Number(item.secondFlavor.id) > 0));
}

async function createOrderViaApi(payload) {
    const csrfToken = await getCsrfToken();
    return fetchApiJson('/api/pedidos', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': csrfToken },
        body: JSON.stringify(payload),
    });
}

function cartTotal() {
    if (!Array.isArray(window.cart) || !cartItemsAreValid()) {
        return 0;
    }

    return window.cart.reduce((total, item) => total + Number(item.price) * Number(item.quantity), 0);
}

function showCheckoutMessage(message, isSuccess = false) {
    if (!checkoutMessage) return;
    checkoutMessage.textContent = message;
    checkoutMessage.classList.toggle('success', isSuccess);
    checkoutMessage.classList.toggle('error', !isSuccess);
}

function clearCheckoutValidation() {
    if (!checkoutForm) return;

    checkoutForm.querySelectorAll('.is-invalid').forEach((field) => field.classList.remove('is-invalid'));
    checkoutForm.querySelectorAll('[aria-invalid="true"]').forEach((field) => field.setAttribute('aria-invalid', 'false'));
}

function markFieldInvalid(fieldName, message) {
    if (!checkoutForm) return;

    const field = checkoutForm.elements.namedItem(fieldName);
    if (!field) return;

    field.classList.add('is-invalid');
    field.setAttribute('aria-invalid', 'true');
    showCheckoutMessage(message, false);
    field.focus();
}

function openCheckout() {
    if (!Array.isArray(window.cart) || !window.cart.length || !cartItemsAreValid()) {
        const message = !Array.isArray(window.cart) || !window.cart.length ? 'Carrinho vazio.' : 'Itens do carrinho inválidos.';
        if (checkoutMessage) {
            showCheckoutMessage(message, false);
        } else {
            alert(message);
        }
        return;
    }

    const total = cartTotal();
    if (!Number.isFinite(total) || total < 0) {
        const message = 'Total do pedido inválido.';
        if (checkoutMessage) {
            showCheckoutMessage(message, false);
        } else {
            alert(message);
        }
        return;
    }

    if (!checkoutPanel) return;

    renderCheckoutSummary();
    const cartCustomerName = document.querySelector('#customer-name')?.value.trim();
    const cartOrderNotes = document.querySelector('#order-notes')?.value.trim();
    const checkoutName = checkoutForm?.elements.namedItem('customerName');
    const checkoutNotes = checkoutForm?.elements.namedItem('customerNotes');
    if (checkoutName && cartCustomerName && !checkoutName.value) checkoutName.value = cartCustomerName;
    if (checkoutNotes && cartOrderNotes && !checkoutNotes.value) checkoutNotes.value = cartOrderNotes;
    checkoutPanel.classList.add('is-open');
    if (backdrop) {
        backdrop.classList.add('is-open');
    }
    checkoutPanel.setAttribute('aria-hidden', 'false');
    if (panel) {
        panel.classList.remove('is-open');
        panel.setAttribute('aria-hidden', 'true');
    }
}

function updateCart() {
    const count = window.cart.reduce((total, item) => total + item.quantity, 0);
    document.querySelectorAll('.cart-count').forEach((element) => (element.textContent = count));
    itemsElement.innerHTML = window.cart.length
        ? window.cart.map((item, index) => `
            <div class="cart-item">
                <div class="cart-item-description">${pizzaDetailsMarkup(item)}<small>${item.quantity}x ${money.format(item.price)} cada</small></div>
                <strong class="cart-item-subtotal">${money.format(item.price * item.quantity)}</strong>
                <div class="cart-item-controls">
                    <button type="button" data-change="${index}" data-step="-1" aria-label="Remover uma unidade">−</button>
                    <span>${item.quantity}</span>
                    <button type="button" data-change="${index}" data-step="1" aria-label="Adicionar uma unidade">+</button>
                    <button type="button" class="cart-item-remove" data-remove-item="${index}" aria-label="Remover pizza do carrinho">×</button>
                </div>
            </div>`).join('')
        : '<p class="cart-empty">Seu carrinho está vazio.</p>';
    if (checkoutPanel && checkoutPanel.classList.contains('is-open')) {
        renderCheckoutSummary();
    }
    renderDeliveryBreakdown();
    saveCart();
}

async function addProduct(element) {
    const productId = Number(element.dataset.productId);
    const product = publicProducts.find((item) => item.id === productId);
    if (!product) return;

    if (catalogFeedback) {
        catalogFeedback.textContent = '';
        catalogFeedback.hidden = true;
    }

    if (product.type === 'BEBIDA') {
        addCartItem({
            itemType: 'BEBIDA',
            name: product.name,
            productId: product.id,
            price: product.price,
            quantity: 1,
            pizzaType: 'INTEIRA',
            firstFlavor: { id: product.id, name: product.name },
            secondFlavor: null,
            border: null,
            addons: [],
            sauces: [],
        });
        return;
    }
    if (!pizzaCustomizer) return;

    try {
        customizerOptions = await fetchApiJson(`/api/opcoes-pizza?tipoProduto=${encodeURIComponent(product.type)}`);
        if (!Array.isArray(customizerOptions)) throw new Error('Não foi possível carregar as opções da pizza.');
        const flavors = publicProducts.filter((item) => item.type === product.type && item.active);
        customizerFirstFlavor.innerHTML = flavors.map((flavor) =>
            `<option value="${escapeHtml(flavor.id)}">${escapeHtml(flavor.name)} · ${money.format(flavor.price)}</option>`).join('');
        customizerSecondFlavor.innerHTML = '<option value="">Selecione o segundo sabor</option>' + flavors
            .map((flavor) => `<option value="${escapeHtml(flavor.id)}">${escapeHtml(flavor.name)} · ${money.format(flavor.price)}</option>`).join('');
        customizerBorder.innerHTML = '<option value="">Sem borda recheada · R$ 0,00</option>'
            + customizerOptions.filter((option) => option.tipo === 'BORDA')
                .map((option) => `<option value="${escapeHtml(option.id)}">${escapeHtml(option.nome)} · + ${money.format(Number(option.precoAdicional))}</option>`).join('');
        syncCustomizerSelects();
        customizerAddonList.innerHTML = customizerOptions.filter((option) => option.tipo === 'ADICIONAL').length
            ? customizerOptions.filter((option) => option.tipo === 'ADICIONAL').map((option) => `
                <label class="customizer-addon-option">
                    <input type="checkbox" value="${escapeHtml(option.id)}">
                    <span>${escapeHtml(option.nome)}</span>
                    <strong>+ ${money.format(Number(option.precoAdicional))}</strong>
                </label>`).join('')
            : '<p class="customizer-no-addons">Nenhum adicional disponível.</p>';
        customizerSauceList.innerHTML = customizerOptions.filter((option) => option.tipo === 'MOLHO').length
            ? customizerOptions.filter((option) => option.tipo === 'MOLHO').map((option) => `
                <label class="customizer-addon-option">
                    <input type="checkbox" value="${escapeHtml(option.id)}">
                    <span>${escapeHtml(option.nome)}</span>
                    <strong>+ ${money.format(Number(option.precoAdicional))}</strong>
                </label>`).join('')
            : '<p class="customizer-no-addons">Nenhum molho disponível.</p>';
        customizerFirstFlavor.value = String(product.id);
        customizerSecondFlavor.value = '';
        customizerBorder.value = '';
        customizerQuantity = 1;
        pizzaCustomizer.querySelector('input[name="pizzaType"][value="INTEIRA"]').checked = true;
        renderPizzaCustomizerSummary();
        pizzaCustomizer.showModal();
    } catch (error) {
        if (catalogFeedback) {
            catalogFeedback.textContent = 'Não foi possível carregar as opções da pizza. Tente novamente.';
            catalogFeedback.hidden = false;
        }
    }
}

function renderPizzaCustomizerSummary() {
    if (!pizzaCustomizer) return;
    syncCustomizerSelects();
    const pizzaType = pizzaCustomizer.querySelector('input[name="pizzaType"]:checked')?.value || 'INTEIRA';
    const isHalfAndHalf = pizzaType === 'MEIO_A_MEIO';
    const firstProduct = publicProducts.find((item) => String(item.id) === customizerFirstFlavor.value);
    const secondProduct = isHalfAndHalf
        ? publicProducts.find((item) => String(item.id) === customizerSecondFlavor.value)
        : null;
    customizerSecondField.hidden = !isHalfAndHalf;
    customizerSecondFlavor.required = isHalfAndHalf;
    pizzaCustomizer.querySelector('.customizer-first-label').textContent = isHalfAndHalf ? '1º sabor' : 'Sabor';
    const basePrice = firstProduct
        ? secondProduct ? Math.max(firstProduct.price, secondProduct.price) : firstProduct.price
        : 0;
    const selectedBorder = customizerOptions.find((option) => String(option.id) === customizerBorder.value && option.tipo === 'BORDA');
    const selectedAddons = [...customizerAddonList.querySelectorAll('input:checked')]
        .map((input) => customizerOptions.find((option) => String(option.id) === input.value))
        .filter(Boolean);
    const selectedSauces = [...customizerSauceList.querySelectorAll('input:checked')]
        .map((input) => customizerOptions.find((option) => String(option.id) === input.value))
        .filter(Boolean);
    const extrasPrice = (selectedBorder ? Number(selectedBorder.precoAdicional) : 0)
        + selectedAddons.reduce((total, option) => total + Number(option.precoAdicional), 0)
        + selectedSauces.reduce((total, option) => total + Number(option.precoAdicional), 0);
    const unitPrice = basePrice + extrasPrice;
    const flavorLabel = !firstProduct
        ? 'Escolha um sabor'
        : isHalfAndHalf
            ? `1/2 ${firstProduct.name}${secondProduct ? ` + 1/2 ${secondProduct.name}` : ' + escolha o segundo sabor'}`
            : firstProduct.name;

    pizzaCustomizer.querySelector('.customizer-summary-type').textContent = isHalfAndHalf ? 'Pizza meio a meio' : 'Pizza inteira';
    pizzaCustomizer.querySelector('.customizer-summary-flavors').textContent = isHalfAndHalf
        ? `Sabores: ${flavorLabel}`
        : `Sabor: ${flavorLabel}`;
    pizzaCustomizer.querySelector('.customizer-summary-border').textContent = selectedBorder
        ? `Borda: ${selectedBorder.nome} · + ${money.format(Number(selectedBorder.precoAdicional))}`
        : 'Sem borda recheada';
    pizzaCustomizer.querySelector('.customizer-summary-addons').textContent = selectedAddons.length
        ? `Adicionais: ${selectedAddons.map((option) => `${option.nome} (+ ${money.format(Number(option.precoAdicional))})`).join(', ')}`
        : 'Sem adicionais';
    pizzaCustomizer.querySelector('.customizer-summary-sauces').textContent = selectedSauces.length
        ? `Molhos: ${selectedSauces.map((option) => `${option.nome} (+ ${money.format(Number(option.precoAdicional))})`).join(', ')}`
        : 'Sem molhos adicionais';
    customizerQuantityValue.textContent = customizerQuantity;
    pizzaCustomizer.querySelector('.customizer-summary-price-label').textContent = `Total · ${customizerQuantity} ${customizerQuantity === 1 ? 'pizza' : 'pizzas'}`;
    pizzaCustomizer.querySelector('.customizer-summary-price').textContent = money.format(unitPrice * customizerQuantity);
    customizerAddToCart.disabled = !firstProduct || (isHalfAndHalf && !secondProduct);
}

function ensureCustomSelect(select) {
    let control = select.nextElementSibling;
    if (control?.classList.contains('custom-select-enhanced')) return control;

    control = document.createElement('div');
    control.className = 'custom-select-enhanced';
    const trigger = document.createElement('button');
    trigger.type = 'button';
    trigger.className = 'custom-select-trigger';
    trigger.setAttribute('aria-haspopup', 'listbox');
    trigger.setAttribute('aria-expanded', 'false');
    if (select.hasAttribute('aria-labelledby')) {
        trigger.setAttribute('aria-labelledby', select.getAttribute('aria-labelledby'));
    }
    const list = document.createElement('div');
    list.className = 'custom-select-options';
    list.setAttribute('role', 'listbox');
    list.hidden = true;
    control.append(trigger, list);
    select.insertAdjacentElement('afterend', control);
    select.classList.add('custom-select-native-enhanced');
    select.tabIndex = -1;
    select.setAttribute('aria-hidden', 'true');

    trigger.addEventListener('click', () => {
        const opening = list.hidden;
        document.querySelectorAll('.custom-select-options').forEach((otherList) => {
            otherList.hidden = true;
            otherList.previousElementSibling?.setAttribute('aria-expanded', 'false');
        });
        list.hidden = !opening;
        trigger.setAttribute('aria-expanded', String(opening));
    });
    list.addEventListener('click', (event) => {
        const optionButton = event.target.closest('[data-custom-select-value]');
        if (!optionButton || optionButton.disabled) return;
        select.value = optionButton.dataset.customSelectValue;
        select.dispatchEvent(new Event('change', { bubbles: true }));
        list.hidden = true;
        trigger.setAttribute('aria-expanded', 'false');
        trigger.focus();
    });
    trigger.addEventListener('keydown', (event) => {
        if (event.key === 'ArrowDown') {
            event.preventDefault();
            list.hidden = false;
            trigger.setAttribute('aria-expanded', 'true');
            list.querySelector('button:not(:disabled)')?.focus();
        }
    });
    list.addEventListener('keydown', (event) => {
        if (event.key === 'Escape') {
            list.hidden = true;
            trigger.setAttribute('aria-expanded', 'false');
            trigger.focus();
        }
    });
    return control;
}

function syncCustomizerSelects() {
    [customizerFirstFlavor, customizerSecondFlavor, customizerBorder].forEach((select) => {
        if (!select) return;
        const control = ensureCustomSelect(select);
        const trigger = control.querySelector('.custom-select-trigger');
        const list = control.querySelector('.custom-select-options');
        const options = [...select.options];
        options.forEach((option) => {
            const repeatedFlavor = select === customizerSecondFlavor
                ? option.value && option.value === customizerFirstFlavor.value
                : select === customizerFirstFlavor
                    ? option.value && option.value === customizerSecondFlavor.value
                    : false;
            option.disabled = Boolean(repeatedFlavor);
        });
        if (customizerFirstFlavor.value && customizerFirstFlavor.value === customizerSecondFlavor.value) {
            customizerSecondFlavor.value = '';
        }
        trigger.textContent = select.selectedOptions[0]?.textContent || 'Selecione uma opção';
        list.replaceChildren(...options.map((option) => {
            const button = document.createElement('button');
            button.type = 'button';
            button.setAttribute('role', 'option');
            button.setAttribute('aria-selected', String(option.value === select.value));
            button.dataset.customSelectValue = option.value;
            button.textContent = option.textContent;
            button.disabled = option.disabled;
            return button;
        }));
    });
}

function addCustomizedPizza() {
    const pizzaType = pizzaCustomizer.querySelector('input[name="pizzaType"]:checked')?.value || 'INTEIRA';
    const firstProduct = publicProducts.find((item) => String(item.id) === customizerFirstFlavor.value);
    const secondProduct = pizzaType === 'MEIO_A_MEIO'
        ? publicProducts.find((item) => String(item.id) === customizerSecondFlavor.value)
        : null;
    if (!firstProduct || (pizzaType === 'MEIO_A_MEIO' && !secondProduct)) return;

    const borderOption = customizerOptions.find((option) => String(option.id) === customizerBorder.value && option.tipo === 'BORDA');
    const addons = [...customizerAddonList.querySelectorAll('input:checked')]
        .map((input) => customizerOptions.find((option) => String(option.id) === input.value))
        .filter(Boolean);
    const sauces = [...customizerSauceList.querySelectorAll('input:checked')]
        .map((input) => customizerOptions.find((option) => String(option.id) === input.value))
        .filter(Boolean);
    const basePrice = secondProduct ? Math.max(firstProduct.price, secondProduct.price) : firstProduct.price;
    const border = borderOption
        ? { id: Number(borderOption.id), name: borderOption.nome, price: Number(borderOption.precoAdicional) }
        : null;
    const normalizedAddons = addons.map((option) => ({
        id: Number(option.id), name: option.nome, price: Number(option.precoAdicional),
    }));
    const normalizedSauces = sauces.map((option) => ({
        id: Number(option.id), name: option.nome, price: Number(option.precoAdicional),
    }));
    const pizza = {
        itemType: 'PIZZA',
        name: pizzaType === 'MEIO_A_MEIO' ? 'Pizza meio a meio' : 'Pizza inteira',
        productId: firstProduct.id,
        pizzaType,
        firstFlavor: { id: firstProduct.id, name: firstProduct.name },
        secondFlavor: secondProduct ? { id: secondProduct.id, name: secondProduct.name } : null,
        border,
        addons: normalizedAddons,
        sauces: normalizedSauces,
        price: basePrice + (border?.price || 0)
            + normalizedAddons.reduce((total, addon) => total + addon.price, 0)
            + normalizedSauces.reduce((total, sauce) => total + sauce.price, 0),
        quantity: customizerQuantity,
    };
    pizzaCustomizer.close();
    addCartItem(pizza);
}

function applyCategoryFilter(category) {
    const menuItems = document.querySelectorAll('.menu-item');
    menuItems.forEach((item) => {
        const matches = category === 'all' || item.dataset.category === category;
        item.style.display = matches ? '' : 'none';
    });

    document.querySelectorAll('.menu-filter-button').forEach((button) => {
        const isActive = button.dataset.filter === category;
        button.classList.toggle('is-active', isActive);
        button.setAttribute('aria-pressed', String(isActive));
    });
}

loadPublicCatalog();
syncCustomizerSelects();

publicMenuFilters?.addEventListener('click', (event) => {
    const button = event.target.closest('.menu-filter-button');
    if (button) applyCategoryFilter(button.dataset.filter);
});

pizzaCustomizer?.querySelector('.pizza-customizer-close')?.addEventListener('click', () => pizzaCustomizer.close());
pizzaCustomizer?.addEventListener('click', (event) => {
    if (event.target === pizzaCustomizer) pizzaCustomizer.close();
});
pizzaCustomizer?.addEventListener('change', (event) => {
    if (event.target.matches('input[name="pizzaType"], .customizer-first-flavor, .customizer-second-flavor, .customizer-border, .customizer-addon-list input, .customizer-sauce-list input')) {
        if (event.target === customizerFirstFlavor && customizerFirstFlavor.value === customizerSecondFlavor.value) {
            customizerSecondFlavor.value = '';
        }
        renderPizzaCustomizerSummary();
    }
});
pizzaCustomizer?.addEventListener('click', (event) => {
    const stepButton = event.target.closest('[data-customizer-quantity]');
    if (!stepButton) return;
    customizerQuantity = Math.min(50, Math.max(1, customizerQuantity + Number(stepButton.dataset.customizerQuantity)));
    renderPizzaCustomizerSummary();
});
customizerAddToCart?.addEventListener('click', addCustomizedPizza);

document.querySelector('.pizza-grid')?.addEventListener('click', (event) => {
    const button = event.target.closest('.btn-card');
    if (button) addProduct(button.closest('[data-product]'));
});

document.querySelector('.menu-list')?.addEventListener('click', (event) => {
    const button = event.target.closest('.btn-menu-add');
    if (button) addProduct(button);
});

document.querySelector('[data-open-cart]').addEventListener('click', openCart);
document.querySelector('.cart-close').addEventListener('click', closeCart);
document.querySelector('.checkout-close').addEventListener('click', closeCheckout);
backdrop.addEventListener('click', () => {
    closeCart();
    closeCheckout();
    closeConfirmation();
});

itemsElement.addEventListener('click', (event) => {
    const removeButton = event.target.closest('[data-remove-item]');
    if (removeButton) {
        window.cart.splice(Number(removeButton.dataset.removeItem), 1);
        updateCart();
        return;
    }
    const button = event.target.closest('[data-change]');
    if (!button) return;
    const index = Number(button.dataset.change);
    const item = window.cart[index];
    if (!item) return;

    item.quantity = Math.min(50, item.quantity + Number(button.dataset.step));
    if (item.quantity <= 0) window.cart.splice(index, 1);
    updateCart();
});

document.querySelector('.checkout-button').addEventListener('click', () => {
    openCheckout();
});
document.querySelector('.cart-continue')?.addEventListener('click', closeCart);

if (orderQueryForm) {
    orderQueryForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        const codeValue = String(orderQueryForm.querySelector('#order-query-code')?.value || '').trim();
        const phoneValue = String(orderQueryForm.querySelector('#order-query-phone')?.value || '').trim();

        if (!codeValue && !phoneValue) {
            showOrderQueryMessage('Informe o código de acompanhamento ou o telefone para consultar o pedido.');
            if (orderQueryResult) orderQueryResult.hidden = true;
            return;
        }

        if (phoneValue && !isValidPhone(phoneValue)) {
            showOrderQueryMessage('Telefone inválido.');
            if (orderQueryResult) orderQueryResult.hidden = true;
            return;
        }

        try {
            const results = await queryOrdersByApi(codeValue, phoneValue);
            renderOrderQueryResult(results[0]);
        } catch (error) {
            showOrderQueryMessage(error.message || 'Pedido não encontrado. Verifique o código ou telefone informado.');
            if (orderQueryResult) orderQueryResult.hidden = true;
            return;
        }

        if (orderQueryMessage) {
            orderQueryMessage.textContent = 'Pedido encontrado.';
            orderQueryMessage.classList.remove('error');
            orderQueryMessage.classList.add('success');
        }
    });
}

if (adminLoginForm) {
    const passwordField = adminLoginForm.elements.namedItem('adminPassword');
    const passwordToggle = adminLoginForm.querySelector('.admin-password-toggle');

    adminLoginForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        if (!validateAdminLogin()) return;

        const submitButton = adminLoginForm.querySelector('.admin-login-submit');
        const email = String(adminLoginForm.elements.namedItem('adminEmail').value || '').trim();
        const senha = String(passwordField.value || '');
        if (submitButton) {
            submitButton.disabled = true;
            submitButton.textContent = 'ENTRANDO...';
        }
        showAdminLoginMessage('Autenticando...', 'success');

        try {
            await authenticateAdmin(email, senha);
            showAdminLoginMessage('Autenticação realizada com sucesso.', 'success');
            openAdminWorkspace();
        } catch (error) {
            showAdminLoginMessage(error.message || 'Não foi possível autenticar. Tente novamente.');
        } finally {
            if (submitButton) {
                submitButton.disabled = false;
                submitButton.textContent = 'ENTRAR';
            }
        }
    });

    adminLoginForm.addEventListener('input', (event) => {
        if (event.target.matches('input')) {
            event.target.classList.remove('is-invalid');
            if (adminLoginMessage) {
                adminLoginMessage.textContent = '';
                adminLoginMessage.classList.remove('success', 'error');
            }
        }
    });

    if (passwordToggle && passwordField) {
        passwordToggle.addEventListener('click', () => {
            const isPasswordVisible = passwordField.type === 'text';
            passwordField.type = isPasswordVisible ? 'password' : 'text';
            passwordToggle.textContent = isPasswordVisible ? 'Mostrar' : 'Ocultar';
            passwordToggle.setAttribute('aria-label', isPasswordVisible ? 'Mostrar palavra-passe' : 'Ocultar palavra-passe');
            passwordToggle.setAttribute('aria-pressed', String(!isPasswordVisible));
        });
    }
}

document.querySelectorAll('.js-open-admin').forEach((link) => {
    link.addEventListener('click', (event) => {
        event.preventDefault();
        openAdminLogin();
    });
});

document.querySelector('.js-exit-admin')?.addEventListener('click', (event) => {
    event.preventDefault();
    setInterfaceMode('public');
    document.querySelector('#cardapio')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
});

adminLogout?.addEventListener('click', async () => {
    adminLogout.disabled = true;
    try {
        await logoutAdmin();
        setInterfaceMode('public');
        adminLoginForm?.reset();
        if (adminLoginMessage) {
            adminLoginMessage.textContent = '';
            adminLoginMessage.classList.remove('success', 'error');
        }
    } finally {
        adminLogout.disabled = false;
    }
});

if (adminOrderList) {
    adminOrderSearch?.addEventListener('input', loadAdminOrders);
    adminOrderStatus?.addEventListener('change', loadAdminOrders);
    adminOrderList.addEventListener('click', async (event) => {
        const button = event.target.closest('[data-order-id]');
        if (!button) return;
        selectedAdminOrderId = Number(button.dataset.orderId);
        adminOrderDetails.hidden = false;
        showAdminStatusMessage('Carregando detalhes...', 'success');
        try {
            const response = await fetch(`/api/admin/pedidos/${selectedAdminOrderId}`, { credentials: 'same-origin' });
            if (handleAdminApiAuthorization(response.status)) return;
            if (!response.ok) {
                throw new Error(await readAdminApiError(response, 'Não foi possível carregar os detalhes.'));
            }
            renderAdminOrderDetails(normalizeAdminOrder(await response.json()));
        } catch (error) {
            showAdminStatusMessage(error.message || 'Não foi possível carregar os detalhes.');
        }
    });
}

adminStatusSave?.addEventListener('click', async () => {
    const status = adminOrderStatusEditor?.value;
    if (!selectedAdminOrderId || !ORDER_STATUSES.includes(status)) {
        showAdminStatusMessage('Selecione um pedido e um status válido.');
        return;
    }
    adminStatusSave.disabled = true;
    showAdminStatusMessage('Atualizando status...', 'success');
    try {
        const csrfToken = await getCsrfToken();
        const response = await fetch(`/api/admin/pedidos/${selectedAdminOrderId}/status`, {
            method: 'PATCH',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': csrfToken,
            },
            body: JSON.stringify({ status }),
        });
        if (handleAdminApiAuthorization(response.status)) return;
        if (!response.ok) {
            throw new Error(await readAdminApiError(response, 'Não foi possível atualizar o status do pedido.'));
        }
        const updatedOrder = normalizeAdminOrder(await response.json());
        adminOrders = adminOrders.map((order) => order.id === updatedOrder.id ? updatedOrder : order);
        renderAdminSummary();
        renderAdminOrderDetails(updatedOrder);
        renderAdminOrders();
        showAdminStatusMessage('Status do pedido atualizado com sucesso.', 'success');
    } catch (error) {
        showAdminStatusMessage(error.message || 'Não foi possível atualizar o status do pedido.');
    } finally {
        adminStatusSave.disabled = false;
    }
});

if (adminCatalogList) {
    adminCatalogList.addEventListener('click', (event) => {
        const editButton = event.target.closest('[data-edit-product]');
        const toggleButton = event.target.closest('[data-toggle-product]');
        if (editButton) {
            editAdminProduct(adminProducts.find((product) => String(product.id) === editButton.dataset.editProduct));
        }
        if (toggleButton) {
            toggleAdminProduct(toggleButton.dataset.toggleProduct);
        }
    });
}

adminProductForm?.addEventListener('submit', submitAdminProduct);
adminProductCancel?.addEventListener('click', resetAdminProductForm);
adminPizzaOptionList?.addEventListener('click', (event) => {
    const editButton = event.target.closest('[data-edit-pizza-option]');
    const toggleButton = event.target.closest('[data-toggle-pizza-option]');
    if (editButton) {
        editAdminPizzaOption(adminPizzaOptions.find((option) => String(option.id) === editButton.dataset.editPizzaOption));
    }
    if (toggleButton) toggleAdminPizzaOption(toggleButton.dataset.togglePizzaOption);
});
adminPizzaOptionForm?.addEventListener('submit', submitAdminPizzaOption);
adminPizzaOptionCancel?.addEventListener('click', resetAdminPizzaOptionForm);

document.querySelectorAll('[data-phone-mask]').forEach((field) => {
    field.addEventListener('input', () => {
        const cursorPosition = field.selectionStart;
        const previousLength = field.value.length;
        field.value = formatPhone(field.value);
        const lengthDifference = field.value.length - previousLength;
        const nextCursorPosition = Math.max(0, (cursorPosition || field.value.length) + lengthDifference);
        field.setSelectionRange(nextCursorPosition, nextCursorPosition);
    });
});

if (checkoutForm) {
    const cepField = checkoutForm.elements.namedItem('customerCep');
    if (cepField) {
        cepField.addEventListener('input', () => {
            cepField.value = formatCep(cepField.value);
            syncDeliveryCepFields(cepField.value);
            void refreshDeliveryQuote(cepField.value, checkoutForm);
        });
        cepField.addEventListener('blur', () => refreshDeliveryQuote(cepField.value, checkoutForm));
    }
}

if (cartDeliveryCep) {
    cartDeliveryCep.addEventListener('input', () => {
        cartDeliveryCep.value = formatCep(cartDeliveryCep.value);
        syncDeliveryCepFields(cartDeliveryCep.value);
        void refreshDeliveryQuote(cartDeliveryCep.value, panel);
    });
    cartDeliveryCep.addEventListener('blur', () => refreshDeliveryQuote(cartDeliveryCep.value, panel));
}
document.querySelector('.cart-quote-button')?.addEventListener('click', () => {
    void refreshDeliveryQuote(cartDeliveryCep?.value, panel, true);
});

document.querySelector('.admin-order-details-close')?.addEventListener('click', () => {
    if (adminOrderDetails) adminOrderDetails.hidden = true;
    selectedAdminOrderId = null;
});

if (checkoutForm) {
    checkoutForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        clearCheckoutValidation();

        const formData = new FormData(checkoutForm);
        const customer = String(formData.get('customerName') || '').trim();
        const email = String(formData.get('customerEmail') || '').trim();
        const address = composeAddress(formData);
        const phone = formatPhone(formData.get('customerPhone'));
        const notes = String(formData.get('customerNotes') || '').trim();

        if (!customer) {
            markFieldInvalid('customerName', 'Informe seu nome.');
            return;
        }

        if (!email) {
            markFieldInvalid('customerEmail', 'Informe seu e-mail.');
            return;
        }

        if (!isValidEmail(email)) {
            markFieldInvalid('customerEmail', 'E-mail inválido.');
            return;
        }

        if (!phone) {
            markFieldInvalid('customerPhone', 'Informe seu telefone.');
            return;
        }

        if (!isValidPhone(phone)) {
            markFieldInvalid('customerPhone', 'Telefone inválido.');
            return;
        }

        const cep = String(formData.get('customerCep') || '').trim();
        if (!cep) {
            markFieldInvalid('customerCep', 'Informe seu CEP.');
            return;
        }
        if (!/^[0-9]{5}-?[0-9]{3}$/.test(cep)) {
            markFieldInvalid('customerCep', 'Informe um CEP válido.');
            return;
        }

        const requiredAddressFields = [
            ['customerStreet', 'Informe a rua ou o logradouro.'],
            ['customerNumber', 'Informe o número do endereço.'],
            ['customerNeighborhood', 'Informe o bairro.'],
            ['customerCity', 'Informe a cidade.'],
            ['customerState', 'Informe o estado.'],
        ];
        for (const [fieldName, message] of requiredAddressFields) {
            if (!String(formData.get(fieldName) || '').trim()) {
                markFieldInvalid(fieldName, message);
                return;
            }
        }

        if (!Array.isArray(window.cart) || !window.cart.length || !cartItemsAreValid()) {
            showCheckoutMessage('Carrinho vazio.', false);
            return;
        }

        if (!cartItemsHaveProductIds()) {
            showCheckoutMessage('Atualize o cardápio antes de finalizar: há item sem identificador válido.', false);
            return;
        }

        const whatsappWindow = window.open('about:blank', '_blank');

        if (!await refreshDeliveryQuote(cep, checkoutForm, true)) {
            whatsappWindow?.close();
            const quoteMessage = checkoutForm.querySelector('.delivery-quote-message')?.textContent;
            showCheckoutMessage(quoteMessage || 'Informe um CEP atendido para calcular a entrega.', false);
            return;
        }

        const payload = {
            clienteNome: customer,
            clienteEmail: email,
            clienteTelefone: normalizePhone(phone),
            cep,
            endereco: address,
            observacoes: notes,
            itens: window.cart.map((item) => ({
                produtoId: Number(item.productId),
                quantidade: Math.trunc(Number(item.quantity)),
                tipoPizza: item.pizzaType,
                segundoProdutoId: item.pizzaType === 'MEIO_A_MEIO' ? Number(item.secondFlavor.id) : null,
                bordaId: item.border ? Number(item.border.id) : null,
                adicionalIds: item.addons.map((addon) => Number(addon.id)),
                molhoIds: item.sauces.map((sauce) => Number(sauce.id)),
            })),
        };
        const submitButton = checkoutForm.querySelector('.checkout-submit');
        if (submitButton) {
            submitButton.disabled = true;
            submitButton.textContent = 'ENVIANDO...';
        }
        showCheckoutMessage('Criando seu pedido...', true);
        try {
            const createdOrder = normalizePublicOrder(await createOrderViaApi(payload));
            if (!createdOrder || !Number.isFinite(createdOrder.total)) {
                throw new Error('A resposta da criação do pedido está indisponível.');
            }
            const message = [
                'Olá! Meu pedido foi criado:',
                `Código: ${createdOrder.code}`,
                `Status: ${createdOrder.status}`,
                ...createdOrder.items.map((item) => `• ${item.quantity}x ${pizzaWhatsAppDescription(item)} — ${money.format(item.subtotal)}`),
                '',
                `Subtotal: ${money.format(createdOrder.subtotal)}`,
                `${createdOrder.approximateFreight ? 'Frete estimado' : 'Frete'}: ${money.format(createdOrder.freight)}`,
                createdOrder.approximateFreight && 'Valor calculado com base aproximada na região do CEP.',
                `Total: ${money.format(createdOrder.total)}`,
                `Nome: ${createdOrder.customer}`,
                `E-mail: ${createdOrder.email}`,
                createdOrder.cep && `CEP: ${formatCep(createdOrder.cep)}`,
                createdOrder.address && `Endereço: ${createdOrder.address}`,
                `Telefone: ${formatPhone(createdOrder.phone)}`,
                createdOrder.notes && `Observações: ${createdOrder.notes}`,
            ].filter(Boolean).join('\n');
            saveRecentOrder(createdOrder);
            showCheckoutMessage('Pedido criado com sucesso.', true);
            openConfirmation(createdOrder);
            const whatsappUrl = `https://wa.me/${WHATSAPP_NUMBER}?text=${encodeURIComponent(message)}`;
            if (confirmationWhatsappLink) {
                confirmationWhatsappLink.href = whatsappUrl;
                confirmationWhatsappLink.hidden = Boolean(whatsappWindow);
            }
            if (whatsappWindow) {
                whatsappWindow.location.href = whatsappUrl;
            }
        } catch (error) {
            whatsappWindow?.close();
            showCheckoutMessage(error.message || 'Não foi possível criar o pedido. Tente novamente.', false);
        } finally {
            if (submitButton) {
                submitButton.disabled = false;
                submitButton.textContent = 'FINALIZAR PEDIDO';
            }
        }
    });
}

document.querySelector('.confirmation-close').addEventListener('click', closeConfirmation);
confirmationTrack?.addEventListener('click', () => {
    const orderData = JSON.parse(localStorage.getItem(ORDER_STORAGE_KEY) || 'null');
    if (orderData) openOrderTracking(orderData);
});
document.querySelector('.confirmation-continue').addEventListener('click', () => {
    closeConfirmation();
    window.cart = [];
    updateCart();
    currentDeliveryQuote = null;
    deliveryQuoteRequestId += 1;
    closeCheckout();
    if (checkoutForm) {
        checkoutForm.reset();
        setDeliveryQuoteMessage(checkoutForm, '');
    }
    renderDeliveryBreakdown();
    const cartCustomerName = document.querySelector('#customer-name');
    const cartOrderNotes = document.querySelector('#order-notes');
    if (cartCustomerName) cartCustomerName.value = '';
    if (cartOrderNotes) cartOrderNotes.value = '';
});

document.querySelector('.js-scroll-to-about').addEventListener('click', () => {
    document.querySelector('#sobre').scrollIntoView({ behavior: 'smooth' });
});
document.querySelector('.js-about-whatsapp').addEventListener('click', () => {
    window.open(`https://wa.me/${WHATSAPP_NUMBER}?text=${encodeURIComponent('Olá! Quero saber mais sobre a Fatia Prime.')}`, '_blank', 'noopener,noreferrer');
});

renderAdminProducts();
applyCategoryFilter('all');
updateCart();
setInterfaceMode('public');
if (window.location.hash === '#acesso-admin') openAdminLogin();
