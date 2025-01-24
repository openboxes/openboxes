import React, { Component } from 'react';

import update from 'immutability-helper';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { hideSpinner, showSpinner } from 'actions';
import invoiceApi from 'api/services/InvoiceApi';
import ArrayField from 'components/form-elements/ArrayField';
import LabelField from 'components/form-elements/LabelField';
import ModalWrapper from 'components/form-elements/ModalWrapper';
import TextField from 'components/form-elements/TextField';
import { ORDER_URL, STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import Checkbox from 'utils/Checkbox';
import { getInvoiceDescription } from 'utils/form-values-utils';
import accountingFormat from 'utils/number-utils';
import Select from 'utils/Select';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

const FIELDS = {
  invoiceItems: {
    type: ArrayField,
    arrowsNavigation: true,
    maxTableHeight: 'calc(100vh - 500px)',
    overflowStyle: 'overlay',
    fields: {
      checked: {
        fieldKey: '',
        label: '',
        flexWidth: '0.4',
        type: ({
          // eslint-disable-next-line react/prop-types
          rowIndex, fieldValue, selectRow,
        }) => (
          <Checkbox
            id={rowIndex.toString()}
            disabled={false}
            className="ml-1"
            value={fieldValue.checked}
            onChange={(value) => selectRow(value, rowIndex)}
          />
        ),
      },
      orderNumber: {
        type: LabelField,
        label: 'react.invoice.orderNumber.label',
        defaultMessage: 'PO Number',
        flexWidth: '1',
        getDynamicAttr: ({ values, rowIndex }) => {
          const orderId = values && values.invoiceItems
              && values.invoiceItems[rowIndex]
              && values.invoiceItems[rowIndex].orderId;
          return { url: orderId ? ORDER_URL.show(orderId) : '' };
        },
      },
      shipmentNumber: {
        type: LabelField,
        label: 'react.invoice.shipmentNumber.label',
        defaultMessage: 'Shipment Number',
        flexWidth: '1',
        getDynamicAttr: ({ values, rowIndex }) => {
          const shipmentId = values && values.invoiceItems
              && values.invoiceItems[rowIndex]
              && values.invoiceItems[rowIndex].shipmentId;
          return { url: shipmentId ? STOCK_MOVEMENT_URL.show(shipmentId) : '' };
        },
      },
      budgetCode: {
        type: LabelField,
        label: 'react.invoice.budgetCode.label',
        defaultMessage: 'Budget Code',
        flexWidth: '1',
      },
      glCode: {
        type: LabelField,
        label: 'react.invoice.glCode.label',
        defaultMessage: 'GL Code',
        flexWidth: '1',
      },
      productCode: {
        type: LabelField,
        label: 'react.invoice.itemNumber.label',
        defaultMessage: 'Item No',
        flexWidth: '1',
      },
      description: {
        type: LabelField,
        label: 'react.invoice.description.label',
        defaultMessage: 'Description',
        flexWidth: '3',
        attributes: {
          className: 'text-left',
        },
        getDynamicAttr: (params) => ({
          formatValue: () => {
            const { values, rowIndex } = params;
            const rowValue = values?.invoiceItems?.[rowIndex];
            // If it's not an adjustment, but product, and it has a synonym, display it
            // with a tooltip with the original name of the product
            return getInvoiceDescription(rowValue);
          },
        }),
      },
      supplierCode: {
        type: LabelField,
        label: 'react.invoice.supplierCode.label',
        defaultMessage: 'Supplier Code',
        flexWidth: '1',
      },
      quantity: {
        type: LabelField,
        label: 'react.invoice.quantity.label',
        defaultMessage: 'Qty',
        flexWidth: '1',
      },
      quantityToInvoice: {
        type: TextField,
        label: 'react.invoice.quantityToInvoice.label',
        defaultMessage: 'Qty to Invoice',
        flexWidth: '1',
        attributes: {
          type: 'number',
        },
        getDynamicAttr: ({
          updateRow, values, rowIndex,
        }) => ({
          onChange: () => updateRow(values, rowIndex),
        }),
      },
      uom: {
        type: LabelField,
        label: 'react.invoice.uom.label',
        defaultMessage: 'UoM',
        flexWidth: '1',
      },
      unitPrice: {
        type: LabelField,
        label: 'react.invoice.unitPrice.label',
        defaultMessage: 'Unit Price',
        flexWidth: '1',
        attributes: {
          formatValue: (value) => (value ? accountingFormat(value) : value),
        },
      },
    },
  },
};

function validate(values) {
  const errors = {};
  errors.invoiceItems = [];

  _.forEach(values.invoiceItems, (item, key) => {
    if (
      item.checked
      && (
        (_.toInteger(item.quantityToInvoice) > item.quantity)
        || _.toInteger(item.quantityToInvoice) <= 0
      )
    ) {
      errors.invoiceItems[key] = { quantityToInvoice: 'react.invoice.errors.quantityToInvoice.label' };
    }
  });

  return errors;
}

const INITIAL_STATE = {
  selectedInvoiceItems: null,
  formValues: { invoiceItems: [] },
  sortOrder: 0,
  orderNumberOptions: [],
  shipmentNumberOptions: [],
  selectedOrderNumbers: [],
  selectedShipmentNumbers: [],
};

class InvoiceItemsModal extends Component {
  constructor(props) {
    super(props);
    this.state = INITIAL_STATE;

    this.onOpen = this.onOpen.bind(this);
    this.onSave = this.onSave.bind(this);
    this.selectRow = this.selectRow.bind(this);
    this.updateRow = this.updateRow.bind(this);
  }

  onOpen() {
    return this.props.onOpen().then(() => {
      this.setState(INITIAL_STATE, () => {
        this.fetchInvoiceItemCandidates();
      });
    });
  }

  onSave() {
    this.props.showSpinner();
    const { invoiceId } = this.props;
    const { selectedInvoiceItems } = this.state;

    const payload = {
      invoiceItems: _.map(selectedInvoiceItems, (item, key) => ({
        id: key,
        quantityToInvoice: _.toInteger(item.quantityToInvoice),
      })),
    };

    invoiceApi.saveInvoiceItems(invoiceId, payload)
      .then(() => {
        this.setState(INITIAL_STATE, () => {
          this.props.hideSpinner();
          this.props.onResponse({ startIndex: 0 });
        });
      })
      .catch(() => this.props.hideSpinner());
  }

  setSelectedOrders(selectedOrderNumbers) {
    this.setState({ selectedOrderNumbers }, () => this.fetchInvoiceItemCandidates());
  }

  setSelectedShipments(selectedShipmentNumbers) {
    this.setState({ selectedShipmentNumbers }, () => this.fetchInvoiceItemCandidates());
  }

  getSortOrder() {
    this.setState((prev) => ({
      sortOrder: prev.sortOrder + 1,
    }));

    return this.state.sortOrder;
  }

  selectRow(value, rowIndex) {
    const { formValues, selectedInvoiceItems } = this.state;
    let newState = {
      formValues: {
        invoiceItems: _.map(formValues.invoiceItems, (item, idx) => {
          if (rowIndex === idx) {
            return {
              ...item,
              checked: value,
              quantityToInvoice: value ? item.quantity : '',
              sortOrder: value ? item.sortOrder : '',
            };
          }
          return { ...item };
        }),
      },
    };
    if (!value) {
      delete selectedInvoiceItems[formValues.invoiceItems[rowIndex].id];
      newState = {
        ...newState,
        selectedInvoiceItems,
      };
    } else {
      newState = {
        ...newState,
        selectedInvoiceItems: {
          ...selectedInvoiceItems,
          [formValues.invoiceItems[rowIndex].id]: {
            ...formValues.invoiceItems[rowIndex],
            quantityToInvoice: value ? formValues.invoiceItems[rowIndex].quantity : '',
            sortOrder: value ? formValues.invoiceItems[rowIndex].sortOrder : '',
          },
        },
      };
    }
    this.setState(newState);
  }

  updateRow(values, index) {
    const { selectedInvoiceItems } = this.state;
    const item = values.invoiceItems[index];
    item.checked = true;
    this.setState({
      formValues: update(values, {
        invoiceItems: { [index]: { $set: item } },
      }),
      selectedInvoiceItems: {
        ...selectedInvoiceItems,
        [item.id]: { quantityToInvoice: item.quantityToInvoice, sortOrder: item.sortOrder },
      },
    });
  }

  fetchInvoiceItemCandidates() {
    const { selectedOrderNumbers, selectedShipmentNumbers, selectedInvoiceItems } = this.state;
    const { invoiceId } = this.props;

    const payload = {
      orderNumbers: _.map(selectedOrderNumbers, (orderNumber) => orderNumber.value),
      shipmentNumbers: _.map(selectedShipmentNumbers, (shipmentNumber) => shipmentNumber.value),
    };

    return invoiceApi.saveInvoiceItemsCandidates(invoiceId, payload)
      .then((resp) => {
        this.setState({
          formValues: {
            invoiceItems: _.map(resp.data.data, (item) => ({
              ...item,
              checked: selectedInvoiceItems && !!selectedInvoiceItems[item.id],
              quantityToInvoice: selectedInvoiceItems && selectedInvoiceItems[item.id] ? selectedInvoiceItems[item.id].quantityToInvoice : '',
              sortOrder: this.getSortOrder(),
            })),
          },
        }, () => {
          this.fetchOrderNumbers(invoiceId);
          this.fetchShipmentNumbers(invoiceId);
        });
      });
  }

  fetchOrderNumbers(invoiceId) {
    if (this.state.orderNumberOptions.length === 0) {
      invoiceApi.getInvoiceOrders(invoiceId)
        .then((resp) => {
          this.setState({
            orderNumberOptions: _.map(resp.data.data, (orderNumber) => (
              { value: orderNumber, label: orderNumber }
            )),
          });
        });
    }
  }

  fetchShipmentNumbers(invoiceId) {
    if (this.state.shipmentNumberOptions.length === 0) {
      invoiceApi.getInvoiceShipments(invoiceId)
        .then((resp) => {
          this.setState({
            shipmentNumberOptions: _.map(resp.data.data, (shipmentNumber) => (
              { value: shipmentNumber, label: shipmentNumber }
            )),
          });
        });
    }
  }

  checkAllVisibleItems(value) {
    const { formValues: formValuesFromState } = this.state;
    const formValues = update(formValuesFromState, {
      invoiceItems: {
        $apply: (items) => items.map((invoiceItem) => (
          {
            ...invoiceItem,
            checked: value,
            quantityToInvoice: value ? invoiceItem.quantity : '',
            sortOrder: value ? invoiceItem.sortOrder : '',
          }
        )),
      },
    });
    const selectedItems = this.state.selectedInvoiceItems ? this.state.selectedInvoiceItems : [];
    if (value) {
      // eslint-disable-next-line no-return-assign
      _.map(formValues.invoiceItems, (invoiceItem, key) =>
        selectedItems[invoiceItem.id] = {
          ...formValues.invoiceItems[key],
          quantityToInvoice: value ? formValues.invoiceItems[key].quantity : '',
          sortOrder: value ? formValues.invoiceItems[key].sortOrder : '',
        });
    } else {
      _.map(formValues.invoiceItems, (invoiceItem, key) => {
        delete selectedItems[formValues.invoiceItems[key].id];
      });
    }

    this.setState({ formValues, selectedInvoiceItems: { ...selectedItems } });
  }

  checkSelected() {
    return _.every(this.state.formValues.invoiceItems, (item) =>
      _.includes(_.keys(this.state.selectedInvoiceItems), item.id));
  }

  render() {
    const {
      formValues,
      orderNumberOptions,
      shipmentNumberOptions,
      selectedOrderNumbers,
      selectedShipmentNumbers,
    } = this.state;
    const {
      translate, btnOpenDisabled,
    } = this.props;

    return (
      <ModalWrapper
        onSave={this.onSave}
        fields={FIELDS}
        validate={validate}
        initialValues={formValues}
        title="react.invoice.addInvoiceItems.label"
        defaultTitleMessage="Add invoice items"
        formProps={{
          selectRow: this.selectRow,
          updateRow: this.updateRow,
        }}
        renderButton={({ openModal }) => (
          <button
            type="button"
            className="btn-xs"
            disabled={btnOpenDisabled}
            onClick={() => {
              this.onOpen().then(() => openModal());
            }}
          >
            <Translate
              id="react.default.button.addLines.label"
              defaultMessage="Add lines"
            />
          </button>
        )}
        btnSaveText="react.invoice.addInvoiceItems.label"
        btnSaveDefaultText="Add invoice items"
        btnOpenDisabled={btnOpenDisabled}
        btnSaveDisabled={!_.find(formValues.invoiceItems, (item) => item.checked)}
      >
        <div className="d-flex mb-3 justify-content-start align-items-center w-100 combined-shipment-filter">
          <Select
            fieldName="orderNumber"
            placeholder={translate('react.invoice.selectOrders.label', 'Select orders...')}
            value={selectedOrderNumbers}
            multi
            options={orderNumberOptions}
            showValueTooltip
            onChange={(value) => this.setSelectedOrders(value)}
            classes=""
            cache={false}
          />
          &nbsp;
          <Select
            fieldName="shipmentNumber"
            placeholder={translate('react.invoice.selectShipments.label', 'Select shipments...')}
            value={selectedShipmentNumbers}
            multi
            options={shipmentNumberOptions}
            showValueTooltip
            onChange={(value) => this.setSelectedShipments(value)}
            classes=""
            cache={false}
          />
        </div>
        <div>
          <Checkbox
            disabled={false}
            className="m-3"
            checked={this.checkSelected()}
            onChange={(value) => this.checkAllVisibleItems(value)}
          />
        </div>
      </ModalWrapper>
    );
  }
}

const mapStateToProps = (state) => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default (connect(mapStateToProps, { showSpinner, hideSpinner })(InvoiceItemsModal));

InvoiceItemsModal.propTypes = {
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  translate: PropTypes.func.isRequired,
  onResponse: PropTypes.func.isRequired,
  onOpen: PropTypes.func.isRequired,
  invoiceId: PropTypes.string.isRequired,
  btnOpenDisabled: PropTypes.bool,
};

InvoiceItemsModal.defaultProps = {
  btnOpenDisabled: false,
};
