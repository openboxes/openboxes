import DateFilter from 'components/form-elements/DateFilter/DateFilter';
import FilterSelectField from 'components/form-elements/FilterSelectField';
import { DateFormat } from 'consts/timeFormat';

const ExpirationHistoryReportFilterFields = {
  location: {
    type: FilterSelectField,
    attributes: {
      valueKey: 'id',
      filterElement: true,
      placeholder: 'react.report.expirationHistory.location.label',
      defaultPlaceholder: 'Location',
      options: [],
      showLabelTooltip: true,
      disabled: true,
    },
  },
  startDate: {
    type: DateFilter,
    attributes: {
      label: 'react.report.expirationHistory.startDate.label',
      defaultMessage: 'Start Date',
      dateFormat: DateFormat.DD_MMM_YYYY,
      filterElement: true,
      required: true,
    },
  },
  endDate: {
    type: DateFilter,
    attributes: {
      label: 'react.report.expirationHistory.endDate.label',
      defaultMessage: 'End Date',
      dateFormat: DateFormat.DD_MMM_YYYY,
      filterElement: true,
      required: true,
    },
  },
};

export default ExpirationHistoryReportFilterFields;
