CREATE OR REPLACE VIEW inventory_audit_details AS
select
    facility.id as facility_id,
    product.id as product_id,
    coalesce(inventory_level.abc_class, product.abc_class) as abc_class,
    transaction.transaction_number,
    transaction.transaction_date,
    transaction.transaction_type_id,
    inventory_item.id as inventory_item_id,
    transaction_entry.bin_location_id as location_id,
    transaction.transaction_date as date_last_counted,
    transaction_entry.quantity as quantity,
    0 as quantity_on_hand

from transaction_entry
    join transaction on transaction_entry.transaction_id = transaction.id
    join inventory on transaction.inventory_id = inventory.id
    join location facility on facility.inventory_id = inventory.id
    join inventory_item on transaction_entry.inventory_item_id = inventory_item.id
    join product on inventory_item.product_id = product.id
    join transaction_type on transaction.transaction_type_id = transaction_type.id
    left outer join inventory_level on product.id = inventory_level.product_id and inventory.id = inventory_level.inventory_id
where transaction_type.transaction_code
