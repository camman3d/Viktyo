# --- Postings and Locations

# --- !Ups

create table location(
  id            bigint not null auto_increment,
  name          varchar(255) not null,
  latitude      double not null,
  longitude     double not null,
  primary key (id)
);

create table posting(
  id            bigint not null auto_increment,
  name          varchar(255) not null,
  posted        bigint not null,
  poster        bigint not null,
  location      bigint not null,
  primary key (id)
);

# --- !Downs

drop table if exists location;
drop table if exists posting;
