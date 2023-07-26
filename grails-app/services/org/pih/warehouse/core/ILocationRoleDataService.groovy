package org.pih.warehouse.core

interface ILocationRoleDataService {
    LocationRole get(String id)

    void delete(String id)

    String deleteLocationRole(String locationRoleId)
}
