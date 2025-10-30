import React, { useEffect, useMemo, useState } from 'react';

import { zodResolver } from '@hookform/resolvers/zod';
import fileDownload from 'js-file-download';
import _ from 'lodash';
import queryString from 'query-string';
import { useForm } from 'react-hook-form';
import { useDispatch } from 'react-redux';
import { useHistory, useLocation } from 'react-router-dom';
import Alert from 'react-s-alert';

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
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import InboundV2Step from 'consts/InboundStep';
import RequisitionStatus from 'consts/requisitionStatus';
import { DateFormat, DateFormatDateFns } from 'consts/timeFormat';
import { OutboundWorkflowState } from 'consts/WorkflowState';
import useInboundAddItemsValidation from 'hooks/inboundV2/addItems/useInboundAddItemsValidation';
import useQueryParams from 'hooks/useQueryParams';
import useSpinner from 'hooks/useSpinner';
import useTranslate from 'hooks/useTranslate';
import apiClient from 'utils/apiClient';
import confirmationModal from 'utils/confirmationModalUtils';
import createInboundWorkflowHeader from 'utils/createInboundWorkflowHeader';
import dateWithoutTimeZone, { formatDateToString } from 'utils/dateUtils';

const useInboundAddItemsForm = ({
  next,
  previous,
}) => {
  const [loading, setLoading] = useState(false);
  // State used to trigger focus reset when changed. When this counter changes,
  // it will reset the focus by clearing the RowIndex and ColumnId in useEffect.
  const [refreshFocusCounter, setRefreshFocusCounter] = useState(0);
  const history = useHistory();
  const location = useLocation();

  const spinner = useSpinner();
  const { validationSchema } = useInboundAddItemsValidation();
  const queryParams = useQueryParams();
  const translate = useTranslate();
  const dispatch = useDispatch();
  const defaultValues = useMemo(() => {
    const values = {
      currentLineItems: [],
      sortOrder: 0,
      values: {
        lineItems: [{
          palletName: '',
          boxName: '',
          product: undefined,
          lotNumber: '',
          expirationDate: '',
          quantityRequested: undefined,
          recipient: undefined,
        }],
      },
      totalCount: 0,
      isFirstPageLoaded: false,
      isPaginated: true,
    };
    return values;
  }, []);

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

  const confirmTransition = (onConfirm, items) => {
    const modalLabels = {
      title: {
        label: 'react.stockMovement.confirmTransition.label',
        default: 'You have entered the same code twice. Do you want to continue?',
      },
      content: {
        label: '',
        default: items.map((item) => (
          <p key={item.sortOrder}>
            {`${item.product.productCode} ${item.product.displayNames?.default || item.product.name} ${item.quantityRequested}`}
          </p>
        )),
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

  const resetFocus = () => {
    setRefreshFocusCounter((prev) => prev + 1);
  };

  const getLineItemsToBeSaved = (lineItems) => {
    const lineItemsToBeAdded = lineItems.filter(
      (item) => !item.statusCode && item.quantityRequested && item.quantityRequested !== '0' && item.product,
    );

    const lineItemsToBeUpdated = lineItems.filter((item) =>
      item.statusCode
      && getValues('currentLineItems').some((oldItem) => oldItem.id === item.id && !_.isEqual(item, oldItem)));

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
      recipient: { id: item.recipient?.id || '' },
      sortOrder: item.sortOrder,
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
    expirationDate: formatDateToString({
      date: item.expirationDate,
      dateFormat: DateFormatDateFns.DD_MMM_YYYY,
    }),
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
      const updateInventoryItemsPayload = {
        id: queryParams.id,
        lineItems: itemsToSave.map((item) => ({
          product: item.product,
          lotNumber: item.lotNumber,
          expirationDate: item.expirationDate,
          quantityRequested: item.quantityRequested,
        })),
      };

      const updateItemsPayload = {
        id: queryParams.id,
        lineItems: itemsToSave,
      };
      try {
        spinner.show();
        // Update inventory items as part of the save process
        await apiClient.post(STOCK_MOVEMENT_UPDATE_INVENTORY_ITEMS(queryParams.id),
          updateInventoryItemsPayload);
        const resp = await apiClient.post(STOCK_MOVEMENT_UPDATE_ITEMS(queryParams.id),
          updateItemsPayload);
        const { data } = resp.data;
        setValue('currentLineItems', data.lineItems?.map(transformLineItem));
        setValue('values.lineItems', data.lineItems?.map(transformLineItem));
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

  const transitionToNextStep = async (formValues) => {
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

  const updateInventoryItemsAndTransitionToNextStep = async (formValues, lineItems) => {
    const itemsToSave = getLineItemsToBeSaved(lineItems);
    const payload = {
      id: queryParams.id,
      lineItems: itemsToSave,
    };
    try {
      spinner.show();
      await apiClient.post(STOCK_MOVEMENT_UPDATE_INVENTORY_ITEMS(queryParams.id), payload);
      transitionToNextStep(formValues);
    } finally {
      spinner.hide();
    }
  };

  const handleExpirationDateMismatch = (updatedValues, lineItems, hasNonZeroQuantity) => {
    if (hasNonZeroQuantity) {
      confirmAction(
        () => updateInventoryItemsAndTransitionToNextStep(updatedValues, lineItems),
        'react.stockMovement.confirmExpiryDateUpdate.message',
        'This will update the expiry date across all depots in the system. Are you sure you want to proceed?',
      );
    } else {
      updateInventoryItemsAndTransitionToNextStep(updatedValues, lineItems);
    }
  };

  const handleTransition = (updatedValues, lineItems) => {
    const updatedLineItems = updatedValues.lineItems;
    const hasExpirationDateMismatch = updatedLineItems?.some(
      (item) => item.inventoryItem && item.expirationDate
        !== item.inventoryItem.expirationDate,
    );

    const hasNonZeroQuantity = updatedLineItems?.some(
      (item) => item.inventoryItem?.quantity && item.inventoryItem.quantity !== '0',
    );

    if (hasExpirationDateMismatch) {
      handleExpirationDateMismatch(updatedValues, lineItems, hasNonZeroQuantity);
    } else {
      transitionToNextStep(updatedValues);
    }
  };

  const saveAndTransitionToNextStep = async (values, lineItems) => {
    try {
      spinner.show();
      const resp = await saveRequisitionItemsInCurrentStep(lineItems);
      const updatedValues = resp
        ? {
          ...values,
          lineItems: resp.data.data.lineItems,
        }
        : values;

      handleTransition(updatedValues, lineItems);
    } finally {
      resetFocus();
      spinner.hide();
    }
  };

  const getItemsMapByProductCode = (lineItems) =>
    lineItems.reduce((acc, item) => {
      const { productCode } = item.product;
      return {
        ...acc,
        [productCode]: [...(acc[productCode] || []), item],
      };
    }, {});

  const checkDuplicatesSaveAndTransitionToNextStep = (formValues, lineItems) => {
    const itemsMap = getItemsMapByProductCode(lineItems);

    const itemsWithSameCodeFlattened = Object.values(itemsMap)
      .filter((item) => item.length > 1)
      .flat();
    if (Object.values(itemsMap)
      .some((item) => item.length > 1) && !(formValues.values.origin.locationType.locationTypeCode === 'SUPPLIER'
      || !formValues.values.hasManageInventory)) {
      confirmTransition(
        () => saveAndTransitionToNextStep(formValues, lineItems),
        itemsWithSameCodeFlattened,
      );
    } else {
      saveAndTransitionToNextStep(formValues, lineItems);
    }
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

      Alert.success(
        translate('react.stockMovement.alert.saveSuccess.label', 'Changes saved successfully'),
        { timeout: 3000 },
      );
    } finally {
      resetFocus();
      spinner.hide();
    }
  };

  const getFilteredLineItems = (formValues) =>
    formValues.values.lineItems.filter((item) => Object.keys(item).length > 0);

  const save = async () => {
    await trigger();
    if (isValid) {
      const lineItems = getFilteredLineItems(getValues());
      const hasInvalidQuantity = checkInvalidQuantities(lineItems);

      if (hasInvalidQuantity) {
        confirmAction(
          () => saveItems(lineItems),
          'react.stockMovement.confirmSave.message',
          'Are you sure you want to save? There are some lines with empty or zero quantity, those lines will be deleted.',
        );
      } else {
        saveItems(lineItems);
      }
    }
  };

  const saveAndExit = async () => {
    const lineItems = getFilteredLineItems(getValues());

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
      await saveRequisitionItemsInCurrentStep(lineItems);
      window.location = STOCK_MOVEMENT_URL.show(queryParams.id);
    } finally {
      resetFocus();
      spinner.hide();
    }
  };

  const updateTotalCount = (value) => {
    const totalCount = getValues('totalCount');
    setValue('totalCount', totalCount + value === 0 ? 1 : totalCount + value);
  };

  const removeItem = async (itemId) => {
    try {
      spinner.show();
      await apiClient.delete(STOCK_MOVEMENT_ITEM_REMOVE(itemId));
    } finally {
      spinner.hide();
    }
  };

  const setLineItems = (response, startIndex, showOnlyImportedItems) => {
    const { data } = response.data;
    const transformedData = data.map(transformLineItem);
    const lineItemsData = !data.length && getValues('values.lineItems').length === 0
      ? [{ sortOrder: 100 }]
      : transformedData;
    const newSortOrder = (lineItemsData.length > 0
      ? lineItemsData[lineItemsData.length - 1].sortOrder : 0) + 100;

    setValue('sortOrder', newSortOrder);
    setValue('currentLineItems', getValues().isPaginated ? _.uniqBy(_.concat(transformedData, ...getValues('currentLineItems')), 'id') : transformedData);
    setValue('values.lineItems', getValues().isPaginated && !showOnlyImportedItems ? _.uniqBy(_.concat(lineItemsData, ...getValues('values.lineItems')), 'id') : lineItemsData);
  };

  const fetchLineItems = async (showOnlyImportedItems = false) => {
    if (queryParams.id) {
      const response = await stockMovementApi.getStockMovementItems(queryParams.id,
        { stepNumber: OutboundWorkflowState.ADD_ITEMS });
      setValue('totalCount', response.data.data.length);
      setLineItems(response, null, showOnlyImportedItems);
      await trigger();
    }
  };

  const removeAll = async () => {
    try {
      spinner.show();
      await apiClient.delete(STOCK_MOVEMENT_REMOVE_ALL_ITEMS(queryParams.id));
      setValue('totalCount', 1);
      setValue('currentLineItems', []);
      setValue('values.lineItems', new Array(1).fill({ sortOrder: 100 }));
      await fetchLineItems();
    } finally {
      resetFocus();
      spinner.hide();
    }
  };

  const previousPage = async () => {
    await trigger();
    if (isValid) {
      try {
        spinner.show();
        await saveRequisitionItemsInCurrentStep(getValues().values.lineItems);
        previous();
      } finally {
        resetFocus();
        spinner.hide();
      }
    } else {
      confirmValidationError();
    }
  };

  const fetchAddItemsPageData = async () => {
    if (queryParams.id) {
      const response = await apiClient.get(STOCK_MOVEMENT_BY_ID(queryParams.id));
      const {
        totalCount,
        data,
      } = response.data;
      const transformedData = {
        ...data,
        identifier: data.identifier,
        stockMovementId: data.id,
        lineItems: data.lineItems?.map(transformLineItem),

      };
      dispatch(
        updateWorkflowHeader(createInboundWorkflowHeader(data), data.displayStatus.name),
      );
      setValue('values', transformedData);
      setValue('totalCount', totalCount || 1);
    }
  };

  const fetchData = async () => {
    if (!queryParams.id) {
      dispatch(updateWorkflowHeader([], null));
      previous();
      return;
    }

    setLoading(true);
    spinner.show();

    try {
      await fetchAddItemsPageData();
      if (getValues().isPaginated) {
        await fetchLineItems();
      }
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
    spinner.show();
    try {
      const formData = new FormData();
      const file = event.target.files[0];
      const { stockMovementId } = getValues().values;

      formData.append('importFile', file.slice(0, file.size, 'text/csv'));
      const config = {
        headers: {
          'content-type': 'multipart/form-data',
        },
      };

      await stockMovementApi.importCsv(stockMovementId, formData, config);

      fetchLineItems(true);
      const { lineItems } = getValues().values;
      const lastLineItem = _.last(lineItems);
      const isLastProductNil = _.isNil(lastLineItem?.product);

      if (isLastProductNil) {
        setValue('values.lineItems', []);
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

  const saveItemsAndExportTemplate = async (formValues, lineItems) => {
    spinner.show();
    const { identifier, stockMovementId } = formValues;
    try {
      await saveRequisitionItemsInCurrentStep(lineItems);
      const response = await stockMovementApi.exportCsv(stockMovementId);
      fileDownload(response.data, `ItemList${identifier ? `-${identifier}` : ''}.csv`, 'text/csv');
    } finally {
      spinner.hide();
    }
  };

  const exportTemplate = async () => {
    const lineItems = _.filter(getValues().values.lineItems, (item) => !_.isEmpty(item));
    saveItemsAndExportTemplate(getValues().values, lineItems);
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
    removeItem,
    updateTotalCount,
    removeAll,
    saveAndExit,
    previousPage,
    refreshFocusCounter,
    resetFocus,
    refresh,
    exportTemplate,
    importTemplate,
  };
};

export default useInboundAddItemsForm;
