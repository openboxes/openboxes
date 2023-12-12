package install

databaseChangeLog = {

    changeSet(author: "openboxes (generated)", id: "1700664714834-1") {
        createTable(tableName: "address") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "address", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "address2", type: "VARCHAR(255)")

            column(name: "city", type: "VARCHAR(255)")

            column(name: "country", type: "VARCHAR(255)")

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "postal_code", type: "VARCHAR(255)")

            column(name: "state_or_province", type: "VARCHAR(255)")

            column(name: "description", type: "VARCHAR(4000)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-2") {
        createTable(tableName: "attribute") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "allow_other", type: "BIT(1)")

            column(name: "active", type: "BIT(1)")

            column(name: "allow_multiple", type: "BIT(1)")

            column(name: "code", type: "VARCHAR(255)") {
                constraints(unique: "true")
            }

            column(name: "default_value", type: "VARCHAR(255)")

            column(name: "description", type: "VARCHAR(255)")

            column(name: "required", type: "BIT(1)")

            column(defaultValueBoolean: "true", name: "exportable", type: "BIT(1)")

            column(name: "unit_of_measure_class_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-3") {
        createTable(tableName: "attribute_entity_type_codes") {
            column(name: "attribute_id", type: "CHAR(38)")

            column(name: "entity_type_code", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-4") {
        createTable(tableName: "attribute_options") {
            column(name: "attribute_id", type: "CHAR(38)")

            column(name: "options_string", type: "VARCHAR(255)")

            column(name: "options_idx", type: "INT")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-5") {
        createTable(tableName: "budget_code") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "code", type: "LONGTEXT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "LONGTEXT")

            column(name: "description", type: "LONGTEXT")

            column(name: "organization_id", type: "CHAR(38)")

            column(name: "date_created", type: "datetime")

            column(name: "last_updated", type: "datetime")

            column(name: "created_by_id", type: "CHAR(38)")

            column(name: "updated_by_id", type: "CHAR(38)")

            column(name: "active", type: "BIT(1)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-6") {
        createTable(tableName: "category") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "parent_category_id", type: "CHAR(38)")

            column(name: "sort_order", type: "INT")

            column(name: "is_root", type: "TINYINT(3)")

            column(name: "gl_account_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-9") {
        createTable(tableName: "comment") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "comment", type: "TEXT")

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "date_read", type: "datetime")

            column(name: "date_sent", type: "datetime")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "recipient_id", type: "CHAR(38)")

            column(name: "sender_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-10") {
        createTable(tableName: "consumption") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "day", type: "INT")

            column(name: "inventory_item_id", type: "CHAR(38)")

            column(name: "month", type: "INT")

            column(name: "product_id", type: "CHAR(38)")

            column(name: "year", type: "INT")

            column(name: "location_id", type: "CHAR(38)")

            column(name: "transaction_date", type: "datetime")

            column(name: "date_created", type: "datetime")

            column(name: "last_updated", type: "datetime")

            column(name: "quantity", type: "INT")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-11") {
        createTable(tableName: "consumption_fact") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "location_key_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "lot_key_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "product_key_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "quantity", type: "DECIMAL(19, 2)") {
                constraints(nullable: "false")
            }

            column(name: "transaction_code", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "transaction_date_key_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "transaction_number", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "transaction_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "unit_cost", type: "DECIMAL(19, 2)") {
                constraints(nullable: "false")
            }

            column(name: "unit_price", type: "DECIMAL(19, 2)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-12") {
        createTable(tableName: "container") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "container_number", type: "VARCHAR(255)")

            column(name: "container_type_id", type: "CHAR(38)")

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "height", type: "FLOAT(12, 3)")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "length", type: "FLOAT(12, 3)")

            column(name: "name", type: "VARCHAR(255)")

            column(name: "parent_container_id", type: "CHAR(38)")

            column(name: "recipient_id", type: "CHAR(38)")

            column(name: "shipment_id", type: "CHAR(38)")

            column(name: "volume_units", type: "VARCHAR(255)")

            column(name: "weight", type: "FLOAT(12, 3)")

            column(name: "weight_units", type: "VARCHAR(255)")

            column(name: "width", type: "FLOAT(12, 3)")

            column(name: "container_status", type: "VARCHAR(255)")

            column(name: "sort_order", type: "INT")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-13") {
        createTable(tableName: "container_type") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "sort_order", type: "INT")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-14") {
        createTable(tableName: "date_dimension") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date", type: "datetime") {
                constraints(nullable: "false", unique: "true")
            }

            column(name: "day_of_month", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "day_of_week", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "month", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "month_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "month_year", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "week", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "weekday_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "year", type: "INT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-15") {
        createTable(tableName: "document") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "content_type", type: "VARCHAR(255)")

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "document_number", type: "VARCHAR(255)")

            column(name: "document_type_id", type: "CHAR(38)")

            column(name: "extension", type: "VARCHAR(255)")

            column(name: "file_contents", type: "MEDIUMBLOB")

            column(name: "file_uri", type: "LONGTEXT")

            column(name: "filename", type: "VARCHAR(255)")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-16") {
        createTable(tableName: "document_type") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "sort_order", type: "INT")

            column(name: "document_code", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-17") {
        createTable(tableName: "donor") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime")

            column(name: "description", type: "VARCHAR(255)")

            column(name: "last_updated", type: "datetime")

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-18") {
        createTable(tableName: "event") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "event_date", type: "datetime")

            column(name: "event_location_id", type: "CHAR(38)")

            column(name: "event_type_id", type: "CHAR(38)")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "created_by_id", type: "CHAR(38)")

            column(name: "comment_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-19") {
        createTable(tableName: "event_type") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "event_code", type: "VARCHAR(255)")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "sort_order", type: "INT")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-20") {
        createTable(tableName: "fulfillment") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "date_fulfilled", type: "datetime")

            column(name: "fulfilled_by_id", type: "CHAR(38)")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "status", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-21") {
        createTable(tableName: "fulfillment_item") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "fulfillment_id", type: "CHAR(38)")

            column(name: "inventory_item_id", type: "CHAR(38)")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "quantity", type: "INT")

            column(name: "requisition_item_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-22") {
        createTable(tableName: "fulfillment_item_shipment_item") {
            column(name: "fulfillment_item_shipment_items_id", type: "CHAR(38)")

            column(name: "shipment_item_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-23") {
        createTable(tableName: "gl_account") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "code", type: "LONGTEXT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "LONGTEXT")

            column(name: "description", type: "LONGTEXT")

            column(name: "gl_account_type_id", type: "CHAR(38)")

            column(name: "date_created", type: "datetime")

            column(name: "last_updated", type: "datetime")

            column(name: "created_by_id", type: "CHAR(38)")

            column(name: "updated_by_id", type: "CHAR(38)")

            column(defaultValueBoolean: "true", name: "active", type: "BIT(1)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-24") {
        createTable(tableName: "gl_account_type") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "code", type: "LONGTEXT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "LONGTEXT")

            column(defaultValue: "ASSET", name: "gl_account_type_code", type: "VARCHAR(100)")

            column(name: "date_created", type: "datetime")

            column(name: "last_updated", type: "datetime")

            column(name: "created_by_id", type: "CHAR(38)")

            column(name: "updated_by_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-25") {
        createTable(tableName: "indicator") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(2000)") {
                constraints(nullable: "false")
            }

            column(name: "expression", type: "TEXT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-26") {
        createTable(tableName: "inventory") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "last_inventory_date", type: "datetime")

            column(name: "date_created", type: "datetime")

            column(name: "last_updated", type: "datetime")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-27") {
        createTable(tableName: "inventory_item") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "lot_number", type: "VARCHAR(255)")

            column(name: "product_id", type: "CHAR(38)")

            column(name: "expiration_date", type: "datetime")

            column(name: "comments", type: "VARCHAR(255)")

            column(name: "lot_status", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-28") {
        createTable(tableName: "inventory_item_snapshot") {
            column(name: "id", type: "CHAR(38)")

            column(name: "version", type: "INT")

            column(name: "date", type: "datetime")

            column(name: "product_id", type: "CHAR(38)")

            column(name: "location_id", type: "CHAR(38)")

            column(name: "inventory_item_id", type: "CHAR(38)")

            column(name: "quantity_on_hand", type: "INT")

            column(name: "quantity_available_to_promise", type: "INT")

            column(name: "quantity_inbound", type: "INT")

            column(name: "quantity_outbound", type: "INT")

            column(name: "last_updated", type: "datetime")

            column(name: "date_created", type: "datetime")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-29") {
        createTable(tableName: "inventory_level") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "inventory_id", type: "CHAR(38)")

            column(name: "min_quantity", type: "INT")

            column(name: "product_id", type: "CHAR(38)")

            column(name: "reorder_quantity", type: "INT")

            column(name: "date_created", type: "datetime")

            column(name: "last_updated", type: "datetime")

            column(name: "status", type: "VARCHAR(255)")

            column(name: "max_quantity", type: "INT")

            column(name: "bin_location", type: "VARCHAR(255)")

            column(name: "abc_class", type: "VARCHAR(255)")

            column(name: "preferred", type: "TINYINT(3)")

            column(name: "expected_lead_time_days", type: "DECIMAL(19, 2)")

            column(name: "forecast_period_days", type: "DECIMAL(19, 2)")

            column(name: "forecast_quantity", type: "DECIMAL(19, 2)")

            column(name: "preferred_bin_location_id", type: "CHAR(38)")

            column(name: "replenishment_location_id", type: "CHAR(38)")

            column(name: "comments", type: "VARCHAR(255)")

            column(name: "internal_location_id", type: "CHAR(38)")

            column(name: "replenishment_period_days", type: "DECIMAL(19, 2)")

            column(name: "demand_time_period_days", type: "DECIMAL(19, 2)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-30") {
        createTable(tableName: "inventory_snapshot") {
            column(name: "id", type: "CHAR(38)")

            column(name: "version", type: "INT")

            column(name: "product_id", type: "CHAR(38)")

            column(name: "location_id", type: "CHAR(38)")

            column(name: "inventory_item_id", type: "CHAR(38)")

            column(name: "quantity_on_hand", type: "INT")

            column(name: "quantity_available_to_promise", type: "INT")

            column(name: "quantity_inbound", type: "INT")

            column(name: "quantity_outbound", type: "INT")

            column(name: "last_updated", type: "datetime")

            column(name: "date_created", type: "datetime")

            column(name: "date", type: "datetime")

            column(name: "bin_location_id", type: "CHAR(38)")

            column(name: "product_code", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(defaultValue: "DEFAULT", name: "lot_number", type: "VARCHAR(255)")

            column(name: "expiration_date", type: "datetime")

            column(defaultValue: "DEFAULT", name: "bin_location_name", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-31") {
        createTable(tableName: "invoice") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "invoice_number", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)")

            column(name: "description", type: "VARCHAR(255)")

            column(name: "invoice_type_id", type: "CHAR(38)")

            column(name: "party_from_id", type: "CHAR(38)")

            column(name: "party_id", type: "CHAR(38)")

            column(name: "date_invoiced", type: "datetime")

            column(name: "date_submitted", type: "datetime")

            column(name: "date_due", type: "datetime")

            column(name: "date_paid", type: "datetime")

            column(name: "currency_uom_id", type: "CHAR(38)")

            column(name: "date_created", type: "datetime")

            column(name: "last_updated", type: "datetime")

            column(name: "created_by_id", type: "CHAR(38)")

            column(name: "updated_by_id", type: "CHAR(38)")

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_posted", type: "datetime")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-32") {
        createTable(tableName: "invoice_document") {
            column(name: "invoice_id", type: "CHAR(38)")

            column(name: "document_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-33") {
        createTable(tableName: "invoice_item") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "invoice_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "product_id", type: "CHAR(38)")

            column(name: "gl_account_id", type: "CHAR(38)")

            column(name: "budget_code_id", type: "CHAR(38)")

            column(name: "quantity", type: "DECIMAL(19, 2)") {
                constraints(nullable: "false")
            }

            column(name: "quantity_uom_id", type: "CHAR(38)")

            column(name: "quantity_per_uom", type: "DECIMAL(19, 2)") {
                constraints(nullable: "false")
            }

            column(name: "amount", type: "DECIMAL(19, 4)")

            column(name: "date_created", type: "datetime")

            column(name: "last_updated", type: "datetime")

            column(name: "created_by_id", type: "CHAR(38)")

            column(name: "updated_by_id", type: "CHAR(38)")

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "unit_price", type: "DECIMAL(19, 4)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-34") {
        createTable(tableName: "invoice_reference_number") {
            column(name: "invoice_reference_numbers_id", type: "CHAR(38)")

            column(name: "reference_number_id", type: "CHAR(38)")

            column(name: "reference_numbers_idx", type: "INT")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-35") {
        createTable(tableName: "invoice_type") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(defaultValue: "INVOICE", name: "code", type: "VARCHAR(100)")

            column(name: "date_created", type: "datetime")

            column(name: "last_updated", type: "datetime")

            column(name: "created_by_id", type: "CHAR(38)")

            column(name: "updated_by_id", type: "CHAR(38)")

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-36") {
        createTable(tableName: "local_transfer") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "source_transaction_id", type: "CHAR(38)")

            column(name: "destination_transaction_id", type: "CHAR(38)")

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-37") {
        createTable(tableName: "localization") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "code", type: "VARCHAR(250)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "locale", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "text", type: "VARCHAR(2000)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-38") {
        createTable(tableName: "location") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "address_id", type: "CHAR(38)")

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "logo_url", type: "VARCHAR(255)")

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "inventory_id", type: "CHAR(38)")

            column(name: "manager_id", type: "CHAR(38)")

            column(name: "logo", type: "MEDIUMBLOB")

            column(name: "managed_locally", type: "BIT(1)")

            column(name: "active", type: "BIT(1)")

            column(name: "location_type_id", type: "CHAR(38)")

            column(name: "parent_location_id", type: "CHAR(38)")

            column(name: "bg_color", type: "VARCHAR(255)")

            column(name: "fg_color", type: "VARCHAR(255)")

            column(name: "location_group_id", type: "CHAR(38)")

            column(defaultValueNumeric: "0", name: "sort_order", type: "INT")

            column(name: "location_number", type: "VARCHAR(255)")

            column(name: "description", type: "VARCHAR(255)")

            column(name: "organization_id", type: "CHAR(38)")

            column(name: "zone_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-39") {
        createTable(tableName: "location_dimension") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "location_group_name", type: "VARCHAR(255)")

            column(name: "location_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "location_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "location_number", type: "VARCHAR(255)")

            column(name: "location_type_code", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "location_type_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "parent_location_name", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-40") {
        createTable(tableName: "location_group") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)")

            column(name: "address_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-41") {
        createTable(tableName: "location_role") {
            column(name: "user_id", type: "CHAR(38)")

            column(name: "location_id", type: "CHAR(38)")

            column(name: "role_id", type: "CHAR(38)")

            column(name: "version", type: "INT")

            column(name: "id", type: "CHAR(38)")

            column(name: "location_roles_idx", type: "INT")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-42") {
        createTable(tableName: "location_supported_activities") {
            column(name: "location_id", type: "CHAR(38)")

            column(name: "supported_activities_string", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-43") {
        createTable(tableName: "location_type") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "sort_order", type: "INT")

            column(name: "location_type_code", type: "VARCHAR(100)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-44") {
        createTable(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", type: "CHAR(38)")

            column(name: "supported_activities_string", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-45") {
        createTable(tableName: "lot_dimension") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "expiration_date", type: "datetime")

            column(name: "inventory_item_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "lot_number", type: "VARCHAR(255)")

            column(name: "product_code", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-46") {
        createTable(tableName: "order") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime")

            column(name: "date_ordered", type: "VARCHAR(255)")

            column(name: "description", type: "VARCHAR(255)")

            column(name: "destination_id", type: "CHAR(38)")

            column(name: "last_updated", type: "datetime")

            column(name: "order_number", type: "VARCHAR(255)")

            column(name: "ordered_by_id", type: "CHAR(38)")

            column(name: "origin_id", type: "CHAR(38)")

            column(name: "recipient_id", type: "CHAR(38)")

            column(name: "status", type: "VARCHAR(255)")

            column(name: "completed_by_id", type: "CHAR(38)")

            column(name: "date_completed", type: "datetime")

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "currency_code", type: "CHAR(3)")

            column(name: "exchange_rate", type: "DECIMAL(19, 8)")

            column(name: "payment_method_type_id", type: "CHAR(38)")

            column(name: "payment_term_id", type: "CHAR(38)")

            column(name: "approved_by_id", type: "CHAR(38)")

            column(name: "date_approved", type: "datetime")

            column(name: "destination_party_id", type: "CHAR(38)")

            column(name: "origin_party_id", type: "CHAR(38)")

            column(name: "created_by_id", type: "CHAR(38)")

            column(name: "updated_by_id", type: "CHAR(38)")

            column(name: "order_type_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-47") {
        createTable(tableName: "order_adjustment") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "order_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "order_item_id", type: "CHAR(38)")

            column(name: "order_adjustment_type_id", type: "CHAR(38)")

            column(name: "amount", type: "DECIMAL(19, 4)")

            column(name: "percentage", type: "DECIMAL(19, 2)")

            column(name: "description", type: "VARCHAR(255)")

            column(name: "comments", type: "VARCHAR(255)")

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "budget_code_id", type: "CHAR(38)")

            column(name: "gl_account_id", type: "CHAR(38)")

            column(name: "canceled", type: "BIT(1)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-48") {
        createTable(tableName: "order_adjustment_invoice") {
            column(name: "invoice_item_id", type: "CHAR(38)")

            column(name: "order_adjustment_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-49") {
        createTable(tableName: "order_adjustment_type") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "code", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "gl_account_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-50") {
        createTable(tableName: "order_comment") {
            column(name: "order_comments_id", type: "CHAR(38)")

            column(name: "comment_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-51") {
        createTable(tableName: "order_document") {
            column(name: "order_documents_id", type: "CHAR(38)")

            column(name: "document_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-52") {
        createTable(tableName: "order_event") {
            column(name: "order_events_id", type: "CHAR(38)")

            column(name: "event_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-53") {
        createTable(tableName: "order_invoice") {
            column(name: "invoice_item_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "order_item_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-54") {
        createTable(tableName: "order_item") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "category_id", type: "CHAR(38)")

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "inventory_item_id", type: "CHAR(38)")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "order_id", type: "CHAR(38)")

            column(name: "product_id", type: "CHAR(38)")

            column(name: "quantity", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "requested_by_id", type: "CHAR(38)")

            column(name: "unit_price", type: "DECIMAL(19, 4)")

            column(defaultValue: "PENDING", name: "order_item_status_code", type: "VARCHAR(100)")

            column(name: "parent_order_item_id", type: "CHAR(38)")

            column(name: "recipient_id", type: "CHAR(38)")

            column(name: "destination_bin_location_id", type: "CHAR(38)")

            column(name: "origin_bin_location_id", type: "CHAR(38)")

            column(name: "currency_code", type: "CHAR(3)")

            column(name: "product_supplier_id", type: "CHAR(38)")

            column(name: "estimated_ready_date", type: "datetime")

            column(name: "estimated_ship_date", type: "datetime")

            column(name: "estimated_delivery_date", type: "datetime")

            column(name: "actual_ready_date", type: "datetime")

            column(name: "actual_ship_date", type: "datetime")

            column(name: "actual_delivery_date", type: "datetime")

            column(name: "product_package_id", type: "CHAR(38)")

            column(name: "quantity_per_uom", type: "DECIMAL(19, 2)")

            column(name: "quantity_uom_id", type: "CHAR(38)")

            column(name: "budget_code_id", type: "CHAR(38)")

            column(name: "gl_account_id", type: "CHAR(38)")

            column(name: "order_index", type: "INT")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-55") {
        createTable(tableName: "order_item_comment") {
            column(name: "order_item_comments_id", type: "CHAR(38)")

            column(name: "comment_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-56") {
        createTable(tableName: "order_shipment") {
            column(name: "order_item_id", type: "CHAR(38)")

            column(name: "shipment_item_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-58") {
        createTable(tableName: "order_type") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "code", type: "VARCHAR(100)") {
                constraints(nullable: "false")
            }

            column(name: "order_type_code", type: "VARCHAR(100)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime")

            column(name: "last_updated", type: "datetime")

            column(name: "created_by_id", type: "CHAR(38)")

            column(name: "updated_by_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-59") {
        createTable(tableName: "organization_sequences") {
            column(name: "sequences", type: "VARCHAR(255)")

            column(name: "sequences_idx", type: "VARCHAR(255)")

            column(name: "sequences_elt", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-60") {
        createTable(tableName: "party") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "party_type_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "class", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "code", type: "VARCHAR(255)")

            column(name: "description", type: "VARCHAR(255)")

            column(name: "name", type: "VARCHAR(255)")

            column(name: "default_location_id", type: "CHAR(38)")

            column(defaultValueBoolean: "true", name: "active", type: "BIT(1)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-61") {
        createTable(tableName: "party_role") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "end_date", type: "datetime")

            column(name: "party_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "role_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "start_date", type: "datetime")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-62") {
        createTable(tableName: "party_type") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "code", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "party_type_code", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-63") {
        createTable(tableName: "payment_method_type") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "payment_method_type_code", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "active", type: "BIT(1)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-64") {
        createTable(tableName: "payment_term") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "code", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "prepayment_percent", type: "DECIMAL(19, 2)")

            column(name: "days_to_payment", type: "INT")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-65") {
        createTable(tableName: "person") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "email", type: "VARCHAR(255)")

            column(name: "first_name", type: "VARCHAR(255)")

            column(name: "last_name", type: "VARCHAR(255)")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "phone_number", type: "VARCHAR(255)")

            column(defaultValueBoolean: "true", name: "active", type: "BIT(1)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-66") {
        createTable(tableName: "picklist") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "name", type: "VARCHAR(255)")

            column(name: "description", type: "VARCHAR(255)")

            column(name: "picker_id", type: "CHAR(38)")

            column(name: "date_picked", type: "datetime")

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "created_by_id", type: "CHAR(38)")

            column(name: "updated_by_id", type: "CHAR(38)")

            column(name: "requisition_id", type: "CHAR(38)")

            column(name: "order_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-67") {
        createTable(tableName: "picklist_item") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "quantity", type: "INT")

            column(name: "picklist_id", type: "CHAR(38)")

            column(name: "inventory_item_id", type: "CHAR(38)")

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "created_by_id", type: "CHAR(38)")

            column(name: "updated_by_id", type: "CHAR(38)")

            column(name: "status", type: "VARCHAR(255)")

            column(name: "reason_code", type: "VARCHAR(255)")

            column(name: "comment", type: "VARCHAR(255)")

            column(name: "requisition_item_id", type: "CHAR(38)")

            column(name: "picklist_items_idx", type: "INT")

            column(name: "bin_location_id", type: "CHAR(38)")

            column(name: "sort_order", type: "INT")

            column(name: "order_item_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-68") {
        createTable(tableName: "preference_type") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false", unique: "true")
            }

            column(defaultValue: "DEFAULT", name: "validation_code", type: "VARCHAR(100)")

            column(name: "date_created", type: "datetime")

            column(name: "last_updated", type: "datetime")

            column(name: "created_by_id", type: "CHAR(38)")

            column(name: "updated_by_id", type: "CHAR(38)")

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-69") {
        createTable(tableName: "product") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "category_id", type: "CHAR(38)")

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "MEDIUMTEXT")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "product_code", type: "VARCHAR(255)") {
                constraints(unique: "true")
            }

            column(name: "unit_of_measure_id", type: "CHAR(38)")

            column(name: "cold_chain", type: "BIT(1)")

            column(name: "manufacturer", type: "VARCHAR(255)")

            column(name: "manufacturer_code", type: "VARCHAR(255)")

            column(name: "ndc", type: "VARCHAR(255)")

            column(name: "upc", type: "VARCHAR(255)")

            column(name: "unit_of_measure", type: "VARCHAR(255)")

            column(name: "created_by_id", type: "CHAR(38)")

            column(name: "updated_by_id", type: "CHAR(38)")

            column(name: "default_uom_id", type: "CHAR(38)")

            column(name: "brand_name", type: "VARCHAR(255)")

            column(name: "vendor", type: "VARCHAR(255)")

            column(name: "vendor_code", type: "VARCHAR(255)")

            column(name: "package_size", type: "INT")

            column(name: "model_number", type: "VARCHAR(255)")

            column(name: "manufacturer_name", type: "VARCHAR(255)")

            column(name: "vendor_name", type: "VARCHAR(255)")

            column(name: "active", type: "BIT(1)")

            column(name: "controlled_substance", type: "BIT(1)")

            column(name: "essential", type: "BIT(1)")

            column(name: "hazardous_material", type: "BIT(1)")

            column(name: "lot_control", type: "BIT(1)")

            column(name: "serialized", type: "BIT(1)")

            column(name: "price_per_unit", type: "DECIMAL(19, 4)")

            column(name: "reconditioned", type: "BIT(1)")

            column(name: "product_type_id", type: "CHAR(38)")

            column(name: "abc_class", type: "VARCHAR(255)")

            column(name: "cost_per_unit", type: "DECIMAL(19, 4)")

            column(name: "gl_account_id", type: "CHAR(38)")

            column(name: "lot_and_expiry_control", type: "BIT(1)")

            column(name: "product_family_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-70") {
        createTable(tableName: "product_association") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "code", type: "VARCHAR(100)") {
                constraints(nullable: "false")
            }

            column(name: "product_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "associated_product_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "quantity", type: "DECIMAL(19, 2)") {
                constraints(nullable: "false")
            }

            column(name: "comments", type: "VARCHAR(255)")

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "mutual_association_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-71") {
        createTable(tableName: "product_attribute") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "attribute_id", type: "CHAR(38)")

            column(name: "product_id", type: "CHAR(38)")

            column(name: "attributes_idx", type: "INT")

            column(name: "value", type: "VARCHAR(255)")

            column(name: "unit_of_measure_id", type: "CHAR(38)")

            column(name: "product_supplier_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-72") {
        createTable(tableName: "product_availability") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "bin_location_id", type: "CHAR(38)")

            column(name: "inventory_item_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "location_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "product_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "product_code", type: "VARCHAR(100)") {
                constraints(nullable: "false")
            }

            column(defaultValue: "DEFAULT", name: "lot_number", type: "VARCHAR(100)") {
                constraints(nullable: "false")
            }

            column(defaultValue: "DEFAULT", name: "bin_location_name", type: "VARCHAR(100)") {
                constraints(nullable: "false")
            }

            column(name: "quantity_allocated", type: "INT")

            column(name: "quantity_on_hand", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "quantity_on_hold", type: "BIGINT")

            column(name: "quantity_available_to_promise", type: "BIGINT")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-73") {
        createTable(tableName: "product_catalog") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "active", type: "BIT(1)")

            column(name: "code", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "LONGTEXT")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "color", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-74") {
        createTable(tableName: "product_catalog_item") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "active", type: "BIT(1)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "product_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "product_catalog_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-75") {
        createTable(tableName: "product_category") {
            column(name: "product_id", type: "CHAR(38)")

            column(name: "category_id", type: "CHAR(38)")

            column(name: "categories_idx", type: "INT")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-76") {
        createTable(tableName: "product_component") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "assembly_product_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "component_product_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "quantity", type: "DECIMAL(19, 2)") {
                constraints(nullable: "false")
            }

            column(name: "unit_of_measure_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "product_components_idx", type: "INT")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-79") {
        createTable(tableName: "product_dimension") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "abc_class", type: "VARCHAR(255)")

            column(name: "active", type: "BIT(1)") {
                constraints(nullable: "false")
            }

            column(name: "category_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "generic_product", type: "VARCHAR(255)")

            column(name: "product_code", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "product_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "product_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "unit_cost", type: "DECIMAL(19, 2)")

            column(name: "unit_price", type: "DECIMAL(19, 2)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-80") {
        createTable(tableName: "product_document") {
            column(name: "product_id", type: "CHAR(38)")

            column(name: "document_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-81") {
        createTable(tableName: "product_group") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "name", type: "VARCHAR(255)")

            column(name: "description", type: "VARCHAR(255)")

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "category_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-82") {
        createTable(tableName: "product_group_product") {
            column(name: "product_group_id", type: "CHAR(38)")

            column(name: "product_id", type: "CHAR(38)")

            column(name: "products_idx", type: "INT")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-83") {
        createTable(tableName: "product_merge_logger") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "primary_product_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "obsolete_product_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "related_object_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "related_object_class_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "date_merged", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "date_reverted", type: "datetime")

            column(name: "comments", type: "VARCHAR(255)")

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "created_by_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "updated_by_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-84") {
        createTable(tableName: "product_package") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "name", type: "VARCHAR(255)")

            column(name: "gtin", type: "VARCHAR(255)")

            column(name: "description", type: "VARCHAR(255)")

            column(name: "uom_id", type: "CHAR(38)")

            column(name: "product_id", type: "CHAR(38)")

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "created_by_id", type: "CHAR(38)")

            column(name: "updated_by_id", type: "CHAR(38)")

            column(name: "quantity", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "product_supplier_id", type: "CHAR(38)")

            column(name: "product_price_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-85") {
        createTable(tableName: "product_price") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "price", type: "DECIMAL(19, 4)") {
                constraints(nullable: "false")
            }

            column(name: "currency_id", type: "CHAR(38)")

            column(name: "from_date", type: "datetime")

            column(name: "to_date", type: "datetime")

            column(name: "date_created", type: "datetime")

            column(name: "last_updated", type: "datetime")

            column(name: "created_by_id", type: "CHAR(38)")

            column(name: "updated_by_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-86") {
        createTable(tableName: "product_supplier") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "brand_name", type: "VARCHAR(255)")

            column(name: "code", type: "VARCHAR(255)")

            column(name: "comments", type: "VARCHAR(255)")

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "LONGTEXT")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "manufacturer_id", type: "CHAR(38)")

            column(name: "manufacturer_code", type: "VARCHAR(255)")

            column(name: "manufacturer_name", type: "VARCHAR(255)")

            column(name: "min_order_quantity", type: "DECIMAL(19, 2)")

            column(name: "model_number", type: "VARCHAR(255)")

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "ndc", type: "VARCHAR(255)")

            column(name: "product_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "product_code", type: "VARCHAR(255)")

            column(name: "rating_type_code", type: "VARCHAR(255)")

            column(name: "standard_lead_time_days", type: "DECIMAL(19, 2)")

            column(name: "supplier_id", type: "CHAR(38)")

            column(name: "supplier_code", type: "VARCHAR(255)")

            column(name: "supplier_name", type: "VARCHAR(255)")

            column(name: "unit_of_measure_id", type: "CHAR(38)")

            column(name: "unit_price", type: "DECIMAL(19, 4)")

            column(name: "upc", type: "VARCHAR(255)")

            column(name: "unit_cost", type: "DECIMAL(19, 4)")

            column(name: "contract_price_id", type: "CHAR(38)")

            column(defaultValueBoolean: "true", name: "active", type: "BIT(1)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-87") {
        createTable(tableName: "product_supplier_preference") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "product_supplier_id", type: "CHAR(38)")

            column(name: "destination_party_id", type: "CHAR(38)")

            column(name: "preference_type_id", type: "CHAR(38)")

            column(name: "comments", type: "VARCHAR(255)")

            column(name: "validity_start_date", type: "datetime")

            column(name: "validity_end_date", type: "datetime")

            column(name: "date_created", type: "datetime")

            column(name: "last_updated", type: "datetime")

            column(name: "created_by_id", type: "CHAR(38)")

            column(name: "updated_by_id", type: "CHAR(38)")

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-88") {
        createTable(tableName: "product_tag") {
            column(name: "product_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "tag_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-89") {
        createTable(tableName: "product_type") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "product_type_code", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "product_identifier_format", type: "VARCHAR(255)")

            column(name: "code", type: "VARCHAR(255)")

            column(name: "sequence_number", type: "INT")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-90") {
        createTable(tableName: "product_type_displayed_fields") {
            column(name: "product_type_id", type: "CHAR(38)")

            column(name: "product_field", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-91") {
        createTable(tableName: "product_type_required_fields") {
            column(name: "product_type_id", type: "CHAR(38)")

            column(name: "product_field", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-92") {
        createTable(tableName: "product_type_supported_activities") {
            column(name: "product_type_id", type: "CHAR(38)")

            column(name: "product_activity_code", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-93") {
        createTable(tableName: "receipt") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "actual_delivery_date", type: "datetime")

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "expected_delivery_date", type: "datetime")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "recipient_id", type: "CHAR(38)")

            column(name: "shipment_id", type: "CHAR(38)")

            column(name: "receipt_number", type: "VARCHAR(255)")

            column(name: "receipt_status_code", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-94") {
        createTable(tableName: "receipt_item") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "comment_id", type: "CHAR(38)")

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "expiration_date", type: "datetime")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "lot_number", type: "VARCHAR(255)")

            column(name: "product_id", type: "CHAR(38)")

            column(name: "quantity_shipped", type: "INT")

            column(name: "quantity_received", type: "INT")

            column(name: "receipt_id", type: "CHAR(38)")

            column(name: "recipient_id", type: "CHAR(38)")

            column(name: "comment", type: "VARCHAR(255)")

            column(name: "shipment_item_id", type: "CHAR(38)")

            column(name: "inventory_item_id", type: "CHAR(38)")

            column(name: "bin_location_id", type: "CHAR(38)")

            column(name: "quantity_canceled", type: "INT")

            column(name: "is_split_item", type: "BIT(1)")

            column(name: "sort_order", type: "INT")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-95") {
        createTable(tableName: "reference_number") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "identifier", type: "VARCHAR(255)")

            column(name: "reference_number_type_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-96") {
        createTable(tableName: "reference_number_type") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "sort_order", type: "INT")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-97") {
        createTable(tableName: "requisition") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime")

            column(name: "date_requested", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "destination_id", type: "CHAR(38)")

            column(name: "last_updated", type: "datetime")

            column(name: "origin_id", type: "CHAR(38)")

            column(name: "recipient_id", type: "CHAR(38)")

            column(name: "request_number", type: "VARCHAR(255)")

            column(name: "requested_by_id", type: "CHAR(38)")

            column(name: "status", type: "VARCHAR(255)")

            column(name: "fulfillment_id", type: "CHAR(38)")

            column(name: "created_by_id", type: "CHAR(38)")

            column(name: "updated_by_id", type: "CHAR(38)")

            column(name: "name", type: "VARCHAR(255)")

            column(name: "date_valid_from", type: "datetime")

            column(name: "date_valid_to", type: "datetime")

            column(name: "recipient_program", type: "VARCHAR(255)")

            column(name: "requested_delivery_date", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR(255)")

            column(name: "commodity_class", type: "VARCHAR(255)")

            column(name: "is_template", type: "TINYINT(3)")

            column(name: "is_published", type: "TINYINT(3)")

            column(name: "date_published", type: "datetime")

            column(name: "verified_by_id", type: "CHAR(38)")

            column(name: "date_verified", type: "datetime")

            column(name: "reviewed_by_id", type: "CHAR(38)")

            column(name: "date_reviewed", type: "datetime")

            column(name: "delivered_by_id", type: "CHAR(38)")

            column(name: "date_delivered", type: "datetime")

            column(name: "received_by_id", type: "CHAR(38)")

            column(name: "date_received", type: "datetime")

            column(name: "checked_by_id", type: "CHAR(38)")

            column(name: "date_checked", type: "datetime")

            column(name: "issued_by_id", type: "CHAR(38)")

            column(name: "date_issued", type: "datetime")

            column(name: "requisition_template_id", type: "CHAR(38)")

            column(name: "replenishment_period", type: "INT")

            column(name: "sort_by_code", type: "VARCHAR(255)")

            column(name: "source_type", type: "VARCHAR(255)")

            column(name: "replenishment_type_code", type: "VARCHAR(255)")

            column(name: "approved_by_id", type: "CHAR(38)")

            column(name: "date_approved", type: "datetime")

            column(name: "date_rejected", type: "datetime")

            column(name: "approval_required", type: "BIT(1)")

            column(name: "rejected_by_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-98") {
        createTable(tableName: "requisition_approvers") {
            column(name: "requisition_id", type: "CHAR(38)")

            column(name: "person_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-99") {
        createTable(tableName: "requisition_comment") {
            column(name: "requisition_id", type: "CHAR(38)")

            column(name: "comment_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-100") {
        createTable(tableName: "requisition_event") {
            column(name: "requisition_id", type: "CHAR(38)")

            column(name: "event_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-101") {
        createTable(tableName: "requisition_item") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "category_id", type: "CHAR(38)")

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "inventory_item_id", type: "CHAR(38)")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "product_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "quantity", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "requisition_id", type: "CHAR(38)")

            column(name: "requested_by_id", type: "CHAR(38)")

            column(name: "unit_price", type: "FLOAT(12)")

            column(name: "product_group_id", type: "CHAR(38)")

            column(name: "comment", type: "VARCHAR(255)")

            column(name: "recipient_id", type: "CHAR(38)")

            column(name: "substitutable", type: "BIT(1)") {
                constraints(nullable: "false")
            }

            column(name: "order_index", type: "INT")

            column(name: "requisition_items_idx", type: "INT")

            column(name: "quantity_canceled", type: "INT")

            column(name: "cancel_reason_code", type: "VARCHAR(255)")

            column(name: "cancel_comments", type: "VARCHAR(255)")

            column(name: "parent_requisition_item_id", type: "CHAR(38)")

            column(name: "product_package_id", type: "CHAR(38)")

            column(name: "requisition_item_type", type: "VARCHAR(255)")

            column(name: "quantity_approved", type: "INT")

            column(name: "created_by_id", type: "CHAR(38)")

            column(name: "updated_by_id", type: "CHAR(38)")

            column(name: "substitution_item_id", type: "CHAR(38)")

            column(name: "modification_item_id", type: "CHAR(38)")

            column(name: "pallet_name", type: "VARCHAR(255)")

            column(name: "box_name", type: "VARCHAR(255)")

            column(name: "lot_number", type: "VARCHAR(255)")

            column(name: "expiration_date", type: "datetime")

            column(name: "pick_reason_code", type: "VARCHAR(255)")

            column(name: "quantity_counted", type: "INT")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-102") {
        createTable(tableName: "role") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "role_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-103") {
        createTable(tableName: "shipment") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "carrier_id", type: "CHAR(38)")

            column(name: "date_created", type: "datetime")

            column(name: "destination_id", type: "CHAR(38)")

            column(name: "donor_id", type: "CHAR(38)")

            column(name: "expected_delivery_date", type: "datetime")

            column(name: "expected_shipping_date", type: "datetime")

            column(name: "last_updated", type: "datetime")

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "origin_id", type: "CHAR(38)")

            column(name: "recipient_id", type: "CHAR(38)")

            column(name: "shipment_method_id", type: "CHAR(38)")

            column(name: "shipment_number", type: "VARCHAR(255)")

            column(name: "shipment_type_id", type: "CHAR(38)")

            column(name: "total_value", type: "FLOAT(12, 2)")

            column(name: "additional_information", type: "LONGTEXT")

            column(name: "stated_value", type: "FLOAT(12, 2)")

            column(name: "weight", type: "FLOAT(12)")

            column(name: "weight_units", type: "VARCHAR(255)")

            column(name: "current_status", type: "VARCHAR(255)")

            column(name: "current_event_id", type: "CHAR(38)")

            column(name: "created_by_id", type: "CHAR(38)")

            column(name: "updated_by_id", type: "CHAR(38)")

            column(name: "requisition_id", type: "CHAR(38)")

            column(name: "description", type: "TEXT")

            column(name: "driver_name", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-104") {
        createTable(tableName: "shipment_comment") {
            column(name: "shipment_comments_id", type: "CHAR(38)")

            column(name: "comment_id", type: "CHAR(38)")

            column(name: "comments_idx", type: "INT")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-105") {
        createTable(tableName: "shipment_document") {
            column(name: "shipment_documents_id", type: "CHAR(38)")

            column(name: "document_id", type: "CHAR(38)")

            column(name: "documents_idx", type: "INT")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-106") {
        createTable(tableName: "shipment_event") {
            column(name: "shipment_events_id", type: "CHAR(38)")

            column(name: "event_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-107") {
        createTable(tableName: "shipment_invoice") {
            column(name: "invoice_item_id", type: "CHAR(38)")

            column(name: "shipment_item_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-108") {
        createTable(tableName: "shipment_item") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "container_id", type: "CHAR(38)")

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "donor_id", type: "CHAR(38)")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "lot_number", type: "VARCHAR(255)")

            column(name: "product_id", type: "CHAR(38)")

            column(name: "quantity", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "recipient_id", type: "CHAR(38)")

            column(name: "shipment_items_idx", type: "INT")

            column(name: "shipment_id", type: "CHAR(38)")

            column(name: "expiration_date", type: "datetime")

            column(name: "inventory_item_id", type: "CHAR(38)")

            column(name: "bin_location_id", type: "CHAR(38)")

            column(name: "requisition_item_id", type: "CHAR(38)")

            column(name: "sort_order", type: "INT")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-109") {
        createTable(tableName: "shipment_method") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "shipper_service_id", type: "CHAR(38)")

            column(name: "tracking_number", type: "VARCHAR(255)")

            column(name: "shipper_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-110") {
        createTable(tableName: "shipment_reference_number") {
            column(name: "shipment_reference_numbers_id", type: "CHAR(38)")

            column(name: "reference_number_id", type: "CHAR(38)")

            column(name: "reference_numbers_idx", type: "INT")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-111") {
        createTable(tableName: "shipment_type") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "sort_order", type: "INT")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-112") {
        createTable(tableName: "shipment_workflow") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "shipment_type_id", type: "CHAR(38)")

            column(name: "excluded_fields", type: "VARCHAR(255)")

            column(name: "document_template", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-113") {
        createTable(tableName: "shipment_workflow_container_type") {
            column(name: "shipment_workflow_container_types_id", type: "CHAR(38)")

            column(name: "container_type_id", type: "CHAR(38)")

            column(name: "container_types_idx", type: "INT")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-114") {
        createTable(tableName: "shipment_workflow_document") {
            column(name: "shipment_workflow_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "document_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-115") {
        createTable(tableName: "shipment_workflow_document_template") {
            column(name: "shipment_workflow_id", type: "CHAR(38)")

            column(name: "document_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-116") {
        createTable(tableName: "shipment_workflow_reference_number_type") {
            column(name: "shipment_workflow_reference_number_types_id", type: "CHAR(38)")

            column(name: "reference_number_type_id", type: "CHAR(38)")

            column(name: "reference_number_types_idx", type: "INT")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-117") {
        createTable(tableName: "shipper") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime")

            column(name: "description", type: "VARCHAR(255)")

            column(name: "last_updated", type: "datetime")

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "parameter_name", type: "VARCHAR(255)")

            column(name: "tracking_format", type: "VARCHAR(255)")

            column(name: "tracking_url", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-118") {
        createTable(tableName: "shipper_service") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "name", type: "VARCHAR(255)")

            column(name: "shipper_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-120") {
        createTable(tableName: "synonym") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "product_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "created_by_id", type: "CHAR(38)")

            column(name: "updated_by_id", type: "CHAR(38)")

            column(name: "locale", type: "VARCHAR(50)")

            column(defaultValue: "ALTERNATE_NAME", name: "synonym_type_code", type: "VARCHAR(100)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-121") {
        createTable(tableName: "tag") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "created_by_id", type: "CHAR(38)")

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "tag", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "updated_by_id", type: "CHAR(38)")

            column(name: "is_active", type: "TINYINT(3)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-122") {
        createTable(tableName: "transaction") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "created_by_id", type: "CHAR(38)")

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "destination_id", type: "CHAR(38)")

            column(name: "inventory_id", type: "CHAR(38)")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "source_id", type: "CHAR(38)")

            column(name: "transaction_date", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "transaction_type_id", type: "CHAR(38)")

            column(name: "confirmed", type: "BIT(1)")

            column(name: "confirmed_by_id", type: "CHAR(38)")

            column(name: "date_confirmed", type: "datetime")

            column(name: "comment", type: "VARCHAR(255)")

            column(name: "incoming_shipment_id", type: "CHAR(38)")

            column(name: "outgoing_shipment_id", type: "CHAR(38)")

            column(name: "updated_by_id", type: "CHAR(38)")

            column(name: "transaction_number", type: "VARCHAR(255)")

            column(name: "requisition_id", type: "CHAR(38)")

            column(name: "order_id", type: "CHAR(38)")

            column(name: "receipt_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-123") {
        createTable(tableName: "transaction_entry") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "inventory_item_id", type: "CHAR(38)")

            column(name: "quantity", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "transaction_id", type: "CHAR(38)")

            column(name: "comments", type: "VARCHAR(255)")

            column(name: "transaction_entries_idx", type: "INT")

            column(name: "bin_location_id", type: "CHAR(38)")

            column(name: "product_id", type: "CHAR(38)")

            column(name: "reason_code", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-124") {
        createTable(tableName: "transaction_fact") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "location_key_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "lot_key_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "product_key_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "quantity", type: "DECIMAL(19, 2)")

            column(name: "transaction_date", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "transaction_date_key_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "transaction_number", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "transaction_type_key_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-125") {
        createTable(tableName: "transaction_type") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "sort_order", type: "INT")

            column(name: "transaction_code", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-126") {
        createTable(tableName: "transaction_type_dimension") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "transaction_code", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "transaction_type_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "transaction_type_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-127") {
        createTable(tableName: "unit_of_measure") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "code", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "uom_class_id", type: "CHAR(38)")

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "created_by_id", type: "CHAR(38)")

            column(name: "updated_by_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-128") {
        createTable(tableName: "unit_of_measure_class") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(defaultValueBoolean: "true", name: "active", type: "BIT(1)")

            column(name: "name", type: "VARCHAR(255)")

            column(name: "code", type: "VARCHAR(255)")

            column(name: "description", type: "VARCHAR(255)")

            column(name: "base_uom_id", type: "CHAR(38)")

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR(255)")

            column(name: "created_by_id", type: "CHAR(38)")

            column(name: "updated_by_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-129") {
        createTable(tableName: "unit_of_measure_conversion") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "active", type: "BIT(1)") {
                constraints(nullable: "false")
            }

            column(name: "from_unit_of_measure_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "to_unit_of_measure_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "conversion_rate", type: "DECIMAL(19, 8)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-130") {
        createTable(tableName: "user") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "last_login_date", type: "datetime")

            column(name: "manager_id", type: "CHAR(38)")

            column(name: "password", type: "VARCHAR(255)")

            column(name: "username", type: "VARCHAR(255)") {
                constraints(unique: "true")
            }

            column(name: "warehouse_id", type: "CHAR(38)")

            column(name: "photo", type: "MEDIUMBLOB")

            column(name: "locale", type: "VARCHAR(255)")

            column(name: "remember_last_location", type: "BIT(1)")

            column(name: "timezone", type: "VARCHAR(255)")

            column(name: "dashboard_config", type: "LONGBLOB")
        }
    }

    changeSet(author: "openboxes (generated)", id: "1700664714834-131") {
        createTable(tableName: "user_role") {
            column(name: "user_id", type: "CHAR(38)")

            column(name: "role_id", type: "CHAR(38)")
        }
    }

}
