CREATE OR REPLACE VIEW on_order_summary AS
/* this select returns all rows for items that are in orders */
(SELECT a.product_id,
        a.destination_id,
        a.quantity_ordered_not_shipped,
        b.quantity_shipped_not_received
 FROM on_order_order_item_summary a
          LEFT OUTER JOIN on_order_shipment_item_summary b
                          ON a.product_id = b.product_id AND a.destination_id = b.destination_id)
/* in order to get all rows for items that are in both order and shipment */
/* and those that are either in order or shipment, we have to combine results from two selects */
UNION
(
/* this select returns all rows for items that are in shipments */
    SELECT b.product_id,
           b.destination_id,
           a.quantity_ordered_not_shipped,
           b.quantity_shipped_not_received
    FROM on_order_order_item_summary a
             RIGHT OUTER JOIN on_order_shipment_item_summary b
                              ON b.product_id = a.product_id AND
                                 b.destination_id = a.destination_id)
