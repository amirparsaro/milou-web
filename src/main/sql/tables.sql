use milou_emails;

create table users (
    id int primary key auto_increment,
    name nvarchar(255) not null,
    email nvarchar(255) not null unique,
    password nvarchar(255) not null
);