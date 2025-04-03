import navigationKey from 'consts/navigationKey';

const useArrowsNavigation = ({
  newRowFocusableCells,
  existingRowFocusableCells,
  tableData,
  setColumnId,
  setRowIndex,
  addNewRow,
  isNewRow,
  getValues,
  setValue,
  onBlur,
}) => {
  const getNextFocus = (columnId, rowIndex) => {
    const currentIndex = newRowFocusableCells.indexOf(columnId);
    const remainingColumns = newRowFocusableCells.slice(currentIndex + 1);
    const newRowIndex = tableData[rowIndex + 1];

    if (!remainingColumns.some((col) => existingRowFocusableCells.includes(col))) {
      if (!newRowIndex) {
        addNewRow();
        return { newColumnId: newRowFocusableCells[0], newRowIndex: rowIndex + 1 };
      }

      if (isNewRow(newRowIndex)) {
        return { newColumnId: newRowFocusableCells[0], newRowIndex: rowIndex + 1 };
      }
      return { newColumnId: existingRowFocusableCells[0], newRowIndex: rowIndex + 1 };
    }

    if (isNewRow(tableData[rowIndex])) {
      return { newColumnId: newRowFocusableCells[currentIndex + 1], newRowIndex: rowIndex };
    }
    return {
      newColumnId: existingRowFocusableCells[existingRowFocusableCells.indexOf(columnId) + 1],
      newRowIndex: rowIndex,
    };
  };

  const getPreviousFocus = (columnId, rowIndex) => {
    const currentIndex = newRowFocusableCells.indexOf(columnId);
    const previousColumns = newRowFocusableCells.slice(0, currentIndex).reverse();
    const previousRow = tableData[rowIndex - 1];
    let newColumnId = newRowFocusableCells[currentIndex - 1];
    let newRowIndex = rowIndex;

    const hasAllowedColumnToLeft = previousColumns
      .some((col) => existingRowFocusableCells.includes(col))
      || isNewRow(tableData[rowIndex]);

    if (currentIndex === 0 || !hasAllowedColumnToLeft) {
      if (!previousRow) {
        return { newColumnId: columnId, newRowIndex: rowIndex };
      }

      newRowIndex = rowIndex - 1;
      newColumnId = isNewRow(previousRow) || previousColumns.id === null
        ? newRowFocusableCells[newRowFocusableCells.length - 1]
        : existingRowFocusableCells[existingRowFocusableCells.length - 1];
    }

    if (!(existingRowFocusableCells.includes(newColumnId) || isNewRow(tableData[newRowIndex]))) {
      newColumnId = previousColumns.find((col) => existingRowFocusableCells.includes(col));
    }

    return { newColumnId, newRowIndex };
  };

  const handleKeyDown = (e, rowIndex, columnId) => {
    const { key, ctrlKey } = e;

    if (key === navigationKey.ARROW_UP) {
      const isInArray = existingRowFocusableCells.includes(columnId);
      if (rowIndex > 0 && (isInArray || isNewRow(tableData[rowIndex - 1]))) {
        setRowIndex(rowIndex - 1);
        setColumnId(columnId);
        onBlur();
      }
      e.preventDefault();
      e.stopPropagation();
    }

    if (key === navigationKey.ARROW_DOWN) {
      const isLastRow = rowIndex === tableData.length - 1;
      const isLastColumn = columnId
        === existingRowFocusableCells[existingRowFocusableCells.length - 1];

      // Here we check getValues and setValue because in cycleCount I haven't implemented it yet.
      // It's a safeguard to ensure they are available before using them
      if (ctrlKey && !isLastRow && getValues && setValue) {
        const currentValue = getValues(`values.lineItems.${rowIndex}.${columnId}`);
        const nextRowIndex = rowIndex + 1;
        setRowIndex(nextRowIndex);
        setColumnId(columnId);
        // Only copy the value if it's not empty/undefined/null
        if (currentValue !== '' && currentValue !== undefined && currentValue !== null) {
          setValue(`values.lineItems.${nextRowIndex}.${columnId}`, currentValue);
        }
        onBlur();
        // We add e.preventDefault(), e.stopPropagation and return here
        // to stop further event propagation, as the next condition below
        // might also be met and add another row unintentionally
        e.preventDefault();
        e.stopPropagation();
      }

      if (!ctrlKey && isLastRow && isLastColumn && getValues && setValue) {
        addNewRow();
        setRowIndex(rowIndex + 1);
        setColumnId(newRowFocusableCells[0]);
        onBlur();
        e.preventDefault();
        e.stopPropagation();
        return;
      }
      if (!ctrlKey && isLastRow && isLastColumn) {
        addNewRow();
        setRowIndex(rowIndex + 1);
        setColumnId(newRowFocusableCells[0]);
        onBlur();
      }

      if (!isLastRow) {
        setRowIndex(rowIndex + 1);
        setColumnId(columnId);
        onBlur();
      }

      e.preventDefault();
      e.stopPropagation();
    }

    if (key === navigationKey.ARROW_RIGHT) {
      const { newColumnId, newRowIndex } = getNextFocus(columnId, rowIndex);
      setColumnId(newColumnId);
      setRowIndex(newRowIndex);
      onBlur();
    }

    if (key === navigationKey.ARROW_LEFT) {
      const { newColumnId, newRowIndex } = getPreviousFocus(columnId, rowIndex);
      setColumnId(newColumnId);
      setRowIndex(newRowIndex);
      onBlur();
    }
  };

  return { handleKeyDown };
};

export default useArrowsNavigation;
