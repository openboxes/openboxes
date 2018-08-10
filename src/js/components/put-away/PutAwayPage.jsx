import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import ReactTable from 'react-table';
import selectTableHOC from 'react-table/lib/hoc/selectTable';
import PropTypes from 'prop-types';

import 'react-table/react-table.css';

import customTreeTableHOC from '../../utils/CustomTreeTable';
import Select from '../../utils/Select';
import apiClient, { parseResponse, flattenRequest } from '../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../actions';

const SelectTreeTable = selectTableHOC(customTreeTableHOC(ReactTable));

/* eslint-disable no-underscore-dangle */
/* eslint-disable no-param-reassign */
/* eslint-disable no-return-assign */

function getNodes(data, node = []) {
  data.forEach((item) => {
    if (Object.prototype.hasOwnProperty.call(item, '_subRows') && item._subRows) {
      node = getNodes(item._subRows, node);
    } else {
      node.push(item._original);
    }
  });
  return node;
}

/**
 * The first page of put-away which shows everything that is currently in receiving bin.
 * User is able to sort either by shipment or by product. By default it is not
 * available to see lines in pending put aways but it can be changed. User can choose
 * one or multiple shipments to view.
 */
class PutAwayPage extends Component {
  constructor(props) {
    super(props);
    const columns = this.getColumns();
    this.state = {
      pendingPutAways: [],
      putawayItems: [],
      columns,
      selection: new Set(),
      selectAll: false,
      selectType: 'checkbox',
      pivotBy: ['stockMovement.name'],
      expanded: {},
      expandedRowsCount: 0,
    };
  }

  componentDidMount() {
    this.fetchPutAwayCandidates();
  }

  /**
   * Called when expander is clicked. Checks expanded rows and counts their number.
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

    const allCurrentRows = this.selectTable
      .getWrappedInstance().getResolvedState().sortedData;
    const expandedRows = _.at(allCurrentRows, expandedRecordsIds);
    const expandedRowsCount = getNodes(expandedRows).length;

    this.setState({ expanded, expandedRowsCount });
  };

  /**
   * Returns an array of columns which are passed to the table.
   * @public
   */
  getColumns = () => [
    {
      Header: 'Code',
      accessor: 'product.productCode',
      style: { whiteSpace: 'normal' },
    }, {
      Header: 'Name',
      accessor: 'product.name',
      style: { whiteSpace: 'normal' },
    }, {
      Header: 'Lot',
      accessor: 'inventoryItem.lotNumber',
      style: { whiteSpace: 'normal' },
    }, {
      Header: 'Expiry',
      accessor: 'inventoryItem.expirationDate',
      style: { whiteSpace: 'normal' },
    }, {
      Header: 'Recipient',
      accessor: 'recipient.name',
      style: { whiteSpace: 'normal' },
    }, {
      Header: 'Qty in receiving',
      accessor: 'quantity',
      style: { whiteSpace: 'normal' },
      Cell: props => <span>{props.value ? props.value.toLocaleString('en-US') : props.value}</span>,
    }, {
      Header: 'Stock Movement',
      accessor: 'stockMovement.name',
      style: { whiteSpace: 'normal' },
      Expander: ({ row, isExpanded }) => (
        <span className="ml-2">
          <input
            type="checkbox"
            name="aggregationCheckbox"
            checked={this.checkSelected(row)}
            value={row._subRows[0]._original.stockMovement.id}
            ref={elem => elem && (elem.indeterminate = this.checkIndeterminate(row))}
            onChange={this.toggleSelectionsByStockMovement}
          />
          <div className={`rt-expander ${isExpanded && '-open'}`}>&bull;</div>
        </span>
      ),
      filterable: true,
    },
  ];

