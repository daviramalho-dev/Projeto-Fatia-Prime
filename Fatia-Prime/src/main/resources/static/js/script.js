const WHATSAPP_NUMBER = '5561993637373';
const CART_STORAGE_KEY = 'fatia-prime-cart';
const money = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const panel = document.querySelector('.cart-panel');
const checkoutPanel = document.querySelector('.checkout-panel');
const checkoutForm = document.querySelector('#checkout-form');
const checkoutMessage = document.querySelector('.checkout-message');
const checkoutItemsElement = document.querySelector('.checkout-items');
const checkoutTotalElement = document.querySelector('.checkout-total strong');
const confirmationPanel = document.querySelector('.confirmation-panel');
const confirmationSummaryElement = document.querySelector('.confirmation-items');
const confirmationCodeElement = document.querySelector('.confirmation-code');
const confirmationCustomerElement = document.querySelector('.confirmation-customer');
const confirmationTotalElement = document.querySelector('.confirmation-total strong');
const backdrop = document.querySelector('.cart-backdrop');
const itemsElement = document.querySelector('.cart-items');
const totalElement = document.querySelector('.cart-total strong');
const ORDER_STORAGE_KEY = 'fatia-prime-last-order';

function sanitizeCart(items) {
    if (!Array.isArray(items)) return [];

    return items.reduce((accumulator, item) => {
        if (!item || typeof item !== 'object') return accumulator;

        const name = typeof item.name === 'string' ? item.name.trim() : '';
        const price = Number(item.price);
        const quantity = Number(item.quantity);

        if (!name || !Number.isFinite(price) || price <= 0 || !Number.isFinite(quantity) || quantity <= 0) {
            return accumulator;
        }

        const existing = accumulator.find((entry) => entry.name === name);
        if (existing) {
            existing.quantity += Math.trunc(quantity);
            return accumulator;
        }

        accumulator.push({ name, price, quantity: Math.trunc(quantity) });
        return accumulator;
    }, []);
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

function openCart() {
    panel.classList.add('is-open');
    backdrop.classList.add('is-open');
    panel.setAttribute('aria-hidden', 'false');
}

function closeCart() {
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
    }
}

function closeConfirmation() {
    if (!confirmationPanel) return;
    confirmationPanel.classList.remove('is-open');
    confirmationPanel.setAttribute('aria-hidden', 'true');
    backdrop.classList.remove('is-open');
}

function generateOrderCode() {
    const datePart = new Date().toISOString().slice(0, 10).replace(/-/g, '');
    const randomPart = Math.random().toString(36).slice(2, 8).toUpperCase();
    return `FP-${datePart}-${randomPart}`;
}

function saveRecentOrder(orderData) {
    try {
        localStorage.setItem(ORDER_STORAGE_KEY, JSON.stringify(orderData));
    } catch (error) {
        console.warn('Não foi possível salvar o último pedido no localStorage.', error);
    }
}

function renderConfirmation(orderData) {
    if (!confirmationPanel || !confirmationSummaryElement || !confirmationCodeElement || !confirmationTotalElement) {
        return;
    }

    const data = orderData || JSON.parse(localStorage.getItem(ORDER_STORAGE_KEY) || 'null');
    if (!data) return;

    confirmationCodeElement.textContent = `Pedido ${data.code}`;
    confirmationSummaryElement.innerHTML = data.items.map((item) => `
        <li>
            <div>
                <strong>${item.name}</strong>
                <small>${item.quantity}x • R$ ${Number(item.price).toFixed(2).replace('.', ',')} cada</small>
            </div>
            <strong>R$ ${(Number(item.price) * Number(item.quantity)).toFixed(2).replace('.', ',')}</strong>
        </li>`).join('');
    confirmationTotalElement.textContent = money.format(Number(data.total));

    const customerInfo = [
        data.customer ? `Nome: ${data.customer}` : null,
        data.phone ? `Telefone: ${data.phone}` : null,
        data.address ? `Endereço: ${data.address}` : null,
    ].filter(Boolean).join(' • ');

    confirmationCustomerElement.innerHTML = customerInfo ? `<strong>Dados do cliente</strong><span>${customerInfo}</span>` : '<strong>Dados do cliente</strong><span>Não informado.</span>';
}

