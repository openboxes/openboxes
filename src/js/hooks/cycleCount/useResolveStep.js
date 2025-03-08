import {
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';

import _ from 'lodash';
import { useDispatch, useSelector } from 'react-redux';

import { fetchBinLocations, fetchUsers } from 'actions';
import cycleCountApi from 'api/services/CycleCountApi';
import { CYCLE_COUNT } from 'api/urls';
import useResolveStepValidation from 'hooks/cycleCount/useResolveStepValidation';
import useSpinner from 'hooks/useSpinner';
import exportFileFromApi from 'utils/file-download-util';
import { checkBinLocationSupport } from 'utils/supportedActivitiesUtils';

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

  const showBinLocation = useMemo(() =>
    checkBinLocationSupport(currentLocation.supportedActivities), [currentLocation?.id]);

  useEffect(() => {
    if (showBinLocation) {
      dispatch(fetchBinLocations(currentLocation?.id));
    }
  }, [currentLocation?.id]);

  const mergeCycleCountItems = (items) => {
    const duplicatedItems = _.groupBy(items,
      (item) => `${item.binLocation?.id}-${item?.inventoryItem?.lotNumber}`);
    return Object.values(duplicatedItems).map((itemsToMerge) => {
      const maxCountIndex = _.maxBy(itemsToMerge, 'countIndex').countIndex;
      const itemFromCount = _.find(itemsToMerge, (item) => item.countIndex === maxCountIndex - 1);
      const itemFromResolve = _.find(itemsToMerge, (item) => item.countIndex === maxCountIndex);
      return itemFromCount ? {
        ...itemFromResolve,
        ...itemFromCount,
        commentFromCount: itemFromCount?.comment,
        quantityRecounted: itemFromResolve?.quantityCounted,
        dateRecounted: itemFromResolve?.dateCounted,
        comment: itemFromResolve?.comment,
      } : {
        ...itemFromResolve,
        commentFromCount: itemFromResolve?.comment,
        quantityRecounted: null,
        dateRecounted: null,
        comment: null,
      };
    });
  };

  const fetchCycleCounts = async () => {
    const { data } = await cycleCountApi.getCycleCounts(
      currentLocation?.id,
      cycleCountIds,
    );
    tableData.current = data?.data?.map((cycleCount) =>
      ({ ...cycleCount, cycleCountItems: mergeCycleCountItems(cycleCount.cycleCountItems) }));
    const recountedDates = tableData.current?.reduce((acc, cycleCount) => ({
      ...acc,
      [cycleCount?.id]: cycleCount?.cycleCountItems?.[0]?.dateRecounted,
    }), {});
    setDateRecounted(recountedDates);
  };

  useEffect(() => {
    fetchCycleCounts();
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
      params: { id: cycleCountIds },
      format,
    });
    hide();
  };

  const refreshCountItems = async () => {
    try {
      show();
      // eslint-disable-next-line no-restricted-syntax
      for (const cycleCountId of cycleCountIds) {
        // eslint-disable-next-line no-await-in-loop
        await cycleCountApi.refreshItems(currentLocation?.id, cycleCountId);
      }
    } finally {
      hide();
      await fetchCycleCounts();
    }
  };

  const assignRecountedBy = (cycleCountId) => (person) => {
    setRecountedBy((prevState) => ({ ...prevState, [cycleCountId]: person }));
  };

  const getRecountedBy = (cycleCountId) => recountedBy?.[cycleCountId];

  const getCountedBy = (cycleCountId) => tableData?.current.find(
    (cycleCount) => cycleCount?.id === cycleCountId,
  )?.cycleCountItems?.[0]?.assignee;

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
    getCountedBy,
    addEmptyRow,
    removeRow,
    printRecountForm,
    refreshCountItems,
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
