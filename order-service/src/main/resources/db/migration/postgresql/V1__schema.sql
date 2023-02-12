create table orders(
    id         serial not null,
    client_id  int4,
    created_at timestamp,
    message    varchar(255),
    status     varchar(255),
    primary key (id)
);

create table order_items(
    id           serial not null,
    amount       int4,
    product_code varchar(255),
    order_id     int4,
    primary key (id),
    constraint orders_fk foreign key (order_id)
        references orders (id)
);