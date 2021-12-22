CREATE OR REPLACE VIEW product_expiry_summary AS
SELECT
    p_a.product_id,
    p_a.location_id,
    i.expiration_date,
    SUM(p_a.quantity_on_hand) AS quantity_on_hand,
    IFNULL(demand.average_daily_demand, 0) AS average_daily_demand
FROM product_availability AS p_a
JOIN inventory_item i ON i.id = p_a.inventory_item_id
LEFT JOIN (
            SELECT
               product_id,
               origin_id AS location_id,
               SUM(quantity_demand) / datediff(CURRENT_DATE, MIN(COALESCE(date_issued, date_requested))) AS average_daily_demand
            FROM product_demand_details
            GROUP BY product_id, location_id
        ) demand ON demand.product_id = p_a.product_id AND demand.location_id = p_a.location_id
WHERE i.expiration_date IS NOT NULL
GROUP BY p_a.product_id, p_a.location_id, i.expiration_date, demand.average_daily_demand;
