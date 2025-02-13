CREATE OR REPLACE VIEW cycle_count_session AS (
	SELECT
		# ID hash of product.id and location.id
		CRC32(CONCAT(location.id, product.id)) as id,
        inventory.id as inventory_id,
		product.id as product_id,
		location.id as facility_id,
        cycle_count_request.id as cycle_count_request_id,
        COALESCE(inventory_level.abc_class, product.abc_class) as abc_class,

        # Consolidate the statuses down to a single field representing the status of the candidate itself.
        COALESCE(cycle_count.status, cycle_count_request.status) as status,

		# Inventory Item Count
		count(*) as inventory_item_count,

		# Comma-separated list of internal locations
		GROUP_CONCAT(DISTINCT product_availability.bin_location_name) as internal_locations,

		# Quantities by SKU
		sum(product_availability.quantity_on_hand) as quantity_on_hand,
		sum(product_availability.quantity_available_to_promise) as quantity_available,

        # Stuff we need to include now or wait for the materialized view
		NULL as negative_item_count,
		NULL as date_last_count,
		NULL as date_next_count,
		NULL as date_latest_inventory
	FROM product_availability
	JOIN location on product_availability.location_id = location.id
	JOIN product on product_availability.product_id = product.id
	JOIN inventory on location.inventory_id = inventory.id
    LEFT OUTER JOIN inventory_level on product.id = inventory_level.product_id AND inventory.id = inventory_level.inventory_id
    LEFT OUTER JOIN cycle_count_request ON product.id = cycle_count_request.product_id AND location.id = cycle_count_request.facility_id
    LEFT OUTER JOIN cycle_count ON cycle_count_request.cycle_count_id = cycle_count.id
    WHERE product_availability.quantity_on_hand > 0
       AND (cycle_count_request.id IS NULL OR (cycle_count_request.status <> 'COMPLETED' AND cycle_count_request.status <> 'CANCELED'))
	GROUP BY location.id, product.id, abc_class, cycle_count_request.id
    ORDER BY abc_class, inventory_item_count desc
);
