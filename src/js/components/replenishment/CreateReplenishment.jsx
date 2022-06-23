import React, { Component } from 'react';

import arrayMutators from 'final-form-arrays';
import update from 'immutability-helper';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { confirmAlert } from 'react-confirm-alert';
import { Form } from 'react-final-form';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { hideSpinner, showSpinner } from 'actions';
import ArrayField from 'components/form-elements/ArrayField';
import CheckboxField from 'components/form-elements/CheckboxField';
import LabelField from 'components/form-elements/LabelField';
import TextField from 'components/form-elements/TextField';
import apiClient, { flattenRequest, parseResponse } from 'utils/apiClient';
import { renderFormField } from 'utils/form-utils';
import Select from 'utils/Select';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';


const FIELD = {
  requirements: {
    type: ArrayField,
    arrowsNavigation: true,
    fields: {
      checked: {
        type: CheckboxField,
        label: 'react.stockMovement.selectAll.label',
        defaultMessage: 'Select All',
        flexWidth: 4,
        getDynamicAttr: ({
          rowIndex, allRowsSelected, selectAllCode, updateSelectedItems,
        }) => ({
          headerHtml: () => (
            <input
              type="checkbox"
              className="mt-1"
              checked={allRowsSelected}
              onClick={selectAllCode}
            />
          ),
          onChange: checkState => updateSelectedItems(checkState, rowIndex),
        }),
      },
      'product.productCode': {
        type: LabelField,
        label: 'react.stockMovement.productCode.label',
        defaultMessage: 'Code',
        flexWidth: 5,
        headerAlign: 'left',
        attributes: {
          cellClassName: 'text-left',
        },
      },
      'product.name': {
        type: LabelField,
        label: 'react.stockMovement.product.label',
        defaultMessage: 'Product',
        flexWidth: 30,
        headerAlign: 'left',
        attributes: {
          cellClassName: 'text-left',
          showValueTooltip: true,
        },
      },
      zone: {
        type: LabelField,
        label: 'react.replenishment.zone.label',
        defaultMessage: 'Zone',
        headerAlign: 'left',
        attributes: {
          className: 'text-left',
        },
      },
      'binLocation.name': {
        type: LabelField,
        label: 'react.replenishment.bin.label',
        defaultMessage: 'Bin',
        headerAlign: 'left',
        attributes: {
          formatValue: (value) => {
            if (value) {
              return value;
            }
            return 'DEFAULT';
          },
          className: 'text-left',
        },
      },
      quantityInBin: {
        type: LabelField,
        label: 'react.replenishment.quantityInBin.label',
        defaultMessage: 'Qty in Bin',
        headerAlign: 'right',
        flexWidth: 8,
        attributes: {
          cellClassName: 'text-right',
        },
      },
      maxQuantity: {
        type: LabelField,
        label: 'react.replenishment.maxQuantity.label',
        defaultMessage: 'Max Qty',
        headerAlign: 'right',
        flexWidth: 8,
        attributes: {
          cellClassName: 'text-right',
        },
      },
      quantityAvailable: {
        type: LabelField,
        label: 'react.replenishment.quantityAvailable.label',
        defaultMessage: 'Qty available',
        headerAlign: 'right',
        flexWidth: 8,
        attributes: {
          cellClassName: 'text-right',
        },
      },
      quantity: {
        type: TextField,
        label: 'react.stockMovement.quantityToTransfer.label',
        defaultMessage: 'Qty to Transfer',
        headerAlign: 'center',
        attributes: {
          type: 'number',
          cellClassName: 'text-center',
        },
        flexWidth: 10,
        fieldKey: '',
        getDynamicAttr: ({
          updateRow, values, rowIndex,
        }) => ({
          onBlur: () => updateRow(values, rowIndex),
        }),
      },
    },
  },
};

function validate(values) {
  const errors = {};
  errors.requirements = [];

  _.forEach(values.requirements, (item, key) => {
    if (item.quantity) {
      if (item.quantity < 0) {
        errors.requirements[key] = { quantity: 'react.replenishment.error.quantity.label' };
      }
      if (item.quantity > item.quantityAvailable) {
        errors.requirements[key] = { quantity: 'react.replenishment.error.quantity.greaterThanQATP.label' };
      }
    }
  });
  const anyRowSelected = _.find(values.requirements, row => row.checked);
  if (!anyRowSelected) {
    _.forEach(values.requirements, (item, key) => {
      errors.requirements[key] = {
        ...errors.requirements[key],
        checked: 'react.replenishment.error.selected.label',
      };
    });
  }
  return errors;
}

const DEFAULT_OPTION = 'BELOW_MINIMUM';

class CreateReplenishment extends Component {
  constructor(props) {
    super(props);
    this.state = {
      inventoryLevelStatus: DEFAULT_OPTION,
      statusOptions: [],
      values: { ...this.props.initialValues, requirements: [] },
      isDirty: false,
    };
    this.updateRow = this.updateRow.bind(this);
    this.updateSelectedItems = this.updateSelectedItems.bind(this);
    this.selectAllRows = this.selectAllRows.bind(this);
    this.allRowsSelected = this.allRowsSelected.bind(this);
  }

  componentDidMount() {
    if (this.props.replenishmentTranslationsFetched) {
      this.dataFetched = true;
      this.fetchStatusOptions();
      this.fetchRequirements(this.props.locationId);
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.replenishmentTranslationsFetched) {
      if (!this.dataFetched) {
        this.dataFetched = true;

        this.fetchStatusOptions();
        this.fetchRequirements(this.props.locationId);
      } else if (this.props.locationId !== nextProps.locationId) {
        this.fetchRequirements(nextProps.locationId);
      }
    }
  }

