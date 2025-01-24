import DateFilter from 'components/form-elements/DateFilter/DateFilter';
import FilterSelectField from 'components/form-elements/FilterSelectField';
import DateFormat from 'consts/dateFormat';

export default (isRequest) => ({
  requisitionStatusCode: {
    type: FilterSelectField,
    attributes: {
      multi: true,
      filterElement: true,
      placeholder: 'react.stockMovement.outbound.filters.requisitionStatus.label',
      defaultPlaceholder: 'Requisition Status',
      showLabelTooltip: true,
      closeMenuOnSelect: false,
      blurInputOnSelect: false,
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
      placeholder: 'react.stockMovement.origin.label',
      defaultPlaceholder: 'Origin',
      options: [],
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
      filterOptions: (options) => options,
      filterElement: true,
      placeholder: 'react.stockMovement.destination.label',
      defaultPlaceholder: 'Destination',
      showLabelTooltip: true,
    },
    getDynamicAttr: ({
      fetchLocations,
    }) => ({
      loadOptions: fetchLocations,
    }),
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
      filterOptions: (options) => options,
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
  ...(isRequest && {
    approver: {
      type: FilterSelectField,
      attributes: {
        show: false,
        openOnClick: false,
        autoload: false,
        cache: false,
        valueKey: 'id',
        labelKey: 'name',
        options: [],
        filterOptions: (options) => options,
        filterElement: true,
        placeholder: 'react.stockMovement.request.approvers.label',
        defaultPlaceholder: 'Approvers',
        showLabelTooltip: true,
        multi: true,
        nullOption: true,
        nullOptionDefaultLabel: 'None',
      },
      getDynamicAttr: ({
        approvers,
      }) => ({
        options: approvers,
      }),
    },
  }),
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
      placeholder: 'react.stockMovement.outbound.filters.requestType.label',
      defaultPlaceholder: 'Request type',
      showLabelTooltip: true,
    },
  },
  createdAfter: {
    type: DateFilter,
    attributes: {
      label: 'react.stockMovement.filter.createdAfter.label',
      defaultMessage: 'Created after',
      localizeDate: true,
      localizedDateFormat: DateFormat.COMMON,
      filterElement: true,
      // date format in which the date will be sent to the API
      dateFormat: 'MM/DD/YYYY',
    },
  },
  createdBefore: {
    type: DateFilter,
    attributes: {
      label: 'react.stockMovement.filter.createdBefore.label',
      defaultMessage: 'Created before',
      localizeDate: true,
      localizedDateFormat: DateFormat.COMMON,
      filterElement: true,
      // date format in which the date will be sent to the API
      dateFormat: 'MM/DD/YYYY',
    },
  },
});
