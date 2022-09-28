import React, { useEffect } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import { fetchShipmentStatusCodes } from 'actions';
import FilterForm from 'components/Filter/FilterForm';
import DateFilter from 'components/form-elements/DateFilter/DateFilter';
import FilterSelectField from 'components/form-elements/FilterSelectField';
import { debounceLocationsFetch, debounceUsersFetch } from 'utils/option-utils';

const filterFields = {
  receiptStatusCodes: {
    type: FilterSelectField,
    attributes: {
      multi: true,
      filterElement: true,
      placeholder: 'Receipt Status',
      showLabelTooltip: true,
      closeMenuOnSelect: false,
    },
    getDynamicAttr: ({ shipmentStatuses }) => ({
      options: shipmentStatuses,
    }),
  },
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
    getDynamicAttr: ({
      fetchLocations,
    }) => ({
      loadOptions: fetchLocations,
    }),
  },
  destination: {
    type: FilterSelectField,
    attributes: {
      valueKey: 'id',
      filterElement: true,
      placeholder: 'Destination',
      showLabelTooltip: true,
      disabled: true,
    },
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

const StockMovementInboundFilters = ({
  setFilterParams,
  debounceTime,
  minSearchLength,
  filterParams,
  fetchStatuses,
  shipmentStatuses,
  isShipmentStatusesFetched,
}) => {
  const fetchUsers = debounceUsersFetch(debounceTime, minSearchLength);
  const fetchLocations = debounceLocationsFetch(debounceTime, minSearchLength);


  useEffect(() => {
    if (!isShipmentStatusesFetched) fetchStatuses();
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
        onClear={form => form.reset({ destination: filterParams.destination })}
        onSubmit={values => setFilterParams({ ...values })}
        hidden={false}
        formProps={{
          shipmentStatuses,
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
  shipmentStatuses: state.shipmentStatuses.data,
  isShipmentStatusesFetched: state.shipmentStatuses.fetched,
});

const mapDispatchToProps = {
  fetchStatuses: fetchShipmentStatusCodes,
};

export default connect(mapStateToProps, mapDispatchToProps)(StockMovementInboundFilters);

StockMovementInboundFilters.defaultProps = {
  filterParams: {},
};

StockMovementInboundFilters.propTypes = {
  setFilterParams: PropTypes.func.isRequired,
  fetchStatuses: PropTypes.func.isRequired,
  debounceTime: PropTypes.number.isRequired,
  isShipmentStatusesFetched: PropTypes.bool.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  filterParams: PropTypes.arrayOf({}),
  shipmentStatuses: PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    variant: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
  }).isRequired,
};
