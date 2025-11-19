import { useEffect, useState } from 'react';

import { zodResolver } from '@hookform/resolvers/zod';
import fileDownload from 'js-file-download';
import _ from 'lodash';
import queryString from 'query-string';
import { useFieldArray, useForm } from 'react-hook-form';
import { useDispatch } from 'react-redux';
import { useHistory, useLocation } from 'react-router-dom';

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
import InboundV2Step from 'consts/InboundStep';
import locationType from 'consts/locationType';
import modalWithTableType from 'consts/modalWithTableType';
import NotificationType from 'consts/notificationTypes';
import RequisitionStatus from 'consts/requisitionStatus';
import { InboundWorkflowState } from 'consts/StockMovementState';
import { DateFormat, DateFormatDateFns } from 'consts/timeFormat';
import useInboundAddItemsValidation from 'hooks/inboundV2/addItems/useInboundAddItemsValidation';
import useHandleModalAction from 'hooks/useHandleModalAction';
import useQueryParams from 'hooks/useQueryParams';
import useSpinner from 'hooks/useSpinner';
import useTranslate from 'hooks/useTranslate';
import apiClient from 'utils/apiClient';
import confirmationModal from 'utils/confirmationModalUtils';
import createInboundWorkflowHeader from 'utils/createInboundWorkflowHeader';
import dateWithoutTimeZone, { formatDateToString } from 'utils/dateUtils';
import useTranslation from 'hooks/useTranslation';

