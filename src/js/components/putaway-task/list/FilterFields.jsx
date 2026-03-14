import FilterSelectField from 'components/form-elements/FilterSelectField';

const STATUS_CATEGORY_OPTIONS = [
  { id: 'OPEN', value: 'OPEN', label: 'Open' },
  { id: 'CLOSED', value: 'CLOSED', label: 'Closed' },
];

const STATUS_OPTIONS = [
  { id: 'PENDING', value: 'PENDING', label: 'Pending' },
  { id: 'STARTED', value: 'STARTED', label: 'Started' },
  { id: 'IN_PROGRESS', value: 'IN_PROGRESS', label: 'In Progress' },
  { id: 'COMPLETED', value: 'COMPLETED', label: 'Completed' },
  { id: 'CANCELED', value: 'CANCELED', label: 'Canceled' },
];

export default {
  statusCategory: {
    type: FilterSelectField,
    attributes: {
      valueKey: 'id',
      filterElement: true,
      placeholder: 'react.putawayTask.filters.statusCategory.placeholder.label',
      defaultPlaceholder: 'Status Category',
      showLabelTooltip: true,
    },
    getDynamicAttr: () => ({
      options: STATUS_CATEGORY_OPTIONS,
    }),
  },
  status: {
    type: FilterSelectField,
    attributes: {
      valueKey: 'id',
      filterElement: true,
      placeholder: 'react.putawayTask.filters.status.placeholder.label',
      defaultPlaceholder: 'Status',
      showLabelTooltip: true,
      multi: true,
      closeMenuOnSelect: false,
      blurInputOnSelect: false,
    },
    getDynamicAttr: () => ({
      options: STATUS_OPTIONS,
    }),
  },
  container: {
    type: FilterSelectField,
    attributes: {
      async: true,
      defaultOptions: true,
      cacheOptions: false,
      valueKey: 'id',
      labelKey: 'label',
      options: [],
      filterOptions: (options) => options,
      filterElement: true,
      placeholder: 'react.putawayTask.filters.container.placeholder.label',
      defaultPlaceholder: 'Container',
      showLabelTooltip: true,
    },
    getDynamicAttr: ({ debouncedContainerFetch }) => ({
      loadOptions: debouncedContainerFetch,
    }),
  },
  destination: {
    type: FilterSelectField,
    attributes: {
      async: true,
      defaultOptions: true,
      cacheOptions: false,
      valueKey: 'id',
      labelKey: 'label',
      options: [],
      filterOptions: (options) => options,
      filterElement: true,
      placeholder: 'react.putawayTask.filters.destination.placeholder.label',
      defaultPlaceholder: 'Destination',
      showLabelTooltip: true,
    },
    getDynamicAttr: ({ debouncedDestinationFetch }) => ({
      loadOptions: debouncedDestinationFetch,
    }),
  },
};
