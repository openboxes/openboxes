import React, { useEffect, useMemo, useState } from 'react';

import _ from 'lodash';
import { Controller } from 'react-hook-form';
import { RiDeleteBinLine, RiErrorWarningLine } from 'react-icons/ri';
import { useDispatch, useSelector } from 'react-redux';
import { Tooltip } from 'react-tippy';

import { fetchBuyers } from 'actions';
import DateField from 'components/form-elements/v2/DateField';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import useDeletePreferenceType from 'hooks/productSupplier/form/useDeletePreferenceType';
import useTranslate from 'hooks/useTranslate';
import Translate from 'utils/Translate';

const usePreferenceTypeVariationsColumns = ({
  errors,
  control,
  remove,
  updatedRows,
  triggerValidation,
}) => {
  const [selectedRowIndex, setSelectedRowIndex] = useState(null);
  const dispatch = useDispatch();
  const {
    preferenceTypes,
    buyers,
  } = useSelector((state) => ({
    preferenceTypes: state.productSupplier.preferenceTypes,
    buyers: state.organizations.buyers,
  }));

  const translate = useTranslate();

  useEffect(() => {
    dispatch(fetchBuyers());
  }, []);

  const isFieldValid = (index, property) => {
    const fieldError = errors?.[index]?.[property];
    const fieldErrorMessage = fieldError?.message || fieldError?.id.message;
    const isRowDirty = _.some(Object.values(updatedRows?.[index] || {}));

    if (!isRowDirty) {
      return true;
    }

    return !fieldErrorMessage;
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

  const afterDelete = () => {
    remove(selectedRowIndex);
    triggerValidation('productSupplierPreferences');
    setSelectedRowIndex(null);
  };

  const onCancel = () => {
    setSelectedRowIndex(null);
  };

  const {
    openConfirmationModal,
  } = useDeletePreferenceType({
    preferenceTypeData: updatedRows?.[selectedRowIndex],
    onCancel,
    afterDelete,
  });

  useEffect(() => {
    if (selectedRowIndex !== null) {
      openConfirmationModal();
    }
  }, [selectedRowIndex]);

  const columns = useMemo(() => [
    {
      Header: translate('react.productSupplier.table.organization.label', 'Organization'),
      sortable: false,
      accessor: 'destinationParty',
      minWidth: 276,
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
                hasErrors={hasErrors}
                showValueTooltip
                scrollableParentContainerClassName="rt-table"
                placeholder={getCustomSelectErrorPlaceholder(
                  {
                    id: 'react.productSupplier.table.selectOrganization.label',
                    defaultMessage: 'Select Organization',
                    displayIcon: hasErrors,
                  },
                )}
                {...field}
                onChange={(val) => {
                  field?.onChange(val);
                  triggerValidation('productSupplierPreferences');
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
      minWidth: 276,
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
                hasErrors={hasErrors}
                scrollableParentContainerClassName="rt-table"
                placeholder={getCustomSelectErrorPlaceholder({
                  id: 'react.productSupplier.table.selectPreferenceType.label',
                  defaultMessage: 'Select Preference Type',
                  displayIcon: hasErrors,
                })}
                {...field}
                onChange={(val) => {
                  field?.onChange(val);
                  triggerValidation('productSupplierPreferences');
                }}
              />
            )}
          />
        );
      },
    },
    {
      Header: translate('react.productSupplier.table.validStartDate.label', 'Valid Start Date'),
      sortable: false,
      accessor: 'validityStartDate',
      style: { overflow: 'visible' },
      minWidth: 157,
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
                triggerValidation('productSupplierPreferences');
              }}
            />
          )}
        />
      ),
    },
    {
      Header: translate('react.productSupplier.table.validEndDate.label', 'Valid End Date'),
      sortable: false,
      accessor: 'validityEndDate',
      minWidth: 157,
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
                triggerValidation('productSupplierPreferences');
              }}
            />
          )}
        />
      ),
    },
    {
      Header: translate('react.productSupplier.table.bidName.label', 'Bid Name'),
      sortable: false,
      accessor: 'comments',
      minWidth: 276,
      Cell: (row) => (
        <Controller
          name={`productSupplierPreferences.${row.index}.comments`}
          control={control}
          key={row.original.id}
          render={({ field }) => (
            <TextInput
              errorMessage={errors?.[row.index]?.comments?.message}
              {...field}
              onChange={(val) => {
                field?.onChange(val);
                triggerValidation('productSupplierPreferences');
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
        <Tooltip
          className="d-flex align-items-center"
          html={(
            <span className="p-1">
              <Translate
                id="react.productSupplier.form.delete"
                defaultMessage="Delete"
              />
            </span>
          )}
        >
          <RiDeleteBinLine
            onClick={() => {
              setSelectedRowIndex(row.index);
            }}
            className="preference-type-bin"
          />
        </Tooltip>
      ),
    },
  ], [errors, buyers, preferenceTypes]);

  return { columns };
};

export default usePreferenceTypeVariationsColumns;
