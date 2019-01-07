import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import ReactTable from 'react-table';
import update from 'immutability-helper';

import 'react-table/react-table.css';

import apiClient, { parseResponse, flattenRequest } from './../../utils/apiClient';
import { hideSpinner, showSpinner } from '../../actions';
import Select from '../../utils/Select';
import Input from '../../utils/Input';

class StocklistManagement extends Component {
  constructor(props) {
    super(props);

    this.state = {
      data: [],
      selectedStocklist: null,
      availableStocklists: [],
      productInfo: null,
    };

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
    this.fetchAvailableStocklists();
    this.fetchProductInfo();
  }

  fetchData() {
    this.props.showSpinner();
    const url = `/openboxes/api/stocklistItems?product.id=${this.props.match.params.productId || ''}`;

    apiClient.get(url)
      .then((response) => {
        this.setState({ data: parseResponse(response.data.data) });
      })
      .catch(this.props.hideSpinner());
  }

  fetchAvailableStocklists() {
    this.props.showSpinner();
    const url = '/openboxes/api/stocklistItems/availableStocklists';

    apiClient.get(url)
      .then((response) => {
        this.setState({
          availableStocklists: _.map(parseResponse(response.data.data), val =>
            ({ value: val, label: val.name })),
        });
      })
      .catch(this.props.hideSpinner());
  }

  fetchProductInfo() {
    this.props.showSpinner();
    const url = `/openboxes/api/products/${this.props.match.params.productId}/withCatalogs`;

    apiClient.get(url)
      .then((response) => {
        this.setState({ productInfo: response.data.data });
      })
      .catch(this.props.hideSpinner());
  }

  addItem(stocklist) {
    this.setState({
      selectedStocklist: null,
      data: update(this.state.data, {
        $push: [{
          stocklistId: stocklist.id,
          name: stocklist.name,
          location: {
            id: stocklist.location.id,
            name: stocklist.location.name,
          },
          locationGroup: {
            id: stocklist.locationGroup.id,
            name: stocklist.locationGroup.name,
          },
          manager: {
            id: stocklist.manager.id,
            name: stocklist.manager.name,
          },
          replenishmentPeriod: stocklist.replenishmentPeriod,
          maxQuantity: null,
          new: true,
        }],
      }),
    });
  }

  editItem(index) {
    this.setState({
      data: update(this.state.data, {
        [index]: {
          edit: { $set: true },
        },
      }),
    });
  }

  updateItemField(index, itemField, fieldValue) {
    this.setState({
      data: update(this.state.data, {
        [index]: {
          [itemField]: { $set: fieldValue },
        },
      }),
    });
  }

  saveItem(index, item) {
    let url = `/openboxes/api/stocklistItems?product.id=${this.props.match.params.productId || ''}`;

    if (!item.new) {
      url = `/openboxes/api/stocklistItems/${item.requisitionItem.id}`;
    }

    apiClient.post(url, flattenRequest(item))
      .then((response) => {
        this.setState({
          data: update(this.state.data, {
            [index]: {
              $set: parseResponse(response.data.data),
            },
          }),
        });
      })
      .catch(this.props.hideSpinner());
  }

  deleteItem(index) {
    const item = this.state.data[index];

    if (item.new) {
      this.removeItem(index);
    } else {
      this.props.showSpinner();
      const url = `/openboxes/api/stocklistItems/${item.requisitionItem.id}`;

      apiClient.delete(url)
        .then(() => {
          this.removeItem(index);
        })
        .catch(this.props.hideSpinner());
    }
  }

  // eslint-disable-next-line no-unused-vars,class-methods-use-this
  printItem(index) {
    // TODO add proper implementation
  }

  // eslint-disable-next-line no-unused-vars,class-methods-use-this
  mailItem(index) {
    // TODO add proper implementation
  }

  removeItem(index) {
    this.setState({
      data: update(this.state.data, {
        $splice: [
          [index, 1],
        ],
      }),
    });
  }

