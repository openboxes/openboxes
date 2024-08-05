import DateFilter from 'components/form-elements/DateFilter/DateFilter';
import FilterSelectField from 'components/form-elements/FilterSelectField';

export default {
  buyerOrganization: {
    type: FilterSelectField,
    attributes: {
      valueKey: 'id',
      filterElement: true,
      placeholder: 'react.invoice.buyerOrganization.label',
      defaultPlaceholder: 'Buyer organization',
      showLabelTooltip: true,
      disabled: true,
    },
    getDynamicAttr: ({ organization }) => ({
      options: [
        {
          id: organization.id,
          value: organization.id,
          name: organization.name,
          label: organization.name,
        },
      ],
    }),
  },
  status: {
    type: FilterSelectField,
    attributes: {
      filterElement: true,
      placeholder: 'react.invoice.status.label',
      defaultPlaceholder: 'Invoice Status',
      showLabelTooltip: true,
    },
    getDynamicAttr: ({ statuses }) => ({
      options: statuses,
    }),
  },
  vendor: {
    type: FilterSelectField,
    attributes: {
      valueKey: 'id',
      filterElement: true,
      placeholder: 'react.invoice.vendor.label',
      defaultPlaceholder: 'Vendor',
      showLabelTooltip: true,
    },
    getDynamicAttr: ({ suppliers }) => ({
      options: suppliers,
    }),
  },
  invoiceTypeCode: {
    type: FilterSelectField,
    attributes: {
      filterElement: true,
      placeholder: 'react.invoice.typeCode.label',
      defaultPlaceholder: 'Invoice Type',
      showLabelTooltip: true,
    },
    getDynamicAttr: ({ typeCodes }) => ({
      options: typeCodes,
    }),
  },
  dateInvoiced: {
    type: DateFilter,
    attributes: {
      dateFormat: 'MM/DD/YYYY',
      filterElement: true,
      label: 'react.invoice.invoiceDate.label',
      defaultMessage: 'Invoice Date',
    },
  },
  createdBy: {
    type: FilterSelectField,
    attributes: {
      async: true,
      openOnClick: false,
      autoload: false,
      cache: false,
      valueKey: 'id',
      labelKey: 'name',
      options: [],
      filterOptions: options => options,
      filterElement: true,
      placeholder: 'react.invoice.createdBy.label',
      defaultPlaceholder: 'Created by',
      showLabelTooltip: true,
    },
    getDynamicAttr: ({
      debouncedPeopleFetch,
    }) => ({
      loadOptions: debouncedPeopleFetch,
    }),
  },
};
