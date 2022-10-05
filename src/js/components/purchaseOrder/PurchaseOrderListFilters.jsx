import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import { fetchBuyers, fetchPurchaseOrderStatuses } from 'actions';
import FilterForm from 'components/Filter/FilterForm';
import DateFilter from 'components/form-elements/DateFilter';
import FilterSelectField from 'components/form-elements/FilterSelectField';
import { debounceLocationsFetch, debounceUsersFetch } from 'utils/option-utils';

const filterFields = {
  status: {
    type: FilterSelectField,
    attributes: {
      multi: true,
      filterElement: true,
      placeholder: 'Status',
      showLabelTooltip: true,
      closeMenuOnSelect: false,
    },
    getDynamicAttr: ({ statuses }) => ({
      options: statuses,
    }),
  },
  statusStartDate: {
    type: DateFilter,
    attributes: {
      label: 'react.purchaseOrder.lastUpdateAfter.label',
      defaultMessage: 'Last update after',
      dateFormat: 'MM/DD/YYYY',
      filterElement: true,
    },
  },
  statusEndDate: {
    type: DateFilter,
    attributes: {
      label: 'react.purchaseOrder.lastUpdateBefore.label',
      defaultMessage: 'Last update before',
      dateFormat: 'MM/DD/YYYY',
      filterElement: true,
    },
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
      placeholder: 'Supplier',
      showLabelTooltip: true,
    },
    getDynamicAttr: ({
      debouncedOriginLocationsFetch,
    }) => ({
      loadOptions: debouncedOriginLocationsFetch,
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
    getDynamicAttr: ({
      debouncedDestinationLocationsFetch,
    }) => ({
      loadOptions: debouncedDestinationLocationsFetch,
    }),
  },
  destinationParty: {
    type: FilterSelectField,
    attributes: {
      valueKey: 'id',
      filterElement: true,
      placeholder: 'Purchasing organization',
      showLabelTooltip: true,
    },
    getDynamicAttr: ({ buyers, isCentralPurchasingEnabled }) => ({
      options: buyers,
      disabled: isCentralPurchasingEnabled,
    }),
  },
  orderedBy: {
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
      placeholder: 'Ordered by',
      showLabelTooltip: true,
    },
    getDynamicAttr: ({
      debouncedUsersFetch,
    }) => ({
      loadOptions: debouncedUsersFetch,
    }),
  },
};

const PurchaseOrderListFilters = ({
  setFilterParams,
  debounceTime,
  minSearchLength,
  supportedActivities,
  currentLocation,
  statuses,
  fetchStatuses,
  buyers,
  fetchBuyerOrganizations,
}) => {
  // Purchasing organizations (organizations with ROLE_BUYER)
  const [defaultValues, setDefaultValues] = useState({});
  const isCentralPurchasingEnabled = supportedActivities.includes('ENABLE_CENTRAL_PURCHASING');

  const determineDefaultValues = () => {
    if (currentLocation?.id && buyers) {
      const initialEmptyValues = Object.keys(filterFields).reduce((acc, key) => {
        if (!acc[key]) return { ...acc, [key]: '' };
        return acc;
      }, {});
      // If central purchasing is enabled, set default purchasing org as currentLocation's org
      if (isCentralPurchasingEnabled) {
        setDefaultValues({
          ...initialEmptyValues,
          destinationParty: buyers.find(org => org.id === currentLocation?.organization?.id),
        });
        return;
      }
      // If central purchasing is not enabled, set default destination as currentLocation
      setDefaultValues({
        ...initialEmptyValues,
        destination: currentLocation,
      });
    }
  };

  useEffect(() => {
    // If statuses not yet in store, fetch them
    if (!statuses || statuses.length === 0) {
      fetchStatuses();
    }

    if (!buyers) {
      fetchBuyerOrganizations();
      return;
    }
    determineDefaultValues();
  }, [buyers, currentLocation?.id]);

  const debouncedOriginLocationsFetch = debounceLocationsFetch(
    debounceTime,
    minSearchLength,
    ['FULFILL_ORDER'],
    true,
    false,
    false,
  );
  const debouncedDestinationLocationsFetch = debounceLocationsFetch(
    debounceTime,
    minSearchLength,
    ['RECEIVE_STOCK'],
    true,
    false,
    false,
  );
  const debouncedUsersFetch = debounceUsersFetch(debounceTime, minSearchLength);

  return (
    <div className="d-flex flex-column list-page-filters">
      <FilterForm
        filterFields={filterFields}
        updateFilterParams={values => setFilterParams({ ...values })}
        formProps={{
          statuses,
          debouncedOriginLocationsFetch,
          debouncedDestinationLocationsFetch,
          debouncedUsersFetch,
          buyers,
          isCentralPurchasingEnabled,
        }}
        defaultValues={defaultValues}
        allowEmptySubmit
        searchFieldPlaceholder="Search by order number or name or supplier"
        hidden={false}
      />
    </div>
  );
};

const mapStateToProps = state => ({
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
  supportedActivities: state.session.supportedActivities,
  currentLocation: state.session.currentLocation,
  // All possible PO statuses from store
  statuses: state.purchaseOrder.statuses,
  buyers: state.organizations.buyers,
});

const mapDispatchToProps = {
  fetchStatuses: fetchPurchaseOrderStatuses,
  fetchBuyerOrganizations: fetchBuyers,
};

export default connect(mapStateToProps, mapDispatchToProps)(PurchaseOrderListFilters);

PurchaseOrderListFilters.propTypes = {
  setFilterParams: PropTypes.func.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  supportedActivities: PropTypes.arrayOf(PropTypes.string).isRequired,
  currentLocation: PropTypes.shape({}).isRequired,
  statuses: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string,
    value: PropTypes.string,
    label: PropTypes.string,
    variant: PropTypes.string,
  })).isRequired,
  fetchStatuses: PropTypes.func.isRequired,
  buyers: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string,
    value: PropTypes.string,
    label: PropTypes.string,
    variant: PropTypes.string,
  })).isRequired,
  fetchBuyerOrganizations: PropTypes.func.isRequired,
};
