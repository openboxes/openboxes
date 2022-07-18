CREATE OR REPLACE VIEW order_item_status AS
    SELECT
        order_id,
        order_number,
        product_code,
        quantity_ordered,
        quantity_shipped,
        order_item_id
    FROM (
        SELECT order.id                 AS order_id,
            `order`.order_number        AS order_number,
            order_item.id               AS order_item_id,
            product.product_code        AS product_code,
            order_item.quantity * order_item.quantity_per_uom    AS quantity_ordered, -- to compare with shipped quantity which is already multiplied by qty per uom
            CASE
              WHEN shipment.current_status IN ('SHIPPED', 'PARTIALLY_RECEIVED', 'RECEIVED') THEN SUM(shipment_item.quantity)
              ELSE 0
          	END AS quantity_shipped
        FROM `order`
            LEFT OUTER JOIN order_item ON order.id = order_item.order_id
            LEFT OUTER JOIN product ON order_item.product_id = product.id
            LEFT OUTER JOIN order_shipment ON order_item.id = order_shipment.order_item_id
            LEFT OUTER JOIN shipment_item ON shipment_item.id = order_shipment.shipment_item_id
            LEFT OUTER JOIN shipment ON shipment.id = shipment_item.shipment_id
        WHERE `order`.order_type_id = 'PURCHASE_ORDER'
          AND order_item.order_item_status_code != 'CANCELLED'
        GROUP BY `order`.id, `order`.order_number, product.product_code, order_item.id, shipment.current_status
    )
AS order_item_status;

CREATE OR REPLACE VIEW order_receipt_status AS
    SELECT
        order_id,
        order_item_id,
        order_number,
        quantity_ordered,
        quantity_received,
        quantity_canceled
    FROM (
        SELECT
            `order`.id                                              AS order_id,
            `order`.order_number                                    AS order_number,
            order_item.id                                           AS order_item_id,
            SUM(order_item.quantity * order_item.quantity_per_uom)  AS quantity_ordered,
            SUM(receipt_item.quantity_received)                     AS quantity_received,
            SUM(receipt_item.quantity_canceled)                     AS quantity_canceled
        FROM `order`
            LEFT OUTER JOIN order_item ON `order`.id = order_item.order_id
            LEFT OUTER JOIN product ON order_item.product_id = product.id
            LEFT OUTER JOIN order_shipment ON order_item.id = order_shipment.order_item_id
            LEFT OUTER JOIN shipment_item ON shipment_item.id = order_shipment.shipment_item_id
            LEFT OUTER JOIN shipment ON shipment.id = shipment_item.shipment_id
            LEFT OUTER JOIN receipt_item ON receipt_item.shipment_item_id = shipment_item.id
        WHERE `order`.order_type_id = 'PURCHASE_ORDER'
          AND order_item.order_item_status_code != 'CANCELLED'
          AND shipment.current_status = 'RECEIVED' OR shipment.current_status = 'PARTIALLY_RECEIVED'
        GROUP BY `order`.id, `order`.order_number, order_item.id, shipment.id
    )
AS order_receipt_status;

CREATE OR REPLACE VIEW order_payment_status_from_shipments AS
    SELECT
        order_id,
        order_number,
        order_item_id,
        quantity_ordered,
        shipment_item_quantity_invoiced,
        order_status,
        (quantity_ordered > 0)                                AS shipment_payment_ordered,
        (shipment_item_quantity_invoiced >= quantity_ordered) AS shipment_item_invoiced,
        invoice_submitted
    FROM (
        SELECT
            `order`.id                                                  AS order_id,
            `order`.order_number                                        AS order_number,
            `order`.status                                              AS order_status,
            order_item.id                                               AS order_item_id,
            product.product_code                                        AS product_code,
            SUM(order_item.quantity * order_item.quantity_per_uom)      AS quantity_ordered,
            SUM(invoice_item.quantity)                                  AS shipment_item_quantity_invoiced,
            invoice.date_submitted		                                AS invoice_submitted
        FROM `order`
            LEFT OUTER JOIN order_item ON order.id = order_item.order_id
            LEFT OUTER JOIN product ON order_item.product_id = product.id
            LEFT OUTER JOIN order_shipment ON order_item.id = order_shipment.order_item_id
            LEFT OUTER JOIN shipment_item ON shipment_item.id = order_shipment.shipment_item_id
            LEFT OUTER JOIN shipment_invoice ON shipment_invoice.shipment_item_id = shipment_item.id
            LEFT OUTER JOIN invoice_item ON invoice_item.id = shipment_invoice.invoice_item_id
            LEFT OUTER JOIN invoice ON invoice.id = invoice_item.invoice_id
        WHERE `order`.order_type_id = 'PURCHASE_ORDER'
          AND order_item.order_item_status_code != 'CANCELLED'
          AND invoice.date_submitted IS NOT NULL
        GROUP BY `order`.id, `order`.order_number, product.product_code, order_item.id, invoice.date_submitted
    )
