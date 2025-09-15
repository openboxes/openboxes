import _ from 'lodash';
import { z } from 'zod';

import useProductSupplierAttributes from 'hooks/productSupplier/form/useProductSupplierAttributes';
import useTranslate from 'hooks/useTranslate';

const useProductSupplierValidation = () => {
  const translate = useTranslate();

  const {
    attributes,
    isSelectType,
    inputTypeSchema,
    selectTypeSchema,
  } = useProductSupplierAttributes();

  const validationSchema = (data) => {
    const checkNecessityOfContractPrice = () => {
      if (data.fixedPrice.contractPricePrice) {
        return true;
      }

      return !data.fixedPrice.contractPriceValidUntil;
    };

    const checkDestinationPartyUniqueness = (destinationParty) => {
      const groupedData = _.groupBy(
        data.productSupplierPreferences.map((preference) => preference?.destinationParty?.id),
      );

      return groupedData[destinationParty?.id].length === 1;
    };

    // preferenceType field should be required
    // when any other field on default preference type subsection is not empty
    const checkPreferenceTypeIsRequired = (subsectionData) => {
      if (
        subsectionData.validityStartDate
        || subsectionData.validityEndDate
        || subsectionData.comments
      ) {
        // if any other field is not empty in preferenceType field should be required
        return Boolean(subsectionData.preferenceType);
      }
      // preferenceType field is valid if all other fields are empty
      return true;
    };

    const attributesValidationSchema = attributes.reduce((acc, attribute) => {
      const errorProps = {
        required_error: `${attribute?.name} is required`,
        invalid_type_error: `${attribute?.name} is required`,
      };

      const attributeSchema = isSelectType(attribute)
        ? selectTypeSchema(attribute, errorProps)
        : inputTypeSchema(attribute, errorProps);

      return {
        ...acc,
        [attribute?.id]: attributeSchema,
      };
    }, {});

    const basicDetailsSchema = z.object({
      code: z
        .string()
        .optional(),
      product: z.object({
        id: z.string(),
        value: z.string(),
        label: z.string(),
      }, {
        invalid_type_error: 'Product is required',
        required_error: 'Product is required',
      })
        .required(),
      productCode: z
        .string()
        .optional(),
      supplier: z.object({
        id: z.string(),
        value: z.string(),
        label: z.string(),
      }, {
        invalid_type_error: 'Supplier is required',
        required_error: 'Supplier is required',
      })
        .required(),
      supplierCode: z
        .string({ required_error: 'Supplier code is required' })
        .max(255, 'Max length of supplier code is 255')
        .optional(),
      name: z
        .string({
          required_error: translate(
            'react.default.form.field.required.label',
            '(Source) Name is required',
            [
              translate('react.productSupplier.form.name.title', '(Source) Name'),
            ],
          ),
        })
        .min(1, translate(
          'react.default.form.field.required.label',
          '(Source) Name is required',
          [
            translate('react.productSupplier.form.name.title', '(Source) Name'),
          ],
        ))
        .max(255, 'Max length of supplier product name is 255'),
      active: z.boolean(),
    });

    const additionalDetailsSchema = z.object({
      manufacturer: z.object({
        id: z.string(),
        value: z.string(),
        label: z.string(),
      })
        .optional()
        .nullable(),
      ratingTypeCode: z.object({
        id: z.string(),
        value: z.string(),
        label: z.string(),
      })
        .optional()
        .nullable(),
      manufacturerCode: z
        .coerce
        .string()
        .optional(),
      brandName: z
        .coerce
        .string()
        .optional(),
    });

    const defaultPreferenceTypeSchema = z.object({
      id: z
        .string()
        .nullish(),
      preferenceType: z.object({
        id: z.string(),
        value: z.string(),
        label: z.string(),
      })
        .nullish(),
      validityStartDate: z
        .string()
        .nullish(),
      validityEndDate: z
        .string()
        .nullish(),
      comments: z
        .string()
        .nullish(),
    })
      .refine(checkPreferenceTypeIsRequired, {
        message: 'Default preference type must also be selected', path: ['preferenceType'],
      });

    const productSupplierPreferenceSchema = z.object({
      id: z
        .string()
        .nullish(),
      destinationParty: z.object({
        id: z.string(),
        value: z.string(),
        label: z.string(),
      }, {
        invalid_type_error: 'Organization is required',
        required_error: 'Organization is required',
      }).required()
        .refine(checkDestinationPartyUniqueness, {
          message: 'Destination party should be unique',
        }),
      preferenceType: z.object({
        id: z.string(),
        label: z.string(),
      }, {
        invalid_type_error: 'Preference type is required',
        required_error: 'Preference type is required',
      })
        .required(),
      validityEndDate: z
        .string()
        .optional()
        .nullable(),
      validityStartDate: z
        .string()
        .optional()
        .nullable(),
      comments: z
        .string()
        .max(255, 'Max length of bid name is 255')
        .optional()
        .nullable(),
    });

    const packageSpecificationSchema = z.object({
      uom: z
        .object({
          id: z.string(),
          value: z.string(),
          label: z.string(),
        }, {
          invalid_type_error: 'Default Source Package is required',
          required_error: 'Default Source Package is required',
        })
        .required(),
      productPackageQuantity: z
        .number({ required_error: 'Package size is required' })
        .gte(1),
      minOrderQuantity: z
        .number()
        .gte(1)
        .optional(),
      productPackagePrice: z
        .number()
        .optional(),
      // since this is a computed field we want to skip validation by setting it to any
      eachPrice: z
        .any()
        .optional(),
    });

    const fixedPriceSchema = z.object({
      contractPricePrice: z
        .number()
        .optional()
        .refine(checkNecessityOfContractPrice, {
          message: 'Contract price is required',
        }),
      contractPriceValidUntil: z
        .string()
        .nullish(),
      tieredPricing: z
        .boolean()
        .optional(),
    });

    // TODO: Add translations for the error messages
    return z
      .object({
        id: z
          .string()
          .optional(),
        basicDetails: basicDetailsSchema,
        additionalDetails: additionalDetailsSchema,
        defaultPreferenceType: defaultPreferenceTypeSchema,
        productSupplierPreferences: z.array(productSupplierPreferenceSchema),
        packageSpecification: packageSpecificationSchema,
        fixedPrice: fixedPriceSchema,
        attributes: z.object(attributesValidationSchema),
      });
  };

  return {
    validationSchema,
  };
};

export default useProductSupplierValidation;
