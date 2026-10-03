merge into categorias (nome) key(nome) values ('Clássicas');
merge into categorias (nome) key(nome) values ('Carnes');
merge into categorias (nome) key(nome) values ('Frango');
merge into categorias (nome) key(nome) values ('Queijos');
merge into categorias (nome) key(nome) values ('Doces');
merge into categorias (nome) key(nome) values ('Bebidas');

merge into produtos (nome, descricao, preco, imagem, destaque, ativo, categoria_id, tipo_produto) key(nome)
values ('Calabresa Prime', 'Calabresa, mozzarella e cebola.', 52.90, 'assets/calabresa-prime.webp', 'MAIS PEDIDA', true, (select id from categorias where nome = 'Carnes'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, destaque, ativo, categoria_id, tipo_produto) key(nome)
values ('Havaiana de Frango', 'Frango desfiado, mozzarella, sour cream, orégano e cebola roxa.', 54.90, 'assets/havaiana-de-frango.webp', 'CUSTO BENEFÍCIO', true, (select id from categorias where nome = 'Frango'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, destaque, ativo, categoria_id, tipo_produto) key(nome)
values ('Costela com Catupiry', 'Costela desfiada, catupiry, mozzarella e cebola roxa.', 62.90, 'assets/costela-catupiry.webp', 'FAVORITA', true, (select id from categorias where nome = 'Carnes'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, destaque, ativo, categoria_id, tipo_produto) key(nome)
values ('Quatro Queijos', 'Mozzarella, provolone, parmesão e gorgonzola.', 59.90, 'assets/quatro-queijos.webp', null, true, (select id from categorias where nome = 'Queijos'), 'SALGADA');

merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Calabresa Spicy', 'Calabresa, mozzarella, cebola e pimenta jalapenho.', 52.90, null, true, (select id from categorias where nome = 'Carnes'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Frango com Catupiry', 'Frango desfiado, mozzarella e catupiry.', 54.90, null, true, (select id from categorias where nome = 'Frango'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Pepperoni Prime', 'Pepperoni, mozzarella e molho especial.', 59.90, null, true, (select id from categorias where nome = 'Carnes'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Portuguesa', 'Presunto, mozzarella, ovo, cebola, ervilha e milho.', 54.90, null, true, (select id from categorias where nome = 'Clássicas'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Margherita Prime', 'Molho de tomate, mozzarella, tomate fresco e manjericão.', 49.90, null, true, (select id from categorias where nome = 'Clássicas'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Napolitana', 'Presunto, mozzarella, tomate, parmesão e orégano.', 52.90, null, true, (select id from categorias where nome = 'Clássicas'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Bacon Prime', 'Bacon crocante, mozzarella, cebola caramelizada e barbecue.', 57.90, null, true, (select id from categorias where nome = 'Carnes'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Carne Seca com Cebola', 'Carne seca desfiada, mozzarella, cebola roxa e catupiry.', 64.90, null, true, (select id from categorias where nome = 'Carnes'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Frango Prime', 'Frango temperado, mozzarella, milho, bacon e molho especial.', 57.90, null, true, (select id from categorias where nome = 'Frango'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Frango com Milho', 'Frango desfiado, mozzarella, milho verde e requeijão cremoso.', 52.90, null, true, (select id from categorias where nome = 'Frango'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Brie com Geleia', 'Queijo brie, mozzarella, geleia de pimenta e castanhas.', 64.90, null, true, (select id from categorias where nome = 'Queijos'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Provolone Prime', 'Provolone, mozzarella, parmesão, tomate seco e orégano.', 59.90, null, true, (select id from categorias where nome = 'Queijos'), 'SALGADA');

merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Strogonoff de Carne', 'Carne ao molho cremoso, mozzarella e batata palha.', 62.90, null, true, (select id from categorias where nome = 'Carnes'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Filé com Cheddar', 'Filé, cheddar cremoso e mozzarella.', 64.90, null, true, (select id from categorias where nome = 'Carnes'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Mussarela', 'Mozzarella, molho de tomate e orégano.', 47.90, null, true, (select id from categorias where nome = 'Clássicas'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Calabresa Tradicional', 'Calabresa, mozzarella e cebola.', 49.90, null, true, (select id from categorias where nome = 'Clássicas'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Atum', 'Atum, mozzarella, cebola e orégano.', 54.90, null, true, (select id from categorias where nome = 'Clássicas'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Alho e Óleo', 'Alho dourado, azeite, mozzarella e orégano.', 47.90, null, true, (select id from categorias where nome = 'Clássicas'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Presunto', 'Presunto, mozzarella e orégano.', 49.90, null, true, (select id from categorias where nome = 'Clássicas'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Escarola com Bacon', 'Escarola refogada, bacon e mozzarella.', 52.90, null, true, (select id from categorias where nome = 'Clássicas'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Lombo Canadense', 'Lombo canadense, mozzarella e cebola.', 56.90, null, true, (select id from categorias where nome = 'Clássicas'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Frango com Cheddar e Bacon', 'Frango, cheddar cremoso e bacon.', 59.90, null, true, (select id from categorias where nome = 'Frango'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Gorgonzola com Nozes', 'Gorgonzola, mozzarella e nozes.', 64.90, null, true, (select id from categorias where nome = 'Queijos'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Catupiry Especial', 'Catupiry cremoso, mozzarella e orégano.', 56.90, null, true, (select id from categorias where nome = 'Queijos'), 'SALGADA');

merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Chocolate com Morango', 'Chocolate ao leite, morangos e leve toque de leite condensado.', 57.90, null, true, (select id from categorias where nome = 'Doces'), 'DOCE');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Romeu e Julieta', 'Goiabada cremosa e queijo mozzarella.', 49.90, null, true, (select id from categorias where nome = 'Doces'), 'DOCE');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Nutella', 'Creme de avelã com chocolate.', 59.90, null, true, (select id from categorias where nome = 'Doces'), 'DOCE');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Nutella com Morango', 'Creme de avelã com chocolate e morangos.', 64.90, null, true, (select id from categorias where nome = 'Doces'), 'DOCE');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Brigadeiro', 'Brigadeiro cremoso e granulado de chocolate.', 52.90, null, true, (select id from categorias where nome = 'Doces'), 'DOCE');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Prestígio', 'Chocolate e coco.', 52.90, null, true, (select id from categorias where nome = 'Doces'), 'DOCE');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Banana com Canela', 'Banana, açúcar e canela.', 44.90, null, true, (select id from categorias where nome = 'Doces'), 'DOCE');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Doce de Leite com Coco', 'Doce de leite cremoso e coco.', 49.90, null, true, (select id from categorias where nome = 'Doces'), 'DOCE');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Oreo com Chocolate Branco', 'Biscoito Oreo e chocolate branco.', 59.90, null, true, (select id from categorias where nome = 'Doces'), 'DOCE');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Beijinho', 'Beijinho cremoso com coco.', 49.90, null, true, (select id from categorias where nome = 'Doces'), 'DOCE');

merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Coca-Cola Original', 'Refrigerante Coca-Cola Original.', 8.99, null, true, (select id from categorias where nome = 'Bebidas'), 'BEBIDA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Coca-Cola Zero', 'Refrigerante Coca-Cola Zero Açúcar.', 8.99, null, true, (select id from categorias where nome = 'Bebidas'), 'BEBIDA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Pepsi Original', 'Refrigerante Pepsi Original.', 7.99, null, true, (select id from categorias where nome = 'Bebidas'), 'BEBIDA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Pepsi Black', 'Refrigerante Pepsi Black sem açúcar.', 7.99, null, true, (select id from categorias where nome = 'Bebidas'), 'BEBIDA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Pepsi Twist', 'Refrigerante Pepsi com toque de limão.', 7.99, null, true, (select id from categorias where nome = 'Bebidas'), 'BEBIDA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Fanta Laranja', 'Refrigerante sabor laranja.', 7.99, null, true, (select id from categorias where nome = 'Bebidas'), 'BEBIDA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Fanta Uva', 'Refrigerante sabor uva.', 7.99, null, true, (select id from categorias where nome = 'Bebidas'), 'BEBIDA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Sprite', 'Refrigerante sabor limão.', 7.99, null, true, (select id from categorias where nome = 'Bebidas'), 'BEBIDA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Guaraná Antarctica', 'Refrigerante Guaraná Antarctica.', 7.99, null, true, (select id from categorias where nome = 'Bebidas'), 'BEBIDA');

delete from produtos where nome = 'Bebida demonstrativa 350 ml';

merge into opcoes_pizza (nome, tipo, tipo_produto, preco_adicional, ativo) key(nome)
values ('Catupiry', 'BORDA', 'SALGADA', 7.00, true);
merge into opcoes_pizza (nome, tipo, tipo_produto, preco_adicional, ativo) key(nome)
values ('Cheddar', 'BORDA', 'SALGADA', 7.50, true);
merge into opcoes_pizza (nome, tipo, tipo_produto, preco_adicional, ativo) key(nome)
values ('Chocolate', 'BORDA', 'DOCE', 7.00, true);
merge into opcoes_pizza (nome, tipo, tipo_produto, preco_adicional, ativo) key(nome)
values ('Queijo extra', 'ADICIONAL', 'SALGADA', 4.00, true);
merge into opcoes_pizza (nome, tipo, tipo_produto, preco_adicional, ativo) key(nome)
values ('Bacon extra', 'ADICIONAL', 'SALGADA', 5.00, true);
merge into opcoes_pizza (nome, tipo, tipo_produto, preco_adicional, ativo) key(nome)
values ('Morango extra', 'ADICIONAL', 'DOCE', 5.00, true);
merge into opcoes_pizza (nome, tipo, tipo_produto, preco_adicional, ativo) key(nome)
values ('Granulado', 'ADICIONAL', 'DOCE', 3.00, true);
merge into opcoes_pizza (nome, tipo, tipo_produto, preco_adicional, ativo) key(nome)
values ('Molho de tomate', 'MOLHO', 'SALGADA', 0.00, true);

delete from opcoes_pizza where nome = 'Molho demonstrativo';
delete from faixas_frete where nome like 'Demonstração - Região %';

update itens_pedido
set tipo_produto_snapshot = (
	select produtos.tipo_produto from produtos where produtos.id = itens_pedido.produto_id
)
where tipo_produto_snapshot is null;