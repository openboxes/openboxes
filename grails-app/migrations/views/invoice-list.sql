CREATE OR REPLACE VIEW invoice_list AS (
   	SELECT
         invoice.id as invoice_id,
         invoice.invoice_number,
         reference_number.identifier as vendor_invoice_number,
         invoice.party_from_id,
         invoice.party_id,
         party.code as party_code,
         party.name as party_name,
         IF(invoice_type.code is not null, invoice_type.code, "INVOICE") as invoice_type_code,
         CASE
             WHEN invoice.date_paid is not null THEN 'PAID'
             WHEN invoice.date_posted is not null THEN 'POSTED'
             WHEN invoice.date_submitted is not null THEN 'SUBMITTED'
             ELSE 'PENDING'
         END as status,
         unit_of_measure.name as currency,
         count(invoice_item.id) as item_count,
         invoice.date_invoiced,
         invoice.date_created,
         person.id as created_by_id,
         CONCAT(person.first_name, ' ', person.last_name) as created_by_name
    FROM invoice
    LEFT JOIN invoice_item ON invoice_item.invoice_id = invoice.id
    LEFT JOIN unit_of_measure ON unit_of_measure.id = invoice.currency_uom_id
    LEFT JOIN person ON person.id = invoice.created_by_id
    LEFT JOIN invoice_reference_number ON invoice_reference_number.invoice_reference_numbers_id = invoice.id
    LEFT JOIN reference_number ON invoice_reference_number.reference_number_id = reference_number.id AND reference_number.reference_number_type_id = 'VENDOR_INVOICE_NUMBER'
    LEFT JOIN party ON invoice.party_id = party.id
    LEFT JOIN invoice_type ON invoice.invoice_type_id = invoice_type.id
    GROUP BY invoice.id, reference_number.id
)
