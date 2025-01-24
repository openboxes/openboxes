import React, { Component } from 'react';

import update from 'immutability-helper';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { hideSpinner, showSpinner } from 'actions';
import ArrayField from 'components/form-elements/ArrayField';
import LabelField from 'components/form-elements/LabelField';
import ModalWrapper from 'components/form-elements/ModalWrapper';
import TextField from 'components/form-elements/TextField';
import ProductSelect from 'components/product-select/ProductSelect';
import { ORDER_URL } from 'consts/applicationUrls';
import apiClient from 'utils/apiClient';
import Checkbox from 'utils/Checkbox';
import { formatProductSupplierSubtext } from 'utils/form-values-utils';
import { debounceProductsInOrders } from 'utils/option-utils';
import Select from 'utils/Select';
import { translateWithDefaultMessage } from 'utils/Translate';

const FIELDS = {
  orderItems: {
    type: ArrayField,
    arrowsNavigation: true,
    maxTableHeight: 'calc(100vh - 500px)',
    fields: {
      checked: {
        fieldKey: '',
        label: 'react.stockMovement.selectAll.label',
        defaultMessage: 'Select All',
        flexWidth: '0.4',
        headerAlign: 'right',
        getDynamicAttr: ({ selectAllItems, allItemsSelected }) => ({
          headerHtml: () => (
            <input
              type="checkbox"
              className="mt-1"
              checked={allItemsSelected}
              onClick={selectAllItems}
            />
          ),
        }),
        type: ({
          // eslint-disable-next-line react/prop-types
          rowIndex, fieldValue, selectRow,
        }) => (
          <Checkbox
            id={rowIndex.toString()}
            disabled={false}
            style={{ marginLeft: '1.1rem' }}
            value={fieldValue.checked}
            onChange={(value) => selectRow(value, rowIndex)}
          />
        ),
      },
      orderNumber: {
        type: LabelField,
        label: 'react.stockMovement.orderNumber.label',
        defaultMessage: 'PO Number',
        flexWidth: '1',
        fieldKey: '',
        getDynamicAttr: ({
          fieldValue,
        }) => ({
          url: fieldValue?.orderId ? ORDER_URL.show(fieldValue.orderId) : '',
        }),
        attributes: {
          formatValue: (fieldValue) => fieldValue && fieldValue.orderNumber,
        },
      },
      productCode: {
        type: LabelField,
        label: 'react.stockMovement.productCode.label',
        defaultMessage: 'Product Code',
        flexWidth: '1',
      },
      productName: {
        type: LabelField,
        label: 'react.stockMovement.productName.label',
        defaultMessage: 'Product name',
        flexWidth: '3',
        attributes: {
          className: 'text-left ml-1',
        },
        getDynamicAttr: ({ values, rowIndex }) => {
          const orderItem = values.orderItems[rowIndex];
          const productSupplierName = formatProductSupplierSubtext({
            name: orderItem?.productSupplierName,
          });

          return {
            color: orderItem?.color,
            showValueTooltip: true,
            tooltipValue: [orderItem?.productName, productSupplierName].join(' ')?.trim(),
            formatValue: () => orderItem?.displayName || orderItem?.productName,
          };
        },
      },
      supplierCode: {
        type: LabelField,
        label: 'react.stockMovement.supplierCode.label',
        defaultMessage: 'Supplier code',
        flexWidth: '1',
      },
      budgetCode: {
        type: LabelField,
        label: 'react.stockMovement.budgetCode.label',
        defaultMessage: 'Budget Code',
        flexWidth: '1',
      },
      recipient: {
        type: LabelField,
        label: 'react.stockMovement.recipient.label',
        defaultMessage: 'Recipient',
        flexWidth: '1.5',
        attributes: {
          showValueTooltip: true,
        },
      },
      quantityAvailable: {
        type: LabelField,
        label: 'react.stockMovement.quantityAvailable.label',
        defaultMessage: 'Quantity Available',
        flexWidth: '1',
      },
      quantityToShip: {
        type: TextField,
        label: 'react.stockMovement.quantityToShip.label',
        defaultMessage: 'Quantity to Ship',
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
        label: 'react.stockMovement.uom.label',
        defaultMessage: 'UoM',
        flexWidth: '1',
      },
    },
  },
};

