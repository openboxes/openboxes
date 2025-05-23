CREATE OR REPLACE VIEW cycle_count_session AS
(
SELECT
    # ID hash of product.id and location.id
    CRC32(CONCAT(product_availability.location_id, product_availability.product_id)) as id,

    # Core properties
    facility.inventory_id                                                            as inventory_id,
    product_availability.product_id                                                  as product_id,
    product_availability.location_id                                                 as facility_id,

    -- Cycle count request ID
    -- FIXME We don't need to include this data in the view. It would be fine as an N+1 query done after the
    --  cycle count session records are retrieved. This is due to the fact that this field will never be used to
    --  filter or sort the cycle count session data AND the N+1 query would be far less expensive given the number
    --  of records being returned by the query given that we're using pagination.
    MAX(cycle_count_request_summary.cycle_count_request_id)                          as cycle_count_request_id,

    -- ABC Classification
    -- I considered using the ABC class from cycle_count_metadata after a change request in the PR, but realized that
    -- the main reason NOT to use the ABC class from cycle_count_metadata is because it converts NULLs to DEFAULT
    -- and users may not understand why that is happening. Therefore, we should display the ABC class as it exists
    -- in the database while using the version in metadata to JOIN with the cycle_count_frequency table.
    -- MIN(cycle_count_metadata.abc_class)                                              as abc_class,
    -- FIXME Using a grouping operator due to a GROUP BY issue. This isn't the best approach since it'll return an
    --  empty string or NULL value ahead of valid ABC classes (A, B, C, etc). But I don't have a better solution and
    --  this will likely work for 99% of cases.
    MIN(product_classification.abc_class)                                           as abc_class,

    -- Status of any pending cycle counts
    -- Consolidate the statuses down to a single field representing the status of the candidate itself.
    -- FIXME Using a grouping operator because the SQL is invalid otherwise. There's probably a better way to handle
    --  this (perhaps pulling this into a separate domain that can be joined to the CycleCountSession when needed for
    --  certain queries).
    MAX(cycle_count_request_summary.status)                                          as status,

    -- Product fields
    product.active                                                                   as product_active,

    # Inventory Item Count
    count(product_availability.id)                                                   as inventory_item_count,

    -- A comma-separated list of internal locations in the format "<zone>: <bin location name>"
    GROUP_CONCAT(DISTINCT (
        IF(
            product_availability.quantity_on_hand != 0,
            CONCAT_WS(': ', zone.name, product_availability.bin_location_name),
            NULL
        )
    ))                                                                                as internal_locations,

    # Quantities by SKU
    -- FIXME The sum could include both positive and negative quantities since we no longer exclude negative
    --  quantities in the main query. If we wanted to pull the sum for just on hand quantities we could do
    --  that via the on_hand_summary subquery JOIN.
    sum(product_availability.quantity_on_hand)                                       as quantity_on_hand,
    sum(product_availability.quantity_allocated)                                     as quantity_allocated,

    -- Negative item count
    SUM(CASE WHEN product_availability.quantity_on_hand < 0 THEN 1 ELSE 0 END)       as negative_item_count,

    -- Date last counted and derived properties
    cycle_count_metadata.date_counted                                                as date_last_count,
    cycle_count_metadata.days_since_last_count                                       as days_since_last_count,
    cycle_count_metadata.date_expected                                               as date_next_count,
    cycle_count_metadata.days_until_next_count                                       as days_until_next_count,
    NULL                                                                             as date_latest_inventory

FROM product_availability
         JOIN location facility ON product_availability.location_id = facility.id
         JOIN product ON product_availability.product_id = product.id

         LEFT OUTER JOIN location bin_location ON product_availability.bin_location_id = bin_location.id
         LEFT OUTER JOIN location zone ON bin_location.zone_id = zone.id

    # FIXME need to add a unique constraint to prevent duplicate inventory levels for a single facility / product
    # Using min(abc_class) means we'll return the highest value if there are multiple inventory levels with different abc classes (A, B, C)
         LEFT OUTER JOIN product_classification
                         ON product_availability.product_id = product_classification.product_id AND
                            facility.inventory_id = product_classification.inventory_id

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
                                 max(date_counted)          as date_counted,
                                 max(days_since_last_count) as days_since_last_count,
                                 max(date_expected)         as date_expected,
                                 max(days_until_next_count) as days_until_next_count
                          FROM cycle_count_metadata
                          GROUP BY inventory_id, product_id) as cycle_count_metadata
                         on cycle_count_metadata.inventory_id = facility.inventory_id and
                            cycle_count_metadata.product_id = product_availability.product_id

GROUP BY product_availability.location_id, product_availability.product_id, facility.inventory_id
    );

