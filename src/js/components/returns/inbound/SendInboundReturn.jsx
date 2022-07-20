import React, { Component } from 'react';

import arrayMutators from 'final-form-arrays';
import _ from 'lodash';
import moment from 'moment';
import PropTypes from 'prop-types';
import { confirmAlert } from 'react-confirm-alert';
import { Form } from 'react-final-form';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import Alert from 'react-s-alert';

import { hideSpinner, showSpinner } from 'actions';
import ArrayField from 'components/form-elements/ArrayField';
import DateField from 'components/form-elements/DateField';
import LabelField from 'components/form-elements/LabelField';
import SelectField from 'components/form-elements/SelectField';
import TextField from 'components/form-elements/TextField';
import apiClient, { flattenRequest, parseResponse } from 'utils/apiClient';
import { renderFormField } from 'utils/form-utils';
import renderHandlingIcons from 'utils/product-handling-icons';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';
import splitTranslation from 'utils/translation-utils';

import 'react-confirm-alert/src/react-confirm-alert.css';

const SHIPMENT_FIELDS = {
  'origin.name': {
    label: 'react.inboundReturns.origin.label',
    defaultMessage: 'Origin',
    type: params => <TextField {...params} />,
    attributes: {
      disabled: true,
    },
  },
  'destination.name': {
    label: 'react.inboundReturns.destination.label',
    defaultMessage: 'Origin',
    type: params => <TextField {...params} />,
    attributes: {
      disabled: true,
    },
  },
  dateShipped: {
    type: DateField,
    label: 'react.stockMovement.shipDate.label',
    defaultMessage: 'Shipment date',
    attributes: {
      dateFormat: 'MM/DD/YYYY',
      required: true,
      autoComplete: 'off',
    },
    getDynamicAttr: ({ issued }) => ({
      disabled: issued,
    }),
  },
  shipmentType: {
    type: SelectField,
    label: 'react.stockMovement.shipmentType.label',
    defaultMessage: 'Shipment type',
    attributes: {
      required: true,
      showValueTooltip: true,
      valueKey: 'id',
      labelKey: 'name',
    },
    getDynamicAttr: ({ shipmentTypes, issued }) => ({
      options: shipmentTypes,
      disabled: issued,
    }),
  },
  trackingNumber: {
    type: TextField,
    label: 'react.stockMovement.trackingNumber.label',
    defaultMessage: 'Tracking number',
    getDynamicAttr: ({ issued }) => ({
      disabled: issued,
    }),
  },
  driverName: {
    type: TextField,
    label: 'react.stockMovement.driverName.label',
    defaultMessage: 'Driver name',
    getDynamicAttr: ({ issued }) => ({
      disabled: issued,
    }),
  },
  comments: {
    type: TextField,
    label: 'react.stockMovement.comments.label',
    defaultMessage: 'Comments',
    getDynamicAttr: ({ issued }) => ({
      disabled: issued,
    }),
  },
  expectedDeliveryDate: {
    type: DateField,
    label: 'react.stockMovement.expectedDeliveryDate.label',
    defaultMessage: 'Expected receipt date',
    attributes: {
      dateFormat: 'MM/DD/YYYY',
      required: true,
      autoComplete: 'off',
    },
    getDynamicAttr: ({ issued }) => ({
      disabled: issued,
    }),
  },
};

