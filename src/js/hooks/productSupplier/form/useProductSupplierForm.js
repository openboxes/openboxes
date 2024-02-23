import { useEffect } from 'react';

import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { useDispatch, useSelector } from 'react-redux';
import { useParams } from 'react-router-dom';

import { fetchRatingTypeCodes } from 'actions';
import productSupplierApi from 'api/services/ProductSupplierApi';
import useProductSupplierValidation from 'hooks/productSupplier/form/useProductSupplierValidation';
import { omitEmptyValues } from 'utils/form-values-utils';
import { splitPreferenceTypes } from 'utils/list-utils';

const useProductSupplierForm = () => {
  const { validationSchema } = useProductSupplierValidation();
  // Check if productSupplierId is provided in the URL (determine whether it is create or edit)
  const { productSupplierId } = useParams();
  const dispatch = useDispatch();

  const {
    ratingTypeCodes,
  } = useSelector((state) => ({
    ratingTypeCodes: state.productSupplier.ratingTypeCodes,
  }));

  useEffect(() => {
    dispatch(fetchRatingTypeCodes());
  }, []);

  // Fetches product supplier to edit and returns default values that should be set
  const getProductSupplier = async () => {
    const response =
      await productSupplierApi.getProductSupplier(productSupplierId);
    const productSupplier = response?.data?.data;
    const { preferenceTypes } = splitPreferenceTypes(productSupplier?.productSupplierPreferences);
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
      productSupplierPreferences: preferenceTypes.map((preferenceType) => ({
        ...preferenceType,
        destinationParty: {
          id: preferenceType.destinationParty?.id,
          label: preferenceType.destinationParty?.name,
          value: preferenceType.destinationParty?.id,
        },
        preferenceType: {
          id: preferenceType.preferenceType?.id,
          label: preferenceType.preferenceType?.name,
          value: preferenceType.preferenceType?.id,
        },
      })),
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

  const onSubmit = (values) => {
    const payload = {
      ...omitEmptyValues(values),
      product: values?.product ? values.product.id : null,
      supplier: values?.supplier ? values.supplier.id : null,
      manufacturer: values?.manufacturer ? values.manufacturer.id : null,
      ratingTypeCode: values?.ratingTypeCode ? values.ratingTypeCode.id : null,
      productSupplierPreferences: values?.productSupplierPreferences,
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
    ratingTypeCodes,
    onSubmit,
  };
};

export default useProductSupplierForm;