  /**
   * Fetches available items to put away from API.
   * @public
   */
  fetchPutAwayCandidates() {
    this.props.showSpinner();
    const url = '/openboxes/api/putaways';

    return apiClient.get(url)
      .then((response) => {
        const putawayItems = [];
        const pendingPutAways = [];
        const putAwayCandidates = parseResponse(response.data.data);

        putAwayCandidates.forEach((item) => {
          // this _id is used internally in TreeTable
          const _id = _.uniqueId('item_');
          if (item.putawayStatus === 'PENDING') {
            pendingPutAways.push({ _id, ...item });
          } else {
            putawayItems.push({
              _id,
              ...item,
              putawayFacility: { id: item.currentFacility ? item.currentFacility.id : null },
            });
          }
        });

        this.setState({ putawayItems, pendingPutAways }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Sends all changes made by user in this step of put-away to API and updates data.
   * @public
   */
  savePutAways() {
    this.props.showSpinner();
    const url = '/openboxes/api/putaways';
    const payload = {
      putawayNumber: '',
      'putawayAssignee.id': '',
      putawayStatus: '',
      putawayDate: '',
      putawayItems: _.filter(this.state.putawayItems, item =>
        _.includes([...this.state.selection], item._id)),
    };

    return apiClient.post(url, flattenRequest(payload))
      .then((response) => {
        const putAway = parseResponse(response.data.data);
        putAway.putawayItems = _.map(putAway.putawayItems, item => ({ _id: _.uniqueId('item_'), ...item }));

        this.props.hideSpinner();
        const expanded = {};

        if (this.state.pivotBy.length) {
          _.forEach(this.state.putawayItems, (item, index) => expanded[index] = true);
        }

        this.props.nextPage({
          putAway,
          pivotBy: this.state.pivotBy,
          expanded,
        });
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Filters put away items depending on user preferences - if they want to include pending one
   * or not.
   * @param {boolean} includePending
   * @public
   */
  filterPutAways(includePending) {
    let putawayItems = [];
    if (includePending) {
      putawayItems = [...this.state.putawayItems, ...this.state.pendingPutAways];
    } else {
      putawayItems = _.filter(this.state.putawayItems, val => val.putawayStatus !== 'PENDING');
    }

    this.setState({ putawayItems });
  }

  /**
   * Returns true if there are some selected rows but also some unselected ones. Needed in order
   * to make stock movements' checkbox indeterminate.
   * @param {object} row
   * @public
   */
  checkIndeterminate(row) {
    return _.some(row._subRows, subRow =>
      !_.includes([...this.state.selection], subRow._original._id))
    && _.some(row._subRows, subRow =>
      _.includes([...this.state.selection], subRow._original._id));
  }

  /**
   * Returns true if every row of stock movement is selected. Needed in order to make stock
   * movements' checkbox checked.
   * @param {object} row
   * @public
   */
  checkSelected(row) {
    return _.every(row._subRows, subRow =>
      _.includes([...this.state.selection], subRow._original._id));
  }

  /**
   * Filters items to toggle by stock movement and then calls toggleSelection method.
   *  @param {object} event
   * @public
   */
  toggleSelectionsByStockMovement = (event) => {
    const { target } = event;
    const { checked, value } = target;
    const itemsToToggle = _.map(_.filter(this.state.putawayItems, product =>
      product.stockMovement.id === value && product.putawayStatus !== 'PENDING'), item => item._id);
    this.toggleSelection(itemsToToggle, checked);
  };

  /**
   * Adds all items that are in the current filtered data to selection array.
   * @public
   */
  toggleAll = () => {
    /*
      Select ALL the records that are in the current filtered data
    */
    const selectAll = !this.state.selectAll;
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
      if (item.putawayStatus !== 'PENDING') {
        selection.push(item._id);
      }
    });
    this.toggleSelection(selection, selectAll);
    this.setState({ selectAll });
  };

  /**
   * Changes the way od displaying table depending on after which element
   * user wants to sort it by.
   * @public
   */
  toggleTree = () => {
    if (this.state.pivotBy.length) {
      this.setState({ pivotBy: [], expanded: {}, expandedRowsCount: 0 });
    } else {
      this.setState({ pivotBy: ['stockMovement.name'], expanded: {}, expandedRowsCount: 0 });
    }
  };

  /**
   * React table's method which detects the selection state itselfs.
   * @param {string} key
   * @public
   */
  isSelected = key =>
    _.includes([...this.state.selection], key);

  /**
   * Adds or deletes item from selection array depending on what user did.
   * @param {object} keys
   * @param {boolean} checked
   * @public
   */
  toggleSelection = (keys, checked) => {
    const selection = new Set(this.state.selection);
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

  /**
   * Method that is passed to react table's option: defaultFilterMethod.
   * It filters rows and converts a string to lowercase letters.
   * @param {object} filter
   * @param {object} row
   * @public
   */
  filterMethod = (filter, row) =>
    (row[filter.id] !== undefined ?
      String(row[filter.id].toLowerCase()).includes(filter.value.toLowerCase()) : true);

  render() {
    const {
      toggleSelection, toggleAll, isSelected,
      onExpandedChange, toggleTree,
    } = this;
    const {
      putawayItems, columns, selectAll, selectType, pivotBy, expanded,
    } = this.state;
    const extraProps =
      {
        selectAll,
        isSelected,
        toggleAll,
        toggleSelection,
        selectType,
        pivotBy,
        expanded,
        onExpandedChange,
      };

    return (
      <div className="container-fluid pt-2">
        <h1>Put-Away </h1>
        <div className="mb-2">
          Show by:
          <button
            className="btn btn-primary ml-2"
            data-toggle="button"
            aria-pressed="false"
            onClick={toggleTree}
          >
            {pivotBy && pivotBy.length ? 'Stock Movement' : 'Product'}
          </button>
        </div>
        <div className="d-flex flex-row align-items-center mb-2">
          <div className="pr-2">Lines in pending put-aways:</div>
          <div style={{ width: '150px' }}>
            <Select
              options={[{ value: false, label: 'Exclude' }, { value: true, label: 'Include' }]}
              onChange={val => this.filterPutAways(val)}
              objectValue
              initialValue={false}
              clearable={false}
            />
          </div>
        </div>
        {
          putawayItems ?
            <SelectTreeTable
              data={putawayItems}
              columns={columns}
              ref={r => this.selectTable = r}
              className="-striped -highlight"
              {...extraProps}
              defaultPageSize={Number.MAX_SAFE_INTEGER}
              minRows={pivotBy && pivotBy.length ?
              10 - this.state.expandedRowsCount : 10}
              style={{
                height: '500px',
              }}
              showPaginationBottom={false}
              filterable
              defaultFilterMethod={this.filterMethod}
              freezeWhenExpanded
              SelectInputComponent={({
                id, checked, onClick, row,
              }) => (
                <input
                  type={selectType}
                  checked={checked}
                  onChange={() => {}}
                  onClick={(e) => {
                    const { shiftKey } = e;

                    e.stopPropagation();
                    onClick(id, shiftKey, row);
                  }}
                  disabled={row.putawayStatus === 'PENDING'}
                />)}
              defaultSorted={[{
                  id: 'name',
                }, {
                  id: 'stockMovement.name',
              }]}
              getTdProps={(state, rowInfo) => ({
                  style: { color: _.get(rowInfo, 'original.putawayStatus') === 'PENDING' ? 'gray' : 'black' },
                  onClick: (event, handleOriginal) => {
                    const { target } = event;
                    // Fire the original onClick handler, if the other part of row is clicked on
                    if (handleOriginal && target.name !== 'aggregationCheckbox') {
                      handleOriginal();
                    }
                  },
                })}
            />
            : null
        }
        <button
          type="button"
          disabled={this.state.selection.size < 1}
          onClick={() => this.savePutAways()}
          className="btn btn-outline-primary float-right my-2"
        >Start Put-Away
        </button>
      </div>
    );
  }
}

export default connect(null, { showSpinner, hideSpinner })(PutAwayPage);

PutAwayPage.propTypes = {
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** Function taking user to the next page */
  nextPage: PropTypes.func.isRequired,
};

