import React, { Component } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';
import ReactTable from 'react-table';
import selectTableHOC from 'react-table/lib/hoc/selectTable';

import {
  createInfoBar,
  hideInfoBar,
  hideSpinner,
  showSpinner,
} from 'actions';
import stockTransferApi from 'api/services/StockTransferApi';
import { TableCell } from 'components/DataTable';
import { STOCK_TRANSFER_URL } from 'consts/applicationUrls';
import DateFormat from 'consts/dateFormat';
import { InfoBar, InfoBarConfigs } from 'consts/infoBar';
import apiClient, { flattenRequest, parseResponse } from 'utils/apiClient';
import customTreeTableHOC from 'utils/CustomTreeTable';
import Filter from 'utils/Filter';
import Translate from 'utils/Translate';
import { formatDate } from 'utils/translation-utils';

import 'react-table/react-table.css';

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
      showExpiredItemsOnly: false,
    };
  }

  componentDidMount() {
    if (this.props.stockTransferTranslationsFetched) {
      this.dataFetched = true;
      // Not passing location id here, because when jumping from gsp to react pages,
      // the redux persist doesn't have enough time to refresh local storage. In this case
      // we want to use location id from the session on backend.
      this.fetchStockTransferCandidates();
    }

    this.props.createInfoBar(InfoBarConfigs[InfoBar.STOCK_TRANSFER_DESCRIPTION]);
  }

  componentWillUnmount() {
    this.props.hideInfoBar(InfoBar.STOCK_TRANSFER_DESCRIPTION);
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
      accessor: 'product.productCode',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="react.stockTransfer.product.label" defaultMessage="Product" />,
      accessor: 'product',
      style: { whiteSpace: 'normal' },
      Cell: (row) => (
        <TableCell
          {...row}
          value={row.value?.displayName ?? row.value?.name}
          tooltip={row.value?.displayName && row.value?.name !== row.value?.displayName}
          tooltipLabel={row.value?.name}
        />
      ),
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
      Cell: (props) => (
        <span>
          {
            props?.value
              ? this.props.formatLocalizedDate(props.value, DateFormat.COMMON)
              : props.value
          }
        </span>
      ),
      Filter,
    }, {
      Header: <Translate id="react.stockTransfer.zone.label" defaultMessage="Zone" />,
      accessor: 'originZone',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="react.stockTransfer.binLocation.label" defaultMessage="Bin Location" />,
      accessor: 'originBinLocation.name',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="react.stockTransfer.quantityOnHand.label" defaultMessage="QOH" />,
      accessor: 'quantityOnHand',
      style: { whiteSpace: 'normal' },
      Cell: (props) => <span>{props.value ? props.value.toLocaleString('en-US') : props.value}</span>,
      Filter,
    }, {
      Header: <Translate id="react.stockTransfer.quantityAvailableToTransfer.label" defaultMessage="Quantity Available to Transfer" />,
      accessor: 'quantityNotPicked',
      style: { whiteSpace: 'normal' },
      Cell: (props) => <span>{props.value ? props.value.toLocaleString('en-US') : props.value}</span>,
      Filter,
    },
  ];

  dataFetched = false;

  switchExpiredItemsFiltering() {
    this.props.showSpinner();
    this.setState((state) => ({
      ...state,
      showExpiredItemsOnly: !state.showExpiredItemsOnly,
    }), async () => {
      await this.fetchStockTransferCandidates();
    });
  }

  /**
   * Fetches available items to stock transfer from API.
   * @public
   */
  fetchStockTransferCandidates(locationId) {
    this.props.showSpinner();
    return stockTransferApi.getStockTransferCandidates(locationId, this.state.showExpiredItemsOnly)
      .then((resp) => {
        const stockTransferCandidates = parseResponse(resp.data.data);
        const stockTransferItems = [];
        stockTransferCandidates.forEach((item) => {
          // this _id is used internally in TreeTable
          const _id = _.uniqueId('item_');
          stockTransferItems.push({ _id, ...item });
        });
        this.setState({ stockTransferItems }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  filterMethod = (filter, row) => {
    let val = row[filter.id];
    if (filter.id === 'expirationDate') {
      val = this.props.formatLocalizedDate(val, DateFormat.COMMON);
    }
    if (filter.id === 'product') {
      val = val ? `${val.name} ${val.displayName}` : null;
    }
    return _.toString(val).toLowerCase().includes(filter.value.toLowerCase());
  };

  /**
   * Sends all changes made by user in this step of stock transfer to API and updates data.
   * @public
   */
  createStockTransfer() {
    this.props.showSpinner();
    const url = '/api/stockTransfers/';
    const payload = {
      stockTransferItems: _.filter(
        this.state.stockTransferItems,
        (item) => _.includes([...this.state.selection], item._id),
      ),
    };

    apiClient.post(url, flattenRequest(payload))
      .then((response) => {
        const stockTransfer = parseResponse(response.data.data);
        this.props.hideSpinner();

        this.props.history.push(STOCK_TRANSFER_URL.createById(stockTransfer.id));
        this.props.nextPage({ stockTransfer });
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Adds all items that are in the current filtered data to selection array.
   * @public
   */
  toggleAll = () => {
    /*
      Select ALL the records that are in the current filtered data
    */
    const { selectAll } = this.state;
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
      if (item && item.quantityNotPicked !== 0) {
        selection.push(item._id);
      }
    });
    this.toggleSelection(selection, !selectAll);
    this.setState({ selectAll: !selectAll });
  };

  /**
   * React table's method which detects the selection state itselfs.
   * @param {string} key
   * @public
   */
  isSelected = (key) =>
    _.includes([...this.state.selection], key);

  /**
   * Adds or deletes item from selection array depending on what user did.
   * @param {object} keys
   * @param {boolean} checked
   * @public
   */
  toggleSelection = (keys, checked) => {
    const { selection: selectionFromState } = this.state;
    const selection = new Set(selectionFromState);
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
    const extraProps = {
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
            {this.state.selection.size}
            {' '}
            <Translate id="react.stockTransfer.selected.label" defaultMessage="Selected" />
          </div>
          <div className="align-items-end h-auto">
            <button
              type="button"
              onClick={() => {
                this.switchExpiredItemsFiltering();
              }}
              className={`btn ${this.state.showExpiredItemsOnly ? 'btn-outline-primary' : 'btn-primary'} btn-xs h-100`}
            >
              <Translate id="" defaultMessage="Expired Items" />
            </button>
            <button
              type="button"
              disabled={this.state.selection.size < 1}
              onClick={() => this.createStockTransfer()}
              className="btn btn-outline-primary btn-form float-right btn-xs h-100"
            >
              <Translate id="react.stockTransfer.startStockTransfer.label" defaultMessage="Start Stock Transfer" />
            </button>
          </div>
        </div>
        {
          stockTransferItems
            ? (
              <SelectTreeTable
                data={stockTransferItems}
                columns={columns}
                ref={(r) => {
                  this.selectTable = r;
                }}
                className="-striped -highlight"
                {...extraProps}
                defaultPageSize={Number.MAX_SAFE_INTEGER}
                minRows={0}
                showPaginationBottom={false}
                filterable
                defaultFilterMethod={this.filterMethod}
                SelectInputComponent={({
                  id,
                  checked,
                  onClick,
                  row,
                }) => (
                  <input
                    type={selectType}
                    checked={checked}
                    disabled={row && row.quantityNotPicked === 0}
                    onChange={() => {}}
                    onClick={(e) => {
                      const { shiftKey } = e;

                      e.stopPropagation();
                      onClick(id, shiftKey, row);
                    }}
                  />
                )}
              />
            )
            : null
        }
        <div className="submit-buttons">
          <button
            type="button"
            disabled={this.state.selection.size < 1}
            onClick={() => this.createStockTransfer()}
            className="btn btn-outline-primary btn-form float-right btn-xs"
          >
            <Translate id="react.stockTransfer.startStockTransfer.label" defaultMessage="Start Stock Transfer" />
          </button>
        </div>
      </div>
    );
  }
}

const mapStateToProps = (state) => ({
  stockTransferTranslationsFetched: state.session.fetchedTranslations.stockTransfer,
  formatLocalizedDate: formatDate(state.localize),
});

const mapDispatchToProps = {
  showSpinner,
  hideSpinner,
  createInfoBar,
  hideInfoBar,
};

export default withRouter(connect(
  mapStateToProps,
  mapDispatchToProps,
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
  formatLocalizedDate: PropTypes.func.isRequired,
  hideInfoBar: PropTypes.func.isRequired,
  createInfoBar: PropTypes.func.isRequired,
};
