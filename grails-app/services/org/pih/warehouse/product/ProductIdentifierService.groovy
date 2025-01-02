package org.pih.warehouse.product

import grails.gorm.transactions.Transactional
import java.util.regex.Matcher
import java.util.regex.Pattern
import org.apache.commons.lang.StringUtils

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.IdentifierGeneratorTypeCode
import org.pih.warehouse.core.IdentifierService
import org.pih.warehouse.core.identification.BlankIdentifierResolver
import org.pih.warehouse.core.identification.IdentifierGeneratorContext

@Transactional
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
     *   2. Else if openboxes.identifier.product.generatorType == IdentifierGeneratorTypeCode.SEQUENCE,
     *      use the value in openboxes.identifier.product.sequence.format
     *   3. Else if openboxes.identifier.product.generatorType == IdentifierGeneratorTypeCode.RANDOM,
     *      use the value in openboxes.identifier.product.random.format
     */
    @Override
    String generate(Product product, IdentifierGeneratorContext params=null) {
        ProductType productType = product.productType

        // If the product type has a custom product identifier format, use it.
        String productIdentifierFormat = productType?.productIdentifierFormat
        if (StringUtils.isNotBlank(productIdentifierFormat)) {
            return generateWithCustomFormat(product, productType, productIdentifierFormat)
        }

        // Otherwise, use the format as defined in the properties.
        return generateWithRegularFormat(product, productType)
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
     * Generates an identifier for the product using the format as defined in the properties.
     *
     * We support two different formats for products (a sequential one and a random one) so we need to use
     * the one that is enabled.
     */
    private String generateWithRegularFormat(Product product, ProductType productType) {
        IdentifierGeneratorTypeCode identifierGeneratorTypeCode = configService.getProperty(
                "openboxes.identifier.product.generatorType", IdentifierGeneratorTypeCode)
        switch (identifierGeneratorTypeCode) {
            case IdentifierGeneratorTypeCode.SEQUENCE:
                return generateForSequence(product ,productType)
                break
            case IdentifierGeneratorTypeCode.RANDOM:
                return generateForRandom(product ,productType)
                break
        }
    }

    private String generateForRandom(Product product, ProductType productType) {
        String format = configService.getProperty("openboxes.identifier.product.random.format")

        if (format.contains(Constants.IDENTIFIER_FORMAT_KEYWORD_SEQUENCE_NUMBER)) {
            throw new IllegalArgumentException("Random format cannot contain a sequenceNumber component. Check your configuration!")
        }
        if (!format.contains(Constants.IDENTIFIER_FORMAT_KEYWORD_RANDOM)) {
            throw new IllegalArgumentException("Random format must contain a random component. Check your configuration!")
        }

        // TODO: This is for backwards compatability. We used to use the format property to specify the random template,
        //       so if there's a format specified, use that as the random. Once we update our environments to use
        //       ".random.template" instead of ".format", this line can be removed, as well as the
        //       "openboxes.identifier.product.generatorType" property and all the logic around it.
        String randomTemplate = configService.getProperty("openboxes.identifier.product.format")

        // Because we're using a format that is conditional on the type, we need to  override it in order to fit
        // with the base identifier service flow. We can use the random flow from the identifier service as is though.
        return super.generate(product, IdentifierGeneratorContext.builder()
                .formatOverride(format)
                .randomTemplateOverride(randomTemplate)
                .customProperties([
                        "productTypeCode": productType?.code,
                ])
                .build())
    }

    private String generateForSequence(Product product, ProductType productType) {
        String format = configService.getProperty("openboxes.identifier.product.sequence.format")

        if (format.contains(Constants.IDENTIFIER_FORMAT_KEYWORD_RANDOM)) {
            throw new IllegalArgumentException("Sequential format cannot contain a random component. Check your configuration!")
        }
        if (!format.contains(Constants.IDENTIFIER_FORMAT_KEYWORD_SEQUENCE_NUMBER)) {
            throw new IllegalArgumentException("Sequential format must contain a sequenceNumber component. Check your configuration!")
        }
        // We don't allow using a sequential product code for products that don't have a type or that are of a
        // non-default type that does not have a code. We do this because we want to keep sequential default
        // and non-default product codes distinct from each other, which isn't possible without the product type code.
        boolean isDefaultProductType = productType?.id == configService.getProperty('openboxes.productType.default.id')
        if (!isDefaultProductType && !productType?.code) {
            throw new IllegalArgumentException("Cannot generate sequential productCode for non-default type products where product type has no code. Check your product type configuration!")
        }

        // TODO: Once sequence number is integrated to the common identifier generator flow, this can be removed.
        //       For now, we need to calculate it manually and treat it as a custom property. Note that we don't
        //       support setting sequenceNumber for non-default products via properties until this task is completed.
        Integer sequenceNumber = productTypeService.getAndSetNextSequenceNumber(productType)
        String sequenceNumberString = formatSequenceNumber(sequenceNumber)

        return super.generate(product, IdentifierGeneratorContext.builder()
        // We need to override the format because default product types use their own format.
                .formatOverride(format)
                .customProperties([
                        "sequenceNumber": sequenceNumberString,
                        "productTypeCode": productType?.code,
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
