import CheckboxField from 'components/form-elements/CheckboxField';
import DateFilter from 'components/form-elements/DateFilter/DateFilter';
import FilterSelectField from 'components/form-elements/FilterSelectField';

export default {
  dateLastCount: {
    type: DateFilter,
    attributes: {
      label: 'react.cycleCount.filter.lastCountedDate.label',
      defaultMessage: 'Last counted date',
      dateFormat: 'MM/DD/YYYY',
      showLabelTooltip: true,
      filterElement: true,
    },
  },
  categories: {
    type: FilterSelectField,
    attributes: {
      multi: true,
      valueKey: 'id',
      filterElement: true,
      placeholder: 'react.cycleCount.filter.category.label',
      defaultPlaceholder: 'Category',
      showLabelTooltip: true,
      closeMenuOnSelect: false,
    },
    getDynamicAttr: ({ categories }) => ({
      options: categories,
    }),
  },
  internalLocations: {
    type: FilterSelectField,
    attributes: {
      multi: true,
      valueKey: 'id',
      labelKey: 'name',
      filterElement: true,
      placeholder: 'react.cycleCount.filter.binLocation.label',
      defaultPlaceholder: 'Bin Location',
      showLabelTooltip: true,
      closeMenuOnSelect: false,
    },
    getDynamicAttr: ({ internalLocations }) => ({
      options: internalLocations,
    }),
  },
  tags: {
    type: FilterSelectField,
    attributes: {
      multi: true,
      valueKey: 'id',
      filterElement: true,
      placeholder: 'react.cycleCount.filter.tags.label',
      defaultPlaceholder: 'Tags',
      showLabelTooltip: true,
      closeMenuOnSelect: false,
    },
    getDynamicAttr: ({ tags }) => ({
      options: tags,
    }),
  },
  catalogs: {
    type: FilterSelectField,
    attributes: {
      multi: true,
      valueKey: 'id',
      filterElement: true,
      placeholder: 'react.cycleCount.filter.productCatalogs.label',
      defaultPlaceholder: 'Product Catalogue',
      showLabelTooltip: true,
      closeMenuOnSelect: false,
    },
    getDynamicAttr: ({ catalogs }) => ({
      options: catalogs,
    }),
  },
  abcClasses: {
    type: FilterSelectField,
    attributes: {
      multi: true,
      valueKey: 'id',
      filterElement: true,
      placeholder: 'react.cycleCount.filter.abcClass.label',
      defaultPlaceholder: 'ABC Class',
      showLabelTooltip: true,
      closeMenuOnSelect: false,
    },
    getDynamicAttr: ({ abcClasses }) => ({
      options: abcClasses,
    }),
  },
  negativeQuantity: {
    type: CheckboxField,
    attributes: {
      withLabel: true,
      label: 'react.cycleCount.filter.negativeInventory.label',
      defaultMessage: 'Show negative quantity only',
      filterElement: true,
    },
  },
};
