-- Using a generic view name in case we decide we need to return more data from transaction
-- tables. However, the name is not that important. We might need to use product availability
create or replace view product_physical_count_history as
(
    select
        location.id as facility_id,
        inventory_item.product_id,
        max(transaction.transaction_date) as date_counted
    from transaction_entry
    join transaction on transaction.id = transaction_entry.transaction_id
    join inventory on inventory.id = transaction.inventory_id
    join location on location.inventory_id = inventory.id
    -- We might not need this JOIN if we can ensure that transaction_entry.product_id
    -- is populated. At the moment it is not populated for all transactions. We should
    -- add that as a tech debt issue if performance becomes an issue.
    join inventory_item on inventory_item.id = transaction_entry.inventory_item_id
    -- We can get a little better performance by using transaction.transaction_type_id = 11
    -- but I wanted this to be more readable in the design
    join transaction_type on transaction_type.id = transaction.transaction_type_id
    where transaction_type.transaction_code = 'PRODUCT_INVENTORY'
    group by location.id, transaction_entry.product_id
);