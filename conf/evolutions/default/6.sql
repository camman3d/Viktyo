# --- Network tables

# --- !Ups

create table network(
  id            bigint not null auto_increment,
  name          varchar(255) not null,
  description   longtext not null,
  primary key (id)
);

# --- !Downs

drop table if exists network;
