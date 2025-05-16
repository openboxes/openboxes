import DateFilter from 'components/form-elements/DateFilter/DateFilter';
import FilterSelectField from 'components/form-elements/FilterSelectField';
import { DateFormat } from 'consts/timeFormat';

export default {
  startDate: {
    type: DateFilter,
    attributes: {
      label: 'react.cycleCountReporting.filters.createdBefore.label',
      defaultMessage: 'Created before',
      dateFormat: DateFormat.DD_MMM_YYYY,
      showLabelTooltip: true,
      filterElement: true,
      top: true,
    },
  },
  endDate: {
    type: DateFilter,
    attributes: {
      label: 'react.cycleCountReporting.filters.createdAfter.label',
      defaultMessage: 'Created after',
      dateFormat: DateFormat.DD_MMM_YYYY,
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
