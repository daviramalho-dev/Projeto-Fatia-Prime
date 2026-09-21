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