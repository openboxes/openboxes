package org.pih.warehouse.core;

import grails.gorm.services.Service;

@Service(UnitOfMeasureConversion)
interface UnitOfMeasureConversionDataService {

    void delete(String id);

    UnitOfMeasureConversion save(UnitOfMeasureConversion unitOfMeasureConversion);

    UnitOfMeasureConversion get(String id);
}
