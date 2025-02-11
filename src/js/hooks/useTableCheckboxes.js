import { useCallback, useEffect, useState } from 'react';

import { useSelector } from 'react-redux';

const useTableCheckboxes = () => {
  const [checkedCheckboxes, setCheckedCheckboxes] = useState([]);
  const [headerCheckboxState, setHeaderCheckboxState] = useState({
    indeterminate: false,
    value: false,
  });
  const { currentLocation } = useSelector((state) => state.session);
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
    const amountOfCheckedCheckboxes = checkedCheckboxes.length;
    setHeaderCheckboxState({
      indeterminate: amountOfCheckedCheckboxes > 0,
      value: amountOfCheckedCheckboxes !== 0,
    });
  }, [checkedCheckboxes.length]);

  const isChecked = useCallback(
    (identifier) => checkedCheckboxes.includes(identifier),
    [checkedCheckboxes],
  );

  const selectHeaderCheckbox = (identifiers) => useCallback(() => {
    setHeaderCheckboxState((headerState) => ({
      indeterminate: !headerState.indeterminate,
      value: !headerState.value,
    }));

    if (headerCheckboxState.indeterminate) {
      setCheckedCheckboxes([]);
      return;
    }

    setCheckedCheckboxes(identifiers);
  }, [
    headerCheckboxState.indeterminate,
    headerCheckboxState.value,
  ]);

  useEffect(() => {
    if (checkedCheckboxes.length > 0
      || headerCheckboxState.value
      || headerCheckboxState.indeterminate) {
      setCheckedCheckboxes([]);
      setHeaderCheckboxState({ indeterminate: false, value: false });
    }
  }, [currentLocation?.id]);

  return {
    selectRow,
    isChecked,
    selectHeaderCheckbox,
    checkedCheckboxes,
    selectedCheckboxesAmount: checkedCheckboxes.length,
    headerCheckboxProps: headerCheckboxState,
  };
};

export default useTableCheckboxes;
