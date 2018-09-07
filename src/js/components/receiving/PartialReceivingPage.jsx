import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import update from 'immutability-helper';
import PropTypes from 'prop-types';

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

const isReceiving = (subfield, fieldValue) => {
  if (subfield) {
    return !_.isNil(fieldValue.quantityReceiving) && fieldValue.quantityReceiving !== '';
  }

  if (!fieldValue.shipmentItems) {
    return false;
  }

  return _.every(fieldValue.shipmentItems, item => !_.isNil(item.quantityReceiving) && item.quantityReceiving !== '');
};

const isIndeterminate = (subfield, fieldValue) => {
  if (subfield) {
    return false;
  }

  if (!fieldValue.shipmentItems) {
    return false;
  }

  return _.some(fieldValue.shipmentItems, item => !_.isNil(item.quantityReceiving) && item.quantityReceiving !== '')
    && _.some(fieldValue.shipmentItems, item => _.isNil(item.quantityReceiving) || item.quantityReceiving === '');
};

const FIELDS = {
  'origin.name': {
    type: LabelField,
    label: 'Origin',
  },
  'destination.name': {
    type: LabelField,
    label: 'Destination',
  },
  dateShipped: {
    type: LabelField,
    label: 'Shipped On',
  },
  dateDelivered: {
    type: DateField,
    label: 'Delivered On',
    attributes: {
      dateFormat: 'MM/DD/YYYY',
    },
  },
  buttonsTop: {
    // eslint-disable-next-line react/prop-types
    type: ({ autofillLines, onSave }) => (
      <div className="mb-3 text-center">
        <button type="button" className="btn btn-outline-success margin-bottom-lg mr-3" onClick={() => autofillLines()}>
          Autofill quantities
        </button>
        <button type="button" className="btn btn-outline-success margin-bottom-lg" onClick={() => onSave()}>Save</button>
        <button type="submit" className="btn btn-outline-primary float-right btn-form">Next</button>
      </div>),
  },
  containers: {
    type: ArrayField,
    rowComponent: TableRowWithSubfields,
    subfieldKey: 'shipmentItems',
    fields: {
      autofillLine: {
        fieldKey: '',
        fixedWidth: '50px',
        type: ({
          // eslint-disable-next-line react/prop-types
          subfield, parentIndex, rowIndex, autofillLines, fieldPreview, fieldValue,
        }) => (
          <Checkbox
            disabled={fieldPreview}
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
      'container.name': {
        type: params => (!params.subfield ? <LabelField {...params} /> : null),
        label: 'Packaging Unit',
        attributes: {
          formatValue: value => (value || 'Unpacked'),
        },
      },
      'product.productCode': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Code',
      },
      'product.name': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Product',
        flexWidth: '18',
        attributes: {
          className: 'text-left ml-1',
          showValueTooltip: true,
        },
      },
      'inventoryItem.lotNumber': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Lot/Serial No',
      },
      'inventoryItem.expirationDate': {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Expiration Date',
        fixedWidth: '130px',
      },
      quantityShipped: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Shipped',
        fixedWidth: '75px',
        attributes: {
          formatValue: value => (value ? (value.toLocaleString('en-US')) : value),
        },
      },
      quantityReceived: {
        type: params => (params.subfield ? <LabelField {...params} /> : null),
        label: 'Received',
        fixedWidth: '75px',
        attributes: {
          formatValue: value => (value ? value.toLocaleString('en-US') : '0'),
        },
      },
      quantityReceiving: {
        type: params => (params.subfield ? <TextField {...params} /> : null),
        label: 'To Receive',
        fixedWidth: '85px',
      },
      binLocation: {
        type: params => (
          params.subfield ?
            <SelectField {...params} /> :
            <Select
              disabled={params.fieldPreview}
              options={params.bins}
              onChange={value => params.setLocation(params.rowIndex, value)}
              objectValue
            />),
        label: 'Bin Location',
        getDynamicAttr: ({ bins }) => ({
          options: bins,
        }),
        attributes: {
          objectValue: true,
        },
      },
      edit: {
        type: params => (params.subfield ? <EditLineModal {...params} /> : null),
        fieldKey: '',
        fixedWidth: '100px',
        attributes: {
          btnOpenText: 'Edit Line',
          title: 'Edit Line',
          className: 'btn btn-outline-primary',
        },
        getDynamicAttr: ({
          fieldValue, saveEditLine, parentIndex, rowIndex,
        }) => ({
          fieldValue,
          saveEditLine,
          parentIndex,
          rowIndex,
        }),
      },
      'recipient.id': {
        type: params => (params.subfield ? <SelectField {...params} /> : null),
        label: 'Recipient',
        getDynamicAttr: ({ users }) => ({
          options: users,
        }),
      },
    },
  },
  buttonsBottom: {
    // eslint-disable-next-line react/prop-types
    type: ({ autofillLines, onSave }) => (
      <div className="my-3 text-center">
        <button type="button" className="btn btn-outline-success margin-bottom-lg mr-3" onClick={() => autofillLines()}>
          Autofill quantities
        </button>
        <button type="button" className="btn btn-outline-success margin-bottom-lg" onClick={() => onSave()}>Save</button>
        <button type="submit" className="btn btn-outline-primary float-right btn-form mt-4 mb-4">Next</button>
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
    return {
      ...shipmentItem,
      quantityReceiving: clearValue ? null
        : _.toInteger(shipmentItem.quantityShipped) - _.toInteger(shipmentItem.quantityReceived),
    };
  }

  constructor(props) {
    super(props);

    this.autofillLines = this.autofillLines.bind(this);
    this.setLocation = this.setLocation.bind(this);
    this.onSave = this.onSave.bind(this);
    this.saveEditLine = this.saveEditLine.bind(this);
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
            $apply: items => (!items ? [] : items.map(item => ({
              ...item,
              binLocation: location,
            }))),
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
          }))}
      </div>
    );
  }
}

const mapStateToProps = state => ({
  usersFetched: state.users.fetched,
  users: state.users.data,
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
  /** All data in the form */
  formValues: PropTypes.shape({
    containers: PropTypes.arrayOf(PropTypes.shape({})),
  }),
  /** Array of available bin locations  */
  bins: PropTypes.arrayOf(PropTypes.shape({})),
};

PartialReceivingPage.defaultProps = {
  formValues: {},
  bins: [],
};
