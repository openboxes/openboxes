import CheckboxField from 'components/form-elements/CheckboxField';
import FilterSelectField from 'components/form-elements/FilterSelectField';

export default {
  origin: {
    type: FilterSelectField,
    attributes: {
      className: 'location-select',
      multi: true,
      filterElement: true,
      placeholder: 'react.stocklists.filters.origin.label',
      defaultPlaceholder: 'Origin',
      showLabelTooltip: true,
      closeMenuOnSelect: false,
      blurInputOnSelect: false,
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
      placeholder: 'react.stocklists.filters.destination.label',
      defaultPlaceholder: 'Destination',
      showLabelTooltip: true,
      closeMenuOnSelect: false,
      blurInputOnSelect: false,
      valueKey: 'id',
      labelKey: 'name',
    },
    getDynamicAttr: ({ locations }) => ({
      options: locations,
    }),
  },
  isPublished: {
    type: CheckboxField,
    attributes: {
      withLabel: true,
      label: 'react.stocklists.includeUnpublished.label',
      defaultMessage: 'Include unpublished stocklists',
      filterElement: true,
    },
  },
};
