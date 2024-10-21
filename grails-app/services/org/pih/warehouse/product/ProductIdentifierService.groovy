package org.pih.warehouse.product

import java.util.regex.Matcher
import java.util.regex.Pattern
import org.apache.commons.lang.StringUtils

import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.IdentifierGeneratorTypeCode
import org.pih.warehouse.core.IdentifierService
import org.pih.warehouse.core.identification.BlankIdentifierResolver
import org.pih.warehouse.core.identification.IdentifierGeneratorContext

// TODO: We should be able to move most of this logic into the identifier service. That way we can support sequential
//       id generation more generally. We'd need an abstract method there for fetching the next sequence number to use,
//       and a new ${sequence} config option to know where to put the sequence.
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
     * Generates product identifier for product with given product type.
     *
     * Options of generated product identifiers (and what needs to be configured to achieve it):
     *  1. If product type is the systems' default (id == openboxes.identifier.defaultProductType.id):
     *      a) SEQUENTIAL - requires:
     *          - setting openboxes.identifier.productCode.generatorType as IdentifierGeneratorTypeCode.SEQUENCE,
     *          - one of:
     *              - productIdentifierFormat (needs sequential part to be present in this field),
     *              - code in combination with with openboxes.identifier.productCode.format template,
     *              - non of both above will result 'number' only sequence, the identifier will be based on
     *                the openboxes.identifier.productCode.delimiter, openboxes.identifier.productCode.format,
     *                openboxes.identifier.productCode.properties and identifier.sequenceNumber.format config
     *                (available only for the systems' default product type)
     *      b) RANDOM - requires:
     *          - setting openboxes.identifier.productCode.generatorType as IdentifierGeneratorTypeCode.RANDOM,
     *          - one of:
     *              - filled productIdentifierFormat
     *              - empty productIdentifierFormat (will be generated basing on the openboxes.identifier.product.format)
     *  2. If product type is NOT the systems' default:
     *      a) SEQUENTIAL - requires one of:
     *          - product identifier format containng sequential part
     *          - code
     *      b) RANDOM - requires one of:
     *          - filled productIdentifierFormat
     *          - empty productIdentifierFormat (will be generated basing on the openboxes.identifier.product.format)
     */
    @Override
    String generate(Product product, IdentifierGeneratorContext params=null) {
        ProductType productType = product.productType
        if (!productType) {
            return super.generate(product, params)
        }

        IdentifierGeneratorTypeCode generatorTypeCode = configService.getProperty('openboxes.identifier.productCode.generatorType', IdentifierGeneratorTypeCode)
        String defaultProductType = configService.getProperty('openboxes.identifier.defaultProductType.id')

        if (productType?.id == defaultProductType) {
            // If product type is systems default then generate basing on the generator type
            switch (generatorTypeCode) {
                case IdentifierGeneratorTypeCode.SEQUENCE:
                    return generateSequentialProductIdentifier(product, productType)
                case IdentifierGeneratorTypeCode.RANDOM:
                    return generateRandomProductIdentifier(product, productType)
                default:
                    return generateRandomProductIdentifier(product, productType)
            }
        }

        // If product type is not default
        // and has sequential identifier format containing "0" character which indicates the sequential part
        // or identifier format is not specified when generatorTypeCode is SEQUENCE but has a code
        // then generate sequential, otherwise random
        Boolean shouldGenerateSequential = generatorTypeCode == IdentifierGeneratorTypeCode.SEQUENCE

        if (productType?.hasSequentialFormat() || (productType?.code && shouldGenerateSequential)) {
            return generateSequentialProductIdentifier(product, productType)
        }

        return generateRandomProductIdentifier(product, productType)
    }

    private String generateSequentialProductIdentifier(Product product, ProductType productType) {
        // If the product type does not have a custom identifier or does not contain character "0" which indicates the sequential part,
        // then generate sequential product code from product type code
        if (!productType.productIdentifierFormat || !productType.productIdentifierFormat.contains("0")) {
            return generateSequentialProductIdentifierFromCode(product, productType)
        }

        String sequenceFormat = extractSequenceFormat(productType.productIdentifierFormat, Constants.DEFAULT_SEQUENCE_NUMBER_FORMAT_CHAR, 1)
        def sequenceNumber = productTypeService.getAndSetNextSequenceNumber(productType)

        return generateSequentialIdentifier(product, productType.productIdentifierFormat, sequenceFormat, sequenceNumber.toString())
    }

    private String generateSequentialProductIdentifierFromCode(Product product, ProductType productType) {
        if (!productType) {
            throw new IllegalArgumentException("Missing product type")
        }

        String defaultProductTypeId = configService.getProperty('openboxes.identifier.defaultProductType.id')
        if (!productType.code && productType.id != defaultProductTypeId) {
            throw new IllegalArgumentException("Can only generate sequential product code without specified product type code " +
                    "for default product type with id: ${defaultProductTypeId}")
        }

        Integer sequenceNumber = productTypeService.getAndSetNextSequenceNumber(productType)
        String sequenceNumberStr = generateSequenceNumber(sequenceNumber.toString())

        return generate(product, IdentifierGeneratorContext.builder()
                        .customProperties([
                                "sequenceNumber": sequenceNumberStr,
                                "code": productType.code,
                        ])
                        .build())
    }

    /**
     * Generate random product identifier for specific productIdentifierFormat if it is set on the given product type,
     * or for configured config.openboxes.identifier.product.format
     */
    private String generateRandomProductIdentifier(Product product, ProductType productType) {
        // If the product type is null or the product type does not have a custom identifier,
        // then generate product code from DEFAULT_PRODUCT_NUMBER_FORMAT
        if (!productType || !productType.productIdentifierFormat) {
            return generate(product)
        }

        // if the product type does not contain sequential part, then generate identifier basing on custom format
        return generate(product, IdentifierGeneratorContext.builder()
                .formatOverride(productType.productIdentifierFormat)
                .build())
    }

    private String extractSequenceFormat(String productIdentifierFormat, String sequentialPatternChar, Integer allowedSequences) {
        Pattern pattern = Pattern.compile("${sequentialPatternChar}+")
        Matcher matcher = pattern.matcher(productIdentifierFormat)
        int count = 0
        String sequenceFormat = ""

        while (matcher.find()) {
            sequenceFormat = matcher.group()
            count++
        }

        // If custom identifier contains more than one sequential part, then throw exception
        if (count > allowedSequences) {
            throw new IllegalArgumentException("Cannot have more sequence numbers than ${allowedSequences} in the same identifier")
        }

        return sequenceFormat
    }

    private String generateSequentialIdentifier(Product product, String identifierFormat, String sequenceFormat, String sequenceNumber) {
        List identifierFormatComponents
        List identifierComponents

        // Split custom identifier by sequence format (with keeping sequence inside identifier components)
        String sequenceDelimiter = "((?<=${sequenceFormat})|(?=${sequenceFormat}))"
        identifierFormatComponents = identifierFormat.split(sequenceDelimiter)
        identifierComponents = identifierFormatComponents.collect { String identifierFormatComponent ->
            if (identifierFormatComponent.contains("0")) {
                return generateSequenceNumber(sequenceNumber.toString(), identifierFormatComponent)
            }
            else {
                return generate(product, IdentifierGeneratorContext.builder()
                        .formatOverride(identifierFormatComponent)
                        .build())
            }
        }

        return identifierComponents.join("")
    }

    private String generateSequenceNumber(String sequenceNumber) {
        String sequenceNumberFormat = configService.getProperty('openboxes.identifier.sequenceNumber.format')
        return generateSequenceNumber(sequenceNumber, sequenceNumberFormat)
    }

    private String generateSequenceNumber(String sequenceNumber, String sequenceNumberFormat) {
        return StringUtils.leftPad(sequenceNumber, sequenceNumberFormat.length(), sequenceNumberFormat.substring(0, 1))
    }
}
