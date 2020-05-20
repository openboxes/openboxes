package org.pih.warehouse.core

enum CostingMethodCode {

    STANDARD('Standard'),
    AVERAGE('Weighted Average'),
    LIFO('Last In First Out'),
    FIFO('First In First Out'),

    static list() {
        [
                STANDARD,
                AVERAGE,
                LIFO,
                FIFO,
        ]
    }

    static listSupported() {
        [
                STANDARD,
        ]
    }

}
