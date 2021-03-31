import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import _ from 'lodash';
import update from 'immutability-helper';
import { getTranslate } from 'react-localize-redux';

import ModalWrapper from '../form-elements/ModalWrapper';
import LabelField from '../form-elements/LabelField';
import ArrayField from '../form-elements/ArrayField';
import TextField from '../form-elements/TextField';
import Checkbox from '../../utils/Checkbox';
import apiClient from '../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../actions';
import { translateWithDefaultMessage } from '../../utils/Translate';

const FIELDS = {
  invoiceItems: {
    type: ArrayField,
    arrowsNavigation: true,
    maxTableHeight: 'calc(100vh - 500px)',
    fields: {
      checked: {
        fieldKey: '',
        label: '',
        flexWidth: '3',
        type: ({
          // eslint-disable-next-line react/prop-types
          rowIndex, fieldValue, selectRow,
        }) => (
          <Checkbox
            id={rowIndex.toString()}
            disabled={false}
            className="ml-4"
            value={fieldValue.checked}
            onChange={value => selectRow(value, rowIndex)}
          />
        ),
      },
      orderNumber: {
        type: LabelField,
        label: 'react.invoice.orderNumber.label',
        defaultMessage: 'PO Number',
      },
      shipmentNumber: {
        type: LabelField,
        label: 'react.invoice.shipmentNumber.label',
        defaultMessage: 'Shipment Number',
      },
      budgetCode: {
        type: LabelField,
        label: 'react.invoice.budgetCode.label',
        defaultMessage: 'Budget Code',
      },
      glCode: {
        type: LabelField,
        label: 'react.invoice.glCode.label',
        defaultMessage: 'GL Code',
      },
      productCode: {
        type: LabelField,
        label: 'react.invoice.itemNumber.label',
        defaultMessage: 'Item No',
      },
      description: {
        type: LabelField,
        label: 'react.invoice.description.label',
        defaultMessage: 'Description',
      },
      quantity: {
        type: LabelField,
        label: 'react.invoice.quantity.label',
        defaultMessage: 'Qty',
      },
      quantityToInvoice: {
        type: TextField,
        label: 'react.invoice.quantityToInvoice.label',
        defaultMessage: 'Qty to Invoice',
        fixedWidth: '140px',
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
      },
      unitPrice: {
        type: LabelField,
        label: 'react.invoice.unitPrice.label',
        defaultMessage: 'Unit Price',
      },
    },
  },
};

function validate(values) {
  const errors = {};
  errors.invoiceItems = [];

  _.forEach(values.invoiceItems, (item, key) => {
    if (
      item.checked &&
      (
        (_.toInteger(item.quantityToInvoice) > item.quantity) ||
        _.toInteger(item.quantityToInvoice) < 0
      )
    ) {
      errors.invoiceItems[key] = { quantityToInvoice: 'react.invoice.errors.quantityToInvoice.label' };
    }
  });

  return errors;
}

const INITIAL_STATE = {
  orderNumberOptions: [],
  selectedOrderNumber: '',
  selectedShipmentNumber: '',
  selectedInvoiceItems: [],
  formValues: { invoiceItems: [] },
  sortOrder: 0,
};

/* eslint-disable */
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
    this.setState(INITIAL_STATE, () => {
      this.fetchInvoiceItemCandidates();
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
        sortOrder: _.toInteger(item.sortOrder),
      })),
    };
    const url = `/openboxes/api/invoices/${invoiceId}/addItems`;

    apiClient.post(url, payload)
    .then(() => {
      this.setState(INITIAL_STATE, () => {
        this.props.hideSpinner();
        this.props.onResponse({ startIndex: 0 });
      });
    })
    .catch(() => this.props.hideSpinner());
  }

  getSortOrder() {
    this.setState({
      sortOrder: this.state.sortOrder + 1,
    });

    return this.state.sortOrder;
  }

  setSelectedOrderNumber(selectedOrderNumber) {
    this.setState({ selectedOrderNumber }, () => this.fetchInvoiceItemCandidates());
  }

  setSelectedShipmentNumber(selectedShipmentNumber) {
    this.setState({ selectedShipmentNumber }, () => this.fetchInvoiceItemCandidates());
  }

  fetchInvoiceItemCandidates() {
    const { selectedOrderNumber, selectedShipmentNumber, selectedInvoiceItems } = this.state;
    const { invoiceId } = this.props;

    let url = `/openboxes/api/invoices/${invoiceId}/invoiceItemCandidates?`;
    url += selectedOrderNumber ? `&orderNumber=${selectedOrderNumber}` : '';
    url += selectedShipmentNumber ? `&shipmentNumber=${selectedShipmentNumber}` : '';

    return apiClient.get(url).then(resp => {
      this.setState({
        formValues: {
          invoiceItems: _.map(resp.data.data, item => ({
            ...item,
            checked: !!selectedInvoiceItems[item.id],
            quantityToInvoice: selectedInvoiceItems[item.id] ? selectedInvoiceItems[item.id].quantityToInvoice : '',
            sortOrder: this.getSortOrder(),
          })),
        },
      });
    });
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

  render() {
    const {
        selectedShipmentNumber, selectedOrderNumber, formValues,
    } = this.state;
    const {
        btnOpenText, btnOpenDefaultText, translate, btnOpenDisabled,
    } = this.props;

    return (
      <ModalWrapper
        onOpen={this.onOpen}
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
        btnSaveText="react.invoice.addInvoiceItems.label"
        btnSaveDefaultText="Add invoice items"
        btnOpenText={btnOpenText}
        btnOpenDefaultText={btnOpenDefaultText}
        btnOpenDisabled={btnOpenDisabled}
        btnSaveDisabled={!_.find(formValues.invoiceItems, item => item.checked)}
      >
        <div className="d-flex mb-3 justify-content-start align-items-center w-100 combined-shipment-filter">
          <input
            id="orderNumber"
            name="orderNumber"
            type="text"
            className="form-control"
            placeholder={translate('react.invoice.ordersNumber.label', 'Order Number')}
            value={selectedOrderNumber}
            onChange={(event) => this.setSelectedOrderNumber(event.target.value)}
          />
          &nbsp;
          <input
            id="shipmentNumber"
            name="shipmentNumber"
            type="text"
            className="form-control"
            placeholder={translate('react.invoice.shipmentNumber.label', 'Shipment Number')}
            value={selectedShipmentNumber}
            onChange={(event) => this.setSelectedShipmentNumber(event.target.value)}
          />
        </div>
      </ModalWrapper>
    );
  }
}

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default (connect(mapStateToProps, { showSpinner, hideSpinner })(InvoiceItemsModal));

InvoiceItemsModal.propTypes = {
  btnOpenText: PropTypes.string,
  btnOpenDefaultText: PropTypes.string,
};

InvoiceItemsModal.defaultProps = {
  btnOpenText: '',
  btnOpenDefaultText: '',
};
