-- Static seed for the OpenBoxes e2e stack (test/e2e/seed.sh), applied once after OBAF's own Liquibase
-- install data (admin/password, Main Warehouse = location 1, Main Supplier = location 2).
-- Only reference data no migration seeds and no API creates; everything a test needs beyond this
-- is built per test (lib/ts/fixtures.ts). Idempotent (INSERT IGNORE on fixed ids).

-- The product category every fixture product uses (the install data has no category at all).
INSERT IGNORE INTO category (id, version, date_created, last_updated, name, description, is_root)
VALUES ('testE2eCategory000000000000000001', 0, NOW(), NOW(), 'Test E2E', 'e2e test fixtures', 1);

-- Event types the packing-location and LPN loading actions look up by code
-- (StockMovementService / PackingLocation: PACKING_SCHEDULED, LOADING_SCHEDULED).
INSERT IGNORE INTO event_type (id, version, date_created, last_updated, name, event_code, active)
VALUES ('testE2eEvtPackingScheduled000001', 0, NOW(), NOW(), 'Packing scheduled', 'PACKING_SCHEDULED', 1),
       ('testE2eEvtLoadingScheduled000001', 0, NOW(), NOW(), 'Loading scheduled', 'LOADING_SCHEDULED', 1);

-- The ZPL label templates the app's print buttons and label URLs need. DocumentService looks them up
-- by document number (two documents with one number break the details calls, so fixed ids).
INSERT IGNORE INTO document_type (id, version, date_created, last_updated, name, document_code)
VALUES ('testE2eZebraTemplateType00000001', 0, NOW(), NOW(), 'Zebra template', 'ZEBRA_TEMPLATE');

INSERT IGNORE INTO document (id, version, date_created, last_updated, name, filename, content_type, document_number, document_type_id, file_contents)
VALUES ('testE2eLabelProduct0000000000001', 0, NOW(), NOW(), 'barcodeLabel:product', 'barcodeLabel-product.zpl', 'text/plain',
        'barcodeLabel:product', 'testE2eZebraTemplateType00000001', '^XA^FO50,60^A0,40^FD${product.productCode}^FS^XZ'),
       ('testE2eLabelInternalLocation0001', 0, NOW(), NOW(), 'barcodeLabel:internalLocation', 'barcodeLabel-internalLocation.zpl', 'text/plain',
        'barcodeLabel:internalLocation', 'testE2eZebraTemplateType00000001', '^XA^FO50,60^A0,40^FD${internalLocation.locationNumber}^FS^XZ'),
       ('testE2eLabelContainer00000000001', 0, NOW(), NOW(), 'barcodeLabel:container', 'barcodeLabel-container.zpl', 'text/plain',
        'barcodeLabel:container', 'testE2eZebraTemplateType00000001', '^XA^FO50,60^A0,40^FD${container.containerNumber}^FS^XZ');
