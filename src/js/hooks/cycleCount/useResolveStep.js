import { useEffect, useRef, useState } from 'react';

import _ from 'lodash';
import { useDispatch, useSelector } from 'react-redux';

import { fetchUsers } from 'actions';
import cycleCountApi from 'api/services/CycleCountApi';
import { CYCLE_COUNT } from 'api/urls';
import useResolveStepValidation from 'hooks/cycleCount/useResolveStepValidation';
import useSpinner from 'hooks/useSpinner';
import exportFileFromApi from 'utils/file-download-util';

// Managing state for all tables, operations on shared state (from resolve step)
const useResolveStep = () => {
  // Table data is stored using useRef to avoid re-renders onBlur
  // (it removes focus while selecting new fields)
  const tableData = useRef([]);
  // Saving selected "recounted by" option
  const [recountedBy, setRecountedBy] = useState({});
  // Saving selected "date recounted" option, initially it's the date fetched from API
  const [dateRecounted, setDateRecounted] = useState({});
  const [isStepEditable, setIsStepEditable] = useState(true);
  const { show, hide } = useSpinner();

  const {
    validationErrors,
    isRootCauseWarningSkipped,
    triggerValidation,
    validateRootCauses,
    shouldHaveRootCause,
    showEmptyRootCauseWarning,
  } = useResolveStepValidation({ tableData });

  const dispatch = useDispatch();

  const {
    cycleCountIds,
    currentLocation,
    users,
  } = useSelector((state) => ({
    users: state.users.data,
    cycleCountIds: state.cycleCount.cycleCounts,
    currentLocation: state.session.currentLocation,
  }));

  useEffect(() => {
    (async () => {
      const { data } = await cycleCountApi.getCycleCounts(
        currentLocation?.id,
        cycleCountIds,
      );
      tableData.current = data?.data;
      const recountedDates = data?.data?.reduce((acc, cycleCount) => ({
        ...acc,
        // Replace it with the recounted date from the response
        [cycleCount?.id]: new Date(),
      }), {});
      setDateRecounted(recountedDates);
    })();
  }, [cycleCountIds]);

  // Fetching data for "recounted by" dropdown
  useEffect(() => {
    if (!users?.length) {
      dispatch(fetchUsers());
    }
  }, []);

  const printRecountForm = async (format) => {
    show();
    await exportFileFromApi({
      url: CYCLE_COUNT(currentLocation?.id),
      params: { ids: cycleCountIds },
      format,
    });
    hide();
  };

  const assignRecountedBy = (cycleCountId) => (person) => {
    setRecountedBy((prevState) => ({ ...prevState, [cycleCountId]: person }));
  };

  const getRecountedBy = (cycleCountId) => recountedBy?.[cycleCountId];

  const removeRow = (cycleCountId, rowId) => {
    const tableIndex = tableData.current.findIndex(
      (cycleCount) => cycleCount?.id === cycleCountId,
    );
    tableData.current = tableData.current.map((data, index) => {
      if (index === tableIndex) {
        return {
          ...data,
          cycleCountItems: data.cycleCountItems.filter((row) => row.id !== rowId),
        };
      }

      return data;
    });
    triggerValidation();
  };

  const addEmptyRow = (productCode, id) => {
    // ID is needed for updating appropriate row
    const emptyRow = {
      id: _.uniqueId('newRow'),
      product: {
        productCode,
      },
      inventoryItem: {
        lotNumber: null,
        expirationDate: null,
      },
      binLocation: null,
      quantityRecounted: null,
      quantityCounted: null,
      rootCause: null,
      comment: '',
    };
    const tableIndex = tableData.current.findIndex(
      (cycleCount) => cycleCount?.id === id,
    );
    tableData.current = tableData.current.map((data, index) => {
      if (index === tableIndex) {
        return {
          ...data,
          cycleCountItems: [
            ...data.cycleCountItems,
            emptyRow,
          ],
        };
      }

      return data;
    });
    triggerValidation();
  };

  const next = () => {
    const isValid = triggerValidation();
    if (!isValid) {
      return;
    }

    const missingRootCauses = validateRootCauses();
    if (!isRootCauseWarningSkipped && missingRootCauses.length > 0) {
      showEmptyRootCauseWarning();
      return;
    }

    console.log('next: ', tableData.current, recountedBy, dateRecounted);
    setIsStepEditable(false);
  };

  const back = () => {
    setIsStepEditable(true);
  };

  const save = () => {
    console.log('save');
  };

  const updateRow = (cycleCountId, rowId, columnId, value) => {
    // Find table index, for which the row should be updated
    const tableIndex = tableData.current.findIndex(
      (cycleCount) => cycleCount?.id === cycleCountId,
    );
    // Find updated row index
    const rowIndex = tableData.current[tableIndex].cycleCountItems.findIndex(
      (row) => row.id === rowId,
    );
    // Nested path in colum names contains "_" instead of "."
    const nestedPath = columnId.replaceAll('_', '.');
    // Update data for: cycleCount (table) -> cycleCountItem (row) -> column (nestedPath)
    _.set(tableData.current, `[${tableIndex}].cycleCountItems[${rowIndex}].${nestedPath}`, value);
  };

  const tableMeta = {
    updateData: (cycleCountId, rowId, columnId, value) => {
      updateRow(cycleCountId, rowId, columnId, value);
    },
  };

  const getRecountedDate = (cycleCountId) => dateRecounted[cycleCountId];

  const setRecountedDate = (cycleCountId) => (date) => {
    setDateRecounted({
      ...date,
      [cycleCountId]: date,
    });
  };

  return {
    tableData: tableData.current,
    tableMeta,
    validationErrors,
    isStepEditable,
    getRecountedBy,
    addEmptyRow,
    removeRow,
    printRecountForm,
    assignRecountedBy,
    getRecountedDate,
    setRecountedDate,
    shouldHaveRootCause,
    next,
    save,
    back,
  };
};

export default useResolveStep;
