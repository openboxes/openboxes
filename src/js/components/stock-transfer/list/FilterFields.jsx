import DateFilter from 'components/form-elements/DateFilter/DateFilter';
import FilterSelectField from 'components/form-elements/FilterSelectField';

export default {
  status: {
    type: FilterSelectField,
    attributes: {
      valueKey: 'id',
      filterElement: true,
      placeholder: 'react.stockTransfer.filters.status.placeholder.label',
      defaultPlaceholder: 'Status',
      showLabelTooltip: true,
      multi: true,
      closeMenuOnSelect: false,
      blurInputOnSelect: false,
    },
    getDynamicAttr: ({ statuses }) => ({
      options: statuses,
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
      placeholder: 'react.stockTransfer.filters.createdBy.placeholder.label',
      defaultPlaceholder: 'Created by',
      showLabelTooltip: true,
    },
    getDynamicAttr: ({
      debouncedPeopleFetch,
    }) => ({
      loadOptions: debouncedPeopleFetch,
    }),
  },
  lastUpdatedStartDate: {
    type: DateFilter,
    attributes: {
      label: 'react.stockTransfer.lastUpdateAfter.label',
      defaultMessage: 'Last update after',
      dateFormat: 'MM/DD/YYYY',
      filterElement: true,
    },
  },
  lastUpdatedEndDate: {
    type: DateFilter,
    attributes: {
      label: 'react.stockTransfer.lastUpdateBefore.label',
      defaultMessage: 'Last update before',
      dateFormat: 'MM/DD/YYYY',
      filterElement: true,
    },
  },
};
