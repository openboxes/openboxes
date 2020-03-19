CREATE OR REPLACE VIEW product_inventory_compare_view AS (
    SELECT product.id                                    as product_id,
           product.product_code,
           product.name                                  as product_name,
           location.id                                   as location_id,
           location.name                                 as location_name,
           case
               when current_quantity <= 0 then "out_of_stock"
               when current_quantity <= min_quantity and min_quantity != 0 then "low_stock"
               when current_quantity <= reorder_quantity and reorder_quantity != 0
                   then "reorder_stock"
               when current_quantity >= max_quantity and max_quantity != 0 then "over_stock"
               else "in_stock"
               end                                       as status,
           coalesce(inventory_level.min_quantity, 0)     as min_quantity,
           coalesce(inventory_level.reorder_quantity, 0) as reorder_quantity,
           coalesce(inventory_level.max_quantity, 0)     as max_quantity,
           product_inventory_pivot_view.previous_quantity,
           product_inventory_pivot_view.current_quantity
    FROM product_inventory_pivot_view
             JOIN product on product.id = product_inventory_pivot_view.product_id
             JOIN location on location.id = product_inventory_pivot_view.location_id
             LEFT JOIN inventory_level on (inventory_level.product_id = product.id
        AND inventory_level.inventory_id = location.inventory_id)
)
