import React, { useEffect, useMemo } from 'react';

import { Controller } from 'react-hook-form';
import { RiDeleteBinLine, RiErrorWarningLine } from 'react-icons/ri';
import { getTranslate } from 'react-localize-redux';
import { useDispatch, useSelector } from 'react-redux';

import { fetchBuyers, fetchPreferenceTypes } from 'actions';
import DateField from 'components/form-elements/v2/DateField';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';
import _ from 'lodash';

const usePreferenceTypeVariationsColumns = ({
  errors,
  control,
  remove,
  updatedRows,
  trigger,
}) => {
  const dispatch = useDispatch();
  const {
    preferenceTypes,
    buyers,
    translate,
  } = useSelector((state) => ({
    preferenceTypes: state.productSupplier.preferenceTypes,
    buyers: state.organizations.buyers,
    translate: translateWithDefaultMessage(getTranslate(state.localize)),
  }));

  useEffect(() => {
    dispatch(fetchPreferenceTypes());
    dispatch(fetchBuyers());
  }, []);

  const isFieldValid = (index, property) => {
    const hasFieldErrors = errors?.[index]?.[property]?.message
                   || errors?.[index]?.[property]?.id?.message;
    const isRowDirty = _.some(Object.values(updatedRows?.[index] || {}));

    if (!isRowDirty) {
      return true;
    }

    return !hasFieldErrors;
  };

  const getCustomSelectErrorPlaceholder = ({
    id,
    defaultMessage,
    displayIcon,
  }) => (
    <div className="custom-select-error-placeholder">
      {displayIcon && <RiErrorWarningLine />}
      <span>
        <Translate
          id={id}
          defaultMessage={defaultMessage}
        />
      </span>
    </div>
  );

  const columns = useMemo(() => [
    {
      Header: translate('react.productSupplier.table.siteName.label', 'Site Name'),
      sortable: false,
      accessor: 'destinationParty',
      minWidth: 300,
      Cell: (row) => (
        <Controller
          key={row.original.id}
          name={`productSupplierPreferences.${row.index}.destinationParty`}
          control={control}
          render={({ field }) => {
            const hasErrors = !isFieldValid(row.index, 'destinationParty');
            return (
              <SelectField
                options={buyers}
                errorMessage={hasErrors}
                placeholder={getCustomSelectErrorPlaceholder(
                  {
                    id: 'react.productSupplier.table.selectSite.label',
                    defaultMessage: 'Select Site',
                    displayIcon: hasErrors,
                  },
                )}
                displayErrorMessage={false}
                {...field}
                onChange={(val) => {
                  field?.onChange(val);
                  trigger();
                }}
              />
            );
          }}
        />
      ),
    },
    {
      Header: translate('react.productSupplier.table.preferenceType.label', 'Preference Type'),
      sortable: false,
      accessor: 'preferenceType',
      minWidth: 300,
      Cell: (row) => {
        const hasErrors = !isFieldValid(row.index, 'preferenceType');
        return (
          <Controller
            name={`productSupplierPreferences.${row.index}.preferenceType`}
            control={control}
            key={row.original.id}
            render={({ field }) => (
              <SelectField
                options={preferenceTypes}
                errorMessage={hasErrors}
                placeholder={getCustomSelectErrorPlaceholder({
                  id: 'react.productSupplier.table.selectPreferenceType.label',
                  defaultMessage: 'Select Preference Type',
                  displayIcon: hasErrors,
                })}
                displayErrorMessage={false}
                {...field}
                onChange={(val) => {
                  field?.onChange(val);
                  trigger();
                }}
              />
            )}
          />
        );
      },
    },
    {
      Header: translate('react.productSupplier.table.validEndDate.label', 'Valid End Date'),
      sortable: false,
      accessor: 'validityEndDate',
      minWidth: 179,
      Cell: (row) => (
        <Controller
          name={`productSupplierPreferences.${row.index}.validityEndDate`}
          control={control}
          key={row.original.id}
          render={({ field }) => (
            <DateField
              errorMessage={errors?.[row.index]?.validityEndDate?.message}
              {...field}
              onChange={(val) => {
                field?.onChange(val);
                trigger();
              }}
            />
          )}
        />
      ),
    },
    {
      Header: translate('react.productSupplier.table.validStartDate.label', 'Valid Start Date'),
      sortable: false,
      accessor: 'validityStartDate',
      style: { overflow: 'visible' },
      minWidth: 179,
      Cell: (row) => (
        <Controller
          name={`productSupplierPreferences.${row.index}.validityStartDate`}
          control={control}
          key={row.original.id}
          render={({ field }) => (
            <DateField
              errorMessage={errors?.[row.index]?.validityStartDate?.message}
              {...field}
              onChange={(val) => {
                field?.onChange(val);
                trigger();
              }}
            />
          )}
        />
      ),
    },
    {
      Header: translate('react.productSupplier.table.bidName.label', 'Bid Name'),
      sortable: false,
      accessor: 'bidName',
      minWidth: 300,
      Cell: (row) => (
        <Controller
          name={`productSupplierPreferences.${row.index}.bidName`}
          control={control}
          key={row.original.id}
          render={({ field }) => (
            <TextInput
              errorMessage={errors?.[row.index]?.bidName?.message}
              {...field}
              onChange={(val) => {
                field?.onChange(val);
                trigger();
              }}
            />
          )}
        />
      ),
    },
    {
      Header: translate('react.productSupplier.table.actions.label', 'Actions'),
      sortable: false,
      minWidth: 70,
      headerClassName: 'justify-content-center',
      className: 'd-flex justify-content-center align-items-center',
      Cell: (row) => (
        <RiDeleteBinLine
          onClick={() => remove(row.index)}
          className="preference-type-variations-bin"
        />
      ),
    },
  ], [errors, buyers, preferenceTypes]);

  return { columns, translate };
};

export default usePreferenceTypeVariationsColumns;
