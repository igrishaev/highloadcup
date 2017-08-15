
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

-- :name get-user-visits :?
select
    v.mark,
    v.visited_at,
    l.place
from visits v
join locations l on v.location = l.id
where
    v.user = :user_id
    /*~ (when (:fromDate params) */
    and v.visited_at > :fromDate
    /*~ ) ~*/
    /*~ (when (:toDate params) */
    and v.visited_at < :toDate
    /*~ ) ~*/
    /*~ (when (:toDistance params) */
    and l.distance < :toDistance
    /*~ ) ~*/
    /*~ (when (:country params) */
    and l.country = :country
    /*~ ) ~*/
order by
    v.visited_at;

-- :name get-location-avg :? :1
select
    avg(v.mark) as avg
from visits v
/*~ (when (or (:fromAge params) (:toAge params) (:gender params)) */
join users u on v.user = u.id
/*~ ) ~*/
where
    v.location = :location_id
    /*~ (when (:fromDate params) */
    and v.visited_at > :fromDate
    /*~ ) ~*/
    /*~ (when (:toDate params) */
    and v.visited_at < :toDate
    /*~ ) ~*/
    /*~ (when (:fromAge params) */
    and u.birth_date < :fromAge
    /*~ ) ~*/
    /*~ (when (:toAge params) */
    and u.birth_date > :toAge
    /*~ ) ~*/
    /*~ (when (:gender params) */
    and u.gender = :gender
    /*~ ) ~*/
order by
    v.visited_at;
