import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import ReactTable from 'react-table';
import PropTypes from 'prop-types';
import Alert from 'react-s-alert';
import { getTranslate } from 'react-localize-redux';
import { confirmAlert } from 'react-confirm-alert';

import 'react-table/react-table.css';
import 'react-confirm-alert/src/react-confirm-alert.css';

import customTreeTableHOC from '../../utils/CustomTreeTable';
import apiClient, { flattenRequest } from '../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../actions';
import Filter from '../../utils/Filter';
import showLocationChangedAlert from '../../utils/location-change-alert';
import Translate, { translateWithDefaultMessage } from '../../utils/Translate';

const SelectTreeTable = (customTreeTableHOC(ReactTable));

/**
 * The last page of put-away which shows everything that user has chosen to put away.
 * Split lines are shown as seperate lines.
 */
class PutAwayCheckPage extends Component {
  static processSplitLines(putawayItems) {
    const newItems = [];

    if (putawayItems) {
      putawayItems.forEach((item) => {
        if (item.splitItems && item.splitItems.length > 0) {
          item.splitItems.forEach((splitItem) => {
            newItems.push({
              ...item,
              quantity: splitItem.quantity,
              putawayFacility: splitItem.putawayFacility,
              putawayLocation: splitItem.putawayLocation,
              splitItems: [],
            });
          });
        } else {
          newItems.push(item);
        }
      });
    }

    return newItems;
  }

  constructor(props) {
    super(props);
    const {
      putAway, pivotBy, expanded, location,
    } = this.props;
    const columns = this.getColumns();
    this.state = {
      putAway: {
        ...putAway,
        putawayItems: PutAwayCheckPage.processSplitLines(putAway.putawayItems),
      },
      completed: putAway.putawayStatus === 'COMPLETED',
      columns,
      pivotBy,
      expanded,
      location,
    };

    this.confirmEmptyBin = this.confirmEmptyBin.bind(this);
    this.confirmLowerQuantity = this.confirmLowerQuantity.bind(this);
    this.save = this.save.bind(this);
  }