AS order_payment_status_from_shipments;

CREATE OR REPLACE VIEW order_payment_status_from_adjustments AS
    SELECT order_id,
        order_number,
        adjustment_id,
        invoice_item_id,
        adjustment_quantity_invoiced,
        order_status,
        (adjustment_quantity_invoiced = 1) AS adjustment_invoiced,
        invoice_submitted
    FROM (
        SELECT order.id             AS order_id,
            order_adjustment.id 		AS adjustment_id,
            `order`.order_number    AS order_number,
            order.status            AS order_status,
            invoice_item.id				  AS invoice_item_id,
            invoice_item.quantity 	AS adjustment_quantity_invoiced,
            invoice.date_submitted	AS invoice_submitted
        FROM `order`
            LEFT OUTER JOIN order_item ON order.id = order_item.order_id
            LEFT OUTER JOIN product ON order_item.product_id = product.id
            LEFT OUTER JOIN order_adjustment ON order_adjustment.order_id = order.id
            LEFT OUTER JOIN order_adjustment_invoice ON order_adjustment_invoice.order_adjustment_id = order_adjustment.id
            LEFT OUTER JOIN invoice_item ON invoice_item.id = order_adjustment_invoice.invoice_item_id
            LEFT OUTER JOIN invoice ON invoice.id = invoice_item.invoice_id
        WHERE `order`.order_type_id = 'PURCHASE_ORDER'
          AND order_item.order_item_status_code != 'CANCELLED'
          AND invoice.date_submitted IS NOT NULL
        GROUP BY `order`.id, `order`.order_number, invoice_item.id, order_adjustment.id, invoice.date_submitted
    )
AS order_payment_status_from_adjustments;

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
        IFNULL(quantity_ordered, 0)     AS quantity_ordered,
        IFNULL(quantity_shipped, 0)     AS quantity_shipped,
        IFNULL(quantity_received, 0)    AS quantity_received,
        IFNULL(quantity_canceled, 0)    AS quantity_canceled,
        IFNULL(quantity_invoiced, 0)    AS quantity_invoiced,
        COALESCE(payment_status, receipt_status, shipment_status, order_item_status, order_status) AS derived_status
    FROM (
        SELECT
            order_item.id                                   AS order_item_id,
            `order`.id                                      AS order_id,
            `order`.order_number                            AS order_number,
            `order`.status                                  AS order_status,
            order_item.product_id                           AS product_id,
            quantity,
            order_item.order_item_status_code               AS order_item_status,
            quantity_uom_id,
            quantity_per_uom,
            unit_price,
            quantity * quantity_per_uom                     AS quantity_ordered,
            order_item_status.quantity_shipped              AS quantity_shipped,
            order_receipt_status.quantity_received          AS quantity_received,
            order_receipt_status.quantity_canceled          AS quantity_canceled,
            order_payment_status_from_shipments.shipment_item_quantity_invoiced        AS quantity_invoiced,
            CASE
                WHEN (IFNULL(SUM(order_item_status.quantity_ordered), 0) + IFNULL(SUM(order_item_status.quantity_shipped), 0) = 0) THEN NULL
                WHEN (IFNULL(SUM(order_item_status.quantity_ordered), 0) = IFNULL(SUM(order_item_status.quantity_shipped), 0)) THEN 'SHIPPED'
                WHEN (IFNULL(SUM(order_item_status.quantity_ordered), 0) > 0 AND IFNULL(SUM(order_item_status.quantity_shipped), 0) > 0) THEN 'PARTIALLY_SHIPPED'
                ELSE NULL
            END AS shipment_status,
            CASE
	            WHEN (IFNULL(SUM(order_receipt_status.quantity_received), 0) = 0) THEN NULL
                WHEN ((IFNULL(SUM(order_receipt_status.quantity_ordered), 0) - IFNULL(SUM(order_receipt_status.quantity_canceled), 0)) <= IFNULL(SUM(order_receipt_status.quantity_received), 0)) THEN 'RECEIVED'
                WHEN ((IFNULL(SUM(order_receipt_status.quantity_ordered), 0) - IFNULL(SUM(order_receipt_status.quantity_canceled), 0)) > IFNULL(SUM(order_receipt_status.quantity_received), 0) AND IFNULL(SUM(quantity_received), 0) > 0) THEN 'PARTIALLY_RECEIVED'
                ELSE NULL
            END AS receipt_status,
            CASE
                WHEN (IFNULL(SUM(order_payment_status_from_shipments.quantity_ordered), 0) = 0) THEN NULL
                WHEN (IFNULL(SUM(shipment_payment_ordered), 0) = IFNULL(SUM(shipment_item_invoiced), 0)) THEN 'INVOICED'
                WHEN (IFNULL(SUM(shipment_payment_ordered), 0)  > 0 AND IFNULL(SUM(shipment_item_invoiced), 0) > 0) THEN 'PARTIALLY_INVOICED'
                WHEN (IFNULL(sum(order_payment_status_from_shipments.quantity_ordered), 0) > 0 and IFNULL(sum(order_payment_status_from_shipments.shipment_item_quantity_invoiced), 0) > 0) THEN 'PARTIALLY_INVOICED'
                ELSE NULL
            END AS payment_status
        FROM order_item
        	JOIN `order` ON order_item.order_id = `order`.id
            LEFT OUTER JOIN order_item_status ON order_item_status.order_item_id = order_item.id
            LEFT OUTER JOIN order_receipt_status ON order_receipt_status.order_item_id = order_item.id
            LEFT OUTER JOIN order_payment_status_from_shipments ON order_payment_status_from_shipments.order_item_id = order_item.id
        WHERE `order`.order_type_id = 'PURCHASE_ORDER'
        GROUP BY order_item.id, order_item_status.quantity_shipped, order_receipt_status.quantity_received, order_receipt_status.quantity_canceled, order_payment_status_from_shipments.shipment_item_quantity_invoiced
    ) AS order_item_summary);

