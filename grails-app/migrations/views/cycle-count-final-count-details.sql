create or replace view cycle_count_final_count_details AS
-- Cycle Count Transaction report
-- Table contains a list of all cycle count transactions in chronological order
-- Transaction details for all product inventory and adjustment transactions associated with a cycle count
-- The variance details CTE is only necessary because we don't seem to be storing the root cause
WITH cycle_count_final_count_details AS (SELECT cycle_count.id                                                        as cycle_count_id,
                                                cycle_count_request.date_created                                      as date_requested,
                                                cycle_count_request.created_by_id                                     as requested_by_id,
                                                cycle_count.date_created                                              as date_started,
                                                cycle_count.created_by_id                                             as started_by_id,
                                                cycle_count.last_updated                                              as date_recorded,
                                                cycle_count.created_by_id                                             as recorded_by_id,
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
                                                cycle_count_item.assignee_id,
                                                cycle_count_item.quantity_on_hand,
                                                cycle_count_item.quantity_counted,
                                                cycle_count_item.quantity_counted - cycle_count_item.quantity_on_hand as quantity_variance,
                                                cycle_count_item.discrepancy_reason_code                              as variance_reason_code,
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
                                                       on ((cycle_count_item.location_id = bin_location.id) OR (cycle_count_item.location_id IS NULL AND bin_location.id IS NULL))
                                                  JOIN location facility on facility.id = cycle_count.facility_id
                                         WHERE cycle_count_request.status = 'COMPLETED'
                                           -- This subquery clause is used to make sure we return the "final count" for every cycle count item
                                           AND cycle_count_item.count_index = (SELECT MAX(count_index)
                                                                               FROM cycle_count_item b
                                                                               WHERE cycle_count_item.cycle_count_id = b.cycle_count_id
                                                                                 AND cycle_count_item.product_id = b.product_id
                                                                                 AND ((cycle_count_item.location_id = b.location_id) OR (cycle_count_item.location_id IS NULL AND b.location_id IS NULL))
                                                                                 AND cycle_count_item.inventory_item_id = b.inventory_item_id))
select cycle_count_id,
       cycle_count_item_id,
       facility_id,
       location_id,
       product_id,
       inventory_item_id,
       transaction_number,
       transaction_date,
       date_requested,
       date_started,
       date_counted,
       date_recorded,
       requested_by_id,
       started_by_id,
       recorded_by_id,
       count_assignee_id,
       recount_assignee_id,
       facility_name,
       product_code,
       product_name,
       location_name,
       lot_number,
       expiration_date,
       quantity_on_hand,
       quantity_counted,
       quantity_variance,
       variance_reason_code,
       variance_comments
from (select ccfcd.cycle_count_id,
             ccfcd.cycle_count_item_id,
             ccfcd.date_requested,
             ccfcd.date_started,
             ccfcd.date_counted,
             ccfcd.date_recorded,
             ccfcd.requested_by_id,
             ccfcd.started_by_id,
             ccfcd.recorded_by_id,
             ccfcd.product_id,
             ccfcd.facility_id,
             ccfcd.location_id,
             ccfcd.inventory_item_id,
             -- fixme we currently only have the recount assignee because the query is targeting the final count
             ccfcd.assignee_id as count_assignee_id,
             ccfcd.assignee_id as recount_assignee_id,
             -- fixme transaction details
             CRC32(ccfcd.cycle_count_id) as transaction_number,
             ccfcd.date_counted as transaction_date,
             ccfcd.product_code,
             ccfcd.product_name,
             ccfcd.facility_name,
             ccfcd.location_name,
             ccfcd.lot_number,
             ccfcd.expiration_date,
             quantity_on_hand,
             quantity_counted,
             quantity_variance,
             group_concat(distinct ccfcd.comment)    as variance_comments,
             group_concat(distinct ccfcd.variance_reason_code) as variance_reason_code
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
