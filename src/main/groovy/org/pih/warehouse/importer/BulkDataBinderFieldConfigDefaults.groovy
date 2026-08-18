package org.pih.warehouse.importer

import org.pih.warehouse.core.parser.EnumParser
import org.pih.warehouse.core.parser.ParserContext

/**
 * Defaults and constants BulkDataBinderFieldConfig
 */
class BulkDataBinderFieldConfigDefaults {

    /**
     * The default configuration when binding a field.
     */
    static final BulkDataBinderFieldConfig DEFAULT_CONFIG = new BulkDataBinderFieldConfig()

    /**
     * The default configuration when we want to manually bind a field via custom data binding.
     */
    static final BulkDataBinderFieldConfig MANUALLY_BOUND = new BulkDataBinderFieldConfig(
            dataBindingMethod: DataBindingMethod.MANUAL
    )

    /**
     * Builds a default configuration for binding Enum fields.
     */
    static BulkDataBinderFieldConfig buildDefaultEnumFieldConfig(Class enumClass) {
        return new BulkDataBinderFieldConfig(
                parser: EnumParser,
                parserContext: new ParserContext(
                        typeToParseTo: enumClass,
                ),
        )
    }
}
