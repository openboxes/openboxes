import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import ReactTable from 'react-table';
import PropTypes from 'prop-types';
import update from 'immutability-helper';
import fileDownload from 'js-file-download';
import { getTranslate } from 'react-localize-redux';
import { Tooltip } from 'react-tippy';
import { confirmAlert } from 'react-confirm-alert';

import 'react-table/react-table.css';

import customTreeTableHOC from '../../utils/CustomTreeTable';
import Select from '../../utils/Select';
import SplitLineModal from './SplitLineModal';
import apiClient, { parseResponse, flattenRequest } from '../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../actions';
import Filter from '../../utils/Filter';
import showLocationChangedAlert from '../../utils/location-change-alert';
import Translate, { translateWithDefaultMessage } from '../../utils/Translate';

const SelectTreeTable = (customTreeTableHOC(ReactTable));

/**
 * The second page of put-away where user can choose put-away bin, split a line
 * or generate put-away list(pdf). It can be sorted either by shipment or by product.
 */
class PutAwaySecondPage extends Component {
  constructor(props) {
    super(props);
    const {
      putAway, pivotBy, expanded, location,
    } = this.props;
    this.getColumns = this.getColumns.bind(this);
    this.fetchItems = this.fetchItems.bind(this);
    const columns = this.getColumns();

    this.state = {
      columns,
      pivotBy,
      expanded,
      bins: [],
      location,
      sortBy: putAway.sortBy,
    };
  }

  componentDidMount() {
    if (this.props.putAwayTranslationsFetched) {
      this.dataFetched = true;
      this.fetchBins();
    }
  }

  componentWillReceiveProps(nextProps) {
    showLocationChangedAlert(
      this.props.translate, this.state.location, nextProps.location,
      () => { window.location = '/openboxes/order/list?orderTypeCode=TRANSFER_ORDER&status=PENDING'; },
    );

    const location = this.state.location.id ? this.state.location : nextProps.location;
    this.setState({ location });

    if (nextProps.putAwayTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;
      this.fetchBins();
    }
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

    this.setState({ expanded });
  };

