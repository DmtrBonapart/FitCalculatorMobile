-- Values per 100 g; existing rows are preserved.
with seed(category, name, calories, protein, fat, carbs) as (
values
('Фрукты','Банан',89,1.09,0.33,22.84),
('Фрукты','Апельсин',47,0.94,0.12,11.75),
('Фрукты','Груша',57,0.36,0.14,15.23),
('Фрукты','Киви',61,1.14,0.52,14.66),
('Фрукты','Виноград',69,0.72,0.16,18.1),
('Овощи','Огурец с кожурой',15,0.65,0.11,3.63),
('Овощи','Помидор',18,0.88,0.2,3.89),
('Овощи','Морковь сырая',41,0.93,0.24,9.58),
('Овощи','Капуста белокочанная сырая',25,1.28,0.1,5.8),
('Овощи','Брокколи сырая',34,2.82,0.37,6.64),
('Овощи','Картофель отварной без кожуры',87,1.87,0.1,20.13),
('Крупы','Гречка отварная',92,3.38,0.62,19.94),
('Крупы','Киноа отварная',120,4.4,1.92,21.3),
('Молочные продукты','Молоко 3,25%',61,3.15,3.25,4.8),
('Молочные продукты','Йогурт натуральный 3,25%',61,3.47,3.25,4.66)
),
inserted as (
insert into public.products (category_id,name,calories,protein,fat,carbs)
select c.id,s.name,s.calories,s.protein,s.fat,s.carbs
from seed s join public.categories c on c.name=s.category
where not exists (select 1 from public.products p where lower(trim(p.name))=lower(s.name))
returning id)
select (select count(*) from inserted) as added,
       (select count(*) from public.products) + (select count(*) from inserted) as total_after;
