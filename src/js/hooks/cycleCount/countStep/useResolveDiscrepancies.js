/* eslint-disable no-restricted-syntax */
/* eslint-disable no-await-in-loop */
import { useCallback, useRef } from 'react';

import queryString from 'query-string';
import { useDispatch, useStore } from 'react-redux';
import { useHistory } from 'react-router-dom';

import { eraseDraft, startResolution } from 'actions';
import cycleCountApi from 'api/services/CycleCountApi';
import { CYCLE_COUNT_PENDING_REQUESTS } from 'api/urls';
import notification from 'components/Layout/notifications/notification';
import { CYCLE_COUNT } from 'consts/applicationUrls';
import { ALL_PRODUCTS_TAB, TO_COUNT_TAB, TO_RESOLVE_TAB } from 'consts/cycleCount';
import cycleCountStatus from 'consts/cycleCountStatus';
import NotificationType from 'consts/notificationTypes';
import useSpinner from 'hooks/useSpinner';
import useTranslate from 'hooks/useTranslate';
import apiClient from 'utils/apiClient';
import confirmationModal from 'utils/confirmationModalUtils';

const useResolveDiscrepancies = ({
  currentLocationId,
  openAssignCountModal,
  setIsSaveDisabled,
  requestIdsWithDiscrepanciesRef,
}) => {
  const internalRef = useRef([]);
  const requestIdsWithDiscrepancies = requestIdsWithDiscrepanciesRef || internalRef;
  const dispatch = useDispatch();
  const store = useStore();
  const history = useHistory();
  const { show, hide } = useSpinner();
  const translate = useTranslate();

  const showSuccessNotification = useCallback((count) => {
    notification(NotificationType.SUCCESS)({
      message: translate(
        'react.cycleCount.popup.success.label',
        `Successfully counted ${count} products`,
        { data: { count } },
      ),
    });
  }, [translate]);

  const submitCount = useCallback(() => {
    const state = store.getState();
    const cycleCounts = Object.values(state.countWorkflow.entities);
    return cycleCounts.reduce((acc, cycleCount) => ([
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
  }, [store, currentLocationId]);

  const redirectToNextTab = useCallback(async () => {
    const statusConfigs = [
      {
        statuses: [
          cycleCountStatus.COUNTED,
          cycleCountStatus.INVESTIGATING,
        ],
        tab: TO_RESOLVE_TAB,
      },
      {
        statuses: [
          cycleCountStatus.CREATED,
          cycleCountStatus.REQUESTED,
          cycleCountStatus.COUNTING,
        ],
        tab: TO_COUNT_TAB,
      },
    ];

    for (const { statuses, tab } of statusConfigs) {
      const { data } = await apiClient.get(CYCLE_COUNT_PENDING_REQUESTS(currentLocationId), {
        params: {
          facility: currentLocationId, statuses, tab, max: 1, offset: 0,
        },
        paramsSerializer: (params) => queryString.stringify(params),
      });

      if (data.totalCount > 0) {
        history.push(CYCLE_COUNT.list(tab));
        return;
      }
    }
    history.push(CYCLE_COUNT.list(ALL_PRODUCTS_TAB));
  }, [currentLocationId, history]);

  const modalLabels = useCallback((count) => ({
    title: { label: 'react.cycleCount.modal.resolveDiscrepanciesTitle.label', default: 'Resolve discrepancies?' },
    content: {
      label: 'react.cycleCount.modal.resolveDiscrepanciesContent.label',
      default: `There are ${count} products with a discrepancy to resolve. Would you like to resolve them?`,
      data: { count },
    },
  }), []);

  const resolveDiscrepanciesModalButtons = useCallback(
    (idsWithDiscrepancies, idsWithoutDiscrepancies) => (onClose) => ([
      {
        variant: 'transparent',
        defaultLabel: 'Not now',
        label: 'react.cycleCount.modal.notNow.label',
        onClick: () => {
          if (idsWithoutDiscrepancies > 0) {
            showSuccessNotification(idsWithoutDiscrepancies);
          }
          hide();
          onClose?.();
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
          await dispatch(startResolution(idsWithDiscrepancies, currentLocationId));
          if (idsWithoutDiscrepancies > 0) {
            showSuccessNotification(idsWithoutDiscrepancies);
          }
          hide();
          openAssignCountModal();
        },
      },
    ]), [currentLocationId],
  );

  const openResolveDiscrepanciesModal = useCallback(
    (idsWithDiscrepancies, idsWithoutDiscrepancies) => {
      confirmationModal({
        buttons: resolveDiscrepanciesModalButtons(idsWithDiscrepancies, idsWithoutDiscrepancies),
        ...modalLabels(idsWithDiscrepancies.length),
        hideCloseButton: true,
        closeOnClickOutside: false,
      });
    }, [resolveDiscrepanciesModalButtons, modalLabels],
  );

  const resolveDiscrepancies = useCallback(async () => {
    try {
      show();
      const submittedCounts = await Promise.all(submitCount());
      requestIdsWithDiscrepancies.current = submittedCounts.reduce((acc, req) => {
        if (req.data.data.status === cycleCountStatus?.COUNTED) {
          return [...acc, req.data.data.requestId];
        }
        return acc;
      }, []);

      dispatch(eraseDraft(currentLocationId, TO_COUNT_TAB));
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
  }, [currentLocationId]);

  return { resolveDiscrepancies, requestIdsWithDiscrepancies };
};

export default useResolveDiscrepancies;
