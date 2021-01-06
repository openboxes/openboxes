CREATE TABLE IF NOT EXISTS stockout_fact
(
    date_dimension_id     bigint(20),
    location_dimension_id bigint(20),
    product_dimension_id  bigint(20),
    quantity_on_hand      smallint,
    primary key (date_dimension_id, location_dimension_id, product_dimension_id)
);

CREATE OR REPLACE VIEW product_stockout AS
(
select stockout_fact_tmp.product_id,
       stockout_fact_tmp.location_id,
       case
           when stockout_days > 21 then 'Stocked out 3-4 weeks'
           when stockout_days > 14 then 'Stocked out 2-3 weeks'
           when stockout_days > 7 then 'Stocked out 1-2 weeks'
           when stockout_days > 0 then 'Stocked out <1 week'
           when stockout_days = 0 then 'Never'
           end as stockout_status
from (
         select product_id,
                location_id,
                count(date_dimension.id) as stockout_days
         from stockout_fact
                  join date_dimension on stockout_fact.date_dimension_id = date_dimension.id
                  join product_dimension
                       on stockout_fact.product_dimension_id = product_dimension.id
                  join location_dimension
                       on stockout_fact.location_dimension_id = location_dimension.id
         where date_dimension.date between date_add(now(), interval -30 day) and now()
         group by product_id, location_id) as stockout_fact_tmp
    );

CREATE OR REPLACE VIEW product_stockout_status AS
(
select product_summary.product_id,
       product_summary.location_id,
       IFNULL(product_stockout.stockout_status, 'Never') as stockout_status
from product_summary
         left outer join product_stockout
                         on product_summary.product_id = product_stockout.product_id
                             and product_summary.location_id = product_stockout.location_id
    );
