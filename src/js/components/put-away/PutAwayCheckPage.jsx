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
    const columns = this.getColumns();

    const {
      initialValues:
      {
        putAway, pivotBy, expanded,
      },
    } = this.props;
    this.state = {
      putAway: {
        ...putAway,
        putawayItems: PutAwayCheckPage.processSplitLines(putAway.putawayItems),
      },
      completed: putAway.putawayStatus === 'COMPLETED',
      columns,
      pivotBy,
      expanded,
      location: this.props.location,
    };

    this.confirmEmptyBin = this.confirmEmptyBin.bind(this);
    this.confirmLowerQuantity = this.confirmLowerQuantity.bind(this);
    this.save = this.save.bind(this);
  }

  componentWillReceiveProps(nextProps) {
    showLocationChangedAlert(
      this.props.translate, this.state.location, nextProps.location,
      () => { window.location = '/openboxes/order/list?orderType=PUTAWAY_ORDER&status=PENDING'; },
    );

    const location = this.state.location.id ? this.state.location : nextProps.location;
    this.setState({
      putAway: {
        ...nextProps.initialValues.putAway,
        putawayItems: PutAwayCheckPage
          .processSplitLines(nextProps.initialValues.putAway.putawayItems),
      },
      completed: nextProps.initialValues.putAway.putawayStatus === 'COMPLETED',
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
      Cell: props => (
        <div>
          {props.value && props.value.zoneName ? <div>{props.value.zoneName}:&nbsp;</div> : ''}
          <div>{props.value ? props.value.name : ''}</div>
        </div>
      ),
      Filter,
    }, {
      Header: <Translate id="react.putAway.currentBin.label" defaultMessage="Current bin" />,
      accessor: 'currentBins',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="react.putAway.putAwayBin.label" defaultMessage="Putaway Bin" />,
      accessor: 'putawayLocation',
      style: { whiteSpace: 'normal' },
      Cell: props => (
        <div>
          {props.value && props.value.zoneName ? <div>{props.value.zoneName}:&nbsp;</div> : ''}
          <div>{props.value ? props.value.name : ''}</div>
        </div>
      ),
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
    const isBinLocationChosen = !_.some(
      this.props.initialValues.putAway.putawayItems,
      putAwayItem =>
        _.isNull(putAwayItem.putawayLocation.id) && _.isEmpty(putAwayItem.splitItems),
    );

    const itemsWithLowerQuantity = _.filter(
      this.props.initialValues.putAway.putawayItems,
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
      ...this.props.initialValues.putAway,
      putawayStatus: 'COMPLETED',
      putawayItems: _.map(this.props.initialValues.putAway.putawayItems, item => ({
        ...item,
        putawayStatus: item.splitItems.length ? 'CANCELLED' : 'COMPLETED',
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
        window.location = `/openboxes/order/show/${this.props.initialValues.putAway.id}`;
      })
      .catch(() => this.props.hideSpinner());
  }

  goToFirstPage() {
    this.props.history.push('/openboxes/putAway/create');
    this.props.goToPage(1, null);
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
      <div className="putaway">
        <div className="d-flex justify-content-between mb-2 putaway-buttons">
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
          <span className="buttons-container classic-form-buttons">
            {this.state.completed ?
              <button
                type="button"
                className="btn btn-outline-secondary btn-xs mr-3"
                onClick={() => this.goToFirstPage()}
              ><Translate id="react.putAway.goBack.label" defaultMessage="Go back to putaway list" />
              </button> :
              <div>
                <button
                  type="button"
                  onClick={() => this.props.previousPage({
                    putAway: this.state.putAway,
                    pivotBy: this.state.pivotBy,
                    expanded: this.state.expanded,
                  })}
                  className="btn btn-outline-secondary btn-xs mr-3"
                ><Translate id="react.default.button.edit.label" defaultMessage="Edit" />
                </button>
                <button
                  type="button"
                  onClick={() => this.completePutAway()}
                  className="btn btn-outline-secondary btn-xs mr-3"
                ><Translate id="react.putAway.completePutAway.label" defaultMessage="Complete Putaway" />
                </button>
              </div>
            }
          </span>
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
        <div className="submit-buttons">
          {
            this.state.completed ?
              <button
                type="button"
                className="btn btn-outline-primary btn-form btn-xs"
                onClick={() => this.goToFirstPage()}
              ><Translate id="react.putAway.goBack.label" defaultMessage="Go back to putaway list" />
              </button> :
              <div className="submit-buttons">
                <button
                  type="button"
                  onClick={() => this.completePutAway()}
                  className="btn btn-outline-primary btn-form float-right btn-xs"
                ><Translate id="react.putAway.completePutAway.label" defaultMessage="Complete Putaway" />
                </button>
                <button
                  type="button"
                  onClick={() => this.props.previousPage({
                    putAway: this.state.putAway,
                    pivotBy: this.state.pivotBy,
                    expanded: this.state.expanded,
                  })}
                  className="btn btn-outline-primary btn-form btn-xs"
                ><Translate id="react.default.button.edit.label" defaultMessage="Edit" />
                </button>
              </div>
          }
        </div>
      </div>
    );
  }
}

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps, { showSpinner, hideSpinner })(PutAwayCheckPage);

PutAwayCheckPage.propTypes = {
  initialValues: PropTypes.shape({
    putAway: PropTypes.arrayOf(PropTypes.shape({})),
    pivotBy: PropTypes.arrayOf(PropTypes.shape({})),
    expanded: PropTypes.arrayOf(PropTypes.shape({})),
  }).isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** Location (currently chosen). To be used in internalLocations and putaways requests. */
  location: PropTypes.shape({
    id: PropTypes.string,
  }).isRequired,
  history: PropTypes.shape({
    push: PropTypes.func,
  }).isRequired,
  translate: PropTypes.func.isRequired,
  previousPage: PropTypes.func.isRequired,
  goToPage: PropTypes.func.isRequired,
};

PutAwayCheckPage.defaultProps = {};
