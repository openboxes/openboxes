import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import ReactTable from 'react-table';
import PropTypes from 'prop-types';
import update from 'immutability-helper';
import fileDownload from 'js-file-download';

import 'react-table/react-table.css';

import customTreeTableHOC from '../../utils/CustomTreeTable';
import Select from '../../utils/Select';
import SplitLineModal from './SplitLineModal';
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
 * The second page of put-away where user can choose put-away bin, split a line
 * or generate put-away list(pdf). It can be sorted either by shipment or by product.
 */
class PutAwaySecondPage extends Component {
  constructor(props) {
    super(props);
    const { putAway, pivotBy, expanded } = this.props;
    this.getColumns = this.getColumns.bind(this);
    const columns = this.getColumns();
    this.state = {
      putAway,
      columns,
      pivotBy,
      expanded,
      bins: [],
      expandedRowsCount: 0,
    };
  }

  componentDidMount() {
    this.fetchBins();
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
      Header: 'Stock Movement',
      accessor: 'stockMovement.name',
      style: { whiteSpace: 'normal' },
      Expander: ({ isExpanded }) => (
        <span className="ml-2">
          <div className={`rt-expander ${isExpanded && '-open'}`}>&bull;</div>
        </span>
      ),
      filterable: true,
    }, {
      Header: 'Put Away Bin',
      accessor: 'putawayLocation.id',
      Cell: (cellInfo) => {
        const splitItems = _.get(this.state.putAway.putawayItems, `[${cellInfo.index}].splitItems`);

        if (splitItems && splitItems.length > 0) {
          return 'Split line';
        }

        return (<Select
          options={this.state.bins}
          value={_.get(this.state.putAway.putawayItems, `[${cellInfo.index}].${cellInfo.column.id}`) || null}
          onChange={value => this.setState({
            putAway: update(this.state.putAway, {
              putawayItems: { [cellInfo.index]: { putawayLocation: { id: { $set: value } } } },
            }),
          })}
        />);
      },
    }, {
      Header: '',
      accessor: 'splitItems',
      Cell: cellInfo => (
        <SplitLineModal
          putawayItem={this.state.putAway.putawayItems[cellInfo.index]}
          splitItems={_.get(this.state.putAway.putawayItems, `[${cellInfo.index}].${cellInfo.column.id}`)}
          saveSplitItems={splitItems => this.setState({
            putAway: update(this.state.putAway, {
              putawayItems: { [cellInfo.index]: { [cellInfo.column.id]: { $set: splitItems } } },
            }),
          })}
          bins={this.state.bins}
        />),
      filterable: false,
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
   * @param {object} filter
   * @param {object} row
   * @public
   */
  filterMethod = (filter, row) =>
    (row[filter.id] !== undefined ?
      String(row[filter.id].toLowerCase()).includes(filter.value.toLowerCase()) : true);

  /**
   * Fetches available bin locations from API.
   * @public
   */
  fetchBins() {
    this.props.showSpinner();
    const url = '/openboxes/api/internalLocations';

    return apiClient.get(url)
      .then((response) => {
        const bins = _.map(response.data.data, bin => (
          { value: bin.id, label: bin.name }
        ));
        this.setState({ bins }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
  * Sends all changes made by user in this step of put-away to API and updates data.
  * @public
  */
  savePutAways() {
    this.props.showSpinner();
    const url = '/openboxes/api/putaways';

    return apiClient.post(url, flattenRequest(this.state.putAway))
      .then((response) => {
        const putAway = parseResponse(response.data.data);
        putAway.putawayItems = _.map(putAway.putawayItems, item => ({ _id: _.uniqueId('item_'), ...item }));

        this.props.hideSpinner();

        this.props.nextPage({
          putAway,
          pivotBy: this.state.pivotBy,
          expanded: this.state.expanded,
        });
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Generates a pdf that shows what should be putted away.
   * @public
   */
  generatePutAwayList() {
    this.props.showSpinner();
    const url = '/openboxes/putAway/generatePdf/ff80818164ae89800164affcfe6e0001';
    const { putawayNumber } = this.state.putAway;

    return apiClient.post(url, flattenRequest(this.state.putAway), { responseType: 'blob' })
      .then((response) => {
        fileDownload(response.data, `PutawayReport${putawayNumber ? `-${putawayNumber}` : ''}.pdf`, 'application/pdf');
        this.props.hideSpinner();
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
          <button
            className="float-right btn btn-outline-secondary align-self-end"
            onClick={() => this.generatePutAwayList()}
          >
            <span><i className="fa fa-print pr-2" />Generate Put-Away list</span>
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
        <button
          type="button"
          onClick={() => this.savePutAways()}
          className="btn btn-outline-primary float-right my-2"
        >Next
        </button>
      </div>
    );
  }
}

export default connect(null, { showSpinner, hideSpinner })(PutAwaySecondPage);

PutAwaySecondPage.propTypes = {
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** Function taking user to the next page */
  nextPage: PropTypes.func.isRequired,
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

PutAwaySecondPage.defaultProps = {
  putAway: {},
  pivotBy: ['stockMovement.name'],
  expanded: {},
};
