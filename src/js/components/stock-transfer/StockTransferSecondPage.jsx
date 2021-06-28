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
import apiClient from '../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../actions';
import Filter from '../../utils/Filter';
import Translate, { translateWithDefaultMessage } from '../../utils/Translate';

const SelectTreeTable = (customTreeTableHOC(ReactTable));

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
      Header: <Translate id="react.stockTransfer.quantityOnHand.label" defaultMessage="QOH" />,
      accessor: 'quantityOnHand',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="react.stockTransfer.currentZone.label" defaultMessage="Current Zone" />,
      accessor: 'zone',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="react.stockTransfer.currentBinLocation.label" defaultMessage="Current Bin Location" />,
      accessor: 'binLocation',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="react.stockTransfer.qtyToTransfer.label" defaultMessage="Qty to Transfer" />,
      accessor: 'transferQty',
      style: { whiteSpace: 'normal' },
      Cell: (props) => {
        const itemIndex = props.index;
        return (
          <Tooltip
            html={this.props.translate(
                'react.stockTransfer.higherQuantity.label',
                'Quantity to transfer cannot be higher than quantity in current bin',
              )}
            disabled={!props.value || props.value <= props.original.quantityOnHand}
            theme="transparent"
            arrow="true"
            delay="150"
            duration="250"
            hideDelay="50"
          >
            <div className={props.value > props.original.quantityOnHand ? 'has-error' : ''}>
              <input
                type="number"
                className="form-control form-control-xs"
                value={props.value}
                onChange={(event) => {
              const stockTransfer = update(this.state.stockTransfer, {
                // eslint-disable-next-line max-len
                stockTransferItems: { [itemIndex]: { transferQty: { $set: event.target.value } } },
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
      accessor: 'transferBin',
      Cell: (cellInfo) => {
        const splitItems = _.get(this.state.stockTransfer.stockTransferItems, `[${cellInfo.index}].splitItems`);

        if (splitItems && splitItems.length > 0) {
          return <Translate id="react.stockTransfer.splitLine.label" defaultMessage="Split line" />;
        }

        return (<Select
          options={this.state.bins}
          objectValue
          value={_.get(this.state.stockTransfer.stockTransferItems, `[${cellInfo.index}].${cellInfo.column.id}`) || null}
          onChange={value => this.changeStockTransfer(update(this.state.stockTransfer, {
            stockTransferItems: { [cellInfo.index]: { transferBin: { $set: value } } },
          }))}
          className="select-xs"
        />);
      },
      Filter,
    }, {
      Header: '',
      accessor: 'splitItems',
      Cell: cellInfo => (
        <div className="d-flex flex-row flex-wrap">
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

    const url = `/openboxes/api/stockTransfer/${this.props.match.params.id}`;

    apiClient.get(url)
      .then(() => {
        // TODO add after fetching API is done, using mocks for testing purposes
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

        this.setState({ stockTransfer: { stockTransferItems } }, () => this.props.hideSpinner());
      });
  }

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
  // eslint-disable-next-line class-methods-use-this
  saveStockTransfer(stockTransfer, callback) {
    callback();
  }

  changeStockTransfer(stockTransfer) {
    this.setState({ stockTransfer });
  }

  deleteItem(itemIndex) {
    this.props.showSpinner();
    const url = `/openboxes/api/stockTransferItems/${_.get(this.state.stockTransfer.stockTransferItems, `[${itemIndex}].id`)}`;

    apiClient.delete(url)
      .then(() => {
        const stockTransfer = update(this.state.stockTransfer, {
          stockTransferItems: {
            $splice: [
              [itemIndex, 1],
            ],
          },
        });

        this.setState({ stockTransfer });
        this.props.hideSpinner();
      })
      // TODO update catch after API with deleting item is done
      .catch(() => {
        const stockTransfer = update(this.state.stockTransfer, {
          stockTransferItems: {
            $splice: [
              [itemIndex, 1],
            ],
          },
        });

        this.setState({ stockTransfer });
        this.props.hideSpinner();
      });
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
        <div className="d-flex justify-content-end mb-3 submit-buttons">
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
            />
            : null
        }
        <div className="submit-buttons">
          <button
            type="button"
            onClick={() => this.nextPage()}
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
  translate: PropTypes.func.isRequired,
  /** All stock transfer's data */
  initialValues: PropTypes.shape({
    stockTransfer: PropTypes.arrayOf(PropTypes.shape({})),
  }).isRequired,
  match: PropTypes.shape({
    params: PropTypes.shape({
      id: PropTypes.string,
    }),
  }).isRequired,
  /** Location (currently chosen). To be used in internalLocations and stock transfer requests. */
  location: PropTypes.shape({
    id: PropTypes.string,
  }).isRequired,
  stockTransferTranslationsFetched: PropTypes.bool.isRequired,
};
