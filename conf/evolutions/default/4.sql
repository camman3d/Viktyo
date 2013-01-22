# --- Postings and Locations

# --- !Ups

create table image(
  id            bigint not null auto_increment,
  name          varchar(255) not null,
  uri           varchar(255) not null,
  primary key (id)
);

# --- !Downs

drop table if exists image;
