drop table if exists stock_take;
drop table if exists latest_stock_take;
drop table if exists latest_stock_take_partial;
drop table if exists latest_credits;
drop table if exists latest_debits;
# ---------------------------------------------------------------------------------------------
CREATE TABLE stock_take AS
select
	transaction_date as transaction_date,
	transaction.date_created as date_created,
	location.id as location_id,
	location.name as location_name,
	inventory_item.id as inventory_item_id,
	inventory_item.lot_number as lot_number,
	product.id as product_id,
	product.product_code as product_code,
	product.name as product_name,
	ifnull(quantity, 0) as quantity
from transaction
join transaction_entry on transaction.id = transaction_entry.transaction_id
join transaction_type on transaction.transaction_type_id = transaction_type.id
join inventory on inventory.id = transaction.inventory_id
join location on location.inventory_id = inventory.id
join inventory_item on transaction_entry.inventory_item_id = inventory_item.id
join product on inventory_item.product_id = product.id
where transaction_type.transaction_code = 'PRODUCT_INVENTORY'
group by date_created, location.id, inventory_item.id, product.id;
ALTER TABLE stock_take ADD FOREIGN KEY (location_id) REFERENCES location(id);
ALTER TABLE stock_take ADD FOREIGN KEY (inventory_item_id) REFERENCES inventory_item(id);
ALTER TABLE stock_take ADD FOREIGN KEY (product_id) REFERENCES product(id);
ALTER TABLE stock_take ADD INDEX (transaction_date);
ALTER TABLE stock_take ADD INDEX (lot_number);
ALTER TABLE stock_take ADD INDEX (product_code);
# ---------------------------------------------------------------------------------------------
create table latest_stock_take AS
select
	latest_stock_take.transaction_date,
	stock_take.location_id,
	stock_take.product_id,
	product_code,
	stock_take.inventory_item_id,
	lot_number,
	ifnull(sum(quantity),0) as quantity
from stock_take
inner join (
	SELECT max(transaction_date) as transaction_date, location_id, product_id
	FROM stock_take
	GROUP BY location_id, product_id
) as latest_stock_take
ON stock_take.product_id = latest_stock_take.product_id
AND stock_take.location_id = latest_stock_take.location_id
AND stock_take.transaction_date = latest_stock_take.transaction_date
GROUP BY location_id, product_id, inventory_item_id;
ALTER TABLE latest_stock_take ADD FOREIGN KEY (location_id) REFERENCES location(id);
ALTER TABLE latest_stock_take ADD FOREIGN KEY (inventory_item_id) REFERENCES inventory_item(id);
ALTER TABLE latest_stock_take ADD FOREIGN KEY (product_id) REFERENCES product(id);
ALTER TABLE latest_stock_take ADD UNIQUE INDEX (location_id, product_id, inventory_item_id);
ALTER TABLE latest_stock_take ADD INDEX (transaction_date);
ALTER TABLE latest_stock_take ADD INDEX (product_code);
ALTER TABLE latest_stock_take ADD INDEX (lot_number);
# ---------------------------------------------------------------------------------------------
create table latest_stock_take_partial AS
select
	transaction.transaction_date as transaction_date,
	transaction_type.transaction_code,
	location.id as location_id,
	location.name as location_name,
	inventory_item.id as inventory_item_id,
	inventory_item.lot_number as lot_number,
	product.id as product_id,
	product.product_code as product_code,
	product.name as product_name,
	ifnull(transaction_entry.quantity, 0) as quantity
from transaction
join transaction_entry on transaction.id = transaction_entry.transaction_id
join transaction_type on transaction.transaction_type_id = transaction_type.id
join inventory on inventory.id = transaction.inventory_id
join location on location.inventory_id = inventory.id
join inventory_item on transaction_entry.inventory_item_id = inventory_item.id
join product on inventory_item.product_id = product.id
join latest_stock_take on (latest_stock_take.product_id = product.id
	AND latest_stock_take.inventory_item_id = inventory_item.id
	AND latest_stock_take.location_id = location.id)
where transaction_type.transaction_code = 'INVENTORY'
and transaction.transaction_date > latest_stock_take.transaction_date
group by location.id, inventory_item.id, product.id;
ALTER TABLE latest_stock_take_partial ADD FOREIGN KEY (location_id) REFERENCES location(id);
ALTER TABLE latest_stock_take_partial ADD FOREIGN KEY (inventory_item_id) REFERENCES inventory_item(id);
ALTER TABLE latest_stock_take_partial ADD FOREIGN KEY (product_id) REFERENCES product(id);
ALTER TABLE latest_stock_take_partial ADD UNIQUE INDEX (location_id, product_id, inventory_item_id);
ALTER TABLE latest_stock_take_partial ADD INDEX (product_code);
ALTER TABLE latest_stock_take_partial ADD INDEX (lot_number);
# ---------------------------------------------------------------------------------------------
# step 2 insert / update from stock take partial where partial stock take happens after full stock take
INSERT INTO latest_stock_take (transaction_date, location_id, inventory_item_id, product_id, quantity)
SELECT transaction_date, location_id, inventory_item_id, product_id, quantity
FROM latest_stock_take_partial
GROUP BY location_id, product_id, inventory_item_id
ON DUPLICATE KEY UPDATE quantity = values(quantity), transaction_date = values(transaction_date);
# ---------------------------------------------------------------------------------------------
# step 4a create latest credits
create table latest_credits AS
select
	transaction_type.transaction_code,
	location.id as location_id,
	location.name as location_name,
	inventory_item.id as inventory_item_id,
	inventory_item.lot_number as lot_number,
	product.id as product_id,
	product.product_code as product_code,
	product.name as product_name,
	ifnull(sum(transaction_entry.quantity), 0) as quantity
