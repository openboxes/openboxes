/* eslint-disable */

import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import _ from 'lodash';

import ModalWrapper from '../../form-elements/ModalWrapper';
import TextField from '../../form-elements/TextField';
import ArrayField from '../../form-elements/ArrayField';
import LabelField from '../../form-elements/LabelField';
import SelectField from '../../form-elements/SelectField';
import DateField from '../../form-elements/DateField';
import { showSpinner, hideSpinner } from '../../../actions';
import { debouncedUsersFetch } from '../../../utils/option-utils';

const FIELDS = {
  splitLineItems: {
    addButton: 'Add Line',
    type: ArrayField,
    disableVirtualization: true,
    fields: {
      productName: {
        type: LabelField,
        flexWidth: '6',
        label: 'Product Name',
      },
      lotNumber: {
        type: LabelField,
        label: 'Lot #',
        fieldKey: 'inventoryItem.id',
      },
      expirationDate: {
        type: DateField,
        label: 'Expiry Date',
        fieldKey: 'inventoryItem.id',
        attributes: {
          dateFormat: 'YYYY/MM/DD',
          disabled: true,
        },
      },
      binLocationName: {
        type: LabelField,
        label: 'Bin',
      },
      quantityShipped: {
        type: TextField,
        label: 'QTY',
        fixedWidth: '150px',
        attributes: {
          type: 'number',
        },
      },
      recipient: {
        type: SelectField,
        label: 'Recipient',
        flexWidth: '2.5',
        fieldKey: '',
        attributes: {
          async: true,
          required: true,
          showValueTooltip: true,
          openOnClick: false,
          autoload: false,
          loadOptions: debouncedUsersFetch,
          cache: false,
          options: [],
          labelKey: 'name',
        },
      },
      palletName: {
        type: TextField,
        label: 'Pallet',
        fixedWidth: '150px',
      },
      boxName: {
        type: TextField,
        label: 'Box',
        fixedWidth: '150px',
      },
    },
  },
};

const validate = (values) => {
  // TODO: validate for total packs quantitiy not exceeding quantity for this item
  const errors = {};
  errors.splitLineItems = [];
  return errors;
};

/** Modal window where user can split line for Packing Page item */
class PackingSplitLineModal extends Component {
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

  componentDidMount() {}

  /**
   * Loads current packing lines for specified item
   * @public
   */
  onOpen() {
    this.setState({
      formValues: {
        splitLineItems: [],
      },
    });
  }

  /**
   * Sends all changes made by user in this modal to API and updates data.
   * @param {object} values
   * @public
   */
  onSave(values) {
    // TODO: send splitted lines to backend
  }

  /**
   * Sums up quantity packed from all available lines.
   * @param {object} values
   * @public
   */
  /* eslint-disable-next-line class-methods-use-this */
  calculatePacked(values) {
    return (
      <div>
        <div className="font-weight-bold pb-2">Quantity Packed: {_.reduce(values.splitLineItems, (sum, val) =>
          (sum + (val.quantity ? _.toInteger(val.quantity) : 0)), 0)}
        </div>
        <hr />
      </div>
    );
  }

  render() {
    return (
      <ModalWrapper
        {...this.state.attr}
        onOpen={this.onOpen}
        onSave={this.onSave}
        fields={FIELDS}
        initialValues={this.state.formValues}
        validate={validate}
        renderBodyWithValues={this.calculatePacked}
      >
        <div>
          {/*<div className="font-weight-bold">Total Quantity: {this.state.attr.fieldValue.totalQuantity} </div>*/}
        </div>
      </ModalWrapper>
    );
  }
}

export default connect(null, { showSpinner, hideSpinner })(PackingSplitLineModal);

PackingSplitLineModal.propTypes = {
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
  /** Function updating page on which modal is located called when user saves changes */
  onResponse: PropTypes.func.isRequired,
};
