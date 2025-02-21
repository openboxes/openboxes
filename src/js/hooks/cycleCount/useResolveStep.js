import { useEffect, useRef, useState } from 'react';

import _ from 'lodash';
import { useDispatch, useSelector } from 'react-redux';

import { fetchUsers } from 'actions';
import cycleCountApi from 'api/services/CycleCountApi';
import useResolveStepValidation from 'hooks/cycleCount/useResolveStepValidation';

import 'react-tippy/dist/tippy.css';

// Managing state for all tables, operations on shared state (from resolve step)
const useResolveStep = () => {
  // Table data is stored using useRef to avoid re-renders onBlur
  // (it removes focus while selecting new fields)
  const tableData = useRef([]);
  // Saving selected "recounted by" option
  const [recountedBy, setRecountedBy] = useState({});
  // Saving selected "date recounted" option, initially it's the date fetched from API
  const [dateRecounted, setDateRecounted] = useState({});

  const {
    validationErrors,
    triggerValidation,
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

  const printRecountForm = () => {
    console.log('print count form');
  };

  const assignRecountedBy = (productCode) => (person) => {
    setRecountedBy((prevState) => ({ ...prevState, [productCode]: person }));
  };

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
        lotNumber: undefined,
        expirationDate: undefined,
      },
      binLocation: undefined,
      quantityRecounted: undefined,
      quantityCounted: undefined,
      rootCause: undefined,
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
    if (isValid) {
      // This data should be combined to a single request
      console.log('next: ', tableData.current, recountedBy, dateRecounted);
    }
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
    addEmptyRow,
    removeRow,
    printRecountForm,
    assignRecountedBy,
    getRecountedDate,
    setRecountedDate,
    next,
  };
};

export default useResolveStep;
