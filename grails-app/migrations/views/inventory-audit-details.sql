CREATE OR REPLACE VIEW inventory_audit_details AS
    WITH product_availability_summary
         AS (SELECT location_id as facility_id,
                    product_id,
                    bin_location_id as location_id,
                    inventory_item_id,
                    SUM(quantity_on_hand) AS quantity_on_hand,
                    SUM(quantity_available_to_promise) AS quantity_available,
                    SUM(quantity_allocated) AS quantity_allocated
FROM product_availability
GROUP BY product_id, facility_id, inventory_item_id, bin_location_id
    )
select facility.id                                            as facility_id,
       product.id                                             as product_id,
       coalesce(inventory_level.abc_class, product.abc_class) as abc_class,
       cycle_count_id,
       transaction.transaction_number,
       transaction.transaction_date,
       transaction.transaction_type_id,
       transaction_type.name,
       inventory_item.id                                      as inventory_item_id,
       transaction_entry.bin_location_id                      as location_id,
       transaction.transaction_date                           as date_last_counted,
       transaction_entry.quantity                             as quantity_adjusted,
       product_availability_summary.quantity_on_hand  as quantity_on_hand,
       ifnull(product.price_per_unit, 0) as price_per_unit
from transaction_entry
         join transaction on transaction_entry.transaction_id = transaction.id
         join inventory on transaction.inventory_id = inventory.id
         join location facility on facility.inventory_id = inventory.id
         join inventory_item on transaction_entry.inventory_item_id = inventory_item.id
         join product on inventory_item.product_id = product.id
         join transaction_type on transaction.transaction_type_id = transaction_type.id
         left outer join location on location.id = transaction_entry.bin_location_id
         left outer join inventory_level
                         on product.id = inventory_level.product_id and inventory.id = inventory_level.inventory_id
         join product_availability_summary on product.id = product_availability_summary.product_id and
                                              facility.id = product_availability_summary.facility_id and
                                              ((location.id = product_availability_summary.location_id)
                                                   OR (product_availability_summary.location_id is null and location.id is null))
where transaction_type.transaction_code != 'PRODUCT_INVENTORY';


