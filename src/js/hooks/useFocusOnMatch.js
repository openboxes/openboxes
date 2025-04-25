import { useEffect } from 'react';

import componentType from 'consts/componentType';

const useFocusOnMatch = ({
  columnId,
  fieldIndex,
  rowIndex,
  fieldId,
  ref,
  type,
}) => {
  useEffect(() => {
    const shouldFocus = columnId
      && fieldIndex === rowIndex
      && fieldId === columnId;

    if (shouldFocus && type === componentType.DATE_FIELD) {
      ref.current?.setOpen(true);
      ref.current?.input?.focus();
      return;
    }
    if (shouldFocus) {
      ref.current?.focus();
    }
  }, [columnId, fieldIndex, rowIndex, fieldId, ref]);
};

export default useFocusOnMatch;
