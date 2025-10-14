/* eslint-disable no-restricted-syntax */
/* eslint-disable no-await-in-loop */

import {
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';

import queryString from 'query-string';
import { useDispatch, useSelector, useStore } from 'react-redux';
import { useHistory } from 'react-router-dom';
import {
  getCurrentLocale,
  getAllCycleCountProducts,
  getCurrentLocationId,
  getCurrentLocationSupportedActivities,
  getCurrentUserId,
  getCycleCountRequestIds,
} from 'selectors';

import {
  eraseDraft,
  fetchBinLocations, fetchCycleCounts,
  fetchLotNumbersByProductIds,
  fetchUsers, setUpdated,
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
import useSpinner from 'hooks/useSpinner';
import useTranslate from 'hooks/useTranslate';
import apiClient from 'utils/apiClient';
import confirmationModal from 'utils/confirmationModalUtils';
import { trimLotNumberSpaces } from 'utils/cycleCountUtils';
import dateWithoutTimeZone from 'utils/dateUtils';
import exportFileFromApi from 'utils/file-download-util';
import { checkBinLocationSupport } from 'utils/supportedActivitiesUtils';

// Managing state for all tables, operations on shared state (from count step)
const useCountStep = () => {
  const [isAssignCountModalOpen, setIsAssignCountModalOpen] = useState(false);
  const assignCountModalData = useRef([]);
  const [isStepEditable, setIsStepEditable] = useState(true);
  const [isSaveDisabled, setIsSaveDisabled] = useState(false);
  const [sortByProductName, setSortByProductName] = useState(false);
  const [importErrors, setImportErrors] = useState([]);
  const assigneeImported = useRef(null);
  const requestIdsWithDiscrepancies = useRef([]);

  const dispatch = useDispatch();
  const history = useHistory();
  const store = useStore();
  const { show, hide } = useSpinner();
  const translate = useTranslate();

  const currentLocationId = useSelector(getCurrentLocationId);
  const supportedActivities = useSelector(getCurrentLocationSupportedActivities);
  const currentUserId = useSelector(getCurrentUserId);
  const cycleCountIds = useSelector(getCycleCountRequestIds);
  const uniqueProductIds = useSelector(getAllCycleCountProducts);
  const locale = useSelector(getCurrentLocale);

  const showBinLocation = useMemo(() =>
    checkBinLocationSupport(supportedActivities), [currentLocationId]);

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
        currentLocationId,
        [],
        'sortOrder,locationType,name',
      ));
    }
  }, [currentLocationId]);

  useEffect(() => {
    dispatch(fetchCycleCounts(
      cycleCountIds,
      currentLocationId,
      sortByProductName,
    ));
  }, [cycleCountIds, sortByProductName]);

  // Fetching data for "counted by" dropdown
  useEffect(() => {
    dispatch(fetchUsers());
  }, []);

  const markAllItemsAsUpdated = () => {
    cycleCountIds.forEach((cycleCountId) => {
      dispatch(setUpdated(cycleCountId, true));
    });
  };

  const markAllItemsAsNotUpdated = (cycleCountId) => dispatch(setUpdated(cycleCountId, false));

  const validateExistenceOfCycleCounts = async (callback) => {
    const { data } = await cycleCountApi.getCycleCounts(
      currentLocationId,
      cycleCountIds,
    );

    const state = store.getState();
    const cycleCounts = Object.values(state.countWorkflow.entities);

    const {
      existingCycleCountsIds,
      canceledCycleCountsIds,
    } = cycleCounts.reduce((acc, curr) => {
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
          locationId: currentLocationId,
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

  const getPayload = (cycleCountItem, shouldSetDefaultAssignee) => {
    const state = store.getState();
    const countedBy = state.countWorkflow.countedBy?.[cycleCountItem.cycleCountId]?.id;
    const dateCounted = state.countWorkflow.dateCounted?.[cycleCountItem.cycleCountId];

    return {
      ...cycleCountItem,
      recount: false,
      assignee: shouldSetDefaultAssignee
        ? countedBy ?? currentUserId
        : countedBy,
      dateCounted,
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
    };
  };

  const save = async (shouldSetDefaultAssignee = false) => {
    try {
      show();
      markAllItemsAsUpdated();
      const cycleCountItemsToUpdateBatch = [];
      const cycleCountItemsToCreateBatch = [];
      const state = store.getState();
      const cycleCounts = Object.values(state.countWorkflow.entities);
      for (const cycleCount of cycleCounts) {
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
        .updateCycleCountItemsBatch(updatePayload, currentLocationId);
      const createPayload = {
        itemsToCreate: cycleCountItemsToCreateBatch.flat().map((item) =>
          getPayload(item, shouldSetDefaultAssignee)),
      };
      await cycleCountApi
        .createCycleCountItemsBatch(createPayload, currentLocationId);
    } finally {
      // After the save, refetch cycle counts so that a new row can't be saved multiple times
      hide();
    }
  };

  const printCountForm = async (format) => {
    show();
    // The backend does the export, so we need to save first to ensure it has accurate data.
    await save();
    await exportFileFromApi({
      url: CYCLE_COUNT_URL(currentLocationId),
      params: { id: cycleCountIds, sortBy: sortByProductName && 'productName' },
      format,
    });
    hide();
  };

  const next = async () => {
    await save({ shouldSetDefaultAssignee: true });
    setIsStepEditable(false);
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

  const submitCount = () => {
    const state = store.getState();
    const cycleCounts = Object.values(state.countWorkflow.entities);
    cycleCounts.reduce((acc, cycleCount) => ([
      ...acc,
      cycleCountApi.submitCount({
        refreshQuantityOnHand: true,
        failOnOutdatedQuantity: false,
        requireRecountOnDiscrepancy: true,
        cycleCountItems: cycleCount.cycleCountItems,
      },
        currentLocationId,
      cycleCount?.id),
    ]), []);
  };

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
    const modalDataWithDisrepancies = [].current.filter(
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
          currentLocationId,
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
        CYCLE_COUNT_PENDING_REQUESTS(currentLocationId),
        {
          params: {
            facility: currentLocationId,
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
      dispatch(eraseDraft(currentLocationId, TO_COUNT_TAB));
      const requestIdsWithoutDiscrepancies = submittedCounts.length - requestIdsWithDiscrepancies.current.length;
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

  // const createCustomItemsFromImport = (items) => (items
  //   ? items.map((item) => ({
  //     ...item,
  //     countIndex: 0,
  //     id: _.uniqueId('newRow'),
  //     custom: true,
  //     inventoryItem: {
  //       lotNumber: item.lotNumber,
  //       expirationDate: item.expirationDate,
  //     },
  //     product: {
  //       id: item.product.id,
  //       productCode: item.product.productCode,
  //     },
  //     binLocation: item.binLocation?.id ? {
  //       id: item.binLocation.id,
  //       name: item.binLocation.name,
  //     } : null,
  //   }))
  //   : []);

  // const removeItemFromCycleCounts = (cycleCounts, cycleCountId, itemId) => ({
  //   ...cycleCounts,
  //   [cycleCountId]: cycleCounts[cycleCountId]
  //     .filter((item) => item.cycleCountItemId !== itemId),
  // });
  //
  // const mergeImportItems = (originalItem, importedItem) => ({
  //   ...originalItem,
  //   quantityCounted: importedItem ? importedItem.quantityCounted : originalItem.quantityCounted,
  //   comment: importedItem ? importedItem.comment : originalItem.comment,
  //   updated: true,
  // });

  const importItems = async (importFile) => {
    // try {
    //   show();
    //   const response = await cycleCountApi.importCycleCountItems(
    //     importFile[0],
    //     currentLocation?.id,
    //   );
    //   setImportErrors(response.data.errors);
    //   let cycleCounts = _.groupBy(response.data.data, 'cycleCountId');
    //   const countedByUpdates = {};
    //   const dateCountedUpdates = {};
    //
    //   tableData.current = tableData.current.map((cycleCount) => {
    //     // After each iteration assign it to false again, so that the flag
    //     // can be reused for next cycle counts in the loop
    //     assigneeImported.current = false;
    //     return {
    //       ...cycleCount,
    //       cycleCountItems: [
    //         ...cycleCount.cycleCountItems
    //           .map((item) => {
    //             const correspondingImportItem = cycleCounts[cycleCount.id]?.find(
    //               (cycleCountItem) => cycleCountItem.cycleCountItemId === item.id,
    //             );
    //             // Assign counted by and date counted only once to prevent performance issues
    //             // At this point, every item after being validated on the backend,
    //             // should have the same assignee and dateCounted set,
    //             // so we can make this operation only once
    //             // this is why we introduce the assigneeImported boolean flag
    //             if (correspondingImportItem && !assigneeImported.current[cycleCount.id]) {
    //               countedByUpdates[cycleCount.id] = correspondingImportItem.assignee;
    //               // Do not allow to clear the date counted dropdown
    //               // if dateCounted was not set in the sheet
    //               if (correspondingImportItem.dateCounted) {
    //                 dateCountedUpdates[cycleCount.id] = correspondingImportItem.dateCounted;
    //               }
    //               // Mark the flag as true, so that it's not triggered for each item
    //               assigneeImported.current = true;
    //             }
    //
    //             if (correspondingImportItem) {
    //             // Remove items from the import that have a corresponding item
    //             // in the current cycle count. It allows us to treat items with
    //             // the wrong ID as new rows that do not already exist.
    //               cycleCounts = removeItemFromCycleCounts(
    //                 cycleCounts,
    //                 cycleCount.id,
    //                 correspondingImportItem.cycleCountItemId,
    //               );
    //             }
    //
    //             return mergeImportItems(item, correspondingImportItem);
    //           }),
    //         ...createCustomItemsFromImport(cycleCounts[cycleCount.id]),
    //       ],
    //     };
    //   });
    //   // Batch update refs
    //   countedBy.current = { ...countedBy.current, ...countedByUpdates };
    //   defaultCountedBy.current = { ...defaultCountedBy.current, ...countedByUpdates };
    //   dateCounted.current = { ...dateCounted.current, ...dateCountedUpdates };
    // } finally {
    //   hide();
    // }
  };

  const handleCountStepHeaderSave = async () => {
    await validateExistenceOfCycleCounts(save);

    // When we click "Save progress", we want to refetch the lot numbers
    // because the user may have created new ones and, without refetching,
    // they won't be available in the dropdown.
    // dispatch(fetchLotNumbersByProductIds(uniqueProductIds));
  };

  return {
    cycleCountIds,
    currentLocationId,
    printCountForm,
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
    handleCountStepHeaderSave,
  };
};

export default useCountStep;
