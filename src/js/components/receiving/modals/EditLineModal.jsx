import React, { Component } from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import { connect } from 'react-redux';

import ModalWrapper from '../../form-elements/ModalWrapper';
import ArrayField from '../../form-elements/ArrayField';
import TextField from '../../form-elements/TextField';
import DateField from '../../form-elements/DateField';
import CheckboxField from '../../form-elements/CheckboxField';
import { showSpinner, hideSpinner } from '../../../actions';

const FIELDS = {
  lines: {
    type: ArrayField,
    disableVirtualization: true,
    // eslint-disable-next-line react/prop-types
    addButton: ({ addRow, shipmentItemId }) => (
      <button
        type="button"
        className="btn btn-outline-success margin-bottom-lg"
        onClick={() => addRow({ shipmentItem: { id: shipmentItemId } })}
      >Add line
      </button>
    ),
    getDynamicRowAttr: ({ rowValues }) => ({
      className: rowValues.remove ? 'crossed-out' : '',
    }),
    fields: {
      remove: {
        type: CheckboxField,
        label: '',
        attributes: {
          custom: true,
        },
      },
      'product.productCode': {
        type: TextField,
        label: 'Code',
        fieldKey: 'disabled',
        getDynamicAttr: ({ fieldValue }) => ({
          disabled: fieldValue,
        }),
      },
      'product.name': {
        type: TextField,
        label: 'Product',
        fieldKey: 'disabled',
        getDynamicAttr: ({ fieldValue }) => ({
          disabled: fieldValue,
        }),
      },
      'inventoryItem.lotNumber': {
        type: TextField,
        label: 'Lot',
      },
      'inventoryItem.expirationDate': {
        type: DateField,
        label: 'Expiry',
        attributes: {
          dateFormat: 'MM/DD/YYYY',
        },
      },
      quantityShipped: {
        type: TextField,
        label: 'Qty Shipped',
        attributes: {
          type: 'number',
        },
      },
    },
  },
};

function validate(values) {
  const errors = {};
  errors.lines = [];

  _.forEach(values.lines, (line, key) => {
    if (line && _.isNil(line.quantityShipped)) {
      errors.lines[key] = { quantityShipped: 'Enter quantity shipped' };
    }
  });

  return errors;
}

/**
 * Modal window where user can edit receiving's line. User can open it on the first page
 * of partial receiving if they want to change lot information.
*/
class EditLineModal extends Component {
  constructor(props) {
    super(props);
    const {
      fieldConfig: { attributes, getDynamicAttr },
    } = props;
    const dynamicAttr = getDynamicAttr ? getDynamicAttr(props) : {};
    const attr = { ...attributes, ...dynamicAttr };

    this.state = {
      attr,
      formValues: [],
    };

    this.onOpen = this.onOpen.bind(this);
    this.onSave = this.onSave.bind(this);
  }

  /**
   * Loads available items into modal's form.
   * @public
  */
  onOpen() {
    this.setState({
      formValues: {
        lines: _.map([this.state.attr.fieldValue], value => ({
          ...value,
          disabled: true,
        })),
      },
    });
  }

  /**
   * Sends all changes made by user in this modal to API and updates data.
   * @param {object} values
   * @public
   */
  onSave(values) {
    this.state.attr.saveEditLine(
      values.lines,
      this.state.attr.parentIndex,
      this.state.attr.rowIndex,
    );
  }

  render() {
    return (
      <ModalWrapper
        {...this.state.attr}
        onOpen={this.onOpen}
        onSave={this.onSave}
        validate={validate}
        initialValues={this.state.formValues}
        fields={FIELDS}
        formProps={{ shipmentItemId: this.state.attr.fieldValue.shipmentItem.id }}
      />
    );
  }
}

export default connect(null, { showSpinner, hideSpinner })(EditLineModal);

EditLineModal.propTypes = {
  /** Name of the field */
  fieldName: PropTypes.string.isRequired,
  /** Configuration of the field */
  fieldConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  /** Index  of current row */
  rowIndex: PropTypes.number.isRequired,
};
