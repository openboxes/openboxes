CREATE OR REPLACE VIEW cycle_count_session AS
(
SELECT
    # ID hash of product.id and location.id
    CRC32(CONCAT(product_availability.location_id, product_availability.product_id)) as id,
    location.inventory_id                                                            as inventory_id,
    product_availability.product_id                                                  as product_id,
    product_availability.location_id                                                 as facility_id,

    -- Cycle count request ID
    MAX(cycle_count_request_summary.cycle_count_request_id)                          as cycle_count_request_id,

    -- ABC Classification
    -- FIXME Using a grouping operator due to a GROUP BY issue. This isn't the best approach since it'll return an
    --  empty string or NULL value ahead of valid ABC classes (A, B, C, etc). But I don't have a better solution and
    --  this will likely work for 99% of cases.
    MIN(COALESCE(inventory_level_summary.abc_class, product.abc_class))              as abc_class,

    -- Status of any pending cycle counts
    -- Consolidate the statuses down to a single field representing the status of the candidate itself.
    -- FIXME Using a grouping operator because the SQL is invalid otherwise. There's probably a better way to handle
    --  this (perhaps pulling this into a separate domain that can be joined to the CycleCountSession when needed for
    --  certain queries).
    MAX(cycle_count_request_summary.status)                                          as status,

    # Inventory Item Count
    count(product_availability.id)                                                   as inventory_item_count,

    # Comma-separated list of internal locations
    GROUP_CONCAT(DISTINCT product_availability.bin_location_name)                    as internal_locations,

    # Quantities by SKU
    sum(product_availability.quantity_on_hand)                                       as quantity_on_hand,
    sum(product_availability.quantity_available_to_promise)                          as quantity_available,

    # Stuff we need to include now or wait for the materialized view
    -- Negative item count
    (SELECT SUM(CASE WHEN pai.quantity_on_hand < 0 THEN 1 ELSE 0 END)
     FROM product_availability pai
     WHERE pai.product_id = product_availability.product_id
       AND pai.location_id = product_availability.location_id)                       as negative_item_count,

    -- Date last counted
    -- NULL as date_last_count,
    (select max(date_counted)
     from product_count_history
     where product_count_history.product_id = product_availability.product_id
       and product_count_history.inventory_id = location.inventory_id)               as date_last_count,

    #    NULL as date_next_count,
    #    NULL as days_until_next_count,
    cycle_count_metadata.date_expected                                               as date_next_count,
    cycle_count_metadata.days_until_next_count                                       as days_until_next_count,
    NULL                                                                             as date_latest_inventory

FROM product_availability
         JOIN location ON product_availability.location_id = location.id
         JOIN product ON product_availability.product_id = product.id
    # FIXME need to add a unique constraint to prevent duplicate inventory levels for a single facility / product
    # Using min(abc_class) means we'll return the highest value if there are multiple inventory levels with different abc classes (A, B, C)
         LEFT OUTER JOIN (SELECT inventory_id,
                                 product_id,
                                 min(abc_class) as abc_class
                          FROM inventory_level
                          WHERE inventory_level.internal_location_id IS NULL
                          GROUP BY inventory_id, product_id) as inventory_level_summary
                         ON product_availability.product_id = inventory_level_summary.product_id AND
                            location.inventory_id = inventory_level_summary.inventory_id

    -- The following subquery pulls the latest cycle count request data
         LEFT OUTER JOIN (SELECT cycle_count_request.facility_id                          as facility_id,
                                 cycle_count_request.product_id                           as product_id,
                                 cycle_count_request.id                                   as cycle_count_request_id,
                                 COALESCE(cycle_count.status, cycle_count_request.status) as status
                          FROM cycle_count_request
                                   LEFT OUTER JOIN cycle_count on cycle_count_request.cycle_count_id = cycle_count.id
                          WHERE cycle_count_request.id IS NULL
                             OR (cycle_count_request.status <> 'COMPLETED'
                              AND cycle_count_request.status <> 'CANCELED')) as cycle_count_request_summary
                         ON cycle_count_request_summary.product_id = product_availability.product_id
                             AND cycle_count_request_summary.facility_id = product_availability.location_id

    -- The following subquery computes the expected next count date and days until next count to help with sorting
    -- This subquery adds about 3 seconds to the response time for this query so it might make sense to pull this into
         LEFT OUTER JOIN (SELECT inventory_id,
                                 product_id,
                                 abc_class,
                                 max(date_expected)         as date_expected,
                                 max(days_until_next_count) as days_until_next_count
                          FROM cycle_count_metadata
                          GROUP BY inventory_id, product_id) as cycle_count_metadata
                         on cycle_count_metadata.inventory_id = location.inventory_id and
                            cycle_count_metadata.product_id = product_availability.product_id

GROUP BY product_availability.location_id, product_availability.product_id, location.inventory_id
    )

