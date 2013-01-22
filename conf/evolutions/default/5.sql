# --- Activity Streams and Text objects

# --- !Ups

create table text(
  id            bigint not null auto_increment,
  textValue     longtext not null,
  primary key (id)
);

create table activity_stream(
  id            bigint not null auto_increment,
  published     long not null,
  actor         long not null,
  verb          varchar(255) not null,
  obj           long not null,
  target        long not null,
  primary key (id)
);

# --- !Downs

drop table if exists text;
drop table if exists activity_stream;
