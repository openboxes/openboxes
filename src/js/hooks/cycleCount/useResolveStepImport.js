import { useCallback, useState } from 'react';

import useSpinner from 'hooks/useSpinner';
import { importCycleCountsRecount } from 'utils/cycleCountUtils';

const useResolveStepImport = ({
  currentLocationId,
  locale,
  tableData,
  recountedBy,
  defaultRecountedBy,
  dateRecounted,
}) => {
  const [importErrors, setImportErrors] = useState([]);
  const { show, hide } = useSpinner();

  const importItems = useCallback(async (importFile) => {
    try {
      show();
      await importCycleCountsRecount({
        importFile: importFile[0],
        locationId: currentLocationId,
        tableData,
        recountedBy,
        dateRecounted,
        defaultRecountedBy,
        setImportErrors,
        locale,
      });
    } finally {
      hide();
    }
  }, [currentLocationId, locale]);

  return {
    importErrors,
    importItems,
  };
};

export default useResolveStepImport;
