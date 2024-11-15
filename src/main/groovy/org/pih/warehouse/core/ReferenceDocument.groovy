package org.pih.warehouse.core

class ReferenceDocument {
    String label

    String url

    String id

    String identifier

    String description

    String name

    static constraints = {
        url(nullable: true)
        description(nullable: true)
        name(nullable: true)
    }
}
