import { useCallback, useRef, useState } from 'react';

import { useDispatch, useStore } from 'react-redux';
import { useNavigate } from 'react-router-dom';

import { clearCountWorkflow } from 'actions';
import { CYCLE_COUNT } from 'consts/applicationUrls';

const useCycleCountModal = (requestIdsWithDiscrepanciesRef) => {
  const [isAssignCountModalOpen, setIsAssignCountModalOpen] = useState(false);
  const assignCountModalData = useRef([]);
  const dispatch = useDispatch();
  const navigate = useNavigate();
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
    dispatch(clearCountWorkflow);
    navigate(CYCLE_COUNT.resolveStep());
  }, []);

  return {
    isAssignCountModalOpen,
    assignCountModalData,
    openAssignCountModal,
    closeAssignCountModal,
  };
};

export default useCycleCountModal;
