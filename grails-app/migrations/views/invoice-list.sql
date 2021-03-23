CREATE OR REPLACE VIEW invoice_list AS (
   	SELECT
         invoice.id,
		 invoice.party_from_id,
         invoice.invoice_number,
         reference_number.identifier as vendor_invoice_number,
         unit_of_measure.name as currency,
         invoice.date_invoiced,
         person.id as created_by_id,
         CONCAT(person.first_name, ' ', person.last_name) as created_by_name,
         count(invoice_item.id) as item_count,
         sum(invoice_item.amount * invoice_item.quantity * invoice_item.quantity_per_uom) as total_value
    FROM invoice
    LEFT JOIN invoice_item ON invoice_item.invoice_id = invoice.id
    LEFT JOIN unit_of_measure ON unit_of_measure.id = invoice.currency_uom_id
    LEFT JOIN person ON person.id = invoice.created_by_id
    LEFT JOIN invoice_reference_number ON invoice_reference_number.invoice_reference_numbers_id = invoice.id
    LEFT JOIN reference_number ON invoice_reference_number.reference_number_id = reference_number.id AND reference_number.reference_number_type_id = 'VENDOR_INVOICE_NUMBER'
    GROUP BY invoice.id, reference_number.id
)
