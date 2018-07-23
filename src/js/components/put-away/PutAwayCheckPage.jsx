import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import ReactTable from 'react-table';
import PropTypes from 'prop-types';
import Alert from 'react-s-alert';

import 'react-table/react-table.css';

import customTreeTableHOC from '../../utils/CustomTreeTable';
import apiClient, { parseResponse, flattenRequest } from '../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../actions';

const SelectTreeTable = (customTreeTableHOC(ReactTable));

class PutAwayCheckPage extends Component {
  constructor(props) {
    super(props);
    const { putAway, pivotBy } = this.props;
    const columns = this.getColumns();
    this.state = {
      putAway,
      completed: false,
      columns,
      pivotBy,
      expanded: {},
    };
  }

  onExpandedChange = (expanded) => {
    this.setState({ expanded });
  };

  getColumns = () => [
    {
      Header: 'Code',
      accessor: 'product.productCode',
      style: { whiteSpace: 'normal' },
    }, {
      Header: 'Name',
      accessor: 'product.name',
      style: { whiteSpace: 'normal' },
    }, {
      Header: 'Lot',
      accessor: 'inventoryItem.lotNumber',
      style: { whiteSpace: 'normal' },
    }, {
      Header: 'Expiry',
      accessor: 'inventoryItem.expirationDate',
      style: { whiteSpace: 'normal' },
    }, {
      Header: 'Recipient',
      accessor: 'recipient.name',
      style: { whiteSpace: 'normal' },
    }, {
      Header: 'QTY',
      accessor: 'quantity',
      style: { whiteSpace: 'normal' },
    }, {
      Header: 'Current bin',
      accessor: 'currentBins',
      style: { whiteSpace: 'normal' },
    }, {
      Header: 'Put Away Bin',
      accessor: 'putawayLocation.name',
      style: { whiteSpace: 'normal' },
    }, {
      Header: 'Stock Movement',
      accessor: 'stockMovement.name',
      style: { whiteSpace: 'normal' },
      Expander: ({ isExpanded }) => (
        <span className="ml-2">
          <div className={`rt-expander ${isExpanded && '-open'}`}>&bull;</div>
        </span>
      ),
      filterable: true,
    },
  ];

  toggleTree = () => {
    if (this.state.pivotBy.length) {
      this.setState({ pivotBy: [], expanded: {} });
    } else {
      this.setState({ pivotBy: ['stockMovement.name'], expanded: {} });
    }
  };

  filterMethod = (filter, row) =>
    (row[filter.id] !== undefined ?
      String(row[filter.id].toLowerCase()).includes(filter.value.toLowerCase()) : true);

  savePutAways() {
    this.props.showSpinner();
    const url = '/openboxes/api/putaways';

    return apiClient.post(url, flattenRequest(this.state.putAway))
      .then((response) => {
        const putAway = parseResponse(response.data.data);
        putAway.putawayItems = _.map(putAway.putawayItems, item => ({ _id: _.uniqueId('item_'), ...item }));

        this.props.hideSpinner();

        Alert.success('Put-Away was successfully completed!');

        this.setState({ putAway, completed: true });
      })
      .catch(() => this.props.hideSpinner());
  }

  render() {
    const {
      onExpandedChange, toggleTree,
    } = this;
    const {
      putAway, columns, pivotBy, expanded,
    } = this.state;
    const extraProps =
      {
        pivotBy,
        expanded,
        onExpandedChange,
      };

    return (
      <div className="container-fluid pt-2">
        <h1>Put-Away </h1>
        <div className="mb-2">
          Show by:
          <button
            className="btn btn-primary ml-2"
            data-toggle="button"
            aria-pressed="false"
            onClick={toggleTree}
          >
            {pivotBy && pivotBy.length ? 'Stock Movement' : 'Product'}
          </button>
        </div>
        {
          putAway.putawayItems ?
            <SelectTreeTable
              data={putAway.putawayItems}
              columns={columns}
              className="-striped -highlight"
              {...extraProps}
              defaultPageSize={10}
              filterable
              defaultFilterMethod={this.filterMethod}
              freezWhenExpanded
              defaultSorted={[{ id: 'name' }, { id: 'stockMovement.name' }]}
            />
            : null
        }
        {
          this.state.completed ?
            <button
              type="button"
              className="btn btn-outline-primary float-right my-2"
              onClick={() => this.props.firstPage()}
            >Go back to put-away list
            </button> :
            <div>
              <button
                type="button"
                onClick={() => this.props.prevPage({
                  putAway: this.state.putAway, pivotBy: this.state.pivotBy,
                })}
                className="btn btn-outline-primary my-2"
              >Edit
              </button>
              <button
                type="button"
                onClick={() => this.savePutAways()}
                className="btn btn-outline-primary float-right my-2"
              >Complete Put Away
              </button>
            </div>
        }
      </div>
    );
  }
}

export default connect(null, { showSpinner, hideSpinner })(PutAwayCheckPage);

PutAwayCheckPage.propTypes = {
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  prevPage: PropTypes.func.isRequired,
  firstPage: PropTypes.func.isRequired,
  putAway: PropTypes.shape({
    putawayItems: PropTypes.arrayOf(PropTypes.shape({})),
  }),
  pivotBy: PropTypes.arrayOf(PropTypes.string),
};

PutAwayCheckPage.defaultProps = {
  putAway: [],
  pivotBy: ['stockMovement.name'],
};
