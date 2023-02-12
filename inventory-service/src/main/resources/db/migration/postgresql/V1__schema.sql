create table products(
    id     integer generated by default as identity,
    amount integer,
    code   varchar(255),
    name   varchar(255),
    price  numeric(19, 2),
    primary key (id)
);