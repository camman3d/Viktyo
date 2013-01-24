# --- Fix activity stream table

# --- !Ups

alter table activity_stream
change column published published bigint not null,
change column actor actor bigint not null,
change column obj obj bigint not null,
change column target target bigint not null;

# --- !Downs

alter table activity_stream
change column published published long not null,
change column actor actor long not null,
change column obj obj long not null,
change column target target long not null;
