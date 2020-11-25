import arrayMutators from 'final-form-arrays';
import update from 'immutability-helper';
import fileDownload from 'js-file-download';
import _ from 'lodash';
import PropTypes from 'prop-types';
import React, { Component } from 'react';
import { Form } from 'react-final-form';
import { connect } from 'react-redux';
import { fetchUsers, hideSpinner, showSpinner } from '../../actions';
import apiClient, { flattenRequest, parseResponse } from '../../utils/apiClient';
import Checkbox from '../../utils/Checkbox';
import { renderFormField } from '../../utils/form-utils';
import Select from '../../utils/Select';
import Translate from '../../utils/Translate';
import ArrayField from '../form-elements/ArrayField';
import DateField from '../form-elements/DateField';
import LabelField from '../form-elements/LabelField';
import SelectField from '../form-elements/SelectField';
import TableRowWithSubfields from '../form-elements/TableRowWithSubfields';
import TextField from '../form-elements/TextField';
import EditLineModal from './modals/EditLineModal';

const isReceived = (subfield, fieldValue) => {
  if (fieldValue && subfield) {
    return (_.toInteger(fieldValue.quantityReceived) + _.toInteger(fieldValue.quantityCanceled)) >=
      _.toInteger(fieldValue.quantityShipped);
  }

  if (fieldValue && !fieldValue.shipmentItems) {
    return true;
  }

  return _.every(fieldValue && fieldValue.shipmentItems, item =>
    _.toInteger(item.quantityReceived) >= _.toInteger(item.quantityShipped));
};

const isReceiving = (subfield, fieldValue) => {
  if (subfield) {
    return fieldValue && !_.isNil(fieldValue.quantityReceiving) && fieldValue.quantityReceiving !== '';
  }

  if (!fieldValue.shipmentItems) {
    return false;
  }

  return _.every(fieldValue && fieldValue.shipmentItems, item => (!_.isNil(item.quantityReceiving) && item.quantityReceiving !== '') || isReceived(true, item))
    && _.some(fieldValue && fieldValue.shipmentItems, item => !_.isNil(item.quantityReceiving) && item.quantityReceiving !== '');
};

const isIndeterminate = (subfield, fieldValue) => {
  if (subfield) {
    return false;
  }

  if (fieldValue && !fieldValue.shipmentItems) {
    return false;
  }

  return _.some(fieldValue && fieldValue.shipmentItems, item => !_.isNil(item.quantityReceiving) && item.quantityReceiving !== '')
    && _.some(fieldValue && fieldValue.shipmentItems, item => (_.isNil(item.quantityReceiving) || item.quantityReceiving === '') && !isReceived(true, item));
};

const isAnyItemSelected = (containers) => {
  if (!_.size(containers)) {
    return false;
  }

  return _.some(containers, cont => _.size(cont.shipmentItems) && _.some(cont.shipmentItems, item => !_.isNil(item.quantityReceiving) && item.quantityReceiving !== ''));
};


const SHIPMENT_FIELDS = {
  'origin.name': {
    label: 'react.partialReceiving.origin.label',
    defaultMessage: 'Origin',
    type: params => <TextField {...params} disabled />,
  },
  'destination.name': {
    label: 'react.partialReceiving.destination.label',
    defaultMessage: 'Destination',
    type: (params) => {
      if (params.canBeEdited && !params.hasStockList) {
        return null;
      }
      return <TextField {...params} disabled />;
    },
  },
  dateShipped: {
    label: 'react.partialReceiving.shippedOn.label',
    defaultMessage: 'Shipped on',
    type: params => <DateField {...params} disabled />,
  },
  dateDelivered: {
    label: 'react.partialReceiving.deliveredOn.label',
    defaultMessage: 'Delivered on',
    type: params => <DateField {...params} disabled />,
  },
};

const TABLE_FIELDS = {
  containers: {
    type: ArrayField,
    arrowsNavigation: true,
    maxTableHeight: 'none',
    rowComponent: TableRowWithSubfields,
    subfieldKey: 'shipmentItems',
    getDynamicRowAttr: ({ rowValues, subfield }) => {
      let className = '';
      if (isReceived(subfield, rowValues)) {
        className = 'text-disabled';
      }
      return { className };
    },
    fields: {
      autofillLine: {
        fieldKey: '',
        label: '',
        type: ({
          // eslint-disable-next-line react/prop-types
          subfield, parentIndex, rowIndex, autofillLines, fieldValue, shipmentReceived,
        }) => (
          <Checkbox
            disabled={shipmentReceived || isReceived(subfield, fieldValue)}
            className={subfield ? 'ml-4' : 'mr-4'}
            value={isReceiving(subfield, fieldValue)}
            indeterminate={isIndeterminate(subfield, fieldValue)}
            onChange={(value) => {
              if (subfield) {
                autofillLines(!value, parentIndex, rowIndex);
              } else {
                autofillLines(!value, rowIndex);
              }
            }}
          />),
      },
      'parentContainer.name': {
        fieldKey: '',
        type: params => (!params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.packLevel1.label',
        defaultMessage: 'Pack level 1',
        attributes: {
          formatValue: fieldValue => (_.get(fieldValue, 'parentContainer.name') || _.get(fieldValue, 'container.name') || 'Unpacked'),
        },
      },
      'container.name': {
        fieldKey: '',
        type: params => (!params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.packLevel2.label',
        defaultMessage: 'Pack level 2',
        attributes: {
          formatValue: fieldValue => (_.get(fieldValue, 'parentContainer.name') ? _.get(fieldValue, 'container.name') || '' : ''),
        },
      },
      'product.productCode': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.code.label',
        defaultMessage: 'Code',
      },
      'product.name': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.product.label',
        defaultMessage: 'Product',
        headerAlign: 'left',
        attributes: {
          className: 'text-left',
          formatValue: value => (
            <span className="d-flex">
              <span className="text-truncate">
                {value}
              </span>
            </span>
          ),
        },
      },
      lotNumber: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.lotSerialNo.label',
        defaultMessage: 'Lot',
      },
      expirationDate: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.expirationDate.label',
        defaultMessage: 'Expiry',
      },
      binLocation: {
        type: params => (
          params.subfield ?
            <SelectField {...params} /> :
            <Select
              disabled={!params.hasBinLocationSupport ||
              params.shipmentReceived || isReceived(false, params.fieldValue)}
              options={params.bins}
              onChange={value => params.setLocation(params.rowIndex, value)}
              objectValue
              className="select-xs"
            />),
        fieldKey: '',
        label: 'react.partialReceiving.binLocation.label',
        defaultMessage: 'Bin Location',
        getDynamicAttr: ({
          bins, hasBinLocationSupport, shipmentReceived, fieldValue,
        }) => ({
          options: bins,
          disabled: !hasBinLocationSupport || shipmentReceived || isReceived(true, fieldValue),
          hide: !hasBinLocationSupport,
        }),
        attributes: {
          objectValue: true,
        },
      },
      'recipient.id': {
        type: params => (params.subfield ? <SelectField {...params} /> : null),
        fieldKey: '',
        label: 'react.partialReceiving.recipient.label',
        defaultMessage: 'Recipient',
        getDynamicAttr: ({ users, shipmentReceived, fieldValue }) => ({
          options: users,
          disabled: shipmentReceived || isReceived(true, fieldValue),
        }),
      },
      quantityShipped: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.shipped.label',
        defaultMessage: 'Shipped',
        attributes: {
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
        },
      },
      quantityReceived: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.received.label',
        defaultMessage: 'Received',
        attributes: {
          formatValue: value => (value ? value.toLocaleString('en-US') : '0'),
        },
      },
      quantityRemaining: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.toReceive.label',
        defaultMessage: 'To receive',
        fieldKey: '',
        getDynamicAttr: ({ fieldValue, shipmentReceived }) => ({
          className: _.toInteger(fieldValue &&
            fieldValue.quantityRemaining) < 0 && !shipmentReceived
            && !isReceived(true, fieldValue) ? 'text-danger' : '',
          formatValue: (val) => {
            if (!val.quantityRemaining) {
              return val.quantityRemaining;
            }

            if (_.toInteger(fieldValue && fieldValue.quantityRemaining) < 0
              && (shipmentReceived || isReceived(true, fieldValue))) {
              return '0';
            }

            return val.quantityRemaining.toLocaleString('en-US');
          },
        }),
      },
      quantityReceiving: {
        type: params => (params.subfield ? <TextField {...params} /> : null),
        fieldKey: '',
        label: 'react.partialReceiving.receivingNow.label',
        defaultMessage: 'Receiving now',
        attributes: {
          autoComplete: 'off',
        },
        getDynamicAttr: ({ shipmentReceived, fieldValue }) => ({
          disabled: shipmentReceived || isReceived(true, fieldValue),
        }),
      },
      edit: {
        type: params => (params.subfield ? <EditLineModal {...params} /> : null),
        fieldKey: '',
        label: '',
        attributes: {
          btnOpenText: 'react.default.button.edit.label',
          btnOpenDefaultText: 'Edit line',
          title: 'react.default.button.edit.label',
          className: 'btn btn-outline-primary',
          defaultTitleMessage: 'Edit',
        },
        getDynamicAttr: ({
          fieldValue, saveEditLine, parentIndex, rowIndex, shipmentReceived,
        }) => ({
          fieldValue,
          saveEditLine,
          parentIndex,
          rowIndex,
          btnOpenDisabled: shipmentReceived || isReceived(true, fieldValue),
        }),
      },
      comment: {
        type: params => (params.subfield ? <TextField {...params} /> : null),
        fieldKey: '',
        label: 'react.partialReceiving.comment.label',
        defaultMessage: 'Comment',
        attributes: {
          autoComplete: 'off',
        },
      },
    },
  },
};

function validate(values) {
  const errors = {};
  errors.containers = [];

  if (!values.dateDelivered) {
    errors.dateDelivered = 'react.default.error.requiredField.label';
  }
  _.forEach(values.containers, (container, key) => {
    errors.containers[key] = { shipmentItems: [] };
    _.forEach(container.shipmentItems, (item, key2) => {
      if (item.quantityReceiving < 0) {
        errors.containers[key].shipmentItems[key2] = { quantityReceiving: 'react.partialReceiving.error.quantityToReceiveNegative.label' };
      }
    });
  });

  return errors;
}

/** The first page of partial receiving where user can see receipt lines and complete it in
 * different ways depending on how they receive it.
 * - If the user is receiving everything with no changes, they click "autofill all quantities"
 * button what will automatically fill all of the "to receive" cells with quantity left in the line.
 * - If the user is receiving by pallet with no changes, they click the checkbox next to the pallet
 * they want to receive what will automatically fill "to receive" column for lines
 * in that pallet with full quantity.
 * - If the user is receiving by line with no lot changes, they go line by line and type in the
 * quantity from each line they want to receive.
 * - If the user has to change lot information, they click the edit line button which allows them
 * to edit the line.
 */
class PartialReceivingPage extends Component {
  static autofillLine(clearValue, shipmentItem) {
    if (isReceived(true, shipmentItem)) {
      return shipmentItem;
    }
    const autofillQuantity = _.toInteger(shipmentItem.quantityShipped) -
          _.toInteger(shipmentItem.quantityReceived);

    return {
      ...shipmentItem,
      quantityReceiving: clearValue || autofillQuantity < 0 ? null : autofillQuantity,
    };
  }

  constructor(props) {
    super(props);

    this.state = {
      values: { ...this.props.initialValues },
    };
    this.autofillLines = this.autofillLines.bind(this);
    this.setLocation = this.setLocation.bind(this);
    this.save = this.save.bind(this);
    this.saveAndExit = this.saveAndExit.bind(this);
    this.saveValues = this.saveValues.bind(this);
    this.onSave = this.onSave.bind(this);
    this.onExit = this.onExit.bind(this);
    this.saveEditLine = this.saveEditLine.bind(this);
    this.exportTemplate = this.exportTemplate.bind(this);
    this.importTemplate = this.importTemplate.bind(this);
  }

  componentDidMount() {
    if (this.props.partialReceivingTranslationsFetched && !this.props.usersFetched) {
      this.dataFetched = true;
      this.props.fetchUsers();
    }
    this.props.hideSpinner();
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.partialReceivingTranslationsFetched && !this.dataFetched
      && !this.props.usersFetched) {
      this.dataFetched = true;

      this.props.fetchUsers();
    }
  }

  onSave() {
    this.save(this.state.values);
  }

  /**
   * Calls save and exit method.
   * @public
   */
  onExit() {
    this.saveAndExit(this.state.values);
  }

  onSubmit(formValues) {
    const containers = _.map(formValues.containers, container => ({
      ...container,
      shipmentItems: _.chain(container.shipmentItems)
        .map((item) => {
          if (item.receiptItemId) {
            return {
              ...item, quantityReceiving: item.quantityReceiving ? item.quantityReceiving : 0,
            };
          }

          return item;
        })
        .filter(item => !_.isNil(item.quantityReceiving) && item.quantityReceiving !== '').value(),
    }));
    const { values } = this.state;
    values.containers = containers;
    this.setState({
      values,
    });

    this.nextPage(values);
  }

  /**
   * Updates items with a location of the bin.
   * @public
   */
  setLocation(rowIndex, location) {
    if (this.props.initialValues.containers && !_.isNil(rowIndex)) {
      const containers = update(this.props.initialValues.containers, {
        [rowIndex]: {
          shipmentItems: {
            $apply: items => (!items ? [] : items.map((item) => {
              if (isReceived(true, item)) {
                return item;
              }

              return { ...item, binLocation: location };
            })),
          },
        },
      });

      window.setFormValue('containers', containers);
    }
  }

  saveValues(formValues) {
    this.props.showSpinner();
    const url = `/openboxes/api/partialReceiving/${this.props.match.params.shipmentId}?stepNumber=1`;

    const payload = {
      ...formValues,
      containers: _.map(formValues.containers, container => ({
        ...container,
        shipmentItems: _.map(container.shipmentItems, (item) => {
          if (!_.get(item, 'recipient.id')) {
            return {
              ...item, recipient: '',
            };
          }

          return item;
        }),
      })),
    };

    return apiClient.post(url, flattenRequest(payload));
  }

  saveAndExit(formValues) {
    this.saveValues(formValues)
      .then(() => {
        const { requisition, shipmentId } = formValues;
        window.location = `/openboxes/stockMovement/show/${requisition || shipmentId}`;
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Calls save method.
   * @public
   */
  save(formValues, callback) {
    this.saveValues(formValues)
      .then((response) => {
        this.props.hideSpinner();

        this.setState({ values: {} }, () =>
          this.setState({ values: parseResponse(response.data.data) }));
        if (callback) {
          callback();
        }
      })
      .catch(() => this.props.hideSpinner());
  }

  dataFetched = false;

  /**
   * Autofills "to receive" cells in different ways depending on what user did.
   * If they click "Autofill quantites" button, it will automatically fill all lines.
   * If they click checkbox next to the pallet, it will automatically fill all lines in that pallet.
   * If they click checbox next to the line, it will automatically fill this line.
   * @public
   */
  autofillLines(clearValue, parentIndex, rowIndex) {
    if (this.state.values.containers) {
      let containers = [];

      if (_.isNil(parentIndex)) {
        containers = update(this.state.values.containers, {
          $apply: items => (!items ? [] : items.map(item => update(item, {
            shipmentItems: {
              $apply: shipmentItems => (!shipmentItems ? [] : shipmentItems.map(shipmentItem =>
                PartialReceivingPage.autofillLine(clearValue, shipmentItem))),
            },
          }))),
        });
      } else if (_.isNil(rowIndex)) {
        containers = update(this.state.values.containers, {
          [parentIndex]: {
            shipmentItems: {
              $apply: items => (!items ? [] : items.map(item =>
                PartialReceivingPage.autofillLine(clearValue, item))),
            },
          },
        });
      } else {
        containers = update(this.state.values.containers, {
          [parentIndex]: {
            shipmentItems: {
              [rowIndex]: {
                $apply: item => PartialReceivingPage.autofillLine(clearValue, item),
              },
            },
          },
        });
      }
      const { values } = this.state;
      values.containers = containers;
      this.setState({ values });
      window.setFormValue('containers', containers);
    }
  }

  /**
   * Saves changes made in edit line modal and updates data.
   * @param {object} editLines
   * @param {number} rowIndex
   * @param {number} parentIndex
   * @public
   */
  saveEditLine(editLines, parentIndex, rowIndex) {
    const formValues = update(this.state.values, {
      containers: {
        [parentIndex]: {
          shipmentItems: {
            $splice: [[rowIndex, 1, ...editLines]],
          },
        },
      },
    });
    this.save(formValues);
  }

  exportTemplate() {
    this.props.showSpinner();

    const { shipmentId } = this.state.values;
    const url = `/openboxes/api/partialReceiving/exportCsv/${shipmentId}`;

    apiClient.post(url, flattenRequest(this.state.values))
      .then((response) => {
        fileDownload(response.data, `PartialReceiving${shipmentId ? `-${shipmentId}` : ''}.csv`, 'text/csv');
        this.props.hideSpinner();
      })
      .catch(() => this.props.hideSpinner());
  }

  importTemplate(event) {
    this.props.showSpinner();
    const formData = new FormData();
    const file = event.target.files[0];

    formData.append('importFile', file.slice(0, file.size, 'text/csv'));
    const config = {
      headers: {
        'content-type': 'multipart/form-data',
      },
    };

    const url = `/openboxes/api/partialReceiving/importCsv/${this.state.values.shipmentId}`;

    return apiClient.post(url, formData, config)
      .then(() => {
        this.props.hideSpinner();
        window.location.reload();
      })
      .catch(() => {
        this.props.hideSpinner();
      });
  }

  transitionToNextStep() {
    const url = `/openboxes/api/partialReceiving/${this.props.match.params.shipmentId}?stepNumber=1`;
    const payload = {
      receiptStatus: 'CHECKING',
    };

    return apiClient.post(url, payload);
  }

  nextPage(formValues) {
    this.props.showSpinner();
    this.transitionToNextStep()
      .then(() => this.props.nextPage(formValues))
      .then(() => this.props.hideSpinner())
      .catch(() => this.props.hideSpinner());
  }

  render() {
    return (
      <div>
        <Form
          onSubmit={values => this.onSubmit(values)}
          validate={validate}
          mutators={{
            ...arrayMutators,
            setValue: ([field, value], state, { changeValue }) => {
              changeValue(state, field, () => value);
            },
          }}
          initialValues={this.state.values}
          render={({ handleSubmit, values, form }) => {
            if (!window.setFormValue) {
              window.setFormValue = form.mutators.setValue;
            }
            return (
              <form onSubmit={handleSubmit}>
                <div className="classic-form classic-form-condensed">
                  <span className="buttons-container classic-form-buttons">
                    <button type="button" className="btn btn-outline-secondary float-right btn-form btn-xs" disabled={this.state.values.shipmentStatus === 'RECEIVED'} onClick={() => this.autofillLines()}>
                      <Translate id="react.partialReceiving.autofillQuantities.label" defaultMessage="Autofill quantities" />
                    </button>
                    <button type="button" className="btn btn-outline-secondary float-right btn-form btn-xs" disabled={!isAnyItemSelected(values.containers) || values.shipmentStatus === 'RECEIVED'} onClick={() => this.saveAndExit(this.state.values)}>
                      <span><i className="fa fa-sign-out pr-2" /><Translate id="react.default.button.saveAndExit.label" defaultMessage="Save and exit" /></span>
                    </button>
                    <button type="button" className="btn btn-outline-secondary float-right btn-form btn-xs" disabled={!isAnyItemSelected(values.containers) || values.shipmentStatus === 'RECEIVED'} onClick={() => this.onSave(this.state.values)}>
                      <Translate id="react.default.button.save.label" defaultMessage="Save" />
                    </button>
                    <button
                      type="button"
                      className="btn btn-outline-secondary btn-xs mr-3"
                      onClick={() => this.exportTemplate()}
                    >
                      <span><i className="fa fa-upload pr-2" />
                        <Translate id="react.default.button.exportTemplate.label" defaultMessage="Export template" />
                      </span>
                    </button>
                    <label
                      htmlFor="csvInput"
                      className="btn btn-outline-secondary btn-xs mr-3"
                    >
                      <span><i className="fa fa-download pr-2" />
                        <Translate id="react.default.button.importTemplate.label" defaultMessage="Import template" />
                      </span>
                      <input
                        id="csvInput"
                        type="file"
                        style={{ display: 'none' }}
                        onChange={this.importTemplate}
                        onClick={(event) => {
                      // eslint-disable-next-line no-param-reassign
                      event.target.value = null;
                      }}
                        accept=".csv"
                      />
                    </label>
                  </span>
                  <div className="form-title">Shipment Informations</div>
                  {_.map(SHIPMENT_FIELDS, (fieldConfig, fieldName) =>
                  renderFormField(fieldConfig, fieldName, {
                    bins: this.props.bins,
                    users: this.props.users,
                    hasBinLocationSupport: this.props.hasBinLocationSupport,
                    locationId: this.props.locationId,
                    shipmentReceived: this.state.values.shipmentStatus === 'RECEIVED',
                  }))}
                </div>
                <div className="my-2 table-form">
                  {_.map(TABLE_FIELDS, (fieldConfig, fieldName) =>
                  renderFormField(fieldConfig, fieldName, {
                    bins: this.props.bins,
                    users: this.props.users,
                    hasBinLocationSupport: this.props.hasBinLocationSupport,
                    locationId: this.props.locationId,
                    shipmentReceived: this.state.values.shipmentStatus === 'RECEIVED',
                  }))}
                </div>
                <div className="submit-buttons">
                  <button type="submit" className="btn btn-outline-primary btn-form float-right btn-xs" disabled={!isAnyItemSelected(values.containers) || values.shipmentStatus === 'RECEIVED'} onClick={() => this.onExit()}>
                    <Translate id="react.default.button.next.label" defaultMessage="Next" />
                  </button>
                </div>
              </form>
            );
          }}
        />
      </div>
    );
  }
}

const mapStateToProps = state => ({
  usersFetched: state.users.fetched,
  users: state.users.data,
  hasBinLocationSupport: state.session.currentLocation.hasBinLocationSupport,
  partialReceivingTranslationsFetched: state.session.fetchedTranslations.partialReceiving,
});

export default connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchUsers,
})(PartialReceivingPage);

PartialReceivingPage.propTypes = {
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** Function fetching users */
  fetchUsers: PropTypes.func.isRequired,
  /** Indicator if users' data is fetched */
  usersFetched: PropTypes.bool.isRequired,
  /** Array of available users  */
  users: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  /** Is true when currently selected location supports bins */
  hasBinLocationSupport: PropTypes.bool.isRequired,
  /** All data in the form */
  initialValues: PropTypes.shape({
    containers: PropTypes.arrayOf(PropTypes.shape({})),
    shipmentStatus: PropTypes.string,
    shipmentId: PropTypes.string,
  }).isRequired,
  /** Array of available bin locations  */
  bins: PropTypes.arrayOf(PropTypes.shape({})),
  /** Location ID (destination). Needs to be used in /api/products request. */
  locationId: PropTypes.string.isRequired,
  partialReceivingTranslationsFetched: PropTypes.bool.isRequired,
  match: PropTypes.shape({
    params: PropTypes.shape({
      shipmentId: PropTypes.string,
    }),
  }).isRequired,
  nextPage: PropTypes.func.isRequired,
};

PartialReceivingPage.defaultProps = {
  bins: [],
  match: {},
};
