import React, { useEffect, useMemo, useState } from 'react';

import { zodResolver } from '@hookform/resolvers/zod';
import moment from 'moment';
import { confirmAlert } from 'react-confirm-alert';
import { useForm } from 'react-hook-form';
import Alert from 'react-s-alert';

import {
  STOCK_MOVEMENT_BY_ID, STOCK_MOVEMENT_ITEM_REMOVE,
  STOCK_MOVEMENT_ITEMS, STOCK_MOVEMENT_REMOVE_ALL_ITEMS,
  STOCK_MOVEMENT_UPDATE_INVENTORY_ITEMS,
  STOCK_MOVEMENT_UPDATE_ITEMS,
  STOCK_MOVEMENT_UPDATE_STATUS,
} from 'api/urls';
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import { DateFormat } from 'consts/timeFormat';
import useInboundAddItemsValidation from 'hooks/inboundV2/addItems/useInboundAddItemsValidation';
import useQueryParams from 'hooks/useQueryParams';
import useSpinner from 'hooks/useSpinner';
import useTranslate from 'hooks/useTranslate';
import apiClient from 'utils/apiClient';

const useInboundAddItemsForm = ({ next, previous }) => {
  const [loading, setLoading] = useState(false);
  const spinner = useSpinner();
  const { validationSchema } = useInboundAddItemsValidation();
  const queryParams = useQueryParams();
  const translate = useTranslate();
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
      newItem: false,
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

  const confirmSave = (onConfirm) => {
    confirmAlert({
      title: translate('react.stockMovement.message.confirmSave.label', 'Confirm save'),
      message: translate(
        'react.stockMovement.confirmSave.message',
        'Are you sure you want to save? There are some lines with empty or zero quantity, those lines will be deleted.',
      ),
      buttons: [
        {
          label: translate('react.default.yes.label', 'Yes'),
          onClick: onConfirm,
        },
        { label: translate('react.default.no.label', 'No') },
      ],
    });
  };

  const confirmInventoryItemExpirationDateUpdate = (onConfirm) => {
    confirmAlert({
      title: translate('react.stockMovement.message.confirmSave.label', 'Confirm save'),
      message: translate(
        'react.stockMovement.confirmExpiryDateUpdate.message',
        'This will update the expiry date across all depots in the system. Are you sure you want to proceed?',
      ),
      buttons: [
        {
          label: translate('react.default.yes.label', 'Yes'),
          onClick: onConfirm,
        },
        {
          label: translate('react.default.no.label', 'No'),
        },
      ],
    });
  };

  const getLineItemsToBeSaved = (lineItems) => {
    const lineItemsToBeAdded = lineItems.filter(
      (item) => !item.statusCode && item.quantityRequested && item.quantityRequested !== '0' && item.product,
    );

    const lineItemsWithStatus = lineItems.filter((item) => item.statusCode);
    const lineItemsToBeUpdated = [];

    lineItemsWithStatus.forEach((item) => {
      const oldItem = getValues()
        .currentLineItems
        .find((old) => old.id === item.id);
      if (!oldItem) return;

      const oldQty = Number(oldItem.quantityRequested) || 0;
      const newQty = Number(item.quantityRequested) || 0;
      const oldRecipient = oldItem.recipient?.id ?? oldItem.recipient;
      const newRecipient = item.recipient?.id ?? item.recipient;

      const keyIntersection = Object.keys(oldItem).filter((key) => key !== 'product' && key in item);

      const hasChanges = keyIntersection.some((key) => !Object.is(item[key], oldItem[key]));

      if (hasChanges || item.product.id !== oldItem.product.id || newQty !== oldQty
        || newRecipient !== oldRecipient) {
        lineItemsToBeUpdated.push(item);
      } else if (
        item.inventoryItem?.expirationDate
        && item.expirationDate
        && item.inventoryItem.expirationDate !== item.expirationDate
      ) {
        lineItemsToBeUpdated.push(item);
      }
    });

    const formatItem = (item) => ({
      id: item.id,
      product: { id: item.product.id },
      quantityRequested: item.quantityRequested,
      palletName: item.palletName,
      boxName: item.boxName,
      lotNumber: item.lotNumber,
      expirationDate: item.expirationDate,
      recipient: { id: item.recipient?.id || '' },
      sortOrder: item.sortOrder,
    });

    return [
      ...lineItemsToBeAdded.map(formatItem),
      ...lineItemsToBeUpdated.map(formatItem),
    ];
  };

  const confirmTransition = (onConfirm, items) => {
    confirmAlert({
      title: translate('react.stockMovement.confirmTransition.label', 'You have entered the same code twice. Do you want to continue?'),
      message: items.map((item) => (
        <p key={item.sortOrder}>
          {`${item.product.productCode} ${item.product.displayNames?.default || item.product.name} ${item.quantityRequested}`}
        </p>
      )),
      buttons: [
        {
          label: translate('react.default.yes.label', 'Yes'),
          onClick: onConfirm,
        },
        {
          label: translate('react.default.no.label', 'No'),
        },
      ],
    });
  };

  const transformLineItem = (item) => ({
    ...item,
    itemId: item.id,
    product: item.product
      ? {
        ...item.product,
        label: item.product.label || `${item.product.id} - ${item.product.name}`,
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
  });

  const saveRequisitionItemsInCurrentStep = async (itemCandidatesToSave) => {
    const itemsToSave = getLineItemsToBeSaved(itemCandidatesToSave).map((item) => ({
      ...item,
      expirationDate: item.expirationDate ? moment(item.expirationDate)
        .format(DateFormat.MM_DD_YYYY) : null,
    }));
    if (!itemsToSave.length) return null;
    const payload = {
      id: queryParams.id,
      lineItems: itemsToSave,
    };

    try {
      spinner.show();
      const resp = await apiClient.post(STOCK_MOVEMENT_UPDATE_ITEMS(queryParams.id), payload);
      const transformedData = {
        ...resp.data.data,
        lineItems: resp.data.data.lineItems?.map(transformLineItem),
      };
      setValue('currentLineItems', transformedData.lineItems);
      setValue('values', transformedData);
      return resp;
    } finally {
      spinner.hide();
    }
  };

  const transitionToNextStep = async (formValues) => {
    try {
      spinner.show();
      const payload = { status: 'CHECKING' };
      if (formValues.values.statusCode === 'CREATED') {
        await apiClient.post(STOCK_MOVEMENT_UPDATE_STATUS(queryParams.id), payload);
      }
      return Promise.resolve();
    } finally {
      spinner.hide();
    }
  };

  const transitionToNextStepAndChangePage = async (formValues) => {
    try {
      spinner.show();
      await transitionToNextStep(formValues);
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
      transitionToNextStepAndChangePage(formValues);
    } finally {
      spinner.hide();
    }
  };

  const saveAndTransitionToNextStep = async (values, lineItems) => {
    try {
      spinner.show();

      const resp = await saveRequisitionItemsInCurrentStep(lineItems);
      const updatedValues = resp ? {
        ...values,
        lineItems: resp.data.data.lineItems,
      } : values;
      const updatedLineItems = updatedValues.lineItems;

      const hasExpirationDateMismatch = updatedLineItems?.some((item) =>
        item.inventoryItem && item.expirationDate !== item.inventoryItem.expirationDate);

      const hasNonZeroQuantity = updatedLineItems?.some((item) =>
        item.inventoryItem?.quantity && item.inventoryItem.quantity !== '0');

      if (hasExpirationDateMismatch) {
        if (hasNonZeroQuantity) {
          confirmInventoryItemExpirationDateUpdate(() =>
            updateInventoryItemsAndTransitionToNextStep(updatedValues, lineItems));
        } else {
          updateInventoryItemsAndTransitionToNextStep(updatedValues, lineItems);
        }
      } else {
        transitionToNextStepAndChangePage(updatedValues);
      }
    } finally {
      spinner.hide();
    }
  };

  const checkDuplicatesSaveAndTransitionToNextStep = (formValues, lineItems) => {
    const itemsMap = lineItems.reduce((acc, item) => {
      const { productCode } = item.product;
      if (!acc[productCode]) {
        acc[productCode] = [];
      }
      acc[productCode].push(item);
      return acc;
    }, {});
    const itemsWithSameCode = Object.values(itemsMap)
      .filter((item) => item.length > 1);
    const itemsWithSameCodeFlattened = itemsWithSameCode.flat();
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
    trigger();
    if (!isValid) return;

    const formValues = getValues();
    const lineItems = formValues.values.lineItems
      .filter((item) => item?.product)
      .map((item) => ({
        ...item,
        expirationDate: item.expirationDate ? moment(item.expirationDate)
          .format(DateFormat.MM_DD_YYYY) : null,
      }));

    const hasInvalidQuantity = lineItems.some((item) => !item.quantityRequested || item.quantityRequested === '0');

    if (hasInvalidQuantity) {
      await confirmSave(() => checkDuplicatesSaveAndTransitionToNextStep(formValues, lineItems));
    } else {
      await checkDuplicatesSaveAndTransitionToNextStep(formValues, lineItems);
    }
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
      spinner.hide();
    }
  };

  const save = () => {
    trigger();
    if (!isValid) return;
    const lineItems = getValues()
      .values
      .lineItems
      .filter((item) => Object.keys(item).length > 0);
    const hasInvalidQuantity = lineItems.some((item) => !item.quantityRequested || item.quantityRequested === '0');

    if (hasInvalidQuantity) {
      confirmSave(() => saveItems(lineItems));
    } else {
      saveItems(lineItems);
    }
  };

  const saveAndExit = async () => {
    const formValues = getValues();
    const lineItems = formValues.values.lineItems.filter((item) => Object.keys(item).length > 0);
    if (isValid) {
      try {
        spinner.show();
        await saveRequisitionItemsInCurrentStep(lineItems);
        window.location = STOCK_MOVEMENT_URL.show(queryParams.id);
      } finally {
        spinner.hide();
      }
    } else {
      confirmAlert({
        title: translate('react.stockMovement.confirmExit.label', 'Confirm save'),
        message: translate(
          'react.stockMovement.confirmExit.message',
          'Validation errors occurred. Are you sure you want to exit and lose unsaved data?',
        ),
        buttons: [
          {
            label: translate('react.default.yes.label', 'Yes'),
            onClick: () => {
              window.location = STOCK_MOVEMENT_URL.show(queryParams.id);
            },
          },
          {
            label: translate('react.default.no.label', 'No'),
          },
        ],
      });
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

  const removeAll = async () => {
    try {
      spinner.show();
      await apiClient.delete(STOCK_MOVEMENT_REMOVE_ALL_ITEMS(queryParams.id));
      setValue('totalCount', 1);
      setValue('currentLineItems', []);
      setValue('values', { ...getValues('values'), lineItems: new Array(1).fill({ sortOrder: 100 }) });
      // eslint-disable-next-line no-use-before-define
      await fetchLineItems();
    } finally {
      spinner.hide();
    }
  };

  const previousPage = async () => {
    trigger();
    if (isValid) {
      try {
        spinner.show();
        await saveRequisitionItemsInCurrentStep(getValues().values.lineItems);
        previous();
      } finally {
        spinner.hide();
      }
    } else {
      confirmAlert({
        title: translate('react.stockMovement.confirmPreviousPage.label', 'Validation error'),
        message: translate('react.stockMovement.confirmPreviousPage.message.label', 'Cannot save due to validation error on page'),
        buttons: [
          {
            label: translate('react.stockMovement.confirmPreviousPage.correctError.label', 'Correct error'),
          },
          {
            label: translate('react.stockMovement.confirmPreviousPage.continue.label', 'Continue (lose unsaved work)'),
            onClick: () => previous(),
          },
        ],
      });
    }
  };

  const fetchAddItemsPageData = async () => {
    if (!queryParams.id) return;

    const response = await apiClient.get(STOCK_MOVEMENT_BY_ID(queryParams.id));
    const { totalCount, data } = response.data;
    const transformedData = {
      ...data,
      lineItems: data.lineItems?.map(transformLineItem),
    };

    setValue('values', transformedData);
    setValue('totalCount', totalCount || 1);
  };

  const loadMoreRows = async ({ startIndex }) => {
    setValue('isFirstPageLoaded', true);
    try {
      spinner.show();
      const response = await apiClient.get(`${STOCK_MOVEMENT_ITEMS(queryParams.id)}?offset=${startIndex}&max=${getValues().pageSize}&stepNumber=2`);
      // eslint-disable-next-line no-use-before-define
      setLineItems(response, startIndex);
    } finally {
      spinner.hide();
    }
  };

  const uniqueById = (array) => {
    const seen = new Map();
    return array.filter((item) => {
      if (!seen.has(item.id)) {
        seen.set(item.id, true);
        return true;
      }
      return false;
    });
  };

  const setLineItems = (response, startIndex) => {
    const { data } = response.data;
    const lineItemsData = data.length === 0 && getValues('values.lineItems').length === 0
      ? [{ sortOrder: 100 }]
      : data.map((val) => ({
        ...transformLineItem(val),
        itemId: val.id,
        disabled: true,
      }));
    const newSortOrder = (lineItemsData.length > 0
      ? lineItemsData[lineItemsData.length - 1].sortOrder : 0) + 100;
    setValue('sortOrder', newSortOrder);
    setValue('currentLineItems', getValues().isPaginated ? uniqueById([...getValues('currentLineItems'), ...data]) : data);
    setValue('values.lineItems', getValues().isPaginated ? uniqueById([...getValues('values.lineItems'), ...lineItemsData]) : lineItemsData);

    if (startIndex !== null && getValues('values.lineItems').length !== getValues().totalCount) {
      loadMoreRows({ startIndex: startIndex + getValues().pageSize });
    }
  };

  const fetchLineItems = async () => {
    if (!queryParams.id) return;
    const url = `${STOCK_MOVEMENT_ITEMS(queryParams.id)}?stepNumber=2`;
    const response = await apiClient.get(url);

    setValue('totalCount', response.data.data.length);
    setLineItems(response, null);
  };

  const fetchData = async () => {
    setLoading(true);
    spinner.show();
    try {
      await fetchAddItemsPageData();
      if (getValues().isPaginated) {
        await fetchLineItems();
      }
    } finally {
      setLoading(false);
      spinner.hide();
    }
  };
  useEffect(() => {
    if (queryParams.step === 'ADD_ITEMS') {
      fetchData();
    }
  }, [queryParams.step]);
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
  };
};

export default useInboundAddItemsForm;
