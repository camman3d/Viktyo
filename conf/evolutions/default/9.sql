# --- Configuration table

# --- !Ups

create table notification(
  id                bigint not null auto_increment,
  `user`              bigint not null auto_increment,
  `from`              bigint not null auto_increment,
  notificationType  varchar(255) not null,
  `data`              longtext not null,
  created           bigint not null auto_increment,
  `read`              boolean not null auto_increment,
  primary key (id)
);

# --- !Downs

drop table if exists notification;
