databaseChangeLog = {

    changeSet(author: "jmiranda (generated)", id: "1613884772808-1") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-2") {
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

            column(name: "unit_of_measure_class_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-3") {
        createTable(tableName: "attribute_options") {
            column(name: "attribute_id", type: "CHAR(38)")

            column(name: "options_string", type: "VARCHAR(255)")

            column(name: "options_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-4") {
        createTable(tableName: "budget_code") {
            column(name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-5") {
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

            column(name: "gl_account_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-6") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-7") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-8") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-9") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-10") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-11") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-12") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-13") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-14") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-15") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-16") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-17") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-18") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-19") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-20") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-21") {
        createTable(tableName: "fulfillment_item_shipment_item") {
            column(name: "fulfillment_item_shipment_items_id", type: "CHAR(38)")

            column(name: "shipment_item_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-22") {
        createTable(tableName: "gl_account") {
            column(name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-23") {
        createTable(tableName: "gl_account_type") {
            column(name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-24") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-25") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-26") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-27") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-28") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-29") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-30") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-31") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-32") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-33") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-34") {
        createTable(tableName: "location_role") {
            column(name: "user_id", type: "CHAR(38)")

            column(name: "location_id", type: "CHAR(38)")

            column(name: "role_id", type: "CHAR(38)")

            column(name: "version", type: "INT")

            column(name: "id", type: "CHAR(38)")

            column(name: "location_roles_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-35") {
        createTable(tableName: "location_supported_activities") {
            column(name: "location_id", type: "CHAR(38)")

            column(name: "supported_activities_string", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-36") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-37") {
        createTable(tableName: "location_type_supported_activities") {
            column(name: "location_type_id", type: "CHAR(38)")

            column(name: "supported_activities_string", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-38") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-39") {
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

            column(name: "exchange_rate", type: "DECIMAL(19, 8)")

            column(name: "payment_method_type_id", type: "CHAR(38)")

            column(name: "payment_term_id", type: "CHAR(38)")

            column(name: "approved_by_id", type: "CHAR(38)")

            column(name: "date_approved", type: "datetime")

            column(name: "destination_party_id", type: "CHAR(38)")

            column(name: "origin_party_id", type: "CHAR(38)")

            column(name: "created_by_id", type: "CHAR(38)")

            column(name: "updated_by_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-40") {
        createTable(tableName: "order_adjustment") {
            column(name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-41") {
        createTable(tableName: "order_adjustment_type") {
            column(name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-42") {
        createTable(tableName: "order_comment") {
            column(name: "order_comments_id", type: "CHAR(38)")

            column(name: "comment_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-43") {
        createTable(tableName: "order_document") {
            column(name: "order_documents_id", type: "CHAR(38)")

            column(name: "document_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-44") {
        createTable(tableName: "order_event") {
            column(name: "order_events_id", type: "CHAR(38)")

            column(name: "event_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-45") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-46") {
        createTable(tableName: "order_item_comment") {
            column(name: "order_item_comments_id", type: "CHAR(38)")

            column(name: "comment_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-47") {
        createTable(tableName: "order_shipment") {
            column(name: "order_item_id", type: "CHAR(38)")

            column(name: "shipment_item_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-48") {
        createTable(tableName: "organization_sequences") {
            column(name: "sequences", type: "VARCHAR(255)")

            column(name: "sequences_idx", type: "VARCHAR(255)")

            column(name: "sequences_elt", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-49") {
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

            column(name: "default_location_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-50") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-51") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-52") {
        createTable(tableName: "payment_method_type") {
            column(name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-53") {
        createTable(tableName: "payment_term") {
            column(name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-54") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-55") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-56") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-57") {
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

            column(name: "gl_account_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-58") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-59") {
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

            column(name: "unit_of_measure_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-60") {
        createTable(tableName: "product_availability") {
            column(name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-61") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-62") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-63") {
        createTable(tableName: "product_category") {
            column(name: "product_id", type: "CHAR(38)")

            column(name: "category_id", type: "CHAR(38)")

            column(name: "categories_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-64") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-65") {
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

            column(defaultValueNumeric: "0", name: "quantity_picked", type: "DECIMAL(32)") {
                constraints(nullable: "false")
            }

            column(defaultValueNumeric: "0", name: "quantity_demand", type: "DECIMAL(32)") {
                constraints(nullable: "false")
            }

            column(name: "reason_code_classification", type: "VARCHAR(31)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-66") {
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

            column(defaultValueNumeric: "0", name: "quantity_picked", type: "DECIMAL(32)") {
                constraints(nullable: "false")
            }

            column(defaultValueNumeric: "0", name: "quantity_demand", type: "DECIMAL(32)") {
                constraints(nullable: "false")
            }

            column(name: "reason_code_classification", type: "VARCHAR(31)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-67") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-68") {
        createTable(tableName: "product_document") {
            column(name: "product_id", type: "CHAR(38)")

            column(name: "document_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-69") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-70") {
        createTable(tableName: "product_group_product") {
            column(name: "product_group_id", type: "CHAR(38)")

            column(name: "product_id", type: "CHAR(38)")

            column(name: "products_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-71") {
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

            column(name: "product_supplier_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-72") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-73") {
        createTable(tableName: "product_tag") {
            column(name: "product_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "tag_id", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-74") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-75") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-76") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-77") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-78") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-79") {
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

            column(name: "source_type", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-80") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-81") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-82") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-83") {
        createTable(tableName: "shipment_comment") {
            column(name: "shipment_comments_id", type: "CHAR(38)")

            column(name: "comment_id", type: "CHAR(38)")

            column(name: "comments_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-84") {
        createTable(tableName: "shipment_document") {
            column(name: "shipment_documents_id", type: "CHAR(38)")

            column(name: "document_id", type: "CHAR(38)")

            column(name: "documents_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-85") {
        createTable(tableName: "shipment_event") {
            column(name: "shipment_events_id", type: "CHAR(38)")

            column(name: "event_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-86") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-87") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-88") {
        createTable(tableName: "shipment_reference_number") {
            column(name: "shipment_reference_numbers_id", type: "CHAR(38)")

            column(name: "reference_number_id", type: "CHAR(38)")

            column(name: "reference_numbers_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-89") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-90") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-91") {
        createTable(tableName: "shipment_workflow_container_type") {
            column(name: "shipment_workflow_container_types_id", type: "CHAR(38)")

            column(name: "container_type_id", type: "CHAR(38)")

            column(name: "container_types_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-92") {
        createTable(tableName: "shipment_workflow_document") {
            column(name: "shipment_workflow_id", type: "CHAR(38)") {
                constraints(nullable: "false")
            }

            column(name: "document_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-93") {
        createTable(tableName: "shipment_workflow_document_template") {
            column(name: "shipment_workflow_id", type: "CHAR(38)")

            column(name: "document_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-94") {
        createTable(tableName: "shipment_workflow_reference_number_type") {
            column(name: "shipment_workflow_reference_number_types_id", type: "CHAR(38)")

            column(name: "reference_number_type_id", type: "CHAR(38)")

            column(name: "reference_number_types_idx", type: "INT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-95") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-96") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-97") {
        createTable(tableName: "stockout_fact") {
            column(name: "date_dimension_id", type: "BIGINT") {
                constraints(primaryKey: "true")
            }

            column(name: "location_dimension_id", type: "BIGINT") {
                constraints(primaryKey: "true")
            }

            column(name: "product_dimension_id", type: "BIGINT") {
                constraints(primaryKey: "true")
            }

            column(name: "quantity_on_hand", type: "SMALLINT")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-98") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-99") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-100") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-101") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-102") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-103") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-104") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-105") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-106") {
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-107") {
        createTable(tableName: "unit_of_measure_conversion") {
            column(name: "id", type: "CHAR(38)") {
                constraints(primaryKey: "true")
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

    changeSet(author: "jmiranda (generated)", id: "1613884772808-108") {
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

            column(name: "dashboard_config", type: "LONGBLOB")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-109") {
        createTable(tableName: "user_role") {
            column(name: "user_id", type: "CHAR(38)")

            column(name: "role_id", type: "CHAR(38)")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-110") {
        addUniqueConstraint(columnNames: "date, location_id, product_code, lot_number, bin_location_name", constraintName: "inventory_snapshot_uniq_idx", tableName: "inventory_snapshot")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-111") {
        addUniqueConstraint(columnNames: "code, locale", constraintName: "localization_code_locale_idx", tableName: "localization")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-112") {
        addUniqueConstraint(columnNames: "location_id, product_code, lot_number, bin_location_name", constraintName: "product_availability_uniq_idx", tableName: "product_availability")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-113") {
        addUniqueConstraint(columnNames: "product_id, lot_number", constraintName: "product_id", tableName: "inventory_item")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-114") {
        addUniqueConstraint(columnNames: "product_id, product_supplier_id, uom_id, quantity", constraintName: "product_package_uniq_idx", tableName: "product_package")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-115") {
        createIndex(indexName: "FK1143A95C8ABEBD5", tableName: "location_dimension") {
            column(name: "location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-116") {
        createIndex(indexName: "FK143BF46AA462C195", tableName: "user_role") {
            column(name: "user_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-117") {
        createIndex(indexName: "FK143BF46AFF37FDB5", tableName: "user_role") {
            column(name: "role_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-118") {
        createIndex(indexName: "FK1799509C20E33E1C", tableName: "requisition") {
            column(name: "verified_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-119") {
        createIndex(indexName: "FK1799509C217F5972", tableName: "requisition") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-120") {
        createIndex(indexName: "FK1799509C2BDD17B3", tableName: "requisition") {
            column(name: "requisition_template_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-121") {
        createIndex(indexName: "FK1799509C36C69275", tableName: "requisition") {
            column(name: "received_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-122") {
        createIndex(indexName: "FK1799509C426DD105", tableName: "requisition") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-123") {
        createIndex(indexName: "FK1799509C4CF042D8", tableName: "requisition") {
            column(name: "delivered_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-124") {
        createIndex(indexName: "FK1799509CD196DBBF", tableName: "requisition") {
            column(name: "issued_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-125") {
        createIndex(indexName: "FK1799509CD2CB8BBB", tableName: "requisition") {
            column(name: "checked_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-126") {
        createIndex(indexName: "FK1799509CDFA74E0B", tableName: "requisition") {
            column(name: "reviewed_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-127") {
        createIndex(indexName: "FK187E54C9DED5FAE7", tableName: "product_catalog_item") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-128") {
        createIndex(indexName: "FK187E54C9FB5E604E", tableName: "product_catalog_item") {
            column(name: "product_catalog_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-129") {
        createIndex(indexName: "FK1BF9A217F5972", tableName: "tag") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-130") {
        createIndex(indexName: "FK1BF9A426DD105", tableName: "tag") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-131") {
        createIndex(indexName: "FK1C92FE2F3E67CF9F", tableName: "party_role") {
            column(name: "party_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-132") {
        createIndex(indexName: "FK1E50D72D72882836", tableName: "transaction_fact") {
            column(name: "product_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-133") {
        createIndex(indexName: "FK1E50D72DA27827C2", tableName: "transaction_fact") {
            column(name: "location_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-134") {
        createIndex(indexName: "FK1E50D72DCA32CFEF", tableName: "transaction_fact") {
            column(name: "transaction_date_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-135") {
        createIndex(indexName: "FK1E50D72DCA354381", tableName: "transaction_fact") {
            column(name: "transaction_type_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-136") {
        createIndex(indexName: "FK1E50D72DD1F27172", tableName: "transaction_fact") {
            column(name: "lot_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-137") {
        createIndex(indexName: "FK299E50ABDED5FAE7", tableName: "synonym") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-138") {
        createIndex(indexName: "FK2D110D6418D76D84", tableName: "order_item") {
            column(name: "origin_bin_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-139") {
        createIndex(indexName: "FK2D110D6429542386", tableName: "order_item") {
            column(name: "product_supplier_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-140") {
        createIndex(indexName: "FK2D110D6429B2552E", tableName: "order_item") {
            column(name: "product_package_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-141") {
        createIndex(indexName: "FK2D110D6444979D51", tableName: "order_item") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-142") {
        createIndex(indexName: "FK2D110D6451A9416E", tableName: "order_item") {
            column(name: "parent_order_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-143") {
        createIndex(indexName: "FK2D110D645ED93B03", tableName: "order_item") {
            column(name: "quantity_uom_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-144") {
        createIndex(indexName: "FK2D110D64605326C", tableName: "order_item") {
            column(name: "destination_bin_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-145") {
        createIndex(indexName: "FK2D110D64911E7578", tableName: "order_item") {
            column(name: "requested_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-146") {
        createIndex(indexName: "FK2D110D64AA992CED", tableName: "order_item") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-147") {
        createIndex(indexName: "FK2D110D64D08EDBE6", tableName: "order_item") {
            column(name: "order_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-148") {
        createIndex(indexName: "FK2D110D64DED5FAE7", tableName: "order_item") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-149") {
        createIndex(indexName: "FK2D110D64EF4C770D", tableName: "order_item") {
            column(name: "category_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-150") {
        createIndex(indexName: "FK2DE9EE6EB8839C0F", tableName: "order_comment") {
            column(name: "order_comments_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-151") {
        createIndex(indexName: "FK2DE9EE6EC4A49BBF", tableName: "order_comment") {
            column(name: "comment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-152") {
        createIndex(indexName: "FK2E4511844A3E746", tableName: "unit_of_measure_conversion") {
            column(name: "from_unit_of_measure_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-153") {
        createIndex(indexName: "FK2E4511849B9434D5", tableName: "unit_of_measure_conversion") {
            column(name: "to_unit_of_measure_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-154") {
        createIndex(indexName: "FK302BCFE619A2EF8", tableName: "category") {
            column(name: "parent_category_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-155") {
        createIndex(indexName: "FK312F6C292388BC5", tableName: "shipment_reference_number") {
            column(name: "reference_number_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-156") {
        createIndex(indexName: "FK313A4BDF14F7BB8E", tableName: "product_group_product") {
            column(name: "product_group_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-157") {
        createIndex(indexName: "FK313A4BDFDED5FAE7", tableName: "product_group_product") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-158") {
        createIndex(indexName: "FK335CD11B6631D8CC", tableName: "document") {
            column(name: "document_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-159") {
        createIndex(indexName: "FK36EBCB1F28CE07", tableName: "user") {
            column(name: "warehouse_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-160") {
        createIndex(indexName: "FK36EBCB41E07A73", tableName: "user") {
            column(name: "manager_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-161") {
        createIndex(indexName: "FK38A5EE5FAF1302EB", tableName: "comment") {
            column(name: "sender_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-162") {
        createIndex(indexName: "FK38A5EE5FF885F087", tableName: "comment") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-163") {
        createIndex(indexName: "FK3A097B1C24DEBC91", tableName: "product_supplier") {
            column(name: "unit_of_measure_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-164") {
        createIndex(indexName: "FK3A097B1C2A475A37", tableName: "product_supplier") {
            column(name: "manufacturer_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-165") {
        createIndex(indexName: "FK3A097B1CDED5FAE7", tableName: "product_supplier") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-166") {
        createIndex(indexName: "FK3A097B1CF42F7E5C", tableName: "product_supplier") {
            column(name: "supplier_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-167") {
        createIndex(indexName: "FK40203B26296B2CA3", tableName: "shipment_method") {
            column(name: "shipper_service_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-168") {
        createIndex(indexName: "FK40203B263896C98E", tableName: "shipment_method") {
            column(name: "shipper_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-169") {
        createIndex(indexName: "FK408272383B5F6286", tableName: "receipt") {
            column(name: "shipment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-170") {
        createIndex(indexName: "FK4082723844979D51", tableName: "receipt") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-171") {
        createIndex(indexName: "FK414EF28F1E2B3CDC", tableName: "requisition") {
            column(name: "destination_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-172") {
        createIndex(indexName: "FK414EF28F44979D51", tableName: "requisition") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-173") {
        createIndex(indexName: "FK414EF28F94567276", tableName: "requisition") {
            column(name: "fulfillment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-174") {
        createIndex(indexName: "FK414EF28FDBDEDAC4", tableName: "requisition") {
            column(name: "origin_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-175") {
        createIndex(indexName: "FK414EF28FDD302242", tableName: "requisition") {
            column(name: "requested_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-176") {
        createIndex(indexName: "FK4A1ABEFE3BE9D843", tableName: "order_adjustment") {
            column(name: "order_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-177") {
        createIndex(indexName: "FK4A1ABEFED08EDBE6", tableName: "order_adjustment") {
            column(name: "order_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-178") {
        createIndex(indexName: "FK4A1ABEFEE1A39520", tableName: "order_adjustment") {
            column(name: "order_adjustment_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-179") {
        createIndex(indexName: "FK4BB27241154F600", tableName: "shipment_workflow_reference_number_type") {
            column(name: "reference_number_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-180") {
        createIndex(indexName: "FK4DA982C35DE21C87", tableName: "requisition_item") {
            column(name: "requisition_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-181") {
        createIndex(indexName: "FK4DA982C3AA992CED", tableName: "requisition_item") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-182") {
        createIndex(indexName: "FK4DA982C3DED5FAE7", tableName: "requisition_item") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-183") {
        createIndex(indexName: "FK4DA982C3EF4C770D", tableName: "requisition_item") {
            column(name: "category_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-184") {
        createIndex(indexName: "FK51F3772FEF4C770D", tableName: "product_group") {
            column(name: "category_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-185") {
        createIndex(indexName: "FK5358E4D614F7BB8E", tableName: "requisition_item") {
            column(name: "product_group_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-186") {
        createIndex(indexName: "FK5358E4D61594028E", tableName: "requisition_item") {
            column(name: "substitution_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-187") {
        createIndex(indexName: "FK5358E4D6217F5972", tableName: "requisition_item") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-188") {
        createIndex(indexName: "FK5358E4D629B2552E", tableName: "requisition_item") {
            column(name: "product_package_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-189") {
        createIndex(indexName: "FK5358E4D6405AC22D", tableName: "requisition_item") {
            column(name: "modification_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-190") {
        createIndex(indexName: "FK5358E4D6426DD105", tableName: "requisition_item") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-191") {
        createIndex(indexName: "FK5358E4D644979D51", tableName: "requisition_item") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-192") {
        createIndex(indexName: "FK5358E4D6DD302242", tableName: "requisition_item") {
            column(name: "requested_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-193") {
        createIndex(indexName: "FK5358E4D6F84BDE18", tableName: "requisition_item") {
            column(name: "parent_requisition_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-194") {
        createIndex(indexName: "FK5A2551DEAC392B33", tableName: "fulfillment") {
            column(name: "fulfilled_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-195") {
        createIndex(indexName: "FK5C6729A3D970DB4", tableName: "event") {
            column(name: "event_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-196") {
        createIndex(indexName: "FK5C6729A4415A5B0", tableName: "event") {
            column(name: "event_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-197") {
        createIndex(indexName: "FK5D1B504A217F5972", tableName: "unit_of_measure_class") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-198") {
        createIndex(indexName: "FK5D1B504A426DD105", tableName: "unit_of_measure_class") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-199") {
        createIndex(indexName: "FK5D1B504A6B9DFD", tableName: "unit_of_measure_class") {
            column(name: "base_uom_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-200") {
        createIndex(indexName: "FK615A48F6217F5972", tableName: "product_package") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-201") {
        createIndex(indexName: "FK615A48F629542386", tableName: "product_package") {
            column(name: "product_supplier_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-202") {
        createIndex(indexName: "FK615A48F63906C4CF", tableName: "product_package") {
            column(name: "uom_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-203") {
        createIndex(indexName: "FK615A48F6426DD105", tableName: "product_package") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-204") {
        createIndex(indexName: "FK651874E1E2B3CDC", tableName: "order") {
            column(name: "destination_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-205") {
        createIndex(indexName: "FK651874E240896CB", tableName: "order") {
            column(name: "approved_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-206") {
        createIndex(indexName: "FK651874E35D76CB0", tableName: "order") {
            column(name: "destination_party_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-207") {
        createIndex(indexName: "FK651874E41B7275F", tableName: "order") {
            column(name: "completed_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-208") {
        createIndex(indexName: "FK651874E44979D51", tableName: "order") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-209") {
        createIndex(indexName: "FK651874E6A8010C1", tableName: "order") {
            column(name: "payment_method_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-210") {
        createIndex(indexName: "FK651874E6D91063C", tableName: "order") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-211") {
        createIndex(indexName: "FK651874E8E7F7DCF", tableName: "order") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-212") {
        createIndex(indexName: "FK651874E9E52B00C", tableName: "order") {
            column(name: "payment_term_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-213") {
        createIndex(indexName: "FK651874EAF6D8801", tableName: "order") {
            column(name: "ordered_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-214") {
        createIndex(indexName: "FK651874EDBDEDAC4", tableName: "order") {
            column(name: "origin_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-215") {
        createIndex(indexName: "FK6581AE69DFE4C4C", tableName: "party") {
            column(name: "party_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-216") {
        createIndex(indexName: "FK6581AE6D1DFC6D7", tableName: "party") {
            column(name: "default_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-217") {
        createIndex(indexName: "FK6C5BE20C800AA15", tableName: "shipment_document") {
            column(name: "document_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-218") {
        createIndex(indexName: "FK6D032BB53B350242", tableName: "shipment_event") {
            column(name: "shipment_events_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-219") {
        createIndex(indexName: "FK6D032BB5786431F", tableName: "shipment_event") {
            column(name: "event_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-220") {
        createIndex(indexName: "FK714F9FB528F75F00", tableName: "location") {
            column(name: "location_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-221") {
        createIndex(indexName: "FK714F9FB53BB36E94", tableName: "location") {
            column(name: "location_group_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-222") {
        createIndex(indexName: "FK714F9FB541E07A73", tableName: "location") {
            column(name: "manager_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-223") {
        createIndex(indexName: "FK714F9FB5606C7D95", tableName: "location") {
            column(name: "organization_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-224") {
        createIndex(indexName: "FK714F9FB561ED379F", tableName: "location") {
            column(name: "address_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-225") {
        createIndex(indexName: "FK714F9FB572A2C5B4", tableName: "location") {
            column(name: "inventory_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-226") {
        createIndex(indexName: "FK714F9FB57AF9A3C0", tableName: "location") {
            column(name: "parent_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-227") {
        createIndex(indexName: "FK7348B491217F5972", tableName: "unit_of_measure") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-228") {
        createIndex(indexName: "FK7348B491426DD105", tableName: "unit_of_measure") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-229") {
        createIndex(indexName: "FK7348B49197D8303E", tableName: "unit_of_measure") {
            column(name: "uom_class_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-230") {
        createIndex(indexName: "FK740B54769DB749D", tableName: "inventory_snapshot") {
            column(name: "bin_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-231") {
        createIndex(indexName: "FK740B547AA992CED", tableName: "inventory_snapshot") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-232") {
        createIndex(indexName: "FK740B547DED5FAE7", tableName: "inventory_snapshot") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-233") {
        createIndex(indexName: "FK74D92A693D2E628A", tableName: "order_event") {
            column(name: "order_events_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-234") {
        createIndex(indexName: "FK74D92A69786431F", tableName: "order_event") {
            column(name: "event_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-235") {
        createIndex(indexName: "FK7975323F4CC49445", tableName: "local_transfer") {
            column(name: "destination_transaction_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-236") {
        createIndex(indexName: "FK7975323F57563498", tableName: "local_transfer") {
            column(name: "source_transaction_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-237") {
        createIndex(indexName: "FK7A19D7561ED379F", tableName: "location_group") {
            column(name: "address_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-238") {
        createIndex(indexName: "FK7AFF67F928F75F00", tableName: "location_type_supported_activities") {
            column(name: "location_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-239") {
        createIndex(indexName: "FK7FA0D2DE1E2B3CDC", tableName: "transaction") {
            column(name: "destination_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-240") {
        createIndex(indexName: "FK7FA0D2DE217F5972", tableName: "transaction") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-241") {
        createIndex(indexName: "FK7FA0D2DE3265A8A9", tableName: "transaction") {
            column(name: "confirmed_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-242") {
        createIndex(indexName: "FK7FA0D2DE426DD105", tableName: "transaction") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-243") {
        createIndex(indexName: "FK7FA0D2DE5DE9E374", tableName: "transaction") {
            column(name: "requisition_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-244") {
        createIndex(indexName: "FK7FA0D2DE5F12AFED", tableName: "transaction") {
            column(name: "incoming_shipment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-245") {
        createIndex(indexName: "FK7FA0D2DE72A2C5B4", tableName: "transaction") {
            column(name: "inventory_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-246") {
        createIndex(indexName: "FK7FA0D2DE828481AF", tableName: "transaction") {
            column(name: "source_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-247") {
        createIndex(indexName: "FK7FA0D2DEB3FB7111", tableName: "transaction") {
            column(name: "transaction_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-248") {
        createIndex(indexName: "FK7FA0D2DEB80B3233", tableName: "transaction") {
            column(name: "outgoing_shipment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-249") {
        createIndex(indexName: "FK7FA0D2DED08EDBE6", tableName: "transaction") {
            column(name: "order_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-250") {
        createIndex(indexName: "FK7FA0D2DEF7076438", tableName: "transaction") {
            column(name: "receipt_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-251") {
        createIndex(indexName: "FK9475736B3BE9D843", tableName: "order_shipment") {
            column(name: "order_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-252") {
        createIndex(indexName: "FK9475736BB06EC4FB", tableName: "order_shipment") {
            column(name: "shipment_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-253") {
        createIndex(indexName: "FK94A534C24DEBC91", tableName: "product_attribute") {
            column(name: "unit_of_measure_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-254") {
        createIndex(indexName: "FK94A534C47B0D087", tableName: "product_attribute") {
            column(name: "attribute_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-255") {
        createIndex(indexName: "FK94A534CDED5FAE7", tableName: "product_attribute") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-256") {
        createIndex(indexName: "FK98293BFB217F5972", tableName: "synonym") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-257") {
        createIndex(indexName: "FK98293BFB426DD105", tableName: "synonym") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-258") {
        createIndex(indexName: "FK9A945A36C800AA15", tableName: "shipment_workflow_document") {
            column(name: "document_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-259") {
        createIndex(indexName: "FK9A945A36EC587CFB", tableName: "shipment_workflow_document") {
            column(name: "shipment_workflow_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-260") {
        createIndex(indexName: "FKA0303E4EDED5FAE7", tableName: "product_category") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-261") {
        createIndex(indexName: "FKA0303E4EEF4C770D", tableName: "product_category") {
            column(name: "category_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-262") {
        createIndex(indexName: "FKA71CAC4A9740C85F", tableName: "product_tag") {
            column(name: "tag_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-263") {
        createIndex(indexName: "FKA71CAC4ADED5FAE7", tableName: "product_tag") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-264") {
        createIndex(indexName: "FKA8B7A49072882836", tableName: "consumption_fact") {
            column(name: "product_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-265") {
        createIndex(indexName: "FKA8B7A490A27827C2", tableName: "consumption_fact") {
            column(name: "location_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-266") {
        createIndex(indexName: "FKA8B7A490CA32CFEF", tableName: "consumption_fact") {
            column(name: "transaction_date_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-267") {
        createIndex(indexName: "FKA8B7A490D1F27172", tableName: "consumption_fact") {
            column(name: "lot_key_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-268") {
        createIndex(indexName: "FKABC21FD12EF4C7F4", tableName: "transaction_entry") {
            column(name: "transaction_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-269") {
        createIndex(indexName: "FKABC21FD169DB749D", tableName: "transaction_entry") {
            column(name: "bin_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-270") {
        createIndex(indexName: "FKABC21FD1AA992CED", tableName: "transaction_entry") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-271") {
        createIndex(indexName: "FKABC21FD1DED5FAE7", tableName: "transaction_entry") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-272") {
        createIndex(indexName: "FKAE3064BA44979D51", tableName: "receipt_item") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-273") {
        createIndex(indexName: "FKAE3064BA69DB749D", tableName: "receipt_item") {
            column(name: "bin_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-274") {
        createIndex(indexName: "FKAE3064BAAA992CED", tableName: "receipt_item") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-275") {
        createIndex(indexName: "FKAE3064BAB06EC4FB", tableName: "receipt_item") {
            column(name: "shipment_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-276") {
        createIndex(indexName: "FKAE3064BADED5FAE7", tableName: "receipt_item") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-277") {
        createIndex(indexName: "FKAE3064BAF7076438", tableName: "receipt_item") {
            column(name: "receipt_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-278") {
        createIndex(indexName: "FKB511C5AD20E351EA", tableName: "product_component") {
            column(name: "component_product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-279") {
        createIndex(indexName: "FKB511C5AD24DEBC91", tableName: "product_component") {
            column(name: "unit_of_measure_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-280") {
        createIndex(indexName: "FKB511C5ADFB4C199C", tableName: "product_component") {
            column(name: "assembly_product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-281") {
        createIndex(indexName: "FKB5A4FE84C4A49BBF", tableName: "order_item_comment") {
            column(name: "comment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-282") {
        createIndex(indexName: "FKC254A2E172A2C5B4", tableName: "inventory_level") {
            column(name: "inventory_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-283") {
        createIndex(indexName: "FKC254A2E1CFDCB4DF", tableName: "inventory_level") {
            column(name: "preferred_bin_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-284") {
        createIndex(indexName: "FKC254A2E1DED5FAE7", tableName: "inventory_level") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-285") {
        createIndex(indexName: "FKC254A2E1F07D879A", tableName: "inventory_level") {
            column(name: "replenishment_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-286") {
        createIndex(indexName: "FKC398CCBAC4A49BBF", tableName: "shipment_comment") {
            column(name: "comment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-287") {
        createIndex(indexName: "FKC73E1616DED5FAE7", tableName: "product_dimension") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-288") {
        createIndex(indexName: "FKC7AA9C4013CE80", tableName: "attribute") {
            column(name: "unit_of_measure_class_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-289") {
        createIndex(indexName: "FKCD71F39B8ABEBD5", tableName: "consumption") {
            column(name: "location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-290") {
        createIndex(indexName: "FKCD71F39BAA992CED", tableName: "consumption") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-291") {
        createIndex(indexName: "FKCD71F39BDED5FAE7", tableName: "consumption") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-292") {
        createIndex(indexName: "FKD08A526BC800AA15", tableName: "product_document") {
            column(name: "document_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-293") {
        createIndex(indexName: "FKD08A526BDED5FAE7", tableName: "product_document") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-294") {
        createIndex(indexName: "FKD3F8383F217F5972", tableName: "picklist") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-295") {
        createIndex(indexName: "FKD3F8383F426DD105", tableName: "picklist") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-296") {
        createIndex(indexName: "FKD3F8383F5DE9E374", tableName: "picklist") {
            column(name: "requisition_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-297") {
        createIndex(indexName: "FKD3F8383FA3E976BC", tableName: "picklist") {
            column(name: "picker_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-298") {
        createIndex(indexName: "FKD3FC6EAB69DB749D", tableName: "product_availability") {
            column(name: "bin_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-299") {
        createIndex(indexName: "FKD3FC6EABAA992CED", tableName: "product_availability") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-300") {
        createIndex(indexName: "FKD3FC6EABDED5FAE7", tableName: "product_availability") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-301") {
        createIndex(indexName: "FKD584C4C4FF77FF9B", tableName: "shipment_workflow") {
            column(name: "shipment_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-302") {
        createIndex(indexName: "FKD790DEBD154F600", tableName: "reference_number") {
            column(name: "reference_number_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-303") {
        createIndex(indexName: "FKDA3BB2981CD3412D", tableName: "shipment_item") {
            column(name: "requisition_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-304") {
        createIndex(indexName: "FKDA3BB2983B5F6286", tableName: "shipment_item") {
            column(name: "shipment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-305") {
        createIndex(indexName: "FKDA3BB29844979D51", tableName: "shipment_item") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-306") {
        createIndex(indexName: "FKDA3BB29849AB6B52", tableName: "shipment_item") {
            column(name: "donor_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-307") {
        createIndex(indexName: "FKDA3BB29869DB749D", tableName: "shipment_item") {
            column(name: "bin_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-308") {
        createIndex(indexName: "FKDA3BB2987400E88E", tableName: "shipment_item") {
            column(name: "container_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-309") {
        createIndex(indexName: "FKDA3BB298AA992CED", tableName: "shipment_item") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-310") {
        createIndex(indexName: "FKDA3BB298DED5FAE7", tableName: "shipment_item") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-311") {
        createIndex(indexName: "FKDEF5AD1317A6E251", tableName: "shipment_workflow_container_type") {
            column(name: "container_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-312") {
        createIndex(indexName: "FKDF7559D73896C98E", tableName: "shipper_service") {
            column(name: "shipper_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-313") {
        createIndex(indexName: "FKE071DE6DB06EC4FB", tableName: "fulfillment_item_shipment_item") {
            column(name: "shipment_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-314") {
        createIndex(indexName: "FKE071DE6DB42751E1", tableName: "fulfillment_item_shipment_item") {
            column(name: "fulfillment_item_shipment_items_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-315") {
        createIndex(indexName: "FKE139719A1E2B3CDC", tableName: "shipment") {
            column(name: "destination_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-316") {
        createIndex(indexName: "FKE139719A217F5972", tableName: "shipment") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-317") {
        createIndex(indexName: "FKE139719A294C1012", tableName: "shipment") {
            column(name: "carrier_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-318") {
        createIndex(indexName: "FKE139719A426DD105", tableName: "shipment") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-319") {
        createIndex(indexName: "FKE139719A44979D51", tableName: "shipment") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-320") {
        createIndex(indexName: "FKE139719A49AB6B52", tableName: "shipment") {
            column(name: "donor_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-321") {
        createIndex(indexName: "FKE139719A5DE9E374", tableName: "shipment") {
            column(name: "requisition_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-322") {
        createIndex(indexName: "FKE139719AA28CC5FB", tableName: "shipment") {
            column(name: "shipment_method_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-323") {
        createIndex(indexName: "FKE139719AD95ACF25", tableName: "shipment") {
            column(name: "current_event_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-324") {
        createIndex(indexName: "FKE139719ADBDEDAC4", tableName: "shipment") {
            column(name: "origin_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-325") {
        createIndex(indexName: "FKE139719AFF77FF9B", tableName: "shipment") {
            column(name: "shipment_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-326") {
        createIndex(indexName: "FKE698D2ECC800AA15", tableName: "order_document") {
            column(name: "document_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-327") {
        createIndex(indexName: "FKE698D2ECFE10118D", tableName: "order_document") {
            column(name: "order_documents_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-328") {
        createIndex(indexName: "FKE7584B1369DB749D", tableName: "picklist_item") {
            column(name: "bin_location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-329") {
        createIndex(indexName: "FKE7814C8117A6E251", tableName: "container") {
            column(name: "container_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-330") {
        createIndex(indexName: "FKE7814C813B5F6286", tableName: "container") {
            column(name: "shipment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-331") {
        createIndex(indexName: "FKE7814C8144979D51", tableName: "container") {
            column(name: "recipient_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-332") {
        createIndex(indexName: "FKE7814C814B6A2E03", tableName: "container") {
            column(name: "parent_container_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-333") {
        createIndex(indexName: "FKED441931C8653BC0", tableName: "product_association") {
            column(name: "associated_product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-334") {
        createIndex(indexName: "FKED441931DED5FAE7", tableName: "product_association") {
            column(name: "product_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-335") {
        createIndex(indexName: "FKED8DCCEF217F5972", tableName: "product") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-336") {
        createIndex(indexName: "FKED8DCCEF426DD105", tableName: "product") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-337") {
        createIndex(indexName: "FKED8DCCEFABD88AC6", tableName: "product") {
            column(name: "product_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-338") {
        createIndex(indexName: "FKED8DCCEFEEB2908D", tableName: "product") {
            column(name: "default_uom_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-339") {
        createIndex(indexName: "FKED8DCCEFEF4C770D", tableName: "product") {
            column(name: "category_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-340") {
        createIndex(indexName: "FKEDC55CD447EBE106", tableName: "fulfillment_item") {
            column(name: "requisition_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-341") {
        createIndex(indexName: "FKEDC55CD494567276", tableName: "fulfillment_item") {
            column(name: "fulfillment_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-342") {
        createIndex(indexName: "FKEDC55CD4AA992CED", tableName: "fulfillment_item") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-343") {
        createIndex(indexName: "FKF58372688ABEBD5", tableName: "location_supported_activities") {
            column(name: "location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-344") {
        createIndex(indexName: "FKFD8E50671A43AB29", tableName: "click_stream_request") {
            column(name: "click_stream_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-345") {
        createIndex(indexName: "fk_budget_code_created_by", tableName: "budget_code") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-346") {
        createIndex(indexName: "fk_budget_code_organization", tableName: "budget_code") {
            column(name: "organization_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-347") {
        createIndex(indexName: "fk_budget_code_updated_by", tableName: "budget_code") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-348") {
        createIndex(indexName: "fk_category_gl_account", tableName: "category") {
            column(name: "gl_account_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-349") {
        createIndex(indexName: "fk_gl_account_created_by", tableName: "gl_account") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-350") {
        createIndex(indexName: "fk_gl_account_gl_account_type", tableName: "gl_account") {
            column(name: "gl_account_type_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-351") {
        createIndex(indexName: "fk_gl_account_type_created_by", tableName: "gl_account_type") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-352") {
        createIndex(indexName: "fk_gl_account_type_updated_by", tableName: "gl_account_type") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-353") {
        createIndex(indexName: "fk_gl_account_updated_by", tableName: "gl_account") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-354") {
        createIndex(indexName: "fk_order_adjustment_budget_code", tableName: "order_adjustment") {
            column(name: "budget_code_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-355") {
        createIndex(indexName: "fk_order_adjustment_gl_account", tableName: "order_adjustment") {
            column(name: "gl_account_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-356") {
        createIndex(indexName: "fk_order_adjustment_type_gl_account", tableName: "order_adjustment_type") {
            column(name: "gl_account_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-357") {
        createIndex(indexName: "fk_order_item_budget_code", tableName: "order_item") {
            column(name: "budget_code_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-358") {
        createIndex(indexName: "fk_order_item_gl_account", tableName: "order_item") {
            column(name: "gl_account_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-359") {
        createIndex(indexName: "fk_product_gl_account", tableName: "product") {
            column(name: "gl_account_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-360") {
        createIndex(indexName: "inventory_snapshot_date_idx", tableName: "inventory_snapshot") {
            column(name: "date")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-361") {
        createIndex(indexName: "inventory_snapshot_last_updated_idx", tableName: "inventory_snapshot") {
            column(name: "location_id")

            column(name: "last_updated")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-362") {
        createIndex(indexName: "location_role_ibfk_1", tableName: "location_role") {
            column(name: "user_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-363") {
        createIndex(indexName: "location_role_ibfk_2", tableName: "location_role") {
            column(name: "role_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-364") {
        createIndex(indexName: "location_role_ibfk_3", tableName: "location_role") {
            column(name: "location_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-365") {
        createIndex(indexName: "picklist_item_ibfk_1", tableName: "picklist_item") {
            column(name: "requisition_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-366") {
        createIndex(indexName: "picklist_item_ibfk_2", tableName: "picklist_item") {
            column(name: "picklist_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-367") {
        createIndex(indexName: "picklist_item_ibfk_3", tableName: "picklist_item") {
            column(name: "created_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-368") {
        createIndex(indexName: "picklist_item_ibfk_4", tableName: "picklist_item") {
            column(name: "updated_by_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-369") {
        createIndex(indexName: "picklist_item_ibfk_5", tableName: "picklist_item") {
            column(name: "inventory_item_id")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-370") {
        createIndex(indexName: "product_id", tableName: "product_demand_details") {
            column(name: "product_id")

            column(name: "origin_id")

            column(name: "destination_id")

            column(name: "date_issued")

            column(name: "date_requested")
        }
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-371") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "location_dimension", constraintName: "FK1143A95C8ABEBD5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-372") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_role", constraintName: "FK143BF46AA462C195", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-373") {
        addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "user_role", constraintName: "FK143BF46AFF37FDB5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "role", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-374") {
        addForeignKeyConstraint(baseColumnNames: "verified_by_id", baseTableName: "requisition", constraintName: "FK1799509C20E33E1C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-375") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "requisition", constraintName: "FK1799509C217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-376") {
        addForeignKeyConstraint(baseColumnNames: "requisition_template_id", baseTableName: "requisition", constraintName: "FK1799509C2BDD17B3", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "requisition", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-377") {
        addForeignKeyConstraint(baseColumnNames: "received_by_id", baseTableName: "requisition", constraintName: "FK1799509C36C69275", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-378") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "requisition", constraintName: "FK1799509C426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-379") {
        addForeignKeyConstraint(baseColumnNames: "delivered_by_id", baseTableName: "requisition", constraintName: "FK1799509C4CF042D8", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-380") {
        addForeignKeyConstraint(baseColumnNames: "issued_by_id", baseTableName: "requisition", constraintName: "FK1799509CD196DBBF", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-381") {
        addForeignKeyConstraint(baseColumnNames: "checked_by_id", baseTableName: "requisition", constraintName: "FK1799509CD2CB8BBB", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-382") {
        addForeignKeyConstraint(baseColumnNames: "reviewed_by_id", baseTableName: "requisition", constraintName: "FK1799509CDFA74E0B", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-383") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_catalog_item", constraintName: "FK187E54C9DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-384") {
        addForeignKeyConstraint(baseColumnNames: "product_catalog_id", baseTableName: "product_catalog_item", constraintName: "FK187E54C9FB5E604E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product_catalog", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-385") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "tag", constraintName: "FK1BF9A217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-386") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "tag", constraintName: "FK1BF9A426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-387") {
        addForeignKeyConstraint(baseColumnNames: "party_id", baseTableName: "party_role", constraintName: "FK1C92FE2F3E67CF9F", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-388") {
        addForeignKeyConstraint(baseColumnNames: "product_key_id", baseTableName: "transaction_fact", constraintName: "FK1E50D72D72882836", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-389") {
        addForeignKeyConstraint(baseColumnNames: "location_key_id", baseTableName: "transaction_fact", constraintName: "FK1E50D72DA27827C2", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-390") {
        addForeignKeyConstraint(baseColumnNames: "transaction_date_key_id", baseTableName: "transaction_fact", constraintName: "FK1E50D72DCA32CFEF", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "date_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-391") {
        addForeignKeyConstraint(baseColumnNames: "transaction_type_key_id", baseTableName: "transaction_fact", constraintName: "FK1E50D72DCA354381", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "transaction_type_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-392") {
        addForeignKeyConstraint(baseColumnNames: "lot_key_id", baseTableName: "transaction_fact", constraintName: "FK1E50D72DD1F27172", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "lot_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-393") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "synonym", constraintName: "FK299E50ABDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-394") {
        addForeignKeyConstraint(baseColumnNames: "origin_bin_location_id", baseTableName: "order_item", constraintName: "FK2D110D6418D76D84", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-395") {
        addForeignKeyConstraint(baseColumnNames: "product_supplier_id", baseTableName: "order_item", constraintName: "FK2D110D6429542386", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product_supplier", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-396") {
        addForeignKeyConstraint(baseColumnNames: "product_package_id", baseTableName: "order_item", constraintName: "FK2D110D6429B2552E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product_package", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-397") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "order_item", constraintName: "FK2D110D6444979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-398") {
        addForeignKeyConstraint(baseColumnNames: "parent_order_item_id", baseTableName: "order_item", constraintName: "FK2D110D6451A9416E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "order_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-399") {
        addForeignKeyConstraint(baseColumnNames: "quantity_uom_id", baseTableName: "order_item", constraintName: "FK2D110D645ED93B03", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-400") {
        addForeignKeyConstraint(baseColumnNames: "destination_bin_location_id", baseTableName: "order_item", constraintName: "FK2D110D64605326C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-401") {
        addForeignKeyConstraint(baseColumnNames: "requested_by_id", baseTableName: "order_item", constraintName: "FK2D110D64911E7578", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-402") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "order_item", constraintName: "FK2D110D64AA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-403") {
        addForeignKeyConstraint(baseColumnNames: "order_id", baseTableName: "order_item", constraintName: "FK2D110D64D08EDBE6", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "order", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-404") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "order_item", constraintName: "FK2D110D64DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-405") {
        addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "order_item", constraintName: "FK2D110D64EF4C770D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "category", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-406") {
        addForeignKeyConstraint(baseColumnNames: "order_comments_id", baseTableName: "order_comment", constraintName: "FK2DE9EE6EB8839C0F", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "order", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-407") {
        addForeignKeyConstraint(baseColumnNames: "comment_id", baseTableName: "order_comment", constraintName: "FK2DE9EE6EC4A49BBF", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "comment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-408") {
        addForeignKeyConstraint(baseColumnNames: "from_unit_of_measure_id", baseTableName: "unit_of_measure_conversion", constraintName: "FK2E4511844A3E746", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-409") {
        addForeignKeyConstraint(baseColumnNames: "to_unit_of_measure_id", baseTableName: "unit_of_measure_conversion", constraintName: "FK2E4511849B9434D5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-410") {
        addForeignKeyConstraint(baseColumnNames: "parent_category_id", baseTableName: "category", constraintName: "FK302BCFE619A2EF8", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "category", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-411") {
        addForeignKeyConstraint(baseColumnNames: "reference_number_id", baseTableName: "shipment_reference_number", constraintName: "FK312F6C292388BC5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "reference_number", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-412") {
        addForeignKeyConstraint(baseColumnNames: "product_group_id", baseTableName: "product_group_product", constraintName: "FK313A4BDF14F7BB8E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product_group", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-413") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_group_product", constraintName: "FK313A4BDFDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-414") {
        addForeignKeyConstraint(baseColumnNames: "document_type_id", baseTableName: "document", constraintName: "FK335CD11B6631D8CC", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "document_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-415") {
        addForeignKeyConstraint(baseColumnNames: "warehouse_id", baseTableName: "user", constraintName: "FK36EBCB1F28CE07", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-416") {
        addForeignKeyConstraint(baseColumnNames: "manager_id", baseTableName: "user", constraintName: "FK36EBCB41E07A73", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-417") {
        addForeignKeyConstraint(baseColumnNames: "sender_id", baseTableName: "comment", constraintName: "FK38A5EE5FAF1302EB", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-418") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "comment", constraintName: "FK38A5EE5FF885F087", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-419") {
        addForeignKeyConstraint(baseColumnNames: "unit_of_measure_id", baseTableName: "product_supplier", constraintName: "FK3A097B1C24DEBC91", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-420") {
        addForeignKeyConstraint(baseColumnNames: "manufacturer_id", baseTableName: "product_supplier", constraintName: "FK3A097B1C2A475A37", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-421") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_supplier", constraintName: "FK3A097B1CDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-422") {
        addForeignKeyConstraint(baseColumnNames: "supplier_id", baseTableName: "product_supplier", constraintName: "FK3A097B1CF42F7E5C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-423") {
        addForeignKeyConstraint(baseColumnNames: "shipper_service_id", baseTableName: "shipment_method", constraintName: "FK40203B26296B2CA3", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipper_service", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-424") {
        addForeignKeyConstraint(baseColumnNames: "shipper_id", baseTableName: "shipment_method", constraintName: "FK40203B263896C98E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipper", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-425") {
        addForeignKeyConstraint(baseColumnNames: "shipment_id", baseTableName: "receipt", constraintName: "FK408272383B5F6286", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-426") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "receipt", constraintName: "FK4082723844979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-427") {
        addForeignKeyConstraint(baseColumnNames: "destination_id", baseTableName: "requisition", constraintName: "FK414EF28F1E2B3CDC", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-428") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "requisition", constraintName: "FK414EF28F44979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-429") {
        addForeignKeyConstraint(baseColumnNames: "fulfillment_id", baseTableName: "requisition", constraintName: "FK414EF28F94567276", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "fulfillment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-430") {
        addForeignKeyConstraint(baseColumnNames: "origin_id", baseTableName: "requisition", constraintName: "FK414EF28FDBDEDAC4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-431") {
        addForeignKeyConstraint(baseColumnNames: "requested_by_id", baseTableName: "requisition", constraintName: "FK414EF28FDD302242", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-432") {
        addForeignKeyConstraint(baseColumnNames: "order_item_id", baseTableName: "order_adjustment", constraintName: "FK4A1ABEFE3BE9D843", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "order_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-433") {
        addForeignKeyConstraint(baseColumnNames: "order_id", baseTableName: "order_adjustment", constraintName: "FK4A1ABEFED08EDBE6", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "order", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-434") {
        addForeignKeyConstraint(baseColumnNames: "order_adjustment_type_id", baseTableName: "order_adjustment", constraintName: "FK4A1ABEFEE1A39520", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "order_adjustment_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-435") {
        addForeignKeyConstraint(baseColumnNames: "reference_number_type_id", baseTableName: "shipment_workflow_reference_number_type", constraintName: "FK4BB27241154F600", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "reference_number_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-436") {
        addForeignKeyConstraint(baseColumnNames: "requisition_id", baseTableName: "requisition_item", constraintName: "FK4DA982C35DE21C87", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "requisition", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-437") {
        addForeignKeyConstraint(baseColumnNames: "requested_by_id", baseTableName: "requisition_item", constraintName: "FK4DA982C3911E7578", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-438") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "requisition_item", constraintName: "FK4DA982C3AA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-439") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "requisition_item", constraintName: "FK4DA982C3DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-440") {
        addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "requisition_item", constraintName: "FK4DA982C3EF4C770D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "category", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-441") {
        addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "product_group", constraintName: "FK51F3772FEF4C770D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "category", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-442") {
        addForeignKeyConstraint(baseColumnNames: "product_group_id", baseTableName: "requisition_item", constraintName: "FK5358E4D614F7BB8E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product_group", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-443") {
        addForeignKeyConstraint(baseColumnNames: "substitution_item_id", baseTableName: "requisition_item", constraintName: "FK5358E4D61594028E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "requisition_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-444") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "requisition_item", constraintName: "FK5358E4D6217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-445") {
        addForeignKeyConstraint(baseColumnNames: "product_package_id", baseTableName: "requisition_item", constraintName: "FK5358E4D629B2552E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product_package", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-446") {
        addForeignKeyConstraint(baseColumnNames: "modification_item_id", baseTableName: "requisition_item", constraintName: "FK5358E4D6405AC22D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "requisition_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-447") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "requisition_item", constraintName: "FK5358E4D6426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-448") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "requisition_item", constraintName: "FK5358E4D644979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-449") {
        addForeignKeyConstraint(baseColumnNames: "requested_by_id", baseTableName: "requisition_item", constraintName: "FK5358E4D6DD302242", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-450") {
        addForeignKeyConstraint(baseColumnNames: "parent_requisition_item_id", baseTableName: "requisition_item", constraintName: "FK5358E4D6F84BDE18", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "requisition_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-451") {
        addForeignKeyConstraint(baseColumnNames: "fulfilled_by_id", baseTableName: "fulfillment", constraintName: "FK5A2551DEAC392B33", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-452") {
        addForeignKeyConstraint(baseColumnNames: "event_type_id", baseTableName: "event", constraintName: "FK5C6729A3D970DB4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "event_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-453") {
        addForeignKeyConstraint(baseColumnNames: "event_location_id", baseTableName: "event", constraintName: "FK5C6729A4415A5B0", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-454") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "unit_of_measure_class", constraintName: "FK5D1B504A217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-455") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "unit_of_measure_class", constraintName: "FK5D1B504A426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-456") {
        addForeignKeyConstraint(baseColumnNames: "base_uom_id", baseTableName: "unit_of_measure_class", constraintName: "FK5D1B504A6B9DFD", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-457") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "product_package", constraintName: "FK615A48F6217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-458") {
        addForeignKeyConstraint(baseColumnNames: "product_supplier_id", baseTableName: "product_package", constraintName: "FK615A48F629542386", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product_supplier", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-459") {
        addForeignKeyConstraint(baseColumnNames: "uom_id", baseTableName: "product_package", constraintName: "FK615A48F63906C4CF", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-460") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "product_package", constraintName: "FK615A48F6426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-461") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_package", constraintName: "FK615A48F6DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-462") {
        addForeignKeyConstraint(baseColumnNames: "destination_id", baseTableName: "order", constraintName: "FK651874E1E2B3CDC", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-463") {
        addForeignKeyConstraint(baseColumnNames: "approved_by_id", baseTableName: "order", constraintName: "FK651874E240896CB", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-464") {
        addForeignKeyConstraint(baseColumnNames: "destination_party_id", baseTableName: "order", constraintName: "FK651874E35D76CB0", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-465") {
        addForeignKeyConstraint(baseColumnNames: "completed_by_id", baseTableName: "order", constraintName: "FK651874E41B7275F", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-466") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "order", constraintName: "FK651874E44979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-467") {
        addForeignKeyConstraint(baseColumnNames: "payment_method_type_id", baseTableName: "order", constraintName: "FK651874E6A8010C1", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "payment_method_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-468") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "order", constraintName: "FK651874E6D91063C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-469") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "order", constraintName: "FK651874E8E7F7DCF", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-470") {
        addForeignKeyConstraint(baseColumnNames: "payment_term_id", baseTableName: "order", constraintName: "FK651874E9E52B00C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "payment_term", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-471") {
        addForeignKeyConstraint(baseColumnNames: "ordered_by_id", baseTableName: "order", constraintName: "FK651874EAF6D8801", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-472") {
        addForeignKeyConstraint(baseColumnNames: "origin_id", baseTableName: "order", constraintName: "FK651874EDBDEDAC4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-473") {
        addForeignKeyConstraint(baseColumnNames: "party_type_id", baseTableName: "party", constraintName: "FK6581AE69DFE4C4C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "party_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-474") {
        addForeignKeyConstraint(baseColumnNames: "default_location_id", baseTableName: "party", constraintName: "FK6581AE6D1DFC6D7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-475") {
        addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "shipment_document", constraintName: "FK6C5BE20C800AA15", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "document", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-476") {
        addForeignKeyConstraint(baseColumnNames: "shipment_events_id", baseTableName: "shipment_event", constraintName: "FK6D032BB53B350242", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-477") {
        addForeignKeyConstraint(baseColumnNames: "event_id", baseTableName: "shipment_event", constraintName: "FK6D032BB5786431F", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "event", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-478") {
        addForeignKeyConstraint(baseColumnNames: "location_type_id", baseTableName: "location", constraintName: "FK714F9FB528F75F00", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-479") {
        addForeignKeyConstraint(baseColumnNames: "location_group_id", baseTableName: "location", constraintName: "FK714F9FB53BB36E94", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location_group", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-480") {
        addForeignKeyConstraint(baseColumnNames: "manager_id", baseTableName: "location", constraintName: "FK714F9FB541E07A73", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-481") {
        addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "location", constraintName: "FK714F9FB5606C7D95", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-482") {
        addForeignKeyConstraint(baseColumnNames: "address_id", baseTableName: "location", constraintName: "FK714F9FB561ED379F", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "address", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-483") {
        addForeignKeyConstraint(baseColumnNames: "inventory_id", baseTableName: "location", constraintName: "FK714F9FB572A2C5B4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "inventory", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-484") {
        addForeignKeyConstraint(baseColumnNames: "parent_location_id", baseTableName: "location", constraintName: "FK714F9FB57AF9A3C0", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-485") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "unit_of_measure", constraintName: "FK7348B491217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-486") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "unit_of_measure", constraintName: "FK7348B491426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-487") {
        addForeignKeyConstraint(baseColumnNames: "uom_class_id", baseTableName: "unit_of_measure", constraintName: "FK7348B49197D8303E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "unit_of_measure_class", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-488") {
        addForeignKeyConstraint(baseColumnNames: "bin_location_id", baseTableName: "inventory_snapshot", constraintName: "FK740B54769DB749D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-489") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "inventory_snapshot", constraintName: "FK740B5478ABEBD5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-490") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "inventory_snapshot", constraintName: "FK740B547AA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-491") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "inventory_snapshot", constraintName: "FK740B547DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-492") {
        addForeignKeyConstraint(baseColumnNames: "order_events_id", baseTableName: "order_event", constraintName: "FK74D92A693D2E628A", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "order", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-493") {
        addForeignKeyConstraint(baseColumnNames: "event_id", baseTableName: "order_event", constraintName: "FK74D92A69786431F", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "event", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-494") {
        addForeignKeyConstraint(baseColumnNames: "destination_transaction_id", baseTableName: "local_transfer", constraintName: "FK7975323F4CC49445", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "transaction", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-495") {
        addForeignKeyConstraint(baseColumnNames: "source_transaction_id", baseTableName: "local_transfer", constraintName: "FK7975323F57563498", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "transaction", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-496") {
        addForeignKeyConstraint(baseColumnNames: "address_id", baseTableName: "location_group", constraintName: "FK7A19D7561ED379F", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "address", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-497") {
        addForeignKeyConstraint(baseColumnNames: "location_type_id", baseTableName: "location_type_supported_activities", constraintName: "FK7AFF67F928F75F00", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-498") {
        addForeignKeyConstraint(baseColumnNames: "destination_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE1E2B3CDC", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-499") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-500") {
        addForeignKeyConstraint(baseColumnNames: "confirmed_by_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE3265A8A9", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-501") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-502") {
        addForeignKeyConstraint(baseColumnNames: "requisition_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE5DE9E374", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "requisition", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-503") {
        addForeignKeyConstraint(baseColumnNames: "incoming_shipment_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE5F12AFED", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-504") {
        addForeignKeyConstraint(baseColumnNames: "inventory_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE72A2C5B4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "inventory", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-505") {
        addForeignKeyConstraint(baseColumnNames: "source_id", baseTableName: "transaction", constraintName: "FK7FA0D2DE828481AF", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-506") {
        addForeignKeyConstraint(baseColumnNames: "transaction_type_id", baseTableName: "transaction", constraintName: "FK7FA0D2DEB3FB7111", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "transaction_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-507") {
        addForeignKeyConstraint(baseColumnNames: "outgoing_shipment_id", baseTableName: "transaction", constraintName: "FK7FA0D2DEB80B3233", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-508") {
        addForeignKeyConstraint(baseColumnNames: "order_id", baseTableName: "transaction", constraintName: "FK7FA0D2DED08EDBE6", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "order", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-509") {
        addForeignKeyConstraint(baseColumnNames: "receipt_id", baseTableName: "transaction", constraintName: "FK7FA0D2DEF7076438", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "receipt", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-510") {
        addForeignKeyConstraint(baseColumnNames: "order_item_id", baseTableName: "order_shipment", constraintName: "FK9475736B3BE9D843", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "order_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-511") {
        addForeignKeyConstraint(baseColumnNames: "shipment_item_id", baseTableName: "order_shipment", constraintName: "FK9475736BB06EC4FB", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipment_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-512") {
        addForeignKeyConstraint(baseColumnNames: "unit_of_measure_id", baseTableName: "product_attribute", constraintName: "FK94A534C24DEBC91", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-513") {
        addForeignKeyConstraint(baseColumnNames: "attribute_id", baseTableName: "product_attribute", constraintName: "FK94A534C47B0D087", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "attribute", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-514") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_attribute", constraintName: "FK94A534CDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-515") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "synonym", constraintName: "FK98293BFB217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-516") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "synonym", constraintName: "FK98293BFB426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-517") {
        addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "shipment_workflow_document", constraintName: "FK9A945A36C800AA15", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "document", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-518") {
        addForeignKeyConstraint(baseColumnNames: "shipment_workflow_id", baseTableName: "shipment_workflow_document", constraintName: "FK9A945A36EC587CFB", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipment_workflow", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-519") {
        addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "product_category", constraintName: "FKA0303E4EEF4C770D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "category", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-520") {
        addForeignKeyConstraint(baseColumnNames: "tag_id", baseTableName: "product_tag", constraintName: "FKA71CAC4A9740C85F", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "tag", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-521") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_tag", constraintName: "FKA71CAC4ADED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-522") {
        addForeignKeyConstraint(baseColumnNames: "product_key_id", baseTableName: "consumption_fact", constraintName: "FKA8B7A49072882836", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-523") {
        addForeignKeyConstraint(baseColumnNames: "location_key_id", baseTableName: "consumption_fact", constraintName: "FKA8B7A490A27827C2", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-524") {
        addForeignKeyConstraint(baseColumnNames: "transaction_date_key_id", baseTableName: "consumption_fact", constraintName: "FKA8B7A490CA32CFEF", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "date_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-525") {
        addForeignKeyConstraint(baseColumnNames: "lot_key_id", baseTableName: "consumption_fact", constraintName: "FKA8B7A490D1F27172", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "lot_dimension", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-526") {
        addForeignKeyConstraint(baseColumnNames: "transaction_id", baseTableName: "transaction_entry", constraintName: "FKABC21FD12EF4C7F4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "transaction", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-527") {
        addForeignKeyConstraint(baseColumnNames: "bin_location_id", baseTableName: "transaction_entry", constraintName: "FKABC21FD169DB749D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-528") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "transaction_entry", constraintName: "FKABC21FD1AA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-529") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "transaction_entry", constraintName: "FKABC21FD1DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-530") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "receipt_item", constraintName: "FKAE3064BA44979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-531") {
        addForeignKeyConstraint(baseColumnNames: "bin_location_id", baseTableName: "receipt_item", constraintName: "FKAE3064BA69DB749D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-532") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "receipt_item", constraintName: "FKAE3064BAAA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-533") {
        addForeignKeyConstraint(baseColumnNames: "shipment_item_id", baseTableName: "receipt_item", constraintName: "FKAE3064BAB06EC4FB", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipment_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-534") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "receipt_item", constraintName: "FKAE3064BADED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-535") {
        addForeignKeyConstraint(baseColumnNames: "receipt_id", baseTableName: "receipt_item", constraintName: "FKAE3064BAF7076438", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "receipt", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-536") {
        addForeignKeyConstraint(baseColumnNames: "component_product_id", baseTableName: "product_component", constraintName: "FKB511C5AD20E351EA", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-537") {
        addForeignKeyConstraint(baseColumnNames: "unit_of_measure_id", baseTableName: "product_component", constraintName: "FKB511C5AD24DEBC91", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-538") {
        addForeignKeyConstraint(baseColumnNames: "assembly_product_id", baseTableName: "product_component", constraintName: "FKB511C5ADFB4C199C", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-539") {
        addForeignKeyConstraint(baseColumnNames: "inventory_id", baseTableName: "inventory_level", constraintName: "FKC254A2E172A2C5B4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "inventory", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-540") {
        addForeignKeyConstraint(baseColumnNames: "preferred_bin_location_id", baseTableName: "inventory_level", constraintName: "FKC254A2E1CFDCB4DF", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-541") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "inventory_level", constraintName: "FKC254A2E1DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-542") {
        addForeignKeyConstraint(baseColumnNames: "replenishment_location_id", baseTableName: "inventory_level", constraintName: "FKC254A2E1F07D879A", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-543") {
        addForeignKeyConstraint(baseColumnNames: "comment_id", baseTableName: "shipment_comment", constraintName: "FKC398CCBAC4A49BBF", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "comment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-544") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_dimension", constraintName: "FKC73E1616DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-545") {
        addForeignKeyConstraint(baseColumnNames: "unit_of_measure_class_id", baseTableName: "attribute", constraintName: "FKC7AA9C4013CE80", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "unit_of_measure_class", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-546") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "consumption", constraintName: "FKCD71F39B8ABEBD5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-547") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "consumption", constraintName: "FKCD71F39BAA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-548") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "consumption", constraintName: "FKCD71F39BDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-549") {
        addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "product_document", constraintName: "FKD08A526BC800AA15", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "document", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-550") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_document", constraintName: "FKD08A526BDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-551") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "picklist", constraintName: "FKD3F8383F217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-552") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "picklist", constraintName: "FKD3F8383F426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-553") {
        addForeignKeyConstraint(baseColumnNames: "requisition_id", baseTableName: "picklist", constraintName: "FKD3F8383F5DE9E374", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "requisition", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-554") {
        addForeignKeyConstraint(baseColumnNames: "picker_id", baseTableName: "picklist", constraintName: "FKD3F8383FA3E976BC", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-555") {
        addForeignKeyConstraint(baseColumnNames: "bin_location_id", baseTableName: "product_availability", constraintName: "FKD3FC6EAB69DB749D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-556") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "product_availability", constraintName: "FKD3FC6EAB8ABEBD5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-557") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "product_availability", constraintName: "FKD3FC6EABAA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-558") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_availability", constraintName: "FKD3FC6EABDED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-559") {
        addForeignKeyConstraint(baseColumnNames: "shipment_type_id", baseTableName: "shipment_workflow", constraintName: "FKD584C4C4FF77FF9B", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipment_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-560") {
        addForeignKeyConstraint(baseColumnNames: "reference_number_type_id", baseTableName: "reference_number", constraintName: "FKD790DEBD154F600", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "reference_number_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-561") {
        addForeignKeyConstraint(baseColumnNames: "requisition_item_id", baseTableName: "shipment_item", constraintName: "FKDA3BB2981CD3412D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "requisition_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-562") {
        addForeignKeyConstraint(baseColumnNames: "shipment_id", baseTableName: "shipment_item", constraintName: "FKDA3BB2983B5F6286", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-563") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "shipment_item", constraintName: "FKDA3BB29844979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-564") {
        addForeignKeyConstraint(baseColumnNames: "donor_id", baseTableName: "shipment_item", constraintName: "FKDA3BB29849AB6B52", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "donor", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-565") {
        addForeignKeyConstraint(baseColumnNames: "bin_location_id", baseTableName: "shipment_item", constraintName: "FKDA3BB29869DB749D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-566") {
        addForeignKeyConstraint(baseColumnNames: "container_id", baseTableName: "shipment_item", constraintName: "FKDA3BB2987400E88E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "container", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-567") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "shipment_item", constraintName: "FKDA3BB298AA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-568") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "shipment_item", constraintName: "FKDA3BB298DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-569") {
        addForeignKeyConstraint(baseColumnNames: "container_type_id", baseTableName: "shipment_workflow_container_type", constraintName: "FKDEF5AD1317A6E251", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "container_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-570") {
        addForeignKeyConstraint(baseColumnNames: "shipper_id", baseTableName: "shipper_service", constraintName: "FKDF7559D73896C98E", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipper", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-571") {
        addForeignKeyConstraint(baseColumnNames: "shipment_item_id", baseTableName: "fulfillment_item_shipment_item", constraintName: "FKE071DE6DB06EC4FB", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipment_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-572") {
        addForeignKeyConstraint(baseColumnNames: "fulfillment_item_shipment_items_id", baseTableName: "fulfillment_item_shipment_item", constraintName: "FKE071DE6DB42751E1", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "fulfillment_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-573") {
        addForeignKeyConstraint(baseColumnNames: "destination_id", baseTableName: "shipment", constraintName: "FKE139719A1E2B3CDC", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-574") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "shipment", constraintName: "FKE139719A217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-575") {
        addForeignKeyConstraint(baseColumnNames: "carrier_id", baseTableName: "shipment", constraintName: "FKE139719A294C1012", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-576") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "shipment", constraintName: "FKE139719A426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-577") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "shipment", constraintName: "FKE139719A44979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-578") {
        addForeignKeyConstraint(baseColumnNames: "donor_id", baseTableName: "shipment", constraintName: "FKE139719A49AB6B52", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "donor", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-579") {
        addForeignKeyConstraint(baseColumnNames: "requisition_id", baseTableName: "shipment", constraintName: "FKE139719A5DE9E374", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "requisition", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-580") {
        addForeignKeyConstraint(baseColumnNames: "shipment_method_id", baseTableName: "shipment", constraintName: "FKE139719AA28CC5FB", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipment_method", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-581") {
        addForeignKeyConstraint(baseColumnNames: "current_event_id", baseTableName: "shipment", constraintName: "FKE139719AD95ACF25", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "event", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-582") {
        addForeignKeyConstraint(baseColumnNames: "origin_id", baseTableName: "shipment", constraintName: "FKE139719ADBDEDAC4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-583") {
        addForeignKeyConstraint(baseColumnNames: "shipment_type_id", baseTableName: "shipment", constraintName: "FKE139719AFF77FF9B", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipment_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-584") {
        addForeignKeyConstraint(baseColumnNames: "document_id", baseTableName: "order_document", constraintName: "FKE698D2ECC800AA15", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "document", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-585") {
        addForeignKeyConstraint(baseColumnNames: "order_documents_id", baseTableName: "order_document", constraintName: "FKE698D2ECFE10118D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "order", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-586") {
        addForeignKeyConstraint(baseColumnNames: "bin_location_id", baseTableName: "picklist_item", constraintName: "FKE7584B1369DB749D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-587") {
        addForeignKeyConstraint(baseColumnNames: "container_type_id", baseTableName: "container", constraintName: "FKE7814C8117A6E251", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "container_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-588") {
        addForeignKeyConstraint(baseColumnNames: "shipment_id", baseTableName: "container", constraintName: "FKE7814C813B5F6286", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "shipment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-589") {
        addForeignKeyConstraint(baseColumnNames: "recipient_id", baseTableName: "container", constraintName: "FKE7814C8144979D51", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "person", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-590") {
        addForeignKeyConstraint(baseColumnNames: "parent_container_id", baseTableName: "container", constraintName: "FKE7814C814B6A2E03", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "container", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-591") {
        addForeignKeyConstraint(baseColumnNames: "associated_product_id", baseTableName: "product_association", constraintName: "FKED441931C8653BC0", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-592") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_association", constraintName: "FKED441931DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-593") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "product", constraintName: "FKED8DCCEF217F5972", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-594") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "product", constraintName: "FKED8DCCEF426DD105", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-595") {
        addForeignKeyConstraint(baseColumnNames: "product_type_id", baseTableName: "product", constraintName: "FKED8DCCEFABD88AC6", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-596") {
        addForeignKeyConstraint(baseColumnNames: "default_uom_id", baseTableName: "product", constraintName: "FKED8DCCEFEEB2908D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "unit_of_measure", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-597") {
        addForeignKeyConstraint(baseColumnNames: "category_id", baseTableName: "product", constraintName: "FKED8DCCEFEF4C770D", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "category", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-598") {
        addForeignKeyConstraint(baseColumnNames: "requisition_item_id", baseTableName: "fulfillment_item", constraintName: "FKEDC55CD447EBE106", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "requisition_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-599") {
        addForeignKeyConstraint(baseColumnNames: "fulfillment_id", baseTableName: "fulfillment_item", constraintName: "FKEDC55CD494567276", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "fulfillment", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-600") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "fulfillment_item", constraintName: "FKEDC55CD4AA992CED", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-601") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "location_supported_activities", constraintName: "FKF58372688ABEBD5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-602") {
        addForeignKeyConstraint(baseColumnNames: "click_stream_id", baseTableName: "click_stream_request", constraintName: "FKFD8E50671A43AB29", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "click_stream", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-603") {
        addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "inventory_item", constraintName: "FKFE019416DED5FAE7", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "product", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-604") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "budget_code", constraintName: "fk_budget_code_created_by", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-605") {
        addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "budget_code", constraintName: "fk_budget_code_organization", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "party", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-606") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "budget_code", constraintName: "fk_budget_code_updated_by", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-607") {
        addForeignKeyConstraint(baseColumnNames: "gl_account_id", baseTableName: "category", constraintName: "fk_category_gl_account", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "gl_account", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-608") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "gl_account", constraintName: "fk_gl_account_created_by", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-609") {
        addForeignKeyConstraint(baseColumnNames: "gl_account_type_id", baseTableName: "gl_account", constraintName: "fk_gl_account_gl_account_type", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "gl_account_type", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-610") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "gl_account_type", constraintName: "fk_gl_account_type_created_by", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-611") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "gl_account_type", constraintName: "fk_gl_account_type_updated_by", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-612") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "gl_account", constraintName: "fk_gl_account_updated_by", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-613") {
        addForeignKeyConstraint(baseColumnNames: "budget_code_id", baseTableName: "order_adjustment", constraintName: "fk_order_adjustment_budget_code", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "budget_code", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-614") {
        addForeignKeyConstraint(baseColumnNames: "gl_account_id", baseTableName: "order_adjustment", constraintName: "fk_order_adjustment_gl_account", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "gl_account", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-615") {
        addForeignKeyConstraint(baseColumnNames: "gl_account_id", baseTableName: "order_adjustment_type", constraintName: "fk_order_adjustment_type_gl_account", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "gl_account", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-616") {
        addForeignKeyConstraint(baseColumnNames: "budget_code_id", baseTableName: "order_item", constraintName: "fk_order_item_budget_code", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "budget_code", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-617") {
        addForeignKeyConstraint(baseColumnNames: "gl_account_id", baseTableName: "order_item", constraintName: "fk_order_item_gl_account", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "gl_account", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-618") {
        addForeignKeyConstraint(baseColumnNames: "gl_account_id", baseTableName: "product", constraintName: "fk_product_gl_account", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "gl_account", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-619") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "location_role", constraintName: "location_role_ibfk_1", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-620") {
        addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "location_role", constraintName: "location_role_ibfk_2", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "role", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-621") {
        addForeignKeyConstraint(baseColumnNames: "location_id", baseTableName: "location_role", constraintName: "location_role_ibfk_3", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "location", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-622") {
        addForeignKeyConstraint(baseColumnNames: "requisition_item_id", baseTableName: "picklist_item", constraintName: "picklist_item_ibfk_1", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "requisition_item", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-623") {
        addForeignKeyConstraint(baseColumnNames: "picklist_id", baseTableName: "picklist_item", constraintName: "picklist_item_ibfk_2", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "picklist", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-624") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "picklist_item", constraintName: "picklist_item_ibfk_3", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-625") {
        addForeignKeyConstraint(baseColumnNames: "updated_by_id", baseTableName: "picklist_item", constraintName: "picklist_item_ibfk_4", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "jmiranda (generated)", id: "1613884772808-626") {
        addForeignKeyConstraint(baseColumnNames: "inventory_item_id", baseTableName: "picklist_item", constraintName: "picklist_item_ibfk_5", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "inventory_item", validate: "true")
    }
}
