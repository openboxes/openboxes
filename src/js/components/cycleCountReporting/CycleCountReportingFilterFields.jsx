import DateFilter from 'components/form-elements/DateFilter/DateFilter';
import FilterSelectField from 'components/form-elements/FilterSelectField';
import { DateFormat } from 'consts/timeFormat';

const cycleCountReportingFilterFields = {
  products: {
    startDate: {
      type: DateFilter,
      attributes: {
        label: 'react.cycleCount.filters.recordedAfter.label',
        defaultMessage: 'Recorded after',
        dateFormat: DateFormat.DD_MMM_YYYY,
        showLabelTooltip: true,
        filterElement: true,
        top: true,
        required: true,
      },
    },
    endDate: {
      type: DateFilter,
      attributes: {
        label: 'react.cycleCount.filters.recordedBefore.label',
        defaultMessage: 'Recorded before',
        dateFormat: DateFormat.DD_MMM_YYYY,
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
        valueKey: 'id',
        labelKey: 'displayName',
        options: [],
        filterElement: true,
        placeholder: 'react.cycleCount.filters.product.label',
        defaultPlaceholder: 'Product',
        showLabelTooltip: true,
      },
      getDynamicAttr: ({ debouncedProductsFetch }) => ({
        loadOptions: debouncedProductsFetch,
      }),
    },
  },
  inventoryTransactions: {
    startDate: {
      type: DateFilter,
      attributes: {
        label: 'react.cycleCount.filters.recordedAfter.label',
        defaultMessage: 'Recorded after',
        dateFormat: DateFormat.DD_MMM_YYYY,
        showLabelTooltip: true,
        filterElement: true,
        top: true,
        required: true,
      },
    },
    endDate: {
      type: DateFilter,
      attributes: {
        label: 'react.cycleCount.filters.recordedBefore.label',
        defaultMessage: 'Recorded before',
        dateFormat: DateFormat.DD_MMM_YYYY,
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
        valueKey: 'id',
        labelKey: 'displayName',
        options: [],
        filterElement: true,
        placeholder: 'react.cycleCount.filters.product.label',
        defaultPlaceholder: 'Product',
        showLabelTooltip: true,
      },
      getDynamicAttr: ({ debouncedProductsFetch }) => ({
        loadOptions: debouncedProductsFetch,
      }),
    },
  },
  indicators: {
    startDate: {
      type: DateFilter,
      attributes: {
        label: 'react.cycleCount.filters.recordedAfter.label',
        defaultMessage: 'Recorded after',
        dateFormat: DateFormat.DD_MMM_YYYY,
        showLabelTooltip: true,
        filterElement: true,
        top: true,
        required: true,
      },
    },
    endDate: {
      type: DateFilter,
      attributes: {
        label: 'react.cycleCount.filters.recordedBefore.label',
        defaultMessage: 'Recorded before',
        dateFormat: DateFormat.DD_MMM_YYYY,
        showLabelTooltip: true,
        filterElement: true,
        top: true,
        required: true,
      },
    },
  },
};

export default cycleCountReportingFilterFields;
