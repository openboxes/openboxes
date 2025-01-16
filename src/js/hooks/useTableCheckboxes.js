import { useCallback, useEffect, useState } from 'react';

const useTableCheckboxes = () => {
  const [checkedCheckboxes, setCheckedCheckboxes] = useState([]);
  const [headerCheckboxState, setHeaderCheckboxState] = useState({
    indeterminate: false,
    value: false,
  });

  const selectRow = (identifier) => useCallback(() => {
    if (checkedCheckboxes.includes(identifier)) {
      setCheckedCheckboxes(
        (checked) => checked.filter((checkedProductId) => identifier !== checkedProductId),
      );
      return;
    }
    setCheckedCheckboxes((checked) => ([...checked, identifier]));
  }, [checkedCheckboxes]);

  useEffect(() => {
    const selectedRows = checkedCheckboxes.length;
    setHeaderCheckboxState({
      indeterminate: selectedRows > 0,
      value: selectedRows !== 0,
    });
  }, [checkedCheckboxes.length]);

  const isChecked = useCallback(
    (identifier) => checkedCheckboxes.includes(identifier),
    [checkedCheckboxes],
  );

  // Argument is a function to avoid calculating data in the main component
  // Data is calculated directly when the function is called
  const selectHeaderCheckbox = (getIdentifiers) => useCallback(() => {
    setHeaderCheckboxState((headerState) => ({
      indeterminate: !headerState.indeterminate,
      value: !headerState.value,
    }));

    if (headerCheckboxState.indeterminate) {
      setCheckedCheckboxes([]);
      return;
    }

    setCheckedCheckboxes(getIdentifiers());
  }, [
    headerCheckboxState.indeterminate,
    headerCheckboxState.value,
  ]);

  return {
    selectRow,
    isChecked,
    selectHeaderCheckbox,
    checkedCheckboxes,
    selectedRowsAmount: checkedCheckboxes.length,
    headerCheckboxProps: headerCheckboxState,
  };
};

export default useTableCheckboxes;
