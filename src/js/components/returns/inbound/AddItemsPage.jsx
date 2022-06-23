import React, { Component } from 'react';

import arrayMutators from 'final-form-arrays';
import update from 'immutability-helper';
import _ from 'lodash';
import moment from 'moment';
import PropTypes from 'prop-types';
import { confirmAlert } from 'react-confirm-alert';
import { Form } from 'react-final-form';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import Alert from 'react-s-alert';

import { fetchUsers, hideSpinner, showSpinner } from 'actions';
import ArrayField from 'components/form-elements/ArrayField';
import ButtonField from 'components/form-elements/ButtonField';
import DateField from 'components/form-elements/DateField';
import SelectField from 'components/form-elements/SelectField';
import TextField from 'components/form-elements/TextField';
import apiClient, { flattenRequest, parseResponse } from 'utils/apiClient';
import { renderFormField } from 'utils/form-utils';
import { debounceProductsFetch } from 'utils/option-utils';
import renderHandlingIcons from 'utils/product-handling-icons';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';


const DELETE_BUTTON_FIELD = {
  type: ButtonField,
  label: 'react.default.button.delete.label',
  defaultMessage: 'Delete',
  flexWidth: '1',
  fieldKey: '',
  buttonLabel: 'react.default.button.delete.label',
  buttonDefaultMessage: 'Delete',
  getDynamicAttr: ({
    fieldValue, removeItem, removeRow,
  }) => ({
    onClick: fieldValue && fieldValue.id ? () => {
      removeItem(fieldValue.id).then(() => {
        removeRow();
      });
    } : () => removeRow(),
    disabled: fieldValue && fieldValue.statusCode === 'SUBSTITUTED',
  }),
  attributes: {
    className: 'btn btn-outline-danger',
  },
};

const FIELDS = {
  returnItems: {
    type: ArrayField,
    arrowsNavigation: true,
    addButton: ({
      // eslint-disable-next-line react/prop-types
      addRow, getSortOrder,
    }) => (
      <button
        type="button"
        className="btn btn-outline-success btn-xs"
        onClick={() => addRow({
          sortOrder: getSortOrder(),
        })}
      ><span><i className="fa fa-plus pr-2" /><Translate id="react.default.button.addLine.label" defaultMessage="Add line" /></span>
      </button>
    ),
    fields: {
      product: {
        type: SelectField,
        label: 'react.inboundReturns.product.label',
        defaultMessage: 'Product',
        headerAlign: 'left',
        flexWidth: '4',
        required: true,
        attributes: {
          className: 'text-left',
          async: true,
          openOnClick: false,
          autoload: false,
          filterOptions: options => options,
          cache: false,
          options: [],
          showValueTooltip: true,
          optionRenderer: option => (
            <strong style={{ color: option.color || 'black' }} className="d-flex align-items-center">
              {option.label}
              &nbsp;
              {renderHandlingIcons(option.handlingIcons)}
            </strong>
          ),
          valueRenderer: option => (
            <span className="d-flex align-items-center">
              <span className="text-truncate">{option.label}</span>&nbsp;{renderHandlingIcons(option ? option.handlingIcons : [])}
            </span>
          ),
        },
        getDynamicAttr: ({
          debouncedProductsFetch, updateRow, rowIndex, values,
        }) => ({
          loadOptions: debouncedProductsFetch,
          onBlur: () => updateRow(values, rowIndex),
        }),
      },
      lotNumber: {
        type: TextField,
        label: 'react.inboundReturns.lot.label',
        defaultMessage: 'Lot',
        flexWidth: '1',
        getDynamicAttr: ({ rowIndex, values, updateRow }) => ({
          onBlur: () => updateRow(values, rowIndex),
        }),
      },
      expirationDate: {
        type: DateField,
        label: 'react.inboundReturns.expiry.label',
        defaultMessage: 'Expiry',
        flexWidth: '1.5',
        attributes: {
          dateFormat: 'MM/DD/YYYY',
          autoComplete: 'off',
          placeholderText: 'MM/DD/YYYY',
        },
        getDynamicAttr: ({ rowIndex, values, updateRow }) => ({
          onBlur: () => updateRow(values, rowIndex),
        }),
      },
      quantity: {
        type: TextField,
        label: 'react.inboundReturns.quantity.label',
        defaultMessage: 'Qty',
        flexWidth: '1',
        required: true,
        attributes: {
          type: 'number',
        },
        getDynamicAttr: ({ rowIndex, values, updateRow }) => ({
          onBlur: () => updateRow(values, rowIndex),
        }),
      },
      recipient: {
        type: SelectField,
        label: 'react.inboundReturns.recipient.label',
        defaultMessage: 'Recipient',
        flexWidth: '1.5',
        getDynamicAttr: ({
          recipients, addRow, rowCount, rowIndex, getSortOrder, updateRow, values,
        }) => ({
          options: recipients,
          onTabPress: rowCount === rowIndex + 1 ? () =>
            addRow({ sortOrder: getSortOrder() }) : null,
          arrowRight: rowCount === rowIndex + 1 ? () =>
            addRow({ sortOrder: getSortOrder() }) : null,
          arrowDown: rowCount === rowIndex + 1 ? () =>
            addRow({ sortOrder: getSortOrder() }) : null,
          onBlur: () => updateRow(values, rowIndex),
        }),
        attributes: {
          labelKey: 'name',
          openOnClick: false,
        },
      },
      deleteButton: DELETE_BUTTON_FIELD,
    },
  },
};

