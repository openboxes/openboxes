-- Using a generic view name in case we decide we need to return more data from cycle count
-- tables. However, this can be renamed if necessary.
create or replace view product_cycle_count_history as (
    select
        location.id as facility_id,
        inventory_item.product_id,
        -- FIXME need to set this to something that's not an auditing field
        max(cycle_count.date_created) as date_counted
    from cycle_count
    join cycle_count_item on cycle_count_item.cycle_count_id = cycle_count.id
    join inventory_item on inventory_item.id = cycle_count_item.inventory_item_id = inventory_item.id
    join location on location.inventory_id = cycle_count.facility_id
    where cycle_count.status = 'COMPLETED'
    group by location.id, inventory_item.product_id
);