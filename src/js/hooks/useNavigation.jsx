const useNavigation = ({
  newRowFocusColumns,
  existingRowFocusColumns,
  tableData,
  setFocusId,
  setFocusIndex,
  addEmptyRow,
  productCode,
  cycleCountId,
}) => {
  const getNextFocus = (columnId, rowIndex) => {
    const currentIndex = newRowFocusColumns.indexOf(columnId);
    const remainingColumns = newRowFocusColumns.slice(currentIndex + 1);
    const newRow = tableData[rowIndex + 1];
    let newColumnId = newRowFocusColumns[currentIndex + 1];

    if (!remainingColumns.some(col => existingRowFocusColumns.includes(col))) {
      if (remainingColumns.length > 0 && tableData[rowIndex]?.id.includes('newRow')) {
        return { newColumnId, newRowIndex: rowIndex };
      }

      if (!newRow) {
        addEmptyRow(productCode, cycleCountId);
        return { newColumnId: newRowFocusColumns[0], newRowIndex: rowIndex + 1 };
      }

      if (newRow?.id.includes('newRow')) {
        return { newColumnId: newRowFocusColumns[0], newRowIndex: rowIndex + 1 };
      }
      return { newColumnId: existingRowFocusColumns[0], newRowIndex: rowIndex + 1 };
    }

    if (!(existingRowFocusColumns.includes(newColumnId) || tableData[rowIndex]?.id.includes('newRow'))) {
      newColumnId = newRowFocusColumns.find(col => existingRowFocusColumns.includes(col));
    }
    return { newColumnId, newRowIndex: rowIndex };
  };

  const getPreviousFocus = (columnId, rowIndex) => {
    const currentIndex = newRowFocusColumns.indexOf(columnId);
    const previousColumns = newRowFocusColumns.slice(0, currentIndex).reverse();
    const previousRow = tableData[rowIndex - 1];
    let newColumnId = newRowFocusColumns[currentIndex - 1];
    let newRowIndex = rowIndex;

    const hasAllowedColumnToLeft = previousColumns
      .some(col => existingRowFocusColumns.includes(col))
      || tableData[rowIndex]?.id.includes('newRow');

    if (currentIndex === 0 || !hasAllowedColumnToLeft) {
      if (previousRow) {
        newRowIndex = rowIndex - 1;
        newColumnId = previousRow.id.includes('newRow')
          ? newRowFocusColumns[newRowFocusColumns.length - 1]
          : existingRowFocusColumns[existingRowFocusColumns.length - 1];
      } else {
        return { newColumnId: columnId, newRowIndex: rowIndex };
      }
    }

    const isAllowedColumn = existingRowFocusColumns.includes(newColumnId) || tableData[newRowIndex]?.id.includes('newRow');
    if (!isAllowedColumn) {
      newColumnId = previousColumns.find(col => existingRowFocusColumns.includes(col))
        || existingRowFocusColumns[existingRowFocusColumns.length - 1];
    }

    return { newColumnId, newRowIndex };
  };

  const handleKeyDown = (e, rowIndex, columnId) => {
    const { key } = e;

    if (key === 'ArrowUp') {
      const isInArray = existingRowFocusColumns.includes(columnId);
      if (rowIndex > 0 && (isInArray || tableData[rowIndex - 1].id.includes('newRow'))) {
        setFocusIndex(rowIndex - 1);
        setFocusId(columnId);
      } else {
        e.preventDefault();
        e.stopPropagation();
      }
    }

    if (key === 'ArrowDown') {
      if (rowIndex < tableData.length - 1) {
        setFocusIndex(rowIndex + 1);
        setFocusId(columnId);
      } else {
        e.preventDefault();
        e.stopPropagation();
      }
    }

    if (key === 'ArrowRight') {
      const { newColumnId, newRowIndex } = getNextFocus(columnId, rowIndex);
      setFocusId(newColumnId);
      setFocusIndex(newRowIndex);
    }

    if (key === 'ArrowLeft') {
      const { newColumnId, newRowIndex } = getPreviousFocus(columnId, rowIndex);
      setFocusId(newColumnId);
      setFocusIndex(newRowIndex);
    }
  };

  return { handleKeyDown };
};

export default useNavigation;
