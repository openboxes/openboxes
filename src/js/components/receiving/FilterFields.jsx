import FilterSelectField from 'components/form-elements/FilterSelectField';
import receiptStatusOptions from 'consts/receiptStatusOptions';

const filterFields = (translate) => ({
  receiptStatusCode: {
    type: FilterSelectField,
    attributes: {
      // top and the className are used to position this filter in the first row,
      // next to the search field
      top: true,
      className: 'order-1',
      multi: true,
      filterElement: true,
      placeholder: 'react.partialReceiving.filters.receiptStatus.label',
      defaultPlaceholder: 'Receipt Status',
      ariaLabel: 'Receipt Status',
      showLabelTooltip: true,
      options: receiptStatusOptions(translate),
      closeMenuOnSelect: false,
      blurInputOnSelect: false,
    },
  },
});

export default filterFields;