class AddItemsPage extends Component {
  constructor(props) {
    super(props);
    this.state = {
      inboundReturn: {},
      sortOrder: 0,
      formValues: { returnItems: [] },
      // isFirstPageLoaded: false,
    };

    this.props.showSpinner();
    this.removeItem = this.removeItem.bind(this);
    this.getSortOrder = this.getSortOrder.bind(this);
    this.confirmSave = this.confirmSave.bind(this);
    this.validate = this.validate.bind(this);
    this.updateRow = this.updateRow.bind(this);

    this.debouncedProductsFetch = debounceProductsFetch(
      this.props.debounceTime,
      this.props.minSearchLength,
      this.props.initialValues.origin.id,
    );
  }

  componentDidMount() {
    if (this.props.inboundReturnsTranslationsFetched) {
      this.dataFetched = true;
      this.fetchInboundReturn();
      this.props.fetchUsers();
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.inboundReturnsTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;
      this.fetchInboundReturn();

      if (!this.props.recipientsFetched) {
        this.props.fetchUsers();
      }
    }
  }

  getSortOrder() {
    this.setState({
      sortOrder: this.state.sortOrder + 100,
    });

    return this.state.sortOrder;
  }

  updateRow(values, index) {
    const item = values.returnItems[index];
    this.setState({
      formValues: update(values, {
        returnItems: { [index]: { $set: item } },
      }),
    });
  }

  dataFetched = false;

  validate(values) {
    const errors = {};
    errors.returnItems = [];
    const date = moment(this.props.minimumExpirationDate, 'MM/DD/YYYY');

    _.forEach(values.returnItems, (item, key) => {
      errors.returnItems[key] = {};
      if (!_.isNil(item.product) && (!item.quantity || item.quantity < 0)) {
        errors.returnItems[key] = { quantity: 'react.inboundReturns.error.enterQuantity.label' };
      }
      const dateRequested = moment(item.expirationDate, 'MM/DD/YYYY');
      if (date.diff(dateRequested) > 0) {
        errors.returnItems[key] = { expirationDate: 'react.inboundReturns.error.invalidDate.label' };
      }
      if (item.expirationDate && (_.isNil(item.lotNumber) || _.isEmpty(item.lotNumber))) {
        errors.returnItems[key] = { lotNumber: 'react.inboundReturns.error.expiryWithoutLot.label' };
      }
      if (!_.isNil(item.product) && item.product.lotAndExpiryControl) {
        if (!item.expirationDate && (_.isNil(item.lotNumber) || _.isEmpty(item.lotNumber))) {
          errors.returnItems[key] = {
            expirationDate: 'react.inboundReturns.error.lotAndExpiryControl.label',
            lotNumber: 'react.inboundReturns.error.lotAndExpiryControl.label',
          };
        } else if (!item.expirationDate) {
          errors.returnItems[key] = { expirationDate: 'react.inboundReturns.error.lotAndExpiryControl.label' };
        } else if (_.isNil(item.lotNumber) || _.isEmpty(item.lotNumber)) {
          errors.returnItems[key] = { lotNumber: 'react.inboundReturns.error.lotAndExpiryControl.label' };
        }
      }
    });
    return errors;
  }

