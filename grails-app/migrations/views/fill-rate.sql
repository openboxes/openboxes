CREATE OR REPLACE VIEW fill_rate AS
SELECT r.id,
       r.status,
       ri.quantity,
       ri.quantity_canceled,
       ri.cancel_reason_code,
       ri.requisition_item_type,
       product.id                AS product_id,
       product.product_code,
       product.name              AS product_name,
       t.transaction_date,
       MONTH(t.transaction_date) AS month,
       YEAR(t.transaction_date)  AS year,
       origin.id                 AS origin_id,
       origin.name               AS origin_name,
       destination.id            AS destination_id,
       destination.name          AS destination_name,
       CASE
           WHEN il.status = 'FORMULARY' THEN TRUE
           ELSE FALSE
           END                   AS formulary,
       CASE
           WHEN
                   ri.quantity_canceled > 0
                   AND ri.cancel_reason_code IN ('STOCKOUT', 'LOW_STOCK', 'COULD_NOT_LOCATE')
               THEN
               0
           ELSE 1
           END                   AS fill_rate
FROM requisition AS r
         JOIN
     requisition_item ri ON r.id = ri.requisition_id
         JOIN
     location AS origin ON origin.id = r.origin_id
         JOIN
     location AS destination ON destination.id = r.destination_id
         JOIN
     product AS product ON product.id = ri.product_id
         LEFT OUTER JOIN
     transaction AS t ON t.requisition_id = r.id
         LEFT OUTER JOIN
     inventory_level AS il ON (il.inventory_id = origin.inventory_id
         AND il.product_id = product.id)
WHERE ri.requisition_item_type = 'ORIGINAL'
  AND r.status = 'ISSUED';
