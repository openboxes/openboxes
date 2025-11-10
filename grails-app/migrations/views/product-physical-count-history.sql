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
    -- FIXME Hard-coding the transaction type to Product inventory (11), Inventory Baseline (12) and Adjustment (3) in order to avoid an
    --  additional JOIN to the transaction_type table.
    --  After migrating from single product inventory transaction to baseline + adjustment transactions
    --  there is a case when we might have only an adjustment if it's first inventory record
    where transaction.transaction_type_id IN ('3', '11', '12')
    and (transaction.comment <> 'Inventory baseline created during old product inventory transactions migration for products that had stock but no inventory baseline transaction as a most recent transaction'
       or transaction.comment IS NULL)
    group by transaction.inventory_id, inventory_item.product_id
);
