import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import ReactTable from 'react-table';
import PropTypes from 'prop-types';
import update from 'immutability-helper';
import { getTranslate } from 'react-localize-redux';
import { Tooltip } from 'react-tippy';

import 'react-table/react-table.css';

import customTreeTableHOC from '../../utils/CustomTreeTable';
import Select from '../../utils/Select';
import apiClient, { flattenRequest, parseResponse } from '../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../actions';
import Filter from '../../utils/Filter';
import Translate, { translateWithDefaultMessage } from '../../utils/Translate';
import { extractStockTransferItems, prepareRequest } from './utils';

const SelectTreeTable = (customTreeTableHOC(ReactTable));

const APPROVED = 'APPROVED';
const CANCELED = 'CANCELED';

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
      bins: [],
    };
  }

  componentDidMount() {
    if (this.props.stockTransferTranslationsFetched) {
      this.dataFetched = true;
      this.fetchBins();
    }
    this.fetchStockTransfer();
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.stockTransferTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;
      this.fetchBins();
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
      accessor: 'product.name',
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
      Header: <Translate id="react.stockTransfer.quantityOnHand.label" defaultMessage="QOH" />,
      accessor: 'quantityOnHand',
      style: { whiteSpace: 'normal' },
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
      Cell: (props) => {
        const itemIndex = props.index;
        const { stockTransferItems } = this.state.stockTransfer;
        const splitItems = _.filter(stockTransferItems, lineItem =>
          lineItem.referenceId && lineItem.referenceId === props.original.referenceId);
        let disabled = false;
        let disabledMessage;
        if (!props.original.id || splitItems.length > 1) {
          const quantityToTransfer = _.reduce(
            splitItems, (sum, val) =>
              (sum + (val.quantity ? _.toInteger(val.quantity) : 0)),
            0,
          );
          if (quantityToTransfer > props.original.quantityOnHand) {
            _.forEach(stockTransferItems, (lineItem) => {
              _.forEach(splitItems, (splitItem) => {
                if (lineItem === splitItem) {
                  disabled = true;
                  disabledMessage = this.props.translate(
                    'react.stockTransfer.higherSplitQuantity.label',
                    'Sum of quantity to transfer of split items cannot be higher than quantity in current bin.',
                  );
                }
              });
            });
          }
        } else if (splitItems.length === 1 &&
          props.original.quantityOnHand < _.toInteger(props.value)) {
          disabled = true;
          disabledMessage = this.props.translate(
            'react.stockTransfer.higherQuantity.label',
            'Quantity to transfer cannot be higher than quantity in current bin',
          );
        }

        if (!_.toInteger(props.value)) {
          disabled = true;
          disabledMessage = this.props.translate(
            'react.stockTransfer.selectOrDeleteLine.label',
            'Please select a quantity or delete the line',
          );
        } else if (_.toInteger(props.value) > props.original.quantityOnHand) {
          disabled = true;
          disabledMessage = this.props.translate(
            'react.stockTransfer.higherThanQoH.label',
            'Cant transfer more than on hand.',
          );
        } else if (_.toInteger(props.value) < 0) {
          disabled = true;
          disabledMessage = this.props.translate(
            'react.stockTransfer.errors.negativeQty.label',
            'Quantity to transfer can\'t be negative',
          );
        }

        return (
          <Tooltip
            html={disabledMessage}
            disabled={!disabled}
            theme="transparent"
            arrow="true"
            delay="150"
            duration="250"
            hideDelay="50"
          >
            <div className={disabled && props.original.status !== CANCELED ? 'has-error' : ''}>
              <input
                type="number"
                className="form-control form-control-xs"
                value={props.value}
                disabled={props.original.status === CANCELED}
                onChange={(event) => {
              const stockTransfer = update(this.state.stockTransfer, {
                stockTransferItems: { [itemIndex]: { quantity: { $set: event.target.value } } },
              });
              this.changeStockTransfer(stockTransfer);
            }}
              />
            </div>
          </Tooltip>);
      },
      Filter,
    }, {
      Header: <Translate id="react.stockTransfer.transferTo.label" defaultMessage="Transfer to" />,
      accessor: 'destinationBinLocation',
      Cell: cellInfo => (<Select
        options={this.state.bins}
        objectValue
        value={_.get(this.state.stockTransfer.stockTransferItems, `[${cellInfo.index}].${cellInfo.column.id}`) || null}
        onChange={value => this.changeStockTransfer(update(this.state.stockTransfer, {
          stockTransferItems: { [cellInfo.index]: { destinationBinLocation: { $set: value } } },
        }))}
        className="select-xs"
        disabled={cellInfo.original.status === CANCELED}
      />),
      Filter,
    }, {
      Header: '',
      accessor: 'splitItems',
      Cell: cellInfo => (
        <div className="d-flex flex-row flex-wrap">
          {!cellInfo.original.referenceId && (
            <button
              className="btn btn-outline-success btn-xs mr-1 mb-1"
              onClick={() => this.splitItem(cellInfo)}
            ><Translate id="react.stockTransfer.splitItem.label" defaultMessage="Split line" />
            </button>
          )}
          <button
            className="btn btn-outline-danger btn-xs mb-1"
            onClick={() => this.deleteItem(cellInfo.index)}
          ><Translate id="react.default.button.delete.label" defaultMessage="Delete" />
          </button>
        </div>),
      filterable: false,
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
    const url = `/openboxes/api/stockTransfers/${this.props.match.params.stockTransferId}`;

    apiClient.get(url)
      .then((response) => {
        const stockTransfer = parseResponse(response.data.data);
        const stockTransferItems = extractStockTransferItems(stockTransfer);

        this.setState(
          { stockTransfer: { ...stockTransfer, stockTransferItems } },
          () => this.props.hideSpinner(),
        );
      })
      .catch(() => this.props.hideSpinner());
  }

  filterMethod = (filter, row) => {
    let val = row[filter.id];
    if (filter.id === 'destinationBinLocation') {
      val = _.get(val, 'name');
    }

    return _.toString(val).toLowerCase().includes(filter.value.toLowerCase());
  };

  /**
   * Fetches available bin locations from API.
   * @public
   */
  fetchBins() {
    this.props.showSpinner();
    const url = `/openboxes/api/internalLocations?location.id=${this.props.location.id}&locationTypeCode=BIN_LOCATION`;

    const mapBins = bins => (_.chain(bins)
      .map(bin => ({
        value: {
          id: bin.id, name: bin.name, zoneId: bin.zoneId, zoneName: bin.zoneName,
        },
        label: bin.name,
      }))
      .orderBy(['label'], ['asc']).value()
    );

    return apiClient.get(url)
      .then((response) => {
        const binGroups = _.partition(response.data.data, bin => (bin.zoneName));
        const binsWithZone = _.chain(binGroups[0]).groupBy('zoneName')
          .map((value, key) => ({ label: key, options: mapBins(value) }))
          .orderBy(['label'], ['asc'])
          .value();
        const binsWithoutZone = mapBins(binGroups[1]);
        this.setState(
          { bins: [...binsWithZone, ...binsWithoutZone] },
          () => this.props.hideSpinner(),
        );
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Sends all changes made by user in this step of stock transfer to API and updates data.
   * @public
   */
  saveStockTransfer(data, callback) {
    const url = '/openboxes/api/stockTransfers/';
    const payload = prepareRequest(data, APPROVED);
    apiClient.post(url, flattenRequest(payload))
      .then((response) => {
        const stockTransfer = parseResponse(response.data.data);
        const stockTransferItems = extractStockTransferItems(stockTransfer);

        this.setState(
          { stockTransfer: { ...stockTransfer, stockTransferItems } },
          () => {
            this.props.hideSpinner();
            if (callback) {
              callback(stockTransfer);
            }
          },
        );
      })
      .catch(() => this.props.hideSpinner());
  }

  changeStockTransfer(stockTransfer) {
    this.setState({ stockTransfer });
  }

  deleteItem(itemIndex) {
    this.props.showSpinner();

    const itemToDelete = _.get(this.state.stockTransfer.stockTransferItems, `[${itemIndex}]`);

    if (itemToDelete.id) {
      const url = `/openboxes/api/stockTransferItems/${itemToDelete.id}`;
      apiClient.delete(url)
        .then((response) => {
          const stockTransfer = parseResponse(response.data.data);
          const stockTransferItems = extractStockTransferItems(stockTransfer);

          this.setState(
            { stockTransfer: { ...stockTransfer, stockTransferItems } },
            () => this.props.hideSpinner(),
          );
        })
        .catch(() => this.props.hideSpinner());
    } else {
      let stockTransfer = update(this.state.stockTransfer, {
        stockTransferItems: {
          $splice: [
            [itemIndex, 1],
          ],
        },
      });

      const originalItem = _.find(
        stockTransfer.stockTransferItems,
        item => item.id === itemToDelete.referenceId,
      );
      const splitItems = _.filter(
        stockTransfer.stockTransferItems,
        item => item.referenceId === originalItem.id,
      );

      if (splitItems.length === 0 && originalItem) {
        const originalItemIndex = _.findIndex(stockTransfer.stockTransferItems, originalItem);
        stockTransfer = update(stockTransfer, {
          stockTransferItems: { [originalItemIndex]: { status: { $set: 'PENDING' } } },
        });
      }

      this.setState({ stockTransfer });
      this.props.hideSpinner();
    }
  }

  /**
   * Save stock transfer and go to next page.
   * @public
   */
  nextPage() {
    this.saveStockTransfer(this.state.stockTransfer, (stockTransfer) => {
      this.props.nextPage({
        stockTransfer,
      });
    });
  }

  /**
   * Generates stock transfer pdf
   * @public
   */
  // eslint-disable-next-line class-methods-use-this
  generateStockTransfer() {
    // TODO add in another ticket
  }

  splitItem(row) {
    const { index, original } = row;

    const newLine = {
      ...original,
      referenceId: original.id ? original.id : original.referenceId,
      id: null,
      status: null,
    };

    const stockTransfer = update(this.state.stockTransfer, {
      stockTransferItems: {
        // If splitting not yet canceled item, then cancel original row and add two new split lines
        // else if splitting already CANCELED line add a new line once
        $splice: original.status !== CANCELED ? [
          [index + 1, 0, newLine],
          [index + 1, 0, newLine],
        ] : [
          [index + 1, 0, newLine],
        ],
        [index]: { $set: original.id ? { ...original, status: CANCELED, quantity: '' } : { ...original, quantity: '' } },
      },
    });

    this.setState({ stockTransfer });
  }

  autofill() {
    const { stockTransfer } = this.state;
    this.setState({
      stockTransfer: {
        ...stockTransfer,
        stockTransferItems: _.map(stockTransfer.stockTransferItems, item => ({
          ...item,
          quantity: item.quantityOnHand,
        })),
      },
    });
  }

  isDisabled() {
    const { stockTransferItems } = this.state.stockTransfer;

    return stockTransferItems && !!stockTransferItems.find((item) => {
      const { quantity, quantityOnHand, status } = item;

      if (status !== 'CANCELED' && (!quantity || quantity > quantityOnHand || quantity <= 0)) {
        return true;
      }

      const splitItems = _.filter(stockTransferItems, lineItem =>
        lineItem.referenceId && lineItem.referenceId === item.referenceId);

      if (!item.id || splitItems.length > 1) {
        const quantityToTransfer = _.reduce(
          splitItems, (sum, val) =>
            (sum + (val.quantity ? _.toInteger(val.quantity) : 0)),
          0,
        );
        if (quantityToTransfer > quantityOnHand) {
          return true;
        }
      }
      return false;
    });
  }

  render() {
    const {
      columns, pivotBy,
    } = this.state;
    const extraProps =
      {
        pivotBy,
      };

    return (
      <div className="stock-transfer">
        <div className="d-flex">
          <div className="submit-buttons">
            <button
              type="button"
              onClick={() => this.autofill(this.state.stockTransfer)}
              className="btn btn-primary btn-form btn-xs"
            ><Translate id="react.partialReceiving.autofillQuantities.label" defaultMessage="Autofill quantities" />
            </button>
          </div>
          <div className="d-flex mb-3 justify-content-end submit-buttons">
            <button
              type="button"
              onClick={() => this.saveStockTransfer(this.state.stockTransfer)}
              className="btn btn-success btn-form btn-xs"
              disabled={_.some(this.state.stockTransfer.stockTransferItems, stockTransferItem =>
              stockTransferItem.quantity > stockTransferItem.quantityAvailable)}
            ><span><i className="fa fa-save pr-2" /><Translate id="react.default.button.save.label" defaultMessage="Save" /></span>
            </button>
            <button
              className="btn btn-outline-success btn-xs mr-3"
              onClick={() => this.generateStockTransfer()}
            >
              <span><i className="fa fa-print pr-2" /><Translate id="react.stockTransfer.generateStockTransfer.label" defaultMessage="Generate Stock Transfer" /></span>
            </button>
          </div>
        </div>
        {
          this.state.stockTransfer.stockTransferItems ?
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
            : null
        }
        <div className="submit-buttons">
          <button
            type="button"
            onClick={() => this.nextPage()}
            disabled={this.isDisabled()}
            className="btn btn-outline-primary btn-form float-right btn-xs"
          ><Translate id="react.default.button.next.label" defaultMessage="Next" />
          </button>
        </div>
      </div>
    );
  }
}

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  stockTransferTranslationsFetched: state.session.fetchedTranslations.stockTransfer,
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
};
