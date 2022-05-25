CREATE OR REPLACE VIEW internal_location AS
SELECT 
	inner_query.id, 
	inner_query.location_id, 
    inner_query.location_number, 
    inner_query.location_name, 
    inner_query.parent_location_id, 
    inner_query.location_type_id, 
    inner_query.supported_activities_string,    
    inner_query.is_locked,
    case
        when quantity_on_hand <= 0 then true
		when allows_multiple_items and quantity_on_hand > 0 then true
		when not allows_multiple_items and quantity_on_hand > 0 then false
    end as is_available,
    inner_query.allows_multiple_items,
    inner_query.product_count,
    inner_query.inventory_item_count,
    inner_query.quantity_on_hand,
    inner_query.quantity_available
FROM 
(
	SELECT 
		location.id, 
		location.id as location_id,
		location.location_number as location_number, 
		location.name as location_name, 
		location.parent_location_id,
		location_type.id as location_type_id,
		location_supported_activities.supported_activities as supported_activities_string,
		(location_supported_activities.supported_activities like '%LOCKED%') as is_locked,    
		(NOT location_supported_activities.supported_activities like '%PUTAWAY_STRATEGY_SINGLE_LPN%') as allows_multiple_items,
		count(distinct case when product_availability.quantity_on_hand > 0 THEN product_availability.product_id END) as product_count,
		count(distinct case when product_availability.quantity_on_hand > 0 THEN product_availability.inventory_item_id END) as inventory_item_count,
		ifnull(sum(product_availability.quantity_on_hand), 0) as quantity_on_hand,
		ifnull(sum(product_availability.quantity_available_to_promise), 0) as quantity_available    
	FROM
		location
			JOIN
		location_type ON location_type.id = location.location_type_id
			LEFT OUTER JOIN         
		product_availability on (location.id = product_availability.bin_location_id and product_availability.location_id = location.parent_location_id)
		
		LEFT OUTER JOIN (
			SELECT 
				location.id as location_id,
				IFNULL(group_concat(location_supported_activities.supported_activities_string), 
					group_concat(location_type_supported_activities.supported_activities_string)) as supported_activities

			from location 
			left outer join location_supported_activities on location.id = location_supported_activities.location_id
			left outer join location_type_supported_activities on location.location_type_id = location_type_supported_activities.location_type_id
			group by location.id) AS location_supported_activities ON location_supported_activities.location_id = location.id
		
	WHERE parent_location_id is NOT NULL
		AND location_type.location_type_code IN ('INTERNAL' , 'BIN_LOCATION')

	group by location.id, location.location_number, location.name, location.parent_location_id, location_type.id
) as inner_query;