function openConfirmation(orderData) {
    if (!confirmationPanel) return;
    renderConfirmation(orderData);
    confirmationPanel.classList.add('is-open');
    confirmationPanel.setAttribute('aria-hidden', 'false');
    backdrop.classList.add('is-open');
    checkoutPanel?.classList.remove('is-open');
    checkoutPanel?.setAttribute('aria-hidden', 'true');
}

function renderCheckoutSummary() {
    if (!checkoutItemsElement || !checkoutTotalElement) return;

    checkoutItemsElement.innerHTML = window.cart.length
        ? window.cart.map((item) => `
            <li>
                <div>
                    <strong>${item.name}</strong>
                    <small>${item.quantity}x ${money.format(item.price)} cada</small>
                </div>
                <strong>${money.format(item.price * item.quantity)}</strong>
            </li>`).join('')
        : '<li><span>Seu carrinho está vazio.</span></li>';

    checkoutTotalElement.textContent = money.format(cartTotal());
}

function openCheckout() {
    if (!window.cart.length) {
        alert('Adicione ao menos uma pizza ao pedido.');
        return;
    }

    if (!checkoutPanel) return;

    renderCheckoutSummary();
    checkoutPanel.classList.add('is-open');
    backdrop.classList.add('is-open');
    checkoutPanel.setAttribute('aria-hidden', 'false');
    panel.classList.remove('is-open');
    panel.setAttribute('aria-hidden', 'true');
}

function cartTotal() {
    return window.cart.reduce((total, item) => total + item.price * item.quantity, 0);
}

function updateCart() {
    const count = window.cart.reduce((total, item) => total + item.quantity, 0);
    document.querySelectorAll('.cart-count').forEach((element) => (element.textContent = count));
    totalElement.textContent = money.format(cartTotal());
    itemsElement.innerHTML = window.cart.length
        ? window.cart.map((item, index) => `
            <div class="cart-item">
                <div><strong>${item.name}</strong><small>${money.format(item.price)} cada</small></div>
                <div class="cart-item-controls">
                    <button type="button" data-change="${index}" data-step="-1" aria-label="Remover uma unidade">−</button>
                    <span>${item.quantity}</span>
                    <button type="button" data-change="${index}" data-step="1" aria-label="Adicionar uma unidade">+</button>
                </div>
            </div>`).join('')
        : '<p class="cart-empty">Seu carrinho está vazio.</p>';
    if (checkoutPanel && checkoutPanel.classList.contains('is-open')) {
        renderCheckoutSummary();
    }
    saveCart();
}

