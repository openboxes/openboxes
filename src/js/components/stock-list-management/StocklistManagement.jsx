import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import ReactTable from 'react-table';
import treeTableHOC from 'react-table/lib/hoc/treeTable';
import update from 'immutability-helper';

import 'react-table/react-table.css';

import apiClient, { parseResponse, flattenRequest } from './../../utils/apiClient';
import { hideSpinner, showSpinner } from '../../actions';
import StocklistTable from './StocklistTable';

const TreeTable = treeTableHOC(ReactTable);

const COLUMNS = [
  {
    accessor: 'locationGroup.name',
  },
  {
    accessor: 'location.name',
  },
  {
    Header: 'Monthly demand',
    accessor: 'monthlyDemand',
    aggregate: vals => _.sum(vals),
    className: 'text-center',
  },
];

class StocklistManagement extends Component {
  constructor(props) {
    super(props);

    this.state = { data: [] };

    this.addItem = this.addItem.bind(this);
    this.editItem = this.editItem.bind(this);
    this.updateItemField = this.updateItemField.bind(this);
    this.saveItem = this.saveItem.bind(this);
    this.deleteItem = this.deleteItem.bind(this);
    this.printItem = this.printItem.bind(this);
    this.mailItem = this.mailItem.bind(this);
  }

  componentWillMount() {
    this.fetchData();
  }

  fetchData() {
    this.props.showSpinner();
    const url = `/openboxes/api/stocklistItems?product.id=${this.props.match.params.productId || ''}`;

    return apiClient.get(url)
      .then((response) => {
        this.setState({ data: parseResponse(response.data.data) });
      })
      .catch(this.props.hideSpinner());
  }

  addItem(locationIndex) {
    const location = this.state.data[locationIndex];

    this.setState({
      data: update(this.state.data, {
        [locationIndex]: {
          stocklistItems: {
            $push: [{
              name: '',
              location: {
                id: location.location.id,
              },
              replenishmentPeriod: null,
              maxQuantity: null,
              new: true,
            }],
          },
        },
      }),
    });
  }

  editItem(locationIndex, itemIndex) {
    this.setState({
      data: update(this.state.data, {
        [locationIndex]: {
          stocklistItems: {
            [itemIndex]: {
              edit: { $set: true },
            },
          },
        },
      }),
    });
  }

  updateItemField(locationIndex, itemIndex, itemField, fieldValue) {
    this.setState({
      data: update(this.state.data, {
        [locationIndex]: {
          stocklistItems: {
            [itemIndex]: {
              [itemField]: { $set: fieldValue },
            },
          },
        },
      }),
    });
  }

  saveItem(locationIndex, itemIndex, item) {
    let url = `/openboxes/api/stocklistItems?product.id=${this.props.match.params.productId || ''}`;

    if (!item.new) {
      url = `/openboxes/api/stocklistItems/${item.requisitionItem.id}`;
    }

    apiClient.post(url, flattenRequest(item))
      .then((response) => {
        this.setState({
          data: update(this.state.data, {
            [locationIndex]: {
              stocklistItems: {
                [itemIndex]: {
                  $set: parseResponse(response.data.data),
                },
              },
            },
          }),
        });
      })
      .catch(this.props.hideSpinner());
  }

  deleteItem(locationIndex, itemIndex) {
    const item = this.state.data[locationIndex].stocklistItems[itemIndex];

    if (item.new) {
      this.removeItem(locationIndex, itemIndex);
    } else {
      this.props.showSpinner();
      const url = `/openboxes/api/stocklistItems/${item.requisitionItem.id}`;

      apiClient.delete(url)
        .then(() => {
          this.removeItem(locationIndex, itemIndex);
        })
        .catch(this.props.hideSpinner());
    }
  }

  // eslint-disable-next-line no-unused-vars,class-methods-use-this
  printItem(locationIndex, itemIndex) {
    // TODO add proper implementation
  }

  // eslint-disable-next-line no-unused-vars,class-methods-use-this
  mailItem(locationIndex, itemIndex) {
    // TODO add proper implementation
  }

  removeItem(locationIndex, itemIndex) {
    this.setState({
      data: update(this.state.data, {
        [locationIndex]: {
          stocklistItems: {
            $splice: [
              [itemIndex, 1],
            ],
          },
        },
      }),
    });
  }

  render() {
    const { data } = this.state;
    return (
      <TreeTable
        className="stocklist-table"
        data={data}
        pivotBy={['locationGroup.name']}
        columns={COLUMNS}
        defaultPageSize={20}
        sortable={false}
        collapseOnDataChange={false}
        resolveData={values => values.map((row) => {
          if (!_.get(row, 'locationGroup.name')) {
            return { ...row, locationGroup: { name: 'No location group' } };
          }

          return row;
        })}
        SubComponent={({ original, index }) => (
          <StocklistTable
            data={original.stocklistItems}
            availableStocklists={original.availableStocklists}
            parentIndex={index}
            addItem={this.addItem}
            editItem={this.editItem}
            updateItemField={this.updateItemField}
            saveItem={this.saveItem}
            deleteItem={this.deleteItem}
            printItem={this.printItem}
            mailItem={this.mailItem}
          />)}
      />
    );
  }
}

export default connect(null, { showSpinner, hideSpinner })(StocklistManagement);

StocklistManagement.propTypes = {
  /** React router's object which contains information about url varaiables and params */
  match: PropTypes.shape({
    params: PropTypes.shape({ productId: PropTypes.string }),
  }).isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
};
