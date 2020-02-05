# ----- Triggers - Metadata tables ------------------------------------------
insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('container_type_trigger','container_type','metadata_channel',1,current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('container_type_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('container_type_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('document_type_trigger','document_type','metadata_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('document_type_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('document_type_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('event_type_trigger','event_type','metadata_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('event_type_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('event_type_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('location_group_trigger','location_group','metadata_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('location_group_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('location_group_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('location_type_trigger','location_type','metadata_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('location_type_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('location_type_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('location_type_supported_activities_trigger','location_type_supported_activities','metadata_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('location_type_supported_activities_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('location_type_supported_activities_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('reference_number_type_trigger','reference_number_type','metadata_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('reference_number_type_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('reference_number_type_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('shipment_type_trigger','shipment_type','metadata_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('shipment_type_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('shipment_type_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('transaction_type_trigger','transaction_type','metadata_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('transaction_type_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('transaction_type_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('unit_of_measure_trigger','unit_of_measure','metadata_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('unit_of_measure_type_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('unit_of_measure_type_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('shipper_trigger','shipper','metadata_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('shipper_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('shipper_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('shipper_service_trigger','shipper_service','metadata_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('shipper_service_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('shipper_service_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('shipment_workflow_trigger','shipment_workflow','metadata_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('shipment_workflow_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('shipment_workflow_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('shipment_workflow_container_type_trigger','shipment_workflow_container_type','metadata_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('shipment_workflow_container_type_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('shipment_workflow_container_type_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('shipment_workflow_reference_number_type_trigger','shipment_workflow_reference_number_type','metadata_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('shipment_workflow_reference_number_type_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('shipment_workflow_reference_number_type_trigger','client_root_router', 100, current_timestamp, current_timestamp);


# ----- Triggers - User tables ------------------------------------------
insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('person_trigger','person','person_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('person_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('person_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('role_trigger','role','person_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('role_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('role_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('user_trigger','user','person_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('user_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('user_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('user_role_trigger','user_role','person_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('user_role_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('user_role_trigger','client_root_router', 100, current_timestamp, current_timestamp);


# ----- Triggers - Product tables ------------------------------------------
insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('attribute_trigger','attribute','product_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('attribute_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('attribute_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('attribute_options_trigger','attribute_options','product_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('attribute_options_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('attribute_options_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('category_trigger','category','product_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('category_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('category_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('inventory_level_trigger','inventory_level','product_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('inventory_level_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('inventory_level_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('product_trigger','product','product_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('product_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('product_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('product_attribute_trigger','product_attribute','product_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('product_attribute_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('product_attribute_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('product_category_trigger','product_category','product_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('product_category_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('product_category_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('product_tags_trigger','product_tags','product_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('product_tags_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('product_tags_trigger','client_root_router', 100, current_timestamp, current_timestamp);


# ----- Triggers - System tables ------------------------------------------
insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('address_trigger','address','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('address_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('address_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('comment_trigger','comment','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('comment_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('comment_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('container_trigger','container','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('container_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('container_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('document_trigger','document','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('document_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('document_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('event_trigger','event','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('event_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('event_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('fulfillment_trigger','fulfillment','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('fulfillment_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('fulfillment_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('fulfillment_item_trigger','fulfillment_item','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('fulfillment_item_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('fulfillment_item_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('fulfillment_item_shipment_item_trigger','fulfillment_item_shipment_item','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('fulfillment_item_shipment_item_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('fulfillment_item_shipment_item_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('inventory_trigger','inventory','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('inventory_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('inventory_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('inventory_item_trigger','inventory_item','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('inventory_item_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('inventory_item_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('local_transfer_trigger','local_transfer','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('local_transfer_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('local_transfer_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('location_trigger','location','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('location_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('location_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('location_supported_activities_trigger','location_supported_activities','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('location_supported_activities_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('location_supported_activities_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('order_trigger','order','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('order_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('order_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('order_comment_trigger','order_comment','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('order_comment_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('order_comment_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('order_document_trigger','order_document','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('order_document_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('order_document_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('order_event_trigger','order_event','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('order_event_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('order_event_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('order_item_trigger','order_item','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('order_item_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('order_item_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('order_item_comment_trigger','order_item_comment','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('order_item_comment_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('order_item_comment_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('order_shipment_trigger','order_shipment','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('order_shipment_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('order_shipment_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('receipt_trigger','receipt','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('receipt_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('receipt_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('receipt_item_trigger','receipt_item','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('receipt_item_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('receipt_item_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('reference_number_trigger','reference_number','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('reference_number_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('reference_number_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('request_trigger','request','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('request_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('request_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('request_comment_trigger','request_comment','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('request_comment_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('request_comment_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('request_document_trigger','request_document','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('request_document_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('request_document_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('request_event_trigger','request_event','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('request_event_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('request_event_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('request_item_trigger','request_item','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('request_item_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('request_item_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('request_shipment_trigger','request_shipment','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('request_shipment_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('request_shipment_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('shipment_trigger','shipment','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('shipment_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('shipment_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('shipment_comment_trigger','shipment_comment','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('shipment_comment_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('shipment_comment_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('shipment_document_trigger','shipment_document','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('shipment_document_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('shipment_document_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('shipment_event_trigger','shipment_event','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('shipment_event_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('shipment_event_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('shipment_item_trigger','shipment_item','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('shipment_item_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('shipment_item_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('shipment_method_trigger','shipment_method','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('shipment_method_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('shipment_method_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('shipment_reference_number_trigger','shipment_reference_number','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('shipment_reference_number_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('shipment_reference_number_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('transaction_trigger','transaction','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('transaction_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('transaction_trigger','client_root_router', 100, current_timestamp, current_timestamp);

insert into sym_trigger (trigger_id,source_table_name,channel_id,sync_on_incoming_batch,last_update_time,create_time) values('transaction_entry_trigger','transaction_entry','system_channel', 1, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('transaction_entry_trigger','root_client_router', 100, current_timestamp, current_timestamp);
insert into sym_trigger_router (trigger_id,router_id,initial_load_order,last_update_time,create_time) values('transaction_entry_trigger','client_root_router', 100, current_timestamp, current_timestamp);

