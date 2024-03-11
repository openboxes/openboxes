import CheckboxField from 'components/form-elements/CheckboxField';
import DateFilter from 'components/form-elements/DateFilter/DateFilter';
import FilterSelectField from 'components/form-elements/FilterSelectField';

export default {
  product: {
    type: FilterSelectField,
    attributes: {
      async: true,
      openOnClick: false,
      autoload: false,
      cache: false,
      valueKey: 'id',
      labelKey: 'displayName',
      options: [],
      filterOptions: (options) => options,
      filterElement: true,
      placeholder: 'react.productSupplier.filters.product.placeholder.label',
      defaultPlaceholder: 'Product',
      showLabelTooltip: true,
    },
    getDynamicAttr: ({
      debouncedProductsFetch,
    }) => ({
      loadOptions: debouncedProductsFetch,
    }),
  },
  supplier: {
    type: FilterSelectField,
    attributes: {
      async: true,
      openOnClick: false,
      autoload: false,
      cache: false,
      valueKey: 'id',
      labelKey: 'name',
      options: [],
      filterOptions: (options) => options,
      filterElement: true,
      placeholder: 'react.productSupplier.filters.supplier.placeholder.label=Supplier\n',
      defaultPlaceholder: 'Supplier',
      showLabelTooltip: true,
    },
    getDynamicAttr: ({
      debouncedOrganizationsFetch,
    }) => ({
      loadOptions: debouncedOrganizationsFetch,
    }),
  },
  defaultPreferenceTypes: {
    type: FilterSelectField,
    attributes: {
      multi: true,
      valueKey: 'id',
      filterElement: true,
      placeholder: 'react.productSupplier.filters.defaultPreferenceType.placeholder.label',
      defaultPlaceholder: 'Default Preference Type',
      showLabelTooltip: true,
      closeMenuOnSelect: false,
    },
    getDynamicAttr: ({ preferenceTypes }) => ({
      options: preferenceTypes,
    }),
  },
  createdFrom: {
    type: DateFilter,
    attributes: {
      label: 'react.productSupplier.filters.createdFrom.placeholder.label',
      defaultMessage: 'Date created from',
      dateFormat: 'MM/DD/YYYY',
      filterElement: true,
    },
  },
  createdTo: {
    type: DateFilter,
    attributes: {
      label: 'react.productSupplier.filters.createdTo.placeholder.label',
      defaultMessage: 'Date created to',
      dateFormat: 'MM/DD/YYYY',
      filterElement: true,
    },
  },
  includeInactive: {
    type: CheckboxField,
    attributes: {
      withLabel: true,
      label: 'react.productSupplier.filters.active.placeholder.label',
      defaultMessage: 'Include inactive',
      filterElement: true,
    },
  },
};
