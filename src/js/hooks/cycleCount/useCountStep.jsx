import { useRef, useState } from 'react';

import _ from 'lodash';
import { z } from 'zod';

import countingPageMockedData from 'consts/countingPageMockedData';
import useForceUpdate from 'hooks/useForceUpdate';
import useTranslate from 'hooks/useTranslate';

// Managing state for all tables, operations on shared state (from count step)
const useCountStep = () => {
  const forceUpdate = useForceUpdate();
  // Table data is stored using useRef to avoid re-renders onBlur
  // (it removes focus while selecting new fields)
  const tableData = useRef(countingPageMockedData.data);
  const [countedBy, setCountedBy] = useState({});
  const [validationErrors, setValidationErrors] = useState({});
  // I am not sure how to response from API will look like, so at least for now, I am grouping
  // the response by product code
  const dataGroupedByTables = _.groupBy(tableData.current, 'product.productCode');

  const translate = useTranslate();

  const printCountForm = () => {
    console.log('print count form');
  };

  const assignCountedBy = (productCode) => (person) => {
    setCountedBy((prevState) => ({ ...prevState, [productCode]: person }));
  };

  const addEmptyRow = (productCode) => {
    // ID is needed for updating appropriate row
    // Product is needed for placing row in appropriate table
    const emptyRow = {
      id: _.uniqueId('newRow'),
      product: {
        productCode,
      },
      internalLocation: undefined,
      lotNumber: undefined,
      expirationDate: undefined,
      quantityCounted: undefined,
      comment: '',
    };
    tableData.current = [...tableData.current, emptyRow];
    forceUpdate();
  };

  const removeRow = (id) => {
    tableData.current = tableData.current.filter((row) => id !== row.id);
    forceUpdate();
  };

  const checkBinLocationUniqueness = (data) => {
    if (!data.internalLocation) {
      return true;
    }
    const product = tableData.current.find((row) => row?.id === data.id)?.product?.productCode;
    const table = _.groupBy(tableData.current, 'product.productCode')[product];
    const groupedBinLocations = _.groupBy(table, 'internalLocation.id');
    return groupedBinLocations[data.internalLocation?.id].length === 1;
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
    expirationDate: z
      .string()
      .optional(),
    lotNumber: z
      .string()
      .optional(),
    internalLocation: z.object({
      id: z.string(),
      label: z.string(),
    }).optional(),
  }).refine((data) => !data.lotNumber || data.expirationDate, {
    path: ['expirationDate'],
    message: translate('react.cycleCount.requiredExpirationDate', 'Expiration date is required'),
  }).refine((data) => data.lotNumber || !data.expirationDate, {
    path: ['lotNumber'],
    message: translate('react.cycleCount.requiredLotNumber', 'Lot number is required'),
  }).refine(checkBinLocationUniqueness, {
    path: ['internalLocation'],
    message: translate('react.cycleCount.uniqueInternalLocation', 'Internal location should be unique'),
  });

  const rowsValidationSchema = z.array(rowValidationSchema);

  const triggerValidation = () => {
    const errors = rowsValidationSchema.safeParse(tableData.current);
    const errorsWithIds = tableData.current.reduce((acc, row, index) => ({
      ...acc,
      [row.id]: errors?.error?.format()?.[index],
    }), {});
    setValidationErrors(errorsWithIds);
    return errors.success;
  };

  const next = () => {
    const isValid = triggerValidation();
    if (isValid) {
      // This data should be combined to a single request
      console.log('next: ', tableData.current, countedBy);
    }
  };

  const updateRow = (id, columnId, value) => {
    tableData.current = (
      tableData.current.map(
        (row) => (id === row.id ? { ...row, [columnId]: value } : row),
      )
    );
  };

  const tableMeta = {
    updateData: (id, columnId, value) => {
      updateRow(id, columnId, value);
    },
  };

  return {
    dataGroupedByTables,
    tableMeta,
    validationErrors,
    addEmptyRow,
    removeRow,
    printCountForm,
    assignCountedBy,
    next,
  };
};

export default useCountStep;
