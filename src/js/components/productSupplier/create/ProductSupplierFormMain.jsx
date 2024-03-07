import React from 'react';

import PropTypes from 'prop-types';

import AttributesSection from 'components/productSupplier/create/sections/AttributesSection';
import DetailsSection from 'components/productSupplier/create/sections/DetailsSection';
import PreferenceTypeSection from 'components/productSupplier/create/sections/PreferenceTypeSection';
import PricingSection from 'components/productSupplier/create/sections/PricingSection';
import { FormErrorPropType } from 'utils/propTypes';

import './styles.scss';

const ProductSupplierFormMain = ({ formProps }) => {
  const {
    control,
    errors,
    triggerValidation,
    setProductPackageQuantity,
    setValue,
  } = formProps;

  return (
    <div className="d-flex gap-12 flex-column">
      <DetailsSection
        control={control}
        errors={{
          basicDetails: errors?.basicDetails,
          additionalDetails: errors?.additionalDetails,
        }}
      />
      <PreferenceTypeSection
        control={control}
        errors={{
          defaultPreferenceType: errors?.defaultPreferenceType,
          productSupplierPreferences: errors?.productSupplierPreferences,
        }}
        triggerValidation={triggerValidation}
        setValue={setValue}
      />
      <PricingSection
        control={control}
        errors={{
          packageSpecification: errors?.packageSpecification,
          fixedPrice: errors?.fixedPrice,
        }}
        setProductPackageQuantity={setProductPackageQuantity}
      />
      <AttributesSection
        control={control}
        errors={errors.attributes}
      />
    </div>
  );
};

export default ProductSupplierFormMain;

ProductSupplierFormMain.propTypes = {
  formProps: PropTypes.shape({
    control: PropTypes.shape({}).isRequired,
    handleSubmit: PropTypes.func.isRequired,
    errors: PropTypes.shape({
      basicDetails: PropTypes.shape({
        code: FormErrorPropType,
        product: FormErrorPropType,
        legacyCode: FormErrorPropType,
        supplier: FormErrorPropType,
        supplierCode: FormErrorPropType,
        name: FormErrorPropType,
        active: FormErrorPropType,
      }),
      additionalDetails: PropTypes.shape({
        manufacturer: FormErrorPropType,
        ratingTypeCode: FormErrorPropType,
        manufacturerCode: FormErrorPropType,
        brandName: FormErrorPropType,
      }),
      defaultPreferenceType: PropTypes.shape({
        preferenceType: FormErrorPropType,
        validityStartDate: FormErrorPropType,
        validityEndDate: FormErrorPropType,
        bidName: FormErrorPropType,
      }),
      productSupplierPreferences: PropTypes.arrayOf(PropTypes.shape({
        destinationParty: FormErrorPropType,
        preferenceType: FormErrorPropType,
        validityStartDate: FormErrorPropType,
        validityEndDate: FormErrorPropType,
        bidName: FormErrorPropType,
      })),
      packageSpecification: PropTypes.shape({
        uom: FormErrorPropType,
        productPackageQuantity: FormErrorPropType,
        minOrderQuantity: FormErrorPropType,
        productPackagePrice: FormErrorPropType,
        eachPrice: FormErrorPropType,
      }),
      fixedPrice: PropTypes.shape({
        contractPricePrice: FormErrorPropType,
        contractPriceValidUntil: FormErrorPropType,
        tieredPricing: FormErrorPropType,
      }),
      attributes: PropTypes.objectOf(FormErrorPropType),
    }),
    triggerValidation: PropTypes.func.isRequired,
    setProductPackageQuantity: PropTypes.func.isRequired,
    setValue: PropTypes.func.isRequired,
  }).isRequired,
};
