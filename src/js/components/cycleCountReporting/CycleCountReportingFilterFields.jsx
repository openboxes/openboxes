import DateFilter from 'components/form-elements/DateFilter/DateFilter';

export default {
  createdBefore: {
    type: DateFilter,
    attributes: {
      label: 'react.cycleCountReporting.filter.createdBefore.label',
      defaultMessage: 'Created before',
      dateFormat: 'MM/DD/YYYY',
      showLabelTooltip: true,
      filterElement: true,
      top: true,
    },
  },
  createdAfter: {
    type: DateFilter,
    attributes: {
      label: 'react.cycleCountReporting.filter.createdAfter.label',
      defaultMessage: 'Created after',
      dateFormat: 'MM/DD/YYYY',
      showLabelTooltip: true,
      filterElement: true,
      top: true,
    },
  },
};
