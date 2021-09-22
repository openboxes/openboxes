import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Form } from 'react-final-form';
import arrayMutators from 'final-form-arrays';
import update from 'immutability-helper';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate } from 'react-localize-redux';

import 'react-confirm-alert/src/react-confirm-alert.css';

import TextField from '../form-elements/TextField';
import ArrayField from '../form-elements/ArrayField';
import LabelField from '../form-elements/LabelField';
import { renderFormField } from '../../utils/form-utils';
import { showSpinner, hideSpinner } from '../../actions';
import apiClient, { flattenRequest, parseResponse } from '../../utils/apiClient';
import Translate, { translateWithDefaultMessage } from '../../utils/Translate';
import Select from '../../utils/Select';

const FIELD = {
  requirements: {
    type: ArrayField,
    arrowsNavigation: true,
    fields: {
      'product.productCode': {
        type: LabelField,
        label: 'react.stockMovement.productCode.label',
        defaultMessage: 'Code',
      },
      'product.name': {
        type: LabelField,
        label: 'react.stockMovement.product.label',
        defaultMessage: 'Product',
      },
      zone: {
        type: LabelField,
        label: 'react.replenishment.currentZone.label',
        defaultMessage: 'Current Zone',
      },
      'binLocation.name': {
        type: LabelField,
        label: 'react.replenishment.currentBinLocation.label',
        defaultMessage: 'Current Bin Location',
        attributes: {
          formatValue: (value) => {
            if (value) {
              return value;
            }
            return 'DEFAULT';
          },
        },
      },
      quantityInBin: {
        type: LabelField,
        label: 'react.replenishment.quantityInBin.label',
        defaultMessage: 'Qty in Bin',
      },
      minQuantity: {
        type: LabelField,
        label: 'react.replenishment.minQuantity.label',
        defaultMessage: 'Min Qty',
      },
      maxQuantity: {
        type: LabelField,
        label: 'react.replenishment.maxQuantity.label',
        defaultMessage: 'Max Qty',
      },
      totalQuantityOnHand: {
        type: LabelField,
        label: 'react.replenishment.totalQuantityOnHand.label',
        defaultMessage: 'Total QoH',
      },
      quantity: {
        type: TextField,
        label: 'react.stockMovement.quantityToTransfer.label',
        defaultMessage: 'Qty to Transfer',
        attributes: {
          type: 'number',
        },
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
    if (item.quantity && item.quantity < 0) {
      errors.requirements[key] = { quantity: 'react.replenishment.error.quantity.label' };
    }
  });
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
    if (inventoryLevelStatus) {
      url += `&inventoryLevelStatus=${inventoryLevelStatus}`;
    }

    return apiClient.get(url)
      .then((resp) => {
        const requirements = _.map(parseResponse(resp.data.data), requirement => ({
          ...requirement,
          quantity: requirement.quantityNeeded,
        }));
        this.setState({ values: { requirements }, isDirty: false }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  createReplenishment() {
    this.props.showSpinner();
    const url = '/openboxes/api/replenishments/';
    const payload = {
      replenishmentItems: _.filter(
        this.state.values.requirements,
        item => _.toInteger(item.quantity) > 0,
      ),
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
              <Select
                value={this.state.inventoryLevelStatus}
                onChange={value => this.inventoryLevelStatusChange(value)}
                options={this.state.statusOptions}
                className="select-sm stocklist-select"
                clearable={false}
                objectValue
              />
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
                    updateRow: this.updateRow,
                    values,
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
