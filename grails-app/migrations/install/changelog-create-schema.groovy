databaseChangeLog = {

    changeSet(author: "jmiranda (generated)", id: "1692045990425-1") {
        createTable(tableName: "address") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-2") {
        createTable(tableName: "attribute") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-3") {
        createTable(tableName: "attribute_entity_type_codes") {
            column(name: "attribute_id", type: "CHAR(38)")

            column(name: "entity_type_code", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-4") {
        createTable(tableName: "attribute_options") {
            column(name: "attribute_id", type: "CHAR(38)")

            column(name: "options_string", type: "VARCHAR(255)")

            column(name: "options_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-5") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-6") {
        createTable(tableName: "category") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-7") {
        createTable(tableName: "click_stream") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-8") {
        createTable(tableName: "click_stream_request") {
            column(name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-9") {
        createTable(tableName: "comment") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-10") {
        createTable(tableName: "consumption") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-11") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-12") {
        createTable(tableName: "container") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-13") {
        createTable(tableName: "container_type") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-14") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-15") {
        createTable(tableName: "document") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-16") {
        createTable(tableName: "document_type") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-17") {
        createTable(tableName: "donor") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-18") {
        createTable(tableName: "event") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-19") {
        createTable(tableName: "event_type") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-20") {
        createTable(tableName: "fulfillment") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-21") {
        createTable(tableName: "fulfillment_item") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-22") {
        createTable(tableName: "fulfillment_item_shipment_item") {
            column(name: "fulfillment_item_shipment_items_id", type: "CHAR(38)")

            column(name: "shipment_item_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-23") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-24") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-25") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-26") {
        createTable(tableName: "inventory") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-27") {
        createTable(tableName: "inventory_item") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-28") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-29") {
        createTable(tableName: "inventory_level") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-30") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-31") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-32") {
        createTable(tableName: "invoice_document") {
            column(name: "invoice_id", type: "CHAR(38)")

            column(name: "document_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-33") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-34") {
        createTable(tableName: "invoice_reference_number") {
            column(name: "invoice_reference_numbers_id", type: "CHAR(38)")

            column(name: "reference_number_id", type: "CHAR(38)")

            column(name: "reference_numbers_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-35") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-36") {
        createTable(tableName: "local_transfer") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-37") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-38") {
        createTable(tableName: "location") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

            column(name: "organization_id", type: "VARCHAR(255)")

            column(name: "zone_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-39") {
        createTable(tableName: "location_dimension") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-40") {
        createTable(tableName: "location_group") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-41") {
        createTable(tableName: "location_role") {
            column(name: "user_id", type: "CHAR(38)")

            column(name: "location_id", type: "CHAR(38)")

            column(name: "role_id", type: "CHAR(38)")

            column(name: "version", type: "INT")

            column(name: "id", type: "CHAR(38)")

            column(name: "location_roles_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-42") {
        createTable(tableName: "location_supported_activities") {
            column(name: "location_id", type: "CHAR(38)")

            column(name: "supported_activities_string", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-43") {
        createTable(tableName: "location_type") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-44") {
        createTable(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", type: "CHAR(38)")

            column(name: "supported_activities_string", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-45") {
        createTable(tableName: "lot_dimension") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-46") {
        createTable(tableName: "order") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-47") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-48") {
        createTable(tableName: "order_adjustment_invoice") {
            column(name: "invoice_item_id", type: "CHAR(38)")

            column(name: "order_adjustment_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-49") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-50") {
        createTable(tableName: "order_comment") {
            column(name: "order_comments_id", type: "CHAR(38)")

            column(name: "comment_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-51") {
        createTable(tableName: "order_document") {
            column(name: "order_documents_id", type: "CHAR(38)")

            column(name: "document_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-52") {
        createTable(tableName: "order_event") {
            column(name: "order_events_id", type: "CHAR(38)")

            column(name: "event_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-53") {
        createTable(tableName: "order_invoice") {
            column(name: "invoice_item_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "order_item_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-54") {
        createTable(tableName: "order_item") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-55") {
        createTable(tableName: "order_item_comment") {
            column(name: "order_item_comments_id", type: "CHAR(38)")

            column(name: "comment_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-56") {
        createTable(tableName: "order_shipment") {
            column(name: "order_item_id", type: "CHAR(38)")

            column(name: "shipment_item_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-57") {
        createTable(tableName: "order_summary_mv") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", unique: "true")
            }

            column(defaultValue: "", name: "order_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "quantity_ordered", type: "DECIMAL(65)")

            column(name: "adjustments_count", type: "DECIMAL(45)")

            column(name: "quantity_shipped", type: "DECIMAL(65, 4)")

            column(name: "quantity_received", type: "DECIMAL(65, 4)")

            column(name: "quantity_canceled", type: "DECIMAL(65, 4)")

            column(name: "quantity_invoiced", type: "DECIMAL(65, 2)")

            column(name: "adjustments_invoiced", type: "DECIMAL(63, 2)")

            column(name: "items_ordered", type: "DECIMAL(45)")

            column(name: "items_shipped", type: "DECIMAL(45)")

            column(name: "items_received", type: "DECIMAL(45)")

            column(name: "items_invoiced", type: "DECIMAL(45)")

            column(name: "order_status", type: "VARCHAR(255)")

            column(name: "shipment_status", type: "VARCHAR(17)")

            column(name: "receipt_status", type: "VARCHAR(18)")

            column(name: "payment_status", type: "VARCHAR(18)")

            column(name: "derived_status", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-58") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-59") {
        createTable(tableName: "organization_sequences") {
            column(name: "sequences", type: "VARCHAR(255)")

            column(name: "sequences_idx", type: "VARCHAR(255)")

            column(name: "sequences_elt", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-60") {
        createTable(tableName: "party") {
            column(name: "id", type: "VARCHAR(255)") {
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

            column(name: "party_type_id", type: "VARCHAR(255)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-61") {
        createTable(tableName: "party_role") {
            column(name: "id", type: "VARCHAR(255)") {
                constraints(nullable: "false", primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-62") {
        createTable(tableName: "party_type") {
            column(name: "id", type: "VARCHAR(255)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "code", type: "VARCHAR(255)") {
                constraints(nullable: "false", unique: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-63") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-64") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-65") {
        createTable(tableName: "person") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-66") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-67") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-68") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-69") {
        createTable(tableName: "product") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-70") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-71") {
        createTable(tableName: "product_attribute") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-72") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-73") {
        createTable(tableName: "product_catalog") {
            column(name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-74") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-75") {
        createTable(tableName: "product_category") {
            column(name: "product_id", type: "CHAR(38)")

            column(name: "category_id", type: "CHAR(38)")

            column(name: "categories_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-76") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-77") {
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

            column(name: "request_item_id", type: "CHAR(38)")

            column(defaultValue: "", name: "product_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "product_code", type: "VARCHAR(255)")

            column(name: "product_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "quantity_requested", type: "INT")

            column(defaultValueNumeric: "0", name: "quantity_canceled", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(defaultValueNumeric: "0", name: "quantity_approved", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(defaultValueNumeric: "0", name: "quantity_modified", type: "DECIMAL(32)") {
                constraints(nullable: "false")
            }

            column(defaultValueNumeric: "0", name: "quantity_picked", type: "DECIMAL(32)") {
                constraints(nullable: "false")
            }

            column(name: "quantity_demand", type: "DECIMAL(32)")

            column(name: "reason_code", type: "VARCHAR(255)")

            column(name: "reason_code_classification", type: "VARCHAR(31)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-78") {
        createTable(tableName: "product_demand_details_tmp") {
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

            column(name: "request_item_id", type: "CHAR(38)")

            column(defaultValue: "", name: "product_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "product_code", type: "VARCHAR(255)")

            column(name: "product_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "quantity_requested", type: "INT")

            column(defaultValueNumeric: "0", name: "quantity_canceled", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(defaultValueNumeric: "0", name: "quantity_approved", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(defaultValueNumeric: "0", name: "quantity_modified", type: "DECIMAL(32)") {
                constraints(nullable: "false")
            }

            column(defaultValueNumeric: "0", name: "quantity_picked", type: "DECIMAL(32)") {
                constraints(nullable: "false")
            }

            column(name: "quantity_demand", type: "DECIMAL(32)")

            column(name: "reason_code", type: "VARCHAR(255)")

            column(name: "reason_code_classification", type: "VARCHAR(31)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-79") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-80") {
        createTable(tableName: "product_document") {
            column(name: "product_id", type: "CHAR(38)")

            column(name: "document_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-81") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-82") {
        createTable(tableName: "product_group_product") {
            column(name: "product_group_id", type: "CHAR(38)")

            column(name: "product_id", type: "CHAR(38)")

            column(name: "products_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-83") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-84") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-85") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-86") {
        createTable(tableName: "product_supplier") {
            column(name: "id", type: "VARCHAR(255)") {
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

            column(name: "manufacturer_id", type: "VARCHAR(255)")

            column(name: "manufacturer_code", type: "VARCHAR(255)")

            column(name: "manufacturer_name", type: "VARCHAR(255)")

            column(name: "min_order_quantity", type: "DECIMAL(19, 2)")

            column(name: "model_number", type: "VARCHAR(255)")

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "ndc", type: "VARCHAR(255)")

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

            column(name: "contract_price_id", type: "CHAR(38)")

            column(defaultValueBoolean: "true", name: "active", type: "BIT(1)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-87") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-88") {
        createTable(tableName: "product_tag") {
            column(name: "product_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "tag_id", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-89") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-90") {
        createTable(tableName: "product_type_displayed_fields") {
            column(name: "product_type_id", type: "CHAR(38)")

            column(name: "product_field", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-91") {
        createTable(tableName: "product_type_required_fields") {
            column(name: "product_type_id", type: "CHAR(38)")

            column(name: "product_field", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-92") {
        createTable(tableName: "product_type_supported_activities") {
            column(name: "product_type_id", type: "CHAR(38)")

            column(name: "product_activity_code", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-93") {
        createTable(tableName: "receipt") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-94") {
        createTable(tableName: "receipt_item") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-95") {
        createTable(tableName: "reference_number") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "identifier", type: "VARCHAR(255)")

            column(name: "reference_number_type_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-96") {
        createTable(tableName: "reference_number_type") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-97") {
        createTable(tableName: "requisition") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-98") {
        createTable(tableName: "requisition_item") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

            column(name: "quantity_counted", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-99") {
        createTable(tableName: "role") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
                constraints(nullable: "false", primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-100") {
        createTable(tableName: "shipment") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-101") {
        createTable(tableName: "shipment_comment") {
            column(name: "shipment_comments_id", type: "CHAR(38)")

            column(name: "comment_id", type: "CHAR(38)")

            column(name: "comments_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-102") {
        createTable(tableName: "shipment_document") {
            column(name: "shipment_documents_id", type: "CHAR(38)")

            column(name: "document_id", type: "CHAR(38)")

            column(name: "documents_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-103") {
        createTable(tableName: "shipment_event") {
            column(name: "shipment_events_id", type: "CHAR(38)")

            column(name: "event_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-104") {
        createTable(tableName: "shipment_invoice") {
            column(name: "invoice_item_id", type: "CHAR(38)")

            column(name: "shipment_item_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-105") {
        createTable(tableName: "shipment_item") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-106") {
        createTable(tableName: "shipment_method") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-107") {
        createTable(tableName: "shipment_reference_number") {
            column(name: "shipment_reference_numbers_id", type: "CHAR(38)")

            column(name: "reference_number_id", type: "CHAR(38)")

            column(name: "reference_numbers_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-108") {
        createTable(tableName: "shipment_type") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-109") {
        createTable(tableName: "shipment_workflow") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-110") {
        createTable(tableName: "shipment_workflow_container_type") {
            column(name: "shipment_workflow_container_types_id", type: "CHAR(38)")

            column(name: "container_type_id", type: "CHAR(38)")

            column(name: "container_types_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-111") {
        createTable(tableName: "shipment_workflow_document") {
            column(name: "shipment_workflow_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "document_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-112") {
        createTable(tableName: "shipment_workflow_document_template") {
            column(name: "shipment_workflow_id", type: "CHAR(38)")

            column(name: "document_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-113") {
        createTable(tableName: "shipment_workflow_reference_number_type") {
            column(name: "shipment_workflow_reference_number_types_id", type: "CHAR(38)")

            column(name: "reference_number_type_id", type: "CHAR(38)")

            column(name: "reference_number_types_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-114") {
        createTable(tableName: "shipper") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-115") {
        createTable(tableName: "shipper_service") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-116") {
        createTable(tableName: "stockout_fact") {
            column(name: "date_dimension_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "location_dimension_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "product_dimension_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true")
            }

            column(name: "quantity_on_hand", type: "SMALLINT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-117") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-118") {
        createTable(tableName: "tag") {
            column(name: "id", type: "VARCHAR(255)") {
                constraints(nullable: "false", primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-119") {
        createTable(tableName: "transaction") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-120") {
        createTable(tableName: "transaction_entry") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-121") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-122") {
        createTable(tableName: "transaction_type") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-123") {
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

            column(name: "transaction_type_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "transaction_type_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-124") {
        createTable(tableName: "unit_of_measure") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-125") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-126") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-127") {
        createTable(tableName: "user") {
            column(defaultValue: "", name: "id", type: "CHAR(38)") {
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

    changeSet(author: "jmiranda (generated)", id: "1692045990425-128") {
        createTable(tableName: "user_role") {
            column(name: "user_id", type: "CHAR(38)")

            column(name: "role_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-129") {
        addUniqueConstraint(columnNames: "date, location_id, product_id, inventory_item_id", constraintName: "inventory_item_snapshot_key", tableName: "inventory_item_snapshot")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-130") {
        addUniqueConstraint(columnNames: "date, location_id, product_code, lot_number, bin_location_name", constraintName: "inventory_snapshot_uniq_idx", tableName: "inventory_snapshot")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-131") {
        addUniqueConstraint(columnNames: "code, locale", constraintName: "localization_code_locale_idx", tableName: "localization")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-132") {
        addUniqueConstraint(columnNames: "location_id, product_code, lot_number, bin_location_name", constraintName: "product_availability_uniq_idx", tableName: "product_availability")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-133") {
        addUniqueConstraint(columnNames: "product_id, lot_number", constraintName: "product_id", tableName: "inventory_item")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-134") {
        addUniqueConstraint(columnNames: "product_id, product_supplier_id, uom_id, quantity", constraintName: "product_package_uniq_idx", tableName: "product_package")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-135") {
        createIndex(indexName: "FK1143A95C8ABEBD5", tableName: "location_dimension") {
            column(name: "location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-136") {
        createIndex(indexName: "FK143BF46AA462C195", tableName: "user_role") {
            column(name: "user_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-137") {
        createIndex(indexName: "FK143BF46AFF37FDB5", tableName: "user_role") {
            column(name: "role_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-138") {
        createIndex(indexName: "FK1799509C20E33E1C", tableName: "requisition") {
            column(name: "verified_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-139") {
        createIndex(indexName: "FK1799509C217F5972", tableName: "requisition") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-140") {
        createIndex(indexName: "FK1799509C2BDD17B3", tableName: "requisition") {
            column(name: "requisition_template_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-141") {
        createIndex(indexName: "FK1799509C36C69275", tableName: "requisition") {
            column(name: "received_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-142") {
        createIndex(indexName: "FK1799509C426DD105", tableName: "requisition") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-143") {
        createIndex(indexName: "FK1799509C4CF042D8", tableName: "requisition") {
            column(name: "delivered_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-144") {
        createIndex(indexName: "FK1799509CD196DBBF", tableName: "requisition") {
            column(name: "issued_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-145") {
        createIndex(indexName: "FK1799509CD2CB8BBB", tableName: "requisition") {
            column(name: "checked_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-146") {
        createIndex(indexName: "FK1799509CDFA74E0B", tableName: "requisition") {
            column(name: "reviewed_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-147") {
        createIndex(indexName: "FK187E54C9DED5FAE7", tableName: "product_catalog_item") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-148") {
        createIndex(indexName: "FK187E54C9FB5E604E", tableName: "product_catalog_item") {
            column(name: "product_catalog_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-149") {
        createIndex(indexName: "FK1BF9A217F5972", tableName: "tag") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-150") {
        createIndex(indexName: "FK1BF9A426DD105", tableName: "tag") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-151") {
        createIndex(indexName: "FK1C92FE2F3E67CF9F", tableName: "party_role") {
            column(name: "party_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-152") {
        createIndex(indexName: "FK1E50D72D72882836", tableName: "transaction_fact") {
            column(name: "product_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-153") {
        createIndex(indexName: "FK1E50D72DA27827C2", tableName: "transaction_fact") {
            column(name: "location_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-154") {
        createIndex(indexName: "FK1E50D72DCA32CFEF", tableName: "transaction_fact") {
            column(name: "transaction_date_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-155") {
        createIndex(indexName: "FK1E50D72DCA354381", tableName: "transaction_fact") {
            column(name: "transaction_type_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-156") {
        createIndex(indexName: "FK1E50D72DD1F27172", tableName: "transaction_fact") {
            column(name: "lot_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-157") {
        createIndex(indexName: "FK299E50ABDED5FAE7", tableName: "synonym") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-158") {
        createIndex(indexName: "FK2D110D6418D76D84", tableName: "order_item") {
            column(name: "origin_bin_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-159") {
        createIndex(indexName: "FK2D110D6429542386", tableName: "order_item") {
            column(name: "product_supplier_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-160") {
        createIndex(indexName: "FK2D110D6429B2552E", tableName: "order_item") {
            column(name: "product_package_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-161") {
        createIndex(indexName: "FK2D110D6444979D51", tableName: "order_item") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-162") {
        createIndex(indexName: "FK2D110D6451A9416E", tableName: "order_item") {
            column(name: "parent_order_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-163") {
        createIndex(indexName: "FK2D110D645ED93B03", tableName: "order_item") {
            column(name: "quantity_uom_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-164") {
        createIndex(indexName: "FK2D110D64605326C", tableName: "order_item") {
            column(name: "destination_bin_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-165") {
        createIndex(indexName: "FK2D110D64911E7578", tableName: "order_item") {
            column(name: "requested_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-166") {
        createIndex(indexName: "FK2D110D64AA992CED", tableName: "order_item") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-167") {
        createIndex(indexName: "FK2D110D64D08EDBE6", tableName: "order_item") {
            column(name: "order_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-168") {
        createIndex(indexName: "FK2D110D64DED5FAE7", tableName: "order_item") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-169") {
        createIndex(indexName: "FK2D110D64EF4C770D", tableName: "order_item") {
            column(name: "category_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-170") {
        createIndex(indexName: "FK2DE9EE6EB8839C0F", tableName: "order_comment") {
            column(name: "order_comments_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-171") {
        createIndex(indexName: "FK2DE9EE6EC4A49BBF", tableName: "order_comment") {
            column(name: "comment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-172") {
        createIndex(indexName: "FK2E4511844A3E746", tableName: "unit_of_measure_conversion") {
            column(name: "from_unit_of_measure_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-173") {
        createIndex(indexName: "FK2E4511849B9434D5", tableName: "unit_of_measure_conversion") {
            column(name: "to_unit_of_measure_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-174") {
        createIndex(indexName: "FK302BCFE619A2EF8", tableName: "category") {
            column(name: "parent_category_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-175") {
        createIndex(indexName: "FK312F6C292388BC5", tableName: "shipment_reference_number") {
            column(name: "reference_number_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-176") {
        createIndex(indexName: "FK313A4BDF14F7BB8E", tableName: "product_group_product") {
            column(name: "product_group_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-177") {
        createIndex(indexName: "FK313A4BDFDED5FAE7", tableName: "product_group_product") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-178") {
        createIndex(indexName: "FK335CD11B6631D8CC", tableName: "document") {
            column(name: "document_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-179") {
        createIndex(indexName: "FK36EBCB1F28CE07", tableName: "user") {
            column(name: "warehouse_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-180") {
        createIndex(indexName: "FK36EBCB41E07A73", tableName: "user") {
            column(name: "manager_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-181") {
        createIndex(indexName: "FK38A5EE5FAF1302EB", tableName: "comment") {
            column(name: "sender_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-182") {
        createIndex(indexName: "FK38A5EE5FF885F087", tableName: "comment") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-183") {
        createIndex(indexName: "FK38EE09DA47B0D087", tableName: "attribute_entity_type_codes") {
            column(name: "attribute_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-184") {
        createIndex(indexName: "FK3A097B1C24DEBC91", tableName: "product_supplier") {
            column(name: "unit_of_measure_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-185") {
        createIndex(indexName: "FK3A097B1C2A475A37", tableName: "product_supplier") {
            column(name: "manufacturer_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-186") {
        createIndex(indexName: "FK3A097B1CDED5FAE7", tableName: "product_supplier") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-187") {
        createIndex(indexName: "FK3A097B1CF42F7E5C", tableName: "product_supplier") {
            column(name: "supplier_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-188") {
        createIndex(indexName: "FK40203B26296B2CA3", tableName: "shipment_method") {
            column(name: "shipper_service_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-189") {
        createIndex(indexName: "FK40203B263896C98E", tableName: "shipment_method") {
            column(name: "shipper_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-190") {
        createIndex(indexName: "FK408272383B5F6286", tableName: "receipt") {
            column(name: "shipment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-191") {
        createIndex(indexName: "FK4082723844979D51", tableName: "receipt") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-192") {
        createIndex(indexName: "FK414EF28F1E2B3CDC", tableName: "requisition") {
            column(name: "destination_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-193") {
        createIndex(indexName: "FK414EF28F44979D51", tableName: "requisition") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-194") {
        createIndex(indexName: "FK414EF28F94567276", tableName: "requisition") {
            column(name: "fulfillment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-195") {
        createIndex(indexName: "FK414EF28FDBDEDAC4", tableName: "requisition") {
            column(name: "origin_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-196") {
        createIndex(indexName: "FK414EF28FDD302242", tableName: "requisition") {
            column(name: "requested_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-197") {
        createIndex(indexName: "FK4A1ABEFE3BE9D843", tableName: "order_adjustment") {
            column(name: "order_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-198") {
        createIndex(indexName: "FK4A1ABEFED08EDBE6", tableName: "order_adjustment") {
            column(name: "order_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-199") {
        createIndex(indexName: "FK4A1ABEFEE1A39520", tableName: "order_adjustment") {
            column(name: "order_adjustment_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-200") {
        createIndex(indexName: "FK4BB27241154F600", tableName: "shipment_workflow_reference_number_type") {
            column(name: "reference_number_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-201") {
        createIndex(indexName: "FK4DA982C35DE21C87", tableName: "requisition_item") {
            column(name: "requisition_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-202") {
        createIndex(indexName: "FK4DA982C3AA992CED", tableName: "requisition_item") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-203") {
        createIndex(indexName: "FK4DA982C3DED5FAE7", tableName: "requisition_item") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-204") {
        createIndex(indexName: "FK4DA982C3EF4C770D", tableName: "requisition_item") {
            column(name: "category_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-205") {
        createIndex(indexName: "FK51F3772FEF4C770D", tableName: "product_group") {
            column(name: "category_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-206") {
        createIndex(indexName: "FK5358E4D614F7BB8E", tableName: "requisition_item") {
            column(name: "product_group_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-207") {
        createIndex(indexName: "FK5358E4D61594028E", tableName: "requisition_item") {
            column(name: "substitution_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-208") {
        createIndex(indexName: "FK5358E4D6217F5972", tableName: "requisition_item") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-209") {
        createIndex(indexName: "FK5358E4D629B2552E", tableName: "requisition_item") {
            column(name: "product_package_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-210") {
        createIndex(indexName: "FK5358E4D6405AC22D", tableName: "requisition_item") {
            column(name: "modification_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-211") {
        createIndex(indexName: "FK5358E4D6426DD105", tableName: "requisition_item") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-212") {
        createIndex(indexName: "FK5358E4D644979D51", tableName: "requisition_item") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-213") {
        createIndex(indexName: "FK5358E4D6DD302242", tableName: "requisition_item") {
            column(name: "requested_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-214") {
        createIndex(indexName: "FK5358E4D6F84BDE18", tableName: "requisition_item") {
            column(name: "parent_requisition_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-215") {
        createIndex(indexName: "FK5A2551DEAC392B33", tableName: "fulfillment") {
            column(name: "fulfilled_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-216") {
        createIndex(indexName: "FK5C6729A3D970DB4", tableName: "event") {
            column(name: "event_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-217") {
        createIndex(indexName: "FK5C6729A4415A5B0", tableName: "event") {
            column(name: "event_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-218") {
        createIndex(indexName: "FK5D1B504A217F5972", tableName: "unit_of_measure_class") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-219") {
        createIndex(indexName: "FK5D1B504A426DD105", tableName: "unit_of_measure_class") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-220") {
        createIndex(indexName: "FK5D1B504A6B9DFD", tableName: "unit_of_measure_class") {
            column(name: "base_uom_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-221") {
        createIndex(indexName: "FK615A48F6217F5972", tableName: "product_package") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-222") {
        createIndex(indexName: "FK615A48F629542386", tableName: "product_package") {
            column(name: "product_supplier_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-223") {
        createIndex(indexName: "FK615A48F63906C4CF", tableName: "product_package") {
            column(name: "uom_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-224") {
        createIndex(indexName: "FK615A48F6426DD105", tableName: "product_package") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-225") {
        createIndex(indexName: "FK651874E1E2B3CDC", tableName: "order") {
            column(name: "destination_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-226") {
        createIndex(indexName: "FK651874E240896CB", tableName: "order") {
            column(name: "approved_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-227") {
        createIndex(indexName: "FK651874E35D76CB0", tableName: "order") {
            column(name: "destination_party_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-228") {
        createIndex(indexName: "FK651874E41B7275F", tableName: "order") {
            column(name: "completed_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-229") {
        createIndex(indexName: "FK651874E44979D51", tableName: "order") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-230") {
        createIndex(indexName: "FK651874E6A8010C1", tableName: "order") {
            column(name: "payment_method_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-231") {
        createIndex(indexName: "FK651874E6D91063C", tableName: "order") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-232") {
        createIndex(indexName: "FK651874E8AF312E3", tableName: "order") {
            column(name: "order_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-233") {
        createIndex(indexName: "FK651874E8E7F7DCF", tableName: "order") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-234") {
        createIndex(indexName: "FK651874E9E52B00C", tableName: "order") {
            column(name: "payment_term_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-235") {
        createIndex(indexName: "FK651874EAF6D8801", tableName: "order") {
            column(name: "ordered_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-236") {
        createIndex(indexName: "FK651874EDBDEDAC4", tableName: "order") {
            column(name: "origin_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-237") {
        createIndex(indexName: "FK6581AE69DFE4C4C", tableName: "party") {
            column(name: "party_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-238") {
        createIndex(indexName: "FK6581AE6D1DFC6D7", tableName: "party") {
            column(name: "default_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-239") {
        createIndex(indexName: "FK6A1A433C3BE9D843", tableName: "order_invoice") {
            column(name: "order_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-240") {
        createIndex(indexName: "FK6A1A433CB95ED8E0", tableName: "order_invoice") {
            column(name: "invoice_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-241") {
        createIndex(indexName: "FK6C5BE20C800AA15", tableName: "shipment_document") {
            column(name: "document_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-242") {
        createIndex(indexName: "FK6D032BB53B350242", tableName: "shipment_event") {
            column(name: "shipment_events_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-243") {
        createIndex(indexName: "FK6D032BB5786431F", tableName: "shipment_event") {
            column(name: "event_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-244") {
        createIndex(indexName: "FK714F9FB528F75F00", tableName: "location") {
            column(name: "location_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-245") {
        createIndex(indexName: "FK714F9FB53BB36E94", tableName: "location") {
            column(name: "location_group_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-246") {
        createIndex(indexName: "FK714F9FB541E07A73", tableName: "location") {
            column(name: "manager_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-247") {
        createIndex(indexName: "FK714F9FB5606C7D95", tableName: "location") {
            column(name: "organization_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-248") {
        createIndex(indexName: "FK714F9FB561ED379F", tableName: "location") {
            column(name: "address_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-249") {
        createIndex(indexName: "FK714F9FB572A2C5B4", tableName: "location") {
            column(name: "inventory_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-250") {
        createIndex(indexName: "FK714F9FB57AF9A3C0", tableName: "location") {
            column(name: "parent_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-251") {
        createIndex(indexName: "FK7348B491217F5972", tableName: "unit_of_measure") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-252") {
        createIndex(indexName: "FK7348B491426DD105", tableName: "unit_of_measure") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-253") {
        createIndex(indexName: "FK7348B49197D8303E", tableName: "unit_of_measure") {
            column(name: "uom_class_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-254") {
        createIndex(indexName: "FK740B54769DB749D", tableName: "inventory_snapshot") {
            column(name: "bin_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-255") {
        createIndex(indexName: "FK740B547AA992CED", tableName: "inventory_snapshot") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-256") {
        createIndex(indexName: "FK740B547DED5FAE7", tableName: "inventory_snapshot") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-257") {
        createIndex(indexName: "FK74D92A693D2E628A", tableName: "order_event") {
            column(name: "order_events_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-258") {
        createIndex(indexName: "FK74D92A69786431F", tableName: "order_event") {
            column(name: "event_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-259") {
        createIndex(indexName: "FK7975323F4CC49445", tableName: "local_transfer") {
            column(name: "destination_transaction_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-260") {
        createIndex(indexName: "FK7975323F57563498", tableName: "local_transfer") {
            column(name: "source_transaction_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-261") {
        createIndex(indexName: "FK7A19D7561ED379F", tableName: "location_group") {
            column(name: "address_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-262") {
        createIndex(indexName: "FK7AFF67F928F75F00", tableName: "location_type_supported_activities") {
            column(name: "location_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-263") {
        createIndex(indexName: "FK7FA0D2DE1E2B3CDC", tableName: "transaction") {
            column(name: "destination_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-264") {
        createIndex(indexName: "FK7FA0D2DE217F5972", tableName: "transaction") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-265") {
        createIndex(indexName: "FK7FA0D2DE3265A8A9", tableName: "transaction") {
            column(name: "confirmed_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-266") {
        createIndex(indexName: "FK7FA0D2DE426DD105", tableName: "transaction") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-267") {
        createIndex(indexName: "FK7FA0D2DE5DE9E374", tableName: "transaction") {
            column(name: "requisition_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-268") {
        createIndex(indexName: "FK7FA0D2DE5F12AFED", tableName: "transaction") {
            column(name: "incoming_shipment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-269") {
        createIndex(indexName: "FK7FA0D2DE72A2C5B4", tableName: "transaction") {
            column(name: "inventory_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-270") {
        createIndex(indexName: "FK7FA0D2DE828481AF", tableName: "transaction") {
            column(name: "source_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-271") {
        createIndex(indexName: "FK7FA0D2DEB3FB7111", tableName: "transaction") {
            column(name: "transaction_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-272") {
        createIndex(indexName: "FK7FA0D2DEB80B3233", tableName: "transaction") {
            column(name: "outgoing_shipment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-273") {
        createIndex(indexName: "FK7FA0D2DED08EDBE6", tableName: "transaction") {
            column(name: "order_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-274") {
        createIndex(indexName: "FK7FA0D2DEF7076438", tableName: "transaction") {
            column(name: "receipt_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-275") {
        createIndex(indexName: "FK7FA87A22B3FB7111", tableName: "transaction_type_dimension") {
            column(name: "transaction_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-276") {
        createIndex(indexName: "FK9475736B3BE9D843", tableName: "order_shipment") {
            column(name: "order_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-277") {
        createIndex(indexName: "FK9475736BB06EC4FB", tableName: "order_shipment") {
            column(name: "shipment_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-278") {
        createIndex(indexName: "FK94A534C24DEBC91", tableName: "product_attribute") {
            column(name: "unit_of_measure_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-279") {
        createIndex(indexName: "FK94A534C29542386", tableName: "product_attribute") {
            column(name: "product_supplier_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-280") {
        createIndex(indexName: "FK94A534C47B0D087", tableName: "product_attribute") {
            column(name: "attribute_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-281") {
        createIndex(indexName: "FK94A534CDED5FAE7", tableName: "product_attribute") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-282") {
        createIndex(indexName: "FK98293BFB217F5972", tableName: "synonym") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-283") {
        createIndex(indexName: "FK98293BFB426DD105", tableName: "synonym") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-284") {
        createIndex(indexName: "FK9A945A36C800AA15", tableName: "shipment_workflow_document") {
            column(name: "document_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-285") {
        createIndex(indexName: "FK9A945A36EC587CFB", tableName: "shipment_workflow_document") {
            column(name: "shipment_workflow_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-286") {
        createIndex(indexName: "FKA0303E4EDED5FAE7", tableName: "product_category") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-287") {
        createIndex(indexName: "FKA0303E4EEF4C770D", tableName: "product_category") {
            column(name: "category_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-288") {
        createIndex(indexName: "FKA71CAC4A9740C85F", tableName: "product_tag") {
            column(name: "tag_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-289") {
        createIndex(indexName: "FKA71CAC4ADED5FAE7", tableName: "product_tag") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-290") {
        createIndex(indexName: "FKA8B7A49072882836", tableName: "consumption_fact") {
            column(name: "product_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-291") {
        createIndex(indexName: "FKA8B7A490A27827C2", tableName: "consumption_fact") {
            column(name: "location_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-292") {
        createIndex(indexName: "FKA8B7A490CA32CFEF", tableName: "consumption_fact") {
            column(name: "transaction_date_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-293") {
        createIndex(indexName: "FKA8B7A490D1F27172", tableName: "consumption_fact") {
            column(name: "lot_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-294") {
        createIndex(indexName: "FKABC21FD12EF4C7F4", tableName: "transaction_entry") {
            column(name: "transaction_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-295") {
        createIndex(indexName: "FKABC21FD169DB749D", tableName: "transaction_entry") {
            column(name: "bin_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-296") {
        createIndex(indexName: "FKABC21FD1AA992CED", tableName: "transaction_entry") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-297") {
        createIndex(indexName: "FKABC21FD1DED5FAE7", tableName: "transaction_entry") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-298") {
        createIndex(indexName: "FKAE3064BA44979D51", tableName: "receipt_item") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-299") {
        createIndex(indexName: "FKAE3064BA69DB749D", tableName: "receipt_item") {
            column(name: "bin_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-300") {
        createIndex(indexName: "FKAE3064BAAA992CED", tableName: "receipt_item") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-301") {
        createIndex(indexName: "FKAE3064BAB06EC4FB", tableName: "receipt_item") {
            column(name: "shipment_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-302") {
        createIndex(indexName: "FKAE3064BADED5FAE7", tableName: "receipt_item") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-303") {
        createIndex(indexName: "FKAE3064BAF7076438", tableName: "receipt_item") {
            column(name: "receipt_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-304") {
        createIndex(indexName: "FKB511C5AD20E351EA", tableName: "product_component") {
            column(name: "component_product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-305") {
        createIndex(indexName: "FKB511C5AD24DEBC91", tableName: "product_component") {
            column(name: "unit_of_measure_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-306") {
        createIndex(indexName: "FKB511C5ADFB4C199C", tableName: "product_component") {
            column(name: "assembly_product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-307") {
        createIndex(indexName: "FKB5A4FE84C4A49BBF", tableName: "order_item_comment") {
            column(name: "comment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-308") {
        createIndex(indexName: "FKBD34ABCD8ABEBD5", tableName: "inventory_item_snapshot") {
            column(name: "location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-309") {
        createIndex(indexName: "FKBD34ABCDAA992CED", tableName: "inventory_item_snapshot") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-310") {
        createIndex(indexName: "FKBD34ABCDDED5FAE7", tableName: "inventory_item_snapshot") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-311") {
        createIndex(indexName: "FKC254A2E16CDADD53", tableName: "inventory_level") {
            column(name: "internal_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-312") {
        createIndex(indexName: "FKC254A2E172A2C5B4", tableName: "inventory_level") {
            column(name: "inventory_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-313") {
        createIndex(indexName: "FKC254A2E1CFDCB4DF", tableName: "inventory_level") {
            column(name: "preferred_bin_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-314") {
        createIndex(indexName: "FKC254A2E1DED5FAE7", tableName: "inventory_level") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-315") {
        createIndex(indexName: "FKC254A2E1F07D879A", tableName: "inventory_level") {
            column(name: "replenishment_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-316") {
        createIndex(indexName: "FKC398CCBAC4A49BBF", tableName: "shipment_comment") {
            column(name: "comment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-317") {
        createIndex(indexName: "FKC73E1616DED5FAE7", tableName: "product_dimension") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-318") {
        createIndex(indexName: "FKC7AA9C4013CE80", tableName: "attribute") {
            column(name: "unit_of_measure_class_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-319") {
        createIndex(indexName: "FKCD71F39B8ABEBD5", tableName: "consumption") {
            column(name: "location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-320") {
        createIndex(indexName: "FKCD71F39BAA992CED", tableName: "consumption") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-321") {
        createIndex(indexName: "FKCD71F39BDED5FAE7", tableName: "consumption") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-322") {
        createIndex(indexName: "FKD08A526BC800AA15", tableName: "product_document") {
            column(name: "document_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-323") {
        createIndex(indexName: "FKD08A526BDED5FAE7", tableName: "product_document") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-324") {
        createIndex(indexName: "FKD2EAD9F8AA992CED", tableName: "lot_dimension") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-325") {
        createIndex(indexName: "FKD3F8383F217F5972", tableName: "picklist") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-326") {
        createIndex(indexName: "FKD3F8383F426DD105", tableName: "picklist") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-327") {
        createIndex(indexName: "FKD3F8383F5DE9E374", tableName: "picklist") {
            column(name: "requisition_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-328") {
        createIndex(indexName: "FKD3F8383FA3E976BC", tableName: "picklist") {
            column(name: "picker_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-329") {
        createIndex(indexName: "FKD3FC6EAB69DB749D", tableName: "product_availability") {
            column(name: "bin_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-330") {
        createIndex(indexName: "FKD3FC6EABAA992CED", tableName: "product_availability") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-331") {
        createIndex(indexName: "FKD584C4C4FF77FF9B", tableName: "shipment_workflow") {
            column(name: "shipment_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-332") {
        createIndex(indexName: "FKD790DEBD154F600", tableName: "reference_number") {
            column(name: "reference_number_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-333") {
        createIndex(indexName: "FKDA3BB2981CD3412D", tableName: "shipment_item") {
            column(name: "requisition_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-334") {
        createIndex(indexName: "FKDA3BB2983B5F6286", tableName: "shipment_item") {
            column(name: "shipment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-335") {
        createIndex(indexName: "FKDA3BB29844979D51", tableName: "shipment_item") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-336") {
        createIndex(indexName: "FKDA3BB29849AB6B52", tableName: "shipment_item") {
            column(name: "donor_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-337") {
        createIndex(indexName: "FKDA3BB29869DB749D", tableName: "shipment_item") {
            column(name: "bin_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-338") {
        createIndex(indexName: "FKDA3BB2987400E88E", tableName: "shipment_item") {
            column(name: "container_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-339") {
        createIndex(indexName: "FKDA3BB298AA992CED", tableName: "shipment_item") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-340") {
        createIndex(indexName: "FKDA3BB298DED5FAE7", tableName: "shipment_item") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-341") {
        createIndex(indexName: "FKDEF5AD1317A6E251", tableName: "shipment_workflow_container_type") {
            column(name: "container_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-342") {
        createIndex(indexName: "FKDF7559D73896C98E", tableName: "shipper_service") {
            column(name: "shipper_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-343") {
        createIndex(indexName: "FKE071DE6DB06EC4FB", tableName: "fulfillment_item_shipment_item") {
            column(name: "shipment_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-344") {
        createIndex(indexName: "FKE071DE6DB42751E1", tableName: "fulfillment_item_shipment_item") {
            column(name: "fulfillment_item_shipment_items_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-345") {
        createIndex(indexName: "FKE139719A1E2B3CDC", tableName: "shipment") {
            column(name: "destination_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-346") {
        createIndex(indexName: "FKE139719A217F5972", tableName: "shipment") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-347") {
        createIndex(indexName: "FKE139719A294C1012", tableName: "shipment") {
            column(name: "carrier_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-348") {
        createIndex(indexName: "FKE139719A426DD105", tableName: "shipment") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-349") {
        createIndex(indexName: "FKE139719A44979D51", tableName: "shipment") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-350") {
        createIndex(indexName: "FKE139719A49AB6B52", tableName: "shipment") {
            column(name: "donor_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-351") {
        createIndex(indexName: "FKE139719A5DE9E374", tableName: "shipment") {
            column(name: "requisition_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-352") {
        createIndex(indexName: "FKE139719AA28CC5FB", tableName: "shipment") {
            column(name: "shipment_method_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-353") {
        createIndex(indexName: "FKE139719AD95ACF25", tableName: "shipment") {
            column(name: "current_event_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-354") {
        createIndex(indexName: "FKE139719ADBDEDAC4", tableName: "shipment") {
            column(name: "origin_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-355") {
        createIndex(indexName: "FKE139719AFF77FF9B", tableName: "shipment") {
            column(name: "shipment_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-356") {
        createIndex(indexName: "FKE698D2ECC800AA15", tableName: "order_document") {
            column(name: "document_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-357") {
        createIndex(indexName: "FKE698D2ECFE10118D", tableName: "order_document") {
            column(name: "order_documents_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-358") {
        createIndex(indexName: "FKE7584B1369DB749D", tableName: "picklist_item") {
            column(name: "bin_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-359") {
        createIndex(indexName: "FKE7814C8117A6E251", tableName: "container") {
            column(name: "container_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-360") {
        createIndex(indexName: "FKE7814C813B5F6286", tableName: "container") {
            column(name: "shipment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-361") {
        createIndex(indexName: "FKE7814C8144979D51", tableName: "container") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-362") {
        createIndex(indexName: "FKE7814C814B6A2E03", tableName: "container") {
            column(name: "parent_container_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-363") {
        createIndex(indexName: "FKED441931C8653BC0", tableName: "product_association") {
            column(name: "associated_product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-364") {
        createIndex(indexName: "FKED441931DED5FAE7", tableName: "product_association") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-365") {
        createIndex(indexName: "FKED8DCCEF217F5972", tableName: "product") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-366") {
        createIndex(indexName: "FKED8DCCEF426DD105", tableName: "product") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-367") {
        createIndex(indexName: "FKED8DCCEFABD88AC6", tableName: "product") {
            column(name: "product_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-368") {
        createIndex(indexName: "FKED8DCCEFEEB2908D", tableName: "product") {
            column(name: "default_uom_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-369") {
        createIndex(indexName: "FKED8DCCEFEF4C770D", tableName: "product") {
            column(name: "category_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-370") {
        createIndex(indexName: "FKEDC55CD447EBE106", tableName: "fulfillment_item") {
            column(name: "requisition_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-371") {
        createIndex(indexName: "FKEDC55CD494567276", tableName: "fulfillment_item") {
            column(name: "fulfillment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-372") {
        createIndex(indexName: "FKEDC55CD4AA992CED", tableName: "fulfillment_item") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-373") {
        createIndex(indexName: "FKF58372688ABEBD5", tableName: "location_supported_activities") {
            column(name: "location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-374") {
        createIndex(indexName: "FKFD8E50671A43AB29", tableName: "click_stream_request") {
            column(name: "click_stream_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-375") {
        createIndex(indexName: "fk_budget_code_created_by", tableName: "budget_code") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-376") {
        createIndex(indexName: "fk_budget_code_organization", tableName: "budget_code") {
            column(name: "organization_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-377") {
        createIndex(indexName: "fk_budget_code_updated_by", tableName: "budget_code") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-378") {
        createIndex(indexName: "fk_category_gl_account", tableName: "category") {
            column(name: "gl_account_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-379") {
        createIndex(indexName: "fk_gl_account_created_by", tableName: "gl_account") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-380") {
        createIndex(indexName: "fk_gl_account_gl_account_type", tableName: "gl_account") {
            column(name: "gl_account_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-381") {
        createIndex(indexName: "fk_gl_account_type_created_by", tableName: "gl_account_type") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-382") {
        createIndex(indexName: "fk_gl_account_type_updated_by", tableName: "gl_account_type") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-383") {
        createIndex(indexName: "fk_gl_account_updated_by", tableName: "gl_account") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-384") {
        createIndex(indexName: "fk_invoice_created_by", tableName: "invoice") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-385") {
        createIndex(indexName: "fk_invoice_currency_uom", tableName: "invoice") {
            column(name: "currency_uom_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-386") {
        createIndex(indexName: "fk_invoice_document_document", tableName: "invoice_document") {
            column(name: "document_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-387") {
        createIndex(indexName: "fk_invoice_document_invoice", tableName: "invoice_document") {
            column(name: "invoice_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-388") {
        createIndex(indexName: "fk_invoice_invoice_type", tableName: "invoice") {
            column(name: "invoice_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-389") {
        createIndex(indexName: "fk_invoice_item_budget_code", tableName: "invoice_item") {
            column(name: "budget_code_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-390") {
        createIndex(indexName: "fk_invoice_item_created_by", tableName: "invoice_item") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-391") {
        createIndex(indexName: "fk_invoice_item_gl_account", tableName: "invoice_item") {
            column(name: "gl_account_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-392") {
        createIndex(indexName: "fk_invoice_item_invoice", tableName: "invoice_item") {
            column(name: "invoice_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-393") {
        createIndex(indexName: "fk_invoice_item_product", tableName: "invoice_item") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-394") {
        createIndex(indexName: "fk_invoice_item_quantity_uom", tableName: "invoice_item") {
            column(name: "quantity_uom_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-395") {
        createIndex(indexName: "fk_invoice_item_updated_by", tableName: "invoice_item") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-396") {
        createIndex(indexName: "fk_invoice_party", tableName: "invoice") {
            column(name: "party_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-397") {
        createIndex(indexName: "fk_invoice_party_from", tableName: "invoice") {
            column(name: "party_from_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-398") {
        createIndex(indexName: "fk_invoice_reference_number_invoice_id", tableName: "invoice_reference_number") {
            column(name: "invoice_reference_numbers_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-399") {
        createIndex(indexName: "fk_invoice_reference_number_reference_number_id", tableName: "invoice_reference_number") {
            column(name: "reference_number_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-400") {
        createIndex(indexName: "fk_invoice_type_created_by", tableName: "invoice_type") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-401") {
        createIndex(indexName: "fk_invoice_type_updated_by", tableName: "invoice_type") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-402") {
        createIndex(indexName: "fk_invoice_updated_by", tableName: "invoice") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-403") {
        createIndex(indexName: "fk_order_adjustment_budget_code", tableName: "order_adjustment") {
            column(name: "budget_code_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-404") {
        createIndex(indexName: "fk_order_adjustment_gl_account", tableName: "order_adjustment") {
            column(name: "gl_account_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-405") {
        createIndex(indexName: "fk_order_adjustment_type_gl_account", tableName: "order_adjustment_type") {
            column(name: "gl_account_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-406") {
        createIndex(indexName: "fk_order_item_budget_code", tableName: "order_item") {
            column(name: "budget_code_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-407") {
        createIndex(indexName: "fk_order_item_gl_account", tableName: "order_item") {
            column(name: "gl_account_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-408") {
        createIndex(indexName: "fk_order_type_created_by", tableName: "order_type") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-409") {
        createIndex(indexName: "fk_order_type_updated_by", tableName: "order_type") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-410") {
        createIndex(indexName: "fk_picklist_item_order_item", tableName: "picklist_item") {
            column(name: "order_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-411") {
        createIndex(indexName: "fk_picklist_order", tableName: "picklist") {
            column(name: "order_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-412") {
        createIndex(indexName: "fk_preference_type_created_by", tableName: "preference_type") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-413") {
        createIndex(indexName: "fk_preference_type_updated_by", tableName: "preference_type") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-414") {
        createIndex(indexName: "fk_product_association_mutual_association", tableName: "product_association") {
            column(name: "mutual_association_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-415") {
        createIndex(indexName: "fk_product_gl_account", tableName: "product") {
            column(name: "gl_account_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-416") {
        createIndex(indexName: "fk_product_merge_logger_created_by", tableName: "product_merge_logger") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-417") {
        createIndex(indexName: "fk_product_merge_logger_obsolete_product", tableName: "product_merge_logger") {
            column(name: "obsolete_product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-418") {
        createIndex(indexName: "fk_product_merge_logger_primary_product", tableName: "product_merge_logger") {
            column(name: "primary_product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-419") {
        createIndex(indexName: "fk_product_merge_logger_updated_by", tableName: "product_merge_logger") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-420") {
        createIndex(indexName: "fk_product_price_created_by", tableName: "product_price") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-421") {
        createIndex(indexName: "fk_product_price_unit_of_measure", tableName: "product_price") {
            column(name: "currency_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-422") {
        createIndex(indexName: "fk_product_price_updated_by", tableName: "product_price") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-423") {
        createIndex(indexName: "fk_product_product_family", tableName: "product") {
            column(name: "product_family_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-424") {
        createIndex(indexName: "fk_product_supplier_preference_created_by", tableName: "product_supplier_preference") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-425") {
        createIndex(indexName: "fk_product_supplier_preference_destination_party", tableName: "product_supplier_preference") {
            column(name: "destination_party_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-426") {
        createIndex(indexName: "fk_product_supplier_preference_preference_type", tableName: "product_supplier_preference") {
            column(name: "preference_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-427") {
        createIndex(indexName: "fk_product_supplier_preference_product_supplier", tableName: "product_supplier_preference") {
            column(name: "product_supplier_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-428") {
        createIndex(indexName: "fk_product_supplier_preference_updated_by", tableName: "product_supplier_preference") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-429") {
        createIndex(indexName: "fk_product_type_displayed_fields_product_type", tableName: "product_type_displayed_fields") {
            column(name: "product_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-430") {
        createIndex(indexName: "fk_product_type_required_fields_product_type", tableName: "product_type_required_fields") {
            column(name: "product_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-431") {
        createIndex(indexName: "fk_product_type_supported_activities_product_type", tableName: "product_type_supported_activities") {
            column(name: "product_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-432") {
        createIndex(indexName: "fk_shipment_reference_number_shipment", tableName: "shipment_reference_number") {
            column(name: "shipment_reference_numbers_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-433") {
        createIndex(indexName: "inventory_snapshot_date_idx", tableName: "inventory_snapshot") {
            column(name: "date")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-434") {
        createIndex(indexName: "inventory_snapshot_last_updated_idx", tableName: "inventory_snapshot") {
            column(name: "location_id")

            column(name: "last_updated")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-435") {
        createIndex(indexName: "location_role_ibfk_1", tableName: "location_role") {
            column(name: "user_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-436") {
        createIndex(indexName: "location_role_ibfk_2", tableName: "location_role") {
            column(name: "role_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-437") {
        createIndex(indexName: "location_role_ibfk_3", tableName: "location_role") {
            column(name: "location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-438") {
        createIndex(indexName: "order_adjustment_invoice_ibfk_1", tableName: "order_adjustment_invoice") {
            column(name: "order_adjustment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-439") {
        createIndex(indexName: "order_adjustment_invoice_ibfk_2", tableName: "order_adjustment_invoice") {
            column(name: "invoice_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-440") {
        createIndex(indexName: "picklist_item_ibfk_1", tableName: "picklist_item") {
            column(name: "requisition_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-441") {
        createIndex(indexName: "picklist_item_ibfk_2", tableName: "picklist_item") {
            column(name: "picklist_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-442") {
        createIndex(indexName: "picklist_item_ibfk_3", tableName: "picklist_item") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-443") {
        createIndex(indexName: "picklist_item_ibfk_4", tableName: "picklist_item") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-444") {
        createIndex(indexName: "picklist_item_ibfk_5", tableName: "picklist_item") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-445") {
        createIndex(indexName: "product_availability_product_location_idx", tableName: "product_availability") {
            column(name: "product_id")

            column(name: "location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-446") {
        createIndex(indexName: "product_id", tableName: "product_demand_details") {
            column(name: "product_id")

            column(name: "origin_id")

            column(name: "destination_id")

            column(name: "date_issued")

            column(name: "date_requested")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-447") {
        createIndex(indexName: "shipment_invoice_ibfk_1", tableName: "shipment_invoice") {
            column(name: "shipment_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-448") {
        createIndex(indexName: "shipment_invoice_ibfk_2", tableName: "shipment_invoice") {
            column(name: "invoice_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-449") {
        createIndex(indexName: "zone_location_ibfk_3", tableName: "location") {
            column(name: "zone_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-450") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "location_dimension", constraintName: "FK1143A95C8ABEBD5", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-451") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_role", constraintName: "FK143BF46AA462C195", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-452") {
        addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "user_role", constraintName: "FK143BF46AFF37FDB5", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "role", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-453") {
        addForeignKeyConstraint(baseColumnNames: "verified_by_id", baseTableName: "requisition", constraintName: "FK1799509C20E33E1C", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-454") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "requisition", constraintName: "FK1799509C217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-455") {
        addForeignKeyConstraint(baseColumnNames: "requisition_template_id", baseTableName: "requisition", constraintName: "FK1799509C2BDD17B3", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "requisition", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-456") {
        addForeignKeyConstraint(baseColumnNames: "received_by_id", baseTableName: "requisition", constraintName: "FK1799509C36C69275", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-457") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "requisition", constraintName: "FK1799509C426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-458") {
        addForeignKeyConstraint(baseColumnNames: "delivered_by_id", baseTableName: "requisition", constraintName: "FK1799509C4CF042D8", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-459") {
        addForeignKeyConstraint(baseColumnNames: "issued_by_id", baseTableName: "requisition", constraintName: "FK1799509CD196DBBF", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-460") {
        addForeignKeyConstraint(baseColumnNames: "checked_by_id", baseTableName: "requisition", constraintName: "FK1799509CD2CB8BBB", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-461") {
        addForeignKeyConstraint(baseColumnNames: "reviewed_by_id", baseTableName: "requisition", constraintName: "FK1799509CDFA74E0B", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-462") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_catalog_item", constraintName: "FK187E54C9DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-463") {
        addForeignKeyConstraint(baseColumnNames: "product_catalog_id", baseTableName: "product_catalog_item", constraintName: "FK187E54C9FB5E604E", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_catalog", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-464") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "tag", constraintName: "FK1BF9A217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-465") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "tag", constraintName: "FK1BF9A426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-466") {
        addForeignKeyConstraint(baseColumnNames: "party_id", baseTableName: "party_role", constraintName: "FK1C92FE2F3E67CF9F", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-467") {
        addForeignKeyConstraint(baseColumnNames: "product_key_id", baseTableName: "transaction_fact", constraintName: "FK1E50D72D72882836", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-468") {
        addForeignKeyConstraint(baseColumnNames: "location_key_id", baseTableName: "transaction_fact", constraintName: "FK1E50D72DA27827C2", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-469") {
        addForeignKeyConstraint(baseColumnNames: "transaction_date_key_id", baseTableName: "transaction_fact", constraintName: "FK1E50D72DCA32CFEF", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "date_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-470") {
        addForeignKeyConstraint(baseColumnNames: "transaction_type_key_id", baseTableName: "transaction_fact", constraintName: "FK1E50D72DCA354381", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "transaction_type_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-471") {
        addForeignKeyConstraint(baseColumnNames: "lot_key_id", baseTableName: "transaction_fact", constraintName: "FK1E50D72DD1F27172", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "lot_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-472") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "synonym", constraintName: "FK299E50ABDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-473") {
        addForeignKeyConstraint(baseColumnNames: "origin_bin_location_id", baseTableName: "order_item", constraintName: "FK2D110D6418D76D84", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-474") {
        addForeignKeyConstraint(baseColumnNames: "product_supplier_id", baseTableName: "order_item", constraintName: "FK2D110D6429542386", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_supplier", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-475") {
        addForeignKeyConstraint(baseColumnNames: "product_package_id", baseTableName: "order_item", constraintName: "FK2D110D6429B2552E", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_package", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-476") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "order_item", constraintName: "FK2D110D6444979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-477") {
        addForeignKeyConstraint(baseColumnNames: "parent_order_item_id", baseTableName: "order_item", constraintName: "FK2D110D6451A9416E", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-478") {
        addForeignKeyConstraint(baseColumnNames: "quantity_uom_id", baseTableName: "order_item", constraintName: "FK2D110D645ED93B03", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-479") {
        addForeignKeyConstraint(baseColumnNames: "destination_bin_location_id", baseTableName: "order_item", constraintName: "FK2D110D64605326C", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-480") {
        addForeignKeyConstraint(baseColumnNames: "requested_by_id", baseTableName: "order_item", constraintName: "FK2D110D64911E7578", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-481") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "order_item", constraintName: "FK2D110D64AA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-482") {
        addForeignKeyConstraint(baseColumnNames: "order_id", baseTableName: "order_item", constraintName: "FK2D110D64D08EDBE6", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-483") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "order_item", constraintName: "FK2D110D64DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-484") {
        addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "order_item", constraintName: "FK2D110D64EF4C770D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "category", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-485") {
        addForeignKeyConstraint(baseColumnNames: "order_comments_id", baseTableName: "order_comment", constraintName: "FK2DE9EE6EB8839C0F", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-486") {
        addForeignKeyConstraint(baseColumnNames: "comment_id", baseTableName: "order_comment", constraintName: "FK2DE9EE6EC4A49BBF", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "comment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-487") {
        addForeignKeyConstraint(baseColumnNames: "from_unit_of_measure_id", baseTableName: "unit_of_measure_conversion", constraintName: "FK2E4511844A3E746", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-488") {
        addForeignKeyConstraint(baseColumnNames: "to_unit_of_measure_id", baseTableName: "unit_of_measure_conversion", constraintName: "FK2E4511849B9434D5", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-489") {
        addForeignKeyConstraint(baseColumnNames: "parent_category_id", baseTableName: "category", constraintName: "FK302BCFE619A2EF8", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "category", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-490") {
        addForeignKeyConstraint(baseColumnNames: "reference_number_id", baseTableName: "shipment_reference_number", constraintName: "FK312F6C292388BC5", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "reference_number", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-491") {
        addForeignKeyConstraint(baseColumnNames: "product_group_id", baseTableName: "product_group_product", constraintName: "FK313A4BDF14F7BB8E", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_group", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-492") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_group_product", constraintName: "FK313A4BDFDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-493") {
        addForeignKeyConstraint(baseColumnNames: "document_type_id", baseTableName: "document", constraintName: "FK335CD11B6631D8CC", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "document_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-494") {
        addForeignKeyConstraint(baseColumnNames: "warehouse_id", baseTableName: "user", constraintName: "FK36EBCB1F28CE07", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-495") {
        addForeignKeyConstraint(baseColumnNames: "manager_id", baseTableName: "user", constraintName: "FK36EBCB41E07A73", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-496") {
        addForeignKeyConstraint(baseColumnNames: "sender_id", baseTableName: "comment", constraintName: "FK38A5EE5FAF1302EB", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-497") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "comment", constraintName: "FK38A5EE5FF885F087", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-498") {
        addForeignKeyConstraint(baseColumnNames: "attribute_id", baseTableName: "attribute_entity_type_codes", constraintName: "FK38EE09DA47B0D087", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "attribute", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-499") {
        addForeignKeyConstraint(baseColumnNames: "unit_of_measure_id", baseTableName: "product_supplier", constraintName: "FK3A097B1C24DEBC91", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-500") {
        addForeignKeyConstraint(baseColumnNames: "manufacturer_id", baseTableName: "product_supplier", constraintName: "FK3A097B1C2A475A37", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-501") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_supplier", constraintName: "FK3A097B1CDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-502") {
        addForeignKeyConstraint(baseColumnNames: "supplier_id", baseTableName: "product_supplier", constraintName: "FK3A097B1CF42F7E5C", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-503") {
        addForeignKeyConstraint(baseColumnNames: "shipper_service_id", baseTableName: "shipment_method", constraintName: "FK40203B26296B2CA3", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipper_service", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-504") {
        addForeignKeyConstraint(baseColumnNames: "shipper_id", baseTableName: "shipment_method", constraintName: "FK40203B263896C98E", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipper", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-505") {
        addForeignKeyConstraint(baseColumnNames: "shipment_id", baseTableName: "receipt", constraintName: "FK408272383B5F6286", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-506") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "receipt", constraintName: "FK4082723844979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-507") {
        addForeignKeyConstraint(baseColumnNames: "destination_id", baseTableName: "requisition", constraintName: "FK414EF28F1E2B3CDC", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-508") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "requisition", constraintName: "FK414EF28F44979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-509") {
        addForeignKeyConstraint(baseColumnNames: "fulfillment_id", baseTableName: "requisition", constraintName: "FK414EF28F94567276", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "fulfillment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-510") {
        addForeignKeyConstraint(baseColumnNames: "origin_id", baseTableName: "requisition", constraintName: "FK414EF28FDBDEDAC4", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-511") {
        addForeignKeyConstraint(baseColumnNames: "requested_by_id", baseTableName: "requisition", constraintName: "FK414EF28FDD302242", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-512") {
        addForeignKeyConstraint(baseColumnNames: "order_item_id", baseTableName: "order_adjustment", constraintName: "FK4A1ABEFE3BE9D843", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-513") {
        addForeignKeyConstraint(baseColumnNames: "order_id", baseTableName: "order_adjustment", constraintName: "FK4A1ABEFED08EDBE6", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-514") {
        addForeignKeyConstraint(baseColumnNames: "order_adjustment_type_id", baseTableName: "order_adjustment", constraintName: "FK4A1ABEFEE1A39520", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order_adjustment_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-515") {
        addForeignKeyConstraint(baseColumnNames: "reference_number_type_id", baseTableName: "shipment_workflow_reference_number_type", constraintName: "FK4BB27241154F600", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "reference_number_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-516") {
        addForeignKeyConstraint(baseColumnNames: "requisition_id", baseTableName: "requisition_item", constraintName: "FK4DA982C35DE21C87", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "requisition", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-517") {
        addForeignKeyConstraint(baseColumnNames: "requested_by_id", baseTableName: "requisition_item", constraintName: "FK4DA982C3911E7578", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-518") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "requisition_item", constraintName: "FK4DA982C3AA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-519") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "requisition_item", constraintName: "FK4DA982C3DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-520") {
        addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "requisition_item", constraintName: "FK4DA982C3EF4C770D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "category", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-521") {
        addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "product_group", constraintName: "FK51F3772FEF4C770D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "category", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-522") {
        addForeignKeyConstraint(baseColumnNames: "product_group_id", baseTableName: "requisition_item", constraintName: "FK5358E4D614F7BB8E", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_group", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-523") {
        addForeignKeyConstraint(baseColumnNames: "substitution_item_id", baseTableName: "requisition_item", constraintName: "FK5358E4D61594028E", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "requisition_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-524") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "requisition_item", constraintName: "FK5358E4D6217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-525") {
        addForeignKeyConstraint(baseColumnNames: "product_package_id", baseTableName: "requisition_item", constraintName: "FK5358E4D629B2552E", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_package", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-526") {
        addForeignKeyConstraint(baseColumnNames: "modification_item_id", baseTableName: "requisition_item", constraintName: "FK5358E4D6405AC22D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "requisition_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-527") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "requisition_item", constraintName: "FK5358E4D6426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-528") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "requisition_item", constraintName: "FK5358E4D644979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-529") {
        addForeignKeyConstraint(baseColumnNames: "requested_by_id", baseTableName: "requisition_item", constraintName: "FK5358E4D6DD302242", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-530") {
        addForeignKeyConstraint(baseColumnNames: "parent_requisition_item_id", baseTableName: "requisition_item", constraintName: "FK5358E4D6F84BDE18", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "requisition_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-531") {
        addForeignKeyConstraint(baseColumnNames: "fulfilled_by_id", baseTableName: "fulfillment", constraintName: "FK5A2551DEAC392B33", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-532") {
        addForeignKeyConstraint(baseColumnNames: "event_type_id", baseTableName: "event", constraintName: "FK5C6729A3D970DB4", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "event_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-533") {
        addForeignKeyConstraint(baseColumnNames: "event_location_id", baseTableName: "event", constraintName: "FK5C6729A4415A5B0", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-534") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "unit_of_measure_class", constraintName: "FK5D1B504A217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-535") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "unit_of_measure_class", constraintName: "FK5D1B504A426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-536") {
        addForeignKeyConstraint(baseColumnNames: "base_uom_id", baseTableName: "unit_of_measure_class", constraintName: "FK5D1B504A6B9DFD", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-537") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "product_package", constraintName: "FK615A48F6217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-538") {
        addForeignKeyConstraint(baseColumnNames: "product_supplier_id", baseTableName: "product_package", constraintName: "FK615A48F629542386", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_supplier", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-539") {
        addForeignKeyConstraint(baseColumnNames: "uom_id", baseTableName: "product_package", constraintName: "FK615A48F63906C4CF", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-540") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "product_package", constraintName: "FK615A48F6426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-541") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_package", constraintName: "FK615A48F6DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-542") {
        addForeignKeyConstraint(baseColumnNames: "destination_id", baseTableName: "order", constraintName: "FK651874E1E2B3CDC", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-543") {
        addForeignKeyConstraint(baseColumnNames: "approved_by_id", baseTableName: "order", constraintName: "FK651874E240896CB", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-544") {
        addForeignKeyConstraint(baseColumnNames: "destination_party_id", baseTableName: "order", constraintName: "FK651874E35D76CB0", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-545") {
        addForeignKeyConstraint(baseColumnNames: "completed_by_id", baseTableName: "order", constraintName: "FK651874E41B7275F", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-546") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "order", constraintName: "FK651874E44979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-547") {
        addForeignKeyConstraint(baseColumnNames: "payment_method_type_id", baseTableName: "order", constraintName: "FK651874E6A8010C1", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "payment_method_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-548") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "order", constraintName: "FK651874E6D91063C", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-549") {
        addForeignKeyConstraint(baseColumnNames: "order_type_id", baseTableName: "order", constraintName: "FK651874E8AF312E3", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-550") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "order", constraintName: "FK651874E8E7F7DCF", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-551") {
        addForeignKeyConstraint(baseColumnNames: "payment_term_id", baseTableName: "order", constraintName: "FK651874E9E52B00C", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "payment_term", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-552") {
        addForeignKeyConstraint(baseColumnNames: "ordered_by_id", baseTableName: "order", constraintName: "FK651874EAF6D8801", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-553") {
        addForeignKeyConstraint(baseColumnNames: "origin_id", baseTableName: "order", constraintName: "FK651874EDBDEDAC4", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-554") {
        addForeignKeyConstraint(baseColumnNames: "party_type_id", baseTableName: "party", constraintName: "FK6581AE69DFE4C4C", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "party_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-555") {
        addForeignKeyConstraint(baseColumnNames: "default_location_id", baseTableName: "party", constraintName: "FK6581AE6D1DFC6D7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-556") {
        addForeignKeyConstraint(baseColumnNames: "order_item_id", baseTableName: "order_invoice", constraintName: "FK6A1A433C3BE9D843", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-557") {
        addForeignKeyConstraint(baseColumnNames: "invoice_item_id", baseTableName: "order_invoice", constraintName: "FK6A1A433CB95ED8E0", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "invoice_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-558") {
        addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "shipment_document", constraintName: "FK6C5BE20C800AA15", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "document", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-559") {
        addForeignKeyConstraint(baseColumnNames: "shipment_events_id", baseTableName: "shipment_event", constraintName: "FK6D032BB53B350242", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-560") {
        addForeignKeyConstraint(baseColumnNames: "event_id", baseTableName: "shipment_event", constraintName: "FK6D032BB5786431F", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "event", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-561") {
        addForeignKeyConstraint(baseColumnNames: "location_type_id", baseTableName: "location", constraintName: "FK714F9FB528F75F00", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-562") {
        addForeignKeyConstraint(baseColumnNames: "location_group_id", baseTableName: "location", constraintName: "FK714F9FB53BB36E94", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location_group", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-563") {
        addForeignKeyConstraint(baseColumnNames: "manager_id", baseTableName: "location", constraintName: "FK714F9FB541E07A73", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-564") {
        addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "location", constraintName: "FK714F9FB5606C7D95", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-565") {
        addForeignKeyConstraint(baseColumnNames: "address_id", baseTableName: "location", constraintName: "FK714F9FB561ED379F", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "address", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-566") {
        addForeignKeyConstraint(baseColumnNames: "inventory_id", baseTableName: "location", constraintName: "FK714F9FB572A2C5B4", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-567") {
        addForeignKeyConstraint(baseColumnNames: "parent_location_id", baseTableName: "location", constraintName: "FK714F9FB57AF9A3C0", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-568") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "unit_of_measure", constraintName: "FK7348B491217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-569") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "unit_of_measure", constraintName: "FK7348B491426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-570") {
        addForeignKeyConstraint(baseColumnNames: "uom_class_id", baseTableName: "unit_of_measure", constraintName: "FK7348B49197D8303E", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure_class", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-571") {
        addForeignKeyConstraint(baseColumnNames: "bin_location_id", baseTableName: "inventory_snapshot", constraintName: "FK740B54769DB749D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-572") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "inventory_snapshot", constraintName: "FK740B5478ABEBD5", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-573") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "inventory_snapshot", constraintName: "FK740B547AA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-574") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "inventory_snapshot", constraintName: "FK740B547DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-575") {
        addForeignKeyConstraint(baseColumnNames: "order_events_id", baseTableName: "order_event", constraintName: "FK74D92A693D2E628A", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-576") {
        addForeignKeyConstraint(baseColumnNames: "event_id", baseTableName: "order_event", constraintName: "FK74D92A69786431F", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "event", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-577") {
        addForeignKeyConstraint(baseColumnNames: "destination_transaction_id", baseTableName: "local_transfer", constraintName: "FK7975323F4CC49445", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "transaction", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-578") {
        addForeignKeyConstraint(baseColumnNames: "source_transaction_id", baseTableName: "local_transfer", constraintName: "FK7975323F57563498", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "transaction", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-579") {
        addForeignKeyConstraint(baseColumnNames: "address_id", baseTableName: "location_group", constraintName: "FK7A19D7561ED379F", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "address", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-580") {
        addForeignKeyConstraint(baseColumnNames: "location_type_id", baseTableName: "location_type_supported_activities", constraintName: "FK7AFF67F928F75F00", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-581") {
        addForeignKeyConstraint(baseColumnNames: "destination_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE1E2B3CDC", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-582") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-583") {
        addForeignKeyConstraint(baseColumnNames: "confirmed_by_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE3265A8A9", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-584") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-585") {
        addForeignKeyConstraint(baseColumnNames: "requisition_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE5DE9E374", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "requisition", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-586") {
        addForeignKeyConstraint(baseColumnNames: "incoming_shipment_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE5F12AFED", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-587") {
        addForeignKeyConstraint(baseColumnNames: "inventory_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE72A2C5B4", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-588") {
        addForeignKeyConstraint(baseColumnNames: "source_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE828481AF", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-589") {
        addForeignKeyConstraint(baseColumnNames: "transaction_type_id", baseTableName: "transaction", constraintName: "FK7FA0D2DEB3FB7111", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "transaction_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-590") {
        addForeignKeyConstraint(baseColumnNames: "outgoing_shipment_id", baseTableName: "transaction", constraintName: "FK7FA0D2DEB80B3233", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-591") {
        addForeignKeyConstraint(baseColumnNames: "order_id", baseTableName: "transaction", constraintName: "FK7FA0D2DED08EDBE6", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-592") {
        addForeignKeyConstraint(baseColumnNames: "receipt_id", baseTableName: "transaction", constraintName: "FK7FA0D2DEF7076438", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "receipt", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-593") {
        addForeignKeyConstraint(baseColumnNames: "transaction_type_id", baseTableName: "transaction_type_dimension", constraintName: "FK7FA87A22B3FB7111", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "transaction_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-594") {
        addForeignKeyConstraint(baseColumnNames: "order_item_id", baseTableName: "order_shipment", constraintName: "FK9475736B3BE9D843", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-595") {
        addForeignKeyConstraint(baseColumnNames: "shipment_item_id", baseTableName: "order_shipment", constraintName: "FK9475736BB06EC4FB", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-596") {
        addForeignKeyConstraint(baseColumnNames: "unit_of_measure_id", baseTableName: "product_attribute", constraintName: "FK94A534C24DEBC91", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-597") {
        addForeignKeyConstraint(baseColumnNames: "product_supplier_id", baseTableName: "product_attribute", constraintName: "FK94A534C29542386", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_supplier", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-598") {
        addForeignKeyConstraint(baseColumnNames: "attribute_id", baseTableName: "product_attribute", constraintName: "FK94A534C47B0D087", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "attribute", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-599") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_attribute", constraintName: "FK94A534CDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-600") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "synonym", constraintName: "FK98293BFB217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-601") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "synonym", constraintName: "FK98293BFB426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-602") {
        addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "shipment_workflow_document", constraintName: "FK9A945A36C800AA15", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "document", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-603") {
        addForeignKeyConstraint(baseColumnNames: "shipment_workflow_id", baseTableName: "shipment_workflow_document", constraintName: "FK9A945A36EC587CFB", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment_workflow", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-604") {
        addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "product_category", constraintName: "FKA0303E4EEF4C770D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "category", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-605") {
        addForeignKeyConstraint(baseColumnNames: "tag_id", baseTableName: "product_tag", constraintName: "FKA71CAC4A9740C85F", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "tag", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-606") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_tag", constraintName: "FKA71CAC4ADED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-607") {
        addForeignKeyConstraint(baseColumnNames: "product_key_id", baseTableName: "consumption_fact", constraintName: "FKA8B7A49072882836", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-608") {
        addForeignKeyConstraint(baseColumnNames: "location_key_id", baseTableName: "consumption_fact", constraintName: "FKA8B7A490A27827C2", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-609") {
        addForeignKeyConstraint(baseColumnNames: "transaction_date_key_id", baseTableName: "consumption_fact", constraintName: "FKA8B7A490CA32CFEF", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "date_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-610") {
        addForeignKeyConstraint(baseColumnNames: "lot_key_id", baseTableName: "consumption_fact", constraintName: "FKA8B7A490D1F27172", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "lot_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-611") {
        addForeignKeyConstraint(baseColumnNames: "transaction_id", baseTableName: "transaction_entry", constraintName: "FKABC21FD12EF4C7F4", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "transaction", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-612") {
        addForeignKeyConstraint(baseColumnNames: "bin_location_id", baseTableName: "transaction_entry", constraintName: "FKABC21FD169DB749D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-613") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "transaction_entry", constraintName: "FKABC21FD1AA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-614") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "transaction_entry", constraintName: "FKABC21FD1DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-615") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "receipt_item", constraintName: "FKAE3064BA44979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-616") {
        addForeignKeyConstraint(baseColumnNames: "bin_location_id", baseTableName: "receipt_item", constraintName: "FKAE3064BA69DB749D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-617") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "receipt_item", constraintName: "FKAE3064BAAA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-618") {
        addForeignKeyConstraint(baseColumnNames: "shipment_item_id", baseTableName: "receipt_item", constraintName: "FKAE3064BAB06EC4FB", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-619") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "receipt_item", constraintName: "FKAE3064BADED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-620") {
        addForeignKeyConstraint(baseColumnNames: "receipt_id", baseTableName: "receipt_item", constraintName: "FKAE3064BAF7076438", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "receipt", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-621") {
        addForeignKeyConstraint(baseColumnNames: "component_product_id", baseTableName: "product_component", constraintName: "FKB511C5AD20E351EA", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-622") {
        addForeignKeyConstraint(baseColumnNames: "unit_of_measure_id", baseTableName: "product_component", constraintName: "FKB511C5AD24DEBC91", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-623") {
        addForeignKeyConstraint(baseColumnNames: "assembly_product_id", baseTableName: "product_component", constraintName: "FKB511C5ADFB4C199C", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-624") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "inventory_item_snapshot", constraintName: "FKBD34ABCD8ABEBD5", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-625") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "inventory_item_snapshot", constraintName: "FKBD34ABCDAA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-626") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "inventory_item_snapshot", constraintName: "FKBD34ABCDDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-627") {
        addForeignKeyConstraint(baseColumnNames: "internal_location_id", baseTableName: "inventory_level", constraintName: "FKC254A2E16CDADD53", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-628") {
        addForeignKeyConstraint(baseColumnNames: "inventory_id", baseTableName: "inventory_level", constraintName: "FKC254A2E172A2C5B4", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-629") {
        addForeignKeyConstraint(baseColumnNames: "preferred_bin_location_id", baseTableName: "inventory_level", constraintName: "FKC254A2E1CFDCB4DF", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-630") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "inventory_level", constraintName: "FKC254A2E1DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-631") {
        addForeignKeyConstraint(baseColumnNames: "replenishment_location_id", baseTableName: "inventory_level", constraintName: "FKC254A2E1F07D879A", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-632") {
        addForeignKeyConstraint(baseColumnNames: "comment_id", baseTableName: "shipment_comment", constraintName: "FKC398CCBAC4A49BBF", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "comment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-633") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_dimension", constraintName: "FKC73E1616DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-634") {
        addForeignKeyConstraint(baseColumnNames: "unit_of_measure_class_id", baseTableName: "attribute", constraintName: "FKC7AA9C4013CE80", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure_class", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-635") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "consumption", constraintName: "FKCD71F39B8ABEBD5", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-636") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "consumption", constraintName: "FKCD71F39BAA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-637") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "consumption", constraintName: "FKCD71F39BDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-638") {
        addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "product_document", constraintName: "FKD08A526BC800AA15", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "document", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-639") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_document", constraintName: "FKD08A526BDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-640") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "lot_dimension", constraintName: "FKD2EAD9F8AA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-641") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "picklist", constraintName: "FKD3F8383F217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-642") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "picklist", constraintName: "FKD3F8383F426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-643") {
        addForeignKeyConstraint(baseColumnNames: "requisition_id", baseTableName: "picklist", constraintName: "FKD3F8383F5DE9E374", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "requisition", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-644") {
        addForeignKeyConstraint(baseColumnNames: "picker_id", baseTableName: "picklist", constraintName: "FKD3F8383FA3E976BC", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-645") {
        addForeignKeyConstraint(baseColumnNames: "bin_location_id", baseTableName: "product_availability", constraintName: "FKD3FC6EAB69DB749D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-646") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "product_availability", constraintName: "FKD3FC6EAB8ABEBD5", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-647") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "product_availability", constraintName: "FKD3FC6EABAA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-648") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_availability", constraintName: "FKD3FC6EABDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-649") {
        addForeignKeyConstraint(baseColumnNames: "shipment_type_id", baseTableName: "shipment_workflow", constraintName: "FKD584C4C4FF77FF9B", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-650") {
        addForeignKeyConstraint(baseColumnNames: "reference_number_type_id", baseTableName: "reference_number", constraintName: "FKD790DEBD154F600", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "reference_number_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-651") {
        addForeignKeyConstraint(baseColumnNames: "requisition_item_id", baseTableName: "shipment_item", constraintName: "FKDA3BB2981CD3412D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "requisition_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-652") {
        addForeignKeyConstraint(baseColumnNames: "shipment_id", baseTableName: "shipment_item", constraintName: "FKDA3BB2983B5F6286", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-653") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "shipment_item", constraintName: "FKDA3BB29844979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-654") {
        addForeignKeyConstraint(baseColumnNames: "donor_id", baseTableName: "shipment_item", constraintName: "FKDA3BB29849AB6B52", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "donor", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-655") {
        addForeignKeyConstraint(baseColumnNames: "bin_location_id", baseTableName: "shipment_item", constraintName: "FKDA3BB29869DB749D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-656") {
        addForeignKeyConstraint(baseColumnNames: "container_id", baseTableName: "shipment_item", constraintName: "FKDA3BB2987400E88E", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "container", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-657") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "shipment_item", constraintName: "FKDA3BB298AA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-658") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "shipment_item", constraintName: "FKDA3BB298DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-659") {
        addForeignKeyConstraint(baseColumnNames: "container_type_id", baseTableName: "shipment_workflow_container_type", constraintName: "FKDEF5AD1317A6E251", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "container_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-660") {
        addForeignKeyConstraint(baseColumnNames: "shipper_id", baseTableName: "shipper_service", constraintName: "FKDF7559D73896C98E", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipper", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-661") {
        addForeignKeyConstraint(baseColumnNames: "shipment_item_id", baseTableName: "fulfillment_item_shipment_item", constraintName: "FKE071DE6DB06EC4FB", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-662") {
        addForeignKeyConstraint(baseColumnNames: "fulfillment_item_shipment_items_id", baseTableName: "fulfillment_item_shipment_item", constraintName: "FKE071DE6DB42751E1", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "fulfillment_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-663") {
        addForeignKeyConstraint(baseColumnNames: "destination_id", baseTableName: "shipment", constraintName: "FKE139719A1E2B3CDC", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-664") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "shipment", constraintName: "FKE139719A217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-665") {
        addForeignKeyConstraint(baseColumnNames: "carrier_id", baseTableName: "shipment", constraintName: "FKE139719A294C1012", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-666") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "shipment", constraintName: "FKE139719A426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-667") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "shipment", constraintName: "FKE139719A44979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-668") {
        addForeignKeyConstraint(baseColumnNames: "donor_id", baseTableName: "shipment", constraintName: "FKE139719A49AB6B52", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "donor", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-669") {
        addForeignKeyConstraint(baseColumnNames: "requisition_id", baseTableName: "shipment", constraintName: "FKE139719A5DE9E374", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "requisition", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-670") {
        addForeignKeyConstraint(baseColumnNames: "shipment_method_id", baseTableName: "shipment", constraintName: "FKE139719AA28CC5FB", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment_method", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-671") {
        addForeignKeyConstraint(baseColumnNames: "current_event_id", baseTableName: "shipment", constraintName: "FKE139719AD95ACF25", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "event", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-672") {
        addForeignKeyConstraint(baseColumnNames: "origin_id", baseTableName: "shipment", constraintName: "FKE139719ADBDEDAC4", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-673") {
        addForeignKeyConstraint(baseColumnNames: "shipment_type_id", baseTableName: "shipment", constraintName: "FKE139719AFF77FF9B", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-674") {
        addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "order_document", constraintName: "FKE698D2ECC800AA15", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "document", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-675") {
        addForeignKeyConstraint(baseColumnNames: "order_documents_id", baseTableName: "order_document", constraintName: "FKE698D2ECFE10118D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-676") {
        addForeignKeyConstraint(baseColumnNames: "bin_location_id", baseTableName: "picklist_item", constraintName: "FKE7584B1369DB749D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-677") {
        addForeignKeyConstraint(baseColumnNames: "container_type_id", baseTableName: "container", constraintName: "FKE7814C8117A6E251", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "container_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-678") {
        addForeignKeyConstraint(baseColumnNames: "shipment_id", baseTableName: "container", constraintName: "FKE7814C813B5F6286", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-679") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "container", constraintName: "FKE7814C8144979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-680") {
        addForeignKeyConstraint(baseColumnNames: "parent_container_id", baseTableName: "container", constraintName: "FKE7814C814B6A2E03", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "container", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-681") {
        addForeignKeyConstraint(baseColumnNames: "associated_product_id", baseTableName: "product_association", constraintName: "FKED441931C8653BC0", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-682") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_association", constraintName: "FKED441931DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-683") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "product", constraintName: "FKED8DCCEF217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-684") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "product", constraintName: "FKED8DCCEF426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-685") {
        addForeignKeyConstraint(baseColumnNames: "product_type_id", baseTableName: "product", constraintName: "FKED8DCCEFABD88AC6", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-686") {
        addForeignKeyConstraint(baseColumnNames: "default_uom_id", baseTableName: "product", constraintName: "FKED8DCCEFEEB2908D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-687") {
        addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "product", constraintName: "FKED8DCCEFEF4C770D", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "category", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-688") {
        addForeignKeyConstraint(baseColumnNames: "requisition_item_id", baseTableName: "fulfillment_item", constraintName: "FKEDC55CD447EBE106", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "requisition_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-689") {
        addForeignKeyConstraint(baseColumnNames: "fulfillment_id", baseTableName: "fulfillment_item", constraintName: "FKEDC55CD494567276", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "fulfillment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-690") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "fulfillment_item", constraintName: "FKEDC55CD4AA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-691") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "location_supported_activities", constraintName: "FKF58372688ABEBD5", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-692") {
        addForeignKeyConstraint(baseColumnNames: "click_stream_id", baseTableName: "click_stream_request", constraintName: "FKFD8E50671A43AB29", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "click_stream", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-693") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "inventory_item", constraintName: "FKFE019416DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-694") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "budget_code", constraintName: "fk_budget_code_created_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-695") {
        addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "budget_code", constraintName: "fk_budget_code_organization", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-696") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "budget_code", constraintName: "fk_budget_code_updated_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-697") {
        addForeignKeyConstraint(baseColumnNames: "gl_account_id", baseTableName: "category", constraintName: "fk_category_gl_account", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "gl_account", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-698") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "gl_account", constraintName: "fk_gl_account_created_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-699") {
        addForeignKeyConstraint(baseColumnNames: "gl_account_type_id", baseTableName: "gl_account", constraintName: "fk_gl_account_gl_account_type", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "gl_account_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-700") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "gl_account_type", constraintName: "fk_gl_account_type_created_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-701") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "gl_account_type", constraintName: "fk_gl_account_type_updated_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-702") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "gl_account", constraintName: "fk_gl_account_updated_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-703") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "invoice", constraintName: "fk_invoice_created_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-704") {
        addForeignKeyConstraint(baseColumnNames: "currency_uom_id", baseTableName: "invoice", constraintName: "fk_invoice_currency_uom", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-705") {
        addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "invoice_document", constraintName: "fk_invoice_document_document", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "document", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-706") {
        addForeignKeyConstraint(baseColumnNames: "invoice_id", baseTableName: "invoice_document", constraintName: "fk_invoice_document_invoice", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "invoice", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-707") {
        addForeignKeyConstraint(baseColumnNames: "invoice_type_id", baseTableName: "invoice", constraintName: "fk_invoice_invoice_type", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "invoice_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-708") {
        addForeignKeyConstraint(baseColumnNames: "budget_code_id", baseTableName: "invoice_item", constraintName: "fk_invoice_item_budget_code", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "budget_code", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-709") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "invoice_item", constraintName: "fk_invoice_item_created_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-710") {
        addForeignKeyConstraint(baseColumnNames: "gl_account_id", baseTableName: "invoice_item", constraintName: "fk_invoice_item_gl_account", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "gl_account", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-711") {
        addForeignKeyConstraint(baseColumnNames: "invoice_id", baseTableName: "invoice_item", constraintName: "fk_invoice_item_invoice", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "invoice", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-712") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "invoice_item", constraintName: "fk_invoice_item_product", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-713") {
        addForeignKeyConstraint(baseColumnNames: "quantity_uom_id", baseTableName: "invoice_item", constraintName: "fk_invoice_item_quantity_uom", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-714") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "invoice_item", constraintName: "fk_invoice_item_updated_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-715") {
        addForeignKeyConstraint(baseColumnNames: "party_id", baseTableName: "invoice", constraintName: "fk_invoice_party", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-716") {
        addForeignKeyConstraint(baseColumnNames: "party_from_id", baseTableName: "invoice", constraintName: "fk_invoice_party_from", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-717") {
        addForeignKeyConstraint(baseColumnNames: "invoice_reference_numbers_id", baseTableName: "invoice_reference_number", constraintName: "fk_invoice_reference_number_invoice_id", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "invoice", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-718") {
        addForeignKeyConstraint(baseColumnNames: "reference_number_id", baseTableName: "invoice_reference_number", constraintName: "fk_invoice_reference_number_reference_number_id", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "reference_number", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-719") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "invoice_type", constraintName: "fk_invoice_type_created_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-720") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "invoice_type", constraintName: "fk_invoice_type_updated_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-721") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "invoice", constraintName: "fk_invoice_updated_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-722") {
        addForeignKeyConstraint(baseColumnNames: "budget_code_id", baseTableName: "order_adjustment", constraintName: "fk_order_adjustment_budget_code", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "budget_code", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-723") {
        addForeignKeyConstraint(baseColumnNames: "gl_account_id", baseTableName: "order_adjustment", constraintName: "fk_order_adjustment_gl_account", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "gl_account", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-724") {
        addForeignKeyConstraint(baseColumnNames: "gl_account_id", baseTableName: "order_adjustment_type", constraintName: "fk_order_adjustment_type_gl_account", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "gl_account", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-725") {
        addForeignKeyConstraint(baseColumnNames: "budget_code_id", baseTableName: "order_item", constraintName: "fk_order_item_budget_code", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "budget_code", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-726") {
        addForeignKeyConstraint(baseColumnNames: "gl_account_id", baseTableName: "order_item", constraintName: "fk_order_item_gl_account", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "gl_account", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-727") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "order_type", constraintName: "fk_order_type_created_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-728") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "order_type", constraintName: "fk_order_type_updated_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-729") {
        addForeignKeyConstraint(baseColumnNames: "order_item_id", baseTableName: "picklist_item", constraintName: "fk_picklist_item_order_item", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-730") {
        addForeignKeyConstraint(baseColumnNames: "order_id", baseTableName: "picklist", constraintName: "fk_picklist_order", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-731") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "preference_type", constraintName: "fk_preference_type_created_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-732") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "preference_type", constraintName: "fk_preference_type_updated_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-733") {
        addForeignKeyConstraint(baseColumnNames: "mutual_association_id", baseTableName: "product_association", constraintName: "fk_product_association_mutual_association", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_association", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-734") {
        addForeignKeyConstraint(baseColumnNames: "gl_account_id", baseTableName: "product", constraintName: "fk_product_gl_account", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "gl_account", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-735") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "product_merge_logger", constraintName: "fk_product_merge_logger_created_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-736") {
        addForeignKeyConstraint(baseColumnNames: "obsolete_product_id", baseTableName: "product_merge_logger", constraintName: "fk_product_merge_logger_obsolete_product", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-737") {
        addForeignKeyConstraint(baseColumnNames: "primary_product_id", baseTableName: "product_merge_logger", constraintName: "fk_product_merge_logger_primary_product", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-738") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "product_merge_logger", constraintName: "fk_product_merge_logger_updated_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-739") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "product_price", constraintName: "fk_product_price_created_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-740") {
        addForeignKeyConstraint(baseColumnNames: "currency_id", baseTableName: "product_price", constraintName: "fk_product_price_unit_of_measure", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-741") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "product_price", constraintName: "fk_product_price_updated_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-742") {
        addForeignKeyConstraint(baseColumnNames: "product_family_id", baseTableName: "product", constraintName: "fk_product_product_family", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_group", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-743") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "product_supplier_preference", constraintName: "fk_product_supplier_preference_created_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-744") {
        addForeignKeyConstraint(baseColumnNames: "destination_party_id", baseTableName: "product_supplier_preference", constraintName: "fk_product_supplier_preference_destination_party", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-745") {
        addForeignKeyConstraint(baseColumnNames: "preference_type_id", baseTableName: "product_supplier_preference", constraintName: "fk_product_supplier_preference_preference_type", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "preference_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-746") {
        addForeignKeyConstraint(baseColumnNames: "product_supplier_id", baseTableName: "product_supplier_preference", constraintName: "fk_product_supplier_preference_product_supplier", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_supplier", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-747") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "product_supplier_preference", constraintName: "fk_product_supplier_preference_updated_by", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-748") {
        addForeignKeyConstraint(baseColumnNames: "product_type_id", baseTableName: "product_type_displayed_fields", constraintName: "fk_product_type_displayed_fields_product_type", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-749") {
        addForeignKeyConstraint(baseColumnNames: "product_type_id", baseTableName: "product_type_required_fields", constraintName: "fk_product_type_required_fields_product_type", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-750") {
        addForeignKeyConstraint(baseColumnNames: "product_type_id", baseTableName: "product_type_supported_activities", constraintName: "fk_product_type_supported_activities_product_type", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "product_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-751") {
        addForeignKeyConstraint(baseColumnNames: "shipment_reference_numbers_id", baseTableName: "shipment_reference_number", constraintName: "fk_shipment_reference_number_shipment", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-752") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "location_role", constraintName: "location_role_ibfk_1", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-753") {
        addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "location_role", constraintName: "location_role_ibfk_2", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "role", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-754") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "location_role", constraintName: "location_role_ibfk_3", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-755") {
        addForeignKeyConstraint(baseColumnNames: "order_adjustment_id", baseTableName: "order_adjustment_invoice", constraintName: "order_adjustment_invoice_ibfk_1", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "order_adjustment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-756") {
        addForeignKeyConstraint(baseColumnNames: "invoice_item_id", baseTableName: "order_adjustment_invoice", constraintName: "order_adjustment_invoice_ibfk_2", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "invoice_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-757") {
        addForeignKeyConstraint(baseColumnNames: "requisition_item_id", baseTableName: "picklist_item", constraintName: "picklist_item_ibfk_1", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "requisition_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-758") {
        addForeignKeyConstraint(baseColumnNames: "picklist_id", baseTableName: "picklist_item", constraintName: "picklist_item_ibfk_2", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "picklist", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-759") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "picklist_item", constraintName: "picklist_item_ibfk_3", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-760") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "picklist_item", constraintName: "picklist_item_ibfk_4", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-761") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "picklist_item", constraintName: "picklist_item_ibfk_5", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-762") {
        addForeignKeyConstraint(baseColumnNames: "shipment_item_id", baseTableName: "shipment_invoice", constraintName: "shipment_invoice_ibfk_1", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "shipment_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-763") {
        addForeignKeyConstraint(baseColumnNames: "invoice_item_id", baseTableName: "shipment_invoice", constraintName: "shipment_invoice_ibfk_2", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "invoice_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1692045990425-764") {
        addForeignKeyConstraint(baseColumnNames: "zone_id", baseTableName: "location", constraintName: "zone_location_ibfk_3", deferrable: "false", initiallyDeferred: "false", onDelete: "RESTRICT", onUpdate: "RESTRICT", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }
}
