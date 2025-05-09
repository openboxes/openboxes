create or replace view cycle_count_final_count_details AS
-- Cycle Count Transaction report
-- Table contains a list of all cycle count transactions in chronological order
-- Transaction details for all product inventory and adjustment transactions associated with a cycle count
-- The variance details CTE is only necessary because we don't seem to be storing the root cause
WITH cycle_count_final_count_details AS (SELECT cycle_count.id                                                        as cycle_count_id,
                                                cycle_count_item.id                                                   as cycle_count_item_id,
                                                cycle_count_item.date_counted                                         as date_counted,
                                                cycle_count.facility_id,
                                                facility.name                                                         as facility_name,
                                                cycle_count_item.product_id,
                                                product.product_code                                                  as product_code,
                                                product.name                                                          as product_name,
                                                cycle_count_item.inventory_item_id,
                                                inventory_item.lot_number                                             as lot_number,
                                                inventory_item.expiration_date                                        as expiration_date,
                                                cycle_count_item.location_id,
                                                bin_location.name                                                     as location_name,
                                                cycle_count_item.quantity_on_hand,
                                                cycle_count_item.quantity_counted,
                                                cycle_count_item.quantity_counted - cycle_count_item.quantity_on_hand as quantity_variance,
                                                cycle_count_item.discrepancy_reason_code                              as root_cause,
                                                cycle_count_item.comment,
                                                cycle_count_item.count_index
                                         FROM cycle_count_item
                                                  JOIN cycle_count ON cycle_count_item.cycle_count_id = cycle_count.id
                                                  JOIN cycle_count_request
                                                       ON cycle_count.id = cycle_count_request.cycle_count_id
                                                  JOIN inventory_item
                                                       on cycle_count_item.inventory_item_id = inventory_item.id
                                                  JOIN product on inventory_item.product_id = product.id
                                                  join location bin_location
                                                       on cycle_count_item.location_id = bin_location.id
                                                  JOIN location facility on facility.id = cycle_count.facility_id
                                         WHERE cycle_count_request.status = 'COMPLETED'
                                           -- This subquery clause is used to make sure we return the "final count" for every cycle count item
                                           AND cycle_count_item.count_index = (SELECT MAX(count_index)
                                                                               FROM cycle_count_item b
                                                                               WHERE cycle_count_item.cycle_count_id = b.cycle_count_id
                                                                                 AND cycle_count_item.product_id = b.product_id
                                                                                 AND cycle_count_item.inventory_item_id = b.inventory_item_id
                                                                                 AND cycle_count_item.location_id = b.location_id))
select cycle_count_id,
       cycle_count_item_id,
       facility_id,
       location_id,
       product_id,
       inventory_item_id,
#               transaction_number                                     as transaction_number,
#               transaction_date,
       date_counted,
       facility_name,
       product_code,
       product_name,
       location_name,
       lot_number,
       expiration_date,
       quantity_on_hand,
       quantity_counted,
       quantity_variance,
       comments,
       root_cause
from (select ccfcd.cycle_count_id,
             ccfcd.cycle_count_item_id,
             ccfcd.date_counted,
             ccfcd.product_id,
             ccfcd.facility_id,
             ccfcd.location_id,
             ccfcd.inventory_item_id,
#               cctd.transaction_number                                          as transaction_number,
#               cctd.transaction_date,
             ccfcd.product_code,
             ccfcd.product_name,
             ccfcd.facility_name,
             ccfcd.location_name,
             ccfcd.lot_number,
             ccfcd.expiration_date,
             quantity_on_hand,
             quantity_counted,
             quantity_variance,
             group_concat(distinct ccfcd.comment)    as comments,
             group_concat(distinct ccfcd.root_cause) as root_cause
      from cycle_count_final_count_details ccfcd
      #                 left outer join cycle_count_transaction_details cctd on cctd.cycle_count_id = ccdd.cycle_count_id
#            and cctd.facility_id = ccfcd.facility_id
#            and cctd.product_id = ccfcd.product_id
#            and cctd.inventory_item_id = ccfcd.inventory_item_id
#            and cctd.location_id = ccfcd.location_id
      group by ccfcd.cycle_count_id,
               # cctd.transaction_number,
               ccfcd.facility_name,
               ccfcd.product_id,
               ccfcd.product_code,
               ccfcd.product_name,
               ccfcd.lot_number,
               ccfcd.location_name
      order by ccfcd.date_counted) as cycle_count_details
