import DateFilter from 'components/form-elements/DateFilter/DateFilter';
import FilterSelectField from 'components/form-elements/FilterSelectField';

export default {
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
      placeholder: 'Created by',
      showLabelTooltip: true,
    },
    getDynamicAttr: ({
      debouncedUsersFetch,
    }) => ({
      loadOptions: debouncedUsersFetch,
    }),
  },
};
