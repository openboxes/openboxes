DROP VIEW IF EXISTS invoice_candidate;
CREATE OR REPLACE VIEW invoice_item_candidate AS
(select order_adjustment.id as id,
        `order`.order_number as order_number,
        `order`.id as order_id,
        '' as shipment_id,
        '' as shipment_number,
        order_adjustment.budget_code_id as budget_code_id,
        coalesce(order_item.gl_account_id, order_adjustment_type.gl_account_id) as gl_account_id,
        product.product_code as product_code,
        order_adjustment.description as description,
        1 as quantity,
        1 as quantity_to_invoice,
        order_item.quantity_uom_id as quantity_uom_id,
        order_item.unit_price as unit_price,
        order_item.quantity_per_uom as quantity_per_uom,
        order_item.quantity as order_item_quantity,
        `order`.currency_code as currency_code,
        `order`.origin_party_id as vendor_id,
        order_adjustment.amount as adjustment_amount,
        order_adjustment.percentage as adjustment_percentage,
        product_supplier.supplier_code as supplier_code
 from order_adjustment
          join `order` on order_adjustment.order_id = `order`.id
          left join order_item on order_adjustment.order_item_id = order_item.id
          left join product_supplier on order_item.product_supplier_id = product_supplier.id
          left join product on order_item.product_id = product.id
          left join order_adjustment_type on order_adjustment.order_adjustment_type_id = order_adjustment_type.id
          left join order_adjustment_invoice on order_adjustment_invoice.order_adjustment_id = order_adjustment.id
 where order_adjustment_invoice.invoice_item_id is null and (IFNULL(order_adjustment.canceled, 0) != 1))
union
(select shipment_item.id as id,
        `order`.order_number as order_number,
        `order`.id as order_id,
        shipment.id as shipment_id,
        shipment.shipment_number as shipment_number,
        order_item.budget_code_id as budget_code_id,
        product.gl_account_id as gl_account_id,
        product.product_code as product_code,
        product.name as description,
        shipment_item.quantity/order_item.quantity_per_uom as quantity,
        shipment_item.quantity/order_item.quantity_per_uom as quantity_to_invoice,
        order_item.quantity_uom_id as quantity_uom_id,
        order_item.unit_price as unit_price,
        order_item.quantity_per_uom as quantity_per_uom,
        order_item.quantity as order_item_quantity,
        `order`.currency_code as currency_code,
        `order`.origin_party_id as vendor_id,
        NULL as adjustment_amount,
        NULL as adjustment_percentage,
        product_supplier.supplier_code as supplier_code
 from shipment_item
          join shipment on shipment_item.shipment_id = shipment.id
          join product on shipment_item.product_id = product.id
          join order_shipment on order_shipment.shipment_item_id = shipment_item.id
          join order_item on order_shipment.order_item_id = order_item.id
          left join product_supplier on order_item.product_supplier_id = product_supplier.id
          join `order` on order_item.order_id = `order`.id
          left join order_invoice on order_invoice.order_item_id = order_item.id
          left join shipment_invoice on shipment_invoice.shipment_item_id = shipment_item.id
 where order_invoice.invoice_item_id is null
   and shipment_invoice.invoice_item_id is null
   and shipment.current_status = 'SHIPPED');
