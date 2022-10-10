import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import FilterForm from 'components/Filter/FilterForm';
import DateFilter from 'components/form-elements/DateFilter/DateFilter';
import FilterSelectField from 'components/form-elements/FilterSelectField';
import { debounceUsersFetch } from 'utils/option-utils';

const filterFields = {
  status: {
    type: FilterSelectField,
    attributes: {
      valueKey: 'id',
      filterElement: true,
      placeholder: 'Status',
      showLabelTooltip: true,
      multi: true,
      closeMenuOnSelect: false,
    },
    getDynamicAttr: ({ statuses }) => ({
      options: statuses,
    }),
  },
  createdBy: {
    type: FilterSelectField,
    attributes: {
      async: true,
      openOnClick: false,
      autoload: false,
      cache: false,
      valueKey: 'id',
      labelKey: 'name',
      options: [],
      filterOptions: options => options,
      filterElement: true,
      placeholder: 'Created by',
      showLabelTooltip: true,
    },
    getDynamicAttr: ({
      debouncedUsersFetch,
    }) => ({
      loadOptions: debouncedUsersFetch,
    }),
  },
  lastUpdatedStartDate: {
    type: DateFilter,
    attributes: {
      label: 'react.stockTransfer.lastUpdateAfter.label',
      defaultMessage: 'Last update after',
      dateFormat: 'MM/DD/YYYY',
      filterElement: true,
    },
  },
  lastUpdatedEndDate: {
    type: DateFilter,
    attributes: {
      label: 'react.stockTransfer.lastUpdateBefore.label',
      defaultMessage: 'Last update before',
      dateFormat: 'MM/DD/YYYY',
      filterElement: true,
    },
  },
};


const StockTransferListFilters = ({
  setFilterParams,
  debounceTime,
  minSearchLength,
  statuses,
  currentLocation,
}) => {
  const [defaultValues, setDefaultValues] = useState({});

  useEffect(() => {
    if (currentLocation?.id) {
      const initialEmptyValues = Object.keys(filterFields).reduce((acc, key) => {
        if (!acc[key]) return { ...acc, [key]: '' };
        return acc;
      }, {});
      setDefaultValues({
        ...initialEmptyValues,
      });
    }
  }, [currentLocation?.id]);
  const debouncedUsersFetch = debounceUsersFetch(debounceTime, minSearchLength);

  return (
    <div className="d-flex flex-column list-page-filters">
      <FilterForm
        filterFields={filterFields}
        updateFilterParams={values => setFilterParams({ ...values })}
        formProps={{
          debouncedUsersFetch,
          statuses,
        }}
        defaultValues={defaultValues}
        searchFieldPlaceholder="Search by transfer number"
        searchFieldId="q"
        allowEmptySubmit
        hidden={false}
      />
    </div>
  );
};

const mapStateToProps = state => ({
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
  statuses: state.stockTransfer.statuses,
  currentLocation: state.session.currentLocation,
});


export default connect(mapStateToProps)(StockTransferListFilters);

StockTransferListFilters.propTypes = {
  setFilterParams: PropTypes.func.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  statuses: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    variant: PropTypes.string.isRequired,
  })).isRequired,
  currentLocation: PropTypes.shape({}).isRequired,
};
