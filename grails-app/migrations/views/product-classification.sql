CREATE OR REPLACE VIEW product_classification AS (
    select
        inventory_level.inventory_id as inventory_id,
        inventory_level.product_id as product_id,
        min(coalesce(inventory_level.abc_class, product.abc_class, NULL)) as abc_class
    from inventory_level
       join product on inventory_level.product_id = product.id
    group by inventory_level.inventory_id, inventory_level.product_id
);