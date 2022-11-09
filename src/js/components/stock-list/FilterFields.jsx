import CheckboxField from 'components/form-elements/CheckboxField';
import FilterSelectField from 'components/form-elements/FilterSelectField';

export default {
  origin: {
    type: FilterSelectField,
    attributes: {
      className: 'location-select',
      multi: true,
      filterElement: true,
      placeholder: 'Origin',
      showLabelTooltip: true,
      closeMenuOnSelect: false,
      valueKey: 'id',
      labelKey: 'name',
    },
    getDynamicAttr: ({ locations }) => ({
      options: locations,
    }),
  },
  destination: {
    type: FilterSelectField,
    attributes: {
      className: 'location-select',
      multi: true,
      filterElement: true,
      placeholder: 'Destination',
      showLabelTooltip: true,
      closeMenuOnSelect: false,
      valueKey: 'id',
      labelKey: 'name',
    },
    getDynamicAttr: ({ locations }) => ({
      options: locations,
    }),
  },
  isPublished: {
    type: CheckboxField,
    label: 'react.stocklists.includeUnpublished.label',
    defaultMessage: 'Include unpublished stocklists',
    attributes: {
      filterElement: true,
    },
  },
};
