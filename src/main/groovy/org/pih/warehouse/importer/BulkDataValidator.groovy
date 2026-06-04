package org.pih.warehouse.importer

import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

import org.pih.warehouse.core.localization.MessageLocalizer
import org.pih.warehouse.core.parser.DefaultTypeParser

/**
 * Takes in a List of Map of bulk data and binds it to a list of strongly typed Importable objects.
 */
@Component
class BulkDataValidator {

    final private BulkDataImportComponentResolver componentResolver
    final private DefaultTypeParser defaultTypeParser
    final private MessageLocalizer messageLocalizer
    final private ApplicationContext context

    BulkDataValidator(final BulkDataImportComponentResolver componentResolver,
                      final DefaultTypeParser defaultTypeParser,
                      final MessageLocalizer messageLocalizer,
                      final ApplicationContext context) {
        this.componentResolver = componentResolver
        this.defaultTypeParser = defaultTypeParser
        this.messageLocalizer = messageLocalizer
        this.context = context
    }

    BulkDataBinderResult bindData(BulkDataType bulkDataType, List<Importable> bulkData) {

        // need to make sure grails validation is called if it has some!
        // if GormValidateable or Validateable or Validatable, call validate!

        // For all errors on the object, add them to the result. Can we split them out by row and column??
        // Then if there's a configurer, also call customconfig
    }
}
