import { useCallback } from 'react';

import useBinLocationAutofill from 'hooks/useBinLocationAutofill';

/**
 * Location autofill of the "Receiving now" table in the edit modal, triggered from the
 * Location column header dropdown. Adapts the shared autofill to the react-hook-form state:
 * rows are read with getValues and the { rowId: { binLocation } } updates are written back
 * with per-path setValue calls.
 */
const useEditModalLocationAutofill = ({
  getValues,
  setValue,
}) => {
  const getRowBinLocation = (item) => item.binLocation;

  const getRows = useCallback(() => getValues('lineItems'), [getValues]);

  const updateLineItems = useCallback((newDataByRowId) => {
    getValues('lineItems').forEach((item, index) => {
      const newData = newDataByRowId[item.rowId];
      if (newData) {
        setValue(`lineItems.${index}.binLocation`, newData.binLocation);
      }
    });
  }, [getValues, setValue]);

  return useBinLocationAutofill({
    getRows,
    getRowBinLocation,
    updateLineItems,
  });
};

export default useEditModalLocationAutofill;
