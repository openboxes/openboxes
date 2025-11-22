import { useCallback, useState } from 'react';

import { useDispatch, useStore } from 'react-redux';

import { fetchCycleCounts, markAllItemsAsNotUpdated } from 'actions';
import { UPDATE_CYCLE_COUNT_IDS } from 'actions/types';
import cycleCountApi from 'api/services/CycleCountApi';
import { CYCLE_COUNT as CYCLE_COUNT_URL } from 'api/urls';
import notification from 'components/Layout/notifications/notification';
import NotificationType from 'consts/notificationTypes';
import { DateFormat } from 'consts/timeFormat';
import useSpinner from 'hooks/useSpinner';
import useTranslate from 'hooks/useTranslate';
import { trimLotNumberSpaces } from 'utils/cycleCountUtils';
import dateWithoutTimeZone from 'utils/dateUtils';
import exportFileFromApi from 'utils/file-download-util';

const useCycleCountPersistence = (
  currentLocationId,
  cycleCountIds,
  sortByProductName,
  currentUserId,
  locale,
) => {
  const dispatch = useDispatch();
  const store = useStore();
  const translate = useTranslate();
  const { show, hide } = useSpinner();
  const [isSaveDisabled, setIsSaveDisabled] = useState(false);

  const validateExistenceOfCycleCounts = useCallback(async (callback) => {
    const { data } = await cycleCountApi.getCycleCounts(
      currentLocationId,
      cycleCountIds,
    );

    const state = store.getState();
    const cycleCounts = Object.values(state.countWorkflow.entities);

    const { existingCycleCountsIds, canceledCycleCountsIds } = cycleCounts.reduce(
      (acc, curr) => {
        if (data.data.find((cycleCount) => cycleCount.id === curr.id)) {
          return { ...acc, existingCycleCountsIds: [...acc.existingCycleCountsIds, curr.id] };
        }
        return { ...acc, canceledCycleCountsIds: [...acc.canceledCycleCountsIds, curr.id] };
      },
      { existingCycleCountsIds: [], canceledCycleCountsIds: [] },
    );

    if (canceledCycleCountsIds.length > 0) {
      dispatch({
        type: UPDATE_CYCLE_COUNT_IDS,
        payload: { locationId: currentLocationId, cycleCounts: existingCycleCountsIds },
      });
      notification(NotificationType.ERROR_FILLED)({
        message: 'Error',
        details: translate(
          'react.cycleCount.canceledCycleCounts.error.label',
          'Some inventory changes may not be appearing because you canceled a product in the current count/recount. Please reload the page to continue.',
        ),
      });
      return false;
    }
    return callback();
  }, [currentLocationId, cycleCountIds, store]);

  const getPayload = useCallback((cycleCountItem, shouldSetDefaultAssignee) => {
    const state = store.getState();
    const countedBy = state.countWorkflow.countedBy?.[cycleCountItem.cycleCountId]?.id;
    const dateCounted = state.countWorkflow.dateCounted?.[cycleCountItem.cycleCountId];

    return {
      ...cycleCountItem,
      recount: false,
      assignee: shouldSetDefaultAssignee ? countedBy ?? currentUserId : countedBy,
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
  }, [store, currentUserId, locale]);

  const save = useCallback(async (shouldSetDefaultAssignee = false) => {
    try {
      show();
      const cycleCountItemsToUpdateBatch = [];
      const cycleCountItemsToCreateBatch = [];
      const state = store.getState();
      const cycleCounts = Object.values(state.countWorkflow.entities);

      // eslint-disable-next-line no-restricted-syntax
      for (const cycleCount of cycleCounts) {
        const itemsBase = { cycleCountId: cycleCount.id };

        const cycleCountItemsToUpdate = cycleCount.cycleCountItems
          .filter((item) => (item.updated || !item.assignee) && !item.id.includes('newRow'))
          .map((item) => ({ ...trimLotNumberSpaces(item), ...itemsBase }));

        if (cycleCountItemsToUpdate.length > 0) {
          cycleCountItemsToUpdateBatch.push(cycleCountItemsToUpdate);
        }

        const cycleCountItemsToCreate = cycleCount.cycleCountItems
          .filter((item) => item.id.includes('newRow'))
          .map((item) => ({ ...trimLotNumberSpaces(item), ...itemsBase }));

        if (cycleCountItemsToCreate.length > 0) {
          cycleCountItemsToCreateBatch.push(cycleCountItemsToCreate);
        }
      }

      dispatch(markAllItemsAsNotUpdated(cycleCountIds));

      if (cycleCountItemsToUpdateBatch.length > 0) {
        await cycleCountApi.updateCycleCountItemsBatch({
          itemsToUpdate: cycleCountItemsToUpdateBatch
            .flat()
            .map((item) => getPayload(item, shouldSetDefaultAssignee)),
        }, currentLocationId);
      }

      if (cycleCountItemsToCreateBatch.length > 0) {
        await cycleCountApi.createCycleCountItemsBatch({
          itemsToCreate: cycleCountItemsToCreateBatch
            .flat()
            .map((item) => getPayload(item, shouldSetDefaultAssignee)),
        }, currentLocationId);
      }
    } finally {
      dispatch(fetchCycleCounts(cycleCountIds, currentLocationId, sortByProductName));
      hide();
    }
  }, [cycleCountIds, currentLocationId, sortByProductName]);

  const printCountForm = useCallback(async (format) => {
    show();
    await save();
    await exportFileFromApi({
      url: CYCLE_COUNT_URL(currentLocationId),
      params: { id: cycleCountIds, sortBy: sortByProductName && 'productName' },
      format,
    });
    hide();
  }, [currentLocationId, cycleCountIds, sortByProductName, save]);

  return {
    save, printCountForm, validateExistenceOfCycleCounts, isSaveDisabled, setIsSaveDisabled,
  };
};

export default useCycleCountPersistence;