  confirmSave(onConfirm) {
    confirmAlert({
      title: this.props.translate('react.inboundReturns.message.confirmSave.label', 'Confirm save'),
      message: this.props.translate(
        'react.inboundReturns.confirmSave.message',
        'Are you sure you want to save? There are some lines with empty or zero quantity, those lines will be deleted.',
      ),
      buttons: [
        {
          label: this.props.translate('react.default.yes.label', 'Yes'),
          onClick: onConfirm,
        },
        {
          label: this.props.translate('react.default.no.label', 'No'),
        },
      ],
    });
  }

  fetchInboundReturn() {
    if (this.props.match.params.inboundReturnId) {
      this.props.showSpinner();
      const url = `/openboxes/api/stockTransfers/${this.props.match.params.inboundReturnId}`;
      apiClient.get(url)
        .then((resp) => {
          const inboundReturn = parseResponse(resp.data.data);
          const returnItems = inboundReturn.stockTransferItems.length > 0 ?
            _.map(inboundReturn.stockTransferItems, item => ({
              ...item,
              product: {
                ...item.product,
                label: `${item.product.productCode} ${item.product.name}`,
              },
            })) : new Array(1).fill({ sortOrder: 100 });

          const sortOrder = _.toInteger(_.last(returnItems).sortOrder) + 100;

          this.setState({
            inboundReturn,
            formValues: { returnItems },
            sortOrder,
          }, () => this.props.hideSpinner());
        })
        .catch(() => this.props.hideSpinner());
    }
  }

  nextPage(formValues) {
    const returnItems = _.filter(formValues.returnItems, val => !_.isEmpty(val) && val.product);

    if (_.some(returnItems, item => !item.quantity || item.quantity === '0')) {
      this.confirmSave(() =>
        this.saveStockTransfer(returnItems, this.state.inboundReturn.status !== 'PLACED' ? 'PLACED' : null)
          .then(() => {
            this.props.nextPage(this.state.inboundReturn);
          })
          .catch(() => this.props.hideSpinner()));
    } else {
      this.saveStockTransfer(returnItems, this.state.inboundReturn.status !== 'PLACED' ? 'PLACED' : null)
        .then(() => {
          this.props.nextPage(this.state.inboundReturn);
        })
        .catch(() => this.props.hideSpinner());
    }
  }

  saveStockTransferInCurrentStep(returnItems) {
    this.props.showSpinner();
    return this.saveStockTransfer(returnItems, null)
      .catch(() => this.props.hideSpinner());
  }

  saveStockTransfer(returnItems, status) {
    const itemsToSave = _.filter(returnItems, item => item.product && item.quantity > 0);
    const updateItemsUrl = `/openboxes/api/stockTransfers/${this.props.match.params.inboundReturnId}`;
    const payload = {
      ...this.state.inboundReturn,
      stockTransferItems: itemsToSave,
    };

    if (status) {
      payload.status = status;
    }

    if (payload.stockTransferItems.length) {
      this.props.showSpinner();
      return apiClient.post(updateItemsUrl, flattenRequest(payload))
        .then(() => this.fetchInboundReturn())
        .catch(() => {
          this.props.hideSpinner();
          return Promise.reject(new Error(this.props.translate('react.inboundReturns.error.saveOrderItems.label', 'Could not save order items')));
        });
    }

    return Promise.reject();
  }

