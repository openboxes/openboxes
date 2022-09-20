import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import FilterForm from 'components/Filter/FilterForm';
import DateField from 'components/form-elements/DateField';
import SelectField from 'components/form-elements/SelectField';
import apiClient from 'utils/apiClient';
import { debounceLocationsFetch, debounceUsersFetch, organizationsFetch } from 'utils/option-utils';

const filterFields = {
  status: {
    type: SelectField,
    label: 'react.purchaseOrder.status.label',
    defaultMessage: 'Status',
    attributes: {
      multi: true,
      filterElement: true,
    },
    getDynamicAttr: ({ statuses }) => ({
      options: statuses,
    }),
  },
  statusEndDate: {
    type: DateField,
    label: 'react.purchaseOrder.lastUpdateBefore.label',
    defaultMessage: 'Last update before',
    attributes: {
      dateFormat: 'MM/DD/YYYY',
      filterElement: true,
    },
  },
  statusStartDate: {
    type: DateField,
    label: 'react.purchaseOrder.lastUpdateAfter.label',
    defaultMessage: 'Last update after',
    attributes: {
      dateFormat: 'MM/DD/YYYY',
      filterElement: true,
    },
  },
  origin: {
    type: SelectField,
    label: 'react.purchaseOrder.supplier.label',
    defaultMessage: 'Supplier',
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
    },
    getDynamicAttr: ({
      debouncedOriginLocationsFetch,
    }) => ({
      loadOptions: debouncedOriginLocationsFetch,
    }),
  },
  destination: {
    type: SelectField,
    label: 'react.purchaseOrder.destination.label',
    defaultMessage: 'Destination',
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
    },
    getDynamicAttr: ({
      debouncedDestinationLocationsFetch,
    }) => ({
      loadOptions: debouncedDestinationLocationsFetch,
    }),
  },
  destinationParty: {
    type: SelectField,
    label: 'react.purchaseOrder.purchasingOrganization.label',
    defaultMessage: 'Purchasing organization',
    attributes: {
      valueKey: 'id',
      filterElement: true,
    },
    getDynamicAttr: ({ purchasingOrganizations, isCentralPurchasingEnabled }) => ({
      options: purchasingOrganizations,
      disabled: isCentralPurchasingEnabled,
    }),
  },
  orderedBy: {
    type: SelectField,
    label: 'react.purchaseOrder.orderedBy.label',
    defaultMessage: 'Ordered by',
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
    },
    getDynamicAttr: ({
      debouncedUsersFetch,
    }) => ({
      loadOptions: debouncedUsersFetch,
    }),
  },
};

const PurchaseOrderListFilters = ({
  setFilterParams, debounceTime, minSearchLength, supportedActivities, currentLocation,
}) => {
  // All possible PO statuses fetched from api
  const [statuses, setStatuses] = useState([]);
  // Purchasing organizations (organizations with ROLE_BUYER)
  const [purchasingOrganizations, setPurchasingOrganizations] = useState([]);
  const [defaultValues, setDefaultValues] = useState({});
  const isCentralPurchasingEnabled = supportedActivities.includes('ENABLE_CENTRAL_PURCHASING');

  useEffect(() => {
    apiClient.get('/openboxes/api/orderSummaryStatus')
      .then((res) => {
        setStatuses(res.data.data);
      });

    organizationsFetch(['ROLE_BUYER'])
      .then((organizations) => {
        setPurchasingOrganizations(organizations);
        // If central purchasing is enabled, set default purchasing org as currentLocation's org
        if (isCentralPurchasingEnabled) {
          setDefaultValues({
            destinationParty: organizations.find(org => org.id === currentLocation.organization.id),
          });
          return;
        }
        // If central purchasing is not enabled, set default destination as currentLocation
        setDefaultValues({
          destination: currentLocation,
        });
      });
  }, []);

  const debouncedOriginLocationsFetch = debounceLocationsFetch(
    debounceTime,
    minSearchLength,
    ['FULFILL_ORDER'],
  );
  const debouncedDestinationLocationsFetch = debounceLocationsFetch(
    debounceTime,
    minSearchLength,
    ['RECEIVE_STOCK'],
  );
  const debouncedUsersFetch = debounceUsersFetch(debounceTime, minSearchLength);

  return (
    <div className="d-flex flex-column purchase-order-list-filters">
      <FilterForm
        filterFields={filterFields}
        onSubmit={values => setFilterParams(values)}
        formProps={{
          statuses,
          debouncedOriginLocationsFetch,
          debouncedDestinationLocationsFetch,
          debouncedUsersFetch,
          purchasingOrganizations,
          isCentralPurchasingEnabled,
        }}
        defaultValues={defaultValues}
      />
    </div>
  );
};

const mapStateToProps = state => ({
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
  supportedActivities: state.session.supportedActivities,
  currentLocation: state.session.currentLocation,
});

export default connect(mapStateToProps)(PurchaseOrderListFilters);

PurchaseOrderListFilters.propTypes = {
  setFilterParams: PropTypes.func.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  supportedActivities: PropTypes.arrayOf(PropTypes.string).isRequired,
  currentLocation: PropTypes.shape({}).isRequired,
};