function validate(values) {
  const errors = {};
  errors.orderItems = [];

  _.forEach(values.orderItems, (item, key) => {
    if (
      item.checked
      && (
        (_.toInteger(item.quantityToShip) > item.quantityAvailable)
        || _.toInteger(item.quantityToShip) < 0
      )
    ) {
      errors.orderItems[key] = { quantityToShip: 'react.combinedShipments.errors.quantityToShip.label' };
    }
  });

  return errors;
}

const INITIAL_STATE = {
  orderNumberOptions: [],
  selectedOrders: [],
  selectedProductId: '',
  selectedOrderItems: [],
  formValues: { orderItems: [] },
  sortOrder: 0,
};

class CombinedShipmentItemsModal extends Component {
  constructor(props) {
    super(props);
    this.state = INITIAL_STATE;

    this.onOpen = this.onOpen.bind(this);
    this.onSave = this.onSave.bind(this);
    this.selectRow = this.selectRow.bind(this);
    this.updateRow = this.updateRow.bind(this);
    this.selectAllItems = this.selectAllItems.bind(this);
    this.allItemsSelected = this.allItemsSelected.bind(this);

    this.debounceProductsInOrders = debounceProductsInOrders(
      this.props.debounceTime,
      this.props.minSearchLength,
      this.props.vendor,
      this.props.destination,
    );
  }

  onOpen() {
    this.props.onOpen().then(() => {
      this.setState(INITIAL_STATE, () => {
        this.getOrderNumberOptions();
        this.fetchOrderItems();
      });
    });
  }

  onSave() {
    this.props.showSpinner();
    const { shipment } = this.props;
    const { selectedOrderItems } = this.state;
    const payload = {
      itemsToAdd: _.map(selectedOrderItems, (item, key) => ({
        orderItemId: key,
        quantityToShip: _.toInteger(item.quantityToShip),
        sortOrder: _.toInteger(item.sortOrder),
      })),
    };
    const url = `/api/combinedShipmentItems/addToShipment/${shipment}`;

    apiClient.post(url, payload)
      .then(() => {
        this.setState(INITIAL_STATE, () => {
          this.props.hideSpinner();
          this.props.onResponse(this.props.overrideFormValue);
        });
      })
      .catch(() => this.props.hideSpinner());
  }

  getSortOrder() {
    this.setState((prev) => ({
      sortOrder: prev.sortOrder + 1,
    }));

    return this.state.sortOrder;
  }

  getOrderNumberOptions() {
    const { vendor, destination } = this.props;
    const url = `/api/orderNumberOptions?vendor=${vendor}&destination=${destination}`;
    apiClient.get(url).then((resp) => this.setState({ orderNumberOptions: resp.data.data }));
  }

  setSelectedOrders(selectedOrders) {
    this.setState({ selectedOrders }, () => this.fetchOrderItems());
  }

  setSelectedProduct(selectedProduct) {
    this.setState({
      selectedProductId: selectedProduct ? selectedProduct.id : '',
    }, () => this.fetchOrderItems());
  }

  fetchOrderItems() {
    const { selectedOrders, selectedProductId, selectedOrderItems } = this.state;
    const { vendor, destination } = this.props;
    const url = '/api/combinedShipmentItems/findOrderItems';
    const payload = {
      orderIds: _.map(selectedOrders, (o) => o.id),
      productId: selectedProductId,
      vendor,
      destination,
    };
    return apiClient.post(url, payload).then((resp) => this.setState({
      formValues: {
        orderItems: _.map(resp.data.orderItems, (item) => ({
          ...item,
          checked: !!selectedOrderItems[item.orderItemId],
          quantityToShip: selectedOrderItems[item.orderItemId] ? selectedOrderItems[item.orderItemId].quantityToShip : '',
          sortOrder: this.getSortOrder(),
        })),
      },
    }));
  }

  selectRow(value, rowIndex) {
    const { formValues, selectedOrderItems } = this.state;
    let newState = {
      formValues: {
        orderItems: _.map(formValues.orderItems, (item, idx) => {
          if (rowIndex === idx) {
            return {
              ...item,
              checked: value,
              quantityToShip: value ? item.quantityAvailable : '',
              sortOrder: value ? item.sortOrder : '',
            };
          }
          return { ...item };
        }),
      },
    };
    if (!value) {
      delete selectedOrderItems[formValues.orderItems[rowIndex].orderItemId];
      newState = {
        ...newState,
        selectedOrderItems,
      };
    } else {
      newState = {
        ...newState,
        selectedOrderItems: {
          ...selectedOrderItems,
          [formValues.orderItems[rowIndex].orderItemId]: {
            quantityToShip: value ? formValues.orderItems[rowIndex].quantityAvailable : '',
            sortOrder: value ? formValues.orderItems[rowIndex].sortOrder : '',
          },
        },
      };
    }
    this.setState(newState);
  }

