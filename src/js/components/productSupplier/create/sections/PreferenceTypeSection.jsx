import React from 'react';

import PropTypes from 'prop-types';

import Section from 'components/Layout/v2/Section';
import DefaultPreferenceType, { defaultPreferenceTypeFormErrors }
  from 'components/productSupplier/create/subsections/DefaultPreferenceType';
import PreferenceTypeVariations, { preferenceTypeVariationsFormErrors }
  from 'components/productSupplier/create/subsections/PreferenceTypeVariations';

const PreferenceTypeSection = ({
  control,
  errors,
  triggerValidation,
  setValue,
}) => (
  <Section title={{ label: 'react.productSupplier.section.preferenceType.title', defaultMessage: 'Preference Type' }}>
    <DefaultPreferenceType
      control={control}
      errors={errors?.defaultPreferenceType}
      setValue={setValue}
      triggerValidation={triggerValidation}
    />
    <PreferenceTypeVariations
      control={control}
      errors={errors?.productSupplierPreferences}
      triggerValidation={triggerValidation}
    />
  </Section>
);

export default PreferenceTypeSection;

PreferenceTypeSection.propTypes = {
  control: PropTypes.shape({}).isRequired,
  errors: PropTypes.shape({
    defaultPreferenceType: defaultPreferenceTypeFormErrors,
    productSupplierPreferences: preferenceTypeVariationsFormErrors,
  }).isRequired,
  triggerValidation: PropTypes.func.isRequired,
  setValue: PropTypes.func.isRequired,
};
