import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import { fetchTranslations } from 'actions';
import DateFilter from 'components/form-elements/DateFilter/DateFilter';
import FilterSelectField from 'components/form-elements/FilterSelectField';
import StockMovementOutboundFilters from 'components/stock-movement/outbound/StockMovementOutboundFilters';
import StockMovementOutboundHeader from 'components/stock-movement/outbound/StockMovementOutboundHeader';
import StockMovementOutboundTable from 'components/stock-movement/outbound/StockMovementOutboundTable';

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

const StockMovementOutboundList = (props) => {
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});

  useEffect(() => {
    props.fetchTranslations(props.locale, 'stockMovement');
    props.fetchTranslations(props.locale, 'StockMovementType');
  }, [props.locale]);

  useEffect(() => {
    // Avoid unnecessary re-fetches if getAppContext triggers fetching session info
    // but currentLocation doesn't change
    // eslint-disable-next-line max-len
    if (props.currentLocation?.id) {
      const initialEmptyValues = Object.keys(filterFields).reduce((acc, key) => {
        if (!acc[key]) return { ...acc, [key]: '' };
        return acc;
      }, {});
      setDefaultFilterValues({
        ...initialEmptyValues,
        origin: {
          id: props.currentLocation.id,
          value: props.currentLocation.id,
          name: props.currentLocation.name,
          label: props.currentLocation.name,
        },
        sourceType: props.isRequestsList ? 'ELECTRONIC' : null,
      });
    }
  }, [props.currentLocation?.id]);


  const selectFiltersForMyStockMovements = () => {
    const currentUserValue = {
      id: props.currentUser.id,
      value: props.currentUser.id,
      label: props.currentUser.name,
      name: props.currentUser.name,
    };
    setDefaultFilterValues(values => ({
      ...values,
      requestedBy: currentUserValue,
      createdBy: currentUserValue,
    }));
  };

  return (
    <div className="d-flex flex-column list-page-main">
      <StockMovementOutboundHeader
        isRequestsOpen={props.isRequestsList}
        showMyStockMovements={selectFiltersForMyStockMovements}
      />
      <StockMovementOutboundFilters
        isRequestsOpen={props.isRequestsList}
        defaultValues={defaultFilterValues}
        setFilterParams={setFilterParams}
        filterFields={filterFields}
      />
      <StockMovementOutboundTable
        isRequestsOpen={props.isRequestsList}
        filterParams={filterParams}
      />
    </div>
  );
};

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
  currentUser: state.session.user,
  currentLocation: state.session.currentLocation,
});

export default connect(mapStateToProps, { fetchTranslations })(StockMovementOutboundList);

StockMovementOutboundList.propTypes = {
  locale: PropTypes.string.isRequired,
  isRequestsList: PropTypes.bool.isRequired,
  currentUser: PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  }).isRequired,
  currentLocation: PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  }).isRequired,
  fetchTranslations: PropTypes.func.isRequired,
};
