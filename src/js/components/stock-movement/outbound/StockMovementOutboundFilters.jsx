import React, { useEffect } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import { fetchRequisitionStatusCodes } from 'actions';
import FilterForm from 'components/Filter/FilterForm';
import { debounceLocationsFetch, debounceUsersFetch } from 'utils/option-utils';

const StockMovementOutboundFilters = ({
  setFilterParams,
  debounceTime,
  minSearchLength,
  fetchStatuses,
  requisitionStatuses,
  isRequisitionStatusesFetched,
  isRequestsOpen,
  filterFields,
  defaultValues,
}) => {
  const fetchUsers = debounceUsersFetch(debounceTime, minSearchLength);
  // eslint-disable-next-line max-len
  const fetchLocations = debounceLocationsFetch(
    debounceTime,
    minSearchLength,
    [],
    true,
    false,
    false,
  );

  useEffect(() => {
    if (!isRequisitionStatusesFetched || requisitionStatuses.length === 0) {
      fetchStatuses();
    }
  }, []);

  return (
    <div className="d-flex flex-column list-page-filters">
      <FilterForm
        searchFieldId="q"
        searchFieldPlaceholder="Search by order number of description"
        filterFields={filterFields}
        defaultValues={defaultValues}
        onClear={form => form.reset({
          origin: defaultValues.origin,
          sourceType: isRequestsOpen ? defaultValues.sourceType : null,
        })}
        updateFilterParams={values => setFilterParams({ ...values })}
        hidden={false}
        formProps={{
          requisitionStatuses,
          fetchUsers,
          fetchLocations,
        }}
      />
    </div>
  );
};

const mapStateToProps = state => ({
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
  requisitionStatuses: state.requisitionStatuses.data,
  isRequisitionStatusesFetched: state.requisitionStatuses.fetched,
});

const mapDispatchToProps = {
  fetchStatuses: fetchRequisitionStatusCodes,
};

export default connect(mapStateToProps, mapDispatchToProps)(StockMovementOutboundFilters);

StockMovementOutboundFilters.propTypes = {
  setFilterParams: PropTypes.func.isRequired,
  fetchStatuses: PropTypes.func.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  isRequisitionStatusesFetched: PropTypes.bool.isRequired,
  isRequestsOpen: PropTypes.bool.isRequired,
  requisitionStatuses: PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    variant: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
  }).isRequired,
  filterFields: PropTypes.shape({}).isRequired,
  defaultValues: PropTypes.shape({}).isRequired,
};
