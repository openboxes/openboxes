import React from 'react';

import PropTypes from 'prop-types';

import DetailsSection from 'components/productSupplier/create/sections/DetailsSection';
import PreferenceTypeSection from 'components/productSupplier/create/sections/PreferenceTypeSection';
import PricingSection from 'components/productSupplier/create/sections/PricingSection';

import './styles.scss';

const ProductSupplierFormMain = ({ formProps }) => {
  const {
    control,
    errors,
    triggerValidation,
    ratingTypeCodes,
  } = formProps;

  return (
    <div className="d-flex gap-12 flex-column">
      <DetailsSection
        control={control}
        errors={errors}
        ratingTypeCodes={ratingTypeCodes}
      />
      <PreferenceTypeSection
        control={control}
        errors={errors.productSupplierPreferences}
        triggerValidation={triggerValidation}
      />
      <PricingSection
        control={control}
        errors={errors}
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
      supplier: PropTypes.shape({
        message: PropTypes.string,
      }),
      name: PropTypes.shape({
        message: PropTypes.string,
      }),
      supplierCode: PropTypes.shape({
        message: PropTypes.string,
      }),
      product: PropTypes.shape({
        message: PropTypes.string,
      }),
      uom: PropTypes.shape({
        message: PropTypes.string,
      }),
      productPackageQuantity: PropTypes.shape({
        message: PropTypes.string,
      }),
      minOrderQuantity: PropTypes.shape({
        message: PropTypes.string,
      }),
      productPackagePrice: PropTypes.shape({
        message: PropTypes.string,
      }),
      eachPrice: PropTypes.shape({
        message: PropTypes.string,
      }),
      productSupplierPreferences: PropTypes.arrayOf(PropTypes.shape({
        destinationParty: PropTypes.shape({
          message: PropTypes.string,
        }),
        preferenceType: PropTypes.shape({
          message: PropTypes.string,
        }),
        validityStartDate: PropTypes.shape({
          message: PropTypes.string,
        }),
        validityEndDate: PropTypes.shape({
          message: PropTypes.string,
        }),
        bidName: PropTypes.shape({
          message: PropTypes.string,
        }),
      })),
    }),
    ratingTypeCodes: PropTypes.shape({
      id: PropTypes.string.isRequired,
      value: PropTypes.string.isRequired,
      label: PropTypes.string.isRequired,
    }).isRequired,
    triggerValidation: PropTypes.func.isRequired,
  }).isRequired,
};
