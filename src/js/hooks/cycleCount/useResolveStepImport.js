import { useCallback, useState } from 'react';

import { useDispatch, useStore } from 'react-redux';

import useSpinner from 'hooks/useSpinner';
import { importCycleCounts } from 'utils/cycleCountUtils';

const useResolveStepImport = (currentLocationId, locale) => {
  const [importErrors, setImportErrors] = useState([]);
  const dispatch = useDispatch();
  const store = useStore();
  const { show, hide } = useSpinner();

  //  TODO: This function should be changed in the next ticket OBPIH-7338,
  // because I copied this from count step and this is not working properly
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
    importItems,
  };
};

export default useResolveStepImport;
