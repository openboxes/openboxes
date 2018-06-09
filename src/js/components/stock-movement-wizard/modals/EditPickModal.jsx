import React, { Component } from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { change, reduxForm, formValueSelector } from 'redux-form';
import { connect } from 'react-redux';

import ModalWrapper from '../../form-elements/ModalWrapper';
import ValueSelectorField from '../../form-elements/ValueSelectorField';
import LabelField from '../../form-elements/LabelField';
import TextField from '../../form-elements/TextField';
import ArrayField from '../../form-elements/ArrayField';
import { renderFormField, generateKey } from '../../../utils/form-utils';

const FIELDS = {
  editPick: {
    type: ArrayField,
    fields: {
      lot: {
        type: LabelField,
        label: 'Lot #',
      },
      expiryDate: {
        type: LabelField,
        label: 'Expiry Date',
      },
      bin: {
        type: LabelField,
        label: 'Bin',
      },
      recipient: {
        type: ValueSelectorField,
        label: 'Includes recipient',
        attributes: {
          formName: 'stock-movement-wizard',
        },
        getDynamicAttr: ({ rowIndex }) => ({
          field: `editPick[${rowIndex}].recipient`,
        }),
        component: LabelField,
        componentConfig: {
          getDynamicAttr: ({ selectedValue }) => ({
            className: selectedValue ? 'fa fa-user' : '',
          }),
        },
      },
      qtyAvailable: {
        type: LabelField,
        label: 'Qty available',
      },
      qtyPicked: {
        type: TextField,
        label: 'Qty picked',
        attributes: {
          type: 'number',
        },
      },
    },
  },
};

/* eslint no-param-reassign: "error" */
class EditPickModal extends Component {
  constructor(props) {
    super(props);

    const {
      fieldConfig: { attributes, getDynamicAttr },
    } = props;
    const dynamicAttr = getDynamicAttr ? getDynamicAttr(props) : {};
    const attr = { ...attributes, ...dynamicAttr };

    this.state = { attr, currentEdit: {} };

    this.onOpen = this.onOpen.bind(this);
    this.onSave = this.onSave.bind(this);
  }

  onOpen() {
    // Get Pick Page data
    const { pickPage } = this.props;
    // Find product that will be edited
    const editPick =
      _.find(pickPage, item =>
        item.product.code === this.state.attr.productCode && item.availableLots.length >= 0);
    // Update specfied field in redux form
    this.props.change('stock-movement-wizard', 'editPick', editPick.availableLots);
    this.setState({
      currentEdit: {
        itemCode: editPick.product.code,
        itemName: editPick.product.name,
        qtyRequested: editPick.quantity,
      },
    });
  }

  onSave() {
    const { pickPage, editPick } = this.props;
    // Get all new lot picks
    const newPicks = editPick;
    // Get lots that were picked before
    const currentPicks = _.filter(pickPage, line =>
      line.product.code === this.state.currentEdit.itemCode && !!line.lot);
    // Get difference between old and new lots, this lots are assumed to be saved
    let picksToSave = _.differenceBy(
      _.filter(newPicks, pick => pick.qtyPicked > 0),
      currentPicks,
      'lotWithBin',
    );
    // For new picks add rowKey (if it does not exist)
    picksToSave = _.map(picksToSave, pick => (
      {
        ...pick,
        rowKey: pick.rowKey || generateKey(),
      }
    ));
    // Get intersections of old and new lots, for this picks we have to check direct differences
    const inters = _.intersectionBy(newPicks, currentPicks, 'lotWithBin');
    _.forEach(inters, (obj) => {
      const { lotWithBin } = obj;
      // Get old pick to be potentially crossed out
      const pickToCrossOut = _.find(currentPicks, pick => pick.lotWithBin === lotWithBin);
      // Get new pick to be potentially added
      const pickToAdd = _.find(newPicks, pick => pick.lotWithBin === lotWithBin);
      picksToSave.push({
        ...pickToCrossOut,
        crossedOut: pickToAdd.qtyPicked !== pickToCrossOut.qtyPicked,
        rowKey: pickToCrossOut.rowKey || generateKey(),
      });
      if (pickToAdd.qtyPicked > 0 && pickToAdd.qtyPicked !== pickToCrossOut.qtyPicked) {
        picksToSave.push({ ...pickToAdd, rowKey: pickToAdd.rowKey || generateKey() });
      }
    });
    // Remove old picks for specified product from current data in Pick Page
    _.remove(pickPage, pick => pick.product.code === this.state.currentEdit.itemCode && pick.lot);
    // Find index after witch we will add our new picks
    const itemIdx = _.findIndex(
      pickPage,
      pick => pick.product.code === this.state.currentEdit.itemCode,
    );
    // Insert our new picked lots
    pickPage.splice(itemIdx + 1, 0, ...(_.sortBy(picksToSave, ['-class', 'lotWithBin'])));
    const filteredPicks = _.filter(picksToSave, pick => !pick.crossedOut);
    // Calculate summarised quantity picked (for specified item)
    pickPage[itemIdx].qtyPicked =
      _.reduce(filteredPicks, (sum, lot) => sum + parseInt(lot.qtyPicked, 10), 0);
    // Update available lots quantities picked for specified item
    _.forEach(pickPage[itemIdx].availableLots, (lot) => {
      const newAvailableLot = _.find(filteredPicks, pick => pick.lotWithBin === lot.lotWithBin);
      lot.qtyPicked = newAvailableLot ? newAvailableLot.qtyPicked : 0;
    });
    // Update specfied field in redux form
    this.props.change('stock-movement-wizard', 'pickPage', pickPage);
  }

  render() {
    return (
      <ModalWrapper {...this.state.attr} onOpen={this.onOpen} onSave={this.onSave}>
        <form className="print-mt">
          <div className="font-weight-bold">Product Code: {this.state.currentEdit.itemCode}</div>
          <div className="font-weight-bold">Product Name: {this.state.currentEdit.itemName}</div>
          <div className="font-weight-bold pb-2">Quantity Requested: {this.state.currentEdit.qtyRequested}</div>
          {_.map(FIELDS, (fieldConfig, fieldName) => renderFormField(fieldConfig, fieldName))}
        </form>
      </ModalWrapper>
    );
  }
}

const selector = formValueSelector('stock-movement-wizard');

const mapStateToProps = state => ({ pickPage: selector(state, 'pickPage'), editPick: selector(state, 'editPick') });

export default reduxForm({
  form: 'stock-movement-wizard',
  destroyOnUnmount: false,
  forceUnregisterOnUnmount: true,
})(connect(mapStateToProps, { change })(EditPickModal));

EditPickModal.propTypes = {
  change: PropTypes.func.isRequired,
  fieldName: PropTypes.string.isRequired,
  fieldConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
  pickPage: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  editPick: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  rowIndex: PropTypes.number.isRequired,
};
