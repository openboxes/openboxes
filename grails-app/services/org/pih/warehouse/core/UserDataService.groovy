package org.pih.warehouse.core

import grails.gorm.services.Service

@Service(value = User, name = "userGormService")
interface UserDataService {
    void delete(String id)

    User save(User user)

    User get(String id)
}
