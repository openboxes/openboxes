# ---------------------------------------------------------------------------------------------
drop table if exists stock_take;
drop table if exists latest_stock_take;
drop table if exists latest_adjustments;
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
		sum(quantity) as quantity
	from transaction
	join transaction_entry on transaction.id = transaction_entry.transaction_id
	join transaction_type on transaction.transaction_type_id = transaction_type.id
	join inventory on inventory.id = transaction.inventory_id
	join location on location.inventory_id = inventory.id
	join inventory_item on transaction_entry.inventory_item_id = inventory_item.id
	join product on inventory_item.product_id = product.id
	where transaction_type.transaction_code = 'PRODUCT_INVENTORY'
	group by date_created, location.id, inventory_item.id;
#ALTER TABLE stock_take ADD FOREIGN KEY (location_id) REFERENCES location(id);
#ALTER TABLE stock_take ADD FOREIGN KEY (inventory_item_id) REFERENCES inventory_item(id);
#ALTER TABLE stock_take ADD FOREIGN KEY (product_id) REFERENCES product(id);
ALTER TABLE stock_take ADD INDEX (transaction_date);
ALTER TABLE stock_take ADD INDEX (lot_number);
ALTER TABLE stock_take ADD INDEX (product_code);
# ---------------------------------------------------------------------------------------------
create table latest_stock_take AS (
	SELECT 
		null as transaction_date,
		location.id as location_id, 
		product.id as product_id, 
		product.product_code as product_code,
		inventory_item.id as inventory_item_id,
		inventory_item.lot_number,
		0 as quantity
	from transaction
	join transaction_entry on transaction.id = transaction_entry.transaction_id
	join inventory on inventory.id = transaction.inventory_id
	join location on location.inventory_id = inventory.id
	join inventory_item on transaction_entry.inventory_item_id = inventory_item.id
	join product on inventory_item.product_id = product.id
	group by location.id, inventory_item.id
);
ALTER TABLE latest_stock_take MODIFY COLUMN transaction_date DATETIME;
#ALTER TABLE latest_stock_take ADD FOREIGN KEY (location_id) REFERENCES location(id);
#ALTER TABLE latest_stock_take ADD FOREIGN KEY (inventory_item_id) REFERENCES inventory_item(id);
#ALTER TABLE latest_stock_take ADD FOREIGN KEY (product_id) REFERENCES product(id);
ALTER TABLE latest_stock_take ADD INDEX (transaction_date);
ALTER TABLE latest_stock_take ADD INDEX (product_code);
#ALTER TABLE latest_stock_take ADD INDEX (lot_number);
#ALTER TABLE latest_stock_take ADD UNIQUE INDEX (location_id, product_id, inventory_item_id);
ALTER TABLE latest_stock_take ADD UNIQUE INDEX (location_id, inventory_item_id);
# ---------------------------------------------------------------------------------------------
INSERT INTO latest_stock_take 
(transaction_date, location_id, product_id, product_code, inventory_item_id, lot_number, quantity)
    select
		latest_stock_take.transaction_date,
		stock_take.location_id,
		stock_take.product_id,
		product_code,
		stock_take.inventory_item_id,
		lot_number,
    # this needs to be sum because some stock count transactions have multiple line items per inventory item
		sum(quantity) as quantity		
	from stock_take
	inner join (
		SELECT max(transaction_date) as transaction_date, location_id, product_id
		FROM stock_take
		GROUP BY location_id, product_id
	) as latest_stock_take ON (
		stock_take.product_id = latest_stock_take.product_id
		AND stock_take.location_id = latest_stock_take.location_id
		AND stock_take.transaction_date = latest_stock_take.transaction_date)
	GROUP BY stock_take.location_id, stock_take.inventory_item_id
ON DUPLICATE KEY UPDATE quantity = values(quantity), transaction_date = values(transaction_date);
# ---------------------------------------------------------------------------------------------
UPDATE latest_stock_take t1
JOIN (
	SELECT product_id, location_id, max(transaction_date) as transaction_date
    FROM latest_stock_take 
	GROUP BY product_id, location_id
) as t2 ON t1.product_id = t2.product_id AND t1.location_id = t2.location_id 
SET t1.transaction_date = t2.transaction_date;
# ---------------------------------------------------------------------------------------------
create table latest_adjustments AS
SELECT 
	transaction.transaction_date as transaction_date,
	transaction_type.transaction_code as transaction_code,
	location.id as location_id,
	location.name as location_name,
	inventory_item.id as inventory_item_id,
	inventory_item.lot_number as lot_number,
	product.id as product_id,
	product.product_code as product_code,
	product.name as product_name,
	transaction_entry.quantity
