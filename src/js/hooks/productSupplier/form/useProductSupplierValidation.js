import _ from 'lodash';
import { z } from 'zod';

import useProductSupplierAttributes from 'hooks/productSupplier/form/useProductSupplierAttributes';

const useProductSupplierValidation = () => {
  const {
    attributes,
    isSelectType,
    inputTypeSchema,
    selectTypeSchema,
  } = useProductSupplierAttributes();

  const validationSchema = (data) => {
    const checkDestinationPartyUniqueness = (destinationParty) => {
      const groupedData = _.groupBy(
        data.productSupplierPreferences.map((preference) => preference?.destinationParty?.id),
      );

      return groupedData[destinationParty?.id].length === 1;
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
      legacyCode: z
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
        .min(1, 'Supplier code is required')
        .max(255, 'Max length of supplier code is 255'),
      name: z
        .string({ required_error: 'Supplier Product Name is required' })
        .min(1, 'Supplier Product Name is required')
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
      preferenceType: z.object({
        id: z.string(),
        value: z.string(),
        label: z.string(),
      })
        .optional()
        .nullable(),
      validityStartDate: z
        .coerce
        .date()
        .optional(),
      validityEndDate: z
        .coerce
        .date()
        .optional(),
      bidName: z
        .string()
        .optional(),
    });

    const productSupplierPreferenceSchema = z.object({
      destinationParty: z.object({
        id: z.string(),
        value: z.string(),
        label: z.string(),
      }, {
        invalid_type_error: 'Site name is required',
        required_error: 'Site name is required',
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
      bidName: z
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
        .optional(),
      contractPriceValidUntil: z
        .coerce
        .date()
        .optional(),
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
