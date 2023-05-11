import DateFilter from 'components/form-elements/DateFilter/DateFilter';
import FilterSelectField from 'components/form-elements/FilterSelectField';

export default {
  status: {
    type: FilterSelectField,
    attributes: {
      multi: true,
      filterElement: true,
      placeholder: 'react.purchaseOrder.filters.status.placeholder.label',
      defaultPlaceholder: 'Status',
      showLabelTooltip: true,
      closeMenuOnSelect: false,
      blurInputOnSelect: false,
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
      placeholder: 'react.purchaseOrder.filters.origin.placeholder.label',
      defaultPlaceholder: 'Supplier',
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
      placeholder: 'react.purchaseOrder.filters.destination.placeholder.label',
      defaultPlaceholder: 'Destination',
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
      placeholder: 'react.purchaseOrder.filters.destinationParty.placeholder.label',
      defaultPlaceholder: 'Purchasing organization',
      showLabelTooltip: true,
    },
    getDynamicAttr: ({ buyers, isCentralPurchasingEnabled }) => ({
      options: buyers,
      disabled: isCentralPurchasingEnabled,
    }),
  },
  paymentTerm: {
    type: FilterSelectField,
    attributes: {
      valueKey: 'id',
      filterElement: true,
      placeholder: 'react.purchaseOrder.column.paymentTerms.label',
      defaultPlaceholder: 'Payment Terms',
      showLabelTooltip: true,
      multi: true,
      closeMenuOnSelect: false,
      blurInputOnSelect: false,
      nullOption: true,
      nullOptionLabel: 'react.purchaseOrder.filters.option.blankPaymentTerm.label',
      nullOptionDefaultLabel: 'Blank Payment Term',
    },
    getDynamicAttr: ({ paymentTerms }) => ({
      options: paymentTerms,
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
      placeholder: 'react.purchaseOrder.filters.orderedBy.placeholder.label',
      defaultPlaceholder: 'Ordered by',
      showLabelTooltip: true,
    },
    getDynamicAttr: ({
      debouncedPeopleFetch,
    }) => ({
      loadOptions: debouncedPeopleFetch,
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
      placeholder: 'react.purchaseOrder.filters.createdBy.placeholder.label',
      defaultPlaceholder: 'Created by',
      showLabelTooltip: true,
    },
    getDynamicAttr: ({
      debouncedPeopleFetch,
    }) => ({
      loadOptions: debouncedPeopleFetch,
    }),
  },
};
