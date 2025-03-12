/* eslint-disable no-restricted-syntax */
/* eslint-disable no-await-in-loop */

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
  const [refreshFocusCounter, setRefreshFocusCounter] = useState(0);
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
    reasonCodes,
    users,
  } = useSelector((state) => ({
    users: state.users.data,
    cycleCountIds: state.cycleCount.cycleCounts,
    reasonCodes: state.cycleCount.reasonCodes,
    currentLocation: state.session.currentLocation,
  }));

  const resetFocus = () => {
    setRefreshFocusCounter((prev) => prev + 1);
  };

  const showBinLocation = useMemo(() =>
    checkBinLocationSupport(currentLocation.supportedActivities), [currentLocation?.id]);

  useEffect(() => {
    if (showBinLocation) {
      dispatch(fetchBinLocations(currentLocation?.id));
    }
  }, [currentLocation?.id]);

  const mapRootCauseToSelectedOption = (rootCause) => (rootCause ? ({
    id: rootCause?.name,
    label: reasonCodes?.find?.((reasonCode) => reasonCode?.id === rootCause?.name)?.label,
    value: rootCause?.name,
  }) : null);

  const mergeCycleCountItems = (items) => {
    const duplicatedItems = _.groupBy(items,
      (item) => `${item.binLocation?.id}-${item?.inventoryItem?.lotNumber}`);
    return Object.values(duplicatedItems).map((itemsToMerge) => {
      if (itemsToMerge.length === 1) {
        const item = itemsToMerge[0];
        return {
          ...item,
          quantityRecounted: item?.quantityCounted,
          dateRecounted: item?.dateCounted,
          recountedBy: item?.assignee,
          quantityCounted: null,
          commentFromCount: null,
          dateCounted: null,
          countedBy: null,
          rootCause: mapRootCauseToSelectedOption(item?.discrepancyReasonCode),
        };
      }

      const maxCountIndex = _.maxBy(itemsToMerge, 'countIndex').countIndex;
      const itemFromCount = _.find(itemsToMerge, (item) => item.countIndex === maxCountIndex - 1);
      const itemFromResolve = _.find(itemsToMerge, (item) => item.countIndex === maxCountIndex);
      return {
        ...itemFromCount,
        ...itemFromResolve,
        commentFromCount: itemFromCount?.comment,
        quantityRecounted: itemFromResolve?.quantityCounted,
        dateCounted: itemFromCount?.dateCounted,
        dateRecounted: itemFromResolve?.dateCounted,
        countedBy: itemFromCount?.assignee,
        recountedBy: itemFromResolve?.assignee,
        rootCause: mapRootCauseToSelectedOption(itemFromResolve?.discrepancyReasonCode),
      };
    });
  };

  const refetchData = async () => {
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
    (async () => {
      await refetchData();
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
      params: { id: cycleCountIds },
      format,
    });
    resetFocus();
    hide();
  };

  const getRecountedBy = (cycleCountId) => recountedBy?.[cycleCountId];

  const getCountedBy = (cycleCountId) => tableData?.current.find(
    (cycleCount) => cycleCount?.id === cycleCountId,
  )?.cycleCountItems?.find((row) => row?.countedBy)?.countedBy;

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
    resetFocus();
    triggerValidation();
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
    resetFocus();
    const isValid = triggerValidation();
    if (!isValid) {
      return;
    }

    const missingRootCauses = validateRootCauses();
    if (!isRootCauseWarningSkipped && missingRootCauses.length > 0) {
      showEmptyRootCauseWarning();
      return;
    }

    setIsStepEditable(false);
  };

  const back = () => {
    setIsStepEditable(true);
    resetFocus();
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

  const assignRecountedBy = (cycleCountId) => (person) => {
    markAllItemsAsUpdated(cycleCountId);
    setRecountedBy((prevState) => ({ ...prevState, [cycleCountId]: person }));
  };

  const getRecountedDate = (cycleCountId) => dateRecounted[cycleCountId];

  const setRecountedDate = (cycleCountId) => (date) => {
    setDateRecounted({
      ...dateRecounted,
      [cycleCountId]: date.format(),
    });
    markAllItemsAsUpdated(cycleCountId);
    resetFocus();
  };

  const getPayload = (cycleCountItem, cycleCount) => ({
    quantityCounted: cycleCountItem?.quantityRecounted,
    countIndex: 1,
    inventoryItem: {
      ...cycleCountItem.inventoryItem,
      product: cycleCountItem?.product?.id,
    },
    binLocation: cycleCountItem?.binLocation,
    comment: cycleCountItem?.comment,
    dateCounted: getRecountedDate(cycleCount?.id),
    id: cycleCountItem?.id,
    facility: cycleCountItem?.facility,
    discrepancyReasonCode: cycleCountItem?.rootCause?.id,
    assignee: getRecountedBy(cycleCount.id)?.id,
    recount: true,
  });

  const save = async () => {
    try {
      show();
      for (const cycleCount of tableData.current) {
        const cycleCountItemsToUpdate = cycleCount.cycleCountItems.filter((item) => (item.updated && !item.id.includes('newRow')));
        for (const cycleCountItem of cycleCountItemsToUpdate) {
          await cycleCountApi.updateCycleCountItem(getPayload(cycleCountItem, cycleCount),
            currentLocation?.id, cycleCountItem?.id);
        }
        const cycleCountItemsToCreate = cycleCount.cycleCountItems.filter((item) => item.id.includes('newRow'));
        for (const cycleCountItem of cycleCountItemsToCreate) {
          await cycleCountApi.createCycleCountItem(getPayload(cycleCountItem, cycleCount),
            currentLocation?.id, cycleCount?.id);
        }

        // Now that we've successfully saved all the items, mark them all as not updated so that
        // we don't try to update them again next time something is changed.
        markAllItemsAsNotUpdated(cycleCount.id);
      }
    } finally {
      // After the save, refetch cycle counts so that a new row can't be saved multiple times
      await refetchData();
      hide();
      resetFocus();
    }
  };

  const submitRecount = () => {
    console.log('submit');
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

  const getProduct = (cycleCountItems) => cycleCountItems[0]?.product;

  const getDateCounted = (cycleCountItems) =>
    cycleCountItems?.find((row) => row.dateCounted)?.dateCounted;

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
    assignRecountedBy,
    getRecountedDate,
    setRecountedDate,
    shouldHaveRootCause,
    next,
    save,
    submitRecount,
    back,
    getProduct,
    getDateCounted,
    refreshFocusCounter,
  };
};

export default useResolveStep;
