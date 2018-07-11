import _ from 'lodash';
import React, { Component } from 'react';
import ReactTable from 'react-table';
import selectTableHOC from 'react-table/lib/hoc/selectTable';
import PropTypes from 'prop-types';

import 'react-table/react-table.css';

import customTreeTableHOC from '../../utils/CustomTreeTable';
import { PUT_AWAY_MOCKS } from '../../mockedData';
import Select from '../../utils/Select';

const SelectTreeTable = selectTableHOC(customTreeTableHOC(ReactTable));

/* eslint-disable no-underscore-dangle */
/* eslint-disable no-param-reassign */
/* eslint-disable no-return-assign */

function getData(includePending) {
  const data = PUT_AWAY_MOCKS.map((item) => {
    // this _id is used internally in TreeTable
    const _id = _.uniqueId();
    return {
      _id,
      ...item,
    };
  });

  if (includePending) {
    return data;
  }

  return _.filter(data, val => val.status !== 'PENDING');
}

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

class PutAwayPage extends Component {
  constructor(props) {
    super(props);
    const data = getData(false);
    const columns = this.getColumns();
    this.state = {
      data,
      columns,
      selection: new Set(),
      selectAll: false,
      selectType: 'checkbox',
      pivotBy: ['stockMovement.name'],
      expanded: {},
    };
  }

  onExpandedChange = (expanded) => {
    this.setState({ expanded });
  };

  getColumns = () => [
    {
      Header: 'Code',
      accessor: 'code',
      style: { whiteSpace: 'normal' },
    }, {
      Header: 'Name',
      accessor: 'name',
      style: { whiteSpace: 'normal' },
    }, {
      Header: 'Lot',
      accessor: 'lot',
      style: { whiteSpace: 'normal' },
    }, {
      Header: 'Expiry',
      accessor: 'expiryDate',
      style: { whiteSpace: 'normal' },
    }, {
      Header: 'Recipient',
      accessor: 'recipient.name',
      style: { whiteSpace: 'normal' },
    }, {
      Header: 'Qty in receiving',
      accessor: 'quantity',
      style: { whiteSpace: 'normal' },
    }, {
      Header: 'Stock Movement',
      accessor: 'stockMovement.name',
      style: { whiteSpace: 'normal' },
      Expander: ({ row, isExpanded }) => (
        <span className="ml-2">
          <input
            id={`stockMovement_${row._subRows[0]._original.stockMovement.id}`}
            type="checkbox"
            name="aggregationCheckbox"
            value={row._subRows[0]._original.stockMovement.id}
            ref={elem => elem && (elem.indeterminate = this.checkIndetermediate(row))}
            onChange={this.toggleSelectionsByStockMovement}
          />
          <div className={`rt-expander ${isExpanded && '-open'}`}>&bull;</div>
        </span>
      ),
      filterable: true,
    },
  ];

  checkIndetermediate(row) {
    return _.some(row._subRows, subRow =>
      !_.includes([...this.state.selection], subRow._original.id))
    && _.some(row._subRows, subRow =>
      _.includes([...this.state.selection], subRow._original.id));
  }

  toggleSelectionsByStockMovement = (event) => {
    const { target } = event;
    const { checked, value } = target;
    const itemsToToggle = _.map(_.filter(this.state.data, product =>
      product.stockMovement.id === parseInt(value, 10) && product.status !== 'PENDING'), item => item.id);
    this.toggleSelection(itemsToToggle, checked);
  };

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
      if (item.status !== 'PENDING') {
        selection.push(item.id);
      }
      const parentCheckbox = document.querySelector(`input[id="stockMovement_${item.stockMovement.id}"]`);
      if (parentCheckbox) {
        parentCheckbox.checked = selectAll;
      }
    });
    this.toggleSelection(selection, selectAll);
    this.setState({ selectAll });
  };

  toggleTree = () => {
    if (this.state.pivotBy.length) {
      this.setState({ pivotBy: [], expanded: {} });
    } else {
      this.setState({ pivotBy: ['stockMovement.name'], expanded: {} });
    }
  };

  isSelected = key =>
    _.includes([...this.state.selection], parseInt(key, 10));

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
    } else if (selection.has(parseInt(keys, 10))) {
      selection.delete(parseInt(keys, 10));
    } else if (!selection.has(parseInt(keys, 10))) {
      selection.add(parseInt(keys, 10));
    }
    this.setState({ selection });
  };

  filterMethod = (filter, row) =>
    (row[filter.id] !== undefined ?
      String(row[filter.id].toLowerCase()).includes(filter.value.toLowerCase()) : true);

  render() {
    const {
      toggleSelection, toggleAll, isSelected,
      onExpandedChange, toggleTree,
    } = this;
    const {
      data, columns, selectAll, selectType, pivotBy, expanded,
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
              onChange={val => this.setState({ data: getData(val) })}
              objectValue
              initialValue={false}
              clearable={false}
            />
          </div>
        </div>
        {
          data ?
            <SelectTreeTable
              data={data}
              columns={columns}
              ref={r => this.selectTable = r}
              className="-striped -highlight"
              {...extraProps}
              defaultPageSize={10}
              filterable
              defaultFilterMethod={this.filterMethod}
              freezWhenExpanded
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
                  disabled={row.status === 'PENDING'}
                />)}
              defaultSorted={[{
                  id: 'name',
                }, {
                  id: 'stockMovement.name',
              }]}
              getTdProps={(state, rowInfo) => ({
                  style: { color: _.get(rowInfo, 'original.status') === 'PENDING' ? 'gray' : 'black' },
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
          onClick={() => this.props.nextPage({
            data: _.filter(data, item => _.includes([...this.state.selection], item.id)),
            pivotBy,
          })}
          className="btn btn-outline-primary float-right my-2"
        >Start Put-Away
        </button>
      </div>
    );
  }
}

export default PutAwayPage;

PutAwayPage.propTypes = {
  nextPage: PropTypes.func.isRequired,
};

