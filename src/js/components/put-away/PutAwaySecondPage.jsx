import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import ReactTable from 'react-table';
import PropTypes from 'prop-types';
import update from 'immutability-helper';
import fileDownload from 'js-file-download';
import { Translate, getTranslate } from 'react-localize-redux';

import 'react-table/react-table.css';

import customTreeTableHOC from '../../utils/CustomTreeTable';
import Select from '../../utils/Select';
import SplitLineModal from './SplitLineModal';
import apiClient, { parseResponse, flattenRequest } from '../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../actions';
import Filter from '../../utils/Filter';
import showLocationChangedAlert from '../../utils/location-change-alert';

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
    const columns = this.getColumns();
    this.state = {
      putAway,
      columns,
      pivotBy,
      expanded,
      bins: [],
      location,
    };
  }

  componentDidMount() {
    this.fetchBins();
  }

  componentWillReceiveProps(nextProps) {
    showLocationChangedAlert(
      this.props.translate, this.state.location, nextProps.location,
      () => { window.location = '/openboxes/order/list?orderTypeCode=TRANSFER_ORDER&status=PENDING'; },
    );

    const location = this.state.location.id ? this.state.location : nextProps.location;
    this.setState({ putAway: nextProps.putAway, location });
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
      Cell: (props) => {
        const itemIndex = props.index;
        const edit = _.get(this.state.putAway.putawayItems, `[${itemIndex}].edit`);

        if (edit) {
          return (<input
            type="number"
            className="form-control form-control-xs"
            value={props.value}
            onChange={(event) => {
              const putAway = update(this.state.putAway, {
                putawayItems: { [itemIndex]: { quantity: { $set: event.target.value } } },
              });

              this.setState({ putAway });
            }}
          />);
        }

        return (<span>{props.value ? props.value.toLocaleString('en-US') : props.value}</span>);
      },
      Filter,
    }, {
      Header: <Translate id="putAway.currentBin.label" />,
      accessor: 'currentBins',
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
    }, {
      Header: <Translate id="putAway.putAwayBin.label" />,
      accessor: 'putawayLocation',
      Cell: (cellInfo) => {
        const splitItems = _.get(this.state.putAway.putawayItems, `[${cellInfo.index}].splitItems`);

        if (splitItems && splitItems.length > 0) {
          return <Translate id="stockMovement.splitLine.label" />;
        }

        return (<Select
          options={this.state.bins}
          objectValue
          value={_.get(this.state.putAway.putawayItems, `[${cellInfo.index}].${cellInfo.column.id}`) || null}
          onChange={value => this.setState({
            putAway: update(this.state.putAway, {
              putawayItems: { [cellInfo.index]: { putawayLocation: { $set: value } } },
            }),
          })}
          className="select-xs"
        />);
      },
      Filter,
    }, {
      Header: '',
      accessor: 'splitItems',
      Cell: cellInfo => (
        <div className="d-flex flex-row">
          <SplitLineModal
            putawayItem={this.state.putAway.putawayItems[cellInfo.index]}
            splitItems={_.get(this.state.putAway.putawayItems, `[${cellInfo.index}].${cellInfo.column.id}`)}
            saveSplitItems={(splitItems) => {
              this.saveSplitItems(splitItems, cellInfo.index);
            }}
            bins={this.state.bins}
          />
          <button
            className="btn btn-outline-primary btn-xs mx-2"
            onClick={() => this.editItem(cellInfo.index)}
          ><Translate id="default.button.edit.label" />
          </button>
          <button
            className="btn btn-outline-danger btn-xs"
            onClick={() => this.deleteItem(cellInfo.index)}
          ><Translate id="default.button.delete.label" />
          </button>
        </div>),
      filterable: false,
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
        this.setState({ bins }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
  * Sends all changes made by user in this step of put-away to API and updates data.
  * @public
  */
  savePutAways(putAwayToSave, callback) {
    this.props.showSpinner();
    const url = `/openboxes/api/putaways?location.id=${this.props.location.id}`;

    return apiClient.post(url, flattenRequest(putAwayToSave))
      .then((response) => {
        const putAway = parseResponse(response.data.data);
        putAway.putawayItems = _.map(putAway.putawayItems, item => ({
          _id: _.uniqueId('item_'),
          ...item,
          splitItems: _.map(item.splitItems, splitItem => ({ _id: _.uniqueId('item_'), ...splitItem })),
        }));

        this.setState({ putAway }, () => {
          this.props.hideSpinner();

          if (callback) {
            callback(putAway);
          }
        });
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

  editItem(itemIndex) {
    const putAway = update(this.state.putAway, {
      putawayItems: {
        [itemIndex]: {
          edit: { $set: true },
          splitItems: {
            $set: _.map(_.filter(
              this.state.putAway.putawayItems[itemIndex].splitItems,
              item => item.id,
            ), item => (
              { ...item, delete: true }
            )),
          },
        },
      },
    });

    this.setState({ putAway });
  }

  deleteItem(itemIndex) {
    this.props.showSpinner();
    const url = `/openboxes/api/putawayItems/${_.get(this.state.putAway.putawayItems, `[${itemIndex}].id`)}`;

    apiClient.delete(url)
      .then(() => {
        const putAway = update(this.state.putAway, {
          putawayItems: {
            $splice: [
              [itemIndex, 1],
            ],
          },
        });

        this.setState({ putAway }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Save put-away and go to next page.
   * @public
   */
  nextPage() {
    this.savePutAways(this.state.putAway, (putAway) => {
      this.props.nextPage({
        putAway,
        pivotBy: this.state.pivotBy,
        expanded: this.state.expanded,
      });
    });
  }

  /**
   * Generates a pdf that shows what should be putted away.
   * @public
   */
  generatePutAwayList() {
    this.props.showSpinner();
    const url = '/openboxes/putAway/generatePdf/ff80818164ae89800164affcfe6e0001';
    const { putawayNumber } = this.state.putAway;

    return apiClient.post(url, flattenRequest(this.state.putAway), { responseType: 'blob' })
      .then((response) => {
        fileDownload(response.data, `PutawayReport${putawayNumber ? `-${putawayNumber}` : ''}.pdf`, 'application/pdf');
        this.props.hideSpinner();
      })
      .catch(() => this.props.hideSpinner());
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
              className="btn btn-outline-secondary btn-xs mr-3"
              onClick={() => this.generatePutAwayList()}
            >
              <span><i className="fa fa-print pr-2" /><Translate id="putAway.generateList.label" /></span>
            </button>
            <button
              type="button"
              onClick={() => this.savePutAways(this.state.putAway)}
              className="btn btn-outline-secondary btn-xs"
            ><Translate id="default.button.save.label" />
            </button>
          </div>
          <button
            type="button"
            onClick={() => this.nextPage()}
            className="btn btn-outline-primary align-self-end btn-xs"
          ><Translate id="default.button.next.label" />
          </button>
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
              defaultSorted={[{ id: 'name' }, { id: 'stockMovement.name' }]}
            />
            : null
        }
        <button
          type="button"
          onClick={() => this.nextPage()}
          className="btn btn-outline-primary float-right my-2 btn-xs"
        ><Translate id="default.button.next.label" />
        </button>
      </div>
    );
  }
}

const mapStateToProps = state => ({
  translate: getTranslate(state.localize),
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
    putawayItems: PropTypes.arrayOf(PropTypes.shape({})),
  }),
  /** An array of available attributes after which a put-away can be sorted by */
  pivotBy: PropTypes.arrayOf(PropTypes.string),
  /** List of currently expanded put-away's items */
  expanded: PropTypes.shape({}),
  /** Location (currently chosen). To be used in internalLocations and putaways requests. */
  location: PropTypes.shape({
    id: PropTypes.string,
  }).isRequired,
};

PutAwaySecondPage.defaultProps = {
  putAway: {},
  pivotBy: ['stockMovement.name'],
  expanded: {},
};