CREATE OR REPLACE VIEW order_summary AS (
    SELECT
        id,
        order_number,
        quantity_ordered,
        quantity_shipped,
        quantity_received,
        quantity_canceled,
        quantity_invoiced,
        order_status,
        shipment_status,
        receipt_status,
        payment_status,
        COALESCE(payment_status, receipt_status, shipment_status, order_status) AS derived_status
    FROM (
        SELECT
            `order`.id                                  AS id,
            `order`.order_number                        AS order_number,
            `order`.status                              AS order_status,
            SUM(order_item_summary.quantity_ordered)    AS quantity_ordered,
            SUM(order_item_summary.quantity_shipped)    AS quantity_shipped,
            SUM(order_item_summary.quantity_received)   AS quantity_received,
            SUM(order_item_summary.quantity_canceled)   AS quantity_canceled,
            SUM(order_item_summary.quantity_invoiced)   AS quantity_invoiced,
            CASE
                WHEN (IFNULL(SUM(order_item_summary.quantity_ordered) + SUM(order_item_summary.quantity_shipped), 0) = 0) THEN NULL
                WHEN (IFNULL(SUM(order_item_summary.quantity_ordered), 0) = IFNULL(SUM(order_item_summary.quantity_shipped), 0)) THEN 'SHIPPED'
                WHEN (IFNULL(SUM(order_item_summary.quantity_ordered), 0) > 0 AND IFNULL(SUM(order_item_summary.quantity_shipped), 0) > 0) THEN 'PARTIALLY_SHIPPED'
                ELSE NULL
            END AS shipment_status,
            CASE
	            WHEN (IFNULL(SUM(order_item_summary.quantity_received), 0) = 0) THEN NULL
                WHEN (IFNULL(SUM(order_item_summary.quantity_ordered), 0) - IFNULL(SUM(order_item_summary.quantity_canceled), 0)) > IFNULL(SUM(order_item_summary.quantity_received), 0) AND (IFNULL(SUM(order_item_summary.quantity_received), 0) > 0) THEN 'PARTIALLY_RECEIVED'
                WHEN (IFNULL(SUM(order_item_summary.quantity_ordered), 0) - IFNULL(SUM(order_item_summary.quantity_canceled), 0)) <= IFNULL(SUM(order_item_summary.quantity_received), 0) THEN 'RECEIVED'
                ELSE NULL
            END AS receipt_status,
            CASE
                WHEN (IFNULL(SUM(order_item_summary.quantity_ordered), 0) = 0) THEN NULL
                WHEN (IFNULL(SUM(order_item_summary.quantity_ordered), 0) = IFNULL(SUM(order_item_summary.quantity_invoiced), 0)) THEN 'INVOICED'
                WHEN (IFNULL(SUM(order_item_summary.quantity_ordered), 0)  > 0 AND IFNULL(SUM(order_item_summary.quantity_invoiced), 0) > 0) THEN 'PARTIALLY_INVOICED'
                ELSE NULL
            END AS payment_status
        FROM order_item
        	JOIN `order` ON order_item.order_id = `order`.id
            JOIN order_item_summary ON order_item_summary.id = order_item.id
        WHERE `order`.order_type_id = 'PURCHASE_ORDER'
        GROUP BY `order`.id
    )
AS order_summary);