  render() {
    const { data } = this.state;
    return (
      <div className="main-container">
        { this.state.productInfo &&
          <div className="mb-2">
            <div className="d-flex flex-row justify-content-between">
              <div className="d-flex flex-row align-items-end">
                <h6 className="mb-0 mr-1">{this.state.productInfo.productCode}</h6>
                <h5 className="mb-0">{this.state.productInfo.name}</h5>
              </div>
              <div className="align-self-center">
                <button
                  className="btn btn-outline-primary btn-xs"
                  onClick={() => { window.location = `/openboxes/inventoryItem/showStockCard/${this.state.productInfo.id}`; }}
                >Return to stock card
                </button>
              </div>
            </div>
            <div className="d-flex flex-row">
              { _.map(this.state.productInfo.catalogs, catalog => (<div className="stocklist-category px-1 mr-1">{catalog.name}</div>)) }
            </div>
          </div>
        }
        <ReactTable
          data={data}
          pivotBy={['locationGroup.name', 'location.name']}
          showPagination={false}
          minRows={0}
          sortable={false}
          collapseOnDataChange={false}
          resolveData={values => values.map((row) => {
            if (!_.get(row, 'locationGroup.name')) {
              return { ...row, locationGroup: { name: 'No location group' } };
            }

            return row;
          })}
          columns={[
            {
              Header: 'Location Group Name',
              accessor: 'locationGroup.name',
              className: 'w-space-normal',
            },
            {
              Header: 'Location Name',
              accessor: 'location.name',
              aggregate: () => '',
              className: 'w-space-normal',
            },
            {
              Header: 'Stocklist Name',
              accessor: 'name',
              aggregate: () => '',
              // eslint-disable-next-line react/prop-types
              Cell: ({ aggregated, original }) => {
                if (aggregated) {
                  return '';
                }

                return (
                  <a href={`/openboxes/requisitionTemplate/show/${original.stocklistId}`}>
                    {original.name}
                  </a>
                );
              },
            },
            {
              Header: 'Monthly demand',
              accessor: 'monthlyDemand',
              aggregate: vals => _.sum(vals),
              className: 'text-center',
            },
            {
              Header: 'Manager',
              accessor: 'manager.name',
              aggregate: () => '',
            },
            {
              Header: 'Replenishment period',
              accessor: 'replenishmentPeriod',
              aggregate: () => '',
              className: 'text-center',
            },
            {
              Header: 'Maximum  Quantity',
              accessor: 'maxQuantity',
              aggregate: vals => _.sum(vals),
              className: 'text-center',
              // eslint-disable-next-line react/prop-types
              Cell: ({ aggregated, index, original }) => {
                if (aggregated) {
                  return '';
                }

                if (!original.new && !original.edit) {
                  return _.isNil(original.maxQuantity) ? '' : original.maxQuantity;
                }

                return (
                  <div className={_.isNil(original.maxQuantity) || original.maxQuantity === '' ? 'has-error' : ''}>
                    <Input
                      value={original.maxQuantity || ''}
                      onChange={value => this.updateItemField(index, 'maxQuantity', value)}
                    />
                  </div>
                );
              },
            },
            {
              Header: 'Unit of measure',
              accessor: 'uom',
              aggregate: () => '',
              className: 'text-center',
            },
            {
              Header: 'Actions',
              accessor: 'edit',
              minWidth: 230,
              className: 'text-center',
              aggregate: () => '',
              // eslint-disable-next-line react/prop-types
              Cell: ({ aggregated, index, original }) => {
                if (aggregated) {
                  return '';
                }

                return (
                  <div>
                    <button
                      className="btn btn-outline-primary btn-xs mx-1"
                      disabled={original.edit || original.new}
                      onClick={() => this.editItem(index)}
                    >Edit
                    </button>
                    <button
                      className="btn btn-outline-primary btn-xs mx-1"
                      disabled={(!original.edit && !original.new) || !original.stocklistId
                      || _.isNil(original.maxQuantity) || original.maxQuantity === ''}
                      onClick={() => this.saveItem(index, original)}
                    >Save
                    </button>
                    <button
                      className="btn btn-outline-danger btn-xs mx-1"
                      onClick={() => this.deleteItem(index)}
                    >Delete
                    </button>
                    <button
                      className="btn btn-outline-secondary btn-xs mx-1"
                      disabled={original.edit || original.new}
                      onClick={() => this.printItem(index)}
                    >Print
                    </button>
                    <button
                      className="btn btn-outline-secondary btn-xs mx-1"
                      disabled={original.edit || original.new}
                      onClick={() => this.mailItem(index)}
                    >Email
                    </button>
                  </div>
                );
              },
            },
          ]}
        />
        <div className="d-flex flex-row my-1">
          <Select
            value={this.state.selectedStocklist}
            onChange={value => this.setState({ selectedStocklist: value })}
            options={this.state.availableStocklists}
            objectValue
            className="select-xs stocklist-select"
          />
          <button
            className="btn btn-outline-success btn-xs ml-1"
            disabled={!this.state.selectedStocklist}
            onClick={() => {
              this.addItem(this.state.selectedStocklist);
            }}
          >Add Stocklist
          </button>
        </div>
      </div>
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
