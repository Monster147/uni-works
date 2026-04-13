insert into PERSON values (1, 'abdial@exemplo.pt', 1, 'Abdial');
insert into PERSON values (2, 'quaresma@exemplo.pt', 2, 'Quaresma');
insert into PERSON values (3, 'elli@exemplo.pt', 3, 'Elli');

insert into SCOOTERMODEL values (100123, 'o ferrari das scooters', 100);
insert into SCOOTERMODEL values (100124, 'o ferrari das scooters', 100);
insert into SCOOTERMODEL values (100125, 'o ferrari das scooters', 100);

insert into TYPEOF values ('resident', 13, 2.20);
insert into TYPEOF values ('tourist', 13, 2.50);

insert into SERVICECOST values (default, default);

insert into STATION values (5, 90.31, 12.43);

insert into CLIENT values (1, '2024-12-04 15:26:31');
insert into CLIENT values (3, '2024-12-04 15:29:25');

insert into EMPLOYEE values (2, 200934);

insert into CARD values (1, 10.90, 'resident', 1);
insert into CARD values (2, 20.00, 'tourist', 3);

insert into TOPUP values('2024-12-04 15:28:31', 1, 5.00);
insert into TOPUP values('2024-12-04 15:31:52', 2, 3.00);

insert into SCOOTER values(1, 10.30, 20.00, 67, 100123);
insert into SCOOTER values(2, 11.30, 19.00, 96, 100124);
insert into SCOOTER values(3, 12.30, 19.00, 96, 100125);


insert into DOCK values(101020, 5, 'free', 1);
insert into DOCK values(101021, 5, 'occupy', 1);
--insert into DOCK values(101021, 5, 'occupy', default); --Este dá erro pois contem o estado de ocupado mas não existe nenhuma scooter associada

insert into REPLACEMENTORDER values ('2024-11-30 10:00:31', '2024-12-04 18:41:01', 2, 5);
insert into REPLACEMENTORDER (dtorder, roccupation, station) values('2024-12-04 12:54:46', 10, 5);

insert into REPLACEMENT values(1, '2024-12-04 18:41:01', 'inplace','2024-11-30 10:00:31', 5, 2);
insert into REPLACEMENT values(2, '2024-12-10 18:41:01', 'remove','2024-12-04 12:54:46', 5, 2);

insert into TRAVEL values('2024-12-04 16:11:01', 'RÁPIDO', 5, '2024-12-04 16:20:34', 1, 1, 5 , 5);
insert into TRAVEL values('2024-12-04 17:00:21', default, 4, '2024-12-04 17:03:39',3 , 2, 5, 5);
insert into TRAVEL values('2024-12-04 17:04:21', default, default, default, 3 , 3, 5, 5);
--insert into TRAVEL values('2024-12-04 17:00:21', 'ERRORS', default, '2024-12-04 17:03:39',3 , 2, 5, 5); -- Este dá erro pois contem um comentário mas nao contém uma avaliação