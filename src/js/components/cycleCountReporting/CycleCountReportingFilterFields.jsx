import DateFilter from 'components/form-elements/DateFilter/DateFilter';
import FilterSelectField from 'components/form-elements/FilterSelectField';

export default {
  endDate: {
    type: DateFilter,
    attributes: {
      label: 'react.cycleCount.filters.recordedAfter.label',
      defaultMessage: 'Recorded after',
      dateFormat: 'MM/DD/YYYY',
      showLabelTooltip: true,
      filterElement: true,
      top: true,
      required: true,
    },
  },
  startDate: {
    type: DateFilter,
    attributes: {
      label: 'react.cycleCount.filters.recordedBefore.label',
      defaultMessage: 'Recorded before',
      dateFormat: 'MM/DD/YYYY',
      showLabelTooltip: true,
      filterElement: true,
      top: true,
      required: true,
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
      placeholder: 'react.cycleCount.filters.product.label',
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
