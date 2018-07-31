import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import ReactTable from 'react-table';
import PropTypes from 'prop-types';
import update from 'immutability-helper';
import fileDownload from 'js-file-download';

import 'react-table/react-table.css';

import customTreeTableHOC from '../../utils/CustomTreeTable';
import Select from '../../utils/Select';
import SplitLineModal from './SplitLineModal';
import apiClient, { parseResponse, flattenRequest } from '../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../actions';

const SelectTreeTable = (customTreeTableHOC(ReactTable));

class PutAwaySecondPage extends Component {
  constructor(props) {
    super(props);
    const { putAway, pivotBy } = this.props;
    this.getColumns = this.getColumns.bind(this);
    const columns = this.getColumns();
    this.state = {
      putAway,
      columns,
      pivotBy,
      expanded: {},
      bins: [],
    };
  }

  componentDidMount() {
    this.fetchBins();
  }

  onExpandedChange = (expanded) => {
    this.setState({ expanded });
  };

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
      Header: 'QTY',
      accessor: 'quantity',
      style: { whiteSpace: 'normal' },
    }, {
      Header: 'Current bin',
      accessor: 'currentBins',
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
    }, {
      Header: 'Put Away Bin',
      accessor: 'putawayLocation.id',
      Cell: (cellInfo) => {
        const splitItems = _.get(this.state.putAway.putawayItems, `[${cellInfo.index}].splitItems`);

        if (splitItems && splitItems.length > 0) {
          return 'Split line';
        }

        return (<Select
          options={this.state.bins}
          value={_.get(this.state.putAway.putawayItems, `[${cellInfo.index}].${cellInfo.column.id}`) || null}
          onChange={value => this.setState({
            putAway: update(this.state.putAway, {
              putawayItems: { [cellInfo.index]: { putawayLocation: { id: { $set: value } } } },
            }),
          })}
        />);
      },
    }, {
      Header: '',
      accessor: 'splitItems',
      Cell: cellInfo => (
        <SplitLineModal
          putawayItem={this.state.putAway.putawayItems[cellInfo.index]}
          splitItems={_.get(this.state.putAway.putawayItems, `[${cellInfo.index}].${cellInfo.column.id}`)}
          saveSplitItems={splitItems => this.setState({
            putAway: update(this.state.putAway, {
              putawayItems: { [cellInfo.index]: { [cellInfo.column.id]: { $set: splitItems } } },
            }),
          })}
          bins={this.state.bins}
        />),
      filterable: false,
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

  fetchBins() {
    this.props.showSpinner();
    const url = '/openboxes/api/internalLocations';

    return apiClient.get(url)
      .then((response) => {
        const bins = _.map(response.data.data, bin => (
          { value: bin.id, label: bin.name }
        ));
        this.setState({ bins }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  savePutAways() {
    this.props.showSpinner();
    const url = '/openboxes/api/putaways';

    return apiClient.post(url, flattenRequest(this.state.putAway))
      .then((response) => {
        const putAway = parseResponse(response.data.data);
        putAway.putawayItems = _.map(putAway.putawayItems, item => ({ _id: _.uniqueId('item_'), ...item }));

        this.props.hideSpinner();

        this.props.nextPage({
          putAway,
          pivotBy: this.state.pivotBy,
        });
      })
      .catch(() => this.props.hideSpinner());
  }

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
          <button
            className="float-right btn btn-outline-secondary align-self-end"
            onClick={() => this.generatePutAwayList()}
          >
            <span><i className="fa fa-print pr-2" />Generate Put-Away list</span>
          </button>
        </div>
        {
          putAway.putawayItems ?
            <SelectTreeTable
              data={putAway.putawayItems}
              columns={columns}
              className="-striped -highlight"
              {...extraProps}
              defaultPageSize={Number.MAX_SAFE_INTEGER}
              minRows={10}
              style={{
                height: '500px',
              }}
              showPaginationBottom={false}
              filterable
              defaultFilterMethod={this.filterMethod}
              freezWhenExpanded
              defaultSorted={[{ id: 'name' }, { id: 'stockMovement.name' }]}
            />
            : null
        }
        <button
          type="button"
          onClick={() => this.savePutAways()}
          className="btn btn-outline-primary float-right my-2"
        >Next
        </button>
      </div>
    );
  }
}

export default connect(null, { showSpinner, hideSpinner })(PutAwaySecondPage);

PutAwaySecondPage.propTypes = {
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  nextPage: PropTypes.func.isRequired,
  putAway: PropTypes.shape({
    putawayItems: PropTypes.arrayOf(PropTypes.shape({})),
  }),
  pivotBy: PropTypes.arrayOf(PropTypes.string),
};

PutAwaySecondPage.defaultProps = {
  putAway: {},
  pivotBy: ['stockMovement.name'],
};
