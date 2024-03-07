import React from 'react';

import PropTypes from 'prop-types';

import Section from 'components/Layout/v2/Section';
import DefaultPreferenceType
  from 'components/productSupplier/create/subsections/DefaultPreferenceType';
import PreferenceTypeVariations
  from 'components/productSupplier/create/subsections/PreferenceTypeVariations';
import { FormErrorPropType } from 'utils/propTypes';

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
  }).isRequired,
  triggerValidation: PropTypes.func.isRequired,
  setValue: PropTypes.func.isRequired,
};
