CREATE OR REPLACE VIEW product_substitution_status AS
    SELECT
    id AS product_id,
    location_id,
    CASE WHEN MIN(original_min_date) > MIN(substitution_min_date) THEN 'EARLIER' ELSE CASE WHEN SUM(substitution_quantity_on_hand) THEN 'YES' ELSE 'NO' END END AS substitution_status
    FROM (
        SELECT
            location.id as location_id,
            product.id,
            product.name,
            MIN(original_inventory_item.expiration_date) AS original_min_date,
            product_association.associated_product_id,
            SUM(DISTINCT substitution_availability.quantity_on_hand) substitution_quantity_on_hand,
            MIN(subsitution_inventory_item.expiration_date) AS substitution_min_date
        FROM product
        LEFT OUTER JOIN
            product_association ON product.id = product_association.product_id AND product_association.code = 'SUBSTITUTE'
        LEFT OUTER JOIN
            product_availability AS original_availability ON original_availability.product_id = product.id
        LEFT OUTER JOIN
            product_availability AS substitution_availability ON substitution_availability.product_id = product_association.associated_product_id
            AND original_availability.location_id = substitution_availability.location_id
        LEFT OUTER JOIN
            inventory_item AS subsitution_inventory_item ON subsitution_inventory_item.id = substitution_availability.inventory_item_id
        LEFT OUTER JOIN
            inventory_item AS original_inventory_item ON original_inventory_item.id = original_availability.inventory_item_id
		JOIN
			location where location_type_id = 2
        GROUP BY product.id, product_association.associated_product_id, location_id) a
    GROUP BY product_id, location_id;
