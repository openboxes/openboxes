import React, { useEffect } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import { fetchRequisitionStatusCodes } from 'actions';
import FilterForm from 'components/Filter/FilterForm';
import DateFilter from 'components/form-elements/DateFilter/DateFilter';
import FilterSelectField from 'components/form-elements/FilterSelectField';
import { debounceLocationsFetch, debounceUsersFetch } from 'utils/option-utils';

const filterFields = {
  requisitionStatusCode: {
    type: FilterSelectField,
    attributes: {
      multi: true,
      filterElement: true,
      placeholder: 'Requisition Status',
      showLabelTooltip: true,
      closeMenuOnSelect: false,
    },
    getDynamicAttr: ({ requisitionStatuses }) => ({
      options: requisitionStatuses,
    }),
  },
  origin: {
    type: FilterSelectField,
    attributes: {
      valueKey: 'id',
      filterElement: true,
      placeholder: 'Origin',
      showLabelTooltip: true,
      disabled: true,
    },
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
    getDynamicAttr: ({
      fetchLocations,
    }) => ({
      loadOptions: fetchLocations,
    }),
  },
  requestedBy: {
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
      placeholder: 'Requested By',
      showLabelTooltip: true,
    },
    getDynamicAttr: ({
      fetchUsers,
    }) => ({
      loadOptions: fetchUsers,
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
      placeholder: 'Created By',
      showLabelTooltip: true,
    },
    getDynamicAttr: ({
      fetchUsers,
    }) => ({
      loadOptions: fetchUsers,
    }),
  },
  updatedBy: {
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
      placeholder: 'Updated By',
      showLabelTooltip: true,
    },
    getDynamicAttr: ({
      fetchUsers,
    }) => ({
      loadOptions: fetchUsers,
    }),
  },
  requestType: {
    type: FilterSelectField,
    attributes: {
      openOnClick: false,
      options: [
        { label: 'STOCK', value: 'STOCK' },
        { label: 'ADHOC', value: 'ADHOC' },
      ],
      filterElement: true,
      placeholder: 'Request type',
      showLabelTooltip: true,
    },
  },
  createdAfter: {
    type: DateFilter,
    attributes: {
      label: 'react.stockMovement.filter.createdAfter.label',
      defaultMessage: 'Created after',
      dateFormat: 'MM/DD/YYYY',
      filterElement: true,
    },
  },
  createdBefore: {
    type: DateFilter,
    attributes: {
      label: 'react.stockMovement.filter.createdBefore.label',
      defaultMessage: 'Created before',
      dateFormat: 'MM/DD/YYYY',
      filterElement: true,
    },
  },
};

const StockMovementOutboundFilters = ({
  setFilterParams,
  debounceTime,
  minSearchLength,
  filterParams,
  fetchStatuses,
  requisitionStatuses,
  isRequisitionStatusesFetched,
  isRequestsOpen,
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
        defaultValues={{
          ...filterParams,
        }}
        onClear={form => form.reset({
          origin: filterParams.origin,
          sourceType: isRequestsOpen ? filterParams.sourceType : null,
        })}
        onSubmit={values => setFilterParams({ ...values })}
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

StockMovementOutboundFilters.defaultProps = {
  filterParams: {},
};

StockMovementOutboundFilters.propTypes = {
  setFilterParams: PropTypes.func.isRequired,
  fetchStatuses: PropTypes.func.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  filterParams: PropTypes.arrayOf({}),
  isRequisitionStatusesFetched: PropTypes.bool.isRequired,
  isRequestsOpen: PropTypes.bool.isRequired,
  requisitionStatuses: PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    variant: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
  }).isRequired,
};