const FIELDS = {
  stockTransferItems: {
    type: ArrayField,
    getDynamicRowAttr: ({ rowValues, translate }) => {
      let className = '';
      let tooltip = '';
      if (rowValues.recalled && rowValues.onHold) {
        className = 'recalled-and-on-hold';
        tooltip = translate('react.inboundReturns.recalledAndOnHold.label');
      } else if (rowValues.recalled) {
        className = 'recalled';
        tooltip = translate('react.inboundReturns.recalled.label');
      } else if (rowValues.onHold) {
        className = 'on-hold';
        tooltip = translate('react.inboundReturns.onHold.label');
      }
      return { className, tooltip };
    },
    fields: {
      'product.productCode': {
        type: LabelField,
        label: 'react.stockMovement.productCode.label',
        defaultMessage: 'Code',
        flexWidth: '1',
      },
      'product.name': {
        type: (params) => {
          const { rowIndex, values } = params;
          const handlingIcons = _.get(values, `stockTransferItems[${rowIndex}].product.handlingIcons`, []);
          const productNameWithIcons = (
            <div className="d-flex">
              <Translate id={params.fieldValue} defaultMessage={params.fieldValue} />
              {renderHandlingIcons(handlingIcons)}
            </div>);
          return (<LabelField {...params} fieldValue={productNameWithIcons} />);
        },
        label: 'react.stockMovement.product.label',
        defaultMessage: 'Product',
        flexWidth: '2',
        headerAlign: 'left',
        attributes: {
          showValueTooltip: true,
          className: 'text-left ml-1',
        },
      },
      lotNumber: {
        type: LabelField,
        label: 'react.inboundReturn.lot.label',
        defaultMessage: 'Lot',
        flexWidth: '1',
      },
      expirationDate: {
        type: LabelField,
        label: 'react.inboundReturn.expiry.label',
        defaultMessage: 'Expiry',
        flexWidth: '1',
      },
      quantity: {
        type: LabelField,
        label: 'react.inboundReturn.quantity.label',
        defaultMessage: 'Quantity',
        flexWidth: '1',
      },
      'recipient.name': {
        type: LabelField,
        label: 'react.inboundReturn.recipient.label',
        defaultMessage: 'Recipient',
        flexWidth: '1',
      },
    },
  },
};

class SendMovementPage extends Component {
  constructor(props) {
    super(props);
    this.state = {
      shipmentTypes: [],
      values: { inboundReturn: { ...this.props.initialValues } },
    };

    this.fetchInboundReturn = this.fetchInboundReturn.bind(this);
    this.validate = this.validate.bind(this);
    this.rollbackReturnOrder = this.rollbackReturnOrder.bind(this);
  }

  componentDidMount() {
    if (this.props.inboundReturnsTranslationsFetched) {
      this.dataFetched = true;
      this.fetchInboundReturn();
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.inboundReturnsTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;
      this.fetchInboundReturn();
    }
  }