function addProduct(element) {
    const product = { name: element.dataset.product, price: Number(element.dataset.price) };
    const existing = window.cart.find((item) => item.name === product.name);
    if (existing) existing.quantity += 1;
    else window.cart.push({ ...product, quantity: 1 });
    updateCart();
    openCart();
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

document.querySelectorAll('.btn-card').forEach((button) => {
    button.addEventListener('click', () => addProduct(button.closest('[data-product]')));
});
document.querySelectorAll('.btn-menu-add').forEach((button) => {
    button.addEventListener('click', () => addProduct(button));
});
document.querySelectorAll('.menu-filter-button').forEach((button) => {
    button.addEventListener('click', () => applyCategoryFilter(button.dataset.filter));
});

document.querySelector('[data-open-cart]').addEventListener('click', openCart);
document.querySelector('.cart-close').addEventListener('click', closeCart);
document.querySelector('.checkout-close').addEventListener('click', closeCheckout);
backdrop.addEventListener('click', () => {
    closeCart();
    closeCheckout();
});

itemsElement.addEventListener('click', (event) => {
    const button = event.target.closest('[data-change]');
    if (!button) return;
    const index = Number(button.dataset.change);
    const item = window.cart[index];
    if (!item) return;

    item.quantity += Number(button.dataset.step);
    if (item.quantity <= 0) window.cart.splice(index, 1);
    updateCart();
});

document.querySelector('.checkout-button').addEventListener('click', () => {
    openCheckout();
});

if (checkoutForm) {
    checkoutForm.addEventListener('submit', (event) => {
        event.preventDefault();
        const formData = new FormData(checkoutForm);
        const customer = String(formData.get('customerName') || '').trim();
        const email = String(formData.get('customerEmail') || '').trim();
        const address = String(formData.get('customerAddress') || '').trim();
        const phone = String(formData.get('customerPhone') || '').trim();
        const notes = String(formData.get('customerNotes') || '').trim();

        if (!customer) {
            if (checkoutMessage) {
                checkoutMessage.textContent = 'Informe seu nome.';
                checkoutMessage.classList.remove('success');
            }
            return;
        }

        if (!email) {
            if (checkoutMessage) {
                checkoutMessage.textContent = 'Informe seu e-mail.';
                checkoutMessage.classList.remove('success');
            }
            return;
        }

        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            if (checkoutMessage) {
                checkoutMessage.textContent = 'E-mail inválido.';
                checkoutMessage.classList.remove('success');
            }
            return;
        }

        if (!address) {
            if (checkoutMessage) {
                checkoutMessage.textContent = 'Informe seu endereço.';
                checkoutMessage.classList.remove('success');
            }
            return;
        }

        if (!phone) {
            if (checkoutMessage) {
                checkoutMessage.textContent = 'Informe seu telefone.';
                checkoutMessage.classList.remove('success');
            }
            return;
        }

        const cleanPhone = phone.replace(/\D/g, '');
        const phoneIsValid = cleanPhone.length === 10 || cleanPhone.length === 11;

        if (!phoneIsValid) {
            if (checkoutMessage) {
                checkoutMessage.textContent = 'Telefone inválido.';
                checkoutMessage.classList.remove('success');
            }
            return;
        }

        if (!Array.isArray(window.cart) || !window.cart.length || !window.cart.every((item) => item && Number(item.quantity) > 0 && Number(item.price) >= 0)) {
            if (checkoutMessage) {
                checkoutMessage.textContent = 'Carrinho vazio.';
                checkoutMessage.classList.remove('success');
            }
            return;
        }

        const total = cartTotal();
        if (!Number.isFinite(total) || total < 0) {
            if (checkoutMessage) {
                checkoutMessage.textContent = 'Total do pedido inválido.';
                checkoutMessage.classList.remove('success');
            }
            return;
        }

        const orderData = {
            code: generateOrderCode(),
            customer,
            email,
            address,
            phone,
            notes,
            items: window.cart.map((item) => ({
                name: item.name,
                price: Number(item.price),
                quantity: Number(item.quantity),
            })),
            total,
        };

        const message = [
            'Olá! Quero fazer este pedido:',
            ...orderData.items.map((item) => `• ${item.quantity}x ${item.name} — ${money.format(item.price * item.quantity)}`),
            '', `Total: ${money.format(total)}`,
            `Nome: ${customer}`,
            `E-mail: ${email}`,
            `Endereço: ${address}`,
            `Telefone: ${phone}`,
            notes && `Observações: ${notes}`,
        ].filter(Boolean).join('\n');

        saveRecentOrder(orderData);

        if (checkoutMessage) {
            checkoutMessage.textContent = 'Pedido preparado para envio.';
            checkoutMessage.classList.add('success');
        }

        window.open(`https://wa.me/${WHATSAPP_NUMBER}?text=${encodeURIComponent(message)}`, '_blank', 'noopener,noreferrer');
        openConfirmation(orderData);
    });
}

document.querySelector('.confirmation-close').addEventListener('click', closeConfirmation);
document.querySelector('.confirmation-continue').addEventListener('click', () => {
    closeConfirmation();
    window.cart = [];
    updateCart();
    closeCheckout();
    if (checkoutForm) {
        checkoutForm.reset();
    }
});

document.querySelector('.js-scroll-to-about').addEventListener('click', () => {
    document.querySelector('#sobre').scrollIntoView({ behavior: 'smooth' });
});
document.querySelector('.js-about-whatsapp').addEventListener('click', () => {
    window.open(`https://wa.me/${WHATSAPP_NUMBER}?text=${encodeURIComponent('Olá! Quero saber mais sobre a Fatia Prime.')}`, '_blank', 'noopener,noreferrer');
});

applyCategoryFilter('all');
updateCart();
