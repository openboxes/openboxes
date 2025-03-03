import { useEffect } from 'react';

import cycleCountColumn from 'consts/cycleCountColumn';

const useFocusOnMatch = ({
  focusId,
  fieldIndex,
  focusIndex,
  fieldId,
  ref,
}) => {
  useEffect(() => {
    const shouldFocus = focusId
      && fieldIndex === focusIndex
      && fieldId === focusId;

    if (shouldFocus && fieldId === cycleCountColumn.EXPIRATION_DATE) {
      ref.current?.setOpen(true);
      ref.current?.input?.focus();
      return;
    }
    if (shouldFocus) {
      ref.current?.focus();
    }
  }, [focusId, fieldIndex, focusIndex, fieldId, ref]);
};

export default useFocusOnMatch;
