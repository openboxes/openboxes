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

/* eslint-disable no-underscore-dangle */
/* eslint-disable no-param-reassign */

function getNodes(data, node = []) {
  data.forEach((item) => {
    if (Object.prototype.hasOwnProperty.call(item, '_subRows') && item._subRows) {
      node = getNodes(item._subRows, node);
    } else {
      node.push(item._original);
    }
  });
  return node;
}

/**
 * The last page of put-away which shows everything that user has chosen to put away.
 * Split lines are shown as seperate lines.
 */
class PutAwayCheckPage extends Component {
  constructor(props) {
    super(props);
    const { putAway, pivotBy, expanded } = this.props;
    const columns = this.getColumns();
    this.state = {
      putAway,
      completed: false,
      columns,
      pivotBy,
      expanded,
      expandedRowsCount: 0,
    };
  }

  /**
   * Called when an expander is clicked. Checks expanded rows and counts their number.
   * @param {object} expanded
   * @public
   */
  onExpandedChange = (expanded) => {
    const expandedRecordsIds = [];

    _.forEach(expanded, (value, key) => {
      if (value) {
        expandedRecordsIds.push(parseInt(key, 10));
      }
    });

    const allCurrentRows = this.selectTable
      .getWrappedInstance().getResolvedState().sortedData;
    const expandedRows = _.at(allCurrentRows, expandedRecordsIds);
    const expandedRowsCount = getNodes(expandedRows).length;

    this.setState({ expanded, expandedRowsCount });
  };

  /**
   * Returns an array of columns which are passed to the table.
   * @public
   */
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
      Cell: props => <span>{props.value ? props.value.toLocaleString('en-US') : props.value}</span>,
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

  /**
   * Changes the way od displaying table depending on after which element
   * user wants to sort it by.
   * @public
   */
  toggleTree = () => {
    if (this.state.pivotBy.length) {
      this.setState({ pivotBy: [], expanded: {}, expandedRowsCount: 0 });
    } else {
      this.setState({ pivotBy: ['stockMovement.name'], expanded: {}, expandedRowsCount: 0 });
    }
  };

  /**
   * Method that is passed to react table's option: defaultFilterMethod.
   * It filters rows and converts a string to lowercase letters.
   * @param {object} row
   * @param {object} filter
   * @public
   */
  filterMethod = (filter, row) =>
    (row[filter.id] !== undefined ?
      String(row[filter.id].toLowerCase()).includes(filter.value.toLowerCase()) : true);

  /**
   * Sends all changes made by user in this step of put-away to API and updates data.
   * @public
   */
  savePutAways() {
    this.props.showSpinner();
    const url = '/openboxes/api/putaways';
    const payload = { ...this.state.putAway, putawayStatus: 'COMPLETE' };

    return apiClient.post(url, flattenRequest(payload))
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
        <h1>Put-Away - {this.state.putAway.putawayNumber}</h1>
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
              defaultPageSize={Number.MAX_SAFE_INTEGER}
              minRows={pivotBy && pivotBy.length ?
                10 - this.state.expandedRowsCount : 10}
              style={{
                height: '500px',
              }}
              showPaginationBottom={false}
              filterable
              defaultFilterMethod={this.filterMethod}
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
                  putAway: this.state.putAway,
                  pivotBy: this.state.pivotBy,
                  expanded: this.state.expanded,
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
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** Function returning user to the previous page */
  prevPage: PropTypes.func.isRequired,
  /** Function taking user to the first page */
  firstPage: PropTypes.func.isRequired,
  /** All put-away's data */
  putAway: PropTypes.shape({
    /** An array of all put-away's items */
    putawayItems: PropTypes.arrayOf(PropTypes.shape({})),
  }),
  /** An array of available attributes after which a put-away can be sorted by */
  pivotBy: PropTypes.arrayOf(PropTypes.string),
  /** List of currently expanded put-away's items */
  expanded: PropTypes.shape({}),
};

PutAwayCheckPage.defaultProps = {
  putAway: [],
  pivotBy: ['stockMovement.name'],
  expanded: {},
};
