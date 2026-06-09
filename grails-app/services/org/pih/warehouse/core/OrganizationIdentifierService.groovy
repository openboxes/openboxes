package org.pih.warehouse.core

import grails.gorm.transactions.Transactional

@Transactional
class OrganizationIdentifierService extends IdentifierService<Organization> {

    @Override
    String getIdentifierName() {
        return "organization"
    }

    @Override
    protected Integer countByIdentifier(String id) {
        return Organization.countByCode(id)
    }
}