  save(formValues) {
    const returnItems = _.filter(formValues.returnItems, item => !_.isEmpty(item));

    if (_.some(returnItems, item => !item.quantity || item.quantity === '0')) {
      this.confirmSave(() => this.saveItems(returnItems));
    } else {
      this.saveItems(returnItems);
    }
  }

  saveAndExit(formValues) {
    const errors = this.validate(formValues).returnItems;
    if (errors.length && errors.every(obj => typeof obj === 'object' && _.isEmpty(obj))) {
      this.saveStockTransferInCurrentStep(formValues.returnItems)
        .then(() => {
          window.location = `/openboxes/stockMovement/show/${this.props.match.params.inboundReturnId}`;
        });
    } else {
      confirmAlert({
        title: this.props.translate('react.inboundReturns.confirmSave.label', 'Confirm save'),
        message: this.props.translate(
          'react.inboundReturns.confirmExit.message',
          'Validation errors occurred. Are you sure you want to exit and lose unsaved data?',
        ),
        buttons: [
          {
            label: this.props.translate('react.default.yes.label', 'Yes'),
            onClick: () => { window.location = `/openboxes/stockMovement/show/${this.props.match.params.inboundReturnId}`; },
          },
          {
            label: this.props.translate('react.default.no.label', 'No'),
          },
        ],
      });
    }
  }

  saveItems(returnItems) {
    this.props.showSpinner();

    this.saveStockTransferInCurrentStep(returnItems)
      .then(() => {
        this.props.hideSpinner();
        Alert.success(this.props.translate('react.inboundReturns.alert.saveSuccess.label', 'Changes saved successfully'), { timeout: 3000 });
      })
      .catch(() => this.props.hideSpinner());
  }

  refresh() {
    confirmAlert({
      title: this.props.translate('react.inboundReturns.message.confirmRefresh.label', 'Confirm refresh'),
      message: this.props.translate(
        'react.inboundReturns.confirmRefresh.message',
        'Are you sure you want to refresh? Your progress since last save will be lost.',
      ),
      buttons: [
        {
          label: this.props.translate('react.default.yes.label', 'Yes'),
          onClick: () => this.fetchInboundReturn(),
        },
        {
          label: this.props.translate('react.default.no.label', 'No'),
        },
      ],
    });
  }

  removeItem(itemId) {
    const removeItemsUrl = `/openboxes/api/stockTransferItems/${itemId}`;

    return apiClient.delete(removeItemsUrl)
      .catch(() => {
        this.props.hideSpinner();
        return Promise.reject(new Error('react.inboundReturns.error.deleteOrderItem.label'));
      });
  }

  removeAll() {
    this.props.showSpinner();
    const removeItemsUrl = `/openboxes/api/stockTransfers/${this.props.match.params.inboundReturnId}/removeAllItems`;

    return apiClient.delete(removeItemsUrl)
      .then(() => this.fetchInboundReturn())
      .catch(() => {
        this.props.hideSpinner();
        return Promise.reject(new Error('react.inboundReturns.error.deleteOrderItem.label'));
      });
  }

  previousPage(values, invalid) {
    if (!invalid) {
      this.saveStockTransferInCurrentStep(values.returnItems)
        .then(() => this.props.previousPage(this.state.inboundReturn));
    } else {
      confirmAlert({
        title: this.props.translate('react.inboundReturns.confirmPreviousPage.label', 'Validation error'),
        message: this.props.translate('react.inboundReturns.confirmPreviousPage.message', 'Cannot save due to validation error on page'),
        buttons: [
          {
            label: this.props.translate('react.inboundReturns.confirmPreviousPage.correctError.label', 'Correct error'),
          },
          {
            label: this.props.translate('react.inboundReturns.confirmPreviousPage.continue.label', 'Continue (lose unsaved work)'),
            onClick: () => this.props.previousPage(this.state.inboundReturn),
          },
        ],
      });
    }
  }