  /**
   * Returns an array of columns which are passed to the table.
   * @public
   */
  getColumns = () => [
    {
      Header: <Translate id="react.putAway.code.label" defaultMessage="Code" />,
      accessor: 'product.productCode',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="react.putAway.name.label" defaultMessage="Name" />,
      accessor: 'product.name',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="react.putAway.lotSerialNo.label" defaultMessage="Lot/Serial No." />,
      accessor: 'inventoryItem.lotNumber',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="react.putAway.expiry.label" defaultMessage="Expiry" />,
      accessor: 'inventoryItem.expirationDate',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="react.putAway.recipient.label" defaultMessage="Recipient" />,
      accessor: 'recipient.name',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="react.putAway.qty.label" defaultMessage="QTY" />,
      accessor: 'quantity',
      style: { whiteSpace: 'normal' },
      Cell: (props) => {
        const itemIndex = props.index;
        const edit = _.get(this.props.putAway.putawayItems, `[${itemIndex}].edit`);
        if (edit) {
          return (
            <Tooltip
              html={this.props.translate(
                'react.putAway.higherQuantity.label',
                'Quantity cannot be higher than original putaway item quantity',
              )}
              disabled={props.value <= props.original.quantityAvailable}
              theme="transparent"
              arrow="true"
              delay="150"
              duration="250"
              hideDelay="50"
            >
              <div className={props.value > props.original.quantityAvailable ? 'has-error' : ''}>
                <input
                  type="number"
                  className="form-control form-control-xs"
                  value={props.value}
                  onChange={(event) => {
              const putAway = update(this.props.putAway, {
                putawayItems: { [itemIndex]: { quantity: { $set: event.target.value } } },
              });

              this.props.changePutAway(putAway);
            }}
                />
              </div>
            </Tooltip>);
        }

        return (
          <Tooltip
            html={this.props.translate(
              'react.putAway.higherQuantity.label',
              'Quantity cannot be higher than original putaway item quantity',
            )}
            disabled={props.original && props.value <= props.original.quantityAvailable}
            theme="transparent"
            arrow="true"
            delay="150"
            duration="250"
            hideDelay="50"
          >
            <div className={props.original && props.value > props.original.quantityAvailable ? 'has-error' : ''}>
              <span>{props.value ? props.value.toLocaleString('en-US') : props.value}</span>
            </div>
          </Tooltip>
        );
      },
      Filter,
    }, {
      Header: <Translate id="react.putAway.preferredBin.label" defaultMessage="Preferred bin" />,
      accessor: 'preferredBin',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="react.putAway.currentBin.label" defaultMessage="Current bin" />,
      accessor: 'currentBins',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="react.putAway.stockMovement.label" defaultMessage="Stock Movement" />,
      accessor: 'stockMovement.name',
      style: { whiteSpace: 'normal' },
      Expander: ({ isExpanded }) => (
        <span className="ml-2">
          <div className={`rt-expander ${isExpanded && '-open'}`}>&bull;</div>
        </span>
      ),
      filterable: true,
      Filter,
    }, {
      Header: <Translate id="react.putAway.putAwayBin.label" defaultMessage="Putaway Bin" />,
      accessor: 'putawayLocation',
      Cell: (cellInfo) => {
        const splitItems = _.get(this.props.putAway.putawayItems, `[${cellInfo.index}].splitItems`);

        if (splitItems && splitItems.length > 0) {
          return <Translate id="react.putAway.splitLine.label" defaultMessage="Split line" />;
        }

        return (<Select
          options={this.state.bins}
          objectValue
          value={_.get(this.props.putAway.putawayItems, `[${cellInfo.index}].${cellInfo.column.id}`) || null}
          onChange={value => this.props.changePutAway(update(this.props.putAway, {
            putawayItems: { [cellInfo.index]: { putawayLocation: { $set: value } } },
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
          <SplitLineModal
            putawayItem={this.props.putAway.putawayItems[cellInfo.index]}
            splitItems={_.get(this.props.putAway.putawayItems, `[${cellInfo.index}].${cellInfo.column.id}`)}
            saveSplitItems={(splitItems) => {
              this.saveSplitItems(splitItems, cellInfo.index);
            }}
            bins={this.state.bins}
          />
          <button
            className="btn btn-outline-primary btn-xs mr-1 mb-1"
            onClick={() => this.editItem(cellInfo.index)}
          ><Translate id="react.default.button.edit.label" defaultMessage="Edit" />
          </button>
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
   * Changes the way od displaying table depending on after which element
   * user wants to sort it by.
   * @public
   */
  toggleTree = () => {
    if (this.state.pivotBy.length) {
      this.setState({ pivotBy: [], expanded: {} });
    } else {
      this.setState({ pivotBy: ['stockMovement.name'], expanded: {} });
    }
  };

  /**
   * Method that is passed to react table's option: defaultFilterMethod.
   * It filters rows and converts a string to lowercase letters.
   * @param {object} filter
   * @param {object} row
   * @public
   */
  filterMethod = (filter, row) => {
    // eslint-disable-next-line no-underscore-dangle
    if (row._aggregated || row._groupedByPivot) {
      return true;
    }

    let val = row[filter.id];
    if (filter.id === 'putawayLocation') {
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

    return apiClient.get(url)
      .then((response) => {
        const bins = _.map(response.data.data, bin => (
          { value: { id: bin.id, name: bin.name }, label: bin.name }
        ));
        const expanded = {};
        _.forEach(bins, (item, index) => { expanded[index] = true; });
        this.setState({ bins, expanded }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Save put-away with new split items.
   * @public
   */
  saveSplitItems(splitItems, itemIndex) {
    const putAway = update(this.props.putAway, {
      putawayItems: { [itemIndex]: { splitItems: { $set: splitItems } } },
    });

    this.props.savePutAways(putAway);
  }

  editItem(itemIndex) {
    const putAway = update(this.props.putAway, {
      putawayItems: {
        [itemIndex]: {
          edit: { $set: true },
          splitItems: {
            $set: _.map(_.filter(
              this.props.putAway.putawayItems[itemIndex].splitItems,
              item => item.id,
            ), item => (
              { ...item, delete: true }
            )),
          },
        },
      },
    });

    this.props.changePutAway(putAway);
  }

  deleteItem(itemIndex) {
    this.props.showSpinner();
    const url = `/openboxes/api/putawayItems/${_.get(this.props.putAway.putawayItems, `[${itemIndex}].id`)}`;

    apiClient.delete(url)
      .then(() => {
        const putAway = update(this.props.putAway, {
          putawayItems: {
            $splice: [
              [itemIndex, 1],
            ],
          },
        });

        this.props.changePutAway(putAway);
        this.props.hideSpinner();
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Save put-away and go to next page.
   * @public
   */
  nextPage() {
    if (_.some(this.props.putAway.putawayItems, putawayItem =>
      putawayItem.quantity > putawayItem.quantityAvailable)) {
      confirmAlert({
        title: this.props.translate('react.putAway.message.putAwayError.label', 'Putaway error'),
        message: this.props.translate(
          'react.putAway.putAwayAlert.message',
          'Cannot put away more than is available in the receiving bin. Reduce quantity of items in red to match the quantity in the receiving bin.',
        ),
        buttons: [
          {
            label: this.props.translate('react.default.ok.label', 'OK'),
          },
        ],
      });
    } else {
      this.props.savePutAways(this.props.putAway, (putAway) => {
        this.props.nextPage({
          putAway,
          pivotBy: this.state.pivotBy,
          expanded: this.state.expanded,
        });
      });
    }
  }

  /**
   * Generates a pdf that shows what should be putted away.
   * @public
   */
  generatePutAwayList() {
    this.props.showSpinner();
    const url = '/openboxes/putAway/generatePdf/ff80818164ae89800164affcfe6e0001';
    const { putawayNumber } = this.props.putAway;

    return apiClient.post(url, flattenRequest(this.props.putAway), { responseType: 'blob' })
      .then((response) => {
        fileDownload(response.data, `PutawayReport${putawayNumber ? `-${putawayNumber}` : ''}.pdf`, 'application/pdf');
        this.fetchItems(this.state.sortBy);
        this.props.hideSpinner();
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Sort putaway items in order chosen by the user
   * @public
   */
  sortPutawayItems() {
    let { sortBy } = this.state;

    switch (sortBy) {
      case 'currentBins':
        sortBy = 'preferredBin';
        break;
      case 'preferredBin':
        sortBy = '';
        break;
      default:
        sortBy = 'currentBins';
        break;
    }

    this.setState({ sortBy });
    this.fetchItems(sortBy);
  }

  fetchItems(sortBy) {
    const url = `/openboxes/api/putaways/${this.props.putAway.id}?sortBy=${sortBy}`;
    return apiClient.get(url)
      .then((response) => {
        this.props.changePutAway({
          ...this.props.putAway,
          sortBy,
          putawayItems: parseResponse(response.data.data.putawayItems),
        });
      })
      .catch(() => this.props.hideSpinner());
  }

  render() {
    const {
      onExpandedChange, toggleTree,
    } = this;
    const {
      columns, pivotBy, expanded, sortBy,
    } = this.state;
    const extraProps =
      {
        pivotBy,
        expanded,
        onExpandedChange,
      };

    return (
      <div className="putaway-wrap">
        <h1><Translate id="react.putAway.putAway.label" defaultMessage="Putaway -" /> {this.props.putAway.putawayNumber}</h1>
        <div className="d-flex justify-content-between mb-2">
          <div>
            <Translate id="react.putAway.showBy.label" defaultMessage="Show by" />:
            <button
              className="btn btn-primary ml-2 btn-xs"
              data-toggle="button"
              aria-pressed="false"
              onClick={toggleTree}
            >
              {pivotBy && pivotBy.length ?
                <Translate id="react.putAway.stockMovement.label" defaultMessage="Stock Movement" />
                : <Translate id="react.putAway.product.label" defaultMessage="Product" /> }
            </button>
          </div>
          <div>
            <button
              type="button"
              onClick={() => this.sortPutawayItems()}
              className="btn btn-outline-secondary btn-xs mr-3"
            >
              <span>
                {this.props.translate(
                  /* eslint-disable no-nested-ternary */
                  `react.putAway.${!sortBy ? 'sortByCurrentBins' : (sortBy === 'currentBins' ? 'sortByPreferredBin' : 'originalOrder')}.label`,
                  !sortBy ? 'Sort by current bins' : (sortBy === 'currentBins' ? 'Sort by preferred bin' : 'Original order'),
                )}
              </span>
            </button>
            <button
              className="btn btn-outline-secondary btn-xs mr-3"
              onClick={() => this.generatePutAwayList()}
            >
              <span><i className="fa fa-print pr-2" /><Translate id="react.putAway.generateList.label" defaultMessage="Generate Putaway list" /></span>
            </button>
            <button
              type="button"
              onClick={() => this.props.savePutAways(this.props.putAway)}
              className="btn btn-outline-secondary btn-xs"
              disabled={_.some(this.props.putAway.putawayItems, putawayItem =>
                putawayItem.quantity > putawayItem.quantityAvailable)}
            ><Translate id="react.default.button.save.label" defaultMessage="Save" />
            </button>
          </div>
          <button
            type="button"
            onClick={() => this.nextPage()}
            className="btn btn-outline-primary align-self-end btn-xs"
          ><Translate id="react.default.button.next.label" defaultMessage="Next" />
          </button>
        </div>
        {
          this.props.putAway.putawayItems ?
            <SelectTreeTable
              data={this.props.putAway.putawayItems}
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
        <button
          type="button"
          onClick={() => this.nextPage()}
          className="btn btn-outline-primary float-right my-2 btn-xs"
        ><Translate id="react.default.button.next.label" defaultMessage="Next" />
        </button>
      </div>
    );
  }
}

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  putAwayTranslationsFetched: state.session.fetchedTranslations.putAway,
});

export default connect(mapStateToProps, { showSpinner, hideSpinner })(PutAwaySecondPage);

PutAwaySecondPage.propTypes = {
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** Function taking user to the next page */
  nextPage: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  /** All put-away's data */
  putAway: PropTypes.shape({
    /** An array of all put-away's items */
    putawayItems: PropTypes.arrayOf(PropTypes.shape({
      splitItems: PropTypes.arrayOf(PropTypes.shape({})),
    })),
    id: PropTypes.string,
    putawayNumber: PropTypes.string,
  }),
  /** An array of available attributes after which a put-away can be sorted by */
  pivotBy: PropTypes.arrayOf(PropTypes.string),
  /** List of currently expanded put-away's items */
  expanded: PropTypes.shape({}),
  /** Location (currently chosen). To be used in internalLocations and putaways requests. */
  location: PropTypes.shape({
    id: PropTypes.string,
  }).isRequired,
  changePutAway: PropTypes.func.isRequired,
  savePutAways: PropTypes.func.isRequired,
  putAwayTranslationsFetched: PropTypes.bool.isRequired,
};

PutAwaySecondPage.defaultProps = {
  putAway: {},
  pivotBy: ['stockMovement.name'],
  expanded: {},
};
