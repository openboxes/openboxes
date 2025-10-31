import React from 'react';

import PropTypes from 'prop-types';
import { useFieldArray, useWatch } from 'react-hook-form';
import { RiAddLine } from 'react-icons/ri';

import DataTable from 'components/DataTable';
import Button from 'components/form-elements/Button';
import Subsection from 'components/Layout/v2/Subsection';
import InvalidItemsIndicator from 'components/productSupplier/create/InvalidItemsIndicator';
import usePreferenceTypeVariationsColumns
  from 'hooks/productSupplier/form/usePreferenceTypeVariationsColumns';
import usePreferenceTypeVariationsFiltering
  from 'hooks/productSupplier/form/usePreferenceTypeVariationsFiltering';
import useResetScrollbar from 'hooks/useResetScrollbar';
import useTranslate from 'hooks/useTranslate';
import { FormErrorPropType } from 'utils/propTypes';

const PreferenceTypeVariations = ({
  control,
  errors,
  triggerValidation,
}) => {
  const { fields, remove, prepend } = useFieldArray({
    control,
    name: 'productSupplierPreferences',
  });

  const updatedRows = useWatch({
    name: 'productSupplierPreferences',
    control,
  });

  const { resetScrollbar } = useResetScrollbar({
    selector: '.rt-table',
  });

  const {
    isFiltered,
    setIsFiltered,
    invalidRowCount,
    tablePageSize,
    getFilterMethod,
    triggerFiltering,
  } = usePreferenceTypeVariationsFiltering({ errors, updatedRows });

  const translate = useTranslate();

  const { columns } = usePreferenceTypeVariationsColumns({
    errors,
    control,
    remove,
    updatedRows,
    triggerValidation,
  });

  const defaultTableRow = {
    destinationParty: '',
    preferenceType: '',
    validityStartDate: null,
    validityEndDate: null,
    comments: '',
  };

  const addNewLine = () => {
    prepend(defaultTableRow);
    triggerValidation('productSupplierPreferences');
    resetScrollbar();
  };

  return (
    <Subsection
      expandedByDefault={false}
      collapsable
      title={{
        label: 'react.productSupplier.subsection.preferenceTypeVariations.title',
        defaultMessage: 'Preference Type Variations',
      }}
    >
      <div className="preference-type-variations-subsection">
        <div className="d-flex justify-content-end align-items-center mb-3">
          <InvalidItemsIndicator
            className="mr-3"
            errorsCounter={invalidRowCount}
            isFiltered={isFiltered}
            setIsFiltered={setIsFiltered}
            triggerValidation={triggerValidation}
            handleOnFilterButtonClick={() => {
              triggerValidation('productSupplierPreferences');
              if (invalidRowCount) {
                setIsFiltered((value) => !value);
                resetScrollbar();
              }
            }}
          />
          <Button
            onClick={addNewLine}
            StartIcon={<RiAddLine className="button-add-icon" />}
            defaultLabel="Add new"
            label="react.productSupplier.table.addNew.label"
          />
        </div>
        <DataTable
          data={fields}
          columns={columns}
          defaultPageSize={4}
          pageSize={tablePageSize}
          showPagination={false}
          loading={false}
          filterAll
          defaultFilterMethod={getFilterMethod}
          filtered={triggerFiltering()}
          noDataText={translate(
            'react.productSupplier.table.empty.label',
            'No Preference Type Variations to display',
          )}
        />
      </div>
    </Subsection>
  );
};

export default PreferenceTypeVariations;

export const preferenceTypeVariationsFormErrors = PropTypes.arrayOf(
  PropTypes.shape({
    destinationParty: FormErrorPropType,
    preferenceType: FormErrorPropType,
    validityStartDate: FormErrorPropType,
    validityEndDate: FormErrorPropType,
    comments: FormErrorPropType,
  }),
);

PreferenceTypeVariations.propTypes = {
  control: PropTypes.shape({}).isRequired,
  errors: preferenceTypeVariationsFormErrors,
  triggerValidation: PropTypes.func.isRequired,
};

PreferenceTypeVariations.defaultProps = {
  errors: [],
};
