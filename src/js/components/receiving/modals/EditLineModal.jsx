import React, { Component } from 'react';

import _ from 'lodash';
import moment from 'moment';
import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { hideSpinner, showSpinner } from 'actions';
import ArrayField from 'components/form-elements/ArrayField';
import DateField from 'components/form-elements/DateField';
import ModalWrapper from 'components/form-elements/ModalWrapper';
import ProductSelectField from 'components/form-elements/ProductSelectField';
import TextField from 'components/form-elements/TextField';
import ConfirmExpirationDateModal from 'components/modals/ConfirmExpirationDateModal';
import DateFormat from 'consts/dateFormat';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

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
          product,
          receiptItemId: null,
          newLine: true,
        })}
      >
        <Translate id="react.default.button.addLine.label" defaultMessage="Add line" />
      </button>
    ),
    fields: {
      product: {
        type: ProductSelectField,
        label: 'react.partialReceiving.product.label',
        defaultMessage: 'Product',
        fieldKey: 'disabled',
        getDynamicAttr: ({ fieldValue }) => ({
          disabled: fieldValue,
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
          localizeDate: true,
          localizedDateFormat: DateFormat.COMMON,
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

// Erase receiving quantity when shipped quantity is equal to 0
const eraseReceivingQuantity = (items) => items.map((item) => {
  if (!_.parseInt(item.quantityShipped)) {
    return { ...item, quantityReceiving: null };
  }

  return item;
});

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
    /**
     * When calculating grouped shipment items while building the modal (it is built once)
     * we want to look at the initial values in order to know the original qty shipped
     * this is why we pass this.props.initialReceiptCandidates to the groupShipmentItems function
     * If we were to pass the this.props.values while building a new row,
     * we already might have edited qty shipped
     * that would be considered as original qty that we should validate further edits with.
     */
    const groupedShipmentItems = this.groupShipmentItems(this.props.initialReceiptCandidates);
    const shipmentItemsQuantityMap = Object.entries(groupedShipmentItems)
      .reduce((acc, [key, value]) =>
        ({
          ...acc,
          [key]: _.sumBy(value, (item) => _.toInteger(item.quantityShipped)),
        }),
      {});

    this.state = {
      attr,
      formValues: [],
      shipmentItemsQuantityMap,
      // This is the original quantity shipped of a shipmentItem. This indicates the maximum.
      shipmentItemQuantityShippedSum: shipmentItemsQuantityMap[attr.fieldValue?.shipmentItemId],
      showMismatchQuantityShippedInfo: false,
      isExpirationModalOpen: false,
      // Stores the resolve function for the ConfirmExpirationDateModal promise
      resolveExpirationModal: null,
      itemsWithMismatchedExpiry: [],
    };

    this.onOpen = this.onOpen.bind(this);
    this.onSave = this.onSave.bind(this);
    this.save = this.save.bind(this);
    this.validate = this.validate.bind(this);
  }

  componentWillReceiveProps(nextProps) {
    const {
      fieldConfig: { attributes, getDynamicAttr },
    } = nextProps;
    const dynamicAttr = getDynamicAttr ? getDynamicAttr(nextProps) : {};
    const attr = { ...attributes, ...dynamicAttr };

    this.setState({ attr });
  }

  groupShipmentItems(values = this.props.values) {
    const { containers } = values;
    /**
     * containers are built like: [{..., shipmentItems: []}]
     * so in the end we have to flat the result,
     * as after mapping, the result would look like
     * [[<shipmentItem>, <shipmentItem>], [<shipmentItem>, <shipmentItem>]]
     */
    const shipmentItems = containers.flatMap((container) => container.shipmentItems);
    // Return the results as map of { [shipmentItemId]: [<shipmentItem>, <shipmentItem>] }
    return _.groupBy(shipmentItems, 'shipmentItemId');
  }

  /**
   * Loads available items into modal's form.
   * @public
  */
  onOpen() {
    const { shipmentItemsQuantityMap, attr } = this.state;
    this.setState((prev) => ({
      formValues: {
        lines: _.map([prev.attr.fieldValue], (value) => ({
          ...value,
          disabled: true,
          originalLine: true,
        })),
      },
      /**
       * This needs to be set again while reopening a modal, due to table indexing problems
       * assuming we have 5 lines, we split the first line to two items,
       * the new, split item contains quantity info from the row,
       * that was at this place before (2nd row, now 3rd row, after splitting the line)
       */
      shipmentItemQuantityShippedSum: shipmentItemsQuantityMap[attr.fieldValue?.shipmentItemId],
    }));
  }

  /**
   * Sends all changes made by user in this modal to API and updates data.
   * @param {object} values
   * @public
   */
  async onSave(values) {
    const lines = eraseReceivingQuantity(values.lines);

    const oldItemsMap = new Map();
    this.state.formValues.lines.forEach((item) => {
      if (item.product) {
        oldItemsMap.set(`${item.product.id}-${item.lotNumber}`, item);
      }
    });

    const itemsWithMismatchedExpiry = [];
    lines.forEach((line) => {
      const key = `${line.product?.id}-${line.lotNumber}`;
      const oldItem = oldItemsMap.get(key);

      if (oldItem && oldItem.quantityOnHand && oldItem.expirationDate !== line.expirationDate) {
        itemsWithMismatchedExpiry.push({
          code: line.product?.productCode,
          product: line.product,
          lotNumber: line.lotNumber,
          previousExpiry: oldItem.expirationDate,
          newExpiry: line.expirationDate,
        });
      }
    });

    if (itemsWithMismatchedExpiry.length > 0) {
      const shouldUpdateExpirationDate =
        await this.confirmExpirationDateSave(itemsWithMismatchedExpiry);
      if (!shouldUpdateExpirationDate) {
        return Promise.reject();
      }
    }

    return this.save({ ...values, lines });
  }

  save(values) {
    this.state.attr.saveEditLine(
      values.lines,
      this.state.attr.parentIndex,
      this.props.values,
      this.props.rowIndex,
    );
  }

  /**
   * Shows Inventory item expiration date update confirmation modal.
   * @param {Array} itemsWithMismatchedExpiry - Array of elements with mismatched expiration dates.
   * @returns {Promise} - Resolves to true if user confirms the update, false if not.
   * @public
   */
  confirmExpirationDateSave(itemsWithMismatchedExpiry) {
    return new Promise((resolve) => {
      this.setState({
        isExpirationModalOpen: true,
        resolveExpirationModal: resolve,
        itemsWithMismatchedExpiry,
      });
    });
  }

  calculateQuantityShippedSum(values) {
    const { shipmentItemQuantityShippedSum } = this.state;
    const originalItem = values.find((item) => item.rowId);
    const qtyShippedSumFromModal = _.sumBy(values, (item) => _.toInteger(item.quantityShipped));
    /**
     * When calculating grouped shipment items while validating,
     * we want to look at the current form values
     * this is why we pass this.props.values to the groupShipmentItems function
     */
    const groupedShipmentItems = this.groupShipmentItems(this.props.values);
    /**
     * We want to exclude from the calculation an "original item" (that contains a rowId).
     * It would receive the rowId and we would find the original item
     * if the item was split at least once. Assuming we had a shipment item with quantity shipped 35
     * we split it to two lines (20, 15) and we try to split one of them again
     * (let's say, the second one), when we open the modal,
     * as existingItemsQuantities we would expect to get 20,
     * and to get the total sum of 35 (15 + x, depending on how many lines we would split again)
     * Since the "15" item is included in the modal (values) and
     * we would not exclude it in the existingItemsQuantities, it would be counted twice,
     * so the existingItemsQuantities for this shipment item would be 35, not 20.
     */
    const sumExistingShipmentItemQuantity = (shipmentItems) =>
      shipmentItems
        .reduce((sum, curr) => (curr.rowId === originalItem?.rowId
          ? sum
          : sum + _.toInteger(curr.quantityShipped)), 0);
    const existingItemsQuantities = Object.entries(groupedShipmentItems)
      .reduce((acc, [key, value]) =>
        ({
          ...acc,
          [key]: sumExistingShipmentItemQuantity(value),
        }),
      {});
    /** If the original item was not found, it means it's first attempt to edit a line,
     *  and we don't have to check for existing quantities in rows below/above
     *  as we only care about sum from the modal
     */
    const existingItemsQuantitySum = originalItem
      ? existingItemsQuantities[originalItem.shipmentItemId]
      : 0;
    /**
     * If sum from modal + eventual existing quantities is not equal to the original shipped qty,
     * show the indicator of not matching quantity
    */
    if ((qtyShippedSumFromModal + existingItemsQuantitySum) !== shipmentItemQuantityShippedSum) {
      this.setState({ showMismatchQuantityShippedInfo: true });
      return;
    }
    this.setState({ showMismatchQuantityShippedInfo: false });
  }

  validate(values) {
    this.calculateQuantityShippedSum(values.lines);
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
      if (line.expirationDate && (_.isNil(line.lotNumber) || _.isEmpty(line.lotNumber))) {
        errors.lines[key] = { lotNumber: 'react.partialReceiving.error.expiryWithoutLot.label' };
      }
      if (!_.isNil(line.product) && line.product.lotAndExpiryControl) {
        if (!line.expirationDate && (_.isNil(line.lotNumber) || _.isEmpty(line.lotNumber))) {
          errors.lines[key] = {
            expirationDate: 'react.partialReceiving.error.lotAndExpiryControl.label',
            lotNumber: 'react.partialReceiving.error.lotAndExpiryControl.label',
          };
        } else if (!line.expirationDate) {
          errors.lines[key] = { expirationDate: 'react.partialReceiving.error.lotAndExpiryControl.label' };
        } else if (_.isNil(line.lotNumber) || _.isEmpty(line.lotNumber)) {
          errors.lines[key] = { lotNumber: 'react.partialReceiving.error.lotAndExpiryControl.label' };
        }
      }
    });

    return errors;
  }

  /**
   * Handles the response from the expiration date confirmation modal.
   * @param {boolean} shouldUpdate - True if the user confirmed the update, false if not.
   * @public
   */
  handleExpirationModalResponse(shouldUpdate) {
    // Resolve the promise returned by confirmExpirationDateSave.
    if (this.state.resolveExpirationModal) {
      this.state.resolveExpirationModal(shouldUpdate);
    }

    // Close the modal and reset its state.
    this.setState({
      isExpirationModalOpen: false,
      resolveExpirationModal: null,
      itemsWithMismatchedExpiry: [],
    });
  }

  render() {
    return (
      <>
        <ModalWrapper
          {...this.state.attr}
          onOpen={this.onOpen}
          onSave={this.onSave}
          validate={this.validate}
          initialValues={this.state.formValues}
          fields={FIELDS}
          wrapperClassName={this.props.wrapperClassName}
          formProps={{
            shipmentItemId: this.state.attr.fieldValue.shipmentItemId,
            binLocation: this.state.attr.fieldValue.binLocation,
            product: this.state.attr.fieldValue.product,
          }}
        >
          <div>
            <div className="font-weight-bold mb-3">
              <Translate id="react.partialReceiving.originalQtyShipped.label" defaultMessage="Original quantity shipped" />
              :
              {this.state.attr.fieldValue.quantityShipped}
            </div>
            {this.state.showMismatchQuantityShippedInfo
            && (
              <div className="font-weight-bold font-red-ob">
                <Translate
                  id="react.partialReceiving.error.mismatchingQuantityShipped.label"
                  defaultMessage="The total edited quantity does not match the original quantity shipped."
                />
              </div>
            )}
          </div>
        </ModalWrapper>
        <ConfirmExpirationDateModal
          isOpen={this.state.isExpirationModalOpen}
          itemsWithMismatchedExpiry={this.state.itemsWithMismatchedExpiry}
          onConfirm={() => this.handleExpirationModalResponse(true)}
          onCancel={() => this.handleExpirationModalResponse(false)}
        />
      </>
    );
  }
}

const mapStateToProps = (state) => ({
  minimumExpirationDate: state.session.minimumExpirationDate,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
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
  minimumExpirationDate: PropTypes.string.isRequired,
  translate: PropTypes.func.isRequired,
  wrapperClassName: PropTypes.string,
  values: PropTypes.shape({
    containers: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  }).isRequired,
  initialReceiptCandidates: PropTypes.shape({
    containers: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  }).isRequired,
};

EditLineModal.defaultProps = {
  wrapperClassName: null,
};
