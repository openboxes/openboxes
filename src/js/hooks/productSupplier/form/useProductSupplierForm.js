import { zodResolver } from '@hookform/resolvers/zod';
import _ from 'lodash';
import { useForm } from 'react-hook-form';
import { useParams } from 'react-router-dom';

import { fetchPreferenceTypes, fetchRatingTypeCodes } from 'actions';
import productSupplierApi from 'api/services/ProductSupplierApi';
import useOptionsFetch from 'hooks/options-data/useOptionsFetch';
import useCalculateEachPrice from 'hooks/productSupplier/form/useCalculateEachPrice';
import useProductSupplierAttributes from 'hooks/productSupplier/form/useProductSupplierAttributes';
import useProductSupplierValidation from 'hooks/productSupplier/form/useProductSupplierValidation';
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
    const response = await productSupplierApi.getProductSupplier(productSupplierId);
    const productSupplier = response?.data?.data;
    const attributes = mapFetchedAttributes(productSupplier?.attributes);
    const {
      preferenceTypes,
      defaultPreferenceType,
    } = splitPreferenceTypes(productSupplier?.productSupplierPreferences);

    return {
      id: productSupplier?.id ?? undefined,
      basicDetails: {
        code: productSupplier?.code ?? undefined,
        product: {
          id: productSupplier?.product?.id,
          value: productSupplier?.product?.id,
          label: `${productSupplier?.product?.productCode} - ${productSupplier?.product?.name}`,
        },
        legacyCode: productSupplier?.legacyCode ?? undefined,
        supplier: productSupplier?.supplier
          ? {
            id: productSupplier?.supplier?.id,
            value: productSupplier?.supplier?.id,
            label: `${productSupplier?.supplier?.code} ${productSupplier?.supplier?.name}`,
          } : undefined,
        supplierCode: productSupplier?.supplierCode ?? undefined,
        name: productSupplier?.name ?? undefined,
        active: productSupplier?.active,
        dateCreated: productSupplier?.dateCreated ?? undefined,
        lastUpdated: productSupplier?.lastUpdated ?? undefined,
      },
      additionalDetails: {
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
        manufacturerCode: productSupplier?.manufacturerCode ?? undefined,
        brandName: productSupplier?.brandName ?? undefined,
      },
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
      packageSpecification: {
        uom: productSupplier?.defaultProductPackage
          ? {
            id: productSupplier?.defaultProductPackage?.uom?.id,
            value: productSupplier?.defaultProductPackage?.uom?.id,
            label: productSupplier?.defaultProductPackage?.uom?.name,
          }
          : undefined,
        productPackageQuantity: productSupplier?.defaultProductPackage?.quantity ?? undefined,
        minOrderQuantity: productSupplier?.minOrderQuantity ?? undefined,
        productPackagePrice: productSupplier?.defaultProductPackage?.productPrice?.price
          ?? undefined,
        eachPrice: productSupplier?.eachPrice ?? undefined,
      },
      fixedPrice: {
        contractPricePrice: productSupplier?.contractPrice?.price ?? undefined,
        contractPriceValidUntil: productSupplier?.contractPrice?.validUntil ?? undefined,
        tieredPricing: productSupplier?.tieredPricing ?? undefined,
      },
      attributes,
      defaultPreferenceType: {
        ...defaultPreferenceType,
        preferenceType: !_.isEmpty(defaultPreferenceType) ? {
          id: defaultPreferenceType?.preferenceType?.id,
          label: defaultPreferenceType?.preferenceType?.name,
          value: defaultPreferenceType?.preferenceType?.id,
        } : undefined,
      },
    };
  };

  const defaultValues = {
    basicDetails: {
      active: true,
    },
    fixedPrice: {
      tieredPricing: false,
    },
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

  useCalculateEachPrice({ control, setValue });

  const onSubmit = (values) => {
    const payload = {
      ...omitEmptyValues(values.basicDetails),
      ...omitEmptyValues(values.additionalDetails),
      ...omitEmptyValues(values.defaultPreferenceType),
      ...omitEmptyValues(values.packageSpecification),
      ...omitEmptyValues(values.fixedPrice),
      attributes: omitEmptyValues(values.attributes),
      productSupplierPreferences: values?.productSupplierPreferences,
      product: values?.product ? values.product.id : null,
      supplier: values?.supplier ? values.supplier.id : null,
      manufacturer: values?.manufacturer ? values.manufacturer.id : null,
      ratingTypeCode: values?.ratingTypeCode ? values.ratingTypeCode.id : null,
      uom: values?.uom ? values.uom.id : null,
      preferenceType: values?.preferenceType ? values.preferenceType.id : null,
    };
    // If values contain id, it means we are editing
    if (values?.id) {
      console.log(payload);
      return;
    }
    console.log(payload);
  };

  // preselect value 1 when unit of measure Each is selected
  const setProductPackageQuantity = (unitOfMeasure) => {
    if (unitOfMeasure?.id === 'EA') {
      setValue('packageSpecification.productPackageQuantity', 1, { shouldValidate: true });
      return;
    }
    setValue('packageSpecification.productPackageQuantity', '');
  };

  return {
    control,
    handleSubmit,
    errors,
    isValid,
    triggerValidation: trigger,
    onSubmit,
    setProductPackageQuantity,
    setValue,
  };
};

export default useProductSupplierForm;
