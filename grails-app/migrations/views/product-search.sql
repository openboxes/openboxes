CREATE OR REPLACE VIEW product_search AS (
SELECT
  concat(p.id, ' ', p_a.location_id)            as id,
  p.id                                          as product_id,
  p_a.location_id                               as location_id,
  p.product_type_id                             as type_id,
  CASE
  	WHEN GROUP_CONCAT(p_t_s_a.product_activity_code, ' ') is null THEN FALSE -- concat with ' ' for easier check, because other codes might include SEARCHABLE key word in code
    WHEN GROUP_CONCAT(p_t_s_a.product_activity_code, ' ') like '%SEARCHABLE %' THEN TRUE
    WHEN GROUP_CONCAT(p_t_s_a.product_activity_code, ' ') not like '%SEARCHABLE %' THEN FALSE
  END                                           as is_searchable_type,
  ifnull(sum(quantity_on_hand), 0)              as quantity_on_hand,
  ifnull(sum(quantity_allocated), 0)            as quantity_allocated,
  ifnull(sum(quantity_on_hold), 0)              as quantity_on_hold,
  ifnull(sum(quantity_available_to_promise), 0) as quantity_available_to_promise
FROM product p
  left outer JOIN product_availability p_a on p.id = p_a.product_id
  left outer JOIN product_type p_t on p_t.id = p.product_type_id
  left outer join product_type_supported_activities p_t_s_a on p_t_s_a.product_type_id = p_t.id
GROUP BY p.id, p_a.location_id);
