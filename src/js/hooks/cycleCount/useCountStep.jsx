/* eslint-disable no-restricted-syntax */
/* eslint-disable no-await-in-loop */

import {
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';

import _ from 'lodash';
import queryString from 'query-string';
import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';
import {
  getCurrentLocale,
  getCurrentLocation,
  getCurrentUser,
  getCycleCountRequestIds,
} from 'selectors';

import {
  eraseDraft,
  fetchBinLocations,
  fetchLotNumbersByProductIds,
  fetchUsers,
  startResolution,
} from 'actions';
import { UPDATE_CYCLE_COUNT_IDS } from 'actions/types';
import cycleCountApi from 'api/services/CycleCountApi';
import { CYCLE_COUNT as CYCLE_COUNT_URL, CYCLE_COUNT_PENDING_REQUESTS } from 'api/urls';
import notification from 'components/Layout/notifications/notification';
import { CYCLE_COUNT } from 'consts/applicationUrls';
import { ALL_PRODUCTS_TAB, TO_COUNT_TAB, TO_RESOLVE_TAB } from 'consts/cycleCount';
import cycleCountStatus from 'consts/cycleCountStatus';
import NotificationType from 'consts/notificationTypes';
import { DateFormat } from 'consts/timeFormat';
import useCountStepValidation from 'hooks/cycleCount/useCountStepValidation';
import useSpinner from 'hooks/useSpinner';
import useTranslate from 'hooks/useTranslate';
import apiClient from 'utils/apiClient';
import confirmationModal from 'utils/confirmationModalUtils';
import trimLotNumberSpaces from 'utils/cycleCountUtils';
import dateWithoutTimeZone from 'utils/dateUtils';
import exportFileFromApi from 'utils/file-download-util';
import { checkBinLocationSupport } from 'utils/supportedActivitiesUtils';

// Managing state for all tables, operations on shared state (from count step)
const useCountStep = () => {
  const [isAssignCountModalOpen, setIsAssignCountModalOpen] = useState(false);
  const assignCountModalData = useRef([]);
  // Table data is stored using useRef to avoid re-renders onBlur
  // (it removes focus while selecting new fields)
  const tableData = useRef([]);
  // Saving selected "counted by" option
  const countedBy = useRef({});
  const defaultCountedBy = useRef({});
  // Saving selected "date counted" option, initially it's the date fetched from API
  const dateCounted = useRef({});
  const [isStepEditable, setIsStepEditable] = useState(true);
  const [isSaveDisabled, setIsSaveDisabled] = useState(false);
  const [sortByProductName, setSortByProductName] = useState(false);
  const [importErrors, setImportErrors] = useState([]);
  const assigneeImported = useRef(null);
  const requestIdsWithDiscrepancies = useRef([]);
  // Here we store cycle count IDs that were updated,
  // and we will mark them as updated before saving.
  const cycleCountsMarkedToUpdate = useRef([]);

  const dispatch = useDispatch();
  const history = useHistory();
  const { show, hide } = useSpinner();
  const translate = useTranslate();

  const {
    cycleCountIds,
    currentLocation,
    currentUser,
    locale,
  } = useSelector((state) => ({
    cycleCountIds: getCycleCountRequestIds(state),
    currentLocation: getCurrentLocation(state),
    currentUser: getCurrentUser(state),
    locale: getCurrentLocale(state),
  }));

  const showBinLocation = useMemo(() =>
    checkBinLocationSupport(currentLocation.supportedActivities), [currentLocation?.id]);

  // Collect unique product IDs from the tableData to fetch lot numbers for those products
  const uniqueProductIds = _.uniq(
    tableData.current.flatMap((c) => c.cycleCountItems)
      .map((i) => i.product?.id),
  );

  useEffect(() => {
    // we want to fetch lot numbers only when the step is editable and there are products
    // in the table because if the step is not editable, there is no need to fetch lot numbers
    if (isStepEditable && uniqueProductIds.length > 0) {
      dispatch(fetchLotNumbersByProductIds(uniqueProductIds));
    }
  }, [JSON.stringify(uniqueProductIds), isStepEditable]);

  useEffect(() => {
    if (showBinLocation) {
      dispatch(fetchBinLocations(
        currentLocation?.id,
        [],
        'sortOrder,locationType,name',
      ));
    }
  }, [currentLocation?.id]);

  const {
    validationErrors,
    triggerValidation,
    forceRerender,
    resetValidationState,
  } = useCountStepValidation({ tableData });

  const filterCountItems = (cycleCounts) => cycleCounts.map((cycleCount) => ({
    ...cycleCount,
    cycleCountItems: cycleCount.cycleCountItems.filter((item) => item.countIndex === 0),
  }));

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

    return [...originalItems, ...customItemsSortedByCreationDate];
  };

  const fetchCycleCounts = async () => {
    if (!cycleCountIds.length) {
      return;
    }

    try {
      show();
      const { data } = await cycleCountApi.getCycleCounts(
        currentLocation?.id,
        cycleCountIds,
        sortByProductName && 'productName',
      );
      tableData.current = filterCountItems(data?.data)?.map((cycleCount) => ({
        ...cycleCount,
        cycleCountItems: moveCustomItemsToTheBottom(cycleCount?.cycleCountItems),
      }));
      // Date counted and assignee are the same for all items,
      // so we create a map looking at first item
      const countedDates = data?.data?.reduce((acc, cycleCount) => ({
        ...acc,
        [cycleCount?.id]: cycleCount?.cycleCountItems[0]?.dateCounted,
      }), {});
      const countedByMap = data?.data?.reduce((acc, cycleCount) => ({
        ...acc,
        [cycleCount?.id]: cycleCount?.cycleCountItems[0]?.assignee,
      }), {});
      dateCounted.current = countedDates;
      countedBy.current = countedByMap;
      defaultCountedBy.current = countedByMap;
    } finally {
      hide();
    }
  };

  useEffect(() => {
    fetchCycleCounts();
  }, [cycleCountIds, sortByProductName]);

  // Fetching data for "counted by" dropdown
  useEffect(() => {
    dispatch(fetchUsers());
  }, []);

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

  const markAllItemsAsUpdated = () => {
    cycleCountsMarkedToUpdate.current.forEach((cycleCountId) => {
      setAllItemsUpdatedState(cycleCountId, true);
    });
    cycleCountsMarkedToUpdate.current = [];
  };

  const markAllItemsAsNotUpdated = (cycleCountId) => setAllItemsUpdatedState(cycleCountId, false);

  const assignCountedBy = (cycleCountId) => (person) => {
    // We need to mark all items as updated if we change the counted by person,
    // because counted by is associated with every cycle count item and needs to be set
    // for every item
    if (!cycleCountsMarkedToUpdate.current.includes(cycleCountId)) {
      cycleCountsMarkedToUpdate.current = [...cycleCountsMarkedToUpdate.current, cycleCountId];
    }
    countedBy.current = { ...countedBy.current, [cycleCountId]: person };
    defaultCountedBy.current = { ...defaultCountedBy.current, [cycleCountId]: person };
  };

  const getCountedBy = (cycleCountId) => countedBy.current?.[cycleCountId];

  const getDefaultCountedBy = (cycleCountId) => defaultCountedBy.current?.[cycleCountId];

  const removeFromState = (cycleCountId, rowId) => {
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
    triggerValidation();
  };

  const removeRow = async (cycleCountId, rowId) => {
    try {
      show();
      if (!rowId.includes('newRow')) {
        await cycleCountApi.deleteCycleCountItem(currentLocation?.id, rowId);
      }
      removeFromState(cycleCountId, rowId);
    } finally {
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
      quantityCounted: null,
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

  const validateExistenceOfCycleCounts = async (callback) => {
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
    return callback();
  };

  const back = () => {
    setIsStepEditable(true);
  };

  const getCountedDate = (cycleCountId) => dateCounted.current[cycleCountId];

  const getPayload = (cycleCountItem, shouldSetDefaultAssignee) => ({
    ...cycleCountItem,
    recount: false,
    assignee: shouldSetDefaultAssignee
      ? getCountedBy(cycleCountItem.cycleCountId)?.id ?? currentUser.id
      : getCountedBy(cycleCountItem.cycleCountId)?.id,
    dateCounted: getCountedDate(cycleCountItem.cycleCountId),
    inventoryItem: {
      ...cycleCountItem?.inventoryItem,
      product: cycleCountItem.product?.id,
      expirationDate: dateWithoutTimeZone({
        date: cycleCountItem?.inventoryItem?.expirationDate,
        currentDateFormat: DateFormat.MMM_DD_YYYY,
        outputDateFormat: DateFormat.MM_DD_YYYY,
        locale,
      }),
    },
    cycleCount: cycleCountItem.cycleCountId,
  });

  const save = async (shouldSetDefaultAssignee = false) => {
    try {
      show();
      markAllItemsAsUpdated();
      resetValidationState();
      const cycleCountItemsToUpdateBatch = [];
      const cycleCountItemsToCreateBatch = [];
      for (const cycleCount of tableData.current) {
        const cycleCountItemsToUpdate = cycleCount.cycleCountItems
          .filter((item) => ((item.updated || !item.assignee) && !item.id.includes('newRow')))
          .map((item) => ({ ...trimLotNumberSpaces(item), cycleCountId: cycleCount.id }));

        if (cycleCountItemsToUpdate.length > 0) {
          cycleCountItemsToUpdateBatch.push(cycleCountItemsToUpdate);
        }
        const cycleCountItemsToCreate = cycleCount.cycleCountItems
          .filter((item) => item.id.includes('newRow'))
          .map((item) => ({ ...trimLotNumberSpaces(item), cycleCountId: cycleCount.id }));

        if (cycleCountItemsToCreate.length > 0) {
          cycleCountItemsToCreateBatch.push(cycleCountItemsToCreate);
        }

        // Now that we've successfully saved all the items, mark them all as not updated so that
        // we don't try to update them again next time something is changed.
        markAllItemsAsNotUpdated(cycleCount.id);
      }
      const updatePayload = {
        itemsToUpdate: cycleCountItemsToUpdateBatch.flat().map((item) =>
          getPayload(item, shouldSetDefaultAssignee)),
      };
      await cycleCountApi
        .updateCycleCountItemsBatch(updatePayload, currentLocation?.id);
      const createPayload = {
        itemsToCreate: cycleCountItemsToCreateBatch.flat().map((item) =>
          getPayload(item, shouldSetDefaultAssignee)),
      };
      await cycleCountApi
        .createCycleCountItemsBatch(createPayload, currentLocation?.id);
    } finally {
      // After the save, refetch cycle counts so that a new row can't be saved multiple times
      await fetchCycleCounts();
      hide();
    }
  };

  const printCountForm = async (format) => {
    show();
    // The backend does the export so we need to save first to ensure it has accurate data.
    await save();
    await exportFileFromApi({
      url: CYCLE_COUNT_URL(currentLocation?.id),
      params: { id: cycleCountIds, sortBy: sortByProductName && 'productName' },
      format,
    });
    hide();
  };

  const next = async () => {
    const isValid = triggerValidation();
    forceRerender();
    if (isValid) {
      await save({ shouldSetDefaultAssignee: true });
      setIsStepEditable(false);
    }
  };

  const modalLabels = (count) => ({
    title: {
      label: 'react.cycleCount.modal.resolveDiscrepanciesTitle.label',
      default: 'Resolve discrepancies?',
    },
    content: {
      label: 'react.cycleCount.modal.resolveDiscrepanciesContent.label',
      default: `There are ${count} products with a discrepancy to resolve. Would you like to resolve them?`,
      data: { count },
    },
  });

  const submitCount = () =>
    tableData.current.reduce((acc, cycleCount) => ([
      ...acc,
      cycleCountApi.submitCount({
        refreshQuantityOnHand: true,
        failOnOutdatedQuantity: false,
        requireRecountOnDiscrepancy: true,
        cycleCountItems: cycleCount.cycleCountItems,
      },
      currentLocation?.id,
      cycleCount?.id),
    ]), []);

  const showSuccessNotification = (count) => {
    notification(NotificationType.SUCCESS)({
      message: translate(
        'react.cycleCount.popup.success.label',
        `Successfully counted ${count} products`,
        {
          data: { count },
        },
      ),
    });
  };

  const mapSelectedRowsToModalData = () => {
    const modalDataWithDisrepancies = tableData.current.filter(
      (cycleCount) => requestIdsWithDiscrepancies.current.includes(cycleCount?.requestId),
    );

    assignCountModalData.current = modalDataWithDisrepancies.map((cycleCount) => ({
      product: cycleCount?.cycleCountItems?.[0]?.product,
      cycleCountRequestId: cycleCount?.requestId,
      inventoryItemsCount: cycleCount?.cycleCountItems?.length || 0,
      assignee: cycleCount?.verificationCount?.assignee,
      deadline: cycleCount?.verificationCount?.deadline,
    }));
  };

  const closeAssignCountModal = () => {
    setIsAssignCountModalOpen(false);
    assignCountModalData.current = [];
    history.push(CYCLE_COUNT.resolveStep());
  };

  const openAssignCountModal = () => {
    mapSelectedRowsToModalData();
    setIsAssignCountModalOpen(true);
  };

  const resolveDiscrepanciesModalButtons = (requestIdsWithDiscrepanciesData,
    requestIdsWithoutDiscrepanciesData) => (onClose) => ([
    {
      variant: 'transparent',
      defaultLabel: 'Not now',
      label: 'react.cycleCount.modal.notNow.label',
      onClick: () => {
        if (requestIdsWithoutDiscrepanciesData > 0) {
          showSuccessNotification(requestIdsWithoutDiscrepanciesData);
        }
        hide();
        onClose()?.();
        history.push(CYCLE_COUNT.list(TO_RESOLVE_TAB));
      },
    },
    {
      variant: 'primary',
      defaultLabel: 'Resolve',
      label: 'react.cycleCount.modal.resolve.label',
      onClick: async () => {
        show();
        onClose?.();
        await dispatch(startResolution(
          requestIdsWithDiscrepanciesData,
          currentLocation?.id,
        ));
        if (requestIdsWithoutDiscrepanciesData > 0) {
          showSuccessNotification(requestIdsWithoutDiscrepanciesData);
        }
        hide();
        openAssignCountModal();
      },
    },
  ]);

  const openResolveDiscrepanciesModal = (requestIdsWithDiscrepanciesData,
    requestIdsWithoutDiscrepanciesData) => {
    confirmationModal({
      buttons: resolveDiscrepanciesModalButtons(requestIdsWithDiscrepanciesData,
        requestIdsWithoutDiscrepanciesData),
      ...modalLabels(requestIdsWithDiscrepanciesData.length),
      hideCloseButton: true,
      closeOnClickOutside: false,
    });
  };

  const redirectToNextTab = async () => {
    const statusConfigs = [
      { statuses: [cycleCountStatus.COUNTED, cycleCountStatus.INVESTIGATING], tab: TO_RESOLVE_TAB },
      {
        statuses: [cycleCountStatus.CREATED, cycleCountStatus.REQUESTED, cycleCountStatus.COUNTING],
        tab: TO_COUNT_TAB,
      },
    ];

    for (const { statuses, tab } of statusConfigs) {
      const { data } = await apiClient.get(
        CYCLE_COUNT_PENDING_REQUESTS(currentLocation?.id),
        {
          params: {
            facility: currentLocation?.id,
            statuses,
            tab,
            max: 1,
            offset: 0,
          },
          paramsSerializer: (params) => queryString.stringify(params),
        },
      );

      if (data.totalCount > 0) {
        history.push(CYCLE_COUNT.list(tab));
        return;
      }
    }

    history.push(CYCLE_COUNT.list(ALL_PRODUCTS_TAB));
  };

  const resolveDiscrepancies = async () => {
    try {
      show();
      const submittedCounts = await Promise.all(submitCount());
      requestIdsWithDiscrepancies.current = submittedCounts
        .reduce((acc, submittedCycleCountRequest) => {
          const { data } = submittedCycleCountRequest;
          if (data.data.status === cycleCountStatus?.COUNTED) {
            return [...acc, data?.data?.requestId];
          }

          return acc;
        }, []);
      dispatch(eraseDraft(currentLocation?.id, TO_COUNT_TAB));
      const requestIdsWithoutDiscrepancies =
        submittedCounts.length - requestIdsWithDiscrepancies.current.length;
      if (requestIdsWithDiscrepancies.current.length > 0) {
        openResolveDiscrepanciesModal(
          requestIdsWithDiscrepancies.current,
          requestIdsWithoutDiscrepancies,
        );
        return;
      }
      showSuccessNotification(submittedCounts.length);
      await redirectToNextTab();
    } finally {
      setIsSaveDisabled(false);
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

  const updateDateCounted = (cycleCountId) => (date) => {
    if (!cycleCountsMarkedToUpdate.current.includes(cycleCountId)) {
      cycleCountsMarkedToUpdate.current = [...cycleCountsMarkedToUpdate.current, cycleCountId];
    }
    dateCounted.current = {
      ...dateCounted.current,
      [cycleCountId]: date ? date.format() : null,
    };
  };

  const createCustomItemsFromImport = (items) => (items
    ? items.map((item) => ({
      ...item,
      countIndex: 0,
      id: _.uniqueId('newRow'),
      custom: true,
      inventoryItem: {
        lotNumber: item.lotNumber,
        expirationDate: item.expirationDate,
      },
      product: {
        id: item.product.id,
        productCode: item.product.productCode,
      },
      binLocation: item.binLocation?.id ? {
        id: item.binLocation.id,
        name: item.binLocation.name,
      } : null,
    }))
    : []);

  const removeItemFromCycleCounts = (cycleCounts, cycleCountId, itemId) => ({
    ...cycleCounts,
    [cycleCountId]: cycleCounts[cycleCountId]
      .filter((item) => item.cycleCountItemId !== itemId),
  });

  const mergeImportItems = (originalItem, importedItem) => ({
    ...originalItem,
    quantityCounted: importedItem ? importedItem.quantityCounted : originalItem.quantityCounted,
    comment: importedItem ? importedItem.comment : originalItem.comment,
    updated: true,
  });

  const importItems = async (importFile) => {
    try {
      show();
      const response = await cycleCountApi.importCycleCountItems(
        importFile[0],
        currentLocation?.id,
      );
      setImportErrors(response.data.errors);
      let cycleCounts = _.groupBy(response.data.data, 'cycleCountId');
      const countedByUpdates = {};
      const dateCountedUpdates = {};

      tableData.current = tableData.current.map((cycleCount) => {
        // After each iteration assign it to false again, so that the flag
        // can be reused for next cycle counts in the loop
        assigneeImported.current = false;
        return {
          ...cycleCount,
          cycleCountItems: [
            ...cycleCount.cycleCountItems
              .map((item) => {
                const correspondingImportItem = cycleCounts[cycleCount.id]?.find(
                  (cycleCountItem) => cycleCountItem.cycleCountItemId === item.id,
                );
                // Assign counted by and date counted only once to prevent performance issues
                // At this point, every item after being validated on the backend,
                // should have the same assignee and dateCounted set,
                // so we can make this operation only once
                // this is why we introduce the assigneeImported boolean flag
                if (correspondingImportItem && !assigneeImported.current[cycleCount.id]) {
                  countedByUpdates[cycleCount.id] = correspondingImportItem.assignee;
                  // Do not allow to clear the date counted dropdown
                  // if dateCounted was not set in the sheet
                  if (correspondingImportItem.dateCounted) {
                    dateCountedUpdates[cycleCount.id] = correspondingImportItem.dateCounted;
                  }
                  // Mark the flag as true, so that it's not triggered for each item
                  assigneeImported.current = true;
                }

                if (correspondingImportItem) {
                // Remove items from the import that have a corresponding item
                // in the current cycle count. It allows us to treat items with
                // the wrong ID as new rows that do not already exist.
                  cycleCounts = removeItemFromCycleCounts(
                    cycleCounts,
                    cycleCount.id,
                    correspondingImportItem.cycleCountItemId,
                  );
                }

                return mergeImportItems(item, correspondingImportItem);
              }),
            ...createCustomItemsFromImport(cycleCounts[cycleCount.id]),
          ],
        };
      });
      // Batch update refs
      countedBy.current = { ...countedBy.current, ...countedByUpdates };
      defaultCountedBy.current = { ...defaultCountedBy.current, ...countedByUpdates };
      dateCounted.current = { ...dateCounted.current, ...dateCountedUpdates };
    } finally {
      triggerValidation();
      hide();
    }
  };

  const handleCountStepHeaderSave = async () => {
    await validateExistenceOfCycleCounts(save);

    // When we click "Save progress", we want to refetch the lot numbers
    // because the user may have created new ones and, without refetching,
    // they won't be available in the dropdown.
    dispatch(fetchLotNumbersByProductIds(uniqueProductIds));
  };

  return {
    tableData: tableData.current,
    tableMeta,
    validationErrors,
    triggerValidation,
    addEmptyRow,
    removeRow,
    printCountForm,
    assignCountedBy,
    getCountedBy,
    getDefaultCountedBy,
    getCountedDate,
    updateDateCounted,
    next,
    back,
    validateExistenceOfCycleCounts,
    resolveDiscrepancies,
    isStepEditable,
    isSaveDisabled,
    setIsSaveDisabled,
    importItems,
    sortByProductName,
    setSortByProductName,
    importErrors,
    isAssignCountModalOpen,
    closeAssignCountModal,
    assignCountModalData,
    forceRerender,
    handleCountStepHeaderSave,
  };
};

export default useCountStep;
