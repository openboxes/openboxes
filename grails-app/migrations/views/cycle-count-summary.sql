create or replace view cycle_count_summary AS
(
select
    # Generate a unique identifier to represent an inventory item being cycle counted
    CRC32(CONCAT(cycle_count_id, product_id))             as id,
    cycle_count_id,
    product_id,
    MAX(transaction_number)                               as transaction_number,
    MAX(date_requested)                                   as date_requested,
    MAX(date_initiated)                                   as date_initiated,
    MAX(date_recorded)                                    as date_recorded,
    MAX(requested_by_id)                                  as requested_by_id,
    MAX(initiated_by_id)                                  as initiated_by_id,
    MAX(recorded_by_id)                                   as recorded_by_id,
    MAX(facility_id)                                      as facility_id,
    MAX(location_id)                                      as location_id,
    MAX(blind_count_date_counted)                         as blind_count_date_counted,
    MAX(blind_count_assignee_id)                          as blind_count_assignee_id,
    sum(blind_count_quantity_counted)                     as blind_count_quantity_counted,
    sum(blind_count_quantity_on_hand)                     as blind_count_quantity_on_hand,
    sum(blind_count_quantity_variance)                    as blind_count_quantity_variance,
    group_concat(blind_count_variance_reason_code)        as blind_count_variance_reason_code,
    group_concat(blind_count_variance_comment)            as blind_count_variance_comment,
    MAX(verification_count_date_counted)                  as verification_count_date_counted,
    MAX(verification_count_assignee_id)                   as verification_count_assignee_id,
    sum(verification_count_quantity_counted)              as verification_count_quantity_counted,
    sum(verification_count_quantity_on_hand)              as verification_count_quantity_on_hand,
    sum(verification_count_quantity_variance)             as verification_count_quantity_variance,
    group_concat(verification_count_variance_reason_code) as verification_count_variance_reason_code,
    group_concat(verification_count_variance_comment)     as verification_count_variance_comment
from cycle_count_details
group by cycle_count_id, product_id
    );