  updateRow(values, index) {
    const { selectedOrderItems } = this.state;
    const item = values.orderItems[index];
    item.checked = true;
    this.setState({
      formValues: update(values, {
        orderItems: { [index]: { $set: item } },
      }),
      selectedOrderItems: {
        ...selectedOrderItems,
        [item.orderItemId]: { quantityToShip: item.quantityToShip, sortOrder: item.sortOrder },
      },
    });
  }

  allItemsSelected() {
    return this.state.formValues.orderItems.every((item) => item.checked);
  }

  selectAllItems() {
    const allItemsSelected = this.allItemsSelected();
    const { orderItems } = this.state.formValues;
    this.setState({
      formValues: {
        orderItems: orderItems.map((item) => ({
          ...item,
          checked: !allItemsSelected,
          quantityToShip: !allItemsSelected ? item.quantityAvailable : '',
          sortOrder: !allItemsSelected ? item.sortOrder : '',
        })),
      },
    }, () => this.updateAllSelectedItems());
  }

  updateAllSelectedItems() {
    const { orderItems } = this.state.formValues;
    const allItemsSelected = this.allItemsSelected();
    const updatedSelectedOrderItems = orderItems.reduce((acc, curr) => {
      if (allItemsSelected) {
        return {
          ...acc,
          [curr.orderItemId]: {
            quantityToShip: curr.quantityAvailable,
            sortOrder: curr.sortOrder,
          },
        };
      }
      return acc;
    }, {});

    this.setState({ selectedOrderItems: updatedSelectedOrderItems });
  }

  render() {
    const {
      orderNumberOptions, selectedOrders, formValues,
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
        title="react.combinedShipments.addItemsToShipment.label"
        defaultTitleMessage="Add items to shipment"
        formProps={{
          selectRow: this.selectRow,
          updateRow: this.updateRow,
          selectAllItems: this.selectAllItems,
          allItemsSelected: this.allItemsSelected(),
        }}
        btnSaveText="react.combinedShipments.addItemsToShipment.label"
        btnSaveDefaultText="Add items to shipment"
        btnOpenText={btnOpenText}
        btnOpenDefaultText={btnOpenDefaultText}
        btnOpenDisabled={btnOpenDisabled}
        btnSaveDisabled={!_.find(formValues.orderItems, (item) => item.checked)}
      >
        <div className="d-flex mb-3 justify-content-start align-items-center w-100 combined-shipment-filter">
          <Select
            fieldName="orderNumber"
            placeholder={translate('react.combinedShipments.selectOrders.label', 'Select orders...')}
            value={selectedOrders}
            multi
            options={orderNumberOptions}
            showValueTooltip
            onChange={(value) => this.setSelectedOrders(value)}
            valueKey="id"
            labelKey="orderNumber"
            classes=""
            cache={false}
          />
          &nbsp;
          <ProductSelect
            showSelectedOptionColor
            placeholder={translate('react.combinedShipments.selectProduct.label', 'Select product...')}
            loadOptions={this.debounceProductsInOrders}
            onChange={(value) => this.setSelectedProduct(value)}
          />
        </div>
      </ModalWrapper>
    );
  }
}

const mapStateToProps = (state) => ({
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps, { showSpinner, hideSpinner })(CombinedShipmentItemsModal);

CombinedShipmentItemsModal.propTypes = {
  vendor: PropTypes.string.isRequired,
  destination: PropTypes.string.isRequired,
  shipment: PropTypes.string.isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  translate: PropTypes.func.isRequired,
  onResponse: PropTypes.func.isRequired,
  onOpen: PropTypes.func.isRequired,
  btnOpenText: PropTypes.string,
  btnOpenDefaultText: PropTypes.string,
  btnOpenDisabled: PropTypes.bool,
  overrideFormValue: PropTypes.func.isRequired,
};

CombinedShipmentItemsModal.defaultProps = {
  btnOpenText: '',
  btnOpenDefaultText: '',
  btnOpenDisabled: false,
};
