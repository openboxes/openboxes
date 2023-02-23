import CheckboxField from 'components/form-elements/CheckboxField';
import FilterSelectField from 'components/form-elements/FilterSelectField';

export default {
  categoryId: {
    type: FilterSelectField,
    attributes: {
      valueKey: 'id',
      filterElement: true,
      placeholder: 'react.productsList.filters.category.label',
      defaultPlaceholder: 'Category',
      showLabelTooltip: true,
      multi: true,
      closeMenuOnSelect: false,
      blurInputOnSelect: false,
    },
    getDynamicAttr: ({ categories }) => ({
      options: categories,
    }),
  },
  includeCategoryChildren: {
    type: CheckboxField,
    label: 'react.productsList.includeSubcategories.label',
    defaultMessage: 'Include all products in all subcategories',
    attributes: {
      filterElement: true,
    },
  },
  catalogId: {
    type: FilterSelectField,
    attributes: {
      valueKey: 'id',
      filterElement: true,
      placeholder: 'react.productsList.filters.catalog.label',
      defaultPlaceholder: 'Formulary',
      showLabelTooltip: true,
      multi: true,
      closeMenuOnSelect: false,
      blurInputOnSelect: false,
    },
    getDynamicAttr: ({ catalogs }) => ({
      options: catalogs,
    }),
  },
  tagId: {
    type: FilterSelectField,
    attributes: {
      valueKey: 'id',
      filterElement: true,
      placeholder: 'react.productsList.filters.tags.label',
      defaultPlaceholder: 'Tags',
      showLabelTooltip: true,
      multi: true,
      closeMenuOnSelect: false,
      blurInputOnSelect: false,
    },
    getDynamicAttr: ({ tags }) => ({
      options: tags,
    }),
  },
  glAccount: {
    type: FilterSelectField,
    attributes: {
      valueKey: 'id',
      placeholder: 'react.productsList.filters.glAccount.label',
      defaultPlaceholder: 'Gl Account',
      multi: true,
      closeMenuOnSelect: false,
      blurInputOnSelect: false,
      filterElement: true,
    },
    getDynamicAttr: ({ glAccounts }) => ({
      options: glAccounts,
    }),
  },
  includeInactive: {
    type: CheckboxField,
    label: 'react.productsList.includeInactive.label',
    defaultMessage: 'Include inactive',
    attributes: {
      filterElement: true,
    },
  },
};
