import { useCallback, useState } from 'react';

import { useDispatch, useStore } from 'react-redux';

import useSpinner from 'hooks/useSpinner';
import { importCycleCounts } from 'utils/cycleCountUtils';

const useCycleCountImport = (currentLocationId, locale) => {
  const [importErrors, setImportErrors] = useState([]);
  const dispatch = useDispatch();
  const store = useStore();
  const { show, hide } = useSpinner();

  const importItems = useCallback(async (importFile) => {
    try {
      show();
      const state = store.getState();
      const currentCycleCountEntities = Object.values(state.countWorkflow.entities);

      const importAction = await importCycleCounts({
        importFile: importFile[0],
        locationId: currentLocationId,
        currentCycleCountEntities,
        setImportErrors,
        locale,
      });

      dispatch(importAction);
    } finally {
      hide();
    }
  }, [currentLocationId, locale, store]);

  return {
    importErrors,
    setImportErrors,
    importItems,
  };
};

export default useCycleCountImport;
