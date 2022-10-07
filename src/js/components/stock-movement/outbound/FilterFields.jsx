import DateFilter from 'components/form-elements/DateFilter/DateFilter';
import FilterSelectField from 'components/form-elements/FilterSelectField';

export default {
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
