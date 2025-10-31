create or replace view cycle_count_details AS
    -- Get one row per inventory item counted within a cycle count
    WITH cycle_count_item_details AS (SELECT
                                          cycle_count_item.cycle_count_id as cycle_count_id,
                                          cycle_count_item.product_id,
                                          cycle_count_item.inventory_item_id,
                                          cycle_count_item.location_id
                                      FROM cycle_count_item
                                      GROUP BY
                                          cycle_count_item.cycle_count_id,
                                          cycle_count_item.product_id,
                                          cycle_count_item.inventory_item_id,
                                          cycle_count_item.location_id
),
    -- Get blind count details
    cycle_count_initial_count_details AS (SELECT cycle_count.id as cycle_count_id,
                                                  cycle_count_item.facility_id,
                                                  cycle_count_item.product_id,
                                                  cycle_count_item.inventory_item_id,
                                                  cycle_count_item.location_id,
                                                  cycle_count_item.date_counted,
                                                  cycle_count_item.assignee_id,
                                                  cycle_count_item.quantity_on_hand,
                                                  cycle_count_item.quantity_counted,
                                                  cycle_count_item.quantity_counted - cycle_count_item.quantity_on_hand as quantity_variance,
                                                  cycle_count_item.discrepancy_reason_code                              as variance_reason_code,
                                                  cycle_count_item.comment                                              as variance_comment,
                                                  cycle_count_item.count_index
                                           FROM cycle_count_item
                                           JOIN cycle_count ON cycle_count_item.cycle_count_id = cycle_count.id
                                           JOIN cycle_count_request ON cycle_count.id = cycle_count_request.cycle_count_id
                                           WHERE cycle_count_item.count_index = 0
),
     -- Get verification count details
     cycle_count_final_count_details AS (SELECT cycle_count.id                                                        as cycle_count_id,
                                                cycle_count_item.date_counted                                         as date_counted,
                                                cycle_count.facility_id,
                                                cycle_count_item.product_id,
                                                cycle_count_item.inventory_item_id,
                                                cycle_count_item.location_id,
                                                cycle_count_item.assignee_id,
                                                cycle_count_item.quantity_on_hand,
                                                cycle_count_item.quantity_counted,
                                                cycle_count_item.quantity_counted - cycle_count_item.quantity_on_hand as quantity_variance,
                                                cycle_count_item.discrepancy_reason_code                              as variance_reason_code,
                                                cycle_count_item.comment as variance_comment,
                                                cycle_count_item.count_index
                                         FROM cycle_count_item
                                                  JOIN cycle_count ON cycle_count_item.cycle_count_id = cycle_count.id
                                                  JOIN cycle_count_request
                                                       ON cycle_count.id = cycle_count_request.cycle_count_id
                                           -- This subquery clause is used to make sure we return the "final count" for every cycle count item
                                           WHERE cycle_count_item.count_index = (SELECT MAX(count_index)
                                                                               FROM cycle_count_item b
                                                                               WHERE cycle_count_item.cycle_count_id = b.cycle_count_id
                                                                                 AND cycle_count_item.product_id = b.product_id
                                                                                 AND ((cycle_count_item.location_id = b.location_id) OR (cycle_count_item.location_id IS NULL AND b.location_id IS NULL))
                                                                                AND cycle_count_item.inventory_item_id = b.inventory_item_id)),
transaction_details AS (
        select
            transaction.cycle_count_id,
            transaction.id as transaction_id,
            transaction.transaction_date,
            transaction.transaction_number,
            transaction_type.name as transaction_type_name,
            transaction_type.transaction_code as transaction_type_code,
            transaction.created_by_id as created_by_id,
            transaction.updated_by_id as updated_by_id,
            transaction.date_created,
            transaction.last_updated
            -- The FORCE INDEX is needed for MySQL to properly scan the transaction table (to make it have type="ref" when calling EXPLAIN on that view)
            -- without it, MySQL assumes it's better to scan whole table (type="ALL") instead of using possible indexes as the key.
            -- Since transaction table is very expensive to query against, scanning the whole table caused huge performances issues for MySQL users
            -- that this query took ~2-5 minutes instead of ~200ms.
            -- Note: This is not an issue in MariaDB - it uses the fk_transaction_cycle_count index automatically, and the performances issues don't appear there
            -- Note 2: Whenever this view needs to be updated, please, remove the FORCE INDEX, call EXPLAIN and see, if this index is still the one, that should be the key.
            from transaction FORCE INDEX(fk_transaction_cycle_count)
                join transaction_type on transaction.transaction_type_id = transaction_type.id
                join inventory on transaction.inventory_id = inventory.id
                join location facility on facility.inventory_id = inventory.id
            where transaction_type.transaction_code = 'PRODUCT_INVENTORY'
              and cycle_count_id is not null
)
SELECT

    id,
    # transaction details
    cycle_count_id,
    transaction_number,
    date_requested,
    date_initiated,
    date_recorded,
    requested_by_id,
    initiated_by_id,
    recorded_by_id,

    # inventory item details
    facility_id,
    location_id,
    product_id,
    inventory_item_id,

    # blind count details
    blind_count_date_counted,
    blind_count_assignee_id,
    blind_count_quantity_on_hand,
    blind_count_quantity_counted,
    blind_count_quantity_variance,
    blind_count_variance_percentage,
    blind_count_variance_reason_code,
    blind_count_variance_comment,
    blind_count_count_index,

    # verification details
    verification_count_date_counted,
    verification_count_assignee_id,
    verification_count_quantity_on_hand,
    verification_count_quantity_counted,
    verification_count_quantity_variance,
    verification_count_variance_percentage,
    verification_count_variance_reason_code,
    verification_count_variance_comment,
    verification_count_count_index
