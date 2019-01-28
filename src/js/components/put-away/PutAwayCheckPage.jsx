import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import ReactTable from 'react-table';
import PropTypes from 'prop-types';
import Alert from 'react-s-alert';
import { getTranslate, Translate } from 'react-localize-redux';
import { confirmAlert } from 'react-confirm-alert';

import 'react-table/react-table.css';
import 'react-confirm-alert/src/react-confirm-alert.css';

import customTreeTableHOC from '../../utils/CustomTreeTable';
import apiClient, { parseResponse, flattenRequest } from '../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../actions';
import Filter from '../../utils/Filter';
import showLocationChangedAlert from '../../utils/location-change-alert';

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

    this.confirmPutaway = this.confirmPutawayco.bind(this);
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
      Header: <Translate id="stockMovement.code.label" />,
      accessor: 'product.productCode',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="stockMovement.name.label" />,
      accessor: 'product.name',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="stockMovement.lotSerialNo.label" />,
      accessor: 'inventoryItem.lotNumber',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="stockMovement.expiry.label" />,
      accessor: 'inventoryItem.expirationDate',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="stockMovement.recipient.label" />,
      accessor: 'recipient.name',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="putAway.qty.label" />,
      accessor: 'quantity',
      style: { whiteSpace: 'normal' },
      Cell: props => <span>{props.value ? props.value.toLocaleString('en-US') : props.value}</span>,
      Filter,
    }, {
      Header: <Translate id="putAway.currentBin.label" />,
      accessor: 'currentBins',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="putAway.putAwayBin.label" />,
      accessor: 'putawayLocation.name',
      style: { whiteSpace: 'normal' },
      Filter,
    }, {
      Header: <Translate id="stockMovement.label" />,
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
      _.isNull(putAwayItem.putawayLocation.id));

    if (!isBinLocationChosen) {
      this.confirmPutaway();
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
      .then((response) => {
        const putAway = parseResponse(response.data.data);
        putAway.putawayItems = _.map(putAway.putawayItems, item => ({
          _id: _.uniqueId('item_'),
          ...item,
          splitItems: _.map(item.splitItems, splitItem => ({ _id: _.uniqueId('item_'), ...splitItem })),
        }));

        this.props.hideSpinner();

        Alert.success(this.props.translate('alert.putAwayCompleted.label'));

        this.setState({
          putAway: {
            ...putAway,
            putawayItems: PutAwayCheckPage.processSplitLines(putAway.putawayItems),
          },
          completed: true,
        });
      })
      .catch(() => this.props.hideSpinner());
  }


  /**
   * Shows confirmation dialog on complete if there are items with empty bin location.
   * @public
   */
  confirmPutaway() {
    confirmAlert({
      title: this.props.translate('message.confirmPutAway.label'),
      message: this.props.translate('confirmPutAway.message'),
      buttons: [
        {
          label: this.props.translate('default.yes.label'),
          onClick: () => this.save(),
        },
        {
          label: this.props.translate('default.no.label'),
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
      <div className="main-container">
        <h1><Translate id="putAway.putAway.label" /> {this.state.putAway.putawayNumber}</h1>
        {
          this.state.completed ?
            <div className="d-flex justify-content-between mb-2">
              <div>
                <Translate id="putAway.showBy.label" />:
                <button
                  className="btn btn-primary ml-2 btn-xs"
                  data-toggle="button"
                  aria-pressed="false"
                  onClick={toggleTree}
                >
                  {pivotBy && pivotBy.length ? <Translate id="stockMovement.label" /> : <Translate id="product.label" /> }
                </button>
              </div>
              <button
                type="button"
                className="btn btn-outline-primary float-right mb-2 btn-xs"
                onClick={() => this.props.firstPage()}
              ><Translate id="putAway.goBack.label" />
              </button>
            </div> :
            <div className="d-flex justify-content-between mb-2">
              <div>
                <Translate id="putAway.showBy.label" />:
                <button
                  className="btn btn-primary ml-2 btn-xs"
                  data-toggle="button"
                  aria-pressed="false"
                  onClick={toggleTree}
                >
                  {pivotBy && pivotBy.length ? <Translate id="stockMovement.label" /> : <Translate id="product.label" /> }
                </button>
              </div>
              <div>
                <button
                  type="button"
                  onClick={() => this.props.prevPage({
                    putAway: this.props.putAway,
                    pivotBy: this.state.pivotBy,
                    expanded: this.state.expanded,
                  })}
                  className="btn btn-outline-primary mb-2 btn-xs mr-2"
                ><Translate id="default.button.edit.label" />
                </button>
                <button
                  type="button"
                  onClick={() => this.completePutAway()}
                  className="btn btn-outline-primary float-right mb-2 btn-xs"
                ><Translate id="putAway.completePutAway.label" />
                </button>
              </div>
            </div>
        }
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
              defaultSorted={[{ id: 'name' }, { id: 'stockMovement.name' }]}
            />
            : null
        }
        {
          this.state.completed ?
            <button
              type="button"
              className="btn btn-outline-primary float-right my-2 btn-xs"
              onClick={() => this.props.firstPage()}
            ><Translate id="putAway.goBack.label" />
            </button> :
            <div>
              <button
                type="button"
                onClick={() => this.completePutAway()}
                className="btn btn-outline-primary float-right my-2 btn-xs"
              ><Translate id="putAway.completePutAway.label" />
              </button>
              <button
                type="button"
                onClick={() => this.props.prevPage({
                  putAway: this.props.putAway,
                  pivotBy: this.state.pivotBy,
                  expanded: this.state.expanded,
                })}
                className="btn btn-outline-primary float-right mr-2 my-2 btn-xs"
              >Edit
              </button>
            </div>
        }
      </div>
    );
  }
}

const mapStateToProps = state => ({
  translate: getTranslate(state.localize),
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
