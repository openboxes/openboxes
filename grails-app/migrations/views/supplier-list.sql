CREATE OR REPLACE VIEW supplier AS (
	SELECT
		location.id AS id,
		location.name AS name,
		location.organization_id,
		COUNT(distinct purchase_order.id) AS pending_orders_count,
        COUNT(distinct shipment.id) AS pending_shipments_count
    FROM location
	JOIN location_type
		ON location_type.id = location.location_type_id
    LEFT OUTER JOIN `order` AS purchase_order
		ON purchase_order.origin_id = location.id
		AND purchase_order.status NOT IN ('COMPLETED', 'CANCELED', 'REJECTED')
		AND purchase_order.order_type_id = 'PURCHASE_ORDER'
	LEFT OUTER JOIN shipment
		ON shipment.origin_id = location.id
		AND shipment.current_status != 'RECEIVED'
    WHERE location_type.location_type_code = 'SUPPLIER'
    GROUP BY id
)
