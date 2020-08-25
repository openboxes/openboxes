CREATE OR REPLACE VIEW product_stocklist AS
    SELECT
        origin_id,
        product_id,
        IFNULL(CEIL(SUM(quantity / replenishment_period * 30)),
                0) AS quantity_demand
    FROM
        requisition_item
            JOIN
        requisition ON requisition.id = requisition_item.requisition_id
    WHERE
        requisition.is_template = TRUE
    GROUP BY origin_id , product_id;
