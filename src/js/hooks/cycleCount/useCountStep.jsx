import { useEffect, useRef, useState } from 'react';

import _ from 'lodash';
import { useDispatch, useSelector } from 'react-redux';
import { z } from 'zod';

import { fetchUsers } from 'actions';
import cycleCountApi from 'api/services/CycleCountApi';
import useForceUpdate from 'hooks/useForceUpdate';
import useTranslate from 'hooks/useTranslate';

// Managing state for all tables, operations on shared state (from count step)
const useCountStep = () => {
  const forceUpdate = useForceUpdate();
  // Table data is stored using useRef to avoid re-renders onBlur
  // (it removes focus while selecting new fields)
  const tableData = useRef([]);
  const [countedBy, setCountedBy] = useState({});
  const [dateCounted, setDateCounted] = useState({});
  const validationErrors = useRef({});

  const translate = useTranslate();

  const dispatch = useDispatch();

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

  const addEmptyRow = (productCode, id) => {
    // ID is needed for updating appropriate row
    // Product is needed for placing row in appropriate table
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
    forceUpdate();
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
    forceUpdate();
  };

  const checkLotNumberUniqueness = (data) => {
    if (!data?.inventory?.lotNumber) {
      return true;
    }
    const product = tableData.current.find((row) => row?.id === data.id)?.product?.productCode;
    const table = _.groupBy(tableData.current, 'product.productCode')[product];
    const groupedLotNumbers = _.groupBy(table, 'inventoryItem.lotNumber');
    return groupedLotNumbers[data.inventoryItem?.lotNumber].length === 1;
  };

  const rowValidationSchema = z.object({
    id: z
      .string(),
    quantityCounted: z
      .number({
        required_error: translate('react.cycleCount.requiredQuantityCounted', 'Quantity counted is required'),
        invalid_type_error: translate('react.cycleCount.requiredQuantityCounted', 'Quantity counted is required'),
      })
      .gte(0),
    inventoryItem: z.object({
      expirationDate: z
        .string()
        .optional(),
      lotNumber: z
        .string()
        .optional(),
    }).optional(),
    internalLocation: z.object({
      id: z.string(),
      name: z.string(),
      label: z.string().optional(),
    }).optional(),
  }).refine((data) => data?.inventoryItem?.lotNumber || !data?.inventoryItem?.expirationDate, {
    path: ['inventoryItem.lotNumber'],
    message: translate('react.cycleCount.requiredLotNumber', 'Lot number is required'),
  }).refine(checkLotNumberUniqueness, {
    path: ['inventoryItem.lotNumber'],
    message: translate('react.cycleCount.uniqueLotNumber', 'Lot number should be unique'),
  });

  const rowsValidationSchema = z.array(rowValidationSchema);

  const triggerValidation = () => {
    const errors = tableData.current.reduce((acc, cycleCount) => {
      const parsedValidation = rowsValidationSchema.safeParse(cycleCount.cycleCountItems);
      return {
        ...acc,
        [cycleCount.id]: {
          errors: parsedValidation?.error?.format(),
          success: parsedValidation.success,
        },
      };
    }, {});

    validationErrors.current = errors;
    return _.every(Object.values(errors), (val) => val.success);
  };

  const next = () => {
    const isValid = triggerValidation();
    forceUpdate();
    if (isValid) {
      // This data should be combined to a single request
      console.log('next: ', tableData.current, countedBy);
    }
  };

  const updateRow = (cycleCountId, rowId, columnId, value) => {
    const tableIndex = tableData.current.findIndex(
      (cycleCount) => cycleCount?.id === cycleCountId,
    );
    const rowIndex = tableData.current[tableIndex].cycleCountItems.findIndex(
      (row) => row.id === rowId,
    );
    // Nested path in colum names contains "_" instead of "."
    const nestedPath = columnId.replaceAll('_', '.');
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
    validationErrors: validationErrors.current,
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
