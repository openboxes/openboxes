create or replace view product_count_history as
(
select inventory_id,
       product_id,
       max(date_counted) as date_counted
from (select inventory_id,
             product_id,
             date_counted as date_counted
      from product_cycle_count_history
      union
      select inventory_id,
             product_id,
             date_counted
      from product_physical_count_history) as inventory_level_summary_unions
group by inventory_id, product_id
    );