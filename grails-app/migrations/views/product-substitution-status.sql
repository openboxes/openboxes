CREATE OR REPLACE VIEW product_substitution_status AS
    SELECT
        association.product_id as product_id, -- original product
        substitute_availability.location_id as location_id, -- substitution product location
        IF(
            MIN(original_inventory_item.expiration_date) IS NULL AND MIN(substitute_inventory_item.expiration_date) IS NOT NULL,
            'EARLIER',
            IF (
                MIN(original_inventory_item.expiration_date) > MIN(substitute_inventory_item.expiration_date),
                'EARLIER',
                'YES'
            )
        ) AS substitution_status
    FROM product_association as association
        -- substitute product_availability join
        -- (used for getting substitute product location and substitution product quantity available to promise)
        LEFT OUTER JOIN
            product_availability AS substitute_availability ON substitute_availability.product_id = association.associated_product_id
        -- original product_availability join
        -- (used for getting original product quantity available to promise, based on the substitute available stock locations)
        LEFT OUTER JOIN
            product_availability AS original_availability ON (
                original_availability.product_id = association.product_id AND
                original_availability.location_id = substitute_availability.location_id AND
                original_availability.quantity_available_to_promise > 0
            )
        -- inventory_item joins based on original and substitution products product availability
        -- (used for original and substitution inventory items expiration date comparison)
        LEFT OUTER JOIN
            inventory_item AS substitute_inventory_item ON substitute_inventory_item.id = substitute_availability.inventory_item_id
        LEFT OUTER JOIN
            inventory_item AS original_inventory_item ON original_inventory_item.id = original_availability.inventory_item_id
    WHERE association.code = 'SUBSTITUTE' AND substitute_availability.quantity_available_to_promise > 0
    GROUP BY association.product_id, substitute_availability.location_id;
