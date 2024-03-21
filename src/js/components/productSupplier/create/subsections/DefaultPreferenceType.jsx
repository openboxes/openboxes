import React from 'react';

import PropTypes from 'prop-types';
import { Controller, useWatch } from 'react-hook-form';
import { RiDeleteBinLine } from 'react-icons/ri';
import { useSelector } from 'react-redux';
import { Tooltip } from 'react-tippy';

import DateField from 'components/form-elements/v2/DateField';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import Subsection from 'components/Layout/v2/Subsection';
import useDeletePreferenceType from 'hooks/productSupplier/form/useDeletePreferenceType';
import { FormErrorPropType } from 'utils/propTypes';
import Translate from 'utils/Translate';

const DefaultPreferenceType = ({
  control,
  errors,
  setValue,
  triggerValidation,
}) => {
  const {
    preferenceTypes,
  } = useSelector((state) => ({
    preferenceTypes: state.productSupplier.preferenceTypes,
  }));

  const updatedDefaultPreferenceType = useWatch({
    name: 'defaultPreferenceType',
    control,
  });

  const emptyPreferenceType = {
    comments: '',
    validityStartDate: null,
    validityEndDate: null,
    preferenceType: null,
  };

  const triggerValidationOnPreferenceType = () => {
    triggerValidation('defaultPreferenceType.preferenceType');
  };

  const afterDelete = () => {
    setValue('defaultPreferenceType', emptyPreferenceType);
    triggerValidationOnPreferenceType();
  };

  const {
    openConfirmationModal,
    isPreferenceTypeEmpty,
  } = useDeletePreferenceType({
    preferenceTypeData: updatedDefaultPreferenceType,
    isDefaultPreferenceType: true,
    afterDelete,
  });

  return (
    <Subsection
      collapsable={false}
      title={{
        label: 'react.productSupplier.subsection.defaultPreferenceType.title',
        defaultMessage: 'Default Preference Type',
      }}
    >
      <div className="row">
        <div className="col-lg col-md-6 px-2 pt-2">
          <Controller
            name="defaultPreferenceType.preferenceType"
            control={control}
            render={({ field }) => (
              <SelectField
                {...field}
                placeholder="Select"
                title={{
                  id: 'react.productSupplier.form.defaultPreferenceType.title',
                  defaultMessage: 'Default Preference Type',
                }}
                tooltip={{
                  id: 'react.productSupplier.form.defaultPreferenceType.tooltip',
                  defaultMessage: 'Company-wide purchasing preference for this supplier established through a competitive bid',
                }}
                options={preferenceTypes}
                hasErrors={Boolean(errors.preferenceType?.message)}
                errorMessage={errors.preferenceType?.message}
              />
            )}
          />
        </div>
        <div className="col-lg col-md-6 px-2 pt-2">
          <Controller
            name="defaultPreferenceType.validityStartDate"
            control={control}
            render={({ field }) => (
              <DateField
                title={{
                  id: 'react.productSupplier.form.validFrom.title',
                  defaultMessage: 'Valid From',
                }}
                placeholder={{
                  id: 'react.default.dateInput.placeholder.label',
                  default: 'Select a date',
                }}
                errorMessage={errors.validityStartDate?.message}
                {...field}
                onBlur={(e) => {
                  field?.onBlur?.(e);
                  triggerValidationOnPreferenceType();
                }}
              />
            )}
          />
        </div>
        <div className="col-lg col-md-6 px-2 pt-2">
          <Controller
            name="defaultPreferenceType.validityEndDate"
            control={control}
            render={({ field }) => (
              <DateField
                title={{
                  id: 'react.productSupplier.form.validUntil.title',
                  defaultMessage: 'Valid Until',
                }}
                placeholder={{
                  id: 'react.default.dateInput.placeholder.label',
                  default: 'Select a date',
                }}
                errorMessage={errors.validityEndDate?.message}
                {...field}
                onBlur={(e) => {
                  field?.onBlur?.(e);
                  triggerValidationOnPreferenceType();
                }}
              />
            )}
          />
        </div>
        <div className="col-lg col-md-6 p-2">
          <Controller
            name="defaultPreferenceType.comments"
            control={control}
            render={({ field }) => (
              <TextInput
                {...field}
                errorMessage={errors.comments?.message}
                title={{
                  id: 'react.productSupplier.form.bidName.title',
                  defaultMessage: 'Bid Name',
                }}
                tooltip={{
                  id: 'react.productSupplier.form.bidName.tooltip',
                  defaultMessage: 'The bid during which the purchasing preference was selected',
                }}
                onBlur={(e) => {
                  field?.onBlur?.(e);
                  triggerValidationOnPreferenceType();
                }}
              />
            )}
          />
        </div>
        <div className="p-2 d-flex align-items-center">
          <Tooltip
            className="d-flex align-items-center"
            html={(
              <span className="p-1">
                <Translate
                  id="react.productSupplier.form.deleteGlobalPreference"
                  defaultMessage="Delete global preference"
                />
              </span>
            )}
          >
            <RiDeleteBinLine
              onClick={() => !isPreferenceTypeEmpty && openConfirmationModal()}
              className={`preference-type-bin ${isPreferenceTypeEmpty ? 'disabled' : 'active'}`}
            />
          </Tooltip>
        </div>
      </div>
    </Subsection>
  );
};

export default DefaultPreferenceType;

export const defaultPreferenceTypeFormErrors = PropTypes.shape({
  preferenceType: FormErrorPropType,
  validityStartDate: FormErrorPropType,
  validityEndDate: FormErrorPropType,
  comments: FormErrorPropType,
});

DefaultPreferenceType.propTypes = {
  control: PropTypes.shape({}).isRequired,
  errors: defaultPreferenceTypeFormErrors,
  setValue: PropTypes.func.isRequired,
  triggerValidation: PropTypes.func.isRequired,
};

DefaultPreferenceType.defaultProps = {
  errors: {},
};