  componentWillReceiveProps(nextProps) {
    showLocationChangedAlert(
      this.props.translate, this.state.location, nextProps.location,
      () => { window.location = '/openboxes/order/list?orderTypeCode=TRANSFER_ORDER&status=PENDING'; },
    );

    const location = this.state.location.id ? this.state.location : nextProps.location;
    this.setState({
      putAway: {
        ...nextProps.putAway,
        putawayItems: PutAwayCheckPage.processSplitLines(nextProps.putAway.putawayItems),
      },
      completed: nextProps.putAway.putawayStatus === 'COMPLETED',
      location,
    });
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
      Cell: props => <span>{props.value ? props.value.toLocaleString('en-US') : props.value}</span>,
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
      Header: <Translate id="react.putAway.putAwayBin.label" defaultMessage="Putaway Bin" />,
      accessor: 'putawayLocation.name',
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
    },
  ];

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
   * @param {object} row
   * @param {object} filter
   * @public
   */
  // eslint-disable-next-line no-underscore-dangle
  filterMethod = (filter, row) => (row._aggregated || row._groupedByPivot
    || _.toString(row[filter.id]).toLowerCase().includes(filter.value.toLowerCase()));

  /**
   * Sends all changes made by user in this step of put-away to API and updates data.
   * @public
   */
  completePutAway() {
    const isBinLocationChosen = !_.some(this.props.putAway.putawayItems, putAwayItem =>
      _.isNull(putAwayItem.putawayLocation.id) && _.isEmpty(putAwayItem.splitItems));

    const itemsWithLowerQuantity = _.filter(
      this.props.putAway.putawayItems,
      putAwayItem => putAwayItem.quantity < putAwayItem.quantityAvailable,
    );

    if (!_.isEmpty(itemsWithLowerQuantity)) {
      this.confirmLowerQuantity(itemsWithLowerQuantity);
    } else if (!isBinLocationChosen) {
      this.confirmEmptyBin();
    } else {
      this.save();
    }
  }

  save() {
    this.props.showSpinner();
    const url = `/openboxes/api/putaways?location.id=${this.state.location.id}`;
    const payload = {
      ...this.props.putAway,
      putawayStatus: 'COMPLETED',
      putawayItems: _.map(this.props.putAway.putawayItems, item => ({
        ...item,
        putawayStatus: 'COMPLETED',
        splitItems: _.map(item.splitItems, splitItem => ({
          ...splitItem,
          putawayStatus: 'COMPLETED',
        })),
      })),
    };

    return apiClient.post(url, flattenRequest(payload))
      .then(() => {
        this.props.hideSpinner();

        Alert.success(this.props.translate('react.putAway.alert.putAwayCompleted.label', 'Putaway was successfully completed!'), { timeout: 3000 });

        this.props.firstPage();
      })
      .catch(() => this.props.hideSpinner());
  }


  /**
   * Shows confirmation dialog on complete if there are items with empty bin location.
   * @public
   */
  confirmEmptyBin() {
    confirmAlert({
      title: this.props.translate('react.putAway.message.confirmPutAway.label', 'Confirm putaway'),
      message: this.props.translate(
        'react.putAway.confirmPutAway.message',
        'Are you sure you want to putaway? There are some lines with empty bin locations.',
      ),
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

  /**
   * Shows confirmation dialog on complete if there are items with quantity in receiving bin
   * @public
   */
  confirmLowerQuantity(items) {
    confirmAlert({
      title: this.props.translate('react.putAway.message.confirmPutAway.label', 'Confirm putaway'),
      message: _.map(items, item =>
        (
          <p>Qty {item.quantityAvailable - item.quantity} {this.props.translate('react.putAway.alert.lowerQty1.label', 'of item')} {' '}
            {item.product.name} {this.props.translate('react.putAway.alert.lowerQty2.label', 'is still in the receiving bin. Do you want to continue?')}
          </p>)),
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
      <div className="putaway-wrap">
        <h1><Translate id="react.putAway.putAway.label" defaultMessage="Putaway -" /> {this.state.putAway.putawayNumber}</h1>
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
          {this.state.completed ?
            <button
              type="button"
              className="btn btn-outline-primary float-right mb-2 btn-xs"
              onClick={() => this.props.firstPage()}
            ><Translate id="react.putAway.goBack.label" defaultMessage="Go back to putaway list" />
            </button> :
            <div>
              <button
                type="button"
                onClick={() => this.props.prevPage({
                  putAway: this.props.putAway,
                  pivotBy: this.state.pivotBy,
                  expanded: this.state.expanded,
                })}
                className="btn btn-outline-primary mb-2 btn-xs mr-2"
              ><Translate id="react.default.button.edit.label" defaultMessage="Edit" />
              </button>
              <button
                type="button"
                onClick={() => this.completePutAway()}
                className="btn btn-outline-primary float-right mb-2 btn-xs"
              ><Translate id="react.putAway.completePutAway.label" defaultMessage="Complete Putaway" />
              </button>
            </div>
          }
        </div>
        {
          putAway.putawayItems ?
            <SelectTreeTable
              data={putAway.putawayItems}
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
        {
          this.state.completed ?
            <button
              type="button"
              className="btn btn-outline-primary float-right my-2 btn-xs"
              onClick={() => this.props.firstPage()}
            ><Translate id="react.putAway.goBack.label" defaultMessage="Go back to putaway list" />
            </button> :
            <div>
              <button
                type="button"
                onClick={() => this.completePutAway()}
                className="btn btn-outline-primary float-right my-2 btn-xs"
              ><Translate id="react.putAway.completePutAway.label" defaultMessage="Complete Putaway" />
              </button>
              <button
                type="button"
                onClick={() => this.props.prevPage({
                  putAway: this.props.putAway,
                  pivotBy: this.state.pivotBy,
                  expanded: this.state.expanded,
                })}
                className="btn btn-outline-primary float-right mr-2 my-2 btn-xs"
              ><Translate id="react.default.button.edit.label" defaultMessage="Edit" />
              </button>
            </div>
        }
      </div>
    );
  }
}

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps, { showSpinner, hideSpinner })(PutAwayCheckPage);

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
    /** Status of the put-away */
    putawayStatus: PropTypes.string,
  }),
  /** An array of available attributes after which a put-away can be sorted by */
  pivotBy: PropTypes.arrayOf(PropTypes.string),
  /** List of currently expanded put-away's items */
  expanded: PropTypes.shape({}),
  /** Location (currently chosen). To be used in internalLocations and putaways requests. */
  location: PropTypes.shape({
    id: PropTypes.string,
  }).isRequired,
  translate: PropTypes.func.isRequired,
};

PutAwayCheckPage.defaultProps = {
  putAway: {},
  pivotBy: ['stockMovement.name'],
  expanded: {},
};