  allRowsSelected() {
    return !_.find(this.state.values.requirements, row => !row.checked);
  }

  selectAllRows() {
    const isAllSelected = this.allRowsSelected();
    this.setState({
      values: update(this.state.values, {
        requirements: {
          $apply: req => req.map(it => ({
            ...it, checked: !isAllSelected,
          })),
        },
      }),
    });
  }

  updateSelectedItems(checkedValue, index) {
    this.setState({
      values: update(this.state.values, {
        requirements: { [index]: { checked: { $set: checkedValue } } },
      }),
    });
  }

  updateRow(values, index) {
    const item = values.requirements[index];
    if (item.quantity !== this.state.values.requirements[index].quantity) {
      this.setState({
        values: update(values, {
          requirements: { [index]: { $set: item } },
        }),
        isDirty: true,
      });
    }
  }

  dataFetched = false;

  fetchStatusOptions() {
    const url = '/openboxes/api/replenishments/statusOptions';
    return apiClient.get(url)
      .then((resp) => {
        const statusOptions = resp.data.data;
        this.setState({ statusOptions });
      })
      .catch(() => {});
  }

  fetchRequirements(locationId) {
    this.props.showSpinner();
    const { inventoryLevelStatus } = this.state;
    let url = `/openboxes/api/requirements?location.id=${locationId}`;
    if (inventoryLevelStatus.id) {
      url += `&inventoryLevelStatus=${inventoryLevelStatus.id}`;
    }

    return apiClient.get(url)
      .then((resp) => {
        const requirements = _.map(parseResponse(resp.data.data), requirement => ({
          ...requirement,
          quantity: requirement.quantityNeeded,
          checked: true,
        }));
        this.setState({ values: { requirements }, isDirty: false }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  createReplenishment(values) {
    this.props.showSpinner();
    const url = '/openboxes/api/replenishments/';
    const payload = {
      replenishmentItems: values.requirements.filter(item => item.checked && item.quantity > 0),
    };

    apiClient.post(url, flattenRequest(payload))
      .then((response) => {
        this.props.hideSpinner();
        this.props.history.push(`/openboxes/replenishment/create/${response.data}`);
        this.props.nextPage(this.state.values);
      })
      .catch(() => this.props.hideSpinner());
  }

  inventoryLevelStatusChange(value) {
    if (this.state.isDirty) {
      confirmAlert({
        title: this.props.translate('react.replenishment.message.confirmStatusChange.label', 'Confirm inventory level status change'),
        message: this.props.translate(
          'react.replenishment.confirmStatusChange.label',
          'Previously edited quantities will be lost after this change. Are you sure?',
        ),
        buttons: [
          {
            label: this.props.translate('react.default.yes.label', 'Yes'),
            onClick: () => this.setState({
              inventoryLevelStatus: value,
            }, () => this.fetchRequirements(this.props.locationId)),
          },
          {
            label: this.props.translate('react.default.no.label', 'No'),
          },
        ],
      });
    } else {
      this.setState({
        inventoryLevelStatus: value,
      }, () => this.fetchRequirements(this.props.locationId));
    }
  }

  render() {
    return (
      <Form
        onSubmit={() => {}}
        validate={validate}
        mutators={{ ...arrayMutators }}
        initialValues={this.state.values}
        render={({ handleSubmit, values, invalid }) => (
          <div className="d-flex flex-column">
            <div className="d-flex justify-content-between submit-buttons mt-0 mb-3">
              <div className="d-flex flex-column">
                <label htmlFor="stock-level-filter">
                  <Translate
                    id="react.replenishment.filter.label"
                    defaultMessage="Replenish bins that have a stock level"
                  />:
                </label>
                <Select
                  name="stock-level-filter"
                  value={this.state.inventoryLevelStatus}
                  onChange={value => this.inventoryLevelStatusChange(value)}
                  options={this.state.statusOptions}
                  className="select-sm stocklist-select"
                  clearable={false}
                />
              </div>
              <button
                type="submit"
                onClick={() => {
                  if (!invalid) {
                    this.createReplenishment(values);
                  }
                }}
                className="btn btn-outline-primary float-right btn-xs"
                disabled={invalid}
              ><Translate id="react.replenishment.next.label" defaultMessage="Next" />
              </button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="table-form">
                {_.map(FIELD, (fieldConfig, fieldName) =>
                  renderFormField(fieldConfig, fieldName, {
                    values,
                    updateRow: this.updateRow,
                    updateSelectedItems: this.updateSelectedItems,
                    allRowsSelected: this.allRowsSelected(),
                    selectAllCode: this.selectAllRows,
                  }))}
              </div>
              <div className="submit-buttons">
                <button
                  type="submit"
                  onClick={() => {
                    if (!invalid) {
                      this.createReplenishment(values);
                    }
                  }}
                  className="btn btn-outline-primary btn-form float-right btn-xs"
                  disabled={invalid}
                ><Translate id="react.replenishment.next.label" defaultMessage="Next" />
                </button>
              </div>
            </form>
          </div>
        )}
      />
    );
  }
}

const mapStateToProps = state => ({
  replenishmentTranslationsFetched: state.session.fetchedTranslations.replenishment,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps, { showSpinner, hideSpinner })(CreateReplenishment);

CreateReplenishment.propTypes = {
  initialValues: PropTypes.shape({}),
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  nextPage: PropTypes.func.isRequired,
  locationId: PropTypes.string.isRequired,
  history: PropTypes.shape({ push: PropTypes.func }).isRequired,
  replenishmentTranslationsFetched: PropTypes.bool.isRequired,
  translate: PropTypes.func.isRequired,
};

CreateReplenishment.defaultProps = {
  initialValues: {},
};
