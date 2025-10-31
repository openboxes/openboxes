
-- FIXME Consider cleaning the ABC classification here instead of in the session view.
--  We could change the default case to DEFAULT here as well. The only
CREATE OR REPLACE VIEW product_classification AS
(
SELECT inventory_level.inventory_id                                      as inventory_id,
       inventory_level.product_id                                        as product_id,
       min(coalesce(inventory_level.abc_class, product.abc_class, NULL)) as abc_class
FROM inventory_level
         JOIN product
              ON inventory_level.product_id = product.id
WHERE inventory_level.internal_location_id IS NULL
GROUP BY inventory_level.inventory_id, inventory_level.product_id
    );
