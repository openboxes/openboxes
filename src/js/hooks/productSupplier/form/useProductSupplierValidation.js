import { z } from 'zod';

const useProductSupplierValidation = () => {
  // TODO: Add translations for the error messages
  const validationSchema = z
    .object({
      id: z.string().optional(),
      code: z.string().optional(),
      product: z.object({
        id: z.string(),
        value: z.string(),
        label: z.string(),
      }, {
        invalid_type_error: 'Product is required',
        required_error: 'Product is required',
      }).required(),
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
      }).required(),
      supplierCode: z
        .string({ required_error: 'Supplier code is required' })
        .min(1, 'Supplier code is required')
        .max(255, 'Max length of supplier code is 255'),
      name: z
        .string({ required_error: 'Supplier Product Name is required' })
        .min(1, 'Supplier Product Name is required')
        .max(255, 'Max length of supplier product name is 255'),
      active: z.boolean(),
      manufacturer: z.object({
        id: z.string(),
        value: z.string(),
        label: z.string(),
      }).optional(),
      ratingTypeCode: z.object({
        id: z.string(),
        value: z.string(),
        label: z.string(),
      }).optional(),
      manufacturerCode: z
        .string()
        .optional(),
      brandName: z
        .string()
        .optional(),
    });

  return {
    validationSchema,
  };
};

export default useProductSupplierValidation;
