import { zodResolver } from '@hookform/resolvers/zod';
import _ from 'lodash';
import moment from 'moment';
import { useForm } from 'react-hook-form';
import { useDispatch } from 'react-redux';
import { useHistory, useParams } from 'react-router-dom';

import {
  fetchPreferenceTypes,
  fetchRatingTypeCodes,
  hideSpinner,
  showSpinner,
} from 'actions';
import productApi from 'api/services/ProductApi';
import productPackageApi from 'api/services/ProductPackageApi';
import productSupplierApi from 'api/services/ProductSupplierApi';
import productSupplierAttributeApi from 'api/services/ProductSupplierAttributeApi';
import productSupplierPreferenceApi from 'api/services/ProductSupplierPreferenceApi';
import notification from 'components/Layout/notifications/notification';
import { PRODUCT_SUPPLIER_URL } from 'consts/applicationUrls';
import NotificationType from 'consts/notificationTypes';
import { DateFormat } from 'consts/timeFormat';
import useOptionsFetch from 'hooks/options-data/useOptionsFetch';
import useCalculateEachPrice from 'hooks/productSupplier/form/useCalculateEachPrice';
import useProductSupplierAttributes from 'hooks/productSupplier/form/useProductSupplierAttributes';
import useProductSupplierValidation from 'hooks/productSupplier/form/useProductSupplierValidation';
import useQueryParams from 'hooks/useQueryParams';
import useTranslate from 'hooks/useTranslate';
import { omitEmptyValues } from 'utils/form-values-utils';
import { splitPreferenceTypes } from 'utils/list-utils';

