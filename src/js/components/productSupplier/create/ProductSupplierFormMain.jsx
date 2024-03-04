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
    setProductPackageQuantity,
  } = formProps;

  return (
    <div className="d-flex gap-12 flex-column">
      <DetailsSection
        control={control}
        errors={errors}
      />
      <PreferenceTypeSection
        control={control}
        errors={errors}
        triggerValidation={triggerValidation}
      />
      <PricingSection
        control={control}
        errors={errors}
        setProductPackageQuantity={setProductPackageQuantity}
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
      defaultPreferenceType: PropTypes.shape({
        message: PropTypes.string,
      }),
      validFrom: PropTypes.shape({
        message: PropTypes.string,
      }),
      validUntil: PropTypes.shape({
        message: PropTypes.string,
      }),
      bidName: PropTypes.shape({
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
    triggerValidation: PropTypes.func.isRequired,
    setProductPackageQuantity: PropTypes.func.isRequired,
  }).isRequired,
};
