import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import { fetchTranslations } from 'actions';
import DateFilter from 'components/form-elements/DateFilter/DateFilter';
import FilterSelectField from 'components/form-elements/FilterSelectField';
import StockMovementInboundFilters from 'components/stock-movement/inbound/StockMovementInboundFilters';
import StockMovementInboundHeader from 'components/stock-movement/inbound/StockMovementInboundHeader';
import StockMovementInboundTable from 'components/stock-movement/inbound/StockMovementInboundTable';

const filterFields = {
  receiptStatusCode: {
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

const StockMovementInboundList = (props) => {
  const [filterParams, setFilterParams] = useState({});
  const [defaultFilterValues, setDefaultFilterValues] = useState({});

  useEffect(() => {
    props.fetchTranslations(props.locale, 'stockMovement');
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
        destination: {
          id: props.currentLocation?.id,
          value: props.currentLocation?.id,
          name: props.currentLocation?.name,
          label: props.currentLocation?.name,
        },
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
      <StockMovementInboundHeader showMyStockMovements={selectFiltersForMyStockMovements} />
      <StockMovementInboundFilters
        defaultValues={defaultFilterValues}
        setFilterParams={setFilterParams}
        filterFields={filterFields}
      />
      <StockMovementInboundTable filterParams={filterParams} />
    </div>
  );
};

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
  currentUser: state.session.user,
  currentLocation: state.session.currentLocation,
});

export default connect(mapStateToProps, { fetchTranslations })(StockMovementInboundList);

StockMovementInboundList.propTypes = {
  locale: PropTypes.string.isRequired,
  currentUser: PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  }).isRequired,
  fetchTranslations: PropTypes.func.isRequired,
  currentLocation: PropTypes.shape({
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  }).isRequired,
};
