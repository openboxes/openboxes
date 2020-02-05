databaseChangeLog = {

    changeSet(author: "jmiranda (generated)", id: "1580360689181-1") {
        createTable(tableName: "address") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-2") {
        createTable(tableName: "attribute") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-3") {
        createTable(tableName: "attribute_options") {
            column(name: "attribute_id", type: "CHAR(38)")

            column(name: "options_string", type: "VARCHAR(255)")

            column(name: "options_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-4") {
        createTable(tableName: "category") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-5") {
        createTable(tableName: "click_stream") {
            column(name: "id", type: "BIGINT") {
                constraints(primaryKey: "true")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "hostname", type: "VARCHAR(255)")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "referrer", type: "VARCHAR(255)")

            column(name: "session_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-6") {
        createTable(tableName: "click_stream_request") {
            column(name: "id", type: "BIGINT") {
                constraints(primaryKey: "true")
            }

            column(name: "click_stream_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "port", type: "INT")

            column(name: "protocol", type: "VARCHAR(8)") {
                constraints(nullable: "false")
            }

            column(name: "query", type: "VARCHAR(255)")

            column(name: "server", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "uri", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "person", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-7") {
        createTable(tableName: "comment") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-8") {
        createTable(tableName: "consumption") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-9") {
        createTable(tableName: "consumption_fact") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-10") {
        createTable(tableName: "container") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-11") {
        createTable(tableName: "container_type") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-12") {
        createTable(tableName: "date_dimension") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-13") {
        createTable(tableName: "document") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

            column(name: "file_uri", type: "TINYBLOB")

            column(name: "filename", type: "VARCHAR(255)")

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-14") {
        createTable(tableName: "document_type") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-15") {
        createTable(tableName: "donor") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-16") {
        createTable(tableName: "event") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-17") {
        createTable(tableName: "event_type") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-18") {
        createTable(tableName: "fulfillment") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-19") {
        createTable(tableName: "fulfillment_item") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-20") {
        createTable(tableName: "fulfillment_item_shipment_item") {
            column(name: "fulfillment_item_shipment_items_id", type: "CHAR(38)")

            column(name: "shipment_item_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-21") {
        createTable(tableName: "indicator") {
            column(name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-22") {
        createTable(tableName: "inventory") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "last_inventory_date", type: "datetime")

            column(name: "date_created", type: "datetime")

            column(name: "last_updated", type: "datetime")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-23") {
        createTable(tableName: "inventory_item") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-24") {
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-25") {
        createTable(tableName: "inventory_level") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-26") {
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-27") {
        createTable(tableName: "local_transfer") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-28") {
        createTable(tableName: "localization") {
            column(name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-29") {
        createTable(tableName: "location") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

            column(name: "local", type: "BIT(1)")

            column(name: "bg_color", type: "VARCHAR(255)")

            column(name: "fg_color", type: "VARCHAR(255)")

            column(name: "location_group_id", type: "CHAR(38)")

            column(defaultValueNumeric: "0", name: "sort_order", type: "INT")

            column(name: "location_number", type: "VARCHAR(255)")

            column(name: "description", type: "VARCHAR(255)")

            column(name: "organization_id", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-30") {
        createTable(tableName: "location_dimension") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "location_group_name", type: "VARCHAR(255)")

            column(name: "location_id", type: "VARCHAR(255)") {
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-31") {
        createTable(tableName: "location_group") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-32") {
        createTable(tableName: "location_role") {
            column(name: "user_id", type: "CHAR(38)")

            column(name: "location_id", type: "CHAR(38)")

            column(name: "role_id", type: "CHAR(38)")

            column(name: "version", type: "INT")

            column(name: "id", type: "CHAR(38)")

            column(name: "location_roles_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-33") {
        createTable(tableName: "location_supported_activities") {
            column(name: "location_id", type: "CHAR(38)")

            column(name: "supported_activities_string", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-34") {
        createTable(tableName: "location_type") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-35") {
        createTable(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", type: "CHAR(38)")

            column(name: "supported_activities_string", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-36") {
        createTable(tableName: "lot_dimension") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "expiration_date", type: "datetime")

            column(name: "inventory_item_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "lot_number", type: "VARCHAR(255)")

            column(name: "product_code", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-37") {
        createTable(tableName: "order") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

            column(defaultValue: "PURCHASE_ORDER", name: "order_type_code", type: "VARCHAR(100)")

            column(name: "currency_code", type: "CHAR(3)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-38") {
        createTable(tableName: "order_comment") {
            column(name: "order_comments_id", type: "CHAR(38)")

            column(name: "comment_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-39") {
        createTable(tableName: "order_document") {
            column(name: "order_documents_id", type: "CHAR(38)")

            column(name: "document_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-40") {
        createTable(tableName: "order_event") {
            column(name: "order_events_id", type: "CHAR(38)")

            column(name: "event_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-41") {
        createTable(tableName: "order_item") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-42") {
        createTable(tableName: "order_item_comment") {
            column(name: "order_item_comments_id", type: "CHAR(38)")

            column(name: "comment_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-43") {
        createTable(tableName: "order_shipment") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "order_item_id", type: "CHAR(38)")

            column(name: "shipment_item_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-44") {
        createTable(tableName: "party") {
            column(name: "id", type: "VARCHAR(255)") {
                constraints(primaryKey: "true")
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

            column(name: "party_type_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "class", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "code", type: "VARCHAR(255)")

            column(name: "description", type: "VARCHAR(255)")

            column(name: "name", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-45") {
        createTable(tableName: "party_role") {
            column(name: "id", type: "VARCHAR(255)") {
                constraints(primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "end_date", type: "datetime")

            column(name: "party_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "role_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "start_date", type: "datetime")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-46") {
        createTable(tableName: "party_type") {
            column(name: "id", type: "VARCHAR(255)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-47") {
        createTable(tableName: "person") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-48") {
        createTable(tableName: "picklist") {
            column(name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-49") {
        createTable(tableName: "picklist_item") {
            column(name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-50") {
        createTable(tableName: "product") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-51") {
        createTable(tableName: "product_association") {
            column(name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-52") {
        createTable(tableName: "product_attribute") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "attribute_id", type: "CHAR(38)")

            column(name: "product_id", type: "CHAR(38)")

            column(name: "attributes_idx", type: "INT")

            column(name: "value", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-53") {
        createTable(tableName: "product_catalog") {
            column(name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "active", type: "BIT(1)")

            column(name: "code", type: "VARCHAR(255)") {
                constraints(nullable: "false", unique: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-54") {
        createTable(tableName: "product_catalog_item") {
            column(name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-55") {
        createTable(tableName: "product_category") {
            column(name: "product_id", type: "CHAR(38)")

            column(name: "category_id", type: "CHAR(38)")

            column(name: "categories_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-56") {
        createTable(tableName: "product_component") {
            column(name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-57") {
        createTable(tableName: "product_demand_details") {
            column(defaultValue: "", name: "request_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "request_status", type: "VARCHAR(255)")

            column(name: "request_number", type: "VARCHAR(255)")

            column(name: "date_created", type: "datetime")

            column(name: "date_requested", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "date_issued", type: "datetime")

            column(defaultValue: "", name: "origin_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "origin_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(defaultValue: "", name: "destination_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "destination_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(defaultValue: "", name: "request_item_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(defaultValue: "", name: "product_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "product_code", type: "VARCHAR(255)")

            column(name: "product_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "quantity_requested", type: "INT") {
                constraints(nullable: "false")
            }

            column(defaultValueNumeric: "0", name: "quantity_canceled", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(defaultValueNumeric: "0", name: "quantity_approved", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(defaultValueNumeric: "0", name: "quantity_modified", type: "DECIMAL(32)") {
                constraints(nullable: "false")
            }

            column(defaultValueNumeric: "0", name: "quantity_substituted", type: "DECIMAL(32)") {
                constraints(nullable: "false")
            }

            column(defaultValueNumeric: "0", name: "quantity_picked", type: "DECIMAL(32)") {
                constraints(nullable: "false")
            }

            column(defaultValueNumeric: "0", name: "quantity_demand", type: "DECIMAL(32)") {
                constraints(nullable: "false")
            }

            column(name: "reason_code_classification", type: "VARCHAR(31)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-58") {
        createTable(tableName: "product_dimension") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true")
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

            column(name: "product_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "product_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "unit_cost", type: "DECIMAL(19, 2)")

            column(name: "unit_price", type: "DECIMAL(19, 2)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-59") {
        createTable(tableName: "product_document") {
            column(name: "product_id", type: "CHAR(38)")

            column(name: "document_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-60") {
        createTable(tableName: "product_group") {
            column(name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-61") {
        createTable(tableName: "product_group_product") {
            column(name: "product_group_id", type: "CHAR(38)")

            column(name: "product_id", type: "CHAR(38)")

            column(name: "products_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-62") {
        createTable(tableName: "product_package") {
            column(name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

            column(name: "price", type: "DECIMAL(19, 4)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-63") {
        createTable(tableName: "product_supplier") {
            column(name: "id", type: "VARCHAR(255)") {
                constraints(primaryKey: "true")
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

            column(name: "manufacturer_id", type: "VARCHAR(255)")

            column(name: "manufacturer_code", type: "VARCHAR(255)")

            column(name: "manufacturer_name", type: "VARCHAR(255)")

            column(name: "min_order_quantity", type: "DECIMAL(19, 2)")

            column(name: "model_number", type: "VARCHAR(255)")

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "ndc", type: "VARCHAR(255)")

            column(name: "preference_type_code", type: "VARCHAR(255)")

            column(name: "product_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "product_code", type: "VARCHAR(255)")

            column(name: "rating_type_code", type: "VARCHAR(255)")

            column(name: "standard_lead_time_days", type: "DECIMAL(19, 2)")

            column(name: "supplier_id", type: "VARCHAR(255)")

            column(name: "supplier_code", type: "VARCHAR(255)")

            column(name: "supplier_name", type: "VARCHAR(255)")

            column(name: "unit_of_measure_id", type: "VARCHAR(255)")

            column(name: "unit_price", type: "DECIMAL(19, 4)")

            column(name: "upc", type: "VARCHAR(255)")

            column(name: "unit_cost", type: "DECIMAL(19, 4)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-64") {
        createTable(tableName: "product_tag") {
            column(name: "product_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "tag_id", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-65") {
        createTable(tableName: "product_type") {
            column(name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-66") {
        createTable(tableName: "receipt") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-67") {
        createTable(tableName: "receipt_item") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-68") {
        createTable(tableName: "reference_number") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "identifier", type: "VARCHAR(255)")

            column(name: "reference_number_type_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-69") {
        createTable(tableName: "reference_number_type") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-70") {
        createTable(tableName: "requisition") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-71") {
        createTable(tableName: "requisition_item") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

            column(name: "recipient_id", type: "VARCHAR(255)")

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
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-72") {
        createTable(tableName: "role") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "role_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-73") {
        createTable(tableName: "shipment") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-74") {
        createTable(tableName: "shipment_comment") {
            column(name: "shipment_comments_id", type: "CHAR(38)")

            column(name: "comment_id", type: "CHAR(38)")

            column(name: "comments_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-75") {
        createTable(tableName: "shipment_document") {
            column(name: "shipment_documents_id", type: "CHAR(38)")

            column(name: "document_id", type: "CHAR(38)")

            column(name: "documents_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-76") {
        createTable(tableName: "shipment_event") {
            column(name: "shipment_events_id", type: "CHAR(38)")

            column(name: "event_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-77") {
        createTable(tableName: "shipment_item") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-78") {
        createTable(tableName: "shipment_method") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-79") {
        createTable(tableName: "shipment_reference_number") {
            column(name: "shipment_reference_numbers_id", type: "CHAR(38)")

            column(name: "reference_number_id", type: "CHAR(38)")

            column(name: "reference_numbers_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-80") {
        createTable(tableName: "shipment_type") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-81") {
        createTable(tableName: "shipment_workflow") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-82") {
        createTable(tableName: "shipment_workflow_container_type") {
            column(name: "shipment_workflow_container_types_id", type: "CHAR(38)")

            column(name: "container_type_id", type: "CHAR(38)")

            column(name: "container_types_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-83") {
        createTable(tableName: "shipment_workflow_document") {
            column(name: "shipment_workflow_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "document_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-84") {
        createTable(tableName: "shipment_workflow_document_template") {
            column(name: "shipment_workflow_id", type: "CHAR(38)")

            column(name: "document_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-85") {
        createTable(tableName: "shipment_workflow_reference_number_type") {
            column(name: "shipment_workflow_reference_number_types_id", type: "CHAR(38)")

            column(name: "reference_number_type_id", type: "CHAR(38)")

            column(name: "reference_number_types_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-86") {
        createTable(tableName: "shipper") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-87") {
        createTable(tableName: "shipper_service") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)")

            column(name: "name", type: "VARCHAR(255)")

            column(name: "shipper_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-88") {
        createTable(tableName: "synonym") {
            column(name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-89") {
        createTable(tableName: "tag") {
            column(name: "id", type: "VARCHAR(255)") {
                constraints(primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "created_by_id", type: "VARCHAR(255)")

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "tag", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "updated_by_id", type: "VARCHAR(255)")

            column(name: "is_active", type: "TINYINT(3)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-90") {
        createTable(tableName: "transaction") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-91") {
        createTable(tableName: "transaction_entry") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-92") {
        createTable(tableName: "transaction_fact") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-93") {
        createTable(tableName: "transaction_type") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-94") {
        createTable(tableName: "transaction_type_dimension") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "transaction_code", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "transaction_type_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "transaction_type_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-95") {
        createTable(tableName: "unit_of_measure") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-96") {
        createTable(tableName: "unit_of_measure_class") {
            column(name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1580360689181-97") {
        createTable(tableName: "user") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
            }

            column(name: "active", type: "BIT(1)")

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
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1580360689181-98") {
        createTable(tableName: "user_role") {
            column(name: "user_id", type: "CHAR(38)")

            column(name: "role_id", type: "CHAR(38)")
        }
    }

}
