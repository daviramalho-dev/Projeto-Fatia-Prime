const WHATSAPP_NUMBER = '5561993637373';
const money = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const cart = [];
const panel = document.querySelector('.cart-panel');
const backdrop = document.querySelector('.cart-backdrop');
const itemsElement = document.querySelector('.cart-items');
const totalElement = document.querySelector('.cart-total strong');

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

function cartTotal() {
    return cart.reduce((total, item) => total + item.price * item.quantity, 0);
}

function updateCart() {
    const count = cart.reduce((total, item) => total + item.quantity, 0);
    document.querySelectorAll('.cart-count').forEach((element) => (element.textContent = count));
    totalElement.textContent = money.format(cartTotal());
    itemsElement.innerHTML = cart.length
        ? cart.map((item, index) => `
            <div class="cart-item">
                <div><strong>${item.name}</strong><small>${money.format(item.price)} cada</small></div>
                <div class="cart-item-controls">
                    <button type="button" data-change="${index}" data-step="-1" aria-label="Remover uma unidade">−</button>
                    <span>${item.quantity}</span>
                    <button type="button" data-change="${index}" data-step="1" aria-label="Adicionar uma unidade">+</button>
                </div>
            </div>`).join('')
        : '<p class="cart-empty">Seu carrinho está vazio.</p>';
}

function addProduct(element) {
    const product = { name: element.dataset.product, price: Number(element.dataset.price) };
    const existing = cart.find((item) => item.name === product.name);
    if (existing) existing.quantity += 1;
    else cart.push({ ...product, quantity: 1 });
    updateCart();
    openCart();
}

document.querySelectorAll('.btn-card').forEach((button) => {
    button.addEventListener('click', () => addProduct(button.closest('[data-product]')));
});
document.querySelectorAll('.btn-menu-add').forEach((button) => {
    button.addEventListener('click', () => addProduct(button));
});
document.querySelector('[data-open-cart]').addEventListener('click', openCart);
document.querySelector('.cart-close').addEventListener('click', closeCart);
backdrop.addEventListener('click', closeCart);

itemsElement.addEventListener('click', (event) => {
    const button = event.target.closest('[data-change]');
    if (!button) return;
    const index = Number(button.dataset.change);
    cart[index].quantity += Number(button.dataset.step);
    if (cart[index].quantity === 0) cart.splice(index, 1);
    updateCart();
});

document.querySelector('.checkout-button').addEventListener('click', () => {
    if (!cart.length) return alert('Adicione ao menos uma pizza ao pedido.');
    const customer = document.querySelector('#customer-name').value.trim();
    const notes = document.querySelector('#order-notes').value.trim();
    const message = [
        'Olá! Quero fazer este pedido:',
        ...cart.map((item) => `• ${item.quantity}x ${item.name} — ${money.format(item.price * item.quantity)}`),
        '', `Total: ${money.format(cartTotal())}`,
        customer && `Nome: ${customer}`,
        notes && `Observações: ${notes}`,
    ].filter(Boolean).join('\n');
    window.open(`https://wa.me/${WHATSAPP_NUMBER}?text=${encodeURIComponent(message)}`, '_blank', 'noopener,noreferrer');
});

document.querySelector('.js-scroll-to-about').addEventListener('click', () => {
    document.querySelector('#sobre').scrollIntoView({ behavior: 'smooth' });
});
document.querySelector('.js-about-whatsapp').addEventListener('click', () => {
    window.open(`https://wa.me/${WHATSAPP_NUMBER}?text=${encodeURIComponent('Olá! Quero saber mais sobre a Fatia Prime.')}`, '_blank', 'noopener,noreferrer');
});

updateCart();
