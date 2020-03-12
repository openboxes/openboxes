import _ from 'lodash';
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import ModalWrapper from '../../form-elements/ModalWrapper';
import TextField from '../../form-elements/TextField';
import ArrayField from '../../form-elements/ArrayField';
import LabelField from '../../form-elements/LabelField';
import SelectField from '../../form-elements/SelectField';
import { showSpinner, hideSpinner } from '../../../actions';
import { debounceUsersFetch } from '../../../utils/option-utils';
import Translate from '../../../utils/Translate';

const FIELDS = {
  splitLineItems: {
    // eslint-disable-next-line react/prop-types
    addButton: ({ addRow, lineItem }) => (
      <button
        type="button"
        className="btn btn-outline-success btn-xs"
        onClick={() => addRow({
          productName: lineItem.productName,
          lotNumber: lineItem.lotNumber,
          expirationDate: lineItem.expirationDate,
          binLocationName: lineItem.binLocationName,
          recipient: lineItem.recipient,
        })}
      > <Translate id="react.default.button.addLine.label" defaultMessage="Add line" />
      </button>
    ),
    type: ArrayField,
    fields: {
      productName: {
        type: LabelField,
        label: 'react.stockMovement.productName.label',
        defaultMessage: 'Product name',
      },
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
      binLocationName: {
        type: LabelField,
        label: 'react.stockMovement.binLocation.label',
        defaultMessage: 'Bin Location',
        getDynamicAttr: ({ hasBinLocationSupport }) => ({
          hide: !hasBinLocationSupport,
        }),
      },
      quantityShipped: {
        type: TextField,
        label: 'react.stockMovement.quantityShipped.label',
        defaultMessage: 'Quantity shipped',
        fixedWidth: '150px',
        attributes: {
          type: 'number',
        },
      },
      recipient: {
        type: SelectField,
        label: 'react.stockMovement.recipient.label',
        defaultMessage: 'Recipient',
        fieldKey: '',
        attributes: {
          async: true,
          required: true,
          showValueTooltip: true,
          openOnClick: false,
          autoload: false,
          cache: false,
          options: [],
          labelKey: 'name',
          filterOptions: options => options,
        },
        getDynamicAttr: props => ({
          loadOptions: props.debouncedUsersFetch,
        }),
      },
      palletName: {
        type: TextField,
        label: 'react.stockMovement.packLevel1.label',
        defaultMessage: 'Pack level 1',
        fixedWidth: '150px',
      },
      boxName: {
        type: TextField,
        label: 'react.stockMovement.packLevel2.label',
        defaultMessage: 'Pack level 2',
        fixedWidth: '150px',
      },
    },
  },
};

/** Modal window where user can split line for Packing Page item */
class PackingSplitLineModal extends Component {
  /**
   * Sums up quantity packed from all available lines.
   * @param {object} values
   * @public
   */
  static calculatePacked(values) {
    return (_.reduce(values, (sum, val) =>
      (sum + (val.quantityShipped ? _.toInteger(val.quantityShipped) : 0)), 0));
  }

  /**
   * Display sum of quantity packed from all available lines.
   * @param {object} values
   * @public
   */
  static displayPackedSum(values) {
    return (
      <div>
        <div className="font-weight-bold pb-2">
          <Translate id="react.stockMovement.quantityPacked.label" defaultMessage="Qty Packed" />: {PackingSplitLineModal.calculatePacked(values.splitLineItems)}
        </div>
        <hr />
      </div>
    );
  }

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
    this.validate = this.validate.bind(this);

    this.debouncedUsersFetch =
      debounceUsersFetch(this.props.debounceTime, this.props.minSearchLength);
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
   * Loads current packing lines for specified item
   * @public
   */
  onOpen() {
    this.setState({
      formValues: {
        splitLineItems: [
          {
            productName: this.state.attr.lineItem.productName,
            lotNumber: this.state.attr.lineItem.lotNumber,
            expirationDate: this.state.attr.lineItem.expirationDate,
            binLocationName: this.state.attr.lineItem.binLocationName,
            quantityShipped: this.state.attr.lineItem.quantityShipped,
            recipient: this.state.attr.lineItem.recipient,
            palletName: this.state.attr.lineItem.palletName,
            boxName: this.state.attr.lineItem.boxName,
          },
        ],
      },
    });
  }

  validate(values) {
    const shippedQty = _.toInteger(this.state.attr.lineItem.quantityShipped);
    const splitItemsQty = PackingSplitLineModal.calculatePacked(values.splitLineItems);
    const errors = { splitLineItems: [] };

    _.forEach(values.splitLineItems, (item, key) => {
      if (shippedQty !== splitItemsQty) {
        errors.splitLineItems[key] = { quantityShipped: 'react.stockMovement.errors.packingQty.label' };
      }
      if (item.quantityShipped < 0) {
        errors.splitLineItems[key] = { quantityShipped: 'react.stockMovement.errors.negativeQtyShipped.label' };
      }
    });

    return errors;
  }

  render() {
    return (
      <ModalWrapper
        {...this.state.attr}
        onOpen={this.onOpen}
        onSave={values =>
          this.state.attr.onSave(_.filter(values.splitLineItems, item => item.quantityShipped))}
        fields={FIELDS}
        initialValues={this.state.formValues}
        formProps={{
          lineItem: this.state.attr.lineItem,
          debouncedUsersFetch: this.debouncedUsersFetch,
          hasBinLocationSupport: this.props.hasBinLocationSupport,
        }}
        validate={this.validate}
        renderBodyWithValues={PackingSplitLineModal.displayPackedSum}
      >
        <div>
          <div className="font-weight-bold">
            <Translate id="react.stockMovement.totalQuantity.label" defaultMessage="Total quantity" />: {this.state.attr.lineItem.quantityShipped}
          </div>
        </div>
      </ModalWrapper>
    );
  }
}

const mapStateToProps = state => ({
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
  hasBinLocationSupport: state.session.currentLocation.hasBinLocationSupport,
});

export default connect(mapStateToProps, { showSpinner, hideSpinner })(PackingSplitLineModal);

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
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  /** Is true when currently selected location supports bins */
  hasBinLocationSupport: PropTypes.bool.isRequired,
};
