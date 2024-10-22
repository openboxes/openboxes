package org.pih.warehouse.product

import java.util.regex.Matcher
import java.util.regex.Pattern
import org.apache.commons.lang.StringUtils

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.IdentifierService
import org.pih.warehouse.core.identification.BlankIdentifierResolver
import org.pih.warehouse.core.identification.IdentifierGeneratorContext

class ProductIdentifierService extends IdentifierService<Product> implements BlankIdentifierResolver<Product> {

    ProductTypeService productTypeService

    @Override
    String getIdentifierName() {
        return "product"
    }

    @Override
    protected Integer countByIdentifier(String id) {
        return Product.countByProductCode(id)
    }

    @Override
    List<Product> getAllUnassignedEntities() {
        return Product.findAll("from Product as p where p.active = true and (p.productCode is null or p.productCode = '')")
    }

    @Override
    void setIdentifierOnEntity(String id, Product entity) {
        entity.productCode = id
    }

    /**
     * Generates a product identifier. The identifier will take the following format:
     *   1. If the product type has a productIdentifierFormat defined, use that.
     *   2. Else if the product is of the default type, uses "openboxes.identifier.product.defaultProductType.format"
     *   3. Else the value in "openboxes.identifier.product.format" if defined
     *   4. Else the value in "openboxes.identifier.default.format"
     */
    @Override
    String generate(Product product, IdentifierGeneratorContext params=null) {
        ProductType productType = product.productType

        // If the product type has a custom product identifier format, use it.
        String productIdentifierFormat = productType?.productIdentifierFormat
        if (StringUtils.isNotBlank(productIdentifierFormat)) {
            return generateWithCustomFormat(product, productType, productIdentifierFormat)
        }

        // If the product belongs to the default product type, use a custom format for default products.
        if (productType?.id == configService.getProperty('openboxes.productType.default.id')) {
            return generateForDefaultProduct(product, productType)
        }

        // Otherwise the product is not of the default type and has no custom format override, so use the regular flow.
        return super.generate(product, params)
    }

    /**
     * Generates an identifier for a given productIdentifierFormat. The rules for this per-productType format are
     * slightly different than the regular id generator flow, so we need custom logic here to support it.
     *
     * Groups of characters matching IDENTIFIER_FORMAT_KEYWORD_SEQUENCE_NUMBER will be converted to a sequence number,
     * then the format will be run through the random generator to fill the rest of the string.
     *
     * Ex: Given a productIdentifierFormat "MNNNN-0000" and the current sequence number is 15 -> "M1234-0015"
     */
    private String generateWithCustomFormat(Product product, ProductType productType, String productIdentifierFormat) {
        // If productIdentifierFormat has a sequential piece, generate it and replace it into the format.
        String formatWithSequentialFilledIn = productType.hasSequentialFormat() ?
                generateAndFillSequenceNumber(productType, productIdentifierFormat) :
                productIdentifierFormat

        // Now that formatWithSequentialFilledIn has the sequential piece filled in, it is effectively a random
        // template. This means we can use the common generate flow by simply setting a custom format "${random}"
        // and overriding the random template with formatWithSequentialFilledIn.
        return super.generate(product, IdentifierGeneratorContext.builder()
                .formatOverride(Constants.IDENTIFIER_FORMAT_KEYWORD_RANDOM_EMBEDDED)
                .randomTemplateOverride(formatWithSequentialFilledIn)
                .build())
    }

    /**
     * Generates an identifier for the product, given that it is of the default type.
     * This is needed since default products have their own identifier format.
     */
    private String generateForDefaultProduct(Product product, ProductType defaultProductType) {
        String defaultFormat = configService.getProperty('openboxes.identifier.product.defaultProductType.format')
        if (StringUtils.isBlank(defaultFormat)) {
            throw new IllegalArgumentException("Cannot find product identifier format for default product type. Check your configuration!")
        }

        // TODO: Once sequence number is integrated to the common identifier generator flow, this can be removed.
        //       For now, we need to calculate it manually and treat it as a custom property. Note that we don't
        //       support setting sequenceNumber for non-default products via properties until this task is completed.
        String sequenceNumberString = null
        if (defaultFormat.contains(Constants.IDENTIFIER_FORMAT_KEYWORD_SEQUENCE_NUMBER)) {
            Integer sequenceNumber = productTypeService.getAndSetNextSequenceNumber(defaultProductType)
            sequenceNumberString = formatSequenceNumber(sequenceNumber)
        }

        return super.generate(product, IdentifierGeneratorContext.builder()
                // We need to override the format because default product types use their own format.
                .formatOverride(defaultFormat)
                .customProperties([
                        "sequenceNumber": sequenceNumberString,
                        "productTypeCode": defaultProductType.code,
                ])
                .build())
    }

    /**
     * Generates a sequence number for the given product type and fills it into the given format.
     * Ex: If productIdentifierFormat == "MNNNN-0000", and sequence number is 15, this will return "MNNNN-0015"
     */
    private String generateAndFillSequenceNumber(ProductType productType, String productIdentifierFormat) {
        String sequenceNumberFormat = extractSequenceNumberFormat(productIdentifierFormat)
        String sequenceNumber = productTypeService.getAndSetNextSequenceNumber(productType).toString()
        String sequenceNumberFormatted = formatSequenceNumber(sequenceNumber, sequenceNumberFormat)

        // Stick the generated sequence number into the original format in place of the sequence number format.
        return productIdentifierFormat.replace(sequenceNumberFormat, sequenceNumberFormatted)
    }

    /**
     * Pulls out just the sequence number portion of a format.
     *
     * Ex: If given "MNNNN-0000", will return "0000"
     */
    private String extractSequenceNumberFormat(String productIdentifierFormat) {
        Pattern pattern = Pattern.compile("${Constants.DEFAULT_SEQUENCE_NUMBER_FORMAT_CHAR}+")
        Matcher matcher = pattern.matcher(productIdentifierFormat)
        int count = 0
        String sequenceFormat = ""
        while (matcher.find()) {
            sequenceFormat = matcher.group()
            count++
        }

        if (count > 1) {
            throw new IllegalArgumentException("Cannot have more than 1 sequence number in the same identifier")
        }

        return sequenceFormat
    }

    /**
     * Given a sequence number, format it to conform to the current settings.
     *
     * Ex: Given sequenceNumberMinSize = 5, and the current sequence number is 15, will output "00015".
     */
    private String formatSequenceNumber(Integer sequenceNumber) {
        Integer sequenceNumberMinSize = configService.getProperty('openboxes.identifier.default.sequenceNumber.minSize', Integer)
        String sequenceNumberFormat = Constants.DEFAULT_SEQUENCE_NUMBER_FORMAT_CHAR * sequenceNumberMinSize
        return formatSequenceNumber(sequenceNumber.toString(), sequenceNumberFormat)
    }

    private String formatSequenceNumber(String sequenceNumber, String sequenceNumberFormat) {
        return StringUtils.leftPad(sequenceNumber, sequenceNumberFormat.length(), sequenceNumberFormat.substring(0, 1))
    }
}
