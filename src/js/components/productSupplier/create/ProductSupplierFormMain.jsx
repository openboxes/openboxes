import React from 'react';

import PropTypes from 'prop-types';

import DetailsSection from 'components/productSupplier/create/sections/DetailsSection';
import PreferenceTypeSection
  from 'components/productSupplier/create/sections/PreferenceTypeSection';

import './styles.scss';

const ProductSupplierFormMain = ({ formProps }) => {
  const {
    control,
    errors,
    mockedRatingTypeCodes,
  } = formProps;

  return (
    <div>
      <DetailsSection
        control={control}
        errors={errors}
        mockedRatingTypeCodes={mockedRatingTypeCodes}
      />
      <PreferenceTypeSection
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
    }),
    mockedRatingTypeCodes: PropTypes.shape({
      id: PropTypes.string.isRequired,
      value: PropTypes.string.isRequired,
      label: PropTypes.string.isRequired,
    }).isRequired,
  }).isRequired,
};
