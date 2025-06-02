use
    milou_emails;

create table users
(
    id       int primary key auto_increment,
    name     nvarchar(255) not null,
    email    nvarchar(255) not null unique,
    password nvarchar(255) not null
);

create table messages
(
    id                int primary key auto_increment,
    title             nvarchar(255)  not null,
    body              nvarchar(6000) not null,
    sender_id         int            not null,
    replied_to_id     int            null,
    forwarded_from_id int            null,

    foreign key (sender_id) references users (id),
    foreign key (replied_to_id) references messages (id),
    foreign key (forwarded_from_id) references messages (id)
);

create table recipients
(
    id           int primary key auto_increment,
    message_read boolean default false not null,
    message_id   int                   not null,
    recipient_id int                   not null,

    foreign key (message_id) references messages (id),
    foreign key (recipient_id) references users (id)
);