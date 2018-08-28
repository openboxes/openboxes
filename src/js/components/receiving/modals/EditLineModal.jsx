import React, { Component } from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import { reduxForm, change } from 'redux-form';
import { connect } from 'react-redux';

import ModalWrapper from '../../form-elements/ModalWrapper';
import ArrayField from '../../form-elements/ArrayField';
import TextField from '../../form-elements/TextField';
import DateField from '../../form-elements/DateField';
import CheckboxField from '../../form-elements/CheckboxField';
import ValueSelectorField from '../../form-elements/ValueSelectorField';
import { renderFormField } from '../../../utils/form-utils';
import { showSpinner, hideSpinner } from '../../../actions';

const FIELDS = {
  lines: {
    type: ValueSelectorField,
    attributes: {
      formName: 'edit-line-form',
    },
    getDynamicAttr: ({ rowIndex }) => ({
      field: `lines[${rowIndex}].removed`,
    }),
    component: ArrayField,
    componentConfig: {
      disableVirtualization: true,
      addButton: 'Add line',
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
        'inventoryItem.product.productCode': {
          type: TextField,
          label: 'Code',
          fieldKey: 'disabled',
          getDynamicAttr: ({ fieldValue }) => ({
            disabled: fieldValue,
          }),
        },
        'inventoryItem.product.name': {
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
        quantity: {
          type: TextField,
          label: 'Qty Shipped',
          attributes: {
            type: 'number',
          },
        },
      },
    },
  },
};

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
    };

    this.onOpen = this.onOpen.bind(this);
    this.onSave = this.onSave.bind(this);
  }

  /**
   * Load available items into modal's form
   * @public
  */
  onOpen() {
    if (this.state.attr.fieldValue) {
      this.props.change('edit-line-form', 'lines', _.map([this.state.attr.fieldValue], value => ({
        ...value,
        disabled: true,
      })));
    } else {
      this.props.change('edit-line-form', 'lines', [{}]);
    }
  }

  /**
  * Send all changes made by user in this modal to API and update data
  * @param {object} values
  * @public
  */
  /* eslint-disable-next-line */
  onSave(values) {}

  render() {
    return (
      <ModalWrapper
        {...this.state.attr}
        onOpen={this.onOpen}
        onSave={this.props.handleSubmit(values => this.onSave(values))}
        btnSaveDisabled={this.props.invalid}
      >
        <form value={this.state.attr.productCode}>
          {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName))}
        </form>
      </ModalWrapper>
    );
  }
}

function validate(values) {
  const errors = {};
  errors.lines = [];

  _.forEach(values.lines, (line, key) => {
    if (line && _.isNil(line.quantity)) {
      errors.lines[key] = { quantity: 'Enter quantity shipped' };
    }
  });

  return errors;
}

export default reduxForm({
  form: 'edit-line-form',
  validate,
})(connect(null, { change, showSpinner, hideSpinner })(EditLineModal));

EditLineModal.propTypes = {
  /** removed in finalform */
  invalid: PropTypes.bool.isRequired,
  /** removed in finalform */
  change: PropTypes.func.isRequired,
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
  /** removed in finalform */
  handleSubmit: PropTypes.func.isRequired,
};
