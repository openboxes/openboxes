import React, { Component } from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { change, formValueSelector, reduxForm } from 'redux-form';
import { connect } from 'react-redux';

import ModalWrapper from '../../form-elements/ModalWrapper';
import TextField from '../../form-elements/TextField';
import ArrayField from '../../form-elements/ArrayField';
import LabelField from '../../form-elements/LabelField';
import { renderFormField } from '../../../utils/form-utils';
import DateField from '../../form-elements/DateField';
import { showSpinner, hideSpinner } from '../../../actions';
import apiClient from '../../../utils/apiClient';

const FIELDS = {
  adjustInventory: {
    addButton: 'Add new lot number',
    type: ArrayField,
    disableVirtualization: true,
    fields: {
      'binLocation.name': {
        type: TextField,
        label: 'Bin',
        fieldKey: 'inventoryItem.id',
        getDynamicAttr: ({ fieldValue }) => ({
          disabled: !!fieldValue,
        }),
      },
      lotNumber: {
        type: TextField,
        label: 'Lot #',
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
        type: LabelField,
        label: 'Previous Qty',
      },
      quantityAdjusted: {
        type: TextField,
        label: 'Current Qty',
        attributes: {
          type: 'number',
        },
      },
      comments: {
        type: TextField,
        label: 'Comments',
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

  onSave(values) {
    this.props.showSpinner();

    const url = '/openboxes/api/stockAdjustments';
    const payload = _.map(values.adjustInventory, (adItem) => {
      const adjustItem = _.find(this.state.attr.fieldValue.availableItems, item => item['inventoryItem.id'] === adItem['inventoryItem.id']);
      if (adjustItem) {
        return {
          id: adjustItem.id,
          'inventoryItem.id': adItem['inventoryItem.id'],
          'binLocation.id': adItem['binLocation.id'] || '',
          quantityAvailable: adItem.quantityAvailable,
          quantityAdjusted: adItem.quantityAdjusted,
          comments: adItem.comments,
        };
      }
      return {
        'inventoryItem.id': adItem['inventoryItem.id'],
        'binLocation.id': adItem['binLocation.id'] || '',
        quantityAvailable: adItem.quantityAvailable,
        quantityAdjusted: adItem.quantityAdjusted,
        comments: adItem.comments,
      };
    });

    return apiClient.post(url, payload).then(() => {
      apiClient.get(`/openboxes/api/stockMovements/${this.state.attr.stockMovementId}?stepNumber=4`)
        .then((resp) => {
          const { pickPageItems } = resp.data.data.pickPage;
          this.props.change('stock-movement-wizard', 'pickPageItems', []);
          this.props.change('stock-movement-wizard', 'pickPageItems', this.state.attr.checkForInitialPicksChanges(pickPageItems));

          this.props.hideSpinner();
        })
        .catch(() => { this.props.hideSpinner(); });
    }).catch(() => { this.props.hideSpinner(); });
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
  adjustInventory: PropTypes.arrayOf(PropTypes.shape({})),
  fieldName: PropTypes.string.isRequired,
  fieldConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  handleSubmit: PropTypes.func.isRequired,
};

AdjustInventoryModal.defaultProps = {
  adjustInventory: [],
};
