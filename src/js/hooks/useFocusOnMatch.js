import { useEffect } from 'react';

import componentType from 'consts/componentType';

const useFocusOnMatch = ({
  focusId,
  fieldIndex,
  focusIndex,
  fieldId,
  ref,
  type,
}) => {
  useEffect(() => {
    const shouldFocus = focusId
      && fieldIndex === focusIndex
      && fieldId === focusId;

    if (shouldFocus && type === componentType.DATE_FIELD) {
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