from (select
          # Generate a unique identifier to represent an inventory item being cycle counted
          CRC32(CONCAT(cycle_count.id, product.product_code, coalesce(inventory_item.lot_number, 'DEFAULT'), coalesce(bin_location.name, 'DEFAULT'))) as id,

          # cycle count details
          cycle_count.id as cycle_count_id,
          cycle_count_request.date_created                                      as date_requested,
          cycle_count_request.created_by_id                                     as requested_by_id,
          cycle_count.date_created                                              as date_initiated,
          cycle_count.created_by_id                                             as initiated_by_id,

          # inventory item details
          facility.id as facility_id,
          inventory_item.id as inventory_item_id,
          product.id as product_id,
          bin_location.id as location_id,

          # transaction details
          transaction_details.transaction_date                                  as date_recorded,
          transaction_details.transaction_number,
          transaction_details.created_by_id                                     as recorded_by_id,

          # blind count
          icd.assignee_id as blind_count_assignee_id,
          icd.date_counted as blind_count_date_counted,
          icd.quantity_on_hand as blind_count_quantity_on_hand,
          icd.quantity_counted as blind_count_quantity_counted,
          icd.quantity_variance as blind_count_quantity_variance,
          (abs(icd.quantity_variance) / icd.quantity_on_hand) as blind_count_variance_percentage,
          icd.variance_reason_code as blind_count_variance_reason_code,
          icd.variance_comment as blind_count_variance_comment,
          icd.count_index as blind_count_count_index,

          # verification count
          fcd.assignee_id as verification_count_assignee_id,
          fcd.date_counted as verification_count_date_counted,
          fcd.quantity_on_hand as verification_count_quantity_on_hand,
          fcd.quantity_counted as verification_count_quantity_counted,
          fcd.quantity_variance as verification_count_quantity_variance,
          (abs(fcd.quantity_variance) / fcd.quantity_on_hand) as verification_count_variance_percentage,
          fcd.variance_reason_code as verification_count_variance_reason_code,
          fcd.variance_comment as verification_count_variance_comment,
          fcd.count_index as verification_count_count_index
      FROM cycle_count_item_details cycle_count_item
              JOIN cycle_count ON cycle_count_item.cycle_count_id = cycle_count.id
              JOIN cycle_count_request ON cycle_count.id = cycle_count_request.cycle_count_id
          JOIN location facility on facility.id = cycle_count.facility_id
          JOIN inventory_item on cycle_count_item.inventory_item_id = inventory_item.id
          JOIN product on inventory_item.product_id = product.id
          LEFT OUTER JOIN location bin_location ON ((cycle_count_item.location_id = bin_location.id)
                                                        OR (cycle_count_item.location_id IS NULL AND bin_location.id IS NULL))
          LEFT OUTER JOIN transaction_details ON cycle_count.id = transaction_details.cycle_count_id
          LEFT OUTER JOIN cycle_count_initial_count_details icd ON cycle_count_item.cycle_count_id = icd.cycle_count_id
                                                                       AND icd.product_id = cycle_count_item.product_id
                                                                       AND icd.inventory_item_id = cycle_count_item.inventory_item_id
                                                                       AND ((icd.location_id = cycle_count_item.location_id) OR (icd.location_id IS NULL AND cycle_count_item.location_id IS NULL))
          LEFT OUTER JOIN cycle_count_final_count_details fcd ON fcd.cycle_count_id = cycle_count_item.cycle_count_id
                                                                     AND fcd.product_id = cycle_count_item.product_id
                                                                     AND fcd.inventory_item_id = cycle_count_item.inventory_item_id
                                                                     AND ((fcd.location_id = cycle_count_item.location_id) OR (fcd.location_id IS NULL AND cycle_count_item.location_id IS NULL))
      WHERE cycle_count_request.status = 'COMPLETED'

      ) as cycle_count_details