const useInboundAddItemsForm = ({
  next,
  previous,
}) => {
  const [loading, setLoading] = useState(false);
  const {
    isOpen: isModalOpen,
    data: modalData,
    type: modalType,
    openModal,
    handleResponse: handleModalResponse,
  } = useHandleModalAction();

  const history = useHistory();
  const location = useLocation();

  const spinner = useSpinner();
  const { validationSchema } = useInboundAddItemsValidation();
  const queryParams = useQueryParams();
  useTranslation('stockMovement');
  const translate = useTranslate();
  const dispatch = useDispatch();
  const defaultTableRow = [{
    palletName: '',
    boxName: '',
    product: undefined,
    lotNumber: '',
    expirationDate: '',
    quantityRequested: undefined,
    recipient: undefined,
  }];
  const defaultValues = {
    currentLineItems: [],
    values: {
      lineItems: defaultTableRow,
    },
  };

  const {
    control,
    getValues,
    handleSubmit,
    formState: {
      errors,
      isValid,
    },
    trigger,
    setValue,
  } = useForm({
    mode: 'onBlur',
    defaultValues,
    resolver: zodResolver(validationSchema),
  });

  const {
    fields: lineItemsArrayFields,
    remove: removeRow,
    append,
  } = useFieldArray({
    control,
    name: 'values.lineItems',
  });

  const formatDate = (date) => (formatDateToString({
    date,
    dateFormat: DateFormatDateFns.DD_MMM_YYYY,
  }));

  const confirmAction = (onConfirm, messageId, defaultMessage) => {
    const modalLabels = {
      title: {
        label: 'react.stockMovement.message.confirmSave.label',
        default: 'Confirm save',
      },
      content: {
        label: messageId,
        default: defaultMessage,
      },
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

    confirmationModal({
      buttons: modalButtons,
      ...modalLabels,
    });
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

    confirmationModal({
      buttons: modalButtons,
      ...modalLabels,
    });
  };

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
    });

    return [...lineItemsToBeAdded.map(formatItem), ...lineItemsToBeUpdated.map(formatItem)];
  };

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
        const resp = await apiClient.post(STOCK_MOVEMENT_UPDATE_ITEMS(queryParams.id),
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
      setValue('values.lineItems', itemCandidatesToSave.filter(
        (item) => item.quantityRequested && item.quantityRequested !== '0',
      ));
    }
    return null;
  };

  const transitionToNextStep = async () => {
    const formValues = getValues();
    try {
      spinner.show();
      const payload = { status: RequisitionStatus.CHECKING };
      if (formValues.values.statusCode === RequisitionStatus.CREATED) {
        await apiClient.post(STOCK_MOVEMENT_UPDATE_STATUS(queryParams.id), payload);
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
      await apiClient.post(STOCK_MOVEMENT_UPDATE_INVENTORY_ITEMS(queryParams.id), payload);
      transitionToNextStep();
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
      // Find all items with mismatched items to show in the confirmation modal
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

  const getItemsMapByProductCode = (lineItems) =>
    _.groupBy(lineItems, (item) => item.product?.productCode);

  const checkDuplicatesSaveAndTransitionToNextStep = async (formValues, lineItems) => {
    const itemsMap = getItemsMapByProductCode(lineItems);
    const duplicateGroups = Object.values(itemsMap).filter((g) => g.length > 1);
    const hasDuplicates = duplicateGroups.length > 0;
    const skipConfirm =
      formValues.values.origin.locationType.locationTypeCode === locationType.SUPPLIER
      || !formValues.values.hasManageInventory;

    if (hasDuplicates && !skipConfirm) {
      const shouldUpdate = await openModal(
        { data: duplicateGroups.flat(), type: modalWithTableType.DUPLICATES },
      );
      if (!shouldUpdate) {
        return Promise.reject();
      }
    }

    return saveAndTransitionToNextStep(lineItems);
  };

  const nextPage = async () => {
    await trigger();
    if (!isValid) {
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
    await trigger();
    if (!isValid) {
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
    saveItems(lineItems);
  };

  const saveAndExit = async () => {
    await trigger();
    if (!isValid) {
      confirmAction(
        () => {
          window.location = STOCK_MOVEMENT_URL.show(queryParams.id);
        },
        'react.stockMovement.confirmExpiryDateUpdate.message',
        'This will update the expiry date across all depots in the system. Are you sure you want to proceed? ',
      );
      return;
    }

    try {
      spinner.show();
      await saveRequisitionItemsInCurrentStep(getValues('values.lineItems'));
      window.location = STOCK_MOVEMENT_URL.show(queryParams.id);
    } finally {
      spinner.hide();
    }
  };

  const removeSavedRow = async (itemId) => {
    try {
      spinner.show();
      await apiClient.delete(STOCK_MOVEMENT_ITEM_REMOVE(itemId));
    } finally {
      spinner.hide();
    }
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
    const response = await stockMovementApi.getStockMovementItems(queryParams.id,
      { stepNumber: InboundWorkflowState.ADD_ITEMS });
    setLineItems(response, showOnlyImportedItems);
  };

  const removeAllRows = async () => {
    try {
      spinner.show();
      await apiClient.delete(STOCK_MOVEMENT_REMOVE_ALL_ITEMS(queryParams.id));
      setValue('currentLineItems', []);
      setValue('values.lineItems', defaultTableRow);
      await fetchLineItems();
    } finally {
      spinner.hide();
    }
  };

  const previousPage = async () => {
    await trigger();
    if (!isValid) {
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

  const fetchAddItemsPageData = async () => {
    const response = await apiClient.get(STOCK_MOVEMENT_BY_ID(queryParams.id));
    const { data } = response.data;
    const transformedData = {
      ...data,
      identifier: data.identifier,
      stockMovementId: data.id,
    };
    dispatch(
      updateWorkflowHeader(createInboundWorkflowHeader(data), data.displayStatus.name),
    );
    setValue('values', transformedData);
  };

  const fetchData = async () => {
    if (!queryParams.id) {
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
      dispatch(updateWorkflowHeader([], null));
      // In case of an error, redirect to the "create" step without the id parameter
      history.push({
        pathname: location.pathname,
        search: queryString.stringify({
          ...queryParams,
          id: undefined,
          step: InboundV2Step.CREATE,
        }, { skipNull: true }),
      });
    } finally {
      setLoading(false);
      spinner.hide();
    }
  };

  const importTemplate = async (event) => {
    try {
      spinner.show();
      const formData = new FormData();
      const file = event.target.files[0];
      const { stockMovementId } = getValues('values');

      formData.append('importFile', file.slice(0, file.size, 'text/csv'));
      const config = {
        headers: {
          'content-type': 'multipart/form-data',
        },
      };

      await stockMovementApi.importCsv(stockMovementId, formData, config);

      fetchLineItems(true);
      const { lineItems } = getValues('values');
      const lastLineItem = _.last(lineItems);
      const isLastProductNil = _.isNil(lastLineItem?.product);

      if (isLastProductNil) {
        setValue('values.lineItems', defaultTableRow);
      }
    } finally {
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

  const exportTemplate = async () => {
    const { lineItems, identifier, stockMovementId } = getValues('values');
    try {
      spinner.show();
      await saveRequisitionItemsInCurrentStep(lineItems);
      const response = await stockMovementApi.exportCsv(stockMovementId);
      fileDownload(response.data, `ItemList${identifier ? `-${identifier}` : ''}.csv`, 'text/csv');
    } finally {
      spinner.hide();
    }
  };

  const addNewLine = () => {
    append(defaultTableRow);
  };

  useEffect(() => {
    fetchData();
  }, []);

  return {
    control,
    getValues,
    setValue,
    handleSubmit,
    errors,
    isValid,
    trigger,
    loading,
    nextPage,
    save,
    removeSavedRow,
    removeAllRows,
    saveAndExit,
    previousPage,
    refresh,
    exportTemplate,
    importTemplate,
    addNewLine,
    removeRow,
    lineItemsArrayFields,
    isModalOpen,
    modalData,
    modalType,
    handleModalResponse,
  };
};

export default useInboundAddItemsForm;
