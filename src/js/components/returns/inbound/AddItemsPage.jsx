import React, { Component } from 'react';

import arrayMutators from 'final-form-arrays';
import _ from 'lodash';
import moment from 'moment';
import PropTypes from 'prop-types';
import { confirmAlert } from 'react-confirm-alert';
import { Form } from 'react-final-form';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { fetchUsers, hideSpinner, showSpinner } from 'actions';
import ProductApi from 'api/services/ProductApi';
import stockTransferApi from 'api/services/StockTransferApi';
import ArrayField from 'components/form-elements/ArrayField';
import ButtonField from 'components/form-elements/ButtonField';
import DateField from 'components/form-elements/DateField';
import ProductSelectField from 'components/form-elements/ProductSelectField';
import SelectField from 'components/form-elements/SelectField';
import TextField from 'components/form-elements/TextField';
import notification from 'components/Layout/notifications/notification';
import ConfirmExpirationDateModal from 'components/modals/ConfirmExpirationDateModal';
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import DateFormat from 'consts/dateFormat';
import NotificationType from 'consts/notificationTypes';
import StockTransferStatus from 'consts/stockTransferStatus';
import { flattenRequest, parseResponse } from 'utils/apiClient';
import { renderFormField, setColumnValue } from 'utils/form-utils';
import Select from 'utils/Select';
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
    // eslint-disable-next-line react/prop-types
    addButton: ({ addRow, getSortOrder }) => (
      <button
        type="button"
        className="btn btn-outline-success btn-xs"
        onClick={() => addRow({ sortOrder: getSortOrder() })}
      >
        <span>
          <i className="fa fa-plus pr-2" />
          <Translate id="react.default.button.addLine.label" defaultMessage="Add line" />
        </span>
      </button>
    ),
    fields: {
      product: {
        type: ProductSelectField,
        label: 'react.inboundReturns.product.label',
        defaultMessage: 'Product',
        headerAlign: 'left',
        flexWidth: '4',
        required: true,
        getDynamicAttr: ({ rowIndex, originId, focusField }) => ({
          locationId: originId,
          onExactProductSelected: ({ product }) => {
            if (focusField && product) {
              focusField(rowIndex, 'quantity');
            }
          },
        }),
      },
      lotNumber: {
        type: TextField,
        label: 'react.inboundReturns.lot.label',
        defaultMessage: 'Lot',
        flexWidth: '1',
      },
      expirationDate: {
        type: DateField,
        label: 'react.inboundReturns.expiry.label',
        defaultMessage: 'Expiry',
        flexWidth: '1.5',
        attributes: {
          localizeDate: true,
          showLocalizedPlaceholder: true,
          localizedDateFormat: DateFormat.COMMON,
          autoComplete: 'off',
        },
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
      },
      recipient: {
        type: SelectField,
        label: 'react.inboundReturns.recipient.label',
        defaultMessage: 'Recipient',
        flexWidth: '1.5',
        getDynamicAttr: ({
          recipients, addRow, rowCount, rowIndex, getSortOrder, setRecipientValue, translate,
        }) => ({
          headerHtml: () => (
            <Select
              placeholder={translate('react.stockMovement.recipient.label', 'Recipient')}
              className="select-xs my-2"
              classNamePrefix="react-select"
              options={recipients}
              onChange={(val) => {
                if (val) {
                  setRecipientValue(val);
                }
              }}
            />
          ),
          options: recipients,
          onTabPress: rowCount === rowIndex + 1 ? () =>
            addRow({ sortOrder: getSortOrder() }) : null,
          arrowRight: rowCount === rowIndex + 1 ? () =>
            addRow({ sortOrder: getSortOrder() }) : null,
          arrowDown: rowCount === rowIndex + 1 ? () =>
            addRow({ sortOrder: getSortOrder() }) : null,
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
      isExpirationModalOpen: false,
      // Stores the resolve function for the ConfirmExpirationDateModal promise
      resolveExpirationModal: null,
      itemsWithMismatchedExpiry: [],
      // isFirstPageLoaded: false,
    };

    this.props.showSpinner();
    this.removeItem = this.removeItem.bind(this);
    this.getSortOrder = this.getSortOrder.bind(this);
    this.confirmEmptyQuantitySave = this.confirmEmptyQuantitySave.bind(this);
    this.confirmExpirationDateSave = this.confirmExpirationDateSave.bind(this);
    this.confirmEmptyQuantitySave = this.confirmEmptyQuantitySave.bind(this);
    this.confirmValidationErrorOnPreviousPage = this.confirmValidationErrorOnPreviousPage
      .bind(this);
    this.validate = this.validate.bind(this);
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
      this.props.fetchUsers();
    }
  }

  getSortOrder() {
    this.setState((prev) => ({
      sortOrder: prev.sortOrder + 100,
    }));

    return this.state.sortOrder;
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

  confirmValidationErrorOnExit() {
    return new Promise((resolve) => {
      confirmAlert({
        title: this.props.translate('react.inboundReturns.confirmSave.label', 'Confirm save'),
        message: this.props.translate(
          'react.inboundReturns.confirmExit.message',
          'Validation errors occurred. Are you sure you want to exit and lose unsaved data?',
        ),
        willUnmount: () => resolve(false),
        buttons: [
          {
            label: this.props.translate('react.default.yes.label', 'Yes'),
            onClick: () => resolve(true),
          },
          {
            label: this.props.translate('react.default.no.label', 'No'),
            onClick: () => resolve(false),
          },
        ],
      });
    });
  }

  confirmValidationErrorOnPreviousPage() {
    return new Promise((resolve) => {
      confirmAlert({
        title: this.props.translate('react.inboundReturns.confirmPreviousPage.label', 'Validation error'),
        message: this.props.translate('react.inboundReturns.confirmPreviousPage.message', 'Cannot save due to validation error on page'),
        willUnmount: () => resolve(false),
        buttons: [
          {
            label: this.props.translate('react.inboundReturns.confirmPreviousPage.correctError.label', 'Correct error'),
            onClick: () => resolve(true),
          },
          {
            label: this.props.translate('react.inboundReturns.confirmPreviousPage.continue.label', 'Continue (lose unsaved work)'),
            onClick: () => resolve(false),
          },
        ],
      });
    });
  }

  confirmEmptyQuantitySave() {
    return new Promise((resolve) => {
      confirmAlert({
        title: this.props.translate('react.inboundReturns.message.confirmSave.label', 'Confirm save'),
        message: this.props.translate(
          'react.inboundReturns.confirmSave.message',
          'Are you sure you want to save? There are some lines with empty or zero quantity, those lines will be deleted.',
        ),
        willUnmount: () => resolve(false),
        buttons: [
          {
            label: this.props.translate('react.default.yes.label', 'Yes'),
            onClick: () => resolve(true),
          },
          {
            label: this.props.translate('react.default.no.label', 'No'),
            onClick: () => resolve(false),
          },
        ],
      });
    });
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

  fetchInboundReturn() {
    if (this.props.match.params.inboundReturnId) {
      this.props.showSpinner();
      stockTransferApi.getStockTransfer(this.props.match.params.inboundReturnId)
        .then((resp) => {
          const inboundReturn = parseResponse(resp.data.data);
          const returnItems = inboundReturn.stockTransferItems.length
            ? inboundReturn.stockTransferItems
            : new Array(1).fill({ sortOrder: 100 });

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

  async nextPage(formValues) {
    const status = this.state.inboundReturn.status !== StockTransferStatus.PLACED
      ? StockTransferStatus.PLACED
      : null;
    this.saveStockTransferInCurrentStep(formValues, status)
      .then(() => this.props.nextPage(this.state.inboundReturn));
  }

  saveStockTransfer(returnItems, status) {
    const itemsToSave = _.filter(returnItems, (item) => item.product && item.quantity > 0);
    const payload = {
      ...this.state.inboundReturn,
      stockTransferItems: itemsToSave,
    };

    if (status) {
      payload.status = status;
    }

    if (payload.stockTransferItems.length) {
      this.props.showSpinner();
      return stockTransferApi.updateStockTransfer(
        this.props.match.params.inboundReturnId,
        flattenRequest(payload),
      )
        .then(() => this.fetchInboundReturn())
        .catch(() => Promise.reject(new Error(this.props.translate('react.inboundReturns.error.saveOrderItems.label', 'Could not save order items'))))
        .finally(() => this.props.hideSpinner());
    }

    return Promise.reject();
  }

  async saveStockTransferInCurrentStep(formValues, status = null) {
    const returnItems = _.filter(
      formValues.returnItems,
      (item) => !_.isEmpty(item) && item.product,
    );

    const hasEmptyOrZeroValues = _.some(returnItems, (item) => !item.quantity || item.quantity === '0');

    if (hasEmptyOrZeroValues) {
      const isConfirmed = await this.confirmEmptyQuantitySave();
      if (!isConfirmed) {
        return Promise.reject();
      }
    }

    const itemsWithLotAndExpirationDate = returnItems.filter(
      (it) => it.expirationDate && it.lotNumber,
    );
    const itemsWithMismatchedExpiry = [];
    // Trying to find at least one instance where the data that we are trying to save
    // does not match the expiration date of the existing inventoryItem in the system
    // eslint-disable-next-line no-restricted-syntax
    await Promise.all(
      itemsWithLotAndExpirationDate.map(async (it) => {
        const { data } = await ProductApi.getInventoryItem(it.product?.id, it.lotNumber);
        if (data.inventoryItem && data.inventoryItem.expirationDate !== it.expirationDate) {
          return itemsWithMismatchedExpiry.push({
            code: it.product?.productCode,
            product: it.product,
            lotNumber: it.lotNumber,
            previousExpiry: data.inventoryItem.expirationDate,
            newExpiry: it.expirationDate,
          });
        }
        return null;
      }),
    );

    if (itemsWithMismatchedExpiry.length > 0) {
      // After finding at least a single instance where expiration date we are trying to save
      // does not match the existing inventoryItem expiration date, we want to inform the user
      // that certain updates to th expiration date in the system will be performed
      const shouldUpdateExpirationDate =
        await this.confirmExpirationDateSave(itemsWithMismatchedExpiry);
      if (!shouldUpdateExpirationDate) {
        return Promise.reject();
      }
    }

    return this.saveStockTransfer(returnItems, status);
  }

  async save(formValues) {
    await this.saveStockTransferInCurrentStep(formValues);
    notification(NotificationType.SUCCESS)({
      message: this.props.translate(
        'react.inboundReturns.alert.saveSuccess.label',
        'Changes saved successfully',
      ),
    });
  }

  async saveAndExit(formValues) {
    const errors = this.validate(formValues).returnItems;
    const hasErrors = errors.length && errors.some((obj) => typeof obj === 'object' && !_.isEmpty(obj));

    if (hasErrors) {
      const isConfirmed = await this.confirmValidationErrorOnExit();
      if (!isConfirmed) {
        return;
      }
    } else {
      try {
        await this.saveStockTransferInCurrentStep(formValues);
      } catch (error) {
        return;
      }
    }

    window.location = STOCK_MOVEMENT_URL.show(this.props.match.params.inboundReturnId);
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
    return stockTransferApi.removeItem(itemId)
      .catch(() => {
        this.props.hideSpinner();
        return Promise.reject(new Error('react.inboundReturns.error.deleteOrderItem.label'));
      });
  }

  removeAll() {
    this.props.showSpinner();
    return stockTransferApi.removeAllItems(this.props.match.params.inboundReturnId)
      .then(() => this.fetchInboundReturn())
      .catch(() => {
        this.props.hideSpinner();
        return Promise.reject(new Error('react.inboundReturns.error.deleteOrderItem.label'));
      });
  }

  async previousPage(values, invalid) {
    if (invalid) {
      const correctErrors = await this.confirmValidationErrorOnPreviousPage();
      if (correctErrors) {
        return;
      }
    }
    await this.saveStockTransferInCurrentStep(values);
    this.props.previousPage(this.state.inboundReturn);
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
      <Form
        onSubmit={() => {}}
        validate={this.validate}
        mutators={{
          ...arrayMutators,
          setColumnValue,
        }}
        initialValues={this.state.formValues}
        render={({
          handleSubmit,
          values,
          invalid,
          form: { mutators },
        }) => (
          <div className="d-flex flex-column">
            <span className="buttons-container">
              <button
                type="button"
                disabled={invalid}
                onClick={() => this.save(values)}
                className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
              >
                <span>
                  <i className="fa fa-save pr-2" />
                  <Translate id="react.default.button.save.label" defaultMessage="Save" />
                </span>
              </button>
              <button
                type="button"
                disabled={invalid}
                onClick={() => this.saveAndExit(values)}
                className="float-right mb-1 btn btn-outline-secondary align-self-end ml-1 btn-xs"
              >
                <span>
                  <i className="fa fa-sign-out pr-2" />
                  <Translate id="react.default.button.saveAndExit.label" defaultMessage="Save and exit" />
                </span>
              </button>
              <button
                type="button"
                disabled={invalid}
                onClick={() => this.removeAll()}
                className="float-right mb-1 btn btn-outline-danger align-self-end btn-xs"
              >
                <span>
                  <i className="fa fa-remove pr-2" />
                  <Translate id="react.default.button.deleteAll.label" defaultMessage="Delete all" />
                </span>
              </button>
            </span>
            <form onSubmit={handleSubmit}>
              <div className="table-form">
                {_.map(FIELDS, (fieldConfig, fieldName) =>
                  renderFormField(fieldConfig, fieldName, {
                    recipients: this.props.recipients,
                    removeItem: this.removeItem,
                    getSortOrder: this.getSortOrder,
                    originId: this.props.initialValues.origin.id,
                    setRecipientValue: (val) => mutators.setColumnValue('returnItems', 'recipient', val),
                    translate: this.props.translate,
                  }))}
              </div>
              <div className="submit-buttons">
                <button
                  type="button"
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
                  disabled={
                    !_.some(values.returnItems, (item) => item.product && _.parseInt(item.quantity))
                  }
                >
                  <Translate id="react.default.button.next.label" defaultMessage="Next" />
                </button>
              </div>
            </form>
            <ConfirmExpirationDateModal
              isOpen={this.state.isExpirationModalOpen}
              itemsWithMismatchedExpiry={this.state.itemsWithMismatchedExpiry}
              onConfirm={() => this.handleExpirationModalResponse(true)}
              onCancel={() => this.handleExpirationModalResponse(false)}
            />
          </div>
        )}
      />
    );
  }
}

const mapStateToProps = (state) => ({
  recipients: state.users.data,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  inboundReturnsTranslationsFetched: state.session.fetchedTranslations.inboundReturns,
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
  translate: PropTypes.func.isRequired,
  inboundReturnsTranslationsFetched: PropTypes.bool.isRequired,
  minimumExpirationDate: PropTypes.string.isRequired,
  match: PropTypes.shape({
    params: PropTypes.shape({
      inboundReturnId: PropTypes.string,
    }),
  }).isRequired,
};
