CREATE TABLE company
(
    id integer NOT NULL,
    name character varying,
    CONSTRAINT company_pkey PRIMARY KEY (id)
);

CREATE TABLE person
(
    id integer NOT NULL,
    name character varying,
    company_id integer references company(id),
    CONSTRAINT person_pkey PRIMARY KEY (id)
);

insert into company(id, name) 
values(1, 'Megafon');
insert into company(id, name) 
values(2, 'MTS');
insert into company(id, name) 
values(3, 'Samsung');
insert into company(id, name) 
values(4, 'LG');
insert into company(id, name) 
values(5, 'Zara');

insert into person(id, name, company_id) 
values(1, 'Nik', 1);
insert into person(id, name, company_id) 
values(2, 'Ben', 1);
insert into person(id, name, company_id) 
values(3, 'Vik', 2);
insert into person(id, name, company_id) 
values(4, 'Eric', 2);
insert into person(id, name, company_id) 
values(5, 'Kevin', 3);
insert into person(id, name) 
values(6, 'Adam');
insert into person(id, name, company_id) 
values(7, 'NOW', 5);
insert into person(id, name, company_id) 
values(8, 'OUT', 5);


-- В одном запросе получить: имена всех person, которые не состоят в компании с id = 5,
-- название компании для каждого человека.
select p.name, pp.name
from company as pp right join person as p
on p.company_id = pp.id
where p.company_id != 5 or p.company_id is null;

-- Необходимо выбрать название компании с максимальным количеством человек 
-- + количество человек в этой компании
-- (нужно учесть, что таких компаний может быть несколько).
WITH g AS (select p.name, count(pp)
from company as p join person as pp
on pp.company_id = p.id
GROUP BY p.name)

SELECT * FROM g WHERE g.count = (SELECT MAX(g.count) FROM g);

