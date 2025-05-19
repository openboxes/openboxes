import DateFilter from 'components/form-elements/DateFilter/DateFilter';
import FilterSelectField from 'components/form-elements/FilterSelectField';

export default {
  endDate: {
    type: DateFilter,
    attributes: {
      label: 'react.cycleCountReporting.filters.createdAfter.label',
      defaultMessage: 'Created after',
      dateFormat: 'MM/DD/YYYY',
      showLabelTooltip: true,
      filterElement: true,
      top: true,
    },
  },
  startDate: {
    type: DateFilter,
    attributes: {
      label: 'react.cycleCountReporting.filters.createdBefore.label',
      defaultMessage: 'Created before',
      dateFormat: 'MM/DD/YYYY',
      showLabelTooltip: true,
      filterElement: true,
      top: true,
    },
  },
  products: {
    type: FilterSelectField,
    attributes: {
      async: true,
      multi: true,
      openOnClick: false,
      autoload: false,
      cache: false,
      valueKey: 'id',
      labelKey: 'displayName',
      options: [],
      filterOptions: (options) => options,
      filterElement: true,
      placeholder: 'react.cycleCountReporting.filters.product.label',
      defaultPlaceholder: 'Product',
      showLabelTooltip: true,
    },
    getDynamicAttr: ({
      debouncedProductsFetch,
    }) => ({
      loadOptions: debouncedProductsFetch,
    }),
  },
};
