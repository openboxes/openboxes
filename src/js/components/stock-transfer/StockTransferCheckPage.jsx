import React, { Component } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import Alert from 'react-s-alert';
import ReactTable from 'react-table';

import { hideSpinner, showSpinner } from 'actions';
import { TableCell } from 'components/DataTable';
import { extractNonCanceledItems } from 'components/stock-transfer/utils';
import { STOCK_TRANSFER_URL } from 'consts/applicationUrls';
import DateFormat from 'consts/dateFormat';
import apiClient, { flattenRequest, parseResponse } from 'utils/apiClient';
import customTreeTableHOC from 'utils/CustomTreeTable';
import Filter from 'utils/Filter';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';
import { formatDate } from 'utils/translation-utils';

import 'react-table/react-table.css';
import 'react-confirm-alert/src/react-confirm-alert.css';

const SelectTreeTable = (customTreeTableHOC(ReactTable));

const COMPLETED = 'COMPLETED';

/**
 * The second page of stock transfer where user can choose qty and bin to transfer
 * or generate stock transfer list(pdf).
 */
class StockTransferSecondPage extends Component {
  constructor(props) {
    super(props);
    this.getColumns = this.getColumns.bind(this);
    const columns = this.getColumns();

    const {
      initialValues:
      {
        stockTransfer,
      },
    } = this.props;

    this.state = {
      stockTransfer: stockTransfer || {},
      columns,
    };

    this.completeStockTransfer = this.completeStockTransfer.bind(this);
  }

