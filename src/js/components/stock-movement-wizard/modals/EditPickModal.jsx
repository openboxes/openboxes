import React, { Component } from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import ModalWrapper from '../../form-elements/ModalWrapper';
import LabelField from '../../form-elements/LabelField';
import TextField from '../../form-elements/TextField';
import ArrayField from '../../form-elements/ArrayField';
import SelectField from '../../form-elements/SelectField';
import apiClient from '../../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../../actions';
import Translate from '../../../utils/Translate';

const FIELDS = {
  reasonCode: {
    type: SelectField,
    label: 'react.stockMovement.reasonCode.label',
    defaultMessage: 'Reason code',
    getDynamicAttr: props => ({
      options: props.reasonCodes,
    }),
  },
  availableItems: {
    type: ArrayField,
    fields: {
      lotNumber: {
        type: LabelField,
        label: 'react.stockMovement.lot.label',
        defaultMessage: 'Lot',
      },
      expirationDate: {
        type: LabelField,
        label: 'react.stockMovement.expiry.label',
        defaultMessage: 'Expiry',
      },
      'binLocation.name': {
        type: LabelField,
        label: 'react.stockMovement.binLocation.label',
        defaultMessage: 'Bin Location',
        hide: ({ hasBinLocationSupport }) => !hasBinLocationSupport,
      },
      quantityAvailable: {
        type: LabelField,
        label: 'react.stockMovement.quantityAvailable.label',
        defaultMessage: 'Qty Available',
        fixedWidth: '150px',
        attributes: {
          formatValue: value => (value ? value.toLocaleString('en-US') : null),
        },
      },
      quantityPicked: {
        type: TextField,
        label: 'react.stockMovement.quantityPicked.label',
        defaultMessage: 'Qty Picked',
        fixedWidth: '140px',
        attributes: {
          type: 'number',
        },
      },
    },
  },
};

function validate(values) {
  const errors = {};
  errors.availableItems = [];
  _.forEach(values.availableItems, (item, key) => {
    if (item.quantityPicked > item.quantityAvailable) {
      errors.availableItems[key] = { quantityPicked: 'react.stockMovement.errors.higherTyPicked.label' };
    }
    if (item.quantityPicked < 0) {
      errors.availableItems[key] = { quantityPicked: 'react.stockMovement.errors.negativeQtyPicked.label' };
    }
  });

  const pickedSum = _.reduce(
    values.availableItems, (sum, val) =>
      (sum + (val.quantityPicked ? _.toInteger(val.quantityPicked) : 0)),
    0,
  );


  if (_.some(values.availableItems, val => !_.isNil(val.quantityPicked)) &&
    !values.reasonCode && pickedSum !== values.quantityRequired) {
    errors.reasonCode = 'react.stockMovement.errors.differentTotalQty.label';
  }

  return errors;
}

/** Modal window where user can edit pick. */
/* eslint no-param-reassign: "error" */
class EditPickModal extends Component {
  constructor(props) {
    super(props);

    const {
      fieldConfig: { attributes, getDynamicAttr },
    } = props;
    const dynamicAttr = getDynamicAttr ? getDynamicAttr(props) : {};
    const attr = { ...attributes, ...dynamicAttr };

    this.state = {
      attr,
      formValues: {},
    };

    this.onOpen = this.onOpen.bind(this);
    this.onSave = this.onSave.bind(this);
  }

  componentWillReceiveProps(nextProps) {
    const {
      fieldConfig: { attributes, getDynamicAttr },
    } = nextProps;
    const dynamicAttr = getDynamicAttr ? getDynamicAttr(nextProps) : {};
    const attr = { ...attributes, ...dynamicAttr };

    this.setState({ attr });
  }

  /**
   * Loads chosen items, required quantity and reason codes into modal's form.
   * @public
   */
  onOpen() {
    const availableItems = _.map(this.state.attr.fieldValue.availableItems, (avItem) => {
      // check if this picklist item already exists
      const picklistItem = _.find(
        _.filter(this.state.attr.fieldValue.picklistItems, listItem => !listItem.initial),
        item => item['inventoryItem.id'] === avItem['inventoryItem.id'] && item['binLocation.id'] === avItem['binLocation.id'],
      );

      if (picklistItem) {
        return {
          ...avItem,
          id: picklistItem.id,
          quantityPicked: picklistItem.quantityPicked,
        };
      }

      return avItem;
    });

    this.setState({
      formValues: {
        availableItems,
        reasonCode: '',
        quantityRequired: this.state.attr.fieldValue.quantityRequired,
      },
    });
  }

  /**
   * Sends all changes made by user in this modal to API and updates data.
   * @param {object} values
   * @public
   */
  onSave(values) {
    this.props.showSpinner();

    const url = `/openboxes/api/stockMovementItems/${this.state.attr.fieldValue['requisitionItem.id']}/updatePicklist`;
    const payload = {
      picklistItems: _.map(values.availableItems, avItem => ({
        id: avItem.id || '',
        'inventoryItem.id': avItem['inventoryItem.id'],
        'binLocation.id': avItem['binLocation.id'] || '',
        quantityPicked: _.isNil(avItem.quantityPicked) ? '' : avItem.quantityPicked,
      })),
      reasonCode: values.reasonCode || '',
    };

    return apiClient.post(url, payload)
      .then((resp) => {
        const pickPageItem = resp.data.data;

        this.state.attr.onResponse(pickPageItem);
        this.props.hideSpinner();
      })
      .catch(() => { this.props.hideSpinner(); });
  }

  /**
   * Sums up quantity picked from all available items.
   * @param {object} values
   * @public
   */
  /* eslint-disable-next-line class-methods-use-this */
  calculatePicked(values) {
    return (
      <div>
        <div className="font-weight-bold pb-2">
          <Translate id="react.stockMovement.quantityPicked.label" defaultMessage="Qty Picked" />: {_.reduce(values.availableItems, (sum, val) =>
            (sum + (val.quantityPicked ? _.toInteger(val.quantityPicked) : 0)), 0)}
        </div>
        <hr />
      </div>
    );
  }

  render() {
    if (this.state.attr.subfield) {
      return null;
    }

    return (
      <ModalWrapper
        {...this.state.attr}
        onOpen={this.onOpen}
        onSave={this.onSave}
        fields={FIELDS}
        validate={validate}
        initialValues={this.state.formValues}
        formProps={{
          reasonCodes: this.state.attr.reasonCodes,
          hasBinLocationSupport: this.props.hasBinLocationSupport,
        }}
        renderBodyWithValues={this.calculatePicked}
      >
        <div>
          <div className="font-weight-bold">
            <Translate id="react.stockMovement.productCode.label" defaultMessage="Product code" />: {this.state.attr.fieldValue.productCode}
          </div>
          <div className="font-weight-bold">
            <Translate id="react.stockMovement.productName.label" defaultMessage="Product name" />: {this.state.attr.fieldValue['product.name']}
          </div>
          <div className="font-weight-bold">
            <Translate id="react.stockMovement.quantityRequired.label" defaultMessage="Qty Required" />: {this.state.attr.fieldValue.quantityRequired}
          </div>
        </div>
      </ModalWrapper>
    );
  }
}

export default connect(null, { showSpinner, hideSpinner })(EditPickModal);

EditPickModal.propTypes = {
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
  /** Is true when currently selected location supports bins */
  hasBinLocationSupport: PropTypes.bool.isRequired,
};
