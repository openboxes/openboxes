import { useCallback, useMemo, useState } from 'react';

import { useDispatch, useSelector } from 'react-redux';
import {
  getCurrentLocale,
  getCurrentLocationId,
  getCurrentUserId,
  getCycleCountRequestIds,
} from 'selectors';

import { fetchLotNumbersByProductIds } from 'actions';
import useCycleCountFetchData from 'hooks/cycleCount/countStep/useCycleCountFetchData';
import useCycleCountImport from 'hooks/cycleCount/countStep/useCycleCountImport';
import useCycleCountModal from 'hooks/cycleCount/countStep/useCycleCountModal';
import useCycleCountPersistence from 'hooks/cycleCount/countStep/useCycleCountPersistence';
import useResolveDiscrepancies from 'hooks/cycleCount/countStep/useResolveDiscrepancies';

const useCountStep = () => {
  const [sortByProductName, setSortByProductName] = useState(false);
  const [isStepEditable, setIsStepEditable] = useState(true);

  const dispatch = useDispatch();
  const currentLocationId = useSelector(getCurrentLocationId);
  const currentUserId = useSelector(getCurrentUserId);
  const cycleCountIds = useSelector(getCycleCountRequestIds);
  const locale = useSelector(getCurrentLocale);

  // 1. Data Fetching
  const { uniqueProductIds } = useCycleCountFetchData(
    currentLocationId,
    cycleCountIds,
    sortByProductName,
    isStepEditable,
  );

  // 2. Import
  const { importErrors, importItems } = useCycleCountImport(currentLocationId, locale);

  // 3. Persistence (Save/Print)
  const {
    save,
    printCountForm,
    validateExistenceOfCycleCounts,
    isSaveDisabled,
    setIsSaveDisabled,
  } = useCycleCountPersistence(
    currentLocationId,
    cycleCountIds,
    sortByProductName,
    currentUserId,
    locale,
  );

  // 4. Resolve Discrepancies
  //  We define useResolveDiscrepancies before useCycleCountModal fully initiates, but
  //  useCycleCountModal needs the Ref from "Resolve", and "Resolve" needs the Open function from
  //  Modal. To solve this circular dependency properly in hooks:
  //  - the Ref is owned by `useResolveDiscrepancies`. We pass it to `useCycleCountModal`.
  const requestIdsWithDiscrepancies = useMemo(
    () => ({ current: [] }),
    [],
  );

  const {
    isAssignCountModalOpen,
    assignCountModalData,
    openAssignCountModal,
    closeAssignCountModal,
  } = useCycleCountModal(requestIdsWithDiscrepancies);

  const { resolveDiscrepancies } = useResolveDiscrepancies({
    currentLocationId,
    openAssignCountModal,
    setIsSaveDisabled,
    // Injecting the ref so the hook updates THIS ref instead of its own internal one
    requestIdsWithDiscrepanciesRef: requestIdsWithDiscrepancies,
  });

  // 5. Navigation & Header Actions
  const next = useCallback(async () => {
    await save(true);
    setIsStepEditable(false);
  }, [save]);

  const back = useCallback(() => {
    setIsStepEditable(true);
  }, []);

  const handleCountStepHeaderSave = useCallback(async () => {
    await validateExistenceOfCycleCounts(save);
    dispatch(fetchLotNumbersByProductIds(uniqueProductIds));
  }, [validateExistenceOfCycleCounts, save, dispatch, uniqueProductIds]);

  return useMemo(() => ({
    cycleCountIds,
    currentLocationId,

    // State
    isStepEditable,
    sortByProductName,
    setSortByProductName,
    isSaveDisabled,
    setIsSaveDisabled,
    importErrors,

    // Actions
    importItems,
    printCountForm,
    validateExistenceOfCycleCounts,
    next,
    back,
    handleCountStepHeaderSave,
    resolveDiscrepancies,

    // Modal
    isAssignCountModalOpen,
    closeAssignCountModal,
    assignCountModalData,
  }), [
    cycleCountIds,
    currentLocationId,
    isStepEditable,
    sortByProductName,
    isSaveDisabled,
    importErrors,
    importItems,
    printCountForm,
    validateExistenceOfCycleCounts,
    next,
    back,
    handleCountStepHeaderSave,
    resolveDiscrepancies,
    isAssignCountModalOpen,
    closeAssignCountModal,
    assignCountModalData,
  ]);
};

export default useCountStep;
