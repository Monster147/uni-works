drop table if exists rental;
drop table if exists court;
drop table if exists club;
drop table if exists utilizador;

create table utilizador
(
    uid   serial PRIMARY KEY,
    name  varchar(80),
    email varchar(80) unique check (email like '%@%'),
    token varchar(80) unique,
    password varchar(124) not null
);

create table club
(
    cid   serial PRIMARY KEY,
    name  varchar(80) unique,
    owner int,
    foreign key (owner) references utilizador (uid)
);

create table court
(
    crid serial PRIMARY KEY,
    name varchar(80),
    club int,
    foreign key (club) references club (cid),
    unique (name, club)
);

create table rental
(
    rid           serial PRIMARY KEY,
    date          date,
    startDuration int,
    endDuration   int,
    utilizador    int,
    court         int,
    club          int,
    foreign key (utilizador) references utilizador (uid),
    foreign key (court) references court (crid),
    foreign key (club) references club (cid)
);
