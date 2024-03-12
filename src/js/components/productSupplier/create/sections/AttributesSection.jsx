import React from 'react';

import PropTypes from 'prop-types';
import { Controller } from 'react-hook-form';

import Section from 'components/Layout/v2/Section';
import AddAttributeOptionModal from 'components/productSupplier/modals/AddAttributeOptionModal';
import useProductSupplierAttributes from 'hooks/productSupplier/form/useProductSupplierAttributes';
import { FormErrorPropType } from 'utils/propTypes';

const AttributesSection = ({ control, errors, setValue }) => {
  const {
    attributesWithInputTypes,
    isAttributeModalOpen,
    selectedAttribute,
    closeAttributeModal,
  } = useProductSupplierAttributes();

  return (
    <>
      <AddAttributeOptionModal
        isOpen={isAttributeModalOpen}
        close={closeAttributeModal}
        selectedAttribute={selectedAttribute}
        setValue={setValue}
      />
      <Section
        title={{ label: 'react.productSupplier.section.attributes.title', defaultMessage: 'Attributes' }}
        className="attributes-section"
      >
        <div className="form-grid-3 mt-4">
          {
            attributesWithInputTypes.map(({ attribute, inputParams, Input }) => {
              const errorMessage = errors?.[attribute?.id]?.message;
              return (
                <div
                  className="p-1"
                  key={`attributes.${attribute?.id}`}
                >
                  <Controller
                    name={`attributes.${attribute?.id}`}
                    key={`attributes.${attribute?.id}-input`}
                    control={control}
                    render={({ field }) => (
                      <Input
                        title={{ defaultMessage: attribute?.name }}
                        hasErrors={Boolean(errorMessage)}
                        errorMessage={errorMessage}
                        {...inputParams}
                        {...attribute}
                        {...field}
                      />
                    )}
                  />
                </div>
              );
            })
          }
        </div>
      </Section>
    </>

  );
};

export default AttributesSection;

AttributesSection.propTypes = {
  control: PropTypes.shape({}).isRequired,
  errors: PropTypes.objectOf(FormErrorPropType),
  setValue: PropTypes.func.isRequired,
};

AttributesSection.defaultProps = {
  errors: {},
};