FROM transaction 
JOIN transaction_type ON transaction.transaction_type_id = transaction_type.id
JOIN transaction_entry ON transaction.id = transaction_entry.transaction_id
join inventory_item on transaction_entry.inventory_item_id = inventory_item.id
join inventory on transaction.inventory_id = inventory.id
join location on location.inventory_id = inventory.id
join product on inventory_item.product_id = product.id
join (
	SELECT 
		location.id as location_id,
		transaction_entry.inventory_item_id as inventory_item_id,
		transaction_type.transaction_code as transaction_code,
		max(transaction.transaction_date) as transaction_date
	FROM transaction
	JOIN transaction_type ON transaction.transaction_type_id = transaction_type.id
	JOIN transaction_entry ON transaction.id = transaction_entry.transaction_id
	join inventory on transaction.inventory_id = inventory.id
	join location on location.inventory_id = inventory.id
	left outer join latest_stock_take on (
		latest_stock_take.inventory_item_id = transaction_entry.inventory_item_id 
		AND latest_stock_take.location_id = location.id
	)
	WHERE transaction_type.transaction_code = "INVENTORY"
	AND (
		transaction.transaction_date >= latest_stock_take.transaction_date 
		OR latest_stock_take.transaction_date is null
	)
	GROUP BY location.id, transaction_entry.inventory_item_id
) as latest_adjustments on (
	latest_adjustments.inventory_item_id = inventory_item.id
	AND latest_adjustments.location_id = location.id
	AND latest_adjustments.transaction_date = transaction.transaction_date
	AND latest_adjustments.transaction_code = transaction_type.transaction_code
)
GROUP BY location.id, inventory_item.id;
#ALTER TABLE latest_stock_take_partial ADD FOREIGN KEY (location_id) REFERENCES location(id);
#ALTER TABLE latest_stock_take_partial ADD FOREIGN KEY (inventory_item_id) REFERENCES inventory_item(id);
#ALTER TABLE latest_stock_take_partial ADD FOREIGN KEY (product_id) REFERENCES product(id);
ALTER TABLE latest_adjustments ADD UNIQUE INDEX (location_id, inventory_item_id);
ALTER TABLE latest_adjustments ADD INDEX (transaction_date);
ALTER TABLE latest_adjustments ADD INDEX (product_code);
ALTER TABLE latest_adjustments ADD INDEX (lot_number);
#  ---------------------------------------------------------------------------------------------
UPDATE latest_stock_take t1
JOIN (
	SELECT inventory_item_id, location_id, quantity, max(transaction_date) as transaction_date
    FROM latest_adjustments 
	GROUP BY inventory_item_id, location_id
    ORDER BY transaction_date DESC
) as t2 ON t1.inventory_item_id = t2.inventory_item_id AND t1.location_id = t2.location_id 
SET t1.quantity = t2.quantity, t1.transaction_date = t2.transaction_date;
#  ---------------------------------------------------------------------------------------------
# step 4a create latest credits
create table latest_credits AS
select
	max(transaction.transaction_date) as transaction_date,
	transaction_type.transaction_code,
	location.id as location_id,
	location.name as location_name,
	inventory_item.id as inventory_item_id,
	inventory_item.lot_number as lot_number,
	product.id as product_id,
	product.product_code as product_code,
	product.name as product_name,
	sum(transaction_entry.quantity) as quantity
from transaction
join transaction_entry on transaction.id = transaction_entry.transaction_id
join transaction_type on transaction.transaction_type_id = transaction_type.id
join inventory on inventory.id = transaction.inventory_id
join location on location.inventory_id = inventory.id
join inventory_item on transaction_entry.inventory_item_id = inventory_item.id
join product on inventory_item.product_id = product.id
join latest_stock_take on (latest_stock_take.inventory_item_id = inventory_item.id 
	AND latest_stock_take.location_id = location.id)
where transaction_type.transaction_code = 'CREDIT'
and (transaction.transaction_date >= latest_stock_take.transaction_date 
	OR latest_stock_take.transaction_date is null)
group by location.id, inventory_item.id;
#ALTER TABLE latest_credits ADD FOREIGN KEY (location_id) REFERENCES location(id);
#ALTER TABLE latest_credits ADD FOREIGN KEY (inventory_item_id) REFERENCES inventory_item(id);
#ALTER TABLE latest_credits ADD FOREIGN KEY (product_id) REFERENCES product(id);
ALTER TABLE latest_credits ADD INDEX (location_id, inventory_item_id);
ALTER TABLE latest_credits ADD INDEX (transaction_date);
ALTER TABLE latest_credits ADD INDEX (product_code);
ALTER TABLE latest_credits ADD INDEX (lot_number);
# ---------------------------------------------------------------------------------------------
# step 4b create latest debits
create table latest_debits AS
select
	max(transaction.transaction_date) as transaction_date,
	transaction_type.transaction_code,
	location.id as location_id,
	location.name as location_name,
	inventory_item.id as inventory_item_id,
	inventory_item.lot_number as lot_number,
	product.id as product_id,
	product.product_code as product_code,
	product.name as product_name,
	sum(transaction_entry.quantity) as quantity
