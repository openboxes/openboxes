import FilterSelectField from 'components/form-elements/FilterSelectField';
import {
  getExpiredStockOptions, getFilterProductOptions,
} from 'consts/filterOptions';

const reorderReportFilterFields = {
  additionalInventoryLocations: {
    type: FilterSelectField,
    attributes: {
      openOnClick: false,
      cache: false,
      multi: true,
      closeMenuOnSelect: false,
      blurInputOnSelect: false,
      valueKey: 'id',
      labelKey: 'label',
      filterElement: true,
      placeholder: 'react.report.reorder.additionalInventoryLocations.label',
      defaultPlaceholder: 'Additional Inventory Locations',
      showLabelTooltip: true,
    },
    getDynamicAttr: ({
      locations,
    }) => ({
      options: locations,
    }),
  },
  expiredStock: {
    type: FilterSelectField,
    attributes: {
      placeholder: 'react.report.reorder.expiredStock.label',
      defaultPlaceholder: 'Expired Stock',
      showLabelTooltip: true,
      filterElement: true,
      valueKey: 'id',
      labelKey: 'label',
      options: getExpiredStockOptions(),
    },
  },
  filterProducts: {
    type: FilterSelectField,
    attributes: {
      placeholder: 'react.report.reorder.filterProducts.label',
      defaultPlaceholder: 'Filter products',
      showLabelTooltip: true,
      filterElement: true,
      valueKey: 'id',
      labelKey: 'label',
      options: getFilterProductOptions(),
    },
  },
  categories: {
    type: FilterSelectField,
    attributes: {
      valueKey: 'id',
      filterElement: true,
      placeholder: 'react.report.reorder.category.label',
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
  tags: {
    type: FilterSelectField,
    attributes: {
      valueKey: 'id',
      filterElement: true,
      placeholder: 'react.report.reorder.tags.label',
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
};

export default reorderReportFilterFields;
