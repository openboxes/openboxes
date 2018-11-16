import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import ReactTable from 'react-table';
import treeTableHOC from 'react-table/lib/hoc/treeTable';

import 'react-table/react-table.css';

import apiClient from './../../utils/apiClient';
import { hideSpinner, showSpinner } from '../../actions';

const TreeTable = treeTableHOC(ReactTable);

const COLUMNS = [
  {
    accessor: 'locationGroup.name',
  },
  {
    accessor: 'location.name',
  },
  {
    Header: 'Monthly demand',
    accessor: 'monthlyDemand',
  },
  {
    Header: 'Stocklist Name',
    accessor: 'name',
  },
  {
    Header: 'Manager',
    accessor: 'manager.name',
  },
  {
    Header: 'Replenishment period',
    accessor: 'replenishmentPeriod',
  },
  {
    Header: 'Maximum  Quantity',
    accessor: 'maxQuantity',
  },
  {
    Header: 'Unit of measure',
    accessor: 'uom',
  },
];

class StocklistManagement extends Component {
  constructor(props) {
    super(props);

    this.state = { data: [] };
  }

  componentWillMount() {
    this.fetchData();
  }

  fetchData() {
    this.props.showSpinner();
    // TODO: add proper endpoint here once it will be added in backend
    const url = '';

    return apiClient.get(url)
      // TODO: extract data from response
      .then(() => this.setState({ data: [] }))
      .catch(this.props.hideSpinner());
  }

  render() {
    const { data } = this.state;
    return (
      <TreeTable
        className="stocklist-table"
        data={data}
        pivotBy={['locationGroup.name', 'location.name']}
        columns={COLUMNS}
        defaultPageSize={20}
        SubComponent={() => <div>Stock List Component</div>}
      />
    );
  }
}

export default connect(null, { showSpinner, hideSpinner })(StocklistManagement);

StocklistManagement.propTypes = {
  hideSpinner: PropTypes.func.isRequired,
  showSpinner: PropTypes.func.isRequired,
};
