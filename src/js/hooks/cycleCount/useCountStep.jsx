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
import { getCurrentLocation, getCycleCountRequestIds } from 'selectors';

import {
  eraseDraft,
  fetchBinLocations,
  fetchUsers,
  startResolution,
} from 'actions';
import { UPDATE_CYCLE_COUNT_IDS } from 'actions/types';
import cycleCountApi from 'api/services/CycleCountApi';
import { CYCLE_COUNT as CYCLE_COUNT_URL, CYCLE_COUNT_PENDING_REQUESTS } from 'api/urls';
import notification from 'components/Layout/notifications/notification';
import ActivityCode from 'consts/activityCode';
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
  // Table data is stored using useRef to avoid re-renders onBlur
  // (it removes focus while selecting new fields)
  const tableData = useRef([]);
  // Saving selected "counted by" option
  const [countedBy, setCountedBy] = useState({});
  const [defaultCountedBy, setDefaultCountedBy] = useState({});
  // Saving selected "date counted" option, initially it's the date fetched from API
  const [dateCounted, setDateCounted] = useState({});
  const [isStepEditable, setIsStepEditable] = useState(true);
  // State used to trigger focus reset when changed. When this counter changes,
  // it will reset the focus by clearing the RowIndex and ColumnId in useEffect.
  const [refreshFocusCounter, setRefreshFocusCounter] = useState(0);
  const [isSaveDisabled, setIsSaveDisabled] = useState(false);

  const dispatch = useDispatch();
  const history = useHistory();
  const { show, hide } = useSpinner();
  const translate = useTranslate();

  const {
    cycleCountIds,
    currentLocation,
  } = useSelector((state) => ({
    cycleCountIds: getCycleCountRequestIds(state),
    currentLocation: getCurrentLocation(state),
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

  const {
    validationErrors,
    triggerValidation,
    forceRerender,
    isFormValid,
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
      setDateCounted(countedDates);
      const countedByMap = data?.data?.reduce((acc, cycleCount) => ({
        ...acc,
        [cycleCount?.id]: cycleCount?.cycleCountItems[0]?.assignee,
      }), {});
      setCountedBy(countedByMap);
      setDefaultCountedBy(countedByMap);
    } finally {
      hide();
    }
  };

  useEffect(() => {
    fetchCycleCounts();
  }, [cycleCountIds]);

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

  const markAllItemsAsUpdated = (cycleCountId) => setAllItemsUpdatedState(cycleCountId, true);

  const markAllItemsAsNotUpdated = (cycleCountId) => setAllItemsUpdatedState(cycleCountId, false);

  const assignCountedBy = (cycleCountId) => (person) => {
    // We need to mark all items as updated if we change the counted by person,
    // because counted by is associated with every cycle count item and needs to be set
    // for every item
    markAllItemsAsUpdated(cycleCountId);
    setCountedBy((prevState) => ({ ...prevState, [cycleCountId]: person }));
    setDefaultCountedBy((prevState) => ({ ...prevState, [cycleCountId]: person }));
    setDefaultCountedBy((prevState) => ({ ...prevState, [cycleCountId]: person }));
    resetFocus();
  };

  const getCountedBy = (cycleCountId) => countedBy?.[cycleCountId];

  const getDefaultCountedBy = (cycleCountId) => defaultCountedBy?.[cycleCountId];

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
    resetFocus();
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
    if (shouldResetFocus) {
      resetFocus();
    }
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
    resetFocus();
  };

  const getCountedDate = (cycleCountId) => dateCounted[cycleCountId];

  const getPayload = (cycleCountItem, cycleCount) => ({
    ...cycleCountItem,
    recount: false,
    assignee: getCountedBy(cycleCount.id)?.id,
    dateCounted: getCountedDate(cycleCount.id),
    inventoryItem: {
      ...cycleCountItem?.inventoryItem,
      product: cycleCountItem.product?.id,
      expirationDate: dateWithoutTimeZone({
        date: cycleCountItem?.inventoryItem?.expirationDate,
        currentDateFormat: DateFormat.MMM_DD_YYYY,
        outputDateFormat: DateFormat.MM_DD_YYYY,
      }),
    },
  });

  const save = async () => {
    try {
      show();
      resetValidationState();
      for (const cycleCount of tableData.current) {
        const cycleCountItemsToUpdate = cycleCount.cycleCountItems
          .filter((item) => (item.updated && !item.id.includes('newRow')))
          .map(trimLotNumberSpaces);
        const updatePayload = {
          itemsToUpdate: cycleCountItemsToUpdate.map((item) => getPayload(item, cycleCount)),
        };
        if (updatePayload.itemsToUpdate.length > 0) {
          await cycleCountApi
            .updateCycleCountItems(updatePayload, currentLocation?.id, cycleCount.id);
        }
        const cycleCountItemsToCreate = cycleCount.cycleCountItems
          .filter((item) => item.id.includes('newRow'))
          .map(trimLotNumberSpaces);
        const createPayload = {
          itemsToCreate: cycleCountItemsToCreate.map((item) => getPayload(item, cycleCount)),
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
      await fetchCycleCounts();
      resetFocus();
      hide();
    }
  };

  const printCountForm = async (format) => {
    show();
    // The backend does the export so we need to save first to ensure it has accurate data.
    await save();
    await exportFileFromApi({
      url: CYCLE_COUNT_URL(currentLocation?.id),
      params: { id: cycleCountIds },
      format,
    });
    resetFocus();
    hide();
  };

  const next = async () => {
    const isValid = triggerValidation();
    const currentCycleCountIds = tableData.current.map((cycleCount) => cycleCount.id);
    forceRerender();
    const areCountedByFilled = _.every(
      currentCycleCountIds,
      (id) => getCountedBy(id)?.id,
    );
    if (isValid && areCountedByFilled) {
      await save();
      setIsStepEditable(false);
    }
    resetFocus();
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

  const resolveDiscrepanciesModalButtons = (requestIdsWithDiscrepancies,
    requestIdsWithoutDiscrepancies) => (onClose) => ([
    {
      variant: 'transparent',
      defaultLabel: 'Not now',
      label: 'react.cycleCount.modal.notNow.label',
      onClick: () => {
        if (requestIdsWithoutDiscrepancies > 0) {
          showSuccessNotification(requestIdsWithoutDiscrepancies);
        }
        history.push(CYCLE_COUNT.list(TO_RESOLVE_TAB));
        onClose?.();
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
          requestIdsWithDiscrepancies,
          currentLocation?.id,
        ));
        if (requestIdsWithoutDiscrepancies > 0) {
          showSuccessNotification(requestIdsWithoutDiscrepancies);
        }
        history.push(CYCLE_COUNT.resolveStep());
        hide();
      },
    },
  ]);

  const openResolveDiscrepanciesModal = (requestIdsWithDiscrepancies,
    requestIdsWithoutDiscrepancies) => {
    confirmationModal({
      buttons: resolveDiscrepanciesModalButtons(requestIdsWithDiscrepancies,
        requestIdsWithoutDiscrepancies),
      ...modalLabels(requestIdsWithDiscrepancies.length),
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
      const requestIdsWithDiscrepancies = submittedCounts
        .reduce((acc, submittedCycleCountRequest) => {
          const { data } = submittedCycleCountRequest;
          if (data.data.status === cycleCountStatus?.COUNTED) {
            return [...acc, data?.data?.requestId];
          }

          return acc;
        }, []);
      dispatch(eraseDraft(currentLocation?.id, TO_COUNT_TAB));
      const requestIdsWithoutDiscrepancies
        = submittedCounts.length - requestIdsWithDiscrepancies.length;
      if (requestIdsWithDiscrepancies.length > 0) {
        openResolveDiscrepanciesModal(requestIdsWithDiscrepancies, requestIdsWithoutDiscrepancies);
        return;
      }
      showSuccessNotification(submittedCounts.length);
      await redirectToNextTab();
    } finally {
      setIsSaveDisabled(false);
      resetFocus();
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

  const setCountedDate = (cycleCountId) => (date) => {
    markAllItemsAsUpdated(cycleCountId);
    setDateCounted((prevState) => ({
      ...prevState,
      [cycleCountId]: date.format(),
    }));
    resetFocus();
  };

  const importItems = async (importFile) => {
    try {
      show();
      const response = await cycleCountApi.importCycleCountItems(
        importFile[0],
        currentLocation?.id,
      );
      console.log(response);
    } finally {
      hide();
    }
    // TODO: Map items to the table
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
    countedBy,
    getCountedDate,
    setCountedDate,
    next,
    back,
    save,
    validateExistenceOfCycleCounts,
    resolveDiscrepancies,
    isStepEditable,
    isFormValid,
    refreshFocusCounter,
    isSaveDisabled,
    setIsSaveDisabled,
    importItems,
  };
};

export default useCountStep;
