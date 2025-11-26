CREATE OR REPLACE VIEW pending_cycle_count_request AS
(
    SELECT
        CRC32(CONCAT(pa.location_id, pa.product_id)) as id,
        ccr.id as cycle_count_request_id,
        ccr.facility_id,
        ccr.product_id,
        COALESCE(cc.status, ccr.status) as status,
        ccr.request_type,
        ccr.blind_count,
        ccr.date_created,
        ccr.last_updated,
        ccr.created_by_id,
        ccr.updated_by_id,
        product_classification.abc_class as abc_class,
        SUM(pa.quantity_on_hand) AS quantity_on_hand,
        SUM(pa.quantity_allocated) AS quantity_allocated,
        SUM(CASE WHEN pa.quantity_on_hand < 0 THEN 1 ELSE 0 END) as negative_item_count,
        -- A comma-separated list of internal locations in the format "<zone>: <bin location name>"
        GROUP_CONCAT(DISTINCT (
            IF(pa.quantity_on_hand != 0, CONCAT_WS(': ', zone.name, pa.bin_location_name), NULL)
        )) as internal_locations
    FROM
        cycle_count_request ccr
            LEFT OUTER JOIN product_availability pa
                ON ccr.product_id = pa.product_id
                AND ccr.facility_id = pa.location_id
            LEFT OUTER JOIN cycle_count cc
                ON ccr.cycle_count_id = cc.id
            JOIN location facility ON pa.location_id = facility.id
            LEFT OUTER JOIN location bin_location ON pa.bin_location_id = bin_location.id
            LEFT OUTER JOIN location zone ON bin_location.zone_id = zone.id
            LEFT OUTER JOIN product_classification
                         ON pa.product_id = product_classification.product_id AND
                            facility.inventory_id = product_classification.inventory_id
            JOIN product ON ccr.product_id = product.id
    WHERE
        -- if a product is inactive, hide any pending requests on it so that a count/recount cannot be started on it
        product.active
    GROUP BY
        ccr.id,
        ccr.facility_id,
        ccr.product_id,
        status,
        ccr.cycle_count_id,
        ccr.request_type,
        ccr.blind_count,
        ccr.date_created,
        ccr.last_updated,
        ccr.created_by_id,
        ccr.updated_by_id
);
