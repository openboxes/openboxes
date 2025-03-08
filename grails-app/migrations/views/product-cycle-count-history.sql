-- Using a generic view name in case we decide we need to return more data from cycle count
-- tables. However, this can be renamed if necessary.
create or replace view product_cycle_count_history as (
    select
        location.inventory_id as inventory_id,
        cycle_count_item.product_id as product_id,
        max(cycle_count_item.date_counted) as date_counted
    from cycle_count
        join cycle_count_request on cycle_count.id = cycle_count_request.cycle_count_id
        join cycle_count_item on cycle_count_item.cycle_count_id = cycle_count.id
        join location on location.id = cycle_count.facility_id
    where cycle_count.status = 'COMPLETED'
    group by location.inventory_id, cycle_count_item.product_id
);