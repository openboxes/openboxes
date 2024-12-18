CREATE OR REPLACE VIEW cycle_count_session AS (
  SELECT
	*,
	RANK() OVER (ORDER BY abc_class, inventory_item_count desc) as ranking
  FROM (
	SELECT
		# ID hash of product.id and location.id
		CRC32(CONCAT(location.id, product.id)) as id,
        inventory.id as inventory_id,
		product.id as product_id,
		location.id as facility_id,
		product.abc_class as abc_class,

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
    WHERE product_availability.quantity_on_hand > 0
	GROUP BY location.id, product.id, abc_class
  ) as cycle_count_session
);
