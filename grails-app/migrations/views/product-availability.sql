CREATE OR REPLACE VIEW product_availability AS
SELECT
  uuid_short() as id,
  location_id,
  product_id,
  bin_location_id,
  inventory_item_id,
  quantity_on_hand
from inventory_snapshot
WHERE date = date(now())+1;
