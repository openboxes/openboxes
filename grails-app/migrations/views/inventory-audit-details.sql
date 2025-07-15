-- View: inventory_adjustment_detail
-- Purpose: Provides item-level detail for all inventory adjustments, including quantity adjusted,
-- typically associated with cycle counts and physical inventory transactions.
CREATE OR REPLACE VIEW inventory_audit_details AS
    -- FIXME We probably want to move this to another view, but I'll leave it for now.
    WITH product_availability_summary
         AS (SELECT location_id                        as facility_id,
                    product_id,
                    SUM(quantity_on_hand)              AS quantity_on_hand,
                    SUM(quantity_available_to_promise) AS quantity_available,
                    SUM(quantity_allocated)            AS quantity_allocated
             FROM product_availability
             GROUP BY product_id, facility_id)
select facility.id                                            as facility_id,
       facility.name as facility_name,
       product.id                                             as product_id,
       product.product_code as product_code,
       product.name as product_name,
       ifnull(product.price_per_unit, 0) as price_per_unit,
       coalesce(inventory_level.abc_class, product.abc_class) as abc_class,
       cycle_count_id,
       transaction.id as transaction_id,
       transaction.transaction_number,
       transaction.transaction_date,
       transaction.transaction_type_id,
       transaction_type.name as transaction_type_name,
       inventory_item.id                                      as inventory_item_id,
       transaction_entry.bin_location_id                      as location_id,
       transaction_entry.quantity                             as quantity_adjusted,
       product_availability_summary.quantity_on_hand  as quantity_on_hand
from transaction_entry
         join transaction on transaction_entry.transaction_id = transaction.id
         join inventory on transaction.inventory_id = inventory.id
         join location facility on facility.inventory_id = inventory.id
         join inventory_item on transaction_entry.inventory_item_id = inventory_item.id
         join product on inventory_item.product_id = product.id
         join transaction_type on transaction.transaction_type_id = transaction_type.id
         -- Must use a left outer join because the bin_location_id might be NULL
         left outer join location on location.id = transaction_entry.bin_location_id
         -- FIXME There's a small chance that there could be multiple inventory levels that match the
         --  given criteria. The best way to deal with this problem would be to create a CTE to group
         --  the inventory level records by inventory and product.
         left outer join inventory_level on product.id = inventory_level.product_id and
                                            inventory.id = inventory_level.inventory_id and
                                            inventory_level.internal_location_id is null
         -- CTE above generates a summation of quantity values for a given product and facility
         join product_availability_summary on product.id = product_availability_summary.product_id and
                                              facility.id = product_availability_summary.facility_id
-- Include only adjustment transactions (ignores the deprecated debit adjustment transaction with id = 10)
where transaction_type.id IN ('3');


