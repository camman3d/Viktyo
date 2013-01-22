# --- Add objId to property table

# --- !Ups

alter table property add column objId bigint not null;

# --- !Downs

alter table property drop column objId;
