import { useEffect } from 'react';

import { zodResolver } from '@hookform/resolvers/zod';
import _ from 'lodash';
import { useForm, useWatch } from 'react-hook-form';
import { useParams } from 'react-router-dom';

import { fetchPreferenceTypes, fetchRatingTypeCodes } from 'actions';
import productSupplierApi from 'api/services/ProductSupplierApi';
import useOptionsFetch from 'hooks/options-data/useOptionsFetch';
import useProductSupplierAttributes from 'hooks/productSupplier/form/useProductSupplierAttributes';
import useProductSupplierValidation from 'hooks/productSupplier/form/useProductSupplierValidation';
import { decimalParser } from 'utils/form-utils';
import { omitEmptyValues } from 'utils/form-values-utils';
import { splitPreferenceTypes } from 'utils/list-utils';

const useProductSupplierForm = () => {
  const { validationSchema } = useProductSupplierValidation();
  const { mapFetchedAttributes } = useProductSupplierAttributes();
  // Check if productSupplierId is provided in the URL (determine whether it is create or edit)
  const { productSupplierId } = useParams();

  useOptionsFetch(
    [fetchRatingTypeCodes, fetchPreferenceTypes],
    { refetchOnLocaleChange: false },
  );

  // Fetches product supplier to edit and returns default values that should be set
  const getProductSupplier = async () => {
    const response =
      await productSupplierApi.getProductSupplier(productSupplierId);
    const productSupplier = response?.data?.data;
    const attributes = mapFetchedAttributes(productSupplier?.attributes);
    const {
      preferenceTypes,
      defaultPreferenceType,
    } = splitPreferenceTypes(productSupplier?.productSupplierPreferences);
    return {
      // Exclude null/empty values
      ...omitEmptyValues(productSupplier),
      product: {
        id: productSupplier?.product?.id,
        value: productSupplier?.product?.id,
        label: `${productSupplier?.product?.productCode} - ${productSupplier?.product?.name}`,
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
      preferenceType: defaultPreferenceType?.preferenceType ? {
        id: defaultPreferenceType.preferenceType.id,
        label: defaultPreferenceType.preferenceType.name,
        value: defaultPreferenceType.preferenceType.id,
      } : undefined,
      validityStartDate: defaultPreferenceType?.validityStartDate ?? undefined,
      validityEndDate: defaultPreferenceType?.validityEndDate ?? undefined,
      bidName: defaultPreferenceType?.bidName ?? undefined,
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
      uom: {
        id: productSupplier?.defaultProductPackage?.uom?.id,
        value: productSupplier?.defaultProductPackage?.uom?.id,
        label: productSupplier?.defaultProductPackage?.uom?.name,
      },
      productPackageQuantity: productSupplier?.defaultProductPackage?.quantity,
      productPackagePrice: productSupplier?.defaultProductPackage?.productPrice?.price,
      contractPricePrice: productSupplier?.contractPrice?.price,
      contractPriceValidUntil: productSupplier?.contractPrice?.validUntil,
      attributes,
    };
  };

  const defaultValues = {
    active: true,
    tieredPricing: false,
  };

  const {
    control,
    handleSubmit,
    trigger,
    setValue,
    formState: { errors, isValid },
  } = useForm({
    // We want the validation errors to occur onBlur of any field
    mode: 'onBlur',
    // If there is a productSupplier param, it means we are editing a product supplier, so fetch it,
    // otherwise the only default value should be the active field
    defaultValues: productSupplierId ? getProductSupplier : defaultValues,
    resolver: (values, context, options) =>
      zodResolver(validationSchema(values))(values, context, options),
  });

  const onSubmit = (values) => {
    const payload = {
      ...omitEmptyValues(values),
      product: values?.product ? values.product.id : null,
      supplier: values?.supplier ? values.supplier.id : null,
      manufacturer: values?.manufacturer ? values.manufacturer.id : null,
      ratingTypeCode: values?.ratingTypeCode ? values.ratingTypeCode.id : null,
      productSupplierPreferences: values?.productSupplierPreferences,
      uom: values?.uom ? values.uom.id : null,
      preferenceType: values?.preferenceType ? values.preferenceType.id : null,
      defaultPreferenceType: values?.defaultPreferenceType ? values.defaultPreferenceType.id : null,
      attributes: omitEmptyValues(values?.attributes),
    };
    // If values contain id, it means we are editing
    if (values?.id) {
      console.log(payload);
      return;
    }
    console.log(payload);
  };

  const packagePrice = useWatch({ control, name: 'productPackagePrice' });
  const productPackageQuantity = useWatch({ control, name: 'productPackageQuantity' });

  // eachPrice is a computed value from packagePrice and productPackageQuantity
  useEffect(() => {
    if (
      !_.isNil(packagePrice) &&
      !_.isNil(productPackageQuantity) &&
      productPackageQuantity !== 0
    ) {
      setValue('eachPrice', decimalParser(packagePrice / productPackageQuantity, 4));
    } else {
      setValue('eachPrice', '');
    }
  },
  [packagePrice, productPackageQuantity]);

  // preselect value 1 when unit of measure Each is selected
  const setProductPackageQuantity = (unitOfMeasure) => {
    if (unitOfMeasure?.id === 'EA') {
      setValue('productPackageQuantity', 1, { shouldValidate: true });
      return;
    }
    setValue('productPackageQuantity', '');
  };

  return {
    control,
    handleSubmit,
    errors,
    isValid,
    triggerValidation: trigger,
    onSubmit,
    setProductPackageQuantity,
  };
};

export default useProductSupplierForm;