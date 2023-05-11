import DateFilter from 'components/form-elements/DateFilter/DateFilter';
import FilterSelectField from 'components/form-elements/FilterSelectField';

export default {
  receiptStatusCode: {
    type: FilterSelectField,
    attributes: {
      multi: true,
      filterElement: true,
      placeholder: 'react.stockMovement.inbound.filters.receiptStatus.label',
      defaultPlaceholder: 'Receipt Status',
      showLabelTooltip: true,
      options: [],
      closeMenuOnSelect: false,
      blurInputOnSelect: false,
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
      placeholder: 'react.stockMovement.origin.label',
      defaultPlaceholder: 'Origin',
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
      options: [],
      filterElement: true,
      placeholder: 'react.stockMovement.destination.label',
      defaultPlaceholder: 'Destination',
      showLabelTooltip: true,
      disabled: true,
    },
  },
  shipmentType: {
    type: FilterSelectField,
    attributes: {
      multi: true,
      filterElement: true,
      placeholder: 'react.stockMovement.shipmentType.label',
      defaultPlaceholder: 'Shipment type',
      showLabelTooltip: true,
      options: [],
      blurInputOnSelect: false,
      closeMenuOnSelect: false,
      valueKey: 'id',
      labelKey: 'displayName',
    },
    getDynamicAttr: ({ shipmentTypes }) => ({
      options: shipmentTypes,
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
      placeholder: 'react.stockMovement.requestedBy.label',
      defaultPlaceholder: 'Requested By',
      showLabelTooltip: true,
    },
    getDynamicAttr: ({
      fetchPeople,
    }) => ({
      loadOptions: fetchPeople,
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
      placeholder: 'react.stockMovement.createdBy.label',
      defaultPlaceholder: 'Created By',
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
      placeholder: 'react.stockMovement.updatedBy.label',
      defaultPlaceholder: 'Updated By',
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
