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

import { fetchBinLocations, fetchUsers } from 'actions';
import { UPDATE_CYCLE_COUNT_IDS } from 'actions/types';
import cycleCountApi from 'api/services/CycleCountApi';
import { CYCLE_COUNT as GET_CYCLE_COUNTS } from 'api/urls';
import ActivityCode from 'consts/activityCode';
import { CYCLE_COUNT } from 'consts/applicationUrls';
import { TO_RESOLVE_TAB } from 'consts/cycleCount';
import { DateFormat } from 'consts/timeFormat';
import useResolveStepValidation from 'hooks/cycleCount/useResolveStepValidation';
import useSpinner from 'hooks/useSpinner';
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
  // Saving selected "recounted by" option
  const [recountedBy, setRecountedBy] = useState({});
  // Saving selected "date recounted" option, initially it's the date fetched from API
  const [dateRecounted, setDateRecounted] = useState({});
  const [isStepEditable, setIsStepEditable] = useState(true);
  // State used to trigger focus reset when changed. When this counter changes,
  // it will reset the focus by clearing the RowIndex and ColumnId in useEffect.
  const [refreshFocusCounter, setRefreshFocusCounter] = useState(0);
  const { show, hide } = useSpinner();
  const history = useHistory();
  const [isSaveDisabled, setIsSaveDisabled] = useState(false);

  const {
    validationErrors,
    isRootCauseWarningSkipped,
    triggerValidation,
    forceRerender,
    validateRootCauses,
    shouldHaveRootCause,
    showEmptyRootCauseWarning,
    isFormValid,
    resetValidationState,
  } = useResolveStepValidation({ tableData });

  const dispatch = useDispatch();

  const {
    cycleCountIds,
    currentLocation,
    reasonCodes,
    users,
  } = useSelector((state) => ({
    users: state.users.data,
    cycleCountIds: state.cycleCount.cycleCounts,
    reasonCodes: state.cycleCount.reasonCodes,
    currentLocation: state.session.currentLocation,
  }));

  const resetFocus = () => {
    setRefreshFocusCounter((prev) => prev + 1);
  };

  const showBinLocation = useMemo(() =>
    checkBinLocationSupport(currentLocation.supportedActivities), [currentLocation?.id]);

  useEffect(() => {
    if (showBinLocation) {
      dispatch(fetchBinLocations(
        currentLocation?.id,
        [ActivityCode.RECEIVE_STOCK],
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
    const { data } = await cycleCountApi.getCycleCounts(
      currentLocation?.id,
      ids,
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
      [cycleCount?.id]: cycleCount?.cycleCountItems?.[0]?.dateRecounted
      || recountedDates?.[cycleCount?.id],
    }), {});
    const recountedByData = tableData.current?.reduce((acc, cycleCount) => ({
      ...acc,
      [cycleCount?.id]: cycleCount?.cycleCountItems?.[0]?.recountedBy
      || recountedBy?.[cycleCount?.id],
    }), {});
    setDateRecounted(recountedDates);
    setRecountedBy(recountedByData);
  };

  useEffect(() => {
    (async () => {
      await refetchData();
    })();
  }, [cycleCountIds]);

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
      params: { id: cycleCountIds },
      format,
    });
    resetFocus();
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

  const getRecountedBy = (cycleCountId) => recountedBy?.[cycleCountId];

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
      resetFocus();
      triggerValidation();
      hide();
    }
  };

  const addEmptyRow = (productId, id, shouldResetFocus = true) => {
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
    if (shouldResetFocus) {
      resetFocus();
    }
    resetValidationState();
    forceRerender();
  };

  const cancelCounts = async (cycleCountRequestIdsToDelete) => {
    try {
      show();
      await cycleCountApi.deleteRequests(currentLocation?.id, cycleCountRequestIdsToDelete);
      await refetchData();

      // If we've canceled every product in the batch, there's no reason to stay on this screen.
      if (tableData.current.length === 0) {
        history.push(CYCLE_COUNT.list(TO_RESOLVE_TAB));
      }
    } finally {
      hide();
    }
  };

  const zeroRecountItemsModalButtons = (cycleCountRequestIdsToDelete) => (onClose) => ([
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
        await cancelCounts(cycleCountRequestIdsToDelete);
      },
    },
  ]);

  const openZeroRecountItemsModal = (productsWithNoRecountItems) => {
    const requestIds = productsWithNoRecountItems.map((entry) => (entry.cycleCountRequestId));
    const productCodes = productsWithNoRecountItems.map((entry) => (entry.product));
    confirmationModal({
      hideCloseButton: false,
      closeOnClickOutside: true,
      buttons: zeroRecountItemsModalButtons(requestIds),
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

  const next = () => {
    resetFocus();
    const isValid = triggerValidation();
    forceRerender();
    const currentCycleCountIds = tableData.current.map((cycleCount) => cycleCount.id);
    const areRecountedByFilled = _.every(
      currentCycleCountIds,
      (id) => getRecountedBy(id)?.id,
    );

    if (!isValid || !areRecountedByFilled) {
      return;
    }

    const productsWithNoRecountItems = cycleCountsWithItemsWithoutRecount.current
      .map((cycleCount) => ({
        cycleCountRequestId: cycleCount.requestId,
        product: getField(cycleCount?.id, 'product.productCode'),
      }));

    if (productsWithNoRecountItems.length > 0) {
      openZeroRecountItemsModal(productsWithNoRecountItems);
      return;
    }

    const missingRootCauses = validateRootCauses();
    if (!isRootCauseWarningSkipped && missingRootCauses.length > 0) {
      showEmptyRootCauseWarning();
      return;
    }

    setIsStepEditable(false);
  };

  const back = () => {
    setIsStepEditable(true);
    resetFocus();
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

  const markAllItemsAsUpdated = (cycleCountId) => setAllItemsUpdatedState(cycleCountId, true);

  const markAllItemsAsNotUpdated = (cycleCountId) => setAllItemsUpdatedState(cycleCountId, false);

  const assignRecountedBy = (cycleCountId) => (person) => {
    markAllItemsAsUpdated(cycleCountId);
    setRecountedBy((prevState) => ({ ...prevState, [cycleCountId]: person }));
    resetFocus();
  };

  const getRecountedDate = (cycleCountId) => dateRecounted[cycleCountId] || moment.now();

  const setRecountedDate = (cycleCountId) => (date) => {
    setDateRecounted({
      ...dateRecounted,
      [cycleCountId]: date.format(),
    });
    markAllItemsAsUpdated(cycleCountId);
    resetFocus();
  };

  const getPayload = (cycleCountItem, cycleCount) => ({
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
    assignee: getRecountedBy(cycleCount.id)?.id,
    recount: true,
  });

  const save = async (shouldRefetch = true) => {
    try {
      show();
      resetValidationState();
      for (const cycleCount of tableData.current) {
        const cycleCountItemsToUpdate = cycleCount.cycleCountItems
          .filter((item) => (item.updated && !item.id.includes('newRow')))
          .map(trimLotNumberSpaces);
        for (const cycleCountItem of cycleCountItemsToUpdate) {
          await cycleCountApi.updateCycleCountItem(getPayload(cycleCountItem, cycleCount),
            currentLocation?.id, cycleCountItem?.id);
        }
        const cycleCountItemsToCreate = cycleCount.cycleCountItems
          .filter((item) => item.id.includes('newRow'))
          .map(trimLotNumberSpaces);
        for (const cycleCountItem of cycleCountItemsToCreate) {
          await cycleCountApi.createCycleCountItem(getPayload(cycleCountItem, cycleCount),
            currentLocation?.id, cycleCount?.id);
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
      resetFocus();
    }
  };

  const refreshCountItems = async (cycleCountIdsForOutdatedProducts = cycleCountIds) => {
    try {
      show();
      await save(false);
      for (const cycleCountId of cycleCountIdsForOutdatedProducts) {
        await cycleCountApi.refreshItems(currentLocation?.id, cycleCountId, true);
      }
    } finally {
      resetFocus();
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
      hideCloseButton: false,
      closeOnClickOutside: false,
    });
  };

  const submitRecount = async () => {
    let outdatedProducts = 0;
    const cycleCountIdsForOutdatedProducts = [];
    try {
      show();
      await save();
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
          payload: cycleCountIdsForOutdatedProducts,
        });
        openReviewProductsModal(outdatedProducts, cycleCountIdsForOutdatedProducts);
        return;
      }
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
    _.set(tableData.current, `[${tableIndex}].cycleCountItems[${rowIndex}].${nestedPath}`, value);

    // Mark item as updated, so that the item can be easily distinguished whether it was updated
    _.set(tableData.current, `[${tableIndex}].cycleCountItems[${rowIndex}].updated`, true);
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
    isFormValid,
    getRecountedBy,
    getCountedBy,
    addEmptyRow,
    removeRow,
    printRecountForm,
    refreshCountItems,
    assignRecountedBy,
    getRecountedDate,
    setRecountedDate,
    shouldHaveRootCause,
    next,
    save,
    submitRecount,
    back,
    getProduct,
    getDateCounted,
    refreshFocusCounter,
    triggerValidation,
    isSaveDisabled,
    setIsSaveDisabled,
    cycleCountsWithItemsWithoutRecount: cycleCountsWithItemsWithoutRecount.current,
  };
};

export default useResolveStep;
