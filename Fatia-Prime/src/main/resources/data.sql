merge into categorias (nome) key(nome) values ('Clássicas');
merge into categorias (nome) key(nome) values ('Carnes');
merge into categorias (nome) key(nome) values ('Frango');
merge into categorias (nome) key(nome) values ('Queijos');
merge into categorias (nome) key(nome) values ('Doces');
merge into categorias (nome) key(nome) values ('Bebidas');

merge into produtos (nome, descricao, preco, imagem, destaque, ativo, categoria_id, tipo_produto) key(nome)
values ('Calabresa Prime', 'Calabresa fatiada selecionada, mussarela especial, cebola e orégano.', 52.90, 'assets/calabresa-prime.webp', 'MAIS PEDIDA', true, (select id from categorias where nome = 'Carnes'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, destaque, ativo, categoria_id, tipo_produto) key(nome)
values ('Havaiana de Frango', 'Frango desfiado temperado, mussarela, sour cream, cebola roxa e orégano.', 54.90, 'assets/havaiana-de-frango.webp', 'CUSTO BENEFÍCIO', true, (select id from categorias where nome = 'Frango'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, destaque, ativo, categoria_id, tipo_produto) key(nome)
values ('Costela com Catupiry', 'Costela bovina desfiada no bafo, Catupiry original, mussarela e cebola roxa.', 62.90, 'assets/costela-catupiry.webp', 'FAVORITA', true, (select id from categorias where nome = 'Carnes'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, destaque, ativo, categoria_id, tipo_produto) key(nome)
values ('Quatro Queijos', 'Combinação equilibrada de mussarela, provolone, queijo parmesão e gorgonzola.', 59.90, 'assets/quatro-queijos.webp', null, true, (select id from categorias where nome = 'Queijos'), 'SALGADA');

merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Calabresa Spicy', 'Calabresa fatiada, mussarela, cebola roxa e pimenta jalapeño.', 52.90, null, true, (select id from categorias where nome = 'Carnes'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Frango com Catupiry', 'Frango desfiado temperado, mussarela derretida e Catupiry original.', 54.90, null, true, (select id from categorias where nome = 'Frango'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Pepperoni', 'Fatias de pepperoni crocante, mussarela e molho de tomate artesanal.', 59.90, null, true, (select id from categorias where nome = 'Carnes'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Portuguesa', 'Presunto cozido, mussarela, ovos, cebola fatiada, ervilhas frescas e milho.', 54.90, null, true, (select id from categorias where nome = 'Clássicas'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Margherita', 'Molho de tomate artesanal, mussarela, fatias de tomate fresco e folhas de manjericão.', 49.90, null, true, (select id from categorias where nome = 'Clássicas'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Napolitana', 'Mussarela, presunto em fatias, rodelas de tomate, queijo parmesão ralado e orégano.', 52.90, null, true, (select id from categorias where nome = 'Clássicas'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Bacon com Barbecue', 'Bacon crocante em tiras, mussarela, cebola caramelizada e molho barbecue.', 57.90, null, true, (select id from categorias where nome = 'Carnes'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Carne Seca com Cebola', 'Carne seca desfiada e refogada, mussarela, cebola roxa e Catupiry.', 64.90, null, true, (select id from categorias where nome = 'Carnes'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Frango Prime', 'Frango desfiado especial, mussarela, milho verde, bacon crocante e molho da casa.', 57.90, null, true, (select id from categorias where nome = 'Frango'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Frango com Milho', 'Frango desfiado temperado, mussarela, milho verde e requeijão cremoso.', 52.90, null, true, (select id from categorias where nome = 'Frango'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Brie com Geleia de Pimenta', 'Queijo brie selecionado, mussarela, geleia de pimenta agridoce e castanhas picadas.', 64.90, null, true, (select id from categorias where nome = 'Queijos'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Provolone Especial', 'Queijo provolone defumado, mussarela, parmesão ralado, tomate seco e orégano.', 59.90, null, true, (select id from categorias where nome = 'Queijos'), 'SALGADA');

merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Strogonoff de Carne', 'Tiras macias de carne ao molho de strogonoff cremoso, mussarela e batata palha crocante.', 62.90, null, true, (select id from categorias where nome = 'Carnes'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Filé com Cheddar', 'Tiras de filé selecionado, cheddar cremoso derretido e mussarela.', 64.90, null, true, (select id from categorias where nome = 'Carnes'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Mussarela', 'Mussarela derretida sobre molho de tomate artesanal, rodelas de tomate fresco e orégano.', 47.90, null, true, (select id from categorias where nome = 'Clássicas'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Calabresa Tradicional', 'Calabresa fatiada em rodelas, cebola fresca e orégano sobre camada de mussarela.', 49.90, null, true, (select id from categorias where nome = 'Clássicas'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Atum', 'Atum sólido especial, mussarela derretida, cebola fatiada e orégano.', 54.90, null, true, (select id from categorias where nome = 'Clássicas'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Alho e Óleo', 'Alho dourado no azeite de oliva, mussarela farta e orégano.', 47.90, null, true, (select id from categorias where nome = 'Clássicas'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Presunto', 'Presunto fatiado selecionado, mussarela derretida e orégano.', 49.90, null, true, (select id from categorias where nome = 'Clássicas'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Escarola com Bacon', 'Escarola fresca refogada no alho, bacon crocante em cubos e mussarela.', 52.90, null, true, (select id from categorias where nome = 'Clássicas'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Lombo Canadense', 'Fatias de lombo canadense defumado, mussarela derretida e cebola roxa.', 56.90, null, true, (select id from categorias where nome = 'Clássicas'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Frango com Cheddar e Bacon', 'Frango desfiado temperado, cheddar cremoso, bacon crocante e mussarela.', 59.90, null, true, (select id from categorias where nome = 'Frango'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Gorgonzola com Nozes', 'Queijo gorgonzola de sabor marcante, mussarela e nozes crocantes picadas.', 64.90, null, true, (select id from categorias where nome = 'Queijos'), 'SALGADA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Catupiry Especial', 'Catupiry original cremoso, mussarela selecionada e toque de orégano.', 56.90, null, true, (select id from categorias where nome = 'Queijos'), 'SALGADA');

merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Chocolate com Morango', 'Chocolate ao leite cremoso coberto com morangos frescos fatiados e fios de leite condensado.', 57.90, null, true, (select id from categorias where nome = 'Doces'), 'DOCE');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Romeu e Julieta', 'Goiabada cremosa combinada com queijo mussarela derretido.', 49.90, null, true, (select id from categorias where nome = 'Doces'), 'DOCE');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Nutella', 'Creme de avelã Nutella original espalhado sobre base macia.', 59.90, null, true, (select id from categorias where nome = 'Doces'), 'DOCE');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Nutella com Morango', 'Creme de avelã Nutella original com morangos frescos fatiados.', 64.90, null, true, (select id from categorias where nome = 'Doces'), 'DOCE');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Brigadeiro', 'Brigadeiro de panela cremoso coberto com granulado de chocolate.', 52.90, null, true, (select id from categorias where nome = 'Doces'), 'DOCE');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Prestígio', 'Chocolate ao leite coberto com coco ralado úmido e leite condensado.', 52.90, null, true, (select id from categorias where nome = 'Doces'), 'DOCE');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Banana com Canela', 'Fatias de banana com açúcar, canela em pó e suave camada de mussarela.', 44.90, null, true, (select id from categorias where nome = 'Doces'), 'DOCE');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Doce de Leite com Coco', 'Doce de leite cremoso suave salpicado com coco ralado.', 49.90, null, true, (select id from categorias where nome = 'Doces'), 'DOCE');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Oreo com Chocolate Branco', 'Chocolate branco derretido com pedaços crocantes de biscoito Oreo.', 59.90, null, true, (select id from categorias where nome = 'Doces'), 'DOCE');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Beijinho', 'Creme de beijinho artesanal com coco ralado e leite condensado.', 49.90, null, true, (select id from categorias where nome = 'Doces'), 'DOCE');

merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Coca-Cola Original', 'Refrigerante Coca-Cola Original em lata de 350 ml.', 8.99, null, true, (select id from categorias where nome = 'Bebidas'), 'BEBIDA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Coca-Cola Zero', 'Refrigerante Coca-Cola Zero Açúcar em lata de 350 ml.', 8.99, null, true, (select id from categorias where nome = 'Bebidas'), 'BEBIDA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Pepsi Original', 'Refrigerante Pepsi Original em lata de 350 ml.', 7.99, null, true, (select id from categorias where nome = 'Bebidas'), 'BEBIDA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Pepsi Black', 'Refrigerante Pepsi Black sem açúcar em lata de 350 ml.', 7.99, null, true, (select id from categorias where nome = 'Bebidas'), 'BEBIDA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Pepsi Twist', 'Refrigerante Pepsi com toque de limão em lata de 350 ml.', 7.99, null, true, (select id from categorias where nome = 'Bebidas'), 'BEBIDA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Fanta Laranja', 'Refrigerante Fanta Laranja em lata de 350 ml.', 7.99, null, true, (select id from categorias where nome = 'Bebidas'), 'BEBIDA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Fanta Uva', 'Refrigerante Fanta Uva em lata de 350 ml.', 7.99, null, true, (select id from categorias where nome = 'Bebidas'), 'BEBIDA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Sprite', 'Refrigerante Sprite sabor limão em lata de 350 ml.', 7.99, null, true, (select id from categorias where nome = 'Bebidas'), 'BEBIDA');
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id, tipo_produto) key(nome)
values ('Guaraná Antarctica', 'Refrigerante Guaraná Antarctica em lata de 350 ml.', 7.99, null, true, (select id from categorias where nome = 'Bebidas'), 'BEBIDA');

delete from produtos where nome in ('Bebida demonstrativa 350 ml', 'Pepperoni Prime', 'Margherita Prime', 'Bacon Prime', 'Brie com Geleia', 'Provolone Prime');

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
