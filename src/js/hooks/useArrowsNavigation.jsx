import navigationKey from 'consts/navigationKey';

const useArrowsNavigation = ({
  newRowFocusColumns,
  existingRowFocusColumns,
  tableData,
  setFocusId,
  setFocusIndex,
  addNewRow,
}) => {
  const getNextFocus = (columnId, rowIndex) => {
    const currentIndex = newRowFocusColumns.indexOf(columnId);
    const remainingColumns = newRowFocusColumns.slice(currentIndex + 1);
    const newRowIndex = tableData[rowIndex + 1];

    if (!remainingColumns.some((col) => existingRowFocusColumns.includes(col))) {
      if (!newRowIndex) {
        addNewRow();
        return { newColumnId: newRowFocusColumns[0], newRowIndex: rowIndex + 1 };
      }

      if (newRowIndex?.id.includes('newRow')) {
        return { newColumnId: newRowFocusColumns[0], newRowIndex: rowIndex + 1 };
      }
      return { newColumnId: existingRowFocusColumns[0], newRowIndex: rowIndex + 1 };
    }

    if (tableData[rowIndex]?.id.includes('newRow')) {
      return { newColumnId: newRowFocusColumns[currentIndex + 1], newRowIndex: rowIndex };
    }
    return {
      newColumnId: existingRowFocusColumns[existingRowFocusColumns.indexOf(columnId) + 1],
      newRowIndex: rowIndex,
    };
  };

  const getPreviousFocus = (columnId, rowIndex) => {
    const currentIndex = newRowFocusColumns.indexOf(columnId);
    const previousColumns = newRowFocusColumns.slice(0, currentIndex).reverse();
    const previousRow = tableData[rowIndex - 1];
    let newColumnId = newRowFocusColumns[currentIndex - 1];
    let newRowIndex = rowIndex;

    const hasAllowedColumnToLeft = previousColumns
      .some((col) => existingRowFocusColumns.includes(col))
      || tableData[rowIndex]?.id.includes('newRow');

    if (currentIndex === 0 || !hasAllowedColumnToLeft) {
      if (!previousRow) {
        return { newColumnId: columnId, newRowIndex: rowIndex };
      }

      newRowIndex = rowIndex - 1;
      newColumnId = previousRow.id.includes('newRow')
        ? newRowFocusColumns[newRowFocusColumns.length - 1]
        : existingRowFocusColumns[existingRowFocusColumns.length - 1];
    }

    if (!(existingRowFocusColumns.includes(newColumnId) || tableData[newRowIndex]?.id.includes('newRow'))) {
      newColumnId = previousColumns.find((col) => existingRowFocusColumns.includes(col));
    }

    return { newColumnId, newRowIndex };
  };

  const handleKeyDown = (e, rowIndex, columnId) => {
    const { key } = e;

    if (key === navigationKey.ARROW_UP) {
      const isInArray = existingRowFocusColumns.includes(columnId);
      if (rowIndex > 0 && (isInArray || tableData[rowIndex - 1].id.includes('newRow'))) {
        setFocusIndex(rowIndex - 1);
        setFocusId(columnId);
      } else {
        e.preventDefault();
        e.stopPropagation();
      }
    }

    if (key === navigationKey.ARROW_DOWN) {
      if (rowIndex < tableData.length - 1) {
        setFocusIndex(rowIndex + 1);
        setFocusId(columnId);
      } else {
        e.preventDefault();
        e.stopPropagation();
      }
    }

    if (key === navigationKey.ARROW_RIGHT) {
      const { newColumnId, newRowIndex } = getNextFocus(columnId, rowIndex);
      setFocusId(newColumnId);
      setFocusIndex(newRowIndex);
    }

    if (key === navigationKey.ARROW_LEFT) {
      const { newColumnId, newRowIndex } = getPreviousFocus(columnId, rowIndex);
      setFocusId(newColumnId);
      setFocusIndex(newRowIndex);
    }
  };

  return { handleKeyDown };
};

export default useArrowsNavigation;
