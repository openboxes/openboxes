/* eslint-disable no-restricted-syntax */
/* eslint-disable no-await-in-loop */

import {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';

import _ from 'lodash';
import moment from 'moment';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';
import {
  getCurrentLocation,
  getCycleCountsIds,
  getReasonCodes,
  getUsers,
} from 'selectors';

import { eraseDraft, fetchBinLocations, fetchUsers } from 'actions';
import { UPDATE_CYCLE_COUNT_IDS } from 'actions/types';
import cycleCountApi from 'api/services/CycleCountApi';
import { CYCLE_COUNT as GET_CYCLE_COUNTS } from 'api/urls';
import notification from 'components/Layout/notifications/notification';
import { CYCLE_COUNT } from 'consts/applicationUrls';
import { TO_RESOLVE_TAB } from 'consts/cycleCount';
import NotificationType from 'consts/notificationTypes';
import { DateFormat } from 'consts/timeFormat';
import useResolveStepValidation from 'hooks/cycleCount/useResolveStepValidation';
import useSpinner from 'hooks/useSpinner';
import useTranslate from 'hooks/useTranslate';
import confirmationModal from 'utils/confirmationModalUtils';
import trimLotNumberSpaces from 'utils/cycleCountUtils';
import dateWithoutTimeZone from 'utils/dateUtils';
import exportFileFromApi from 'utils/file-download-util';
import { checkBinLocationSupport } from 'utils/supportedActivitiesUtils';

// Managing state for all tables, operations on shared state (from resolve step)
const useResolveStep = () => {
  // Table data is stored using useRef to avoid re-renders onBlur
  // (it removes focus while selecting new fields)
  const tableData = useRef([]);
  const cycleCountsWithItemsWithoutRecount = useRef([]);
  // Saving selected "recounted by" and "date recounted" options using useRef
  const recountedBy = useRef({});
  const defaultRecountedBy = useRef({});
  const dateRecounted = useRef({});
  const [isStepEditable, setIsStepEditable] = useState(true);
  const { show, hide } = useSpinner();
  const history = useHistory();
  const [isSaveDisabled, setIsSaveDisabled] = useState(false);
  const [sortByProductName, setSortByProductName] = useState(false);
  // Here we store cycle count IDs that were updated,
  // and we will mark them as updated before saving.
  const itemsToUpdate = useRef([]);
  const {
    validationErrors,
    isRootCauseWarningSkipped,
    triggerValidation,
    forceRerender,
    validateRootCauses,
    shouldHaveRootCause,
    showEmptyRootCauseWarning,
    resetValidationState,
  } = useResolveStepValidation({ tableData });

  const dispatch = useDispatch();

  const {
    cycleCountIds,
    currentLocation,
    reasonCodes,
    users,
    currentUser,
  } = useSelector((state) => ({
    users: getUsers(state),
    cycleCountIds: getCycleCountsIds(state),
    reasonCodes: getReasonCodes(state),
    currentLocation: getCurrentLocation(state),
    currentUser: state.session.user,
  }));

  const translate = useTranslate();

  const showBinLocation = useMemo(() =>
    checkBinLocationSupport(currentLocation.supportedActivities), [currentLocation?.id]);

  useEffect(() => {
    if (showBinLocation) {
      dispatch(fetchBinLocations(
        currentLocation?.id,
        [],
        'sortOrder,locationType,name',
      ));
    }
  }, [currentLocation?.id]);

  const mapRootCauseToSelectedOption = (rootCause) => (rootCause ? ({
    id: rootCause?.name,
    label: reasonCodes?.find?.((reasonCode) => reasonCode?.id === rootCause?.name)?.label,
    value: rootCause?.name,
  }) : null);

  const mergeCycleCountItems = (items) => {
    const maxCountIndex = _.maxBy(items, 'countIndex')?.countIndex;

    // If no recount items exist, we should have an empty table at this step
    if (maxCountIndex === 0) {
      return [];
    }

    const duplicatedItems = _.groupBy(items,
      (item) => `${item.binLocation?.id}-${item?.inventoryItem?.lotNumber}`);

    return Object.values(duplicatedItems).flatMap((itemsToMerge) => {
      // When inventory is deleted the QoH is zero so the recount item was deleted,
      // but it is still returning the original count item (because QoH was not zero
      // at the time of the original count), so when we don't have more items than those
      // which are coming from the counting step we have to filter those items out. It is
      // not changed on the backend, because in that case we will lose the information
      // about the original count
      if (_.every(itemsToMerge, (item) => item.countIndex < maxCountIndex && maxCountIndex > 0)) {
        return null;
      }

      // Mapping items that are created on the recount step
      if (itemsToMerge.length === 1) {
        const item = itemsToMerge[0];
        return [{
          ...item,
          quantityRecounted: item?.quantityCounted,
          dateRecounted: item?.dateCounted,
          recountedBy: item?.assignee,
          quantityCounted: null,
          commentFromCount: null,
          dateCounted: null,
          countedBy: null,
          rootCause: mapRootCauseToSelectedOption(item?.discrepancyReasonCode),
        }];
      }

      const groupedByCountIndex = _.groupBy(itemsToMerge, 'countIndex');
      const itemFromCount = _.find(itemsToMerge, (item) => item.countIndex === maxCountIndex - 1);
      const itemsFromResolve = groupedByCountIndex[maxCountIndex] || [];

      // Merging items coming from count + recount step
      return itemsFromResolve.map((item) => ({
        ...itemFromCount,
        ...item,
        quantityOnHand: item?.quantityOnHand,
        commentFromCount: itemFromCount?.comment,
        quantityRecounted: item?.quantityCounted,
        quantityCounted: itemFromCount?.quantityCounted,
        dateCounted: itemFromCount?.dateCounted,
        dateRecounted: item?.dateCounted,
        countedBy: itemFromCount?.assignee,
        recountedBy: item?.assignee,
        rootCause: mapRootCauseToSelectedOption(item?.discrepancyReasonCode),
      }));
    }).filter(Boolean);
  };

  const getItemsWithoutRecountIndexes = (items) => {
    const duplicatedItems = _.groupBy(items,
      (item) => `${item.binLocation?.id}-${item?.inventoryItem?.lotNumber}`);

    const duplicatedItemsValues = Object.values(duplicatedItems);

    return duplicatedItemsValues
      .filter((group) => group.length < 2 && group.every((item) => item.countIndex === 0)).flat();
  };

  // Function used for maintaining the same order in the resolve tab between saves.
  // It's needed because items are returned in the same order as they are on the record stock,
  // but we want to have editable items at the bottom in the order that those items were
  // added to the table.
  const moveCustomItemsToTheBottom = (cycleCountItems) => {
    const { false: originalItems, true: customItems } = _.groupBy(
      cycleCountItems,
      'custom',
    );

    if (!customItems) {
      return originalItems;
    }

    const customItemsSortedByCreationDate = _.sortBy(customItems, 'dateCreated');

    if (!originalItems) {
      return customItemsSortedByCreationDate;
    }

    return [...originalItems, ...customItemsSortedByCreationDate];
  };

  const refetchData = async (ids = cycleCountIds) => {
    if (ids.length === 0) {
      return;
    }
    try {
      show();
      const { data } = await cycleCountApi.getCycleCounts(
        currentLocation?.id,
        ids,
        sortByProductName && 'productName',
      );
      tableData.current = data?.data?.map((cycleCount) => {
        const mergedItems = mergeCycleCountItems(cycleCount.cycleCountItems);
        return ({ ...cycleCount, cycleCountItems: moveCustomItemsToTheBottom(mergedItems) || [] });
      });
      cycleCountsWithItemsWithoutRecount.current = data?.data?.reduce((acc, cycleCount) => {
        const cycleCountItems = getItemsWithoutRecountIndexes(cycleCount.cycleCountItems);
        if (!cycleCountItems.length) {
          return acc;
        }
        return [
          ...acc,
          {
            ...cycleCount,
            cycleCountItems,
          },
        ];
      }, []);
      const recountedDates = tableData.current?.reduce((acc, cycleCount) => ({
        ...acc,
        [cycleCount?.id]: cycleCount?.cycleCountItems?.[0]?.dateRecounted,
      }), {});
      const recountedByData = tableData.current?.reduce((acc, cycleCount) => ({
        ...acc,
        [cycleCount?.id]: cycleCount?.cycleCountItems?.[0]?.recountedBy,
      }), {});
      dateRecounted.current = recountedDates;
      recountedBy.current = recountedByData;
      defaultRecountedBy.current = recountedByData;
    } finally {
      hide();
    }
  };

  useEffect(() => {
    (async () => {
      await refetchData();
    })();
  }, [cycleCountIds, sortByProductName]);

  // Fetching data for "recounted by" dropdown
  useEffect(() => {
    if (!users?.length) {
      dispatch(fetchUsers());
    }
  }, []);

  const printRecountForm = async (format) => {
    show();
    await exportFileFromApi({
      url: GET_CYCLE_COUNTS(currentLocation?.id),
      params: { id: cycleCountIds, sortBy: sortByProductName && 'productName' },
      format,
    });
    hide();
  };

  const getField = useCallback((id, fieldName) => {
    const findCycleCount = (data) => data?.find(
      (cycleCount) => cycleCount.id === id,
    );
    const findByField = (data) => findCycleCount(data)?.cycleCountItems.find(
      (cycleCountItem) => _.get(cycleCountItem, fieldName),
    );
    return _.get(findByField(tableData.current), fieldName)
      || _.get(findByField(cycleCountsWithItemsWithoutRecount.current), fieldName);
  }, []);

  const getRecountedBy = (cycleCountId) => recountedBy.current?.[cycleCountId];

  const getDefaultRecountedBy = (cycleCountId) => defaultRecountedBy.current?.[cycleCountId];

  const getCountedBy = (cycleCountId) => {
    const countedBy = (data) => data.find(
      (cycleCount) => cycleCount?.id === cycleCountId,
    )?.cycleCountItems?.find((row) => row?.countedBy || row?.assignee);

    return countedBy(tableData.current)?.countedBy
      || countedBy(cycleCountsWithItemsWithoutRecount.current)?.assignee;
  };

  const removeRowFromState = (cycleCountId, rowId) => {
    const tableIndex = tableData.current.findIndex(
      (cycleCount) => cycleCount?.id === cycleCountId,
    );
    tableData.current = tableData.current.map((data, index) => {
      if (index === tableIndex) {
        return {
          ...data,
          cycleCountItems: data.cycleCountItems.filter((row) => row.id !== rowId),
        };
      }

      return data;
    });
  };

  const removeRow = (cycleCountId, rowId) => {
    try {
      show();
      if (!rowId.includes('newRow')) {
        cycleCountApi.deleteCycleCountItem(currentLocation?.id, rowId);
      }
      removeRowFromState(cycleCountId, rowId);
    } finally {
      triggerValidation();
      hide();
    }
  };

  const addEmptyRow = (productId, id) => {
    // ID is needed for updating appropriate row
    const emptyRow = {
      id: _.uniqueId('newRow'),
      product: {
        id: productId,
      },
      inventoryItem: {
        lotNumber: null,
        expirationDate: null,
      },
      binLocation: null,
      quantityRecounted: null,
      quantityCounted: null,
      rootCause: null,
      comment: '',
    };
    const tableIndex = tableData.current.findIndex(
      (cycleCount) => cycleCount?.id === id,
    );
    tableData.current = tableData.current.map((data, index) => {
      if (index === tableIndex) {
        return {
          ...data,
          cycleCountItems: [
            ...data.cycleCountItems,
            emptyRow,
          ],
        };
      }

      return data;
    });
    resetValidationState();
    forceRerender();
  };

  const cancelCounts = async (cycleCountRequestIdsToDelete, cycleCountIdsToDelete) => {
    try {
      show();
      await cycleCountApi.deleteRequests(currentLocation?.id, cycleCountRequestIdsToDelete);
      await refetchData();
      // Updated cycle count ids (remove from state cycle counts that have just been canceled)
      const updatedCycleCountsIds = cycleCountIds
        .filter((id) => !cycleCountIdsToDelete.includes(id));
      dispatch({
        type: UPDATE_CYCLE_COUNT_IDS,
        payload: {
          locationId: currentLocation?.id,
          cycleCounts: updatedCycleCountsIds,
        },
      });
      // If we've canceled every product in the batch, there's no reason to stay on this screen.
      if (tableData.current.length === 0) {
        history.push(CYCLE_COUNT.list(TO_RESOLVE_TAB));
      }
    } finally {
      hide();
    }
  };

  const zeroRecountItemsModalButtons = (cycleCountRequestIdsToDelete, cycleCountIdsToDelete) =>
    (onClose) => ([
      {
        variant: 'transparent',
        label: 'react.cycleCount.modal.zeroRecountItems.back.label',
        defaultLabel: 'Not Now',
        onClick: () => {
          onClose?.();
        },
      },
      {
        variant: 'primary',
        label: 'react.cycleCount.modal.zeroRecountItems.confirm.label',
        defaultLabel: 'Cancel Products',
        onClick: async () => {
          onClose?.();
          await cancelCounts(cycleCountRequestIdsToDelete, cycleCountIdsToDelete);
        },
      },
    ]);

  const openZeroRecountItemsModal = (emptyCycleCounts) => {
    const requestIds = emptyCycleCounts.map((entry) => (entry.requestId));
    const cycleCountsIdsToDelete = emptyCycleCounts.map((entry) => (entry.id));
    const productCodes = emptyCycleCounts.map((entry) => getField(entry.id, 'product.id'));
    confirmationModal({
      hideCloseButton: false,
      closeOnClickOutside: true,
      buttons: zeroRecountItemsModalButtons(requestIds, cycleCountsIdsToDelete),
      title: {
        label: 'react.cycleCount.modal.zeroRecountItems.title.label',
        default: 'Cancel Counts?',
      },
      content: {
        label: 'react.cycleCount.modal.zeroRecountItems.content.label',
        default: `Product(s) ${productCodes} have zero quantity on hand across all inventory items. Cancel the cycle count on these products to proceed.`,
        data: {
          productCodes,
        },
      },
    });
  };

  const validateExistenceOfCycleCounts = async () => {
    const { data } = await cycleCountApi.getCycleCounts(
      currentLocation?.id,
      cycleCountIds,
    );
    const {
      existingCycleCountsIds,
      canceledCycleCountsIds,
    } = tableData.current.reduce((acc, curr) => {
      if (data.data.find((cycleCount) => cycleCount.id === curr.id)) {
        return {
          ...acc,
          existingCycleCountsIds: [...acc.existingCycleCountsIds, curr.id],
        };
      }
      return {
        ...acc,
        canceledCycleCountsIds: [...acc.canceledCycleCountsIds, curr.id],
      };
    }, { existingCycleCountsIds: [], canceledCycleCountsIds: [] });
    if (canceledCycleCountsIds.length > 0) {
      dispatch({
        type: UPDATE_CYCLE_COUNT_IDS,
        payload: {
          locationId: currentLocation?.id,
          cycleCounts: existingCycleCountsIds,
        },
      });
      notification(NotificationType.ERROR_FILLED)({
        message: 'Error',
        details: translate('react.cycleCount.canceledCycleCounts.error.label',
          'Some inventory changes may not be appearing because you canceled a product in the current count/recount. Please reload the page to continue.'),
      });
      return false;
    }
    return true;
  };

  const back = () => {
    setIsStepEditable(true);
  };

  const setAllItemsUpdatedState = (cycleCountId, updated) => {
    const tableIndex = tableData.current.findIndex(
      (cycleCount) => cycleCount?.id === cycleCountId,
    );
    tableData.current = tableData.current.map((cycleCount, index) => {
      if (index === tableIndex) {
        return {
          ...cycleCount,
          cycleCountItems: cycleCount.cycleCountItems.map((item) => ({ ...item, updated })),
        };
      }
      return cycleCount;
    });
  };

  const markAllItemsAsNotUpdated = (cycleCountId) => setAllItemsUpdatedState(cycleCountId, false);

  const assignRecountedBy = (cycleCountId) => (person) => {
    if (!itemsToUpdate.current.includes(cycleCountId)) {
      itemsToUpdate.current = [...itemsToUpdate.current, cycleCountId];
    }
    recountedBy.current = { ...recountedBy.current, [cycleCountId]: person };
    defaultRecountedBy.current = { ...defaultRecountedBy.current, [cycleCountId]: person };
  };

  const getRecountedDate = (cycleCountId) => dateRecounted.current[cycleCountId] || moment.now();

  const updateRecountedDate = (cycleCountId) => (date) => {
    if (!itemsToUpdate.current.includes(cycleCountId)) {
      itemsToUpdate.current = [...itemsToUpdate.current, cycleCountId];
    }
    dateRecounted.current = {
      ...dateRecounted.current,
      [cycleCountId]: date ? date.format() : null,
    };
  };

  const getPayload = (cycleCountItem, cycleCount, shouldSetDefaultAssignee) => ({
    quantityCounted: cycleCountItem?.quantityRecounted,
    countIndex: 1,
    inventoryItem: {
      ...cycleCountItem.inventoryItem,
      product: cycleCountItem?.product?.id,
      expirationDate: dateWithoutTimeZone({
        date: cycleCountItem?.inventoryItem?.expirationDate,
        currentDateFormat: DateFormat.MMM_DD_YYYY,
        outputDateFormat: DateFormat.MM_DD_YYYY,
      }),
    },
    binLocation: cycleCountItem?.binLocation,
    comment: cycleCountItem?.comment,
    dateCounted: getRecountedDate(cycleCount?.id),
    id: cycleCountItem?.id,
    facility: cycleCountItem?.facility,
    discrepancyReasonCode: cycleCountItem?.rootCause?.id,
    assignee: shouldSetDefaultAssignee
      ? getRecountedBy(cycleCount.id)?.id ?? currentUser.id
      : getRecountedBy(cycleCount.id)?.id,
    recount: true,
  });

  const save = async ({
    shouldRefetch = true,
    shouldValidateExistence = true,
    shouldSetDefaultAssignee = false,
  }) => {
    try {
      show();
      // Before saving, we need to change the state updated to true for all items that were changed
      itemsToUpdate.current.map((cycleCountId) => setAllItemsUpdatedState(cycleCountId, true));
      itemsToUpdate.current = [];
      if (shouldValidateExistence) {
        const isValid = await validateExistenceOfCycleCounts();
        if (!isValid) {
          return;
        }
      }
      resetValidationState();
      for (const cycleCount of tableData.current) {
        const cycleCountItemsToUpdate = cycleCount.cycleCountItems
          .filter((item) => ((item.updated || !item.assignee) && !item.id.includes('newRow')))
          .map(trimLotNumberSpaces);
        const updatePayload = {
          itemsToUpdate: cycleCountItemsToUpdate.map((item) =>
            getPayload(item, cycleCount, shouldSetDefaultAssignee)),
        };
        if (updatePayload.itemsToUpdate.length > 0) {
          await cycleCountApi
            .updateCycleCountItems(updatePayload, currentLocation?.id, cycleCount.id);
        }
        const cycleCountItemsToCreate = cycleCount.cycleCountItems
          .filter((item) => item.id.includes('newRow'))
          .map(trimLotNumberSpaces);
        const createPayload = {
          itemsToCreate: cycleCountItemsToCreate.map((item) =>
            getPayload(item, cycleCount, shouldSetDefaultAssignee)),
        };
        if (createPayload.itemsToCreate.length > 0) {
          await cycleCountApi
            .createCycleCountItems(createPayload, currentLocation?.id, cycleCount.id);
        }

        // Now that we've successfully saved all the items, mark them all as not updated so that
        // we don't try to update them again next time something is changed.
        markAllItemsAsNotUpdated(cycleCount.id);
      }
    } finally {
      // After the save, refetch cycle counts so that a new row can't be saved multiple times
      if (shouldRefetch) {
        await refetchData();
      }
      hide();
    }
  };

  const next = async () => {
    const isValid = triggerValidation();
    forceRerender();
    const areCycleCountsUpToDate = await validateExistenceOfCycleCounts();
    if (!areCycleCountsUpToDate) {
      return;
    }
    if (!isValid) {
      return;
    }

    const emptyCycleCounts = tableData.current.filter(
      (cycleCount) => !cycleCount?.cycleCountItems?.length,
    );

    if (emptyCycleCounts.length) {
      openZeroRecountItemsModal(emptyCycleCounts);
      return;
    }

    const missingRootCauses = validateRootCauses();
    if (!isRootCauseWarningSkipped && missingRootCauses.length > 0) {
      showEmptyRootCauseWarning();
      return;
    }

    await save({ shouldSetDefaultAssignee: true });
    setIsStepEditable(false);
  };

  const refreshCountItems = async (cycleCountIdsForOutdatedProducts = cycleCountIds) => {
    try {
      show();
      const isValid = await validateExistenceOfCycleCounts();
      if (!isValid) {
        return;
      }
      await save({ shouldRefetch: false });
      for (const cycleCountId of cycleCountIdsForOutdatedProducts) {
        await cycleCountApi.refreshItems(currentLocation?.id, cycleCountId, true, 1);
      }
    } finally {
      hide();
      await refetchData(cycleCountIdsForOutdatedProducts);
    }
  };

  const mapItemToSubmitRecountPayload = (cycleCountItem) => ({
    assignee: cycleCountItem?.recountedBy,
    binLocation: cycleCountItem?.binLocation,
    comment: cycleCountItem?.comment,
    countIndex: 1,
    dateCounted: cycleCountItem?.dateRecounted,
    discrepancyReasonCode: cycleCountItem?.discrepancyReasonCode,
    facility: currentLocation?.id,
    id: cycleCountItem?.id,
    inventoryItem: cycleCountItem?.inventoryItem,
    product: cycleCountItem?.product,
    quantityCounted: cycleCountItem?.quantityRecounted,
  });

  const modalLabels = (outdatedProductsCount) => ({
    title: {
      label: 'react.cycleCount.modal.reviewProductsTitle.label',
      default: `${outdatedProductsCount} products have been updated`,
      data: {
        outdatedProductsCount,
      },
    },
    content: {
      label: 'react.cycleCount.modal.reviewProductsContent.label',
      default: `The inventory of ${outdatedProductsCount} products have been updated while you were working. Please review the changes and adjust your entries as needed.`,
      data: {
        outdatedProductsCount,
      },
    },
  });

  const reviewProductsModalButtons = (cycleCountIdsForOutdatedProducts) => (onClose) => ([
    {
      variant: 'primary',
      defaultLabel: 'Review Products',
      label: 'react.cycleCount.modal.reviewProducts.label',
      onClick: async () => {
        setIsSaveDisabled(false);
        setIsStepEditable(true);
        await refreshCountItems(cycleCountIdsForOutdatedProducts);
        onClose?.();
      },
    },
  ]);

  const openReviewProductsModal = (outdatedProductsCount, cycleCountIdsForOutdatedProducts) => {
    confirmationModal({
      buttons: reviewProductsModalButtons(cycleCountIdsForOutdatedProducts),
      ...modalLabels(outdatedProductsCount),
      hideCloseButton: true,
      closeOnClickOutside: false,
    });
  };

  const submitRecount = async () => {
    let outdatedProducts = 0;
    const cycleCountIdsForOutdatedProducts = [];
    try {
      show();
      const isValid = await validateExistenceOfCycleCounts();
      if (!isValid) {
        return;
      }
      await save({
        shouldRefetch: true,
        shouldValidateExistence: false,
      });
      for (const cycleCount of tableData.current) {
        try {
          await cycleCountApi.submitRecount({
            refreshQuantityOnHand: true,
            failOnOutdatedQuantity: true,
            requireRecountOnDiscrepancy: false,
            cycleCountItems: cycleCount?.cycleCountItems?.map(
              (item) => mapItemToSubmitRecountPayload(item),
            ),
          },
          currentLocation?.id,
          cycleCount?.id);
        } catch {
          cycleCountIdsForOutdatedProducts.push(cycleCount.id);
          outdatedProducts += 1;
        }
      }
      if (outdatedProducts > 0) {
        dispatch({
          type: UPDATE_CYCLE_COUNT_IDS,
          payload: {
            locationId: currentLocation?.id,
            cycleCounts: cycleCountIdsForOutdatedProducts,
          },
        });
        openReviewProductsModal(outdatedProducts, cycleCountIdsForOutdatedProducts);
        return;
      }
      dispatch(eraseDraft(currentLocation?.id, TO_RESOLVE_TAB));
      history.push(CYCLE_COUNT.list(TO_RESOLVE_TAB));
    } finally {
      hide();
    }
  };

  const updateRow = (cycleCountId, rowId, columnId, value) => {
    // Find table index, for which the row should be updated
    const tableIndex = tableData.current.findIndex(
      (cycleCount) => cycleCount?.id === cycleCountId,
    );
    // Find updated row index
    const rowIndex = tableData.current[tableIndex].cycleCountItems.findIndex(
      (row) => row.id === rowId,
    );
    // Nested path in colum names contains "_" instead of "."
    const nestedPath = columnId.replaceAll('_', '.');
    // Update data for: cycleCount (table) -> cycleCountItem (row) -> column (nestedPath)
    const valueChanged = _.get(tableData.current, `[${tableIndex}].cycleCountItems[${rowIndex}].${nestedPath}`) !== value;
    _.set(tableData.current, `[${tableIndex}].cycleCountItems[${rowIndex}].${nestedPath}`, value);

    // Mark item as updated, so that the item can be easily distinguished whether it was updated
    if (valueChanged) {
      _.set(tableData.current, `[${tableIndex}].cycleCountItems[${rowIndex}].updated`, true);
    }
  };

  const tableMeta = {
    updateData: (cycleCountId, rowId, columnId, value) => {
      updateRow(cycleCountId, rowId, columnId, value);
    },
  };

  const getProduct = (id) => ({
    name: getField(id, 'product.name'),
    productCode: getField(id, 'product.productCode'),
    id: getField(id, 'product.id'),
  });

  const getDateCounted = (id) => getField(id, 'dateCounted');

  return {
    tableData: tableData.current || [],
    tableMeta,
    validationErrors,
    isStepEditable,
    getRecountedBy,
    getDefaultRecountedBy,
    getCountedBy,
    addEmptyRow,
    removeRow,
    printRecountForm,
    refreshCountItems,
    assignRecountedBy,
    getRecountedDate,
    updateRecountedDate,
    shouldHaveRootCause,
    next,
    save,
    submitRecount,
    back,
    getProduct,
    getDateCounted,
    triggerValidation,
    isSaveDisabled,
    setIsSaveDisabled,
    cycleCountsWithItemsWithoutRecount: cycleCountsWithItemsWithoutRecount.current,
    sortByProductName,
    setSortByProductName,
    forceRerender,
  };
};

export default useResolveStep;
