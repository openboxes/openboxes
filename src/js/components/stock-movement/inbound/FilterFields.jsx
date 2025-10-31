import DateFilter from 'components/form-elements/DateFilter/DateFilter';
import FilterSelectField from 'components/form-elements/FilterSelectField';
import DateFormat from 'consts/dateFormat';

export default {
  receiptStatusCode: {
    type: FilterSelectField,
    attributes: {
      multi: true,
      filterElement: true,
      placeholder: 'react.stockMovement.inbound.filters.receiptStatus.label',
      defaultPlaceholder: 'Receipt Status',
      ariaLabel: 'Receipt Status',
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
      filterOptions: (options) => options,
      filterElement: true,
      placeholder: 'react.stockMovement.origin.label',
      defaultPlaceholder: 'Origin',
      ariaLabel: 'Origin',
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
      ariaLabel: 'Destination',
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
      ariaLabel: 'Shipment type',
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
      filterOptions: (options) => options,
      filterElement: true,
      placeholder: 'react.stockMovement.requestedBy.label',
      defaultPlaceholder: 'Requested By',
      ariaLabel: 'Requested By',
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
      filterOptions: (options) => options,
      filterElement: true,
      placeholder: 'react.stockMovement.createdBy.label',
      defaultPlaceholder: 'Created By',
      ariaLabel: 'Created By',
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
      filterOptions: (options) => options,
      filterElement: true,
      placeholder: 'react.stockMovement.updatedBy.label',
      defaultPlaceholder: 'Updated By',
      ariaLabel: 'Updated By',
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
      localizeDate: true,
      localizedDateFormat: DateFormat.COMMON,
      label: 'react.stockMovement.filter.createdAfter.label',
      defaultMessage: 'Created after',
      // date format in which the date will be sent to the API
      dateFormat: 'MM/DD/YYYY',
      ariaLabel: 'Created after',
      filterElement: true,
    },
  },
  createdBefore: {
    type: DateFilter,
    attributes: {
      localizeDate: true,
      localizedDateFormat: DateFormat.COMMON,
      label: 'react.stockMovement.filter.createdBefore.label',
      defaultMessage: 'Created before',
      // date format in which the date will be sent to the API
      dateFormat: 'MM/DD/YYYY',
      ariaLabel: 'Created before',
      filterElement: true,
    },
  },
};
