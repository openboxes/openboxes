CREATE OR REPLACE VIEW product_inventory_expiry_view AS (
    SELECT location.id                                     AS location_id,
           location.name                                   AS location_name,
           bin.id                                          AS bin_id,
           bin.name                                        AS bin_name,
           product.id                                      AS product_id,
           product.product_code                            AS product_code,
           product.name                                    as product_name,
           inventory_item.id                               AS inventory_item_id,
           inventory_item.lot_number                       AS lot_number,
           inventory_item.expiration_date                  AS expiration_date,
           datediff(inventory_item.expiration_date, now()) as days_until_expiry,
           quantity_on_hand
    FROM inventory_snapshot
             JOIN location on inventory_snapshot.location_id = location.id
             LEFT JOIN location bin on inventory_snapshot.bin_location_id = bin.id
             JOIN inventory_item on inventory_item.id = inventory_snapshot.inventory_item_id
             JOIN product on inventory_item.product_id = product.id
    WHERE date = (select max(date) from inventory_snapshot)
);
