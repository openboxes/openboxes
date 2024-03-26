import React, { Component } from 'react';

import arrayMutators from 'final-form-arrays';
import update from 'immutability-helper';
import fileDownload from 'js-file-download';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { confirmAlert } from 'react-confirm-alert';
import { Form } from 'react-final-form';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import { hideSpinner, showSpinner } from 'actions';
import ArrayField from 'components/form-elements/ArrayField';
import LabelField from 'components/form-elements/LabelField';
import SelectField from 'components/form-elements/SelectField';
import TableRowWithSubfields from 'components/form-elements/TableRowWithSubfields';
import TextField from 'components/form-elements/TextField';
import EditLineModal from 'components/receiving/modals/EditLineModal';
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import apiClient, { flattenRequest, parseResponse } from 'utils/apiClient';
import Checkbox from 'utils/Checkbox';
import { renderFormField } from 'utils/form-utils';
import { formatProductDisplayName, getReceivingPayloadContainers } from 'utils/form-values-utils';
import Select from 'utils/Select';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

const isReceived = (subfield, fieldValue) => {
  if (!fieldValue) {
    return false;
  }

  if (fieldValue && subfield) {
    return (_.toInteger(fieldValue.quantityReceived) + _.toInteger(fieldValue.quantityCanceled)) >=
      _.toInteger(fieldValue.quantityShipped);
  }

  if (fieldValue && !fieldValue.shipmentItems) {
    return true;
  }

  return _.every(fieldValue && fieldValue.shipmentItems, (item) =>
    _.toInteger(item.quantityReceived) + _.toInteger(item.quantityCanceled) >=
      _.toInteger(item.quantityShipped));
};

const isReceiving = (subfield, fieldValue) => {
  if (subfield) {
    return fieldValue && !_.isNil(fieldValue.quantityReceiving) && fieldValue.quantityReceiving !== '';
  }

  if (!fieldValue.shipmentItems) {
    return false;
  }

  return _.every(fieldValue && fieldValue.shipmentItems, (item) => (!_.isNil(item.quantityReceiving) && item.quantityReceiving !== '') || isReceived(true, item))
    && _.some(fieldValue && fieldValue.shipmentItems, (item) => !_.isNil(item.quantityReceiving) && item.quantityReceiving !== '');
};

const isLineDisabled = (subfield, fieldValue) => {
  const { quantityReceiving, quantityShipped } = fieldValue;
  if (quantityReceiving === 0 && quantityReceiving === quantityShipped) {
    return false;
  }

  return isReceiving(subfield, fieldValue);
};

const isIndeterminate = (subfield, fieldValue) => {
  if (subfield) {
    return false;
  }

  if (fieldValue && !fieldValue.shipmentItems) {
    return false;
  }

  return _.some(fieldValue && fieldValue.shipmentItems, (item) => !_.isNil(item.quantityReceiving) && item.quantityReceiving !== '')
    && _.some(fieldValue && fieldValue.shipmentItems, (item) => (_.isNil(item.quantityReceiving) || item.quantityReceiving === '') && !isReceived(true, item));
};

const isAnyItemSelected = (containers) => {
  if (!_.size(containers)) {
    return false;
  }

  return _.some(containers, (cont) => _.size(cont.shipmentItems) && _.some(cont.shipmentItems, (item) => !_.isNil(item.quantityReceiving) && item.quantityReceiving !== ''));
};

const emptyLinesCounter = (values) =>
  values.containers.reduce((acc, container) => {
    const { shipmentItems } = container;
    const amountOfEmptyLines = shipmentItems
      .filter((item) => !item.quantityReceiving && item.quantityReceiving !== 0)
      .length;

    return acc + amountOfEmptyLines;
  }, 0);

