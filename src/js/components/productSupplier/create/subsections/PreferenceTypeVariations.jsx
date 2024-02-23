import React from 'react';

import _ from 'lodash';
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

const PreferenceTypeVariations = ({
  control,
  errors,
  trigger,
}) => {
  const { fields, remove, prepend } = useFieldArray({
    control,
    name: 'productSupplierPreferences',
  });

  const updatedRows = useWatch({
    name: 'productSupplierPreferences',
    control,
  });

  const {
    isFiltered,
    setIsFiltered,
    getFilterMethod,
    triggerFiltering,
    isRowValid,
  } = usePreferenceTypeVariationsFiltering({ errors, updatedRows });

  const { columns, translate } = usePreferenceTypeVariationsColumns({
    errors,
    control,
    remove,
    updatedRows,
    trigger,
  });

  const defaultTableRow = {
    destinationParty: '',
    preferenceType: '',
    validityStartDate: '',
    validityEndDate: '',
    bidName: '',
  };

  return (
    <Subsection
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
            errorsCounter={Object.keys(errors).filter(isRowValid).length}
            isFiltered={isFiltered}
            setIsFiltered={setIsFiltered}
            trigger={trigger}
          />
          <Button
            onClick={() => {
              prepend(defaultTableRow);
              trigger();
            }}
            StartIcon={<RiAddLine className="button-add-icon" />}
            defaultLabel="Add new"
            label="react.productSupplier.table.addNew.label"
          />
        </div>
        <DataTable
          data={fields}
          columns={columns}
          defaultPageSize={4}
          pageSize={fields.length <= 4 ? 4 : fields.length}
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

PreferenceTypeVariations.propTypes = {
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
  ),
  trigger: PropTypes.func.isRequired,
};

PreferenceTypeVariations.defaultProps = {
  errors: {},
};
