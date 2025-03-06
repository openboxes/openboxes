CREATE OR REPLACE VIEW cycle_count_session AS
(
SELECT
    # ID hash of product.id and location.id
    CRC32(CONCAT(location.id, product.id))                                       as id,
    location.inventory_id                                                                 as inventory_id,
    product.id                                                                   as product_id,
    location.id                                                                  as facility_id,

    -- Cycle count request ID
    cycle_count_request_summary.cycle_count_request_id as cycle_count_request_id,

    -- ABC Classification
    COALESCE(inventory_level_summary.abc_class, product.abc_class)               as abc_class,

    -- Status of any pending cycle counts
    -- Consolidate the statuses down to a single field representing the status of the candidate itself.
    cycle_count_request_summary.status as status,

    # Inventory Item Count
    count(product_availability.id)                                               as inventory_item_count,

    # Comma-separated list of internal locations
    GROUP_CONCAT(DISTINCT product_availability.bin_location_name)                as internal_locations,

    # Quantities by SKU
    sum(product_availability.quantity_on_hand)                                   as quantity_on_hand,
    sum(product_availability.quantity_available_to_promise)                      as quantity_available,

    # Stuff we need to include now or wait for the materialized view
    -- Negative item count
    (SELECT SUM(CASE WHEN pai.quantity_on_hand < 0 THEN 1 ELSE 0 END)
     FROM product_availability pai
     WHERE pai.product_id = product_availability.product_id
       AND pai.location_id = product_availability.location_id)                   as negative_item_count,

    -- Date last counted
    (select date_counted
     from product_count_history
     where product_count_history.product_id = product_availability.product_id
       and product_count_history.inventory_id = location.inventory_id) as date_last_count,

    NULL                                                                         as date_next_count,
    NULL                                                                         as date_latest_inventory
FROM product_availability
         JOIN location on product_availability.location_id = location.id
         JOIN product ON product_availability.product_id = product.id
    # FIXME need to add a unique constraint to prevent duplicate inventory levels for a single facility / product
    # Using min(abc_class) means we'll return the highest value if there are multiple inventory levels with different abc classes (A, B, C)
        LEFT OUTER JOIN (select
                              inventory_id,
                              product_id,
                              min(abc_class) as abc_class
                          from inventory_level where inventory_level.internal_location_id IS NULL
                          group by inventory_id, product_id) as inventory_level_summary
                         ON product_availability.product_id = inventory_level_summary.product_id AND location.inventory_id = inventory_level_summary.inventory_id

        LEFT OUTER JOIN (select cycle_count_request.facility_id                          as facility_id,
                                cycle_count_request.product_id                           as product_id,
                                cycle_count_request.id                                   as cycle_count_request_id,
                                COALESCE(cycle_count.status, cycle_count_request.status) as status
                         from cycle_count_request
                                  left outer join cycle_count on cycle_count_request.cycle_count_id = cycle_count.id
                         where cycle_count_request.id IS NULL
                            OR (cycle_count_request.status <> 'COMPLETED'
                                    AND cycle_count_request.status <> 'CANCELED')) as cycle_count_request_summary
            on cycle_count_request_summary.product_id = product_availability.product_id
                   and cycle_count_request_summary.facility_id = product_availability.location_id


GROUP BY product_availability.location_id, product_availability.product_id
HAVING sum(product_availability.quantity_on_hand) > 0
ORDER BY NULL
    );
