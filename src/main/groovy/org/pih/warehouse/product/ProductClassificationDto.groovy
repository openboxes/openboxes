package org.pih.warehouse.product

class ProductClassificationDto {

    String name

    Map toJson() {
        [
                name: name,
        ]
    }
}
