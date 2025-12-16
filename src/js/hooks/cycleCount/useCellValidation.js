import {
  useMemo, useRef,
  useState,
} from 'react';

import { useSelector } from 'react-redux';
import { getCountWorkflowIsFormSubmitted, makeGetErrorForField } from 'selectors';

const useCellValidation = ({
  initialValue, cycleCountId, index, fieldName,
}) => {
  const [hasFocus, setHasFocus] = useState(false);

  const wasFieldTouched = useRef(false);

  const getErrorForField = useMemo(() => makeGetErrorForField(), []);

  const isFormSubmitted = useSelector(getCountWorkflowIsFormSubmitted);

  const error = useSelector((state) => getErrorForField(
    state,
    cycleCountId,
    parseInt(index, 10),
    fieldName,
  ));

  const onChangeValidationHandler = () => {
    wasFieldTouched.current = true;

    if (!hasFocus) {
      setHasFocus(true);
    }
  };

  const onBlurValidationHandler = () => {
    setHasFocus(false);
  };

  /**
   * Error visibility logic for form fields
   *
   * A validation error is shown only when ALL the following conditions are met:
   *
   * 1. An actual validation error exists "error"
   * 2. The field is NOT currently being edited "!hasFocus"
   *    - While the user is typing, errors are temporarily hidden to avoid noise
   *
   * 3. At least one of the following is true:
   *    a) The form has been submitted "isFormSubmitted"
   *       - After submit, all invalid fields should show their errors
   *
   *    b) The field has been interacted with before "wasFieldTouched.current"
   *       - If the user edited the field and left it (blur),
   *         validation feedback should be shown even without submitting the form
   *
   *    c) The field has a value "initialValue"
   *      - For fields that are pre-filled (e.g., editing existing data),
   *
   * This behavior ensures that:
   * - No errors are shown on the initial page load
   * - Errors appear after form submission
   * - Errors appear after a field was edited and validated
   * - Errors are hidden while the user is actively editing a field
   */

  const showError = error
    && !hasFocus
    && (isFormSubmitted || wasFieldTouched.current || initialValue);

  return {
    showError,
    error,
    onChangeValidationHandler,
    onBlurValidationHandler,
  };
};

export default useCellValidation;
