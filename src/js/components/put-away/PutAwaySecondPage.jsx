import React, { Component } from 'react';

import update from 'immutability-helper';
import fileDownload from 'js-file-download';
import _ from 'lodash';
import PropTypes from 'prop-types';
import queryString from 'query-string';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import ReactTable from 'react-table';
import { Tooltip } from 'react-tippy';

import { hideSpinner, showSpinner } from 'actions';
import { PUTAWAY_GENERATE_PDF } from 'api/urls';
import { TableCell } from 'components/DataTable';
import SplitLineModal from 'components/put-away/SplitLineModal';
import { ORDER_URL } from 'consts/applicationUrls';
import DateFormat from 'consts/dateFormat';
import apiClient, { flattenRequest, parseResponse } from 'utils/apiClient';
import customTreeTableHOC from 'utils/CustomTreeTable';
import Filter from 'utils/Filter';
import showLocationChangedAlert from 'utils/location-change-alert';
import Select from 'utils/Select';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';
import { formatDate } from 'utils/translation-utils';

import 'react-table/react-table.css';

const SelectTreeTable = (customTreeTableHOC(ReactTable));

function hasValidationError(quantity, quantityAvailable) {
  return (quantity > quantityAvailable) || quantity < 1;
}

/**
 * The second page of put-away where user can choose put-away bin, split a line
 * or generate put-away list(pdf). It can be sorted either by shipment or by product.
 */
class PutAwaySecondPage extends Component {
  constructor(props) {
    super(props);
    this.getColumns = this.getColumns.bind(this);
    this.fetchItems = this.fetchItems.bind(this);
    this.editItem = this.editItem.bind(this);
    const columns = this.getColumns();

    const {
      initialValues:
      {
        putAway, pivotBy, expanded,
      },
    } = this.props;

    this.state = {
      putAway: putAway || {},
      columns,
      pivotBy: pivotBy || ['stockMovement.name'],
      expanded,
      bins: [],
      location: this.props.location,
      sortBy: putAway && putAway.sortBy,
    };
  }

  componentDidMount() {
    if (this.props.putAwayTranslationsFetched) {
      this.dataFetched = true;
      this.fetchBins();
    }
    this.fetchPutAway();
  }

