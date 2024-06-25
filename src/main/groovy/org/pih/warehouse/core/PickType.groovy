package org.pih.warehouse.core

enum PickType {
    MANUAL('MANUAL'),
    IMPORT('IMPORT'),
    AUTO('AUTO')

    String name

    PickType(String name) {
        this.name = name
    }

    @Override
    String toString() {
        return name()
    }
}
