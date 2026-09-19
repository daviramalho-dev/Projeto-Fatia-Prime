const WHATSAPP_NUMBER = '5561993637373';
const CART_STORAGE_KEY = 'fatia-prime-cart';
const money = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const panel = document.querySelector('.cart-panel');
const checkoutPanel = document.querySelector('.checkout-panel');
const checkoutForm = document.querySelector('#checkout-form');
const checkoutMessage = document.querySelector('.checkout-message');
const checkoutItemsElement = document.querySelector('.checkout-items');
const checkoutTotalElement = document.querySelector('.checkout-total strong');
const backdrop = document.querySelector('.cart-backdrop');
const itemsElement = document.querySelector('.cart-items');
const totalElement = document.querySelector('.cart-total strong');

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

        const requiredFields = { customer, email, address, phone };
        const invalidField = Object.entries(requiredFields).find(([, value]) => !value);

        if (invalidField) {
            if (checkoutMessage) {
                checkoutMessage.textContent = 'Preencha todos os campos obrigatórios antes de finalizar o pedido.';
                checkoutMessage.classList.remove('success');
            }
            return;
        }

        const emailIsValid = /\S+@\S+\.\S+/.test(email);
        const phoneIsValid = phone.replace(/\D/g, '').length >= 10;

        if (!emailIsValid || !phoneIsValid) {
            if (checkoutMessage) {
                checkoutMessage.textContent = 'Informe um e-mail e telefone válidos.';
                checkoutMessage.classList.remove('success');
            }
            return;
        }

        const message = [
            'Olá! Quero fazer este pedido:',
            ...window.cart.map((item) => `• ${item.quantity}x ${item.name} — ${money.format(item.price * item.quantity)}`),
            '', `Total: ${money.format(cartTotal())}`,
            `Nome: ${customer}`,
            `E-mail: ${email}`,
            `Endereço: ${address}`,
            `Telefone: ${phone}`,
            notes && `Observações: ${notes}`,
        ].filter(Boolean).join('\n');

        if (checkoutMessage) {
            checkoutMessage.textContent = 'Pedido preparado para envio.';
            checkoutMessage.classList.add('success');
        }

        window.open(`https://wa.me/${WHATSAPP_NUMBER}?text=${encodeURIComponent(message)}`, '_blank', 'noopener,noreferrer');
    });
}

document.querySelector('.js-scroll-to-about').addEventListener('click', () => {
    document.querySelector('#sobre').scrollIntoView({ behavior: 'smooth' });
});
document.querySelector('.js-about-whatsapp').addEventListener('click', () => {
    window.open(`https://wa.me/${WHATSAPP_NUMBER}?text=${encodeURIComponent('Olá! Quero saber mais sobre a Fatia Prime.')}`, '_blank', 'noopener,noreferrer');
});

applyCategoryFilter('all');
updateCart();
