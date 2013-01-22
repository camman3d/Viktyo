# --- First database schema

# --- !Ups

create table object(
  id            bigint not null auto_increment,
  objType       tinyint not null,
  objId         bigint not null,
  primary key (id)
);

create table property(
  id            bigint not null auto_increment,
  attribute     varchar(255) not null,
  value         longtext not null,
  primary key (id)
);

create table user(
  id            bigint not null auto_increment,
  fullname      varchar(255) not null,
  username      varchar(255) not null,
  password      varchar(255) not null,
  primary key (id)
);

# --- !Downs

drop table if exists object;
drop table if exists property;
drop table if exists user;