  componentWillReceiveProps(nextProps) {
    showLocationChangedAlert(
      this.props.translate, this.state.location, nextProps.location,
      () => { window.location = `${ORDER_URL.list()}?orderType=PUTAWAY_ORDER&status=PENDING`; },
    );

    this.setState((prev) => ({
      location: prev.location.id ? prev.location : nextProps.location,
    }));

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
      accessor: 'product',
      Cell: (row) => (
        <TableCell
          {...row}
          value={row.value?.displayNameOrDefaultName}
          tooltip={row.value?.name !== row.value?.displayNameOrDefaultName}
          tooltipLabel={row.value?.name}
        />
      ),
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
        const edit = _.get(this.state.putAway.putawayItems, `[${itemIndex}].edit`);
        let disabledMessage;

        if (props?.value < 1) {
          disabledMessage = this.props.translate(
            'react.putAway.negativeQuantity.label',
            'Quantity cannot be less than 1',
          );
        }

        if (props?.value > props.original?.quantityAvailable) {
          disabledMessage = this.props.translate(
            'react.putAway.higherQuantity.label',
            'Quantity cannot be greater than original putaway item quantity',
          );
        }

        if (edit) {
          return (
            <Tooltip
              html={disabledMessage}
              disabled={!disabledMessage}
              theme="transparent"
              arrow="true"
              delay="150"
              duration="250"
              hideDelay="50"
            >
              <div className={hasValidationError(props?.value, props.original?.quantityAvailable) ? 'has-error' : ''}>
                <input
                  type="number"
                  className="form-control form-control-xs"
                  value={props.value}
                  onChange={(event) => {
                    const putAway = update(this.state.putAway, {
                      putawayItems: { [itemIndex]: { quantity: { $set: event.target.value } } },
                    });

                    this.changePutAway(putAway);
                  }}
                />
              </div>
            </Tooltip>
          );
        }

        return (
          <Tooltip
            html={disabledMessage}
            disabled={!disabledMessage}
            theme="transparent"
            arrow="true"
            delay="150"
            duration="250"
            hideDelay="50"
          >
            <div className={hasValidationError(props.value, props.original?.quantityAvailable) ? 'has-error' : ''}>
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
      Cell: (props) => (
        <div>
          {props.value && props.value.zoneName ? (
            <div>
              {props.value.zoneName}
              :&nbsp;
            </div>
          ) : ''}
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
        const splitItems = _.get(this.state.putAway.putawayItems, `[${cellInfo.index}].splitItems`);

        if (splitItems && splitItems.length > 0) {
          return <Translate id="react.putAway.splitLine.label" defaultMessage="Split line" />;
        }

        return (
          <Select
            options={this.state.bins}
            valueKey="id"
            labelKey="name"
            value={_.get(this.state.putAway.putawayItems, `[${cellInfo.index}].${cellInfo.column.id}`) || null}
            onChange={(value) => this.changePutAway(update(this.state.putAway, {
              putawayItems: { [cellInfo.index]: { putawayLocation: { $set: value } } },
            }))}
            className="select-xs"
          />
        );
      },
      Filter,
    }, {
      Header: '',
      accessor: 'splitItems',
      Cell: (cellInfo) => (
        <div className="d-flex flex-row flex-wrap">
          <SplitLineModal
            putawayItem={this.state.putAway.putawayItems[cellInfo.index]}
            splitItems={_.get(this.state.putAway.putawayItems, `[${cellInfo.index}].${cellInfo.column.id}`)}
            saveSplitItems={(splitItems) => {
              this.saveSplitItems(splitItems, cellInfo.index);
            }}
            bins={this.state.bins}
          />
          <button
            type="button"
            className="btn btn-outline-primary btn-xs mr-1 mb-1"
            onClick={() => this.editItem(cellInfo.index)}
          >
            <Translate id="react.default.button.edit.label" defaultMessage="Edit" />
          </button>
          <button
            type="button"
            className="btn btn-outline-danger btn-xs mb-1"
            onClick={() => this.deleteItem(cellInfo.index)}
          >
            <Translate id="react.default.button.delete.label" defaultMessage="Delete" />
          </button>
        </div>
      ),
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
   * Fetches putaway items and sets them in redux form and in
   * state as current line items.
   * @public
   */

  fetchPutAway() {
    if (this.props.match.params.putAwayId) {
      this.props.showSpinner();

      const url = `/api/putaways/${this.props.match.params.putAwayId}`;

      apiClient.get(url)
        .then((response) => {
          const putAway = parseResponse(response.data.data);

          const putawayItems = _.map(
            putAway.putawayItems,
            (val) => ({
              ...val,
              putawayLocation: (val.putawayLocation.id || val.preferredBin.id) ? {
                id: val.putawayLocation.id ? val.putawayLocation.id : val.preferredBin.id,
                name: val.putawayLocation.name ? val.putawayLocation.name : val.preferredBin.name,
                zoneId: val.putawayLocation.id ? val.putawayLocation.zoneId
                  : val.preferredBin.zoneId,
                zoneName: val.putawayLocation.id ? val.putawayLocation.zoneName
                  : val.preferredBin.zoneName,
              } : null,
            }),
          );

          this.props.hideSpinner();

          const expanded = {};
          _.forEach(putawayItems, (item, index) => { expanded[index] = true; });

          this.setState({ expanded, putAway: { ...putAway, putawayItems } });
        })
        .catch(() => this.props.hideSpinner());
    }
  }

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
    if (filter.id === 'product') {
      val = val ? `${val.name} ${val.displayNameOrDefaultName}` : null;
    }
    if (filter.id === 'inventoryItem.expirationDate') {
      val = this.props.formatLocalizedDate(val, DateFormat.COMMON);
    }
    return _.toString(val).toLowerCase().includes(filter.value.toLowerCase());
  };

  /**
   * Fetches available bin locations from API.
   * @public
   */
  fetchBins() {
    this.props.showSpinner();
    const url = '/api/internalLocations';

    const mapBins = (bins) => (_.chain(bins)
      .orderBy(['name'], ['asc']).value()
    );

    return apiClient.get(url, {
      paramsSerializer: (parameters) => queryString.stringify(parameters),
      params: {
        'location.id': this.props.location.id,
        locationTypeCode: ['BIN_LOCATION', 'INTERNAL'],
        ignoreActivityCodes: ['RECEIVE_STOCK'],
      },
    })
      .then((response) => {
        const binGroups = _.partition(response.data.data, (bin) => (bin.zoneName));
        const binsWithZone = _.chain(binGroups[0]).groupBy('zoneName')
          .map((value, key) => ({ name: key, options: mapBins(value) }))
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
   * Save put-away with new split items.
   * @public
   */
  saveSplitItems(splitItems, itemIndex) {
    const putAway = update(this.state.putAway, {
      putawayItems: { [itemIndex]: { splitItems: { $set: splitItems } } },
    });

    this.savePutAways(putAway);
  }

  /**
   * Sends all changes made by user in this step of put-away to API and updates data.
   * @public
   */
  savePutAways(putAwayToSave, callback) {
    this.props.showSpinner();
    const url = `/api/putaways?location.id=${this.state.location.id}`;

    return apiClient.post(url, flattenRequest(putAwayToSave))
      .then((response) => {
        const putAway = parseResponse(response.data.data);

        this.setState({ putAway }, () => {
          this.props.hideSpinner();

          if (callback) {
            callback(putAway);
          }
        });
      })
      .catch(() => this.props.hideSpinner());
  }

  changePutAway(putAway) {
    this.setState({ putAway });
  }

  editItem(itemIndex) {
    const putAway = update(this.state.putAway, {
      putawayItems: {
        [itemIndex]: {
          edit: { $set: true },
          splitItems: {
            $set: _.map(_.filter(
              this.state.putAway.putawayItems[itemIndex].splitItems,
              (item) => item.id,
            ), (item) => (
              { ...item, delete: true }
            )),
          },
        },
      },
    });

    this.changePutAway(putAway);
  }

  deleteItem(itemIndex) {
    this.props.showSpinner();
    const url = `/api/putawayItems/${_.get(this.state.putAway.putawayItems, `[${itemIndex}].id`)}`;

    apiClient.delete(url)
      .then(() => {
        const putAway = update(this.state.putAway, {
          putawayItems: {
            $splice: [
              [itemIndex, 1],
            ],
          },
        });

        this.changePutAway(putAway);
        this.props.hideSpinner();
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Save put-away and go to next page.
   * @public
   */

  nextPage() {
    if (_.some(this.state.putAway.putawayItems, (putawayItem) =>
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
      this.savePutAways(this.state.putAway, (putAway) => {
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
    const { putawayNumber } = this.state.putAway;

    return apiClient.get(
      PUTAWAY_GENERATE_PDF(this.state.putAway.id),
      { responseType: 'blob', params: { sortBy: this.state.sortBy } },
    )
      .then((response) => {
        fileDownload(response.data, `PutawayReport${putawayNumber ? `-${putawayNumber}` : ''}.pdf`, 'application/pdf');
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
    const url = `/api/putaways/${this.state.putAway.id}?sortBy=${sortBy}`;
    return apiClient.get(url)
      .then((response) => {
        const putawayItems = _.map(
          parseResponse(response.data.data.putawayItems),
          (val) => ({
            ...val,
            putawayLocation: (val.putawayLocation.id || val.preferredBin.id) ? {
              id: val.putawayLocation.id ? val.putawayLocation.id : val.preferredBin.id,
              name: val.putawayLocation.name ? val.putawayLocation.name : val.preferredBin.name,
              zoneId: val.putawayLocation.id ? val.putawayLocation.zoneId : val.preferredBin.zoneId,
              zoneName: val.putawayLocation.id ? val.putawayLocation.zoneName
                : val.preferredBin.zoneName,
            } : null,
          }),
        );
        this.changePutAway({
          ...this.state.putAway,
          sortBy,
          putawayItems,
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
    const extraProps = {
      pivotBy,
      expanded,
      onExpandedChange,
    };

    return (
      <div className="putaway">
        <div className="d-flex justify-content-between mb-2 putaway-buttons">
          <div>
            <Translate id="react.putAway.showBy.label" defaultMessage="Show by" />
            :
            <button
              type="button"
              className="btn btn-primary ml-2 btn-xs"
              data-toggle="button"
              aria-pressed="false"
              onClick={toggleTree}
            >
              {pivotBy && pivotBy.length
                ? <Translate id="react.putAway.stockMovement.label" defaultMessage="Stock Movement" />
                : <Translate id="react.putAway.product.label" defaultMessage="Product" /> }
            </button>
          </div>
          <span className="buttons-container classic-form-buttons">
            <button
              type="button"
              onClick={() => this.savePutAways(
                this.state.putAway,
                () => this.sortPutawayItems(),
              )}
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
              type="button"
              className="btn btn-outline-secondary btn-xs mr-3"
              onClick={() => this.savePutAways(
                this.state.putAway,
                () => this.generatePutAwayList(),
              )}
            >
              <span>
                <i className="fa fa-print pr-2" />
                <Translate id="react.putAway.generateList.label" defaultMessage="Generate Putaway list" />
              </span>
            </button>
            <button
              type="button"
              onClick={() => this.savePutAways(this.state.putAway)}
              className="btn btn-outline-secondary btn-xs"
              disabled={_.some(this.state.putAway.putawayItems, (putawayItem) =>
                hasValidationError(putawayItem.quantity, putawayItem.quantityAvailable))}
            >
              <Translate id="react.default.button.save.label" defaultMessage="Save" />
            </button>
          </span>
        </div>
        {
          this.state.putAway.putawayItems
            ? (
              <SelectTreeTable
                data={this.state.putAway.putawayItems}
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
            disabled={_.some(this.state.putAway.putawayItems, (putawayItem) =>
              hasValidationError(putawayItem.quantity, putawayItem.quantityAvailable))}
            type="button"
            onClick={() => this.nextPage()}
            className="btn btn-outline-primary btn-form float-right btn-xs"
          >
            <Translate id="react.default.button.next.label" defaultMessage="Next" />
          </button>
        </div>
      </div>
    );
  }
}

const mapStateToProps = (state) => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  formatLocalizedDate: formatDate(state.localize),
  putAwayTranslationsFetched: state.session.fetchedTranslations.putAway,
});

export default connect(
  mapStateToProps,
  {
    showSpinner, hideSpinner,
  },
)(PutAwaySecondPage);

PutAwaySecondPage.defaultProps = {
  original: {},
};

PutAwaySecondPage.propTypes = {
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** Function taking user to the next page */
  nextPage: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  /** All put-away's data */
  initialValues: PropTypes.shape({
    putAway: PropTypes.arrayOf(PropTypes.shape({})),
    pivotBy: PropTypes.arrayOf(PropTypes.shape({})),
    expanded: PropTypes.arrayOf(PropTypes.shape({})),
  }).isRequired,
  original: PropTypes.shape({
    quantityAvailable: PropTypes.number,
  }),
  match: PropTypes.shape({
    params: PropTypes.shape({
      putAwayId: PropTypes.string,
    }),
  }).isRequired,
  /** Location (currently chosen). To be used in internalLocations and putaways requests. */
  location: PropTypes.shape({
    id: PropTypes.string,
  }).isRequired,
  putAwayTranslationsFetched: PropTypes.bool.isRequired,
  formatLocalizedDate: PropTypes.func.isRequired,
};
