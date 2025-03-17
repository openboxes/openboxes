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
import { useHistory } from 'react-router-dom';

import {
  eraseDraft,
  fetchBinLocations,
  fetchUsers,
  startResolution,
} from 'actions';
import cycleCountApi from 'api/services/CycleCountApi';
import { CYCLE_COUNT as CYCLE_COUNT_URL } from 'api/urls';
import { CYCLE_COUNT } from 'consts/applicationUrls';
import { TO_COUNT_TAB, TO_RESOLVE_TAB } from 'consts/cycleCount';
import cycleCountStatus from 'consts/cycleCountStatus';
import useCountStepValidation from 'hooks/cycleCount/useCountStepValidation';
import useSpinner from 'hooks/useSpinner';
import confirmationModal from 'utils/confirmationModalUtils';
import exportFileFromApi from 'utils/file-download-util';
import { checkBinLocationSupport } from 'utils/supportedActivitiesUtils';

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
  const history = useHistory();
  const { show, hide } = useSpinner();

  const {
    cycleCountIds,
    currentLocation,
  } = useSelector((state) => ({
    cycleCountIds: state.cycleCount.requests,
    currentLocation: state.session.currentLocation,
  }));

  const showBinLocation = useMemo(() =>
    checkBinLocationSupport(currentLocation.supportedActivities), [currentLocation?.id]);

  useEffect(() => {
    if (showBinLocation) {
      dispatch(fetchBinLocations(currentLocation?.id));
    }
  }, [currentLocation?.id]);

  const {
    validationErrors,
    triggerValidation,
    triggerRerenderAfterAddingNewRow,
    isFormValid,
  } = useCountStepValidation({ tableData });

  const filterCountItems = (cycleCounts) => cycleCounts.map((cycleCount) => ({
    ...cycleCount,
    cycleCountItems: cycleCount.cycleCountItems.filter((item) => item.countIndex === 0),
  }));

  const fetchCycleCounts = async () => {
    try {
      show();
      const { data } = await cycleCountApi.getCycleCounts(
        currentLocation?.id,
        cycleCountIds,
      );
      tableData.current = filterCountItems(data?.data);
      // Date counted and assignee are the same for all items,
      // so we create a map looking at first item
      const countedDates = data?.data?.reduce((acc, cycleCount) => ({
        ...acc,
        [cycleCount?.id]: cycleCount?.cycleCountItems[0]?.dateCounted,
      }), {});
      setDateCounted(countedDates);
      const countedByMap = data?.data?.reduce((acc, cycleCount) => ({
        ...acc,
        [cycleCount?.id]: cycleCount?.cycleCountItems[0]?.assignee,
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

  const printCountForm = async (format) => {
    show();
    await exportFileFromApi({
      url: CYCLE_COUNT_URL(currentLocation?.id),
      params: { id: cycleCountIds },
      format,
    });
    hide();
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
    setDefaultCountedBy((prevState) => ({ ...prevState, [cycleCountId]: person }));
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
    triggerRerenderAfterAddingNewRow();
  };

  const next = () => {
    const isValid = triggerValidation();
    const areCountedByFilled = _.every(
      cycleCountIds,
      (id) => getCountedBy(id)?.id,
    );
    if (isValid && areCountedByFilled) {
      setIsStepEditable(false);
    }
  };

  const back = () => {
    setIsStepEditable(true);
  };

  const getCountedDate = (cycleCountId) => dateCounted[cycleCountId];

  const save = async () => {
    try {
      show();
      for (const cycleCount of tableData.current) {
        const cycleCountItemsToUpdate = cycleCount.cycleCountItems.filter((item) => (item.updated && !item.id.includes('newRow')));
        for (const cycleCountItem of cycleCountItemsToUpdate) {
          await cycleCountApi.updateCycleCountItem({
            ...cycleCountItem,
            recount: false,
            assignee: getCountedBy(cycleCount.id)?.id,
            dateCounted: getCountedDate(cycleCount.id),
          },
          currentLocation?.id, cycleCountItem?.id);
        }
        const cycleCountItemsToCreate = cycleCount.cycleCountItems.filter((item) => item.id.includes('newRow'));
        for (const cycleCountItem of cycleCountItemsToCreate) {
          await cycleCountApi.createCycleCountItem({
            ...cycleCountItem,
            recount: false,
            inventoryItem: {
              ...cycleCountItem.inventoryItem,
              product: cycleCountItem.product?.id,
            },
            assignee: getCountedBy(cycleCount.id)?.id,
            dateCounted: getCountedDate(cycleCount.id),
          }, currentLocation?.id, cycleCount?.id);
        }

        // Now that we've successfully saved all the items, mark them all as not updated so that
        // we don't try to update them again next time something is changed.
        markAllItemsAsNotUpdated(cycleCount.id);
      }
    } finally {
      // After the save, refetch cycle counts so that a new row can't be saved multiple times
      await fetchCycleCounts();
      hide();
    }
  };

  const modalLabels = {
    title: {
      label: 'react.cycleCount.modal.resolveDiscrepanciesTitle.label',
      default: 'Resolve discrepancies?',
    },
    content: {
      label: 'react.cycleCount.modal.resolveDiscrepanciesContent.label',
      default: 'There are discrepancies to resolve. Would you like to resolve them?',
    },
  };

  const submitCount = () =>
    tableData.current.reduce((acc, cycleCount) => ([
      ...acc,
      cycleCountApi.submitCount({
        refreshQuantityOnHand: true,
        failOnOutdatedQuantity: false,
        requireRecountOnDiscrepancy: true,
        cycleCountItems: cycleCount.cycleCountItems,
      },
      currentLocation?.id,
      cycleCount?.id),
    ]), []);

  const resolveDiscrepanciesModalButtons = (requestIdsWithDiscrepancies) => (onClose) => ([
    {
      variant: 'transparent',
      defaultLabel: 'Not now',
      label: 'react.cycleCount.modal.notNow.label',
      onClick: () => {
        history.push(CYCLE_COUNT.list(TO_RESOLVE_TAB));
        onClose?.();
      },
    },
    {
      variant: 'primary',
      defaultLabel: 'Resolve',
      label: 'react.cycleCount.modal.resolve.label',
      onClick: async () => {
        await dispatch(startResolution(
          requestIdsWithDiscrepancies,
          currentLocation?.id,
        ));
        history.push(CYCLE_COUNT.resolveStep());
        onClose?.();
      },
    },
  ]);

  const openResolveDiscrepanciesModal = (requestIdsWithDiscrepancies) => {
    confirmationModal({
      buttons: resolveDiscrepanciesModalButtons(requestIdsWithDiscrepancies),
      ...modalLabels,
      hideCloseButton: true,
    });
  };

  const resolveDiscrepancies = async () => {
    try {
      show();
      await save();
      const submittedCounts = await Promise.all(submitCount());

      const requestIdsWithDiscrepancies = submittedCounts
        .reduce((acc, submittedCycleCountRequest) => {
          const { data } = submittedCycleCountRequest;
          if (data.data.status === cycleCountStatus?.COUNTED) {
            return [...acc, data?.data?.requestId];
          }

          return acc;
        }, []);

      if (requestIdsWithDiscrepancies.length > 0) {
        openResolveDiscrepanciesModal(requestIdsWithDiscrepancies);
        return;
      }
      dispatch(eraseDraft());
      history.push(CYCLE_COUNT.list(TO_COUNT_TAB));
    } finally {
      hide();
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

  const setCountedDate = (cycleCountId) => (date) => {
    markAllItemsAsUpdated(cycleCountId);
    setDateCounted((prevState) => ({
      ...prevState,
      [cycleCountId]: date.format(),
    }));
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
    resolveDiscrepancies,
    isStepEditable,
    isFormValid,
  };
};

export default useCountStep;
