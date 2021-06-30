import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import ReactTable from 'react-table';
import selectTableHOC from 'react-table/lib/hoc/selectTable';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';

import 'react-table/react-table.css';

import customTreeTableHOC from '../../utils/CustomTreeTable';
import { showSpinner, hideSpinner } from '../../actions';
import Filter from '../../utils/Filter';
import Translate from '../../utils/Translate';
import apiClient from '../../utils/apiClient';

const SelectTreeTable = selectTableHOC(customTreeTableHOC(ReactTable));

/* eslint-disable no-underscore-dangle */

function getNodes(data, node = []) {
  data.forEach((item) => {
    if (Object.prototype.hasOwnProperty.call(item, '_subRows') && item._subRows) {
      // eslint-disable-next-line no-param-reassign
      node = getNodes(item._subRows, node);
    } else {
      node.push(item._original);
    }
  });
  return node;
}

/**
 * The first page of stock transfer where user chooses product to transfer
 */
class CreateStockTransfer extends Component {
  constructor(props) {
    super(props);
    const columns = this.getColumns();
    this.state = {
      stockTransferItems: [],
      columns,
      selection: new Set(),
      selectAll: false,
      selectType: 'checkbox',
    };
  }

  componentDidMount() {
    if (this.props.stockTransferTranslationsFetched) {
      this.dataFetched = true;
      this.fetchStockTransferCandidates(this.props.locationId);
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.stockTransferTranslationsFetched) {
      if (!this.dataFetched) {
        this.dataFetched = true;

        this.fetchStockTransferCandidates(this.props.locationId);
      } else if (this.props.locationId !== nextProps.locationId) {
        this.fetchStockTransferCandidates(nextProps.locationId);
      }
    }
  }

  /**
   * Returns an array of columns which are passed to the table.
   * @public
   */
  getColumns = () => [
    {
      Header: <Translate id="react.stockTransfer.code.label" defaultMessage="Code" />,
      accessor: 'productCode',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="react.stockTransfer.product.label" defaultMessage="Product" />,
      accessor: 'productName',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="react.stockTransfer.lot.label" defaultMessage="Lot" />,
      accessor: 'lotNumber',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="react.stockTransfer.expiry.label" defaultMessage="Expiry" />,
      accessor: 'expirationDate',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="react.stockTransfer.zone.label" defaultMessage="Zone" />,
      accessor: 'zone',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="react.stockTransfer.binLocation.label" defaultMessage="Bin Location" />,
      accessor: 'binLocation',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="react.stockTransfer.quantityOnHand.label" defaultMessage="QOH" />,
      accessor: 'quantityOnHand',
      style: { whiteSpace: 'normal' },
      Cell: props => <span>{props.value ? props.value.toLocaleString('en-US') : props.value}</span>,
      Filter,
    },
  ];

  dataFetched = false;

  /**
   * Fetches available items to stock transfer from API.
   * @public
   */
  fetchStockTransferCandidates(locationId) {
    this.props.showSpinner();
    const url = `/openboxes/api/stockTransfers?location.id=${locationId}`;

    return apiClient.get(url)
      .then(() => {
        // TODO add after API for fetching is implemented, using mocks for testing purpose
      })
      .catch(() => {
        const stockTransferItems = [{
          _id: 1,
          productCode: 'code2',
          productName: 'product1',
          lotNumber: 'lot3',
          expirationDate: '7/11/2021',
          zone: 'zone2',
          binLocation: 'bin2',
          quantityOnHand: 45,
        }, {
          _id: 2,
          productCode: 'code2',
          productName: 'product1',
          lotNumber: 'lot2',
          expirationDate: '7/1/2021',
          zone: 'zone1',
          binLocation: 'bin1',
          quantityOnHand: 51,
        }, {
          _id: 3,
          productCode: 'code2',
          productName: 'product2',
          lotNumber: 'lot3',
          expirationDate: '7/22/2021',
          zone: 'zone2',
          binLocation: 'bin1',
          quantityOnHand: 88,
        }, {
          _id: 4,
          productCode: 'code2',
          productName: 'product3',
          lotNumber: 'lot2',
          expirationDate: '7/25/2021',
          zone: 'zone3',
          binLocation: 'bin1',
          quantityOnHand: 41,
        }, {
          _id: 5,
          productCode: 'code2',
          productName: 'product2',
          lotNumber: 'lot3',
          expirationDate: '7/2/2021',
          zone: 'zone3',
          binLocation: 'bin1',
          quantityOnHand: 43,
        }];

        this.setState({ stockTransferItems }, () => this.props.hideSpinner());
      });
  }

