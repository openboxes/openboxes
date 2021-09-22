CREATE OR REPLACE VIEW requirement AS (
	SELECT
	  i_l.id,
		i_l.product_id,
		loc.id as location_id,
		i_l.internal_location_id as bin_location_id,
		IFNULL(pa_in_bin.quantity_on_hand, 0) as quantity_in_bin,
		IFNULL(i_l.min_quantity, 0) as min_quantity,
		IFNULL(i_l.max_quantity, 0) as max_quantity,
		IFNULL(i_l.reorder_quantity, 0) as reorder_quantity,
		IFNULL(total_pa.total_quantity_on_hand, 0) as total_quantity_on_hand,
      CASE
        WHEN IFNULL(pa_in_bin.quantity_on_hand, 0) < IFNULL(i_l.min_quantity, 0) THEN 'BELOW_MINIMUM'
        WHEN IFNULL(pa_in_bin.quantity_on_hand, 0) < IFNULL(i_l.reorder_quantity, 0) THEN 'BELOW_REORDER'
        WHEN IFNULL(pa_in_bin.quantity_on_hand, 0) = 0 THEN 'OUT_OF_STOCK'
        WHEN IFNULL(pa_in_bin.quantity_on_hand, 0) > IFNULL(i_l.max_quantity, 0) THEN 'ABOVE_MAXIMUM'
        WHEN IFNULL(pa_in_bin.quantity_on_hand, 0) >= IFNULL(i_l.min_quantity, 0) AND IFNULL(pa_in_bin.quantity_on_hand, 0) <= IFNULL(i_l.max_quantity, 0) THEN 'IN_STOCK'
        ELSE NULL
      END AS status
	FROM inventory_level i_l
    JOIN location loc on loc.inventory_id = i_l.inventory_id
    LEFT OUTER JOIN (
      SELECT
        pa_in_bin.product_id,
        pa_in_bin.location_id,
        pa_in_bin.bin_location_id,
        sum(pa_in_bin.quantity_on_hand) as quantity_on_hand
      FROM product_availability pa_in_bin
      GROUP BY pa_in_bin.product_id, pa_in_bin.location_id, pa_in_bin.bin_location_id
    ) pa_in_bin on (pa_in_bin.product_id = i_l.product_id and pa_in_bin.bin_location_id <=> i_l.internal_location_id and pa_in_bin.location_id = loc.id)
    LEFT OUTER JOIN (
      SELECT
        tpa.product_id,
        tpa.location_id,
        sum(tpa.quantity_on_hand) as total_quantity_on_hand
      FROM product_availability tpa
      GROUP BY tpa.product_id, tpa.location_id
    ) total_pa on (total_pa.product_id = i_l.product_id and total_pa.location_id = loc.id)
	WHERE i_l.max_quantity IS NOT NULL or i_l.min_quantity IS NOT NULL or i_l.reorder_quantity IS NOT NULL
	ORDER BY i_l.last_updated DESC
);
