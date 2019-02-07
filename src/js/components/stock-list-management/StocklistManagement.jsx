import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import ReactTable from 'react-table';
import update from 'immutability-helper';
import { getTranslate } from 'react-localize-redux';
import { Tooltip } from 'react-tippy';

import 'react-table/react-table.css';

import apiClient, { parseResponse, flattenRequest } from './../../utils/apiClient';
import { hideSpinner, showSpinner } from '../../actions';
import Select from '../../utils/Select';
import Input from '../../utils/Input';
import EmailModal from './EmailModal';
import Translate, { translateWithDefaultMessage } from '../../utils/Translate';

class StocklistManagement extends Component {
  constructor(props) {
    super(props);

    this.state = {
      data: [],
      selectedStocklist: null,
      availableStocklists: [],
      productInfo: null,
      users: [],
    };

    this.addItem = this.addItem.bind(this);
    this.editItem = this.editItem.bind(this);
    this.updateItemField = this.updateItemField.bind(this);
    this.saveItem = this.saveItem.bind(this);
    this.deleteItem = this.deleteItem.bind(this);
  }

  componentDidMount() {
    this.fetchData();
    this.fetchAvailableStocklists();
    this.fetchProductInfo();
    this.fetchUsers();
  }

  fetchUsers() {
    this.props.showSpinner();
    const url = '/openboxes/api/generic/person';

    apiClient.get(url)
      .then((response) => {
        const users = _.map(response.data.data, user => (
          { value: { id: user.id, email: user.email, label: user.name }, label: user.name }
        ));
        this.setState({ users });
        this.props.hideSpinner();
      })
      .catch(() => this.props.hideSpinner());
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
              ><Translate id="stockListManagement.returnStockCard.label" defaultMessage="Return to stock card" />
              </button>
            </div>
          </div>
          <div className="d-flex flex-row">
            { _.map(this.state.productInfo.catalogs, catalog => (<div key={catalog.id} className="stocklist-category px-1 mr-1">{catalog.name}</div>)) }
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
              Header:
  <Tooltip
    title={this.props.translate('stockListManagement.locationGroup.label', 'Location Group Name')}
    theme="transparent"
    delay="150"
    duration="250"
    hideDelay="50"
  ><Translate id="stockListManagement.locationGroup.label" defaultMessage="Location Group Name" />
  </Tooltip>,
              accessor: 'locationGroup.name',
              className: 'w-space-normal',
            },
            {
              Header:
  <Tooltip
    title={this.props.translate('stockListManagement.locationName.label', 'Location Name')}
    theme="transparent"
    delay="150"
    duration="250"
    hideDelay="50"
  ><Translate id="stockListManagement.locationName.label" defaultMessage="Location Name" />
  </Tooltip>,
              accessor: 'location.name',
              aggregate: () => '',
              className: 'w-space-normal',
            },
            {
              Header:
  <Tooltip
    title={this.props.translate('stockListManagement.stockListName.label', 'Stocklist Name')}
    theme="transparent"
    delay="150"
    duration="250"
    hideDelay="50"
  ><Translate id="stockListManagement.stockListName.label" defaultMessage="Stocklist Name" />
  </Tooltip>,
              accessor: 'name',
              aggregate: () => '',
              // eslint-disable-next-line react/prop-types
              Cell: ({ aggregated, original }) => {
                if (aggregated) {
                  return '';
                }

                return (
                  <a href={`/openboxes/requisitionTemplate/show/${original.stocklistId}`}>
                    <Tooltip
                      title={original.name}
                      theme="transparent"
                      delay="150"
                      duration="250"
                      hideDelay="50"
                    >{original.name}
                    </Tooltip>
                  </a>
                );
              },
            },
            {
              Header:
  <Tooltip
    title={this.props.translate('stockListManagement.monthlyStockListQty.label', 'Monthly Stocklist Qty')}
    theme="transparent"
    delay="150"
    duration="250"
    hideDelay="50"
  ><Translate id="stockListManagement.monthlyStockListQty.label" defaultMessage="Monthly Stocklist Qty" />
  </Tooltip>,
              accessor: 'monthlyDemand',
              aggregate: vals => _.sum(vals),
              className: 'text-center',
              Cell: ({ aggregated, original }) => {
                if (aggregated) {
                  return '';
                }

                return (
                  <Tooltip
                    title={original.monthlyDemand}
                    theme="transparent"
                    delay="150"
                    duration="250"
                    hideDelay="50"
                  >{original.monthlyDemand}
                  </Tooltip>
                );
              },
            },
            {
              Header:
  <Tooltip
    title={this.props.translate('stockListManagement.manager.label', 'Manager')}
    theme="transparent"
    delay="150"
    duration="250"
    hideDelay="50"
  ><Translate id="stockListManagement.manager.label" defaultMessage="Manager" />
  </Tooltip>,
              accessor: 'manager.name',
              aggregate: () => '',
              Cell: ({ aggregated, original }) => {
                if (aggregated) {
                  return '';
                }

                return (
                  <Tooltip
                    title={original.manager.name}
                    theme="transparent"
                    delay="150"
                    duration="250"
                    hideDelay="50"
                  >{original.manager.name}
                  </Tooltip>
                );
              },
            },
            {
              Header:
  <Tooltip
    title={this.props.translate('stockListManagement.replenishmentPeriod.label', 'Replenishment period')}
    theme="transparent"
    delay="150"
    duration="250"
    hideDelay="50"
  ><Translate id="stockListManagement.replenishmentPeriod.label" defaultMessage="Replenishment period" />
  </Tooltip>,
              accessor: 'replenishmentPeriod',
              aggregate: () => '',
              className: 'text-center',
              Cell: ({ aggregated, original }) => {
                if (aggregated) {
                  return '';
                }

                return (
                  <Tooltip
                    title={original.replenishmentPeriod}
                    theme="transparent"
                    delay="150"
                    duration="250"
                    hideDelay="50"
                  >{original.replenishmentPeriod}
                  </Tooltip>
                );
              },
            },
            {
              Header:
  <Tooltip
    title={this.props.translate('stockListManagement.replenishmentQty.label', 'Replenishment Qty')}
    theme="transparent"
    delay="150"
    duration="250"
    hideDelay="50"
  ><Translate id="stockListManagement.replenishmentQty.label" defaultMessage="Replenishment Qty" />
  </Tooltip>,
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
                    <Tooltip
                      title={original.maxQuantity}
                      theme="transparent"
                      delay="150"
                      duration="250"
                      hideDelay="50"
                    >
                      <Input
                        value={original.maxQuantity || ''}
                        onChange={value => this.updateItemField(index, 'maxQuantity', value)}
                      />
                    </Tooltip>
                  </div>
                );
              },
            },
            {
              Header:
  <Tooltip
    title={this.props.translate('stockListManagement.uom.label', 'Unit of measure')}
    theme="transparent"
    delay="150"
    duration="250"
    hideDelay="50"
  ><Translate id="stockListManagement.uom.label" defaultMessage="Unit of measure" />
  </Tooltip>,
              accessor: 'uom',
              aggregate: () => '',
              className: 'text-center',
              Cell: ({ aggregated, original }) => {
                if (aggregated) {
                  return '';
                }

                return (
                  <Tooltip
                    title={original.uom}
                    theme="transparent"
                    delay="150"
                    duration="250"
                    hideDelay="50"
                  >{original.uom}
                  </Tooltip>
                );
              },
            },
            {
              Header:
  <Tooltip
    title={this.props.translate('stockListManagement.actions.label', 'Actions')}
    theme="transparent"
    delay="150"
    duration="250"
    hideDelay="50"
  ><Translate id="stockListManagement.actions.label" defaultMessage="Actions" />
  </Tooltip>,
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
                  <div className="d-flex">
                    <button
                      className="btn btn-outline-primary btn-xs mx-1"
                      disabled={original.edit || original.new}
                      onClick={() => this.editItem(index)}
                    ><Translate id="default.button.edit.label" defaultMessage="Edit" />
                    </button>
                    <button
                      className="btn btn-outline-primary btn-xs mr-1"
                      disabled={(!original.edit && !original.new) || !original.stocklistId
                      || _.isNil(original.maxQuantity) || original.maxQuantity === ''}
                      onClick={() => this.saveItem(index, original)}
                    ><Translate id="default.button.save.label" defaultMessage="Save" />
                    </button>
                    <button
                      className="btn btn-outline-danger btn-xs mr-1"
                      onClick={() => this.deleteItem(index)}
                    ><Translate id="default.button.delete.label" defaultMessage="Delete" />
                    </button>
                    <a
                      className="btn btn-outline-secondary btn-xs mr-1"
                      disabled={original.edit || original.new}
                      href={`/openboxes/stocklist/renderPdf/${original.stocklistId}`}
                    ><Translate id="default.button.print.label" defaultMessage="Print" />
                    </a>
                    <EmailModal
                      stocklistId={original.stocklistId}
                      users={this.state.users}
                      manager={original.manager}
                    />
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
          ><Translate id="stockListManagement.addStockList.label" defaultMessage="Add stocklist" />
          </button>
        </div>
      </div>
    );
  }
}

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps, { showSpinner, hideSpinner })(StocklistManagement);

StocklistManagement.propTypes = {
  /** React router's object which contains information about url varaiables and params */
  match: PropTypes.shape({
    params: PropTypes.shape({ productId: PropTypes.string }),
  }).isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
};
