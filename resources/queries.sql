
-- :name create-users-table :!
create table users (
    id         integer primary key,
    email      text not null,
    first_name text not null,
    last_name  text not null,
    gender     text not null,
    birth_date integer not null
);

-- :name create-locations-table :!
create table locations (
    id       integer primary key,
    place    text not null,
    country  text not null,
    city     text not null,
    distance integer not null
);

-- :name create-visits-table :!
create table visits (
    id         integer primary key,
    location   integer not null,
    user       integer not null,
    visited_at integer not null,
    mark       integer not null
);
