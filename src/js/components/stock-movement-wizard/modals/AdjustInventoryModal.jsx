import React, { Component } from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { change, reduxForm } from 'redux-form';
import { connect } from 'react-redux';

import ModalWrapper from '../../form-elements/ModalWrapper';
import TextField from '../../form-elements/TextField';
import ArrayField from '../../form-elements/ArrayField';
import { renderFormField } from '../../../utils/form-utils';
import DateField from '../../form-elements/DateField';
import { showSpinner, hideSpinner } from '../../../actions';

const FIELDS = {
  adjustInventory: {
    addButton: 'Add new lot number',
    type: ArrayField,
    disableVirtualization: true,
    fields: {
      lotNumber: {
        type: TextField,
        label: 'Lot #',
        fieldKey: 'inventoryItem.id',
        getDynamicAttr: ({ fieldValue }) => ({
          disabled: !!fieldValue,
        }),
      },
      'binLocation.name': {
        type: TextField,
        label: 'Bin',
        fieldKey: 'inventoryItem.id',
        getDynamicAttr: ({ fieldValue }) => ({
          disabled: !!fieldValue,
        }),
      },
      expirationDate: {
        type: DateField,
        label: 'Expiry Date',
        fieldKey: 'inventoryItem.id',
        getDynamicAttr: ({ fieldValue }) => ({
          dateFormat: 'YYYY/MM/DD',
          disabled: !!fieldValue,
        }),
      },
      quantityAvailable: {
        type: TextField,
        label: 'Qty Available',
      },
    },
  },
};

class AdjustInventoryModal extends Component {
  constructor(props) {
    super(props);

    const {
      fieldConfig: { attributes, getDynamicAttr },
    } = props;
    const dynamicAttr = getDynamicAttr ? getDynamicAttr(props) : {};
    const attr = { ...attributes, ...dynamicAttr };

    this.state = { attr };
    this.onOpen = this.onOpen.bind(this);
    this.onSave = this.onSave.bind(this);
  }

  onOpen() {
    this.props.change(
      'stock-movement-wizard',
      'adjustInventory',
      this.state.attr.fieldValue.availableItems,
    );
  }

  // temporary disablers
  /* eslint-disable class-methods-use-this */
  /* eslint-disable no-unused-vars */
  onSave(values) {
    this.props.showSpinner();
  }

  render() {
    if (this.state.attr.subfield) {
      return null;
    }

    return (
      <ModalWrapper
        {...this.state.attr}
        onOpen={this.onOpen}
        onSave={this.props.handleSubmit(values => this.onSave(values))}
      >
        <form className="print-mt">
          {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName))}
        </form>
      </ModalWrapper>
    );
  }
}

export default reduxForm({
  form: 'stock-movement-wizard',
  destroyOnUnmount: false,
  forceUnregisterOnUnmount: true,
})(connect(null, { change, showSpinner, hideSpinner })(AdjustInventoryModal));

AdjustInventoryModal.propTypes = {
  change: PropTypes.func.isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  fieldName: PropTypes.string.isRequired,
  fieldConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
  handleSubmit: PropTypes.func.isRequired,
};
