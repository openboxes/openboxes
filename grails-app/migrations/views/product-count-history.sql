create or replace view product_count_history as
(
select
    facility_id,
    product_id,
    max(date_counted) as date_counted,
    max(date_received) as date_received
from (select facility_id,
             product_id,
             date_counted as date_counted,
             null         as date_received
      from product_cycle_count_history
      union
      select facility_id,
             product_id,
             date_counted,
             null
      from product_physical_count_history
--          union
--          select facility_id,
--                product_id,
--                null,
--                date_received
--          from product_receipt_history
     ) as inventory_level_summary_unions
    group by facility_id, product_id
    )
;