-- liquibase formatted sql

-- changeset db:1732196145835-1
create table if not exists test (id int auto_increment not null, primary key (id));
