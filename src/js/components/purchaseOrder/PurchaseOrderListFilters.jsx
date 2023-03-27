import React, { useCallback } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import FilterForm from 'components/Filter/FilterForm';
import { debounceLocationsFetch, debouncePeopleFetch } from 'utils/option-utils';


const PurchaseOrderListFilters = ({
  setFilterParams,
  debounceTime,
  minSearchLength,
  filterFields,
  defaultValues,
  formProps,
  supportedActivities,
}) => {
  const debouncedOriginLocationsFetch = useCallback(debounceLocationsFetch(
    debounceTime,
    minSearchLength,
    ['FULFILL_ORDER'],
    true,
    false,
    false,
  ), [debounceTime, minSearchLength]);

  const debouncedDestinationLocationsFetch = useCallback(
    debounceLocationsFetch(
      debounceTime,
      minSearchLength,
      ['RECEIVE_STOCK'],
      true,
      false,
      false,
    ),
    [debounceTime, minSearchLength],
  );

  const debouncedPeopleFetch = useCallback(
    debouncePeopleFetch(debounceTime, minSearchLength),
    [debounceTime, minSearchLength],
  );

  const filtersToIgnore = supportedActivities.includes('ENABLE_CENTRAL_PURCHASING') ? ['destinationParty'] : ['destination'];

  return (
    <div className="d-flex flex-column list-page-filters">
      <FilterForm
        filterFields={filterFields}
        updateFilterParams={values => setFilterParams({ ...values })}
        formProps={{
          ...formProps,
          debouncedOriginLocationsFetch,
          debouncedDestinationLocationsFetch,
          debouncedPeopleFetch,
        }}
        ignoreClearFilters={filtersToIgnore}
        defaultValues={defaultValues}
        allowEmptySubmit
        searchFieldDefaultPlaceholder="Search by order number or name"
        searchFieldPlaceholder="react.purchaseOrder.searchField.placeholder.label"
        hidden={false}
      />
    </div>
  );
};

const mapStateToProps = state => ({
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
  supportedActivities: state.session.supportedActivities,
});

export default connect(mapStateToProps)(PurchaseOrderListFilters);

PurchaseOrderListFilters.propTypes = {
  setFilterParams: PropTypes.func.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  filterFields: PropTypes.shape({}).isRequired,
  defaultValues: PropTypes.shape({}).isRequired,
  formProps: PropTypes.shape({}).isRequired,
  supportedActivities: PropTypes.arrayOf(PropTypes.string).isRequired,
};
