-- Using a generic view name in case we decide we need to return more data from transaction
-- tables. However, the name is not that important. We might need to use product availability
create or replace view product_physical_count_history as
(
    select
        transaction.inventory_id as inventory_id,
        transaction_entry.product_id,
        max(transaction.transaction_date) as date_counted
    from transaction_entry
    join transaction on transaction.id = transaction_entry.transaction_id
    where transaction.transaction_type_id = '11'
    group by transaction.inventory_id, transaction_entry.product_id
);