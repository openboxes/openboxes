import React from 'react';

import PropTypes from 'prop-types';

import Section from 'components/Layout/v2/Section';
import PreferenceTypeVariations
  from 'components/productSupplier/create/subsections/PreferenceTypeVariations';

const PreferenceTypeSection = ({
  control,
  errors,
  trigger,
}) => (
  <Section title={{ label: 'react.productSupplier.section.preferenceType.title', defaultMessage: 'Preference Type' }}>
    <PreferenceTypeVariations
      control={control}
      errors={errors}
      trigger={trigger}
    />
  </Section>
);

export default PreferenceTypeSection;

PreferenceTypeSection.propTypes = {
  control: PropTypes.shape({}).isRequired,
  errors: PropTypes.arrayOf(
    PropTypes.shape({
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
    }),
  ).isRequired,
  trigger: PropTypes.func.isRequired,
};
