# ----- Node Groups ------------------------------------------
insert into sym_node_group (node_group_id, description) values ('root', 'Boston office cloud server');
insert into sym_node_group (node_group_id, description) values ('client', 'Depot / pharmacy');

# ----- Node Group Links -------------------------------------
insert into sym_node_group_link (source_node_group_id, target_node_group_id, data_event_action) values ('client', 'root', 'P');
insert into sym_node_group_link (source_node_group_id, target_node_group_id, data_event_action) values ('root', 'client', 'W');

# ----- Nodes ------------------------------------------------
insert into sym_node (node_id, node_group_id, external_id, sync_enabled) values ('procurement', 'root', 'procurement', 1);
insert into sym_node_identity values ('procurement');

# ----- Routers ----------------------------------------------
insert into sym_router (router_id,source_node_group_id,target_node_group_id,create_time,last_update_time)
values('root_client_router', 'root', 'client', current_timestamp, current_timestamp);
insert into sym_router (router_id,source_node_group_id,target_node_group_id,create_time,last_update_time)
values('client_root_router', 'client', 'root', current_timestamp, current_timestamp);


