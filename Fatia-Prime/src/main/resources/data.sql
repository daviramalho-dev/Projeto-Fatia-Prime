merge into categorias (nome) key(nome) values ('Clássicas');
merge into categorias (nome) key(nome) values ('Carnes');
merge into categorias (nome) key(nome) values ('Frango');
merge into categorias (nome) key(nome) values ('Queijos');

merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id) key(nome)
values ('Calabresa Prime', 'Calabresa, mozzarella e cebola.', 49.90, 'assets/calabresa prime.jpg', true, (select id from categorias where nome = 'Carnes'));
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id) key(nome)
values ('Havaiana de Frango', 'Frango desfiado, mozzarella, sour cream, orégano e cebola roxa.', 34.90, 'assets/havaiana.jpg', true, (select id from categorias where nome = 'Frango'));
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id) key(nome)
values ('Costela com Catupiry', 'Costela desfiada, catupiry, mozzarella e cebola roxa.', 56.90, 'assets/costela.jpg', true, (select id from categorias where nome = 'Carnes'));
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id) key(nome)
values ('Quatro Queijos', 'Mozzarella, provolone, parmesão e gorgonzola.', 56.90, 'assets/maguerita.jpg', true, (select id from categorias where nome = 'Queijos'));

merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id) key(nome)
values ('Calabresa Spicy', 'Calabresa, mozzarella, cebola e pimenta jalapenho.', 49.90, null, true, (select id from categorias where nome = 'Carnes'));
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id) key(nome)
values ('Frango com Catupiry', 'Frango desfiado, mozzarella e catupiry.', 52.90, null, true, (select id from categorias where nome = 'Frango'));
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id) key(nome)
values ('Pepperoni Prime', 'Pepperoni, mozzarella e molho especial.', 54.90, null, true, (select id from categorias where nome = 'Carnes'));
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id) key(nome)
values ('Portuguesa', 'Presunto, mozzarella, ovo, cebola, ervilha e milho.', 51.90, null, true, (select id from categorias where nome = 'Clássicas'));
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id) key(nome)
values ('Margherita Prime', 'Molho de tomate, mozzarella, tomate fresco e manjericão.', 48.90, null, true, (select id from categorias where nome = 'Clássicas'));
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id) key(nome)
values ('Napolitana', 'Presunto, mozzarella, tomate, parmesão e orégano.', 50.90, null, true, (select id from categorias where nome = 'Clássicas'));
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id) key(nome)
values ('Bacon Prime', 'Bacon crocante, mozzarella, cebola caramelizada e barbecue.', 55.90, null, true, (select id from categorias where nome = 'Carnes'));
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id) key(nome)
values ('Carne Seca com Cebola', 'Carne seca desfiada, mozzarella, cebola roxa e catupiry.', 59.90, null, true, (select id from categorias where nome = 'Carnes'));
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id) key(nome)
values ('Frango Prime', 'Frango temperado, mozzarella, milho, bacon e molho especial.', 54.90, null, true, (select id from categorias where nome = 'Frango'));
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id) key(nome)
values ('Frango com Milho', 'Frango desfiado, mozzarella, milho verde e requeijão cremoso.', 51.90, null, true, (select id from categorias where nome = 'Frango'));
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id) key(nome)
values ('Brie com Geleia', 'Queijo brie, mozzarella, geleia de pimenta e castanhas.', 58.90, null, true, (select id from categorias where nome = 'Queijos'));
merge into produtos (nome, descricao, preco, imagem, ativo, categoria_id) key(nome)
values ('Provolone Prime', 'Provolone, mozzarella, parmesão, tomate seco e orégano.', 57.90, null, true, (select id from categorias where nome = 'Queijos'));