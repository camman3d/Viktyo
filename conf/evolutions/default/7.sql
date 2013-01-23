# --- Configuration table

# --- !Ups

create table configuration(
  id            bigint not null auto_increment,
  name          varchar(255) not null,
  dataType      varchar(128) not null,
  data          longtext not null,
  primary key (id)
);

# --- !Downs

drop table if exists configuration;