const TABLE_FIELDS = {
  containers: {
    type: ArrayField,
    arrowsNavigation: true,
    rowComponent: TableRowWithSubfields,
    headerFontSize: '0.775rem',
    subfieldKey: 'shipmentItems',
    getDynamicRowAttr: ({ rowValues, subfield, translate }) => {
      let className = '';
      let tooltip = null;
      const received = isReceived(subfield, rowValues);
      const receiving = isReceiving(subfield, rowValues);
      if (received) {
        className = 'text-disabled';
      }
      if (!received && receiving && rowValues.product && rowValues.product.lotAndExpiryControl) {
        if (!rowValues.lotNumber || !rowValues.expirationDate) {
          tooltip = translate('react.partialReceiving.error.lotAndExpiryControl.label');
          className += ' has-control-error';
        }
      }
      return {
        className,
        tooltip,
      };
    },
    fields: {
      autofillLine: {
        fieldKey: '',
        label: '',
        flexWidth: '0.1',
        type: ({
          // eslint-disable-next-line react/prop-types
          subfield, parentIndex, rowIndex, autofillLines, fieldValue, shipmentReceived, values,
        }) => (
          <Checkbox
            disabled={shipmentReceived || isReceived(subfield, fieldValue)}
            className={subfield ? 'ml-4' : 'mr-4'}
            value={isLineDisabled(subfield, fieldValue)}
            indeterminate={isIndeterminate(subfield, fieldValue)}
            onChange={(value) => {
              if (subfield) {
                autofillLines(values, !value, parentIndex, rowIndex);
              } else {
                autofillLines(values, !value, rowIndex);
              }
            }}
          />
        ),
      },
      'parentContainer.name': {
        fieldKey: '',
        type: (params) => (!params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.packLevel1.label',
        defaultMessage: 'Pack level 1',
        flexWidth: '1',
        attributes: {
          formatValue: (fieldValue) => (_.get(fieldValue, 'parentContainer.name') || _.get(fieldValue, 'container.name') || 'Unpacked'),
          showValueTooltip: true,
        },
      },
      'container.name': {
        fieldKey: '',
        type: (params) => (!params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.packLevel2.label',
        defaultMessage: 'Pack level 2',
        flexWidth: '1',
        attributes: {
          formatValue: (fieldValue) => (_.get(fieldValue, 'parentContainer.name') ? _.get(fieldValue, 'container.name') || '' : ''),
        },
      },
      'product.productCode': {
        type: (params) => (params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.code.label',
        defaultMessage: 'Code',
        headerAlign: 'left',
        flexWidth: '1',
        attributes: {
          className: 'text-left ml-1',
        },
      },
      product: {
        type: (params) => (params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.product.label',
        defaultMessage: 'Product',
        headerAlign: 'left',
        flexWidth: '4',
        attributes: {
          className: 'text-left ml-1',
          showValueTooltip: true,
          formatValue: formatProductDisplayName,
        },
        getDynamicAttr: ({ fieldValue }) => ({
          tooltipValue: fieldValue?.name,
        }),
      },
      lotNumber: {
        type: (params) => (params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.lotSerialNo.label',
        defaultMessage: 'Lot/Serial No.',
        flexWidth: '1',
      },
      expirationDate: {
        type: (params) => (params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.expirationDate.label',
        defaultMessage: 'Expiration date',
        flexWidth: '1',
      },
      binLocation: {
        type: (params) => (
          params.subfield ?
            <SelectField {...params} /> : (
              <Select
                disabled={!params.hasBinLocationSupport ||
              params.shipmentReceived || isReceived(false, params.fieldValue)}
                options={params.bins}
                onChange={(value) => params.setLocation(params.rowIndex, value)}
                valueKey="id"
                labelKey="name"
                className="select-xs"
                clearable={false}
              />
            )),
        fieldKey: '',
        flexWidth: '2',
        label: 'react.partialReceiving.binLocation.label',
        defaultMessage: 'Bin Location',
        getDynamicAttr: ({
          bins, hasBinLocationSupport, shipmentReceived, fieldValue,
        }) => ({
          options: bins,
          disabled: !hasBinLocationSupport || shipmentReceived || isReceived(true, fieldValue),
          hide: !hasBinLocationSupport,
        }),
        attributes: {
          clearable: false,
          valueKey: 'id',
          labelKey: 'name',
        },
      },
      'recipient.name': {
        type: (params) => (params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.recipient.label',
        defaultMessage: 'Recipient',
        headerAlign: 'left',
        flexWidth: '1',
        attributes: {
          className: 'text-left ml-1',
          showValueTooltip: true,
        },
      },
      quantityShipped: {
        type: (params) => (params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.shipped.label',
        defaultMessage: 'Shipped',
        flexWidth: '1',
        attributes: {
          formatValue: (value) => (value ? (value.toLocaleString('en-US')) : value),
        },
      },
      quantityReceived: {
        type: (params) => (params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.received.label',
        defaultMessage: 'Received',
        flexWidth: '1',
        attributes: {
          formatValue: (value) => (value ? value.toLocaleString('en-US') : '0'),
        },
        getDynamicAttr: ({ hasPartialReceivingSupport }) => ({
          hide: !hasPartialReceivingSupport,
        }),
      },
      quantityRemaining: {
        type: (params) => (params.subfield ? <LabelField {...params} /> : null),
        label: 'react.partialReceiving.toReceive.label',
        defaultMessage: 'To receive',
        fieldKey: '',
        flexWidth: '0.8',
        getDynamicAttr: ({ fieldValue, shipmentReceived, hasPartialReceivingSupport }) => ({
          className: _.toInteger(fieldValue &&
            fieldValue.quantityRemaining) < 0 && !shipmentReceived
            && !isReceived(true, fieldValue) ? 'text-danger' : '',
          formatValue: (val) => {
            if (!val.quantityRemaining) {
              return val.quantityRemaining;
            }

            if (_.toInteger(fieldValue && fieldValue.quantityRemaining) < 0
              && (shipmentReceived || isReceived(true, fieldValue))) {
              return '0';
            }

            return val.quantityRemaining.toLocaleString('en-US');
          },
          hide: !hasPartialReceivingSupport,
        }),
      },
      quantityReceiving: {
        type: (params) => (params.subfield ? <TextField {...params} /> : null),
        fieldKey: '',
        label: 'react.partialReceiving.receivingNow.label',
        defaultMessage: 'Receiving now',
        flexWidth: '1',
        attributes: {
          autoComplete: 'off',
        },
        getDynamicAttr: ({ shipmentReceived, fieldValue }) => ({
          disabled: shipmentReceived || isReceived(true, fieldValue),
          formatValue: (val) => {
            const { quantityShipped, quantityRemaining } = fieldValue;
            return quantityShipped === 0 && quantityRemaining === quantityShipped ? null : val;
          },
        }),
      },
      edit: {
        type: (params) => (params.subfield ? <EditLineModal {...params} wrapperClassName="edit-button-cell" /> : null),
        fieldKey: '',
        flexWidth: '0.9',
        label: '',
        attributes: {
          btnOpenText: 'react.default.button.edit.label',
          btnOpenDefaultText: 'Edit line',
          title: 'react.default.button.edit.label',
          className: 'btn btn-outline-primary',
          defaultTitleMessage: 'Edit',
        },
        getDynamicAttr: ({
          fieldValue, saveEditLine, parentIndex, rowIndex, shipmentReceived,
        }) => ({
          fieldValue,
          saveEditLine,
          parentIndex,
          rowIndex,
          btnOpenDisabled: shipmentReceived || isReceived(true, fieldValue),
        }),
      },
      comment: {
        type: (params) => (params.subfield ? <TextField {...params} /> : null),
        fieldKey: '',
        flexWidth: '1',
        label: 'react.partialReceiving.comment.label',
        defaultMessage: 'Comment',
        headerAlign: 'left',
        attributes: {
          autoComplete: 'off',
        },
        getDynamicAttr: ({ shipmentReceived, fieldValue }) => ({
          disabled: shipmentReceived || isReceived(true, fieldValue),
        }),
      },
    },
  },
};

function validate(values) {
  const errors = {};
  errors.containers = [];

  if (!values.dateDelivered) {
    errors.dateDelivered = 'react.default.error.requiredField.label';
  }
  _.forEach(values.containers, (container, key) => {
    errors.containers[key] = { shipmentItems: [] };
    _.forEach(container.shipmentItems, (item, key2) => {
      if (item.quantityReceiving < 0) {
        errors.containers[key].shipmentItems[key2] = {
          quantityReceiving: 'react.partialReceiving.error.quantityToReceiveNegative.label',
        };
      }
      const receiving = !_.isNil(item.quantityReceiving) && item.quantityReceiving !== '';
      if (receiving && !_.isNil(item.product) && item.product.lotAndExpiryControl) {
        if (!item.expirationDate && (_.isNil(item.lotNumber) || _.isEmpty(item.lotNumber))) {
          errors.containers[key].shipmentItems[key2] = {
            expirationDate: 'react.partialReceiving.error.lotAndExpiryControl.label',
            lotNumber: 'react.partialReceiving.error.lotAndExpiryControl.label',
          };
        } else if (!item.expirationDate) {
          errors.containers[key].shipmentItems[key2] = {
            expirationDate: 'react.partialReceiving.error.lotAndExpiryControl.label',
          };
        } else if (_.isNil(item.lotNumber) || _.isEmpty(item.lotNumber)) {
          errors.containers[key].shipmentItems[key2] = {
            lotNumber: 'react.partialReceiving.error.lotAndExpiryControl.label',
          };
        }
      }
    });
  });

  return errors;
}

/** The first page of partial receiving where user can see receipt lines and complete it in
 * different ways depending on how they receive it.
 * - If the user is receiving everything with no changes, they click "autofill all quantities"
 * button what will automatically fill all of the "to receive" cells with quantity left in the line.
 * - If the user is receiving by pallet with no changes, they click the checkbox next to the pallet
 * they want to receive what will automatically fill "to receive" column for lines
 * in that pallet with full quantity.
 * - If the user is receiving by line with no lot changes, they go line by line and type in the
 * quantity from each line they want to receive.
 * - If the user has to change lot information, they click the edit line button which allows them
 * to edit the line.
 */
class PartialReceivingPage extends Component {
  static autofillLine(clearValue, shipmentItem) {
    if (isReceived(true, shipmentItem)) {
      return shipmentItem;
    }
    const autofillQuantity = _.toInteger(shipmentItem.quantityShipped) -
          _.toInteger(shipmentItem.quantityReceived);

    return {
      ...shipmentItem,
      quantityReceiving: clearValue || autofillQuantity < 0 ? null : autofillQuantity,
    };
  }

  constructor(props) {
    super(props);

    this.state = {
      values: {},
    };
    this.autofillLines = this.autofillLines.bind(this);
    this.setLocation = this.setLocation.bind(this);
    this.save = this.save.bind(this);
    this.saveAndExit = this.saveAndExit.bind(this);
    this.saveValues = this.saveValues.bind(this);
    this.saveEditLine = this.saveEditLine.bind(this);
    this.exportTemplate = this.exportTemplate.bind(this);
    this.importTemplate = this.importTemplate.bind(this);
    this.rewriteQuantitiesAfterSave = this.rewriteQuantitiesAfterSave.bind(this);
  }

  componentDidMount() {
    this.fetchPartialReceiptCandidates();
    if (this.props.partialReceivingTranslationsFetched) {
      this.dataFetched = true;
    }
    this.props.hideSpinner();
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.partialReceivingTranslationsFetched && !this.dataFetched) {
      this.dataFetched = true;
    }
  }

  confirmReceive(formValues, emptyLinesCount) {
    const { translate } = this.props;
    confirmAlert({
      title: translate('react.partialReceiving.message.confirmReceive.label', 'Confirm receiving'),
      message: translate(
        'react.partialReceiving.confirmReceive.notSupportingPartialReceiving.message',
        `You have not entered a value in the receiving quantity for ${emptyLinesCount} line/lines on this shipment. ` +
        'These lines will be received as zero. ' +
        'You will not be able to go back and receive them later. Do you want to continue?.',
        {
          count: emptyLinesCount,
        },
      ),
      buttons: [
        {
          label: translate('react.default.yes.label', 'Yes'),
          onClick: () => this.onSubmit(formValues),
        },
        {
          label: translate('react.default.no.label', 'No'),
        },
      ],
    });
  }

  buildShipmentItems(containers) {
    if (!this.props.hasPartialReceivingSupport) {
      return containers?.map((container) => ({
        ...container,
        shipmentItems: container?.shipmentItems
          .map((item) => ({
            ...item,
            quantityReceiving: item.quantityReceiving ? item.quantityReceiving : 0,
          })),
      }));
    }
    return containers?.map((container) => ({
      ...container,
      shipmentItems: container?.shipmentItems
        .map((item) => {
          if (item.receiptItemId) {
            return {
              ...item,
              quantityReceiving: item.quantityReceiving ? item.quantityReceiving : 0,
            };
          }
          return item;
        })
        .filter((item) => !_.isNil(item.quantityReceiving) && item.quantityReceiving !== ''),
    }));
  }

  onSubmit(formValues) {
    const containers = this.buildShipmentItems(formValues.containers);
    const { values } = this.state;
    this.setState({
      values: {
        ...values,
        containers,
      },
    });
    this.nextPage({ ...values, containers });
  }

  /**
   * Updates items with a location of the bin.
   * @public
   */
  setLocation(rowIndex, location) {
    if (this.state.values.containers && !_.isNil(rowIndex)) {
      const containers = update(this.state.values.containers, {
        [rowIndex]: {
          shipmentItems: {
            $apply: (items) => (!items ? [] : items.map((item) => {
              if (isReceived(true, item)) {
                return item;
              }

              return { ...item, binLocation: location };
            })),
          },
        },
      });
      const { values } = this.state;
      values.containers = containers;
      this.setState({ values });
      window.setFormValue('containers', containers);
    }
  }

  /**
   * Fetches available receipts from API.
   * @public
   */
  fetchPartialReceiptCandidates() {
    this.props.showSpinner();
    const url = `/api/partialReceiving/${this.props.match.params.shipmentId}?stepNumber=1`;

    return apiClient.get(url)
      .then((response) => {
        this.setState({ values: {} }, () => {
          this.setState({ values: parseResponse(response.data.data) });
        });
      })
      .catch(() => this.props.hideSpinner());
  }

  saveValues(formValues) {
    this.props.showSpinner();
    const url = `/api/partialReceiving/${this.props.match.params.shipmentId}?stepNumber=1`;

    const payload = {
      ...formValues,
      recipient: formValues?.recipient?.id,
      containers: getReceivingPayloadContainers(formValues),
    };
    return apiClient.post(url, flattenRequest(payload));
  }

  saveAndExit(formValues) {
    this.saveValues(formValues)
      .then(() => {
        const { requisition, shipmentId } = formValues;
        window.location = STOCK_MOVEMENT_URL.show(requisition || shipmentId);
      })
      .catch(() => this.props.hideSpinner());
  }

  /**
   * Calls save method.
   * @public
   */
  save(formValues, callback) {
    this.saveValues(formValues)
      .then((response) => {
        this.props.hideSpinner();

        this.setState({ values: {} }, () =>
          this.setState({ values: parseResponse(response.data.data) }));
        if (callback) {
          callback();
        }
      })
      .catch(() => this.props.hideSpinner());
  }

  dataFetched = false;

  /**
   * Autofills "to receive" cells in different ways depending on what user did.
   * If they click "Autofill quantites" button, it will automatically fill all lines.
   * If they click checkbox next to the pallet, it will automatically fill all lines in that pallet.
   * If they click checbox next to the line, it will automatically fill this line.
   * @public
   */
  autofillLines(values, clearValue, parentIndex, rowIndex) {
    if (values.containers) {
      let containers = [];

      if (_.isNil(parentIndex)) {
        containers = update(values.containers, {
          $apply: (items) => (!items ? [] : items.map((item) => update(item, {
            shipmentItems: {
              $apply: (shipmentItems) => (!shipmentItems ? [] : shipmentItems.map((shipmentItem) =>
                PartialReceivingPage.autofillLine(clearValue, shipmentItem))),
            },
          }))),
        });
      } else if (_.isNil(rowIndex)) {
        containers = update(values.containers, {
          [parentIndex]: {
            shipmentItems: {
              $apply: (items) => (!items ? [] : items.map((item) =>
                PartialReceivingPage.autofillLine(clearValue, item))),
            },
          },
        });
      } else {
        containers = update(values.containers, {
          [parentIndex]: {
            shipmentItems: {
              [rowIndex]: {
                $apply: (item) => PartialReceivingPage.autofillLine(clearValue, item),
              },
            },
          },
        });
      }

      const newValues = { ...values };
      newValues.containers = containers;
      this.setState({ values: newValues });
      window.setFormValue('containers', containers);
    }
  }

  rewriteQuantitiesAfterSave({
    formValues,
    editLines,
    fetchedContainers,
    editLinesIndex,
    parentIndex,
  }) {
    // Get list of shipment items from all of containers
    const flattenedShipmentItems = formValues.containers.reduce((acc, container) => [
      ...acc,
      ...container.shipmentItems,
    ], []);

    // Calculate index of line items coming from modal. When there are
    // more than one container, the index is relative to the container, so as an example:
    // There are two container: First has 3 shipment items, second has 2. When we are
    // adding new lines to the second container at second index, the "editLinesIndex" is 2,
    // so we have to add sizes of previous containers.
    const getContainerEditLineIndex = formValues.containers.reduce((acc, container, idx) => {
      if (idx < parentIndex) {
        return acc + container.shipmentItems.length;
      }

      return acc;
    }, 0) + editLinesIndex;

    // Get list of shipment items from all containers. It's fetched data with actual quantities etc.
    const flattenedFetchedShipmentItems = fetchedContainers.reduce((acc, container) => [
      ...acc,
      container.shipmentItems,
    ], []);

    // Concatenated values from first table part (lines after those coming from modal),
    // with those lines from modal and with values after that lines.
    const newTableValue = [
      ..._.take(flattenedShipmentItems, getContainerEditLineIndex),
      ...editLines,
      ..._.takeRight(
        flattenedShipmentItems,
        flattenedShipmentItems.length - getContainerEditLineIndex - 1,
      ),
    ];

    // Updating line items in the table. All values are taken from fetched
    // items (updating all quantities) except the quantityReceiving which determines
    // whether the line should be checked
    const rewroteTableValue = _.zip(newTableValue, _.flatten(flattenedFetchedShipmentItems))
      .map(([shipmentItem, fetchedShipmentItem]) => ({
        ...fetchedShipmentItem,
        quantityReceiving: shipmentItem?.quantityReceiving,
      }));

    // Splitting values into containers
    const { shipmentItems } = fetchedContainers.reduce((acc, container) => ({
      shipmentItems: [
        ...acc.shipmentItems,
        _.slice(rewroteTableValue, acc.startIndex, acc.startIndex + container.shipmentItems.length),
      ],
      startIndex: acc.startIndex + container.shipmentItems.length,
    }), { shipmentItems: [], startIndex: 0 });

    return formValues.containers.map((container, idx) => ({
      ...container,
      shipmentItems: shipmentItems[idx],
    }));
  }

  /*
   * Saves changes made in edit line modal and updates data.
   * @param {object} editLines
   * @param {number} parentIndex
   * @public
   */
  saveEditLine(editLines, parentIndex, formValues, rowIndex) {
    this.props.showSpinner();
    const editedLinesToSave = {
      ...this.state.values,
      containers: [{ ...this.state.values.containers[parentIndex], shipmentItems: editLines }],
    };
    this.saveValues(editedLinesToSave)
      .then((response) => {
        const containers = this.rewriteQuantitiesAfterSave({
          formValues,
          editLines,
          parentIndex,
          fetchedContainers: response.data.data.containers,
          editLinesIndex: rowIndex,
        });
        this.setState({
          values: parseResponse({
            ...response.data.data,
            containers,
          }),
        });
      })
      .finally(() => this.props.hideSpinner());
  }

  exportTemplate() {
    this.props.showSpinner();
    const { values } = this.state;
    const { shipmentId } = values;
    const url = `/api/partialReceiving/exportCsv/${shipmentId}`;
    /** We have to omit product.displayNames, due to an error
     *  while binding bindData(partialReceiptItem, shipmentItemMap)
     *  it expects product.displayNames to have a setter, as we pass
     *  product.displayNames.default: XYZ, to the update method, but it's not a
     *  writable property.
     *  With deprecated product.translatedName it was not the case, because
     *  it was recognizing the transient and we didn't access product.translatedName.something
     *  but product.translatedName directly
     * */
    const valuesWithoutDisplayNames = {
      ...values,
      containers: values?.containers?.map?.((container) => ({
        ...container,
        shipmentItems: container?.shipmentItems?.map?.((item) => _.omit(item, 'product.displayNames')),
      })),
      recipient: {
        id: values.recipient?.id,
      },
    };
    apiClient.post(url, flattenRequest(valuesWithoutDisplayNames))
      .then((response) => {
        fileDownload(response.data, `PartialReceiving${shipmentId ? `-${shipmentId}` : ''}.csv`, 'text/csv');
        this.props.hideSpinner();
      })
      .catch(() => this.props.hideSpinner());
  }

  importTemplate(event) {
    this.props.showSpinner();
    const formData = new FormData();
    const file = event.target.files[0];

    formData.append('importFile', file.slice(0, file.size, 'text/csv'));
    const config = {
      headers: {
        'content-type': 'multipart/form-data',
      },
    };

    const url = `/api/partialReceiving/importCsv/${this.state.values.shipmentId}`;

    return apiClient.post(url, formData, config)
      .then(() => {
        this.props.hideSpinner();
        window.location.reload();
      })
      .catch(() => {
        this.props.hideSpinner();
      });
  }

  transitionToNextStep(formValues) {
    const url = `/api/partialReceiving/${this.props.match.params.shipmentId}?stepNumber=1`;
    const payload = {
      receiptStatus: 'CHECKING',
      ...formValues,
      recipient: formValues?.recipient?.id,
      containers: getReceivingPayloadContainers(formValues),
    };

    return apiClient.post(url, flattenRequest(payload));
  }

  nextPage(formValues) {
    this.props.showSpinner();
    this.transitionToNextStep(formValues)
      .then(() => this.props.nextPage(formValues))
      .then(() => this.props.hideSpinner())
      .catch(() => this.props.hideSpinner());
  }

  render() {
    return (
      <div>
        <Form
          onSubmit={(values) => {
            const { hasPartialReceivingSupport } = this.props;
            const emptyLinesCount = emptyLinesCounter(values);
            // If there are empty lines (any other value than 0 would be truthy),
            // and the current location doesn't support partial receiving, show the confirm modal
            return !hasPartialReceivingSupport && emptyLinesCount
              ? this.confirmReceive(values, emptyLinesCount)
              : this.onSubmit(values);
          }}
          validate={validate}
          autofillLines={this.autofillLines}
          mutators={{
            ...arrayMutators,
            setValue: ([field, value], state, { changeValue }) => {
              changeValue(state, field, () => value);
            },
          }}
          initialValues={this.state.values}
          render={({ handleSubmit, values, form }) => {
            if (!window.setFormValue) {
              window.setFormValue = form.mutators.setValue;
            }
            return (
              <form onSubmit={handleSubmit}>
                <div className="d-flex flex-column">
                  <div>
                    <span className="buttons-container">
                      <button type="button" className="btn btn-outline-secondary float-right btn-form btn-xs" disabled={this.state.values.shipmentStatus === 'RECEIVED'} onClick={() => this.autofillLines(values)}>
                        <Translate id="react.partialReceiving.autofillQuantities.label" defaultMessage="Autofill quantities" />
                      </button>
                      <button type="button" className="btn btn-outline-secondary float-right btn-form btn-xs" disabled={!isAnyItemSelected(values.containers) || values.shipmentStatus === 'RECEIVED'} onClick={() => this.saveAndExit(values)}>
                        <span>
                          <i className="fa fa-sign-out pr-2" />
                          <Translate id="react.default.button.saveAndExit.label" defaultMessage="Save and exit" />
                        </span>
                      </button>
                      <button type="button" className="btn btn-outline-secondary float-right btn-form btn-xs" disabled={!isAnyItemSelected(values.containers) || values.shipmentStatus === 'RECEIVED'} onClick={() => this.save(values)}>
                        <Translate id="react.default.button.save.label" defaultMessage="Save" />
                      </button>
                      <button
                        type="button"
                        className="btn btn-outline-secondary btn-xs mr-3"
                        onClick={() => this.exportTemplate()}
                      >
                        <span>
                          <i className="fa fa-upload pr-2" />
                          <Translate id="react.default.button.exportTemplate.label" defaultMessage="Export template" />
                        </span>
                      </button>
                      <label
                        htmlFor="csvInput"
                        className="btn btn-outline-secondary btn-xs mr-3"
                      >
                        <span>
                          <i className="fa fa-download pr-2" />
                          <Translate id="react.default.button.importTemplate.label" defaultMessage="Import template" />
                        </span>
                        <input
                          id="csvInput"
                          type="file"
                          style={{ display: 'none' }}
                          onChange={this.importTemplate}
                          onClick={(event) => {
                            // eslint-disable-next-line no-param-reassign
                            event.target.value = null;
                          }}
                          accept=".csv"
                        />
                      </label>
                    </span>
                  </div>
                  <div className="my-2 table-form">
                    {_.map(TABLE_FIELDS, (fieldConfig, fieldName) =>
                      renderFormField(fieldConfig, fieldName, {
                        autofillLines: this.autofillLines,
                        saveEditLine: this.saveEditLine,
                        setLocation: this.setLocation,
                        bins: this.props.bins,
                        hasBinLocationSupport: this.props.hasBinLocationSupport,
                        locationId: this.props.locationId,
                        shipmentReceived: this.state.values.shipmentStatus === 'RECEIVED',
                        values,
                        hasPartialReceivingSupport: this.props.hasPartialReceivingSupport,
                        translate: this.props.translate,
                      }))}
                  </div>
                  <div className="submit-buttons">
                    <button type="submit" className="btn btn-outline-primary btn-form float-right btn-xs" disabled={!isAnyItemSelected(values.containers) || values.shipmentStatus === 'RECEIVED'}>
                      <Translate id="react.default.button.next.label" defaultMessage="Next" />
                    </button>
                  </div>
                </div>
              </form>
            );
          }}
        />
      </div>
    );
  }
}

const mapStateToProps = (state) => ({
  hasBinLocationSupport: state.session.currentLocation.hasBinLocationSupport,
  partialReceivingTranslationsFetched: state.session.fetchedTranslations.partialReceiving,
  hasPartialReceivingSupport: state.session.currentLocation.hasPartialReceivingSupport,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps, {
  showSpinner, hideSpinner,
})(PartialReceivingPage);

PartialReceivingPage.propTypes = {
  /** Function called when data is loading */
  showSpinner: PropTypes.func.isRequired,
  /** Function called when data has loaded */
  hideSpinner: PropTypes.func.isRequired,
  hasBinLocationSupport: PropTypes.bool.isRequired,
  /** Array of available bin locations  */
  bins: PropTypes.arrayOf(PropTypes.shape({})),
  /** Location ID (destination). Needs to be used in /api/products request. */
  locationId: PropTypes.string.isRequired,
  partialReceivingTranslationsFetched: PropTypes.bool.isRequired,
  match: PropTypes.shape({
    params: PropTypes.shape({
      shipmentId: PropTypes.string,
    }),
  }).isRequired,
  nextPage: PropTypes.func.isRequired,
  /** Is true when currently selected location supports partial receiving */
  hasPartialReceivingSupport: PropTypes.bool.isRequired,
  translate: PropTypes.func.isRequired,
};

PartialReceivingPage.defaultProps = {
  bins: [],
  match: {},
};