  fetchShipmentTypes() {
    const url = '/openboxes/api/generic/shipmentType';
    return apiClient.get(url)
      .then((response) => {
        const shipmentTypes = _.map(response.data.data, (type) => {
          const [en, fr] = _.split(type.name, '|fr:');
          return {
            ...type,
            label: this.props.locale === 'fr' && fr ? fr : en,
          };
        });

        this.setState({ shipmentTypes }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  dataFetched = false;

  fetchInboundReturn() {
    this.props.showSpinner();
    const url = `/openboxes/api/stockTransfers/${this.props.match.params.inboundReturnId}`;

    return apiClient.get(url)
      .then((resp) => {
        const inboundReturn = parseResponse(resp.data.data);
        this.setState({
          values: {
            inboundReturn: {
              ...inboundReturn,
              shipmentType: {
                ...inboundReturn.shipmentType,
                label: splitTranslation(inboundReturn.shipmentType.name, this.props.locale),
              },
            },
          },
        }, () => this.fetchShipmentTypes());
      })
      .catch(() => this.props.hideSpinner());
  }

  rollbackReturnOrder(values) {
    this.props.showSpinner();
    const url = `/openboxes/api/stockTransfers/${this.props.match.params.inboundReturnId}/rollback`;

    const isDestination = this.props.currentLocationId === values.destination.id;

    if (isDestination) {
      apiClient.post(url)
        .then(() => {
          this.props.hideSpinner();
          window.location.reload();
        });
    } else {
      this.props.hideSpinner();
      Alert.error(this.props.translate(
        'react.stockMovement.alert.rollbackShipment.label',
        'You are not able to rollback shipment from your location.',
      ));
    }
  }

  sendInboundReturn(values, invalid) {
    if (!invalid) {
      this.props.showSpinner();
      const payload = {
        ...values,
      };
      const url = `/openboxes/api/stockTransfers/${this.props.match.params.inboundReturnId}/sendShipment`;
      this.saveValues(payload)
        .then(() => {
          apiClient.post(url, flattenRequest(payload))
            .then(() => {
              window.location = `/openboxes/stockMovement/show/${this.props.match.params.inboundReturnId}`;
            })
            .catch(() => {
              this.props.hideSpinner();
            });
        })
        .catch(() => this.props.hideSpinner());
    }
  }

  validate(values) {
    const errors = {};
    const date = moment(this.props.minimumExpirationDate, 'MM/DD/YYYY');
    const dateShipped = moment(values.dateShipped, 'MM/DD/YYYY');
    const expectedDeliveryDate = moment(values.expectedDeliveryDate, 'MM/DD/YYYY');

    if (date.diff(dateShipped) > 0) {
      errors.dateShipped = 'react.stockMovement.error.invalidDate.label';
    }
    if (!values.dateShipped) {
      errors.dateShipped = 'react.default.error.requiredField.label';
    }
    if (!values.shipmentType) {
      errors.shipmentType = 'react.default.error.requiredField.label';
    }
    if (!values.expectedDeliveryDate) {
      errors.expectedDeliveryDate = 'react.default.error.requiredField.label';
    }
    if (moment(dateShipped).diff(expectedDeliveryDate) > 0) {
      errors.expectedDeliveryDate = 'react.stockMovement.error.pastDate.label';
    }

    return errors;
  }

  saveAndExit(values) {
    const errors = this.validate(values);
    if (_.isEmpty(errors)) {
      this.saveValues(values)
        .then(() => {
          window.location = `/openboxes/stockMovement/show/${this.props.match.params.inboundReturnId}`;
        });
    } else {
      confirmAlert({
        title: this.props.translate('react.stockMovement.confirmExit.label', 'Confirm save'),
        message: this.props.translate(
          'react.stockMovement.confirmExit.message',
          'Validation errors occurred. Are you sure you want to exit and lose unsaved data?',
        ),
        buttons: [
          {
            label: this.props.translate('react.default.yes.label', 'Yes'),
            onClick: () => { window.location = `/openboxes/stockMovement/show/${this.props.match.params.inboundReturnId}`; },
          },
          {
            label: this.props.translate('react.default.no.label', 'No'),
          },
        ],
      });
    }
  }

  save(values) {
    this.saveValues(values)
      .then((resp) => {
        const inboundReturn = parseResponse(resp.data.data);
        this.setState({
          values: {
            inboundReturn,
          },
        }, () => this.props.hideSpinner());
      })
      .catch(() => this.props.hideSpinner());
  }

  saveValues(values) {
    this.props.showSpinner();
    const url = `/openboxes/api/stockTransfers/${this.props.match.params.inboundReturnId}`;
    const payload = {
      ...values,
      shipmentType: {
        id: values.shipmentType.id,
      },
      trackingNumber: values.trackingNumber || '',
      driverName: values.driverName || '',
      comments: values.comments || '',
      dateShipped: values.dateShipped || '',
      expectedDeliveryDate: values.expectedDeliveryDate || '',
    };

    return apiClient.put(url, flattenRequest(payload));
  }

  previousPage(values, invalid) {
    if (!invalid) {
      this.saveValues(values)
        .then(() => this.props.previousPage(values));
    } else {
      confirmAlert({
        title: this.props.translate('react.stockMovement.confirmPreviousPage.label', 'Validation error'),
        message: this.props.translate('react.stockMovement.confirmPreviousPage.message.label', 'Cannot save due to validation error on page'),
        buttons: [
          {
            label: this.props.translate('react.stockMovement.confirmPreviousPage.correctError.label', 'Correct error'),
          },
          {
            label: this.props.translate('react.stockMovement.confirmPreviousPage.continue.label', 'Continue (lose unsaved work)'),
            onClick: () => this.props.previousPage(values),
          },
        ],
      });
    }
  }

  render() {
    const { inboundReturn } = this.state.values;

    return (
      <Form
        onSubmit={() => {}}
        validate={this.validate}
        mutators={{ ...arrayMutators }}
        initialValues={inboundReturn}
        render={({ handleSubmit, values, invalid }) => (
          <form onSubmit={handleSubmit}>
            <div className="classic-form classic-form-condensed">
              <span className="buttons-container classic-form-buttons">
                { !(values && values.status === 'COMPLETED') ?
                  <span>
                    <button
                      type="button"
                      onClick={() => this.save(values)}
                      className="btn btn-outline-secondary float-right btn-form btn-xs"
                      disabled={invalid}
                    >
                      <span><i className="fa fa-save pr-2" /><Translate id="react.default.button.save.label" defaultMessage="Save" /></span>
                    </button>
                    <button
                      type="button"
                      onClick={() => this.saveAndExit(values)}
                      className="float-right mb-1 btn btn-outline-secondary align-self-end btn-xs"
                    >
                      <span><i className="fa fa-sign-out pr-2" /><Translate id="react.default.button.saveAndExit.label" defaultMessage="Save and exit" /></span>
                    </button>
                  </span>
                    :
                  <button
                    type="button"
                    disabled={invalid}
                    onClick={() => {
                      window.location = `/openboxes/stockMovement/show/${this.props.match.params.inboundReturnId}`;
                    }}
                    className="float-right mb-1 btn btn-outline-danger align-self-end btn-xs mr-2"
                  >
                    <span><i className="fa fa-sign-out pr-2" /> <Translate id="react.default.button.exit.label" defaultMessage="Exit" /> </span>
                  </button> }
              </span>
              <div className="form-title"><Translate id="react.attribute.options.label" defaultMessage="Sending options" /></div>
              {_.map(SHIPMENT_FIELDS, (fieldConfig, fieldName) =>
                renderFormField(fieldConfig, fieldName, {
                  shipmentTypes: this.state.shipmentTypes,
                  issued: values && values.status === 'COMPLETED',
                }))}
            </div>
            <div>
              <div className="d-flex justify-content-between">
                <button
                  type="submit"
                  className="btn btn-outline-primary btn-form btn-xs mx-0"
                  disabled={values && values.status === 'COMPLETED'}
                  onClick={() => this.previousPage(values, invalid)}
                >
                  <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
                </button>
                <div className="d-flex">
                  {values.status === 'COMPLETED' && this.props.isUserAdmin &&
                  <button
                    type="button"
                    onClick={() => this.rollbackReturnOrder(values)}
                    className="btn btn-outline-success float-right btn-form btn-xs"
                  >
                    <i className="fa fa-undo pr-2" />
                    <Translate id="react.default.button.rollback.label" defaultMessage="Rollback" />
                  </button>}
                  <button
                    type="submit"
                    onClick={() => this.sendInboundReturn(values, invalid)}
                    className="btn btn-outline-success float-right btn-form btn-xs mx-0"
                    disabled={values && values.status === 'COMPLETED'}
                  ><Translate id="react.stockMovement.sendShipment.label" defaultMessage="Send shipment" />
                  </button>
                </div>
              </div>
              <div className="my-2 table-form">
                {_.map(FIELDS, (fieldConfig, fieldName) =>
                  renderFormField(fieldConfig, fieldName, {
                    translate: this.props.translate,
                    values,
                  }))}
              </div>
            </div>
          </form>
        )}
      />
    );
  }
}

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  inboundReturnsTranslationsFetched: state.session.fetchedTranslations.inboundReturns,
  minimumExpirationDate: state.session.minimumExpirationDate,
  locale: state.session.activeLanguage,
  isUserAdmin: state.session.isUserAdmin,
  currentLocationId: state.session.currentLocation.id,
});

export default connect(mapStateToProps, { showSpinner, hideSpinner })(SendMovementPage);

SendMovementPage.propTypes = {
  initialValues: PropTypes.shape({}).isRequired,
  previousPage: PropTypes.func.isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  inboundReturnsTranslationsFetched: PropTypes.bool.isRequired,
  match: PropTypes.shape({
    params: PropTypes.shape({
      inboundReturnId: PropTypes.string,
    }),
  }).isRequired,
  minimumExpirationDate: PropTypes.string.isRequired,
  locale: PropTypes.string.isRequired,
  isUserAdmin: PropTypes.bool.isRequired,
  currentLocationId: PropTypes.string.isRequired,
};
