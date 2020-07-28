CREATE OR REPLACE VIEW product_availability AS
SELECT
  location_id,
  product_id,
  lot_number,
  bin_location_name,
  bin_location_id,
  inventory_item_id,
  quantity_on_hand,
  last_updated,
  date_created
from inventory_snapshot
WHERE date = date(now())+1;
