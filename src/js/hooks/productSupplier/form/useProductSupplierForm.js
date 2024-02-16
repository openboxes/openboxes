import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { useParams } from 'react-router-dom';
import { z } from 'zod';

import productSupplierApi from 'api/services/ProductSupplierApi';
import { omitEmptyValues } from 'utils/form-values-utils';

const useProductSupplierForm = () => {
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

  // Check if productSupplierId is provided in the URL (determine whether it is create or edit)
  const { productSupplierId } = useParams();

  // Fetches product supplier to edit and returns default values that should be set
  const getProductSupplier = async () => {
    const response =
      await productSupplierApi.getProductSupplier(productSupplierId);
    const productSupplier = response?.data?.data;
    return {
      // Exclude null/empty values
      ...omitEmptyValues(productSupplier),
      product: {
        id: productSupplier?.product?.id,
        value: productSupplier?.product?.id,
        label: productSupplier?.product?.name,
      },
      supplier: {
        id: productSupplier?.supplier?.id,
        value: productSupplier?.supplier?.id,
        label: productSupplier?.supplier?.name,
      },
      manufacturer: productSupplier?.manufacturer
        ? {
          id: productSupplier?.manufacturer.id,
          value: productSupplier?.manufacturer.id,
          label: productSupplier?.manufacturer.name,
        }
        : undefined,
      active: true,
    };
  };

  const {
    control,
    handleSubmit,
    formState: { errors, isValid },
  } = useForm({
    // We want the validation errors to occur onBlur of any field
    mode: 'onBlur',
    // If there is a productSupplier param, it means we are editing a product supplier, so fetch it,
    // otherwise the only default value should be the active field
    defaultValues: productSupplierId ? getProductSupplier : { active: true },
    resolver: zodResolver(validationSchema),
  });

  // TODO: To be replaced by rating type codes returned from the API
  const mockedRatingTypeCodes = [
    {
      id: 'OUTSTANDING',
      value: 'OUTSTANDING',
      label: 'OUTSTANDING',
    },
    {
      id: 'GOOD',
      value: 'GOOD',
      label: 'GOOD',
    },
    {
      id: 'FAIR',
      value: 'FAIR',
      label: 'FAIR',
    },
  ];

  const onSubmit = (values) => {
    const payload = {
      ...omitEmptyValues(values),
      product: values?.product ? values.product.id : null,
      supplier: values?.supplier ? values.supplier.id : null,
      manufacturer: values?.manufacturer ? values.manufacturer.id : null,
      ratingTypeCode: values?.ratingTypeCode ? values.ratingTypeCode.id : null,
    };
    // If values contain id, it means we are editing
    if (values?.id) {
      console.log(payload);
      return;
    }
    console.log(payload);
  };

  return {
    control,
    handleSubmit,
    errors,
    isValid,
    mockedRatingTypeCodes,
    onSubmit,
  };
};

export default useProductSupplierForm;
