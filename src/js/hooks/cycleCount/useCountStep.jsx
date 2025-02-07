import { useEffect, useRef, useState } from 'react';

import _ from 'lodash';
import { useDispatch, useSelector } from 'react-redux';

import { fetchUsers } from 'actions';
import cycleCountApi from 'api/services/CycleCountApi';
import useCountStepValidation from 'hooks/cycleCount/useCountStepValidation';

// Managing state for all tables, operations on shared state (from count step)
const useCountStep = () => {
  // Table data is stored using useRef to avoid re-renders onBlur
  // (it removes focus while selecting new fields)
  const tableData = useRef([]);
  // Saving selected "counted by" option
  const [countedBy, setCountedBy] = useState({});
  // Saving selected "date counted" option, initially it's the date fetched from API
  const [dateCounted, setDateCounted] = useState({});

  const dispatch = useDispatch();

  const {
    validationErrors,
    triggerValidation,
  } = useCountStepValidation({ tableData });

  const {
    cycleCountIds,
    currentLocation,
    users,
  } = useSelector((state) => ({
    users: state.users.data,
    cycleCountIds: state.cycleCount.toCount,
    currentLocation: state.session.currentLocation,
  }));

  useEffect(() => {
    (async () => {
      const { data } = await cycleCountApi.getCycleCounts(
        currentLocation?.id,
        cycleCountIds,
      );
      tableData.current = data?.data;
      const countedDates = data?.data?.reduce((acc, cycleCount) => ({
        ...acc,
        [cycleCount?.id]: cycleCount?.cycleCountItems[0].dateCounted,
      }), {});
      setDateCounted(countedDates);
    })();
  }, [cycleCountIds]);

  // Fetching data for "counted by" dropdown
  useEffect(() => {
    if (!users?.length) {
      dispatch(fetchUsers());
    }
  }, []);

  const printCountForm = () => {
    console.log('print count form');
  };

  const assignCountedBy = (productCode) => (person) => {
    setCountedBy((prevState) => ({ ...prevState, [productCode]: person }));
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
      binLocation: {
        name: undefined,
      },
      quantityCounted: undefined,
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
      console.log('next: ', tableData.current, countedBy);
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

  const getCountedDate = (cycleCountId) => dateCounted[cycleCountId];

  const setCountedDate = (cycleCountId) => (date) => {
    setDateCounted({
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
    printCountForm,
    assignCountedBy,
    getCountedDate,
    setCountedDate,
    next,
  };
};

export default useCountStep;
