import CheckboxField from 'components/form-elements/CheckboxField';
import DateFilter from 'components/form-elements/DateFilter/DateFilter';
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
  glAccountsId: {
    type: FilterSelectField,
    attributes: {
      valueKey: 'id',
      placeholder: 'react.productsList.filters.glAccount.label',
      defaultPlaceholder: 'GL Account',
      multi: true,
      closeMenuOnSelect: false,
      blurInputOnSelect: false,
      filterElement: true,
    },
    getDynamicAttr: ({ glAccounts }) => ({
      options: glAccounts,
    }),
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
  productFamilyId: {
    type: FilterSelectField,
    attributes: {
      valueKey: 'id',
      filterElement: true,
      placeholder: 'react.productsList.column.productFamily.label',
      defaultPlaceholder: 'Product Family',
      showLabelTooltip: true,
      multi: true,
      closeMenuOnSelect: false,
      blurInputOnSelect: false,
    },
    getDynamicAttr: ({ productGroups }) => ({
      options: productGroups,
    }),
  },
  createdAfter: {
    type: DateFilter,
    attributes: {
      label: 'react.productsList.filters.createdAfter.label',
      defaultMessage: 'Created after',
      dateFormat: 'MM/DD/YYYY',
      filterElement: true,
    },
  },
  createdBefore: {
    type: DateFilter,
    attributes: {
      label: 'react.productsList.filters.createdBefore.label',
      defaultMessage: 'Created before',
      dateFormat: 'MM/DD/YYYY',
      filterElement: true,
    },
  },
  includeInactive: {
    type: CheckboxField,
    attributes: {
      withLabel: true,
      label: 'react.productsList.includeInactive.label',
      defaultMessage: 'Include inactive',
      filterElement: true,
    },
  },
  includeCategoryChildren: {
    type: CheckboxField,
    attributes: {
      withLabel: true,
      label: 'react.productsList.includeSubcategories.label',
      defaultMessage: 'Include subcategories',
      filterElement: true,
    },
  },
};
