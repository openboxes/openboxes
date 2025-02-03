import { useRef, useState } from 'react';

import _ from 'lodash';

import countingPageMockedData from 'consts/countingPageMockedData';
import useForceUpdate from 'hooks/useForceUpdate';

// Managing state for all tables, operations on shared state (from count step)
const useCountStep = () => {
  const forceUpdate = useForceUpdate();
  const tableData = useRef(countingPageMockedData.data);
  const [countedBy, setCountedBy] = useState({});
  // I am not sure how to response from API will look like, so at least for now, I am grouping
  // the response by product code
  const dataGroupedByTables = _.groupBy(tableData.current, 'product.productCode');

  const printCountForm = () => {
    console.log('print count form');
  };

  const next = () => {
    // This data should be combined to a single request
    console.log('next: ', tableData.current, countedBy);
  };

  const updateRow = (id, columnId, value) => {
    tableData.current = (
      tableData.current.map(
        (row) => (id === row.id ? { ...row, [columnId]: value } : row),
      )
    );
  };

  const assignCountedBy = (productCode) => (person) => {
    setCountedBy((prevState) => ({ ...prevState, [productCode]: person }));
  };

  const tableMeta = {
    updateData: (id, columnId, value) => {
      updateRow(id, columnId, value);
    },
  };

  const addEmptyRow = (productCode) => {
    // ID is needed for updating appropriate row
    // Product is needed for placing row in appropriate table
    const emptyRow = {
      id: _.uniqueId('newRow'),
      product: {
        productCode,
      },
      internalLocation: '',
      lotNumber: '',
      expirationDate: null,
      quantityCounted: null,
      comment: '',
    };
    tableData.current = [...tableData.current, emptyRow];
    forceUpdate();
  };

  const removeRow = (id) => {
    tableData.current = (
      tableData.current.data.filter((row) => id !== row.id)
    );
    forceUpdate();
  };

  return {
    dataGroupedByTables,
    tableMeta,
    addEmptyRow,
    removeRow,
    printCountForm,
    assignCountedBy,
    next,
  };
};

export default useCountStep;
