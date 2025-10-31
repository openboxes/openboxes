create or replace view product_receipt_history as
(
select shipment.destination_id as facility_id,
       receipt_item.product_id,
       receipt_item.inventory_item_id,
       actual_delivery_date    as date_received
from receipt_item
         join receipt on receipt_item.receipt_id = receipt.id
         join shipment on receipt.shipment_id = shipment.id
         join location on shipment.destination_id = location.id
    );