CREATE OR REPLACE VIEW product_supplier_clean AS (
SELECT
        id,
        code,
        name,
        product_id,
        supplier_id,
        manufacturer_id,
        REPLACE(REPLACE(REPLACE(supplier_code, ' ', ''), '-', ''), '.', '') as supplier_code,
        REPLACE(REPLACE(REPLACE(manufacturer_code, ' ', ''), '-', ''), '.', '') as manufacturer_code
    FROM product_supplier
)