from transaction
join transaction_entry on transaction.id = transaction_entry.transaction_id
join transaction_type on transaction.transaction_type_id = transaction_type.id
join inventory on inventory.id = transaction.inventory_id
join location on location.inventory_id = inventory.id
join inventory_item on transaction_entry.inventory_item_id = inventory_item.id
join product on inventory_item.product_id = product.id
join latest_stock_take on (latest_stock_take.product_id = product.id
	AND latest_stock_take.inventory_item_id = inventory_item.id
	AND latest_stock_take.location_id = location.id)
where transaction_type.transaction_code = 'CREDIT'
and transaction.transaction_date > latest_stock_take.transaction_date
group by location.id, inventory_item.id, product.id;
ALTER TABLE latest_credits ADD FOREIGN KEY (location_id) REFERENCES location(id);
ALTER TABLE latest_credits ADD FOREIGN KEY (inventory_item_id) REFERENCES inventory_item(id);
ALTER TABLE latest_credits ADD FOREIGN KEY (product_id) REFERENCES product(id);
ALTER TABLE latest_credits ADD INDEX (product_code);
ALTER TABLE latest_credits ADD INDEX (lot_number);
# ---------------------------------------------------------------------------------------------
# step 4b update latest stock take with latest credits
UPDATE latest_stock_take lst
JOIN latest_credits lc on lst.inventory_item_id = lc.inventory_item_id and lst.location_id = lc.location_id
SET lst.quantity = lst.quantity + lc.quantity;
# ---------------------------------------------------------------------------------------------
# step 5a create latest debits
create table latest_debits AS
select
	transaction_type.transaction_code,
	location.id as location_id,
	location.name as location_name,
	inventory_item.id as inventory_item_id,
	inventory_item.lot_number as lot_number,
	product.id as product_id,
	product.product_code as product_code,
	product.name as product_name,
	ifnull(sum(transaction_entry.quantity),0) as quantity
from transaction
join transaction_entry on transaction.id = transaction_entry.transaction_id
join transaction_type on transaction.transaction_type_id = transaction_type.id
join inventory on inventory.id = transaction.inventory_id
join location on location.inventory_id = inventory.id
join inventory_item on transaction_entry.inventory_item_id = inventory_item.id
join product on inventory_item.product_id = product.id
join latest_stock_take on (latest_stock_take.product_id = product.id
	AND latest_stock_take.inventory_item_id = inventory_item.id
	AND latest_stock_take.location_id = location.id)
where transaction_type.transaction_code = 'DEBIT'
and transaction.transaction_date > latest_stock_take.transaction_date
group by location.id, inventory_item.id, product.id;
ALTER TABLE latest_debits ADD FOREIGN KEY (location_id) REFERENCES location(id);
ALTER TABLE latest_debits ADD FOREIGN KEY (inventory_item_id) REFERENCES inventory_item(id);
ALTER TABLE latest_debits ADD FOREIGN KEY (product_id) REFERENCES product(id);
ALTER TABLE latest_debits ADD INDEX (product_code);
ALTER TABLE latest_debits ADD INDEX (lot_number);
# ---------------------------------------------------------------------------------------------
# step 5a update latest stock take with latest debits
UPDATE latest_stock_take lst
JOIN latest_debits ld on lst.inventory_item_id = ld.inventory_item_id and lst.location_id = ld.location_id
SET lst.quantity = lst.quantity - ld.quantity;
# ---------------------------------------------------------------------------------------------
# step 6 populate inventory item summary
LOCK TABLES inventory_item_summary WRITE, latest_stock_take READ;
TRUNCATE TABLE inventory_item_summary;
INSERT INTO inventory_item_summary (id, version, date, location_id, inventory_item_id, product_id, quantity, date_created, last_updated)
SELECT uuid(), 0, transaction_date, location_id, inventory_item_id, product_id, quantity, now(), now()
FROM latest_stock_take
GROUP BY location_id, product_id, inventory_item_id
ON DUPLICATE KEY UPDATE quantity = values(quantity), date = values(date);
UNLOCK TABLES;