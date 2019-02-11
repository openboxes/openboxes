import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import update from 'immutability-helper';
import PropTypes from 'prop-types';
import fileDownload from 'js-file-download';

import TextField from '../form-elements/TextField';
import SelectField from '../form-elements/SelectField';
import ArrayField from '../form-elements/ArrayField';
import LabelField from '../form-elements/LabelField';
import DateField from '../form-elements/DateField';
import TableRowWithSubfields from '../form-elements/TableRowWithSubfields';
import { renderFormField } from '../../utils/form-utils';
import Select from '../../utils/Select';
import Checkbox from '../../utils/Checkbox';
import { showSpinner, hideSpinner, fetchUsers } from '../../actions';
import EditLineModal from './modals/EditLineModal';
import Translate from '../../utils/Translate';
import apiClient, { flattenRequest } from '../../utils/apiClient';

const isReceived = (subfield, fieldValue) => {
  if (subfield) {
    return (_.toInteger(fieldValue.quantityReceived) + _.toInteger(fieldValue.quantityCanceled)) >=
      _.toInteger(fieldValue.quantityShipped);
  }

  if (!fieldValue.shipmentItems) {
    return true;
  }

  return _.every(fieldValue.shipmentItems, item =>
    _.toInteger(item.quantityReceived) >= _.toInteger(item.quantityShipped));
};

const isReceiving = (subfield, fieldValue) => {
  if (subfield) {
    return !_.isNil(fieldValue.quantityReceiving) && fieldValue.quantityReceiving !== '';
  }

  if (!fieldValue.shipmentItems) {
    return false;
  }

  return _.every(fieldValue.shipmentItems, item => (!_.isNil(item.quantityReceiving) && item.quantityReceiving !== '') || isReceived(true, item))
    && _.some(fieldValue.shipmentItems, item => !_.isNil(item.quantityReceiving) && item.quantityReceiving !== '');
};

const isIndeterminate = (subfield, fieldValue) => {
  if (subfield) {
    return false;
  }

  if (!fieldValue.shipmentItems) {
    return false;
  }

  return _.some(fieldValue.shipmentItems, item => !_.isNil(item.quantityReceiving) && item.quantityReceiving !== '')
    && _.some(fieldValue.shipmentItems, item => (_.isNil(item.quantityReceiving) || item.quantityReceiving === '') && !isReceived(true, item));
};

const isAnyItemSelected = (containers) => {
  if (!_.size(containers)) {
    return false;
  }

  return _.some(containers, cont => _.size(cont.shipmentItems) && _.some(cont.shipmentItems, item => !_.isNil(item.quantityReceiving) && item.quantityReceiving !== ''));
};