  render() {
    return (
      <Form
        onSubmit={() => {}}
        validate={this.validate}
        mutators={{ ...arrayMutators }}
        initialValues={this.state.formValues}
        render={({ handleSubmit, values, invalid }) => (
          <div className="d-flex flex-column">
            <span className="buttons-container">
              <button
                type="button"
                disabled={invalid}
                onClick={() => this.save(values)}
                className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
              >
                <span><i className="fa fa-save pr-2" /><Translate id="react.default.button.save.label" defaultMessage="Save" /></span>
              </button>
              <button
                type="button"
                disabled={invalid}
                onClick={() => this.saveAndExit(values)}
                className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
              >
                <span><i className="fa fa-sign-out pr-2" /><Translate id="react.default.button.saveAndExit.label" defaultMessage="Save and exit" /></span>
              </button>
              <button
                type="button"
                disabled={invalid}
                onClick={() => this.removeAll()}
                className="float-right mb-1 btn btn-outline-danger align-self-end btn-xs"
              >
                <span><i className="fa fa-remove pr-2" /><Translate id="react.default.button.deleteAll.label" defaultMessage="Delete all" /></span>
              </button>
            </span>
            <form onSubmit={handleSubmit}>
              <div className="table-form">
                {_.map(FIELDS, (fieldConfig, fieldName) =>
                  renderFormField(fieldConfig, fieldName, {
                    recipients: this.props.recipients,
                    removeItem: this.removeItem,
                    debouncedProductsFetch: this.debouncedProductsFetch,
                    getSortOrder: this.getSortOrder,
                    updateRow: this.updateRow,
                    values,
                  }))}
              </div>
              <div className="submit-buttons">
                <button
                  type="submit"
                  onClick={() => this.previousPage(values, invalid)}
                  className="btn btn-outline-primary btn-form btn-xs"
                >
                  <Translate id="react.default.button.previous.label" defaultMessage="Previous" />
                </button>
                <button
                  type="submit"
                  onClick={() => {
                    if (!invalid) {
                      this.nextPage(values);
                    }
                  }}
                  className="btn btn-outline-primary btn-form float-right btn-xs"
                  disabled={!_.some(values.returnItems, item => !_.isEmpty(item))}
                ><Translate id="react.default.button.next.label" defaultMessage="Next" />
                </button>
              </div>
            </form>
          </div>
        )}
      />
    );
  }
}

const mapStateToProps = state => ({
  recipients: state.users.data,
  recipientsFetched: state.users.fetched,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  inboundReturnsTranslationsFetched: state.session.fetchedTranslations.inboundReturns,
  debounceTime: state.session.searchConfig.debounceTime,
  minSearchLength: state.session.searchConfig.minSearchLength,
  minimumExpirationDate: state.session.minimumExpirationDate,
});

export default (connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchUsers,
})(AddItemsPage));

AddItemsPage.propTypes = {
  initialValues: PropTypes.shape({
    origin: PropTypes.shape({
      id: PropTypes.string,
    }),
  }).isRequired,
  previousPage: PropTypes.func.isRequired,
  nextPage: PropTypes.func.isRequired,
  showSpinner: PropTypes.func.isRequired,
  hideSpinner: PropTypes.func.isRequired,
  fetchUsers: PropTypes.func.isRequired,
  recipients: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  recipientsFetched: PropTypes.bool.isRequired,
  translate: PropTypes.func.isRequired,
  inboundReturnsTranslationsFetched: PropTypes.bool.isRequired,
  debounceTime: PropTypes.number.isRequired,
  minSearchLength: PropTypes.number.isRequired,
  minimumExpirationDate: PropTypes.string.isRequired,
  match: PropTypes.shape({
    params: PropTypes.shape({
      inboundReturnId: PropTypes.string,
    }),
  }).isRequired,
};