  /**
   * Sends all changes made by user in this step of stock transfer to API and updates data.
   * @public
   */
  // TODO after API for creating stock transfer implemented
  createStockTransfer() {
    this.props.nextPage();
  }

  /**
   * Adds all items that are in the current filtered data to selection array.
   * @public
   */
  toggleAll = () => {
    /*
      Select ALL the records that are in the current filtered data
    */
    const selectAll = !this.state.selectAll;
    const selection = [];
    // we need to get at the internals of ReactTable
    const wrappedInstance = this.selectTable.getWrappedInstance();
    // the 'sortedData' property contains the currently accessible
    // records based on the filter and sort
    const currentRecords = wrappedInstance.getResolvedState().sortedData;
    // we need to get all the 'real' (original) records out to get at their IDs
    const nodes = getNodes(currentRecords);
    // we just push all the IDs onto the selection array
    nodes.forEach((item) => {
      selection.push(item._id);
    });
    this.toggleSelection(selection, selectAll);
    this.setState({ selectAll });
  };

  /**
   * React table's method which detects the selection state itselfs.
   * @param {string} key
   * @public
   */
  isSelected = key =>
    _.includes([...this.state.selection], key);

  /**
   * Adds or deletes item from selection array depending on what user did.
   * @param {object} keys
   * @param {boolean} checked
   * @public
   */
  toggleSelection = (keys, checked) => {
    const selection = new Set(this.state.selection);
    if (Array.isArray(keys)) {
      if (checked) {
        _.forEach(keys, (item) => {
          selection.add(item);
        });
      } else {
        _.forEach(keys, (item) => {
          selection.delete(item);
        });
      }
    } else if (selection.has(keys)) {
      selection.delete(keys);
    } else if (!selection.has(keys)) {
      selection.add(keys);
    }
    this.setState({ selection });
  };

  render() {
    const {
      toggleSelection, toggleAll, isSelected,
    } = this;
    const {
      stockTransferItems, columns, selectAll, selectType,
    } = this.state;
    const extraProps =
      {
        selectAll,
        isSelected,
        toggleAll,
        toggleSelection,
        selectType,
      };

    return (
      <div className="stock-transfer">
        <div className="d-flex justify-content-between stock-transfer-buttons">
          <div className="count-selected ">
            {this.state.selection.size} <Translate id="react.stockTransfer.selected.label" defaultMessage="Selected" />
          </div>
          <button
            type="button"
            disabled={this.state.selection.size < 1}
            onClick={() => this.createStockTransfer()}
            className="btn btn-outline-primary btn-form float-right btn-xs"
          ><Translate id="react.stockTransfer.startStockTransfer.label" defaultMessage="Start Stock Transfer" />
          </button>
        </div>
        {
          stockTransferItems ?
            <SelectTreeTable
              data={stockTransferItems}
              columns={columns}
              ref={(r) => { this.selectTable = r; }}
              className="-striped -highlight"
              {...extraProps}
              defaultPageSize={Number.MAX_SAFE_INTEGER}
              minRows={0}
              showPaginationBottom={false}
              filterable
              SelectInputComponent={({
                id, checked, onClick, row,
              }) => (
                <input
                  type={selectType}
                  checked={checked}
                  onChange={() => {}}
                  onClick={(e) => {
                    const { shiftKey } = e;

                    e.stopPropagation();
                    onClick(id, shiftKey, row);
                  }}
                />)}
            />
            : null
        }
        <div className="submit-buttons">
          <button
            type="button"
            disabled={this.state.selection.size < 1}
            onClick={() => this.createStockTransfer()}
            className="btn btn-outline-primary btn-form float-right btn-xs"
          ><Translate id="react.stockTransfer.startStockTransfer.label" defaultMessage="Start Stock Transfer" />
          </button>
        </div>
      </div>
    );
  }
}

const mapStateToProps = state => ({
  stockTransferTranslationsFetched: state.session.fetchedTranslations.stockTransfer,
});

export default withRouter(connect(
  mapStateToProps,
  {
    showSpinner, hideSpinner,
  },
)(CreateStockTransfer));

CreateStockTransfer.propTypes = {
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** Function taking user to the next page */
  nextPage: PropTypes.func.isRequired,
  /** Location ID (currently chosen). To be used in stock transfer requests. */
  locationId: PropTypes.string.isRequired,
  /** React router's object used to manage session history */
  history: PropTypes.shape({ push: PropTypes.func }).isRequired,
  stockTransferTranslationsFetched: PropTypes.bool.isRequired,
};
