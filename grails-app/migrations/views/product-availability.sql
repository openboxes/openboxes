DROP TABLE product_availability;
CREATE TABLE product_availability AS
SELECT
  uuid_short() as id,
  location_id,
  product_id,
  bin_location_id,
  inventory_item_id,
  quantity_on_hand
from inventory_snapshot
WHERE date = date(now())+1;
ALTER TABLE product_availability ADD PRIMARY KEY (id);
ALTER TABLE product_availability ADD FOREIGN KEY (location_id) REFERENCES location (id);
ALTER TABLE product_availability ADD FOREIGN KEY (bin_location_id) REFERENCES location (id);
ALTER TABLE product_availability ADD FOREIGN KEY (product_id) REFERENCES product (id);
ALTER TABLE product_availability ADD FOREIGN KEY (inventory_item_id) REFERENCES inventory_item (id);
