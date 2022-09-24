import React from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import { fetchBuyers, fetchPurchaseOrderStatuses } from 'actions';
import FilterForm from 'components/Filter/FilterForm';
import CheckboxField from 'components/form-elements/CheckboxField';
import FilterSelectField from 'components/form-elements/FilterSelectField';
import { debounceLocationsFetch } from 'utils/option-utils';

const filterFields = {
  origin: {
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
      placeholder: 'Origin',
      showLabelTooltip: true,
    },
    getDynamicAttr: ({ debouncedLocationsFetch }) => ({
      loadOptions: debouncedLocationsFetch,
    }),
  },
  destination: {
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
      placeholder: 'Destination',
      showLabelTooltip: true,
    },
    getDynamicAttr: ({ debouncedLocationsFetch }) => ({
      loadOptions: debouncedLocationsFetch,
    }),
  },
  isPublished: {
    type: CheckboxField,
    label: 'react.stocklists.includeUnpublished.label',
    defaultMessage: 'Include unpublished stocklists',
    attributes: {
      filterElement: true,
    },
  },
};

const StockListFilters = ({ setFilterParams, debounceTime, minSearchLength }) => {
  const debouncedLocationsFetch = debounceLocationsFetch(debounceTime, minSearchLength);

  return (
    <div className="d-flex flex-column list-page-filters">
      <FilterForm
        searchFieldId="q"
        filterFields={filterFields}
        onSubmit={values => setFilterParams(values)}
        formProps={{ debouncedLocationsFetch }}
      />
    </div>
  );
};

const mapStateToProps = state => ({
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
});

const mapDispatchToProps = {
  fetchStatuses: fetchPurchaseOrderStatuses,
  fetchBuyerOrganizations: fetchBuyers,
};

export default connect(mapStateToProps, mapDispatchToProps)(StockListFilters);

StockListFilters.propTypes = {
  setFilterParams: PropTypes.func.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
};
