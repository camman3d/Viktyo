# --- Configuration table

# --- !Ups

create table notification(
  id                bigint not null auto_increment,
  user              bigint not null,
  `from`            bigint not null,
  notificationType  varchar(255) not null,
  data              longtext not null,
  created           bigint not null,
  `read`            boolean not null,
  primary key (id)
);

# --- !Downs

drop table if exists notification;
