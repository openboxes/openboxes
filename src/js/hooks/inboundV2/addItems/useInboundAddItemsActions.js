import { useEffect, useState } from 'react';

import _ from 'lodash';
import queryString from 'query-string';
import { useFieldArray } from 'react-hook-form';
import { useDispatch } from 'react-redux';
import { useHistory, useParams } from 'react-router-dom';

import { updateWorkflowHeader } from 'actions';
import stockMovementApi from 'api/services/StockMovementApi';
import {
  STOCK_MOVEMENT_BY_ID,
  STOCK_MOVEMENT_ITEM_REMOVE,
  STOCK_MOVEMENT_REMOVE_ALL_ITEMS,
  STOCK_MOVEMENT_UPDATE_INVENTORY_ITEMS,
  STOCK_MOVEMENT_UPDATE_ITEMS,
  STOCK_MOVEMENT_UPDATE_STATUS,
} from 'api/urls';
import notification from 'components/Layout/notifications/notification';
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import locationType from 'consts/locationType';
import modalWithTableType from 'consts/modalWithTableType';
import NotificationType from 'consts/notificationTypes';
import RequisitionStatus from 'consts/requisitionStatus';
import { InboundWorkflowState } from 'consts/StockMovementState';
import { DateFormat, DateFormatDateFns } from 'consts/timeFormat';
import useSpinner from 'hooks/useSpinner';
import useTranslate from 'hooks/useTranslate';
import apiClient from 'utils/apiClient';
import confirmationModal from 'utils/confirmationModalUtils';
import createInboundWorkflowHeader from 'utils/createInboundWorkflowHeader';
import dateWithoutTimeZone, { formatDateToString } from 'utils/dateUtils';

