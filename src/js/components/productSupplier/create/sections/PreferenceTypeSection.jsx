import React from 'react';

import PropTypes from 'prop-types';

import Section from 'components/Layout/v2/Section';
import DefaultPreferenceType
  from 'components/productSupplier/create/subsections/DefaultPreferenceType';
import PreferenceTypeVariations
  from 'components/productSupplier/create/subsections/PreferenceTypeVariations';

const PreferenceTypeSection = ({
  control,
  errors,
  triggerValidation,
}) => (
  <Section title={{ label: 'react.productSupplier.section.preferenceType.title', defaultMessage: 'Preference Type' }}>
    <DefaultPreferenceType
      control={control}
      errors={errors}
    />
    <PreferenceTypeVariations
      control={control}
      errors={errors.productSupplierPreferences}
      triggerValidation={triggerValidation}
    />
  </Section>
);

export default PreferenceTypeSection;

PreferenceTypeSection.propTypes = {
  control: PropTypes.shape({}).isRequired,
  errors: PropTypes.shape({
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
  }).isRequired,
  triggerValidation: PropTypes.func.isRequired,
};