const useProductSupplierForm = () => {
  const { validationSchema } = useProductSupplierValidation();
  const { mapFetchedAttributes } = useProductSupplierAttributes();
  // Check if productSupplierId is provided in the URL (determine whether it is create or edit)
  const { productSupplierId } = useParams();
  const queryParams = useQueryParams();

  const history = useHistory();
  const translate = useTranslate();
  const dispatch = useDispatch();

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
        productCode: productSupplier?.productCode ?? undefined,
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
        createdBy: {
          id: productSupplier?.createdBy?.id ?? undefined,
          name: productSupplier?.createdBy?.name ?? undefined,
        },
        updatedBy: {
          id: productSupplier?.updatedBy?.id ?? undefined,
          name: productSupplier?.updatedBy?.name ?? undefined,
        },
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
        id: defaultPreferenceType?.id ?? undefined,
        preferenceType: !_.isEmpty(defaultPreferenceType) ? {
          id: defaultPreferenceType?.preferenceType?.id,
          label: defaultPreferenceType?.preferenceType?.name,
          value: defaultPreferenceType?.preferenceType?.id,
        } : undefined,
        validityStartDate: defaultPreferenceType?.validityStartDate ?? undefined,
        validityEndDate: defaultPreferenceType?.validityEndDate ?? undefined,
        comments: defaultPreferenceType?.comments ?? undefined,
      },
    };
  };

  const initializeDefaultValues = async () => {
    if (productSupplierId) {
      return getProductSupplier();
    }

    if (queryParams.productId) {
      const productResponse = await productApi.getProduct(queryParams.productId);
      const product = productResponse?.data?.data;
      return {
        basicDetails: {
          active: true,
          product: {
            id: product?.id,
            value: product?.id,
            label: `${product?.productCode} - ${product?.name}`,
          },
        },
        fixedPrice: {
          tieredPricing: false,
        },
        productSupplierPreferences: [],
      };
    }

    return {
      basicDetails: {
        active: true,
      },
      fixedPrice: {
        tieredPricing: false,
      },
      productSupplierPreferences: [],
    };
  };

  const {
    control,
    handleSubmit,
    trigger,
    setValue,
    formState: { errors, isValid },
    getValues,
  } = useForm({
    // We want the validation errors to occur onBlur of any field
    mode: 'onBlur',
    // If there is a productSupplier param, it means we are editing a product supplier, so fetch it,
    // otherwise the only default value should be the active field
    defaultValues: initializeDefaultValues,
    resolver: (values, context, options) =>
      zodResolver(validationSchema(values))(values, context, options),
  });

  useCalculateEachPrice({ control, setValue });

  const buildDetailsPayload = ({ basicDetails, additionalDetails, tieredPricing }) => {
    const { product, supplier } = basicDetails;
    const { manufacturer, ratingTypeCode } = additionalDetails;

    return {
      ...omitEmptyValues(basicDetails),
      ...omitEmptyValues(additionalDetails),
      product: product ? product.id : null,
      supplier: supplier ? supplier.id : null,
      manufacturer: manufacturer ? manufacturer.id : null,
      ratingTypeCode: ratingTypeCode ? ratingTypeCode.id : null,
      tieredPricing,
    };
  };

  const buildPreferencesPayload = ({
    defaultPreferenceType,
    productSupplierPreferences,
    productSupplier,
  }) => {
    // Map preference variations from the table
    const preferenceVariations = productSupplierPreferences?.map((productSupplierPreference) => ({
      ...productSupplierPreference,
      validityStartDate: productSupplierPreference.validityStartDate
        ? moment(productSupplierPreference.validityStartDate).format(DateFormat.MM_DD_YYYY)
        : null,
      validityEndDate: productSupplierPreference.validityEndDate
        ? moment(productSupplierPreference.validityEndDate).format(DateFormat.MM_DD_YYYY)
        : null,
      preferenceType: productSupplierPreference?.preferenceType?.id,
      productSupplier,
    }));
    // Map the default preference type
    const defaultPreferenceTypeMapped = {
      ...defaultPreferenceType,
      validityStartDate: defaultPreferenceType.validityStartDate
        ? moment(defaultPreferenceType.validityStartDate).format(DateFormat.MM_DD_YYYY)
        : null,
      validityEndDate: defaultPreferenceType.validityEndDate
        ? moment(defaultPreferenceType.validityEndDate).format(DateFormat.MM_DD_YYYY)
        : null,
      preferenceType: defaultPreferenceType?.preferenceType?.id,
      productSupplier,
    };
    // Combine the variations and the default preference type into a single list
    // Filter out elements that do not have any value filled
    // (do not take productSupplier and id into account,
    // as they would be filled anyway, because they come from previous response)
    const preferencesCombined = [
      defaultPreferenceTypeMapped,
      ...preferenceVariations,
    ].filter((preference) => _.some(Object.values(_.omit(preference, 'productSupplier', 'id'))));

    return {
      productSupplierPreferences: preferencesCombined,
    };
  };

  const buildPackagePayload = ({ packageSpecification, fixedPrice, productSupplier }) => {
    const { uom } = packageSpecification;
    return {
      ...omitEmptyValues(packageSpecification),
      ...omitEmptyValues(fixedPrice),
      contractPricePrice: fixedPrice?.contractPricePrice ?? null,
      contractPriceValidUntil: fixedPrice?.contractPriceValidUntil
        ? moment(fixedPrice?.contractPriceValidUntil).format(DateFormat.MM_DD_YYYY)
        : null,
      uom: uom ? uom.id : null,
      productSupplier,
    };
  };

  const buildAttributesPayload = ({ attributes, productSupplier }) => {
    const attributesMapped = Object.entries(attributes).map(([attributeId, values]) => ({
      attribute: attributeId,
      productSupplier,
      value: values?.value ?? values ?? '',
    }));

    return {
      productAttributes: attributesMapped,
    };
  };

  const onSubmit = async (values) => {
    const {
      basicDetails,
      additionalDetails,
      defaultPreferenceType,
      packageSpecification,
      fixedPrice,
      attributes,
      productSupplierPreferences,
    } = values;

    const { tieredPricing } = fixedPrice;

    // First build payload for the details part
    const detailsPayload = buildDetailsPayload({ basicDetails, additionalDetails, tieredPricing });
    // Either create or update an existing product supplier details
    try {
      dispatch(showSpinner());
      const detailsResponse = productSupplierId
        ? await productSupplierApi.updateDetails(detailsPayload, productSupplierId)
        : await productSupplierApi.saveDetails(detailsPayload);

      // Id of created/updated product supplier
      const productSupplier = detailsResponse.data?.data?.id;

      // Build package and pricing payload and send a request
      const packagePayload = buildPackagePayload({
        packageSpecification,
        fixedPrice,
        productSupplier,
      });
      await productPackageApi.save(packagePayload);

      // Build preferences payload and if payload array is not empty, send a request
      const preferencesPayload = buildPreferencesPayload({
        defaultPreferenceType,
        productSupplierPreferences,
        productSupplier,
      });

      if (preferencesPayload.productSupplierPreferences?.length) {
        await productSupplierPreferenceApi.saveOrUpdateBatch(preferencesPayload);
      }

      // Build attributes payload and send a request
      const attributesPayload = buildAttributesPayload({ attributes, productSupplier });
      await productSupplierAttributeApi.updateAttributes(attributesPayload);

      // Show a success message and redirect to the list page
      const successMessage = productSupplierId
        ? translate('react.productSupplier.form.success.update', 'Product source has been updated successfully')
        : translate('react.productSupplier.form.success.create', 'Product source has been created successfully');
      notification(NotificationType.SUCCESS)({ message: successMessage });
      history.push(PRODUCT_SUPPLIER_URL.list());
    } finally {
      dispatch(hideSpinner());
    }
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
    getValues,
  };
};

export default useProductSupplierForm;
