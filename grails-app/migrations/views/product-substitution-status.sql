CREATE OR REPLACE VIEW product_substitution_status AS
    SELECT
      product_id,
      location_id,
      CASE WHEN MIN(original_min_date) > MIN(substitution_min_date) THEN 'EARLIER' ELSE CASE WHEN SUM(substitution_quantity_on_hand) THEN 'YES' ELSE 'NO' END END AS substitution_status
    FROM (
        SELECT
            p_a.product_id as product_id, -- original product
            p_s.product_id as associated_product_id, -- substitution product
            p_s.location_id as location_id, -- substitution product location
            MIN(original_inventory_item.expiration_date) AS original_min_date,
            SUM(DISTINCT p_s.quantity_on_hand) substitution_quantity_on_hand,
            MIN(subsitution_inventory_item.expiration_date) AS substitution_min_date
        FROM product_association as p_a

        -- product_availability join (used for getting substitute product location and substitution product qoh)
        LEFT OUTER JOIN
            product_availability AS p_s ON p_s.product_id = p_a.associated_product_id

        -- inventory_item joins based on original and substitution products (used for original and substitution inventory items expiration date comparison)
        LEFT OUTER JOIN
            inventory_item AS subsitution_inventory_item ON subsitution_inventory_item.id = p_s.inventory_item_id
        LEFT OUTER JOIN
            inventory_item AS original_inventory_item ON original_inventory_item.product_id = p_a.product_id

        WHERE p_a.code = 'SUBSTITUTE' AND p_s.quantity_on_hand > 0

        GROUP BY product_id, associated_product_id, location_id) a
    WHERE original_min_date > substitution_min_date or substitution_quantity_on_hand > 0
    GROUP BY product_id, location_id;
