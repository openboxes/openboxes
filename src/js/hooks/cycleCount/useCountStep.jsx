import { useEffect, useRef, useState } from 'react';

import _ from 'lodash';
import { useDispatch, useSelector } from 'react-redux';

import { fetchUsers } from 'actions';
import cycleCountApi from 'api/services/CycleCountApi';
import useCountStepValidation from 'hooks/cycleCount/useCountStepValidation';
import useSpinner from 'hooks/useSpinner';

// Managing state for all tables, operations on shared state (from count step)
const useCountStep = () => {
  // Table data is stored using useRef to avoid re-renders onBlur
  // (it removes focus while selecting new fields)
  const tableData = useRef([]);
  // Saving selected "counted by" option
  const [countedBy, setCountedBy] = useState({});
  const [defaultCountedBy, setDefaultCountedBy] = useState({});
  // Saving selected "date counted" option, initially it's the date fetched from API
  const [dateCounted, setDateCounted] = useState({});
  const [isStepEditable, setIsStepEditable] = useState(true);
  const dispatch = useDispatch();
  const { show, hide } = useSpinner();

  const {
    validationErrors,
    triggerValidation,
  } = useCountStepValidation({ tableData });

  const {
    cycleCountIds,
    currentLocation,
  } = useSelector((state) => ({
    cycleCountIds: state.cycleCount.requests,
    currentLocation: state.session.currentLocation,
  }));
  const fetchCycleCounts = async () => {
    try {
      show();
      const { data } = await cycleCountApi.getCycleCounts(
        currentLocation?.id,
        cycleCountIds,
      );
      tableData.current = data?.data;
      // Date counted and assignee are the same for all items,
      // so we create a map looking at first item
      const countedDates = data?.data?.reduce((acc, cycleCount) => ({
        ...acc,
        [cycleCount?.id]: cycleCount?.cycleCountItems[0].dateCounted,
      }), {});
      setDateCounted(countedDates);
      const countedByMap = data?.data?.reduce((acc, cycleCount) => ({
        ...acc,
        [cycleCount?.id]: cycleCount?.cycleCountItems[0].assignee,
      }), {});
      setCountedBy(countedByMap);
      setDefaultCountedBy(countedByMap);
    } finally {
      hide();
    }
  };

  useEffect(() => {
    fetchCycleCounts();
  }, [cycleCountIds]);

  // Fetching data for "counted by" dropdown
  useEffect(() => {
    dispatch(fetchUsers());
  }, []);

  const printCountForm = () => {
    console.log('print count form');
  };

  const setAllItemsUpdatedState = (cycleCountId, updated) => {
    const tableIndex = tableData.current.findIndex(
      (cycleCount) => cycleCount?.id === cycleCountId,
    );
    tableData.current = tableData.current.map((cycleCount, index) => {
      if (index === tableIndex) {
        return {
          ...cycleCount,
          cycleCountItems: cycleCount.cycleCountItems.map((item) => ({ ...item, updated })),
        };
      }
      return cycleCount;
    });
  };

  const markAllItemsAsUpdated = (cycleCountId) => setAllItemsUpdatedState(cycleCountId, true);

  const markAllItemsAsNotUpdated = (cycleCountId) => setAllItemsUpdatedState(cycleCountId, false);

  const assignCountedBy = (cycleCountId) => (person) => {
    // We need to mark all items as updated if we change the counted by person,
    // because counted by is associated with every cycle count item and needs to be set
    // for every item
    markAllItemsAsUpdated(cycleCountId);
    setCountedBy((prevState) => ({ ...prevState, [cycleCountId]: person }));
  };

  const getCountedBy = (cycleCountId) => countedBy?.[cycleCountId];

  const getDefaultCountedBy = (cycleCountId) => defaultCountedBy?.[cycleCountId];

  const removeRow = async (cycleCountId, rowId) => {
    if (rowId.includes('newRow')) {
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
      return;
    }
    try {
      show();
      await cycleCountApi.deleteCycleCountItem(currentLocation?.id, rowId);
    } finally {
      hide();
      await fetchCycleCounts();
    }
  };

  const addEmptyRow = (productId, id) => {
    // ID is needed for updating appropriate row
    const emptyRow = {
      id: _.uniqueId('newRow'),
      product: {
        id: productId,
      },
      inventoryItem: {
        lotNumber: null,
        expirationDate: null,
      },
      binLocation: null,
      quantityCounted: null,
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
      setIsStepEditable(false);
    }
  };
  const back = () => {
    setIsStepEditable(true);
  };

  const save = async () => {
    // eslint-disable-next-line no-restricted-syntax
    for (const cycleCount of tableData.current) {
      const cycleCountItemsToUpdate = cycleCount.cycleCountItems.filter((item) => (item.updated && !item.id.includes('newRow')));
      // eslint-disable-next-line no-restricted-syntax
      for (const cycleCountItem of cycleCountItemsToUpdate) {
        try {
          show();
          // eslint-disable-next-line no-await-in-loop
          await cycleCountApi.updateCycleCountItem({
            ...cycleCountItem,
            recount: false,
            assignee: getCountedBy(cycleCount.id)?.id,
          },
          currentLocation?.id, cycleCountItem?.id);
        } finally {
          hide();
        }
      }
      const cycleCountItemsToCreate = cycleCount.cycleCountItems.filter((item) => item.id.includes('newRow'));
      // eslint-disable-next-line no-restricted-syntax
      for (const cycleCountItem of cycleCountItemsToCreate) {
        try {
          show();
          // eslint-disable-next-line no-await-in-loop
          await cycleCountApi.createCycleCountItem({
            ...cycleCountItem,
            recount: false,
            inventoryItem: {
              ...cycleCountItem.inventoryItem,
              product: cycleCountItem.product?.id,
            },
            assignee: getCountedBy(cycleCount.id)?.id,
          }, currentLocation?.id, cycleCount?.id);
        } finally {
          hide();
        }
      }

      // Now that we've successfully saved all the items, mark them all as not updated so that
      // we don't try to update them again next time something is changed.
      markAllItemsAsNotUpdated(cycleCount.id);
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

    // Mark item as updated, so that the item can be easily distinguished whether it was updated
    _.set(tableData.current, `[${tableIndex}].cycleCountItems[${rowIndex}].updated`, true);
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
    getCountedBy,
    getDefaultCountedBy,
    countedBy,
    getCountedDate,
    setCountedDate,
    next,
    back,
    save,
    isStepEditable,
  };
};

export default useCountStep;
