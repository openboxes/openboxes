import React, { Component } from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import { connect } from 'react-redux';
import moment from 'moment';

import ModalWrapper from '../../form-elements/ModalWrapper';
import ArrayField from '../../form-elements/ArrayField';
import TextField from '../../form-elements/TextField';
import DateField from '../../form-elements/DateField';
import SelectField from '../../form-elements/SelectField';
import { showSpinner, hideSpinner } from '../../../actions';
import Translate from '../../../utils/Translate';
import { debounceProductsFetch } from '../../../utils/option-utils';

const FIELDS = {
  lines: {
    type: ArrayField,
    addButton: ({
    // eslint-disable-next-line react/prop-types
      addRow, shipmentItemId, binLocation, product,
    }) => (
      <button
        type="button"
        className="btn btn-outline-success btn-xs"
        onClick={() => addRow({
          shipmentItemId,
          binLocation,
          product: {
            ...product,
           label: `${product.productCode} - ${product.name}`,
          },
          receiptItemId: null,
          newLine: true,
        })}
      ><Translate id="react.default.button.addLine.label" defaultMessage="Add line" />
      </button>
    ),
    fields: {
      product: {
        type: SelectField,
        label: 'react.partialReceiving.product.label',
        defaultMessage: 'Product',
        fieldKey: 'disabled',
        attributes: {
          className: 'text-left',
          async: true,
          openOnClick: false,
          autoload: false,
          filterOptions: options => options,
          cache: false,
          options: [],
          showValueTooltip: true,
        },
        getDynamicAttr: ({ fieldValue, debouncedProductsFetch }) => ({
          disabled: fieldValue,
          loadOptions: debouncedProductsFetch,
        }),
      },
      lotNumber: {
        type: TextField,
        label: 'react.partialReceiving.lot.label',
        defaultMessage: 'Lot',
      },
      expirationDate: {
        type: DateField,
        label: 'react.partialReceiving.expiry.label',
        defaultMessage: 'Expiry',
        attributes: {
          dateFormat: 'MM/DD/YYYY',
          autoComplete: 'off',
        },
      },
      quantityShipped: {
        type: TextField,
        label: 'react.partialReceiving.quantityShipped.label',
        defaultMessage: 'Quantity shipped',
        attributes: {
          type: 'number',
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
      formValues: [],
    };

    this.onOpen = this.onOpen.bind(this);
    this.onSave = this.onSave.bind(this);
    this.validate = this.validate.bind(this);

    this.debouncedProductsFetch = debounceProductsFetch(
      this.props.debounceTime,
      this.props.minSearchLength,
      this.props.locationId,
    );
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
   * Loads available items into modal's form.
   * @public
  */
  onOpen() {
    this.setState({
      formValues: {
        lines: _.map([this.state.attr.fieldValue], value => ({
          ...value,
          product: {
            ...value.product,
            label: `${_.get(value, 'product.productCode')} - ${_.get(value, 'product.name')}`,
          },
          disabled: true,
          originalLine: true,
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

  validate(values) {
    const errors = {};
    errors.lines = [];
    const date = moment(this.props.minimumExpirationDate, 'MM/DD/YYYY');

    _.forEach(values.lines, (line, key) => {
      if (line && _.isNil(line.quantityShipped)) {
        errors.lines[key] = { quantityShipped: 'react.partialReceiving.error.enterQuantityShipped.label' };
      }
      if (line.quantityShipped < 0) {
        errors.lines[key] = { quantityShipped: 'react.partialReceiving.error.quantityShippedNegative.label' };
      }
      const dateRequested = moment(line.expirationDate, 'MM/DD/YYYY');
      if (date.diff(dateRequested) > 0) {
        errors.lines[key] = { expirationDate: 'react.partialReceiving.error.invalidDate.label' };
      }
    });

    return errors;
  }

  render() {
    return (
      <ModalWrapper
        {...this.state.attr}
        onOpen={this.onOpen}
        onSave={this.onSave}
        validate={this.validate}
        initialValues={this.state.formValues}
        fields={FIELDS}
        formProps={{
          shipmentItemId: this.state.attr.fieldValue.shipmentItemId,
          debouncedProductsFetch: this.debouncedProductsFetch,
          binLocation: this.state.attr.fieldValue.binLocation,
          product: this.state.attr.fieldValue.product,
        }}
      >
        <div>
          <div className="font-weight-bold mb-3">
            <Translate id="react.partialReceiving.originalQtyShipped.label" defaultMessage="Original quantity shipped" />: {this.state.attr.fieldValue.quantityShipped}
          </div>
        </div>
      </ModalWrapper>
    );
  }
}

const mapStateToProps = state => ({
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
  minimumExpirationDate: state.session.minimumExpirationDate,
});

export default connect(mapStateToProps, { showSpinner, hideSpinner })(EditLineModal);

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
  /** Location ID (destination). Needs to be used in /api/products request. */
  locationId: PropTypes.string.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  minimumExpirationDate: PropTypes.string.isRequired,
};
