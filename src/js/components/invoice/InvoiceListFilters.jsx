import React, { useCallback } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import FilterForm from 'components/Filter/FilterForm';
import filterFields from 'components/invoice/FilterFields';
import useInvoiceFilters from 'hooks/list-pages/invoice/useInvoiceFilters';
import { debouncePeopleFetch } from 'utils/option-utils';

const InvoiceListFilters = ({
  setFilterParams,
  debounceTime,
  minSearchLength,
  currentLocation,
  statuses,
  suppliers,
  typeCodes,
}) => {
  const { defaultValues, setFilterValues } =
    useInvoiceFilters({ setFilterParams });

  const debouncedPeopleFetch = useCallback(
    debouncePeopleFetch(debounceTime, minSearchLength),
    [debounceTime, minSearchLength],
  );

  return (
    <div className="d-flex flex-column list-page-filters">
      <FilterForm
        filterFields={filterFields}
        updateFilterParams={values => setFilterValues({ ...values })}
        formProps={{
          statuses,
          debouncedPeopleFetch,
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


export default connect(mapStateToProps)(InvoiceListFilters);

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
  suppliers: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
  })).isRequired,
  typeCodes: PropTypes.arrayOf(PropTypes.shape({
    id: PropTypes.string.isRequired,
    value: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
  })).isRequired,
};
