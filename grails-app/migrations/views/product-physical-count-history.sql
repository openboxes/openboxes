-- Using a generic view name in case we decide we need to return more data from transaction
-- tables. However, the name is not that important. We might need to use product availability
create or replace view product_physical_count_history as
(
    select
        transaction.inventory_id as inventory_id,
        inventory_item.product_id,
        max(transaction.transaction_date) as date_counted
    from transaction_entry
    join transaction on transaction.id = transaction_entry.transaction_id
    join inventory_item on transaction_entry.inventory_item_id = inventory_item.id
    -- FIXME Hard-coding the transaction type to PRODUCT_INVENTORY in order to avoid an
    --  additional JOIN to the transaction_type table.
    where transaction.transaction_type_id IN ('3', '11', '12')
    group by transaction.inventory_id, inventory_item.product_id
);
