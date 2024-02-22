import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { useParams } from 'react-router-dom';

import productSupplierApi from 'api/services/ProductSupplierApi';
import useProductSupplierValidation from 'hooks/productSupplier/form/useProductSupplierValidation';
import { omitEmptyValues } from 'utils/form-values-utils';

const useProductSupplierForm = () => {
  const { validationSchema } = useProductSupplierValidation();
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
      supplier: productSupplier?.supplier
        ? {
          id: productSupplier?.supplier?.id,
          value: productSupplier?.supplier?.id,
          label: `${productSupplier?.supplier?.code} ${productSupplier?.supplier?.name}`,
        } : undefined,
      manufacturer: productSupplier?.manufacturer
        ? {
          id: productSupplier?.manufacturer.id,
          value: productSupplier?.manufacturer.id,
          label: productSupplier?.manufacturer.name,
        }
        : undefined,
      ratingTypeCode: productSupplier?.ratingTypeCode
        ? {
          id: productSupplier?.ratingTypeCode,
          value: productSupplier?.ratingTypeCode,
          label: productSupplier?.ratingTypeCode,
        }
        : undefined,
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
