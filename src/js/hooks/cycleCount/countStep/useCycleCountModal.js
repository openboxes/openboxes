import { useCallback, useRef, useState } from 'react';

import { useStore } from 'react-redux';
import { useHistory } from 'react-router-dom';

import { CYCLE_COUNT } from 'consts/applicationUrls';

const useCycleCountModal = (requestIdsWithDiscrepanciesRef) => {
  const [isAssignCountModalOpen, setIsAssignCountModalOpen] = useState(false);
  const assignCountModalData = useRef([]);
  const history = useHistory();
  const store = useStore();

  const mapSelectedRowsToModalData = useCallback(() => {
    const state = store.getState();
    const cycleCounts = Object.values(state.countWorkflow.entities);

    const modalDataWithDiscrepancies = cycleCounts.filter(
      (cycleCount) => requestIdsWithDiscrepanciesRef.current.includes(cycleCount?.requestId),
    );

    assignCountModalData.current = modalDataWithDiscrepancies.map((cycleCount) => ({
      product: cycleCount?.cycleCountItems?.[0]?.product,
      cycleCountRequestId: cycleCount?.requestId,
      inventoryItemsCount: cycleCount?.cycleCountItems?.length || 0,
      assignee: cycleCount?.verificationCount?.assignee,
      deadline: cycleCount?.verificationCount?.deadline,
    }));
  }, [store]);

  const openAssignCountModal = useCallback(() => {
    mapSelectedRowsToModalData();
    setIsAssignCountModalOpen(true);
  }, []);

  const closeAssignCountModal = useCallback(() => {
    setIsAssignCountModalOpen(false);
    assignCountModalData.current = [];
    history.push(CYCLE_COUNT.resolveStep());
  }, []);

  return {
    isAssignCountModalOpen,
    assignCountModalData,
    openAssignCountModal,
    closeAssignCountModal,
  };
};

export default useCycleCountModal;
