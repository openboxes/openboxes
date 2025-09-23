import React from 'react';

import PropTypes from 'prop-types';

import AttributesSection from 'components/productSupplier/create/sections/AttributesSection';
import DetailsSection from 'components/productSupplier/create/sections/DetailsSection';
import PreferenceTypeSection from 'components/productSupplier/create/sections/PreferenceTypeSection';
import PricingSection from 'components/productSupplier/create/sections/PricingSection';
import {
  additionalDetailsFormErrors,
} from 'components/productSupplier/create/subsections/AdditionalDetails';
import { basicDetailsFormErrors } from 'components/productSupplier/create/subsections/BasicDetails';
import {
  defaultPreferenceTypeFormErrors,
} from 'components/productSupplier/create/subsections/DefaultPreferenceType';
import { fixedPriceFormErrors } from 'components/productSupplier/create/subsections/FixedPrice';
import {
  packageSpecificationFormErrors,
} from 'components/productSupplier/create/subsections/PackageSpecification';
import {
  preferenceTypeVariationsFormErrors,
} from 'components/productSupplier/create/subsections/PreferenceTypeVariations';
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
        triggerValidation={triggerValidation}
        setProductPackageQuantity={setProductPackageQuantity}
      />
      <AttributesSection
        control={control}
        errors={errors.attributes}
        setValue={setValue}
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
      basicDetails: basicDetailsFormErrors,
      additionalDetails: additionalDetailsFormErrors,
      defaultPreferenceType: defaultPreferenceTypeFormErrors,
      productSupplierPreferences: preferenceTypeVariationsFormErrors,
      packageSpecification: packageSpecificationFormErrors,
      fixedPrice: fixedPriceFormErrors,
      attributes: PropTypes.objectOf(FormErrorPropType),
    }),
    triggerValidation: PropTypes.func.isRequired,
    setProductPackageQuantity: PropTypes.func.isRequired,
    setValue: PropTypes.func.isRequired,
  }).isRequired,
};
