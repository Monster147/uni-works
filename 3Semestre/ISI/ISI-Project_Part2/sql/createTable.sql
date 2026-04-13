create table PERSON(
 id serial primary key,
 email varchar(40) unique check (email like '%@%'),
 taxnumber integer unique,
 name varchar(50)
);

create table SCOOTERMODEL(
 number serial primary key,
 desigantion varchar(30),
 autonomy integer check (autonomy >= 0)
 );

create table TYPEOF(
 reference char(10) primary key check (reference in ('resident', 'tourist')),
 nodays integer check (nodays>=0), 
 price numeric(4,2) check (price>=0)
 );

create table SERVICECOST(
 unlock numeric(3,2) default 1.00,
 usable numeric(3,2) default 0.15
 );

create table STATION(
 id serial primary key,
 latitude numeric(6,4),
 longitude numeric(6,4)
 );

create table CLIENT(
 person integer primary key,
 dtregister timestamp not null,
 foreign key (person) references PERSON (id) ON DELETE CasCADE ON UPDATE CasCADE
);

create table EMPLOYEE(
 person integer primary key,
 number serial unique,
 foreign key (person) references PERSON (id) ON DELETE CasCADE ON UPDATE CasCADE
);

create table CARD(
 id serial primary key,
 credit numeric(4,2) check (credit>=0),
 typeof char(10),
 client integer,
 foreign key (typeof) references TYPEOF (reference) ON DELETE CasCADE ON UPDATE CasCADE,
 foreign key (client) references CLIENT (person) ON DELETE CasCADE ON UPDATE CasCADE
);

create table TOPUP(
 dttopup timestamp,
 card integer,
 value numeric(4,2) check (value >= 0),
 PRIMARY KEY (dttopup, card),
 foreign key (card) references CARD (id) ON DELETE CasCADE ON UPDATE CasCADE
 );

create table SCOOTER(
 id serial primary key,
 weight numeric(4,2) check (weight >= 0), 
 maxvelocity numeric(4,2) check (maxvelocity >=0), 
 battery integer check (battery>=0),--and battery <= (select autonomy from SCOOTERMODEL where number = model)), 
 model integer,
 foreign key (model) references SCOOTERMODEL (number) ON DELETE CasCADE ON UPDATE CasCADE
 );

create table DOCK(
 number serial not null,
 station integer not null,
 state varchar(30) check (state in ('free', 'occupy', 'under maintenance')) not null,
 scooter integer,
 PRIMARY KEY (number, station),
 foreign key (station) references STATION (id) ON DELETE CasCADE ON UPDATE CasCADE,
 foreign key (scooter) references SCOOTER (id) ON DELETE CasCADE ON UPDATE CasCADE,
 check (not (scooter is null and state = 'occupy'))
);

create table REPLACEMENTORDER(
 dtorder timestamp,
 dtreplacement timestamp check (dtreplacement > dtorder) default null,
 roccupation integer check ( roccupation between 0 and 100),
 station integer,
 PRIMARY KEY (dtorder, station),
 foreign key (station) references STATION (id) ON DELETE CasCADE ON UPDATE CasCADE
 );

create table REPLACEMENT(
 number serial,
 dtreplacement timestamp check (dtreplacement > dtreporder),
 action char(8) check( action in ('inplace', 'remove')),
 dtreporder timestamp,
 repstation integer,
 employee integer,
 PRIMARY KEY (number, dtreporder, repstation),
 foreign key (dtreporder, repstation) references REPLACEMENTORDER (dtorder, station) ON DELETE CasCADE ON UPDATE CasCADE,
 foreign key (employee) references EMPLOYEE (person) ON DELETE CasCADE ON UPDATE CasCADE
 );

create table TRAVEL(
 dtinitial timestamp,
 comment varchar(100) check (evaluation is not null or comment is null) default null,
 evaluation integer check (evaluation is null or evaluation between 1 and 5) default null ,
 dtfinal timestamp check ( dtfinal is null or dtfinal > dtinitial) default null,
 client integer,
 scooter integer unique,
 stinitial integer,
 stfinal integer,
 PRIMARY KEY (dtinitial, client),
 foreign key (client) references CLIENT (person) ON DELETE CasCADE ON UPDATE CasCADE,
 foreign key (scooter) references SCOOTER (id) ON DELETE CasCADE ON UPDATE CasCADE,
 foreign key (stinitial) references STATION (id) ON DELETE CasCADE ON UPDATE CasCADE,
 foreign key (stfinal) references STATION (id) ON DELETE CasCADE ON UPDATE CasCADE
 );