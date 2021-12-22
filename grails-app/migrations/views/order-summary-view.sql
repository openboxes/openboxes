CREATE OR REPLACE VIEW order_item_status AS
    SELECT order_id,
        order_number,
        product_code,
        quantity_ordered,
        quantity_shipped,
        order_status,
        (quantity_ordered > 0)                 AS shipment_ordered,
        (quantity_shipped >= quantity_ordered) AS shipped, 				    -- indicates if item was shipped
        (quantity_shipped > 0)  			         AS partially_shipped 	-- indicates if item was partially shipped
    FROM (
        SELECT order.id                 AS order_id,
            `order`.order_number        AS order_number,
            order.status                AS order_status,
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
        order_number,
        quantity_ordered,
        quantity_received,
        order_status,
        shipment_status,
        (quantity_ordered > 0)                  AS receipt_ordered,
        (quantity_received >= quantity_ordered) AS received
    FROM (
        SELECT order.id                                             AS order_id,
            `order`.order_number                                    AS order_number,
            order.status                                            AS order_status,
            order_item.id                                           AS order_item_id,
            product.product_code                                    AS product_code,
            SUM(order_item.quantity * order_item.quantity_per_uom)  AS quantity_ordered,
            SUM(shipment_item.quantity)                             AS quantity_received,
            shipment.current_status		                              AS shipment_status
        FROM `order`
            LEFT OUTER JOIN order_item ON order.id = order_item.order_id
            LEFT OUTER JOIN product ON order_item.product_id = product.id
            LEFT OUTER JOIN order_shipment ON order_item.id = order_shipment.order_item_id
            LEFT OUTER JOIN shipment_item ON shipment_item.id = order_shipment.shipment_item_id
            LEFT OUTER JOIN shipment ON shipment.id = shipment_item.shipment_id
        WHERE `order`.order_type_id = 'PURCHASE_ORDER'
          AND order_item.order_item_status_code != 'CANCELLED'
          AND shipment.current_status = 'RECEIVED'
        GROUP BY `order`.id, `order`.order_number, product.product_code, order_item.id, shipment.id
    )
AS order_receipt_status;

CREATE OR REPLACE VIEW order_payment_status_from_shipments AS
    SELECT
        order_id,
        order_number,
        quantity_ordered,
        shipment_item_quantity_invoiced,
        order_status,
        (quantity_ordered > 0)                                AS shipment_payment_ordered,
        (shipment_item_quantity_invoiced >= quantity_ordered) AS shipment_item_invoiced,
        invoice_submitted
    FROM (
        SELECT order.id                                               AS order_id,
            `order`.order_number                                      AS order_number,
            order.status                                              AS order_status,
            order_item.id                                             AS order_item_id,
            product.product_code                                      AS product_code,
            SUM(order_item.quantity * order_item.quantity_per_uom)    AS quantity_ordered,
            SUM(invoice_item.quantity)                                AS shipment_item_quantity_invoiced,
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

CREATE OR REPLACE VIEW order_summary AS (
    SELECT id,
           order_number,
           order_status,
           shipment_status,
           receipt_status,
           payment_status,
           COALESCE(payment_status, receipt_status, shipment_status, order_status) AS derived_status
    FROM (
        SELECT `order`.id as id,
            `order`.order_number,
            `order`.status as order_status,
            CASE
                WHEN (IFNULL(SUM(shipment_ordered), 0) + IFNULL(SUM(shipped), 0) = 0) THEN NULL
                WHEN (IFNULL(SUM(shipment_ordered), 0) + IFNULL(SUM(partially_shipped), 0) = 0) THEN NULL
                WHEN (IFNULL(SUM(shipment_ordered), 0) = IFNULL(SUM(shipped), 0)) THEN 'SHIPPED'
                WHEN (IFNULL(SUM(shipment_ordered), 0) > 0 AND IFNULL(SUM(partially_shipped), 0) > 0) THEN 'PARTIALLY_SHIPPED'
                ELSE NULL
            END AS shipment_status,
            IFNULL(SUM(receipt_ordered), 0) AS total_receipt_ordered,
            IFNULL(SUM(received), 0)        AS total_received,
            CASE
                WHEN (IFNULL(SUM(receipt_ordered), 0) + IFNULL(SUM(received), 0) = 0) THEN NULL
                WHEN (IFNULL(SUM(receipt_ordered), 0) = IFNULL(SUM(received), 0)) THEN 'RECEIVED'
                WHEN (IFNULL(SUM(receipt_ordered), 0) > 0 AND IFNULL(SUM(received), 0) > 0) THEN 'PARTIALLY_RECEIVED'
                ELSE NULL
            END AS receipt_status,
            IFNULL(SUM(adjustment_quantity_invoiced), 0)  AS total_adjustments,
            IFNULL(SUM(adjustment_invoiced), 0)           AS total_adjustments_invoiced,
            IFNULL(SUM(shipment_payment_ordered), 0)      AS total_shipment_payment_ordered,
            IFNULL(SUM(shipment_item_invoiced), 0)        AS total_invoiced,
            CASE
                WHEN (IFNULL(SUM(shipment_payment_ordered), 0) + IFNULL(SUM(adjustment_quantity_invoiced), 0) = 0) THEN NULL
                WHEN (IFNULL(SUM(shipment_payment_ordered), 0) + IFNULL(SUM(adjustment_quantity_invoiced), 0) = IFNULL(SUM(shipment_item_invoiced), 0) + IFNULL(SUM(adjustment_invoiced), 0)) THEN 'INVOICED'
                WHEN (IFNULL(SUM(shipment_payment_ordered), 0) + IFNULL(SUM(adjustment_quantity_invoiced), 0) > 0 AND IFNULL(SUM(shipment_item_invoiced), 0) + IFNULL(SUM(adjustment_invoiced), 0) > 0) THEN 'PARTIALLY_INVOICED'
                WHEN (IFNULL(sum(order_payment_status_from_shipments.quantity_ordered), 0) > 0 and IFNULL(sum(order_payment_status_from_shipments.shipment_item_quantity_invoiced), 0) > 0) THEN 'PARTIALLY_INVOICED'
                ELSE NULL
            END AS payment_status
        FROM `order`
            LEFT OUTER JOIN order_item_status on order_item_status.order_id = `order`.id
            LEFT OUTER JOIN order_receipt_status on order_receipt_status.order_id = `order`.id
            LEFT OUTER JOIN order_payment_status_from_shipments on order_payment_status_from_shipments.order_id = `order`.id
            LEFT OUTER JOIN order_payment_status_from_adjustments on order_payment_status_from_adjustments.order_id = `order`.id
        GROUP BY `order`.id, `order`.order_number, `order`.status
    ) AS order_summary);