  componentDidMount() {
    if (this.props.stockTransferTranslationsFetched) {
      this.dataFetched = true;
    }
    this.fetchStockTransfer();
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.stockTransferTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;
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
      Header: <Translate id="react.stockTransfer.quantityAvailableToTransfer.label" defaultMessage="Quantity Available to Transfer" />,
      accessor: 'quantityNotPicked',
      style: { whiteSpace: 'normal' },
      width: 220,
      Filter,
    }, {
      Header: <Translate id="react.stockTransfer.currentZone.label" defaultMessage="Current Zone" />,
      accessor: 'originZone',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="react.stockTransfer.currentBinLocation.label" defaultMessage="Current Bin Location" />,
      accessor: 'originBinLocation.name',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="react.stockTransfer.qtyToTransfer.label" defaultMessage="Qty to Transfer" />,
      accessor: 'quantity',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="react.stockTransfer.transferTo.label" defaultMessage="Transfer to" />,
      accessor: 'destinationBinLocation.name',
      style: { whiteSpace: 'normal' },
      Filter,
    },
  ];

  dataFetched = false;

  /**
   * Fetches stock transfer items and sets them in redux form and in
   * state as current line items.
   * @public
   */

  fetchStockTransfer() {
    this.props.showSpinner();
    const url = `/api/stockTransfers/${this.props.match.params.stockTransferId}`;

    apiClient.get(url)
      .then((response) => {
        const stockTransfer = parseResponse(response.data.data);
        const stockTransferItems = extractNonCanceledItems(stockTransfer);

        this.setState(
          {
            stockTransfer: { ...stockTransfer, stockTransferItems },
            originalItems: stockTransfer.stockTransferItems,
          },
          () => this.props.hideSpinner(),
        );
      })
      .catch(() => this.props.hideSpinner());
  }

  filterMethod = (filter, row) => {
    let val = row[filter.id];
    if (filter.id === 'product') {
      val = val ? `${val.name} ${val.displayName}` : null;
    }
    return _.toString(val).toLowerCase().includes(filter.value.toLowerCase());
  };

  /**
   * Sends all changes made by user in this step of stock transfer to API and updates data.
   * @public
   */
  save() {
    this.props.showSpinner();
    const url = `/api/stockTransfers/${this.props.match.params.stockTransferId}`;

    const payload = {
      ...this.state.stockTransfer,
      stockTransferItems: this.state.originalItems,
      status: COMPLETED,
    };

    return apiClient.put(url, flattenRequest(payload))
      .then(() => {
        this.props.hideSpinner();
        Alert.success(this.props.translate('react.stockTransfer.alert.stockTransferCompleted.label', 'Stock transfer was successfully completed!'), { timeout: 3000 });
        window.location = STOCK_TRANSFER_URL.show(this.state.stockTransfer.id);
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Sends all changes made by user in this step of stock transfer to API and updates data.
   * @public
   */
  completeStockTransfer() {
    const itemsWithLowerQuantity = _.filter(
      this.state.stockTransfer.stockTransferItems,
      (stockTransferItem) => stockTransferItem.transferQty < stockTransferItem.quantityNotPicked,
    );

    if (!_.isEmpty(itemsWithLowerQuantity)) {
      this.confirmLowerQuantity(itemsWithLowerQuantity);
    } else {
      this.save();
    }
  }

  /**
   * Shows confirmation dialog on complete if qty to be transferred>=qty on hand in original bin
   * @public
   */
  confirmLowerQuantity(items) {
    confirmAlert({
      title: this.props.translate('react.stockTransfer.message.confirmStockTransfer.label', 'Confirm Stock Transfer'),
      message: _.map(items, (item) =>
        (
          <p>
            Qty
            {item.quantityNotPicked - item.transferQty}
            {' '}
            {this.props.translate('react.stockTransfer.alert.lowerQty1.label', 'of item')}
            {' '}
            {' '}
            {item.productName}
            {' '}
            {this.props.translate('react.stockTransfer.alert.lowerQty2.label', 'is still in the receiving bin. Do you want to continue?')}
          </p>
        )),
      buttons: [
        {
          label: this.props.translate('react.default.yes.label', 'Yes'),
          onClick: () => this.save(),
        },
        {
          label: this.props.translate('react.default.no.label', 'No'),
        },
      ],
    });
  }

  previousPage() {
    this.props.showSpinner();
    const url = `/api/stockTransfers/${this.props.match.params.stockTransferId}`;

    const payload = {
      ...this.state.stockTransfer,
      stockTransferItems: this.state.originalItems,
    };

    return apiClient.put(url, flattenRequest(payload))
      .then(() => {
        this.props.hideSpinner();
        this.props.previousPage({ stockTransfer: this.state.stockTransfer });
      })
      .catch(() => this.props.hideSpinner());
  }

  render() {
    const {
      columns, pivotBy,
    } = this.state;
    const extraProps = {
      pivotBy,
    };

    return (
      <div className="stock-transfer">
        {this.state.stockTransfer && this.state.stockTransfer.status === 'COMPLETED'
          && (
          <div className="submit-buttons d-flex justify-content-end mb-3 mt-0">
            <button
              type="button"
              onClick={() => {
                window.location = STOCK_TRANSFER_URL.show(this.state.stockTransfer.id);
              }}
              className="btn btn-outline-primary btn-form btn-xs"
              hidden={this.state.stockTransfer.status !== 'COMPLETED'}
            >
              <Translate id="react.default.button.exit.label" defaultMessage="Exit" />
            </button>
          </div>
          )}
        {
          this.state.stockTransfer.stockTransferItems
            ? (
              <SelectTreeTable
                data={this.state.stockTransfer.stockTransferItems}
                columns={columns}
                ref={(r) => { this.selectTable = r; }}
                className="-striped -highlight"
                {...extraProps}
                defaultPageSize={Number.MAX_SAFE_INTEGER}
                minRows={0}
                showPaginationBottom={false}
                filterable
                defaultFilterMethod={this.filterMethod}
              />
            )
            : null
        }
        <div className="submit-buttons">
          <button
            type="button"
            onClick={() => this.previousPage()}
            className="btn btn-outline-primary btn-form btn-xs"
            disabled={this.state.stockTransfer.status === 'COMPLETED'}
          >
            <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
          </button>
          <button
            type="button"
            onClick={() => this.completeStockTransfer()}
            className="btn btn-outline-success float-right btn-xs mr-3"
            disabled={this.state.stockTransfer.status === 'COMPLETED'}
          >
            <Translate id="react.stockTransfer.completeStockTransfer.label" defaultMessage="Complete Stock Transfer" />
          </button>
        </div>
      </div>
    );
  }
}

const mapStateToProps = (state) => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  stockTransferTranslationsFetched: state.session.fetchedTranslations.stockTransfer,
  formatLocalizedDate: formatDate(state.localize),
});

export default connect(
  mapStateToProps,
  {
    showSpinner, hideSpinner,
  },
)(StockTransferSecondPage);

StockTransferSecondPage.propTypes = {
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** Function taking user to the next page */
  nextPage: PropTypes.func.isRequired,
  /** Function taking user to the previous page */
  previousPage: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  /** All stock transfer's data */
  initialValues: PropTypes.shape({
    stockTransfer: PropTypes.arrayOf(PropTypes.shape({})),
  }).isRequired,
  match: PropTypes.shape({
    params: PropTypes.shape({
      stockTransferId: PropTypes.string,
    }),
  }).isRequired,
  /** Location (currently chosen). To be used in internalLocations and stock transfer requests. */
  location: PropTypes.shape({
    id: PropTypes.string,
  }).isRequired,
  stockTransferTranslationsFetched: PropTypes.bool.isRequired,
  formatLocalizedDate: PropTypes.func.isRequired,
};
