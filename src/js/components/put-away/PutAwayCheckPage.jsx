import React, { Component } from 'react';
import ReactTable from 'react-table';
import PropTypes from 'prop-types';

import 'react-table/react-table.css';

import customTreeTableHOC from '../../utils/CustomTreeTable';

const SelectTreeTable = (customTreeTableHOC(ReactTable));

class PutAwayCheckPage extends Component {
  constructor(props) {
    super(props);
    const { data, pivotBy } = this.props;
    const columns = this.getColumns();
    this.state = {
      data,
      columns,
      pivotBy,
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
      Header: 'QTY',
      accessor: 'quantity',
      style: { whiteSpace: 'normal' },
    }, {
      Header: 'Current bin',
      accessor: 'currentBin',
      style: { whiteSpace: 'normal' },
    }, {
      Header: 'Put Away Bin',
      accessor: 'putAwayBin',
      style: { whiteSpace: 'normal' },
    }, {
      Header: 'Stock Movement',
      accessor: 'stockMovement.name',
      style: { whiteSpace: 'normal' },
      Expander: ({ isExpanded }) => (
        <span className="ml-2">
          <div className={`rt-expander ${isExpanded && '-open'}`}>&bull;</div>
        </span>
      ),
      filterable: true,
    },
  ];

  toggleTree = () => {
    if (this.state.pivotBy.length) {
      this.setState({ pivotBy: [], expanded: {} });
    } else {
      this.setState({ pivotBy: ['stockMovement.name'], expanded: {} });
    }
  };

  filterMethod = (filter, row) =>
    (row[filter.id] !== undefined ?
      String(row[filter.id].toLowerCase()).includes(filter.value.toLowerCase()) : true);

  render() {
    const {
      onExpandedChange, toggleTree,
    } = this;
    const {
      data, columns, pivotBy, expanded,
    } = this.state;
    const extraProps =
      {
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
        {
          data ?
            <SelectTreeTable
              data={data}
              columns={columns}
              className="-striped -highlight"
              {...extraProps}
              defaultPageSize={10}
              filterable
              defaultFilterMethod={this.filterMethod}
              freezWhenExpanded
              defaultSorted={[{ id: 'name' }, { id: 'stockMovement.name' }]}
            />
            : null
        }
        <button
          type="button"
          onClick={() => this.props.prevPage({
            data: this.state.data, pivotBy: this.state.pivotBy,
          })}
          className="btn btn-outline-primary my-2"
        >Edit
        </button>
        <button
          type="button"
          onClick={() => this.props.nextPage(this.state.data)}
          className="btn btn-outline-primary float-right my-2"
        >Complete Put Away
        </button>
      </div>
    );
  }
}

export default PutAwayCheckPage;

PutAwayCheckPage.propTypes = {
  nextPage: PropTypes.func.isRequired,
  prevPage: PropTypes.func.isRequired,
  data: PropTypes.arrayOf(PropTypes.shape({})),
  pivotBy: PropTypes.arrayOf(PropTypes.string),
};

PutAwayCheckPage.defaultProps = {
  data: [],
  pivotBy: ['stockMovement.name'],
};
