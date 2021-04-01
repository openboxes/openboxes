CREATE OR REPLACE VIEW stocklist_item_list AS (
   	SELECT
        requisition_item.id as id,
        product.id as product_id,
        requisition.id as stocklist_id,
        requisition.name as name,
        location.id as location_id,
        location.name as location_name,
        location_group.id as location_group_id,
        location_group.name as location_group_name,
        manager.id as manager_id,
        CONCAT(manager.first_name, ' ', manager.last_name) as manager_name,
        manager.email as manager_email,
        product.unit_of_measure as uom,
        requisition_item.quantity as max_quantity,
        requisition.replenishment_period,
        CEIL((requisition_item.quantity / requisition.replenishment_period) * 30) as monthly_demand
    FROM requisition_item
    JOIN requisition ON requisition_item.requisition_id = requisition.id
    JOIN location ON requisition.origin_id = location.id
    LEFT OUTER JOIN location_group ON location.location_group_id = location_group.id
    LEFT OUTER JOIN person as manager ON manager.id = requisition.requested_by_id
    LEFT OUTER JOIN product ON product.id = requisition_item.product_id
    WHERE requisition.is_published = TRUE AND requisition.is_template = TRUE
)
