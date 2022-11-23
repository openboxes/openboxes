import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import { fetchInvoiceStatuses, fetchInvoiceTypeCodes, fetchSuppliers } from 'actions';
import FilterForm from 'components/Filter/FilterForm';
import DateFilter from 'components/form-elements/DateFilter';
import FilterSelectField from 'components/form-elements/FilterSelectField';
import { debounceUsersFetch } from 'utils/option-utils';

const filterFields = {
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
      debouncedUsersFetch,
    }) => ({
      loadOptions: debouncedUsersFetch,
    }),
  },
};

const InvoiceListFilters = ({
  setFilterParams,
  debounceTime,
  minSearchLength,
  currentLocation,
  statuses,
  fetchStatuses,
  suppliers,
  fetchSupplierOrganizations,
  typeCodes,
  fetchTypeCodes,
}) => {
  const [defaultValues, setDefaultValues] = useState({});

  useEffect(() => {
    // Avoid unnecessary re-fetches if getAppContext triggers fetching session info
    // but currentLocation doesn't change
    if (currentLocation?.id) {
      const initialEmptyValues = Object.keys(filterFields).reduce((acc, key) => {
        if (!acc[key]) return { ...acc, [key]: '' };
        return acc;
      }, {});
      setDefaultValues({
        ...initialEmptyValues,
        buyerOrganization: {
          id: currentLocation?.organization?.id,
          value: currentLocation?.organization?.id,
          name: currentLocation?.organization?.name,
          label: currentLocation?.organization?.name,
        },
      });
    }
  }, [currentLocation?.id]);

  useEffect(() => {
    // If statuses or invoice type codes not yet in store, fetch them
    if (!statuses || statuses.length === 0) {
      fetchStatuses();
    }
    if (!typeCodes || typeCodes.length === 0) {
      fetchTypeCodes();
    }

    // TODO: If editing organizations is in React,
    //  fetch only if length === 0, as edit would should force refetch anyway
    fetchSupplierOrganizations();
  }, []);

  const debouncedUsersFetch = debounceUsersFetch(debounceTime, minSearchLength);

  return (
    <div className="d-flex flex-column list-page-filters">
      <FilterForm
        filterFields={filterFields}
        updateFilterParams={values => setFilterParams({ ...values })}
        formProps={{
          statuses,
          debouncedUsersFetch,
          suppliers,
          typeCodes,
          organization: currentLocation.organization,
        }}
        defaultValues={defaultValues}
        searchFieldPlaceholder="react.invoice.searchField.placeholder.label"
        searchFieldDefaultPlaceholder="Search by invoice number..."
        searchFieldId="invoiceNumber"
        hidden={false}
        ignoreClearFilters={['buyerOrganization']}
      />
    </div>
  );
};

const mapStateToProps = state => ({
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
  currentLocation: state.session.currentLocation,
  // All possible invoice statuses from store
  statuses: state.invoices.statuses,
  suppliers: state.organizations.suppliers,
  typeCodes: state.invoices.typeCodes,
});

const mapDispatchToProps = {
  fetchStatuses: fetchInvoiceStatuses,
  fetchSupplierOrganizations: fetchSuppliers,
  fetchTypeCodes: fetchInvoiceTypeCodes,
};

export default connect(mapStateToProps, mapDispatchToProps)(InvoiceListFilters);

InvoiceListFilters.propTypes = {
  setFilterParams: PropTypes.func.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  currentLocation: PropTypes.shape({}).isRequired,
  statuses: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    variant: PropTypes.string.isRequired,
  })).isRequired,
  fetchStatuses: PropTypes.func.isRequired,
  suppliers: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  })).isRequired,
  fetchSupplierOrganizations: PropTypes.func.isRequired,
  typeCodes: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
  })).isRequired,
  fetchTypeCodes: PropTypes.func.isRequired,
};
