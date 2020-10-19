import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import _ from 'lodash';

import ModalWrapper from '../../form-elements/ModalWrapper';
import LabelField from '../../form-elements/LabelField';
import Select from '../../../utils/Select';
import ArrayField from '../../form-elements/ArrayField';
import TextField from '../../form-elements/TextField';
import Checkbox from '../../../utils/Checkbox';
import apiClient from '../../../utils/apiClient';
import { showSpinner, hideSpinner } from '../../../actions';
import Translate from '../../../utils/Translate';
import { debounceProductsFetch } from '../../../utils/option-utils';
import renderHandlingIcons from '../../../utils/product-handling-icons';

const FIELDS = {
  orderItems: {
    type: ArrayField,
    arrowsNavigation: true,
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
            disabled={false}
            className="ml-4"
            value={fieldValue.checked}
            onChange={value => selectRow(value, rowIndex)}
          />),
      },
      orderNumber: {
        type: LabelField,
        label: 'react.stockMovement.orderNumber.label',
        defaultMessage: 'PO Number',
      },
      productCode: {
        type: LabelField,
        label: 'react.stockMovement.productCode.label',
        defaultMessage: 'Product Code',
      },
      productName: {
        type: LabelField,
        label: 'react.stockMovement.productName.label',
        defaultMessage: 'Product name',
      },
      budgetCode: {
        type: LabelField,
        label: 'react.stockMovement.budgetCode.label',
        defaultMessage: 'Budget Code',
      },
      recipient: {
        type: LabelField,
        label: 'react.stockMovement.recipient.label',
        defaultMessage: 'Recipient',
      },
      quantityAvailable: {
        type: LabelField,
        label: 'react.stockMovement.quantityAvailable.label',
        defaultMessage: 'Quantity Available',
      },
      quantityToShip: {
        type: TextField,
        label: 'react.stockMovement.quantityToShip.label',
        defaultMessage: 'Quantity to Ship',
        fixedWidth: '140px',
        attributes: {
          type: 'number',
        },
      },
      uom: {
        type: LabelField,
        label: 'react.stockMovement.uom.label',
        defaultMessage: 'UoM',
      },
    },
  },
};

function validate(values) {
  const errors = {};
  errors.orderItems = [];

  _.forEach(values.orderItems, (item, key) => {
    if (
      item.checked &&
      (
        (_.toInteger(item.quantityToShip) > item.quantityAvailable) ||
        _.toInteger(item.quantityToShip) < 0
      )
    ) {
      errors.orderItems[key] = { quantityRevised: 'react.stockMovement.errors.lowerQty.label' };
    }
  });

  return errors;
}

class CombinedShipmentItemsModal extends Component {
  constructor(props) {
    super(props);
    this.state = {
      orderNumberOptions: [],
      selectedOrders: [],
      selectedProductId: '',
      formValues: { orderItems: [] },
    };

    this.onOpen = this.onOpen.bind(this);
    this.onSave = this.onSave.bind(this);
    this.selectRow = this.selectRow.bind(this);

    this.debouncedProductsFetch = debounceProductsFetch(
      this.props.debounceTime,
      this.props.minSearchLength,
      '',
    );
  }

  onOpen() {
    this.setState({
      orderNumberOptions: [],
      selectedOrders: [],
      selectedProductId: '',
      formValues: { orderItems: [] },
    });
    this.getOrderNumberOptions();
  }

  onSave(values) {
    this.props.showSpinner();
    const { shipment } = this.props;
    const payload = {
      itemsToAdd: _.map(_.filter(values.orderItems, v => v.checked), item => ({
        ...item,
        quantityToShip: _.toInteger(item.quantityToShip),
      })),
    };
    const url = `/openboxes/api/combinedShipmentItems/addToShipment/${shipment}`;

    apiClient.post(url, payload);
    this.props.hideSpinner();
  }

  getOrderNumberOptions() {
    const { vendor, destination } = this.props;
    const url = `/openboxes/api/orderNumberOptions?vendor=${vendor}&destination=${destination}`;
    apiClient.get(url).then(resp => this.setState({ orderNumberOptions: resp.data.data }));
  }

  setSelectedOrders(selectedOrders) {
    this.setState({ selectedOrders });
  }

  setSelectedProduct(selectedProduct) {
    this.setState({ selectedProductId: selectedProduct ? selectedProduct.id : '' });
  }

  fetchOrderItems() {
    const { selectedOrders, selectedProductId } = this.state;
    const url = '/openboxes/api/combinedShipmentItems/findOrderItems';
    const payload = { orderIds: selectedOrders, productId: selectedProductId };
    return apiClient.post(url, payload).then(resp => this.setState({
      formValues: {
        orderItems: _.map(resp.data.orderItems, item => ({
          ...item,
          checked: false,
        })),
      },
    }));
  }

  selectRow(value, rowIndex) {
    this.setState({
      formValues: {
        orderItems: _.map(this.state.formValues.orderItems, (item, idx) => {
          if (rowIndex === idx) {
            return {
              ...item,
              checked: value,
              quantityToShip: value ? item.quantityAvailable : '',
            };
          }
          return { ...item };
        }),
      },
    });
  }

  render() {
    const { orderNumberOptions, selectedOrders } = this.state;

    return (
      <ModalWrapper
        onOpen={this.onOpen}
        onSave={this.onSave}
        fields={FIELDS}
        validate={validate}
        initialValues={this.state.formValues}
        title="react.combinedShipments.addItemsToShipment.label"
        defaultTitleMessage="Add items to shipment"
        formProps={{
          selectRow: this.selectRow,
        }}
        btnSaveText="react.combinedShipments.addItemsToShipment.label"
        btnSaveDefaultText="Add items to shipment"
      >
        <div className="d-flex mb-3 justify-content-start align-items-center w-100 combined-shipment-filter">
          <Select
            fieldName="orderNumber"
            value={selectedOrders}
            multi
            options={orderNumberOptions}
            showValueTooltip
            onChange={value => this.setSelectedOrders(value)}
            classes=""
            filterOption={options => options}
            cache={false}
            attributes={{
              openOnClick: false,
              autoload: false,
              filterOptions: options => options,
              cache: false,
              className: 'text-left w-100 mt-0',
            }}
          />
          &nbsp;
          <Select
            async
            options={[]}
            classes=""
            showValueTooltip
            loadOptions={this.debouncedProductsFetch}
            onChange={value => this.setSelectedProduct(value)}
            openOnClick={false}
            autoload={false}
            filterOption={options => options}
            cache={false}
            optionRenderer={option => (
              <strong style={{ color: option.color ? option.color : 'black' }} className="d-flex align-items-center">
                {option.label}
                &nbsp;
                {renderHandlingIcons(option.value ? option.value.handlingIcons : [])}
              </strong>
            )}
            valueRenderer={option => (
              <span className="d-flex align-items-center">
                <span className="text-truncate">
                  {option.label}
                </span>
                &nbsp;
                {renderHandlingIcons(option ? option.handlingIcons : [])}
              </span>
            )}
            attributes={{
              openOnClick: false,
              autoload: false,
              cache: false,
              className: 'text-left w-100 ml-2 mt-0',
            }}
          />
          &nbsp;
          <button type="button" className="btn btn-outline-primary" style={{ height: '36px' }} onClick={() => this.fetchOrderItems()} >
            <Translate id="react.combinedShipments.search.label" defaultMessage="Search" />
          </button>
        </div>
      </ModalWrapper>
    );
  }
}

const mapStateToProps = state => ({
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
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
  // onResponse: PropTypes.func.isRequired,
};