const useInboundAddItemsActions = ({
  control,
  getValues,
  setValue,
  trigger,
  defaultTableRow,
  next,
  previous,
  openModal,
}) => {
  const [loading, setLoading] = useState(false);
  const spinner = useSpinner();
  const translate = useTranslate();
  const dispatch = useDispatch();
  const history = useHistory();
  const { stockMovementId } = useParams();

  const {
    fields: lineItemsArrayFields,
    remove: removeRow,
    append,
  } = useFieldArray({
    control,
    name: 'values.lineItems',
  });

  const getNextSortOrder = () => {
    const maxSortOrder = Math.max(0, ...getValues('values.lineItems').map(item => item.sortOrder || 0));
    return maxSortOrder + 100;
  };

  const addNewLine = () => {
    append([{
      ...defaultTableRow[0],
      sortOrder: getNextSortOrder(),
    }]);
  };

  const formatDate = (date) => formatDateToString({
    date,
    dateFormat: DateFormatDateFns.DD_MMM_YYYY,
  });

  const transformLineItem = (item) => ({
    ...item,
    itemId: item.id,
    product: item.product
      ? {
        ...item.product,
        label: item.product.label || `${item.product.productCode} - ${item.product.name}`,
        value: item.product.value || item.product.id,
      }
      : null,
    recipient: item.recipient
      ? {
        ...item.recipient,
        label: item.recipient.label || item.recipient.name,
        value: item.recipient.value || item.recipient.id,
      }
      : null,
    expirationDate: formatDate(item.expirationDate),
    inventoryItem: item.inventoryItem
      ? {
        ...item.inventoryItem,
        expirationDate: formatDate(item.inventoryItem.expirationDate),
      }
      : null,
  });

  const shouldUpdateItem = (lineItems) =>
    lineItems.filter(
      (item) =>
        item.statusCode
        && getValues('currentLineItems').some((oldItem) => {
          if (oldItem.id !== item.id) {
            return false;
          }
          const expirationDateChanged = (item.expirationDate
              && item.inventoryItem?.expirationDate !== item.expirationDate)
            || (!item.expirationDate && oldItem.expirationDate);

          return !_.isEqual(item, oldItem) || expirationDateChanged;
        }),
    );

  const getLineItemsToBeSaved = (lineItems) => {
    const lineItemsToBeAdded = lineItems.filter(
      (item) => !item.statusCode && item.quantityRequested && item.quantityRequested !== '0' && item.product,
    );

    const lineItemsToBeUpdated = shouldUpdateItem(lineItems);

    const formatItem = (item) => ({
      id: item.id,
      product: { id: item.product?.id },
      quantityRequested: item.quantityRequested,
      palletName: item.palletName,
      boxName: item.boxName,
      lotNumber: item.lotNumber,
      expirationDate: dateWithoutTimeZone({
        date: item.expirationDate,
        outputDateFormat: DateFormat.MM_DD_YYYY,
      }),
      recipient: { id: item.recipient?.id },
      sortOrder: item.sortOrder,
    });

    return [...lineItemsToBeAdded.map(formatItem), ...lineItemsToBeUpdated.map(formatItem)];
  };

  const checkInvalidQuantities = (lineItems) =>
    lineItems.some((item) => !item.quantityRequested || item.quantityRequested === '0');

  const saveRequisitionItemsInCurrentStep = async (itemCandidatesToSave) => {
    const itemsToSave = getLineItemsToBeSaved(itemCandidatesToSave)
      .map((item) => ({
        ...item,
        expirationDate: item.expirationDate
          ? dateWithoutTimeZone({
            date: item.expirationDate,
            outputDateFormat: DateFormat.MM_DD_YYYY,
          })
          : null,
      }));
    if (itemsToSave.length) {
      const payload = {
        lineItems: itemsToSave,
      };
      try {
        spinner.show();
        const resp = await apiClient.post(STOCK_MOVEMENT_UPDATE_ITEMS(stockMovementId),
          payload);
        const { data } = resp.data;
        const transformedLineItems = data.lineItems.map(transformLineItem);
        setValue('currentLineItems', transformedLineItems);
        setValue('values.lineItems', transformedLineItems);
        return resp;
      } finally {
        spinner.hide();
      }
    }

    // If there are no items to save, clear the line items with zero or empty quantity
    if (checkInvalidQuantities(itemCandidatesToSave)) {
      const filteredItems = itemCandidatesToSave.filter(
        (item) => item.quantityRequested && item.quantityRequested !== '0',
      );
      setValue('values.lineItems', filteredItems.length > 0 ? filteredItems : defaultTableRow);
    }
    return null;
  };

  const setLineItems = (response, showOnlyImportedItems) => {
    const { data } = response.data;
    const existingLineItems = getValues('values.lineItems') || [];
    const existingCurrentLineItems = getValues('currentLineItems') || [];

    const lineItemsData = !data.length && existingLineItems.length === 0
      ? defaultTableRow
      : data.map(transformLineItem);

    setValue('currentLineItems', _.uniqBy([...lineItemsData, ...existingCurrentLineItems], 'id'));
    setValue(
      'values.lineItems',
      showOnlyImportedItems ? lineItemsData : _.uniqBy([...lineItemsData, ...existingLineItems], 'id'),
    );
  };

  const fetchLineItems = async (showOnlyImportedItems = false) => {
    const response = await stockMovementApi.getStockMovementItems(stockMovementId, {
      stepNumber: InboundWorkflowState.ADD_ITEMS,
    });
    setLineItems(response, showOnlyImportedItems);
  };

  const removeSavedRow = async (itemId) => {
    try {
      spinner.show();
      await apiClient.delete(STOCK_MOVEMENT_ITEM_REMOVE(itemId));
    } finally {
      spinner.hide();
    }
  };

  const removeAllRows = async () => {
    try {
      spinner.show();
      await apiClient.delete(STOCK_MOVEMENT_REMOVE_ALL_ITEMS(stockMovementId));
      setValue('currentLineItems', []);
      setValue('values.lineItems', defaultTableRow);
    } finally {
      spinner.hide();
    }
  };

  const fetchAddItemsPageData = async () => {
    const response = await apiClient.get(STOCK_MOVEMENT_BY_ID(stockMovementId));
    const { data } = response.data;
    const transformedData = {
      ...data,
      identifier: data.identifier,
      stockMovementId: data.id,
    };
    dispatch(updateWorkflowHeader(createInboundWorkflowHeader(data), data.displayStatus.name));
    setValue('values', transformedData);
  };

  const confirmAction = (onConfirm, messageId, defaultMessage) => {
    const modalLabels = {
      title: {
        label: 'react.stockMovement.message.confirmSave.label',
        default: 'Confirm save',
      },
      content: { label: messageId, default: defaultMessage },
    };

    const modalButtons = (onClose) => [
      {
        variant: 'transparent',
        defaultLabel: 'No',
        label: 'react.default.no.label',
        onClick: () => onClose(),
      },
      {
        variant: 'primary',
        defaultLabel: 'Yes',
        label: 'react.default.yes.label',
        onClick: () => {
          onConfirm();
          onClose();
        },
      },
    ];

    confirmationModal({ buttons: modalButtons, ...modalLabels });
  };

  const confirmValidationError = () => {
    const modalLabels = {
      title: {
        label: 'react.stockMovement.confirmPreviousPage.label',
        default: 'Validation error',
      },
      content: {
        label: 'react.stockMovement.confirmPreviousPage.message.label',
        default: 'Cannot save due to validation error on page',
      },
    };

    const modalButtons = (onClose) => [
      {
        variant: 'primary',
        defaultLabel: 'Correct error',
        label: 'react.stockMovement.confirmPreviousPage.correctError.label',
        onClick: () => onClose(),
      },
      {
        variant: 'danger',
        defaultLabel: 'Continue (lose unsaved work)',
        label: 'react.stockMovement.confirmPreviousPage.continue.label',
        onClick: () => {
          previous();
          onClose();
        },
      },
    ];

    confirmationModal({ buttons: modalButtons, ...modalLabels });
  };

  const transitionToNextStep = async () => {
    const formValues = getValues();
    try {
      spinner.show();
      const payload = { status: RequisitionStatus.CHECKING };
      if (formValues.values.statusCode === RequisitionStatus.CREATED) {
        await apiClient.post(STOCK_MOVEMENT_UPDATE_STATUS(stockMovementId), payload);
      }
      next();
    } finally {
      spinner.hide();
    }
  };

  const updateInventoryItemsAndTransitionToNextStep = async (lineItems) => {
    const itemsToSave = getLineItemsToBeSaved(lineItems);
    const payload = {
      lineItems: itemsToSave,
    };
    try {
      spinner.show();
      await apiClient.post(STOCK_MOVEMENT_UPDATE_INVENTORY_ITEMS(stockMovementId), payload);
      await transitionToNextStep();
    } finally {
      spinner.hide();
    }
  };

  const handleTransition = async ({ updatedLineItems, lineItems }) => {
    const hasExpiryMismatch = updatedLineItems.some(
      (item) => item.inventoryItem && item.expirationDate !== item.inventoryItem.expirationDate,
    );

    if (!hasExpiryMismatch) {
      return transitionToNextStep();
    }
    const hasNonZeroQuantity = updatedLineItems.some(
      (item) => item.inventoryItem?.quantity && item.inventoryItem.quantity !== '0',
    );

    if (hasNonZeroQuantity) {
      // Find all items with mismatched expiration dates to show in the confirmation modal
      const mismatchedItems = updatedLineItems
        .filter((item) =>
          item.inventoryItem
          && item.inventoryItem.expirationDate !== item.expirationDate
          && item.inventoryItem.quantity
          && item.inventoryItem.quantity !== '0')
        .map((item) => ({
          code: item.product?.productCode,
          product: item.product,
          lotNumber: item.lotNumber,
          previousExpiry: item.inventoryItem.expirationDate,
          newExpiry: item.expirationDate,
        }));

      if (mismatchedItems.length > 0) {
        const shouldUpdate = await openModal(
          { data: mismatchedItems, type: modalWithTableType.EXPIRATION },
        );
        if (!shouldUpdate) {
          return Promise.reject();
        }
      }
    }

    return updateInventoryItemsAndTransitionToNextStep(lineItems);
  };

  const saveAndTransitionToNextStep = async (lineItems) => {
    try {
      spinner.show();
      const resp = await saveRequisitionItemsInCurrentStep(lineItems);
      const updatedLineItems = resp ? resp.data.data.lineItems : [];
      handleTransition({ updatedLineItems, lineItems });
    } finally {
      spinner.hide();
    }
  };

  const checkDuplicatesSaveAndTransitionToNextStep = async (formValues, lineItems) => {
    const itemsMap = _.groupBy(
      lineItems.filter((item) => item.product?.productCode),
      (item) => item.product.productCode,
    );
    const duplicateGroups = Object.values(itemsMap).filter((g) => g.length > 1);
    const hasDuplicates = duplicateGroups.length > 0;
    const skipConfirm =
      formValues.values.origin.locationType.locationTypeCode === locationType.SUPPLIER
      || !formValues.values.hasManageInventory;

    if (hasDuplicates && !skipConfirm) {
      const shouldUpdate = await openModal({
        data: duplicateGroups.flat(),
        type: modalWithTableType.DUPLICATES,
      });
      if (!shouldUpdate) {
        return Promise.reject();
      }
    }

    return saveAndTransitionToNextStep(lineItems);
  };

  const nextPage = async () => {
    const isFormValid = await trigger();
    if (!isFormValid) {
      return;
    }
    const formValues = getValues();
    const lineItems = formValues.values.lineItems.filter((item) => item?.product);

    const hasInvalidQuantity = checkInvalidQuantities(lineItems);

    if (hasInvalidQuantity) {
      confirmAction(
        () => checkDuplicatesSaveAndTransitionToNextStep(formValues, lineItems),
        'react.stockMovement.confirmSave.message',
        'Are you sure you want to save? There are some lines with empty or zero quantity, those lines will be deleted.',
      );
      return;
    }
    await checkDuplicatesSaveAndTransitionToNextStep(formValues, lineItems);
  };

  const saveItems = async (lineItems) => {
    try {
      spinner.show();
      await saveRequisitionItemsInCurrentStep(lineItems);
      notification(NotificationType.SUCCESS)({
        message: translate('react.stockMovement.alert.saveSuccess.label', 'Changes saved successfully'),
      });
    } finally {
      spinner.hide();
    }
  };

  const save = async () => {
    const isFormValid = await trigger();
    if (!isFormValid) {
      return;
    }
    const lineItems = getValues('values.lineItems');
    const hasInvalidQuantity = checkInvalidQuantities(lineItems);

    if (hasInvalidQuantity) {
      confirmAction(
        () => saveItems(lineItems),
        'react.stockMovement.confirmSave.message',
        'Are you sure you want to save? There are some lines with empty or zero quantity, those lines will be deleted.',
      );
      return;
    }
    await saveItems(lineItems);
  };

  const saveAndExit = async () => {
    const isFormValid = await trigger();
    if (!isFormValid) {
      confirmAction(
        () => {
          window.location = STOCK_MOVEMENT_URL.show(stockMovementId);
        },
        'react.stockMovement.confirmExit.message',
        'Validation errors occurred. Are you sure you want to exit and lose unsaved data?',
      );
      return;
    }

    try {
      spinner.show();
      await saveRequisitionItemsInCurrentStep(getValues('values.lineItems'));
      window.location = STOCK_MOVEMENT_URL.show(stockMovementId);
    } finally {
      spinner.hide();
    }
  };

  const previousPage = async () => {
    const isFormValid = await trigger();
    if (!isFormValid) {
      confirmValidationError();
      return;
    }
    try {
      spinner.show();
      await saveRequisitionItemsInCurrentStep(getValues('values.lineItems'));
      previous();
    } finally {
      spinner.hide();
    }
  };

  const fetchData = async () => {
    if (!stockMovementId) {
      dispatch(updateWorkflowHeader([], null));
      previous();
      return;
    }
    setLoading(true);

    try {
      spinner.show();
      await fetchAddItemsPageData();
      await fetchLineItems();
    } catch {
      // Any error during fetching means we cannot continue the edit flow.
      // In that case, redirect the user to the create inbound flow.
      dispatch(updateWorkflowHeader([], null));
      history.push({
        pathname: '/openboxes/stockMovement/createInbound',
        search: queryString.stringify({ direction: 'INBOUND' }),
      });
    } finally {
      setLoading(false);
      spinner.hide();
    }
  };

  const refresh = async () => {
    confirmAction(
      () => fetchData(),
      'react.stockMovement.confirmRefresh.message',
      'Are you sure you want to refresh? Your progress since last save will be lost.',
    );
  };

  useEffect(() => {
    fetchData();
  }, []);

  return {
    loading,
    lineItemsArrayFields,
    addNewLine,
    removeRow,
    removeAllRows,
    saveRequisitionItemsInCurrentStep,
    fetchLineItems,
    removeSavedRow,
    nextPage,
    previousPage,
    save,
    saveAndExit,
    refresh,
  };
};

export default useInboundAddItemsActions;
