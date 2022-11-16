import React, { useEffect, useState } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import { fetchTranslations } from 'actions';
import InvoiceListFilters from 'components/invoice/InvoiceListFilters';
import InvoiceListHeader from 'components/invoice/InvoiceListHeader';
import InvoiceListTable from 'components/invoice/InvoiceListTable';


const InvoiceList = (props) => {
  // Filter params are stored here, to be able to pass them to table component
  const [filterParams, setFilterParams] = useState({});

  useEffect(() => {
    props.fetchTranslations(props.locale, 'invoice');
    props.fetchTranslations(props.locale, 'reactTable');
  }, [props.locale]);


  return (
    <div className="d-flex flex-column list-page-main">
      <InvoiceListHeader />
      <InvoiceListFilters setFilterParams={setFilterParams} />
      <InvoiceListTable filterParams={filterParams} />
    </div>
  );
};

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
});

export default connect(mapStateToProps, { fetchTranslations })(InvoiceList);


InvoiceList.propTypes = {
  locale: PropTypes.string.isRequired,
  fetchTranslations: PropTypes.func.isRequired,
};