const FIELDS = {
  'origin.name': {
    type: LabelField,
    label: 'stockMovement.origin.label',
    defaultMessage: 'Origin',
  },
  'destination.name': {
    type: LabelField,
    label: 'stockMovement.destination.label',
    defaultMessage: 'Destination',
  },
  dateShipped: {
    type: LabelField,
    label: 'partialReceiving.shippedOn.label',
    defaultMessage: 'Shipped on',
  },
  dateDelivered: {
    type: DateField,
    label: 'partialReceiving.deliveredOn.label',
    defaultMessage: 'Delivered on',
    attributes: {
      showTimeSelect: true,
      dateFormat: 'MM/DD/YYYY HH:mm Z',
      autoComplete: 'off',
    },
    getDynamicAttr: ({ shipmentReceived }) => ({
      disabled: shipmentReceived,
    }),
  },
  buttonsTop: {
    type: ({
      // eslint-disable-next-line max-len, react/prop-types
      autofillLines, onSave, saveDisabled, shipmentReceived, exportTemplate, importTemplate,
    }) => (
      <div className="mb-1 text-center">
        <button type="button" className="btn btn-outline-success mr-3 btn-xs" disabled={shipmentReceived} onClick={() => autofillLines()}>
          <Translate id="partialReceiving.autofillQuantities.label" defaultMessage="Autofill quantities" />
        </button>
        <button type="button" className="btn btn-outline-success btn-xs mr-3" disabled={saveDisabled || shipmentReceived} onClick={() => onSave()}>
          <Translate id="default.button.save.label" defaultMessage="Save" />
        </button>
        <button
          type="button"
          className="btn btn-outline-secondary btn-xs mr-3"
          onClick={() => exportTemplate()}
        >
          <span><i className="fa fa-upload pr-2" />
            <Translate id="default.button.exportTemplate.label" defaultMessage="Export template" />
          </span>
        </button>
        <label
          htmlFor="csvInput"
          className="btn btn-outline-secondary btn-xs mt-2"
        >
          <span><i className="fa fa-download pr-2" />
            <Translate id="default.button.importTemplate.label" defaultMessage="Import template" />
          </span>
          <input
            id="csvInput"
            type="file"
            style={{ display: 'none' }}
            onChange={importTemplate}
            onClick={(event) => {
              // eslint-disable-next-line no-param-reassign
              event.target.value = null;
            }}
            accept=".csv"
          />
        </label>
        <button type="submit" className="btn btn-outline-primary float-right btn-form btn-xs" disabled={saveDisabled || shipmentReceived}>
          <Translate id="default.button.next.label" defaultMessage="Next" />
        </button>
      </div>),
  },
  containers: {
    type: ArrayField,
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
        flexWidth: '0',
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
        label: 'stockMovement.pallet.label',
        defaultMessage: 'Pallet',
        flexWidth: '0.8',
        attributes: {
          formatValue: fieldValue => (_.get(fieldValue, 'parentContainer.name') || _.get(fieldValue, 'container.name') || 'Unpacked'),
        },
      },
      'container.name': {
        fieldKey: '',
        type: params => (!params.subfield ? <LabelField {...params} /> : null),
        label: 'stockMovement.box.label',
        defaultMessage: 'Box',
        flexWidth: '0.8',
        attributes: {
          formatValue: fieldValue => (_.get(fieldValue, 'parentContainer.name') ? _.get(fieldValue, 'container.name') || '' : ''),
        },
      },
      'product.productCode': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'stockMovement.code.label',
        defaultMessage: 'Code',
        flexWidth: '0.8',
      },
      'product.name': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'stockMovement.name.label',
        defaultMessage: 'Name',
        flexWidth: '3.3',
        attributes: {
          className: 'text-left ml-1',
          showValueTooltip: true,
        },
      },
      lotNumber: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'stockMovement.lotSerialNo.label',
        defaultMessage: 'Lot/Serial No.',
        flexWidth: '1',
      },
      expirationDate: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'stockMovement.expirationDate.label',
        defaultMessage: 'Expiration date',
        flexWidth: '1.5',
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
        flexWidth: '1.7',
        label: 'stockMovement.binLocation.label',
        defaultMessage: 'Bin Location',
        getDynamicAttr: ({
          bins, hasBinLocationSupport, shipmentReceived, fieldValue,
        }) => ({
          options: bins,
          disabled: !hasBinLocationSupport || shipmentReceived || isReceived(true, fieldValue),
        }),
        attributes: {
          objectValue: true,
        },
      },
      'recipient.id': {
        type: params => (params.subfield ? <SelectField {...params} /> : null),
        fieldKey: '',
        flexWidth: '1.5',
        label: 'stockMovement.recipient.label',
        defaultMessage: 'Recipient',
        getDynamicAttr: ({ users, shipmentReceived, fieldValue }) => ({
          options: users,
          disabled: shipmentReceived || isReceived(true, fieldValue),
        }),
      },
      quantityShipped: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'partialReceiving.shipped.label',
        defaultMessage: 'Shipped',
        flexWidth: '0.8',
        attributes: {
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
        },
      },
      quantityReceived: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'partialReceiving.received.label',
        defaultMessage: 'Received',
        flexWidth: '0.8',
        attributes: {
          formatValue: value => (value ? value.toLocaleString('en-US') : '0'),
        },
      },
      quantityRemaining: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'partialReceiving.toReceive.label',
        defaultMessage: 'To receive',
        flexWidth: '0.8',
        fieldKey: '',
        getDynamicAttr: ({ fieldValue, shipmentReceived }) => ({
          className: _.toInteger(fieldValue.quantityRemaining) < 0 && !shipmentReceived
            && !isReceived(true, fieldValue) ? 'text-danger' : '',
          formatValue: (val) => {
            if (!val.quantityRemaining) {
              return val.quantityRemaining;
            }

            if (_.toInteger(fieldValue.quantityRemaining) < 0
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
        label: 'partialReceiving.receivingNow.label',
        defaultMessage: 'Receiving now',
        flexWidth: '1',
        getDynamicAttr: ({ shipmentReceived, fieldValue }) => ({
          disabled: shipmentReceived || isReceived(true, fieldValue),
        }),
      },
      edit: {
        type: params => (params.subfield ? <EditLineModal {...params} /> : null),
        fieldKey: '',
        label: '',
        flexWidth: '1',
        attributes: {
          btnOpenText: 'partialReceiving.editLine.label',
          title: 'partialReceiving.editLine.label',
          className: 'btn btn-outline-primary',
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
        label: 'partialReceiving.comment.label',
        defaultMessage: 'Comment',
        flexWidth: '1.3',
      },
    },
  },
  buttonsBottom: {
    type: ({
      // eslint-disable-next-line react/prop-types
      autofillLines, onSave, saveDisabled, shipmentReceived, exportTemplate, importTemplate,
    }) => (
      <div className="my-1 text-center">
        <button type="button" className="btn btn-outline-success mr-3 btn-xs" disabled={shipmentReceived} onClick={() => autofillLines()}>
          <Translate id="partialReceiving.autofillQuantities.label" defaultMessage="Autofill quantities" />
        </button>
        <button type="button" className="btn btn-outline-success btn-xs mr-3" disabled={saveDisabled || shipmentReceived} onClick={() => onSave()}>
          <Translate id="default.button.save.label" defaultMessage="Save" />
        </button>
        <button
          type="button"
          className="btn btn-outline-secondary btn-xs mr-3"
          onClick={() => exportTemplate()}
        >
          <span><i className="fa fa-upload pr-2" />
            <Translate id="default.button.exportTemplate.label" defaultMessage="Export template" />
          </span>
        </button>
        <label
          htmlFor="csvInput"
          className="btn btn-outline-secondary btn-xs mt-2"
        >
          <span><i className="fa fa-download pr-2" />
            <Translate id="default.button.importTemplate.label" defaultMessage="Import template" />
          </span>
          <input
            id="csvInput"
            type="file"
            style={{ display: 'none' }}
            onChange={importTemplate}
            onClick={(event) => {
              // eslint-disable-next-line no-param-reassign
              event.target.value = null;
            }}
            accept=".csv"
          />
        </label>
        <button type="submit" className="btn btn-outline-primary float-right btn-form btn-xs" disabled={saveDisabled || shipmentReceived}>
          <Translate id="default.button.next.label" defaultMessage="Next" />
        </button>
      </div>),
  },
};

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

    this.autofillLines = this.autofillLines.bind(this);
    this.setLocation = this.setLocation.bind(this);
    this.onSave = this.onSave.bind(this);
    this.saveEditLine = this.saveEditLine.bind(this);
    this.exportTemplate = this.exportTemplate.bind(this);
    this.importTemplate = this.importTemplate.bind(this);
  }

  componentDidMount() {
    if (!this.props.usersFetched) {
      this.fetchData(this.props.fetchUsers);
    }
  }

  /**
   * Calls save method.
   * @public
   */
  onSave() {
    this.props.save(this.props.formValues);
  }

  /**
   * Updates items with a location of the bin.
   * @public
   */
  setLocation(rowIndex, location) {
    if (this.props.formValues.containers && !_.isNil(rowIndex)) {
      const containers = update(this.props.formValues.containers, {
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

      this.props.change('containers', containers);
    }
  }

  /**
   * Autofills "to receive" cells in different ways depending on what user did.
   * If they click "Autofill quantites" button, it will automatically fill all lines.
   * If they click checkbox next to the pallet, it will automatically fill all lines in that pallet.
   * If they click checbox next to the line, it will automatically fill this line.
   * @public
   */
  autofillLines(clearValue, parentIndex, rowIndex) {
    if (this.props.formValues.containers) {
      let containers = [];

      if (_.isNil(parentIndex)) {
        containers = update(this.props.formValues.containers, {
          $apply: items => (!items ? [] : items.map(item => update(item, {
            shipmentItems: {
              $apply: shipmentItems => (!shipmentItems ? [] : shipmentItems.map(shipmentItem =>
                PartialReceivingPage.autofillLine(clearValue, shipmentItem))),
            },
          }))),
        });
      } else if (_.isNil(rowIndex)) {
        containers = update(this.props.formValues.containers, {
          [parentIndex]: {
            shipmentItems: {
              $apply: items => (!items ? [] : items.map(item =>
                PartialReceivingPage.autofillLine(clearValue, item))),
            },
          },
        });
      } else {
        containers = update(this.props.formValues.containers, {
          [parentIndex]: {
            shipmentItems: {
              [rowIndex]: {
                $apply: item => PartialReceivingPage.autofillLine(clearValue, item),
              },
            },
          },
        });
      }

      this.props.change('containers', containers);
    }
  }

  /**
   * Fetches data using function given as an argument.
   * @param {function} fetchFunction
   * @public
   */
  fetchData(fetchFunction) {
    this.props.showSpinner();
    fetchFunction()
      .then(() => this.props.hideSpinner())
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Saves changes made in edit line modal and updates data.
   * @param {object} editLines
   * @param {number} rowIndex
   * @param {number} parentIndex
   * @public
   */
  saveEditLine(editLines, parentIndex, rowIndex) {
    const formValues = update(this.props.formValues, {
      containers: {
        [parentIndex]: {
          shipmentItems: {
            $splice: [[rowIndex, 1, ...editLines]],
          },
        },
      },
    });
    this.props.save(formValues);
  }

  exportTemplate() {
    this.props.showSpinner();

    const { shipmentId } = this.props.formValues;
    const url = `/openboxes/api/partialReceiving/exportCsv/${shipmentId}`;

    apiClient.post(url, flattenRequest(this.props.formValues))
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

    const url = `/openboxes/api/partialReceiving/importCsv/${this.props.formValues.shipmentId}`;

    return apiClient.post(url, formData, config)
      .then(() => {
        this.props.hideSpinner();
        window.location.reload();
      })
      .catch(() => {
        this.props.hideSpinner();
      });
  }

  render() {
    return (
      <div>
        {_.map(FIELDS, (fieldConfig, fieldName) =>
          renderFormField(fieldConfig, fieldName, {
            autofillLines: this.autofillLines,
            setLocation: this.setLocation,
            onSave: this.onSave,
            saveEditLine: this.saveEditLine,
            bins: this.props.bins,
            users: this.props.users,
            hasBinLocationSupport: this.props.hasBinLocationSupport,
            locationId: this.props.locationId,
            saveDisabled: !isAnyItemSelected(this.props.formValues.containers),
            shipmentReceived: this.props.formValues.shipmentStatus === 'RECEIVED',
            exportTemplate: this.exportTemplate,
            importTemplate: this.importTemplate,
          }))}
      </div>
    );
  }
}

const mapStateToProps = state => ({
  usersFetched: state.users.fetched,
  users: state.users.data,
  hasBinLocationSupport: state.session.currentLocation.hasBinLocationSupport,
});

export default connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchUsers,
})(PartialReceivingPage);

PartialReceivingPage.propTypes = {
  /** Function changing the value of a field in the Redux store */
  change: PropTypes.func.isRequired,
  /** Function sending all changes mage by user to API and updating data */
  save: PropTypes.func.isRequired,
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
  formValues: PropTypes.shape({
    containers: PropTypes.arrayOf(PropTypes.shape({})),
    shipmentStatus: PropTypes.string,
    shipmentId: PropTypes.string,
  }),
  /** Array of available bin locations  */
  bins: PropTypes.arrayOf(PropTypes.shape({})),
  /** Location ID (destination). Needs to be used in /api/products request. */
  locationId: PropTypes.string.isRequired,
};

PartialReceivingPage.defaultProps = {
  formValues: {},
  bins: [],
};
