import CheckboxField from 'components/form-elements/CheckboxField';
import DateFilter from 'components/form-elements/DateFilter/DateFilter';
import FilterSelectField from 'components/form-elements/FilterSelectField';
import { DateFormat } from 'consts/timeFormat';

const cycleCountFilterFields = {
  allProductsTab: {
    dateLastCount: {
      type: DateFilter,
      attributes: {
        label: 'react.cycleCount.filter.lastCountedDate.label',
        defaultMessage: 'Last counted date',
        dateFormat: DateFormat.DD_MMM_YYYY,
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
        defaultMessage: 'Negative quantity',
        filterElement: true,
        showCustomTooltip: true,
        customTooltipLabel: 'react.cycleCount.filter.negativeInventory.tooltip.label',
      },
    },
  },
  toCountTab: {
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
    countAssignees: {
      type: FilterSelectField,
      attributes: {
        async: true,
        multi: true,
        openOnClick: false,
        autoload: false,
        cache: false,
        valueKey: 'id',
        labelKey: 'displayName',
        options: [],
        filterOptions: (options) => options,
        filterElement: true,
        placeholder: 'react.cycleCount.filter.assignee.label',
        defaultPlaceholder: 'Assignee',
        showLabelTooltip: true,
      },
      getDynamicAttr: ({ debouncedPeopleFetch }) => ({
        loadOptions: debouncedPeopleFetch,
      }),
    },
    countDeadline: {
      type: DateFilter,
      attributes: {
        label: 'react.cycleCount.filter.deadline.label',
        defaultMessage: 'Deadline',
        dateFormat: DateFormat.DD_MMM_YYYY,
        showLabelTooltip: true,
        filterElement: true,
      },
    },
    negativeQuantity: {
      type: CheckboxField,
      attributes: {
        withLabel: true,
        label: 'react.cycleCount.filter.negativeInventory.label',
        defaultMessage: 'Negative quantity',
        filterElement: true,
        showCustomTooltip: true,
        customTooltipLabel: 'react.cycleCount.filter.negativeInventory.tooltip.label',
      },
    },
  },
  toResolveTab: {
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
    recountAssignees: {
      type: FilterSelectField,
      attributes: {
        async: true,
        multi: true,
        openOnClick: false,
        autoload: false,
        cache: false,
        valueKey: 'id',
        labelKey: 'displayName',
        options: [],
        filterOptions: (options) => options,
        filterElement: true,
        placeholder: 'react.cycleCount.filter.assignee.label',
        defaultPlaceholder: 'Assignee',
        showLabelTooltip: true,
      },
      getDynamicAttr: ({ debouncedPeopleFetch }) => ({
        loadOptions: debouncedPeopleFetch,
      }),
    },
    recountDeadline: {
      type: DateFilter,
      attributes: {
        label: 'react.cycleCount.filter.deadline.label',
        defaultMessage: 'Deadline',
        dateFormat: DateFormat.DD_MMM_YYYY,
        showLabelTooltip: true,
        filterElement: true,
      },
    },
    negativeQuantity: {
      type: CheckboxField,
      attributes: {
        withLabel: true,
        label: 'react.cycleCount.filter.negativeInventory.label',
        defaultMessage: 'Negative quantity',
        filterElement: true,
        showCustomTooltip: true,
        customTooltipLabel: 'react.cycleCount.filter.negativeInventory.tooltip.label',
      },
    },
  },
};

export default cycleCountFilterFields;