from transaction
join transaction_entry on transaction.id = transaction_entry.transaction_id
join transaction_type on transaction.transaction_type_id = transaction_type.id
join inventory on inventory.id = transaction.inventory_id
join location on location.inventory_id = inventory.id
join inventory_item on transaction_entry.inventory_item_id = inventory_item.id
join product on inventory_item.product_id = product.id
join latest_stock_take on (latest_stock_take.inventory_item_id = inventory_item.id 
	AND latest_stock_take.location_id = location.id)    
where transaction_type.transaction_code = 'DEBIT'
and (transaction.transaction_date >= latest_stock_take.transaction_date
	OR latest_stock_take.transaction_date is null)
group by location.id, inventory_item.id;
#ALTER TABLE latest_debits ADD FOREIGN KEY (location_id) REFERENCES location(id);
#ALTER TABLE latest_debits ADD FOREIGN KEY (inventory_item_id) REFERENCES inventory_item(id);
#ALTER TABLE latest_debits ADD FOREIGN KEY (product_id) REFERENCES product(id);
ALTER TABLE latest_debits ADD INDEX (location_id, inventory_item_id);
ALTER TABLE latest_debits ADD INDEX (transaction_date);
ALTER TABLE latest_debits ADD INDEX (product_code);
ALTER TABLE latest_debits ADD INDEX (lot_number);
# ---------------------------------------------------------------------------------------------
# step 5a update latest stock take with latest credits
/*
INSERT INTO latest_stock_take 
(transaction_date, location_id, product_id, product_code, inventory_item_id, lot_number, quantity)
SELECT max(transaction_date), location_id, product_id, product_code, inventory_item_id, lot_number, sum(quantity)
FROM latest_credits
GROUP BY location_id, product_id, inventory_item_id
ON DUPLICATE KEY UPDATE credits = quantity + values(quantity);
#UPDATE latest_stock_take 
#LEFT OUTER JOIN latest_credits on latest_stock_take.inventory_item_id = latest_credits.inventory_item_id 
#	and latest_stock_take.location_id = latest_credits.location_id
#SET latest_stock_take.quantity = latest_stock_take.quantity + latest_credits.quantity;
# ---------------------------------------------------------------------------------------------
# step 5b update latest stock take with latest debits
INSERT INTO latest_stock_take (transaction_date, location_id, product_id, product_code, inventory_item_id, lot_number, 
	quantity, quantity_partial, debits, credits)
SELECT 
	max(transaction_date), 
	location_id, 
	product_id, 
	product_code,
	inventory_item_id, 
	lot_number, 
    
	0 - sum(quantity)
FROM latest_debits
GROUP BY location_id, inventory_item_id
ON DUPLICATE KEY UPDATE debits = quantity + values(quantity);
*/
#UPDATE latest_stock_take lst
#JOIN latest_debits ld on lst.inventory_item_id = ld.inventory_item_id and lst.location_id = ld.location_id
#SET lst.quantity = lst.quantity - ld.quantity;
# ---------------------------------------------------------------------------------------------
# step 6 populate inventory item summary
#DROP TABLE IF EXISTS inventory_item_summary;

LOCK TABLES 
	inventory_item_summary WRITE, 
    latest_stock_take READ, 
	latest_adjustments READ, 
    latest_credits READ, 
    latest_debits READ;

TRUNCATE inventory_item_summary;

INSERT INTO inventory_item_summary (id, version, location_id, inventory_item_id, product_id,
	quantity0, adjustments, credits, debits, quantity, date_created, last_updated)
select
	uuid(),
	0,
	latest_stock_take.location_id as location_id,
	latest_stock_take.inventory_item_id as inventory_item_id, 
	latest_stock_take.product_id as product_id, 
    latest_stock_take.quantity as quantity0,
    latest_adjustments.quantity as adjustments,
    latest_credits.quantity as credits,
    latest_debits.quantity as debits,    
	ifnull(latest_adjustments.quantity, latest_stock_take.quantity) +
		ifnull(latest_credits.quantity,0) - ifnull(latest_debits.quantity,0) as quantity,     
    now(),
    now()    
from latest_stock_take
left outer join latest_adjustments on (
	latest_adjustments.inventory_item_id = latest_stock_take.inventory_item_id 
	and latest_adjustments.location_id = latest_stock_take.location_id)
left outer join latest_debits on (
	latest_debits.inventory_item_id = latest_stock_take.inventory_item_id 
    and latest_debits.location_id = latest_stock_take.location_id)
left outer join latest_credits on (
	latest_credits.inventory_item_id = latest_stock_take.inventory_item_id 
    and latest_credits.location_id = latest_stock_take.location_id)
GROUP BY location_id, inventory_item_id;


UNLOCK TABLES;