use milou_emails;

create table users (
    id int primary key auto_increment,
    name nvarchar(255) not null,
    email nvarchar(255) not null unique,
    password nvarchar(255) not null
);

create table messages (
    id int primary key auto_increment,
    title nvarchar(255) not null,
    body nvarchar(6000) not null
);

create table Recipients (
    id int primary key auto_increment,
    message_id int not null,
    user_id int not null,

    foreign key (message_id) references messages(id),
    foreign key (user_id) references users(id)
);