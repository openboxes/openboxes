CREATE OR REPLACE VIEW order_item_status AS
    SELECT
        order_id,
        order_number,
        -- Using MAX as a required replacement for aggregate function. We cannot use SUM on quantity ordered, because
        -- on the multiple shipment case, the quantity ordered value would be multiplied by the amount of rows
        IFNULL(MAX(quantity_ordered), 0)    AS quantity_ordered,
        IFNULL(SUM(quantity_shipped), 0)    AS quantity_shipped,
        order_item_id
    FROM (
        SELECT
            `order`.id                  AS order_id,
            `order`.order_number        AS order_number,
            order_item.id               AS order_item_id,
            CASE
                WHEN order_item.order_item_status_code = 'CANCELED' THEN 0
                -- Using MAX as an aggregate function. We cannot use SUM on quantity ordered, because on the multiple
                -- shipment item (split) case, the quantity ordered value would be multiplied by the amount of rows
                ELSE IFNULL(MAX(order_item.quantity), 0)
            END AS quantity_ordered,
            CASE
                -- quantity divided by order_item.quantity_per_uom to match other quantities that are in uom
                WHEN shipment.current_status IN ('SHIPPED', 'PARTIALLY_RECEIVED', 'RECEIVED') THEN SUM(shipment_item.quantity / order_item.quantity_per_uom)
                ELSE 0
          	END AS quantity_shipped
        FROM `order`
            LEFT OUTER JOIN order_item ON order.id = order_item.order_id
            LEFT OUTER JOIN order_shipment ON order_item.id = order_shipment.order_item_id
            LEFT OUTER JOIN shipment_item ON shipment_item.id = order_shipment.shipment_item_id
            LEFT OUTER JOIN shipment ON shipment.id = shipment_item.shipment_id
        WHERE `order`.order_type_id = 'PURCHASE_ORDER'
        GROUP BY `order`.id, order_item.id, shipment.id
    )
AS order_item_status GROUP BY order_id, order_item_id;

CREATE OR REPLACE VIEW order_receipt_status AS
    SELECT
        order_id,
        order_item_id,
        order_number,
        IFNULL(SUM(quantity_received), 0) AS quantity_received,
        IFNULL(SUM(quantity_canceled), 0) AS quantity_canceled
    FROM (
        SELECT
            `order`.id               AS order_id,
            `order`.order_number     AS order_number,
            order_item.id            AS order_item_id,
            -- quantity divided by order_item.quantity_per_uom to match other quantities that are in uom
            SUM(receipt_item.quantity_received / order_item.quantity_per_uom)   AS quantity_received,
            SUM(receipt_item.quantity_canceled / order_item.quantity_per_uom)   AS quantity_canceled
        FROM `order`
            LEFT OUTER JOIN order_item ON `order`.id = order_item.order_id
            LEFT OUTER JOIN order_shipment ON order_item.id = order_shipment.order_item_id
            LEFT OUTER JOIN shipment_item ON shipment_item.id = order_shipment.shipment_item_id
            LEFT OUTER JOIN shipment ON shipment.id = shipment_item.shipment_id
            LEFT OUTER JOIN receipt_item ON receipt_item.shipment_item_id = shipment_item.id
        WHERE `order`.order_type_id = 'PURCHASE_ORDER'
          AND order_item.order_item_status_code != 'CANCELLED'
          AND shipment.current_status = 'RECEIVED' OR shipment.current_status = 'PARTIALLY_RECEIVED'
        GROUP BY `order`.id, `order`.order_number, order_item.id, shipment.id
    )
AS order_receipt_status GROUP BY order_item_id;

CREATE OR REPLACE VIEW order_item_payment_status AS
    SELECT
        order_id,
        order_number,
        order_item_id,
        IFNULL(SUM(quantity_ordered), 0)  	AS quantity_ordered,
        IFNULL(SUM(quantity_invoiced), 0) 	AS quantity_invoiced
    FROM (
        SELECT
            order_item.id                                               AS order_item_id,
            `order`.id                                                  AS order_id,
            `order`.order_number                                        AS order_number,
            order_item.quantity                                   		AS quantity_ordered, -- in uom
            SUM(invoice_item.quantity)                                  AS quantity_invoiced -- in uom
        FROM `order`
            LEFT OUTER JOIN order_item ON order.id = order_item.order_id
            LEFT OUTER JOIN order_shipment ON order_item.id = order_shipment.order_item_id
            LEFT OUTER JOIN shipment_item ON shipment_item.id = order_shipment.shipment_item_id
            LEFT OUTER JOIN shipment_invoice ON shipment_invoice.shipment_item_id = shipment_item.id
            LEFT OUTER JOIN invoice_item ON invoice_item.id = shipment_invoice.invoice_item_id
            LEFT OUTER JOIN invoice ON invoice.id = invoice_item.invoice_id
        WHERE `order`.order_type_id = 'PURCHASE_ORDER'
          AND order_item.order_item_status_code != 'CANCELLED'
          AND (invoice.invoice_type_id != '5' OR invoice.invoice_type_id IS NULL)
          AND invoice.date_posted IS NOT NULL
        GROUP BY `order`.id, order_item.id, invoice_item.id, shipment_item.id
    )
AS order_item_payment_status
GROUP BY order_item_id;

CREATE OR REPLACE VIEW order_adjustment_payment_status AS
    SELECT
        order_id,
        order_number,
        adjustment_id,
        invoice_item_id,
        order_status,
        CASE
            WHEN order_adjustment_canceled IS TRUE THEN 0
            ELSE 1
        END AS quantity_ordered,
        quantity_invoiced
    FROM (
        SELECT
            `order`.id                          AS order_id,
            order_adjustment.id 		        AS adjustment_id,
            order_adjustment.canceled 		    AS order_adjustment_canceled,
            `order`.order_number                AS order_number,
            order.status                        AS order_status,
            invoice_item.id				        AS invoice_item_id,
            CASE
                WHEN invoice.date_posted IS NOT NULL THEN IFNULL(invoice_item.quantity, 0)
                ELSE 0
            END AS quantity_invoiced
        FROM `order`
            LEFT OUTER JOIN order_adjustment ON order_adjustment.order_id = `order`.id
            LEFT OUTER JOIN order_adjustment_invoice ON order_adjustment_invoice.order_adjustment_id = order_adjustment.id
            LEFT OUTER JOIN invoice_item ON invoice_item.id = order_adjustment_invoice.invoice_item_id
            LEFT OUTER JOIN invoice ON invoice.id = invoice_item.invoice_id
        WHERE `order`.order_type_id = 'PURCHASE_ORDER'
          AND (invoice.invoice_type_id != '5' OR invoice.invoice_type_id IS NULL)
        GROUP BY `order`.id, `order`.order_number, invoice_item.id, order_adjustment.id
    )
AS order_adjustment_payment_status;

CREATE OR REPLACE VIEW order_item_summary AS (
    SELECT
        order_item_id AS id,
        order_id,
        order_number,
        product_id,
        quantity,
        order_item_status,
        quantity_uom_id,
        quantity_per_uom,
        unit_price,
        quantity_ordered     AS quantity_ordered,
        quantity_shipped     AS quantity_shipped,
        quantity_received    AS quantity_received,
        quantity_canceled    AS quantity_canceled,
        quantity_invoiced    AS quantity_invoiced,
        IF(quantity_ordered > 0, 1, 0) AS is_item_fully_ordered,
        IF(quantity_shipped > 0 AND quantity_shipped = quantity_ordered, 1, 0) AS is_item_fully_shipped,
        IF(quantity_received + quantity_canceled > 0 AND quantity_received + quantity_canceled = quantity_ordered, 1, 0)  AS is_item_fully_received,
        IF(quantity_invoiced > 0 AND quantity_invoiced = quantity_ordered, 1, 0) AS is_item_fully_invoiced,
        COALESCE(payment_status, receipt_status, shipment_status, order_item_status, order_status) AS derived_status
    FROM (
        SELECT
            order_item.id                                       AS order_item_id,
            `order`.id                                          AS order_id,
            `order`.order_number                                AS order_number,
            `order`.status                                      AS order_status,
            order_item.product_id                               AS product_id,
            quantity,
            order_item.order_item_status_code                   AS order_item_status,
            quantity_uom_id,
            quantity_per_uom,
            unit_price,
            IFNULL(SUM(order_item_status.quantity_ordered), 0)             AS quantity_ordered,
            IFNULL(SUM(order_item_status.quantity_shipped), 0)             AS quantity_shipped,
            IFNULL(SUM(order_receipt_status.quantity_received), 0)         AS quantity_received,
            IFNULL(SUM(order_receipt_status.quantity_canceled), 0)         AS quantity_canceled,
            IFNULL(SUM(order_item_payment_status.quantity_invoiced), 0)    AS quantity_invoiced,
            CASE
                WHEN SUM(order_item_status.quantity_ordered) + SUM(order_item_status.quantity_shipped) = 0 THEN NULL
                WHEN SUM(order_item_status.quantity_ordered) = SUM(order_item_status.quantity_shipped) THEN 'SHIPPED'
                WHEN SUM(order_item_status.quantity_ordered) > 0 AND SUM(order_item_status.quantity_shipped) > 0 THEN 'PARTIALLY_SHIPPED'
                ELSE NULL
            END AS shipment_status,
            CASE
	            WHEN SUM(order_receipt_status.quantity_received) = 0 THEN NULL
                WHEN (SUM(order_item_status.quantity_ordered) - SUM(order_receipt_status.quantity_canceled)) <= SUM(order_receipt_status.quantity_received) THEN 'RECEIVED'
                WHEN (SUM(order_item_status.quantity_ordered) - SUM(order_receipt_status.quantity_canceled)) > SUM(order_receipt_status.quantity_received) AND SUM(order_receipt_status.quantity_received) > 0 THEN 'PARTIALLY_RECEIVED'
                ELSE NULL
            END AS receipt_status,
            CASE
                WHEN SUM(order_item_status.quantity_ordered) + SUM(order_item_payment_status.quantity_invoiced) = 0 THEN NULL
                WHEN SUM(order_item_status.quantity_ordered) = SUM(order_item_payment_status.quantity_invoiced) THEN 'INVOICED'
                WHEN SUM(order_item_status.quantity_ordered) > SUM(order_item_payment_status.quantity_invoiced) AND SUM(order_item_payment_status.quantity_invoiced) > 0 THEN 'PARTIALLY_INVOICED'
                ELSE NULL
            END AS payment_status
        FROM order_item
        	JOIN `order` ON order_item.order_id = `order`.id
            LEFT OUTER JOIN order_item_status ON order_item_status.order_item_id = order_item.id
            LEFT OUTER JOIN order_receipt_status ON order_receipt_status.order_item_id = order_item.id
            LEFT OUTER JOIN order_item_payment_status ON order_item_payment_status.order_item_id = order_item.id
        WHERE `order`.order_type_id = 'PURCHASE_ORDER'
        GROUP BY order_item.id
    ) AS order_item_summary);

CREATE OR REPLACE VIEW order_summary AS (
    SELECT
        id,
        id as order_id,
        quantity_ordered,
        adjustments_count,
        quantity_shipped,
        quantity_received,
        quantity_canceled,
        quantity_invoiced,
        adjustments_invoiced,
        items_ordered,
        items_shipped,
        items_received,
        items_invoiced,
        order_status,
        shipment_status,
        receipt_status,
        payment_status,
        CASE
            WHEN  shipment_status = 'SHIPPED' AND receipt_status = 'RECEIVED' AND payment_status = 'INVOICED' THEN 'COMPLETED'
            ELSE COALESCE(payment_status, receipt_status, shipment_status, order_status)
        END AS derived_status
    FROM (
        SELECT
            id,
            order_status,
            SUM(items_and_adjustments_union.quantity_ordered)    AS quantity_ordered,
            SUM(items_and_adjustments_union.adjustments_count) 		AS adjustments_count,
            SUM(items_and_adjustments_union.quantity_shipped)    AS quantity_shipped,
            SUM(items_and_adjustments_union.quantity_received)   AS quantity_received,
            SUM(items_and_adjustments_union.quantity_canceled)   AS quantity_canceled,
            SUM(items_and_adjustments_union.quantity_invoiced)   AS quantity_invoiced,
            SUM(items_and_adjustments_union.items_ordered)    AS items_ordered,
            SUM(items_and_adjustments_union.items_shipped)    AS items_shipped,
            SUM(items_and_adjustments_union.items_received)   AS items_received,
            SUM(items_and_adjustments_union.items_invoiced)   AS items_invoiced,
            SUM(items_and_adjustments_union.adjustments_invoiced)   AS adjustments_invoiced,
            CASE
                WHEN (SUM(items_and_adjustments_union.quantity_ordered) + SUM(items_and_adjustments_union.quantity_shipped)) = 0 THEN NULL
                WHEN SUM(items_and_adjustments_union.quantity_ordered) = SUM(items_and_adjustments_union.quantity_shipped) THEN 'SHIPPED'
                WHEN SUM(items_and_adjustments_union.quantity_ordered) > 0 AND SUM(items_and_adjustments_union.quantity_shipped) > 0 THEN 'PARTIALLY_SHIPPED'
                ELSE NULL
            END AS shipment_status,
            CASE
	            WHEN SUM(items_and_adjustments_union.quantity_received) = 0 THEN NULL
                WHEN (SUM(items_and_adjustments_union.quantity_ordered) - SUM(items_and_adjustments_union.quantity_canceled)) > SUM(items_and_adjustments_union.quantity_received) AND SUM(items_and_adjustments_union.quantity_received) > 0 THEN 'PARTIALLY_RECEIVED'
                WHEN (SUM(items_and_adjustments_union.quantity_ordered) - SUM(items_and_adjustments_union.quantity_canceled)) <= SUM(items_and_adjustments_union.quantity_received) THEN 'RECEIVED'
                ELSE NULL
            END AS receipt_status,
            CASE
                WHEN (SUM(items_and_adjustments_union.quantity_ordered) + SUM(items_and_adjustments_union.adjustments_count)) = 0 THEN NULL
                WHEN (SUM(items_and_adjustments_union.quantity_ordered) + SUM(items_and_adjustments_union.adjustments_count)) = (SUM(items_and_adjustments_union.quantity_invoiced) + SUM(items_and_adjustments_union.adjustments_invoiced)) THEN 'INVOICED'
                WHEN (SUM(items_and_adjustments_union.quantity_ordered) + SUM(items_and_adjustments_union.adjustments_count)) > 0 AND (SUM(items_and_adjustments_union.quantity_invoiced) + SUM(items_and_adjustments_union.adjustments_invoiced) > 0) THEN 'PARTIALLY_INVOICED'
                ELSE NULL
            END AS payment_status
        FROM (
            -- There is need to make an union of order item summary and order adjustments payment status
            -- to not get duplicated quantities and to get proper payment status for order summary
            SELECT
                `order`.id                                              AS id,
                `order`.status                                          AS order_status,
                IFNULL(SUM(order_item_summary.quantity_ordered), 0)     AS quantity_ordered,
                SUM(order_item_summary.is_item_fully_ordered)           AS items_ordered,
                0									 		            AS adjustments_count,
                IFNULL(SUM(order_item_summary.quantity_shipped), 0)     AS quantity_shipped,
                SUM(order_item_summary.is_item_fully_shipped)           AS items_shipped,
                IFNULL(SUM(order_item_summary.quantity_received), 0)    AS quantity_received,
                SUM(order_item_summary.is_item_fully_received)          AS items_received,
                IFNULL(SUM(order_item_summary.quantity_canceled), 0)    AS quantity_canceled,
                IFNULL(SUM(order_item_summary.quantity_invoiced), 0)    AS quantity_invoiced,
                SUM(order_item_summary.is_item_fully_invoiced)          AS items_invoiced,
                0   										            AS adjustments_invoiced
            FROM `order`
                LEFT OUTER JOIN order_item ON order_item.order_id = `order`.id
                LEFT OUTER JOIN order_item_summary ON order_item_summary.id = order_item.id
            WHERE `order`.order_type_id = 'PURCHASE_ORDER'
            GROUP BY `order`.id
            UNION
            SELECT
                `order`.id                                  			            AS id,
                `order`.status                              			            AS order_status,
                0    													            AS quantity_ordered,
                0                                                                   AS items_ordered,
                IFNULL(SUM(order_adjustment_payment_status.quantity_ordered), 0)	AS adjustments_count,
                0    													            AS quantity_shipped,
                0                                                                   AS items_shipped,
                0   													            AS quantity_received,
                0                                                                   AS items_received,
                0   													            AS quantity_canceled,
                0   													            AS quantity_invoiced,
                0                                                                   AS items_invoiced,
                SUM(order_adjustment_payment_status.quantity_invoiced)	            AS adjustments_invoiced
            FROM `order`
                LEFT OUTER JOIN order_adjustment ON order_adjustment.order_id = `order`.id
                LEFT OUTER JOIN order_adjustment_payment_status ON order_adjustment_payment_status.adjustment_id = order_adjustment.id
            WHERE `order`.order_type_id = 'PURCHASE_ORDER'
            GROUP BY `order`.id
        ) AS items_and_adjustments_union GROUP BY id, order_status
    )
AS order_summary);
