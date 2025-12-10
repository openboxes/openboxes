import { useCallback, useMemo } from 'react';

import { useDispatch, useStore } from 'react-redux';
import { getCountWorkflowEntities } from 'selectors';

import { setErrors } from 'actions';
import useTranslate from 'hooks/useTranslate';
import cycleCountSchemes from 'schemes/cycleCountSchemes';

const useCountStepValidation = () => {
  const translate = useTranslate();
  const dispatch = useDispatch();
  const store = useStore();

  const countStepValidationSchema = useMemo(
    () => cycleCountSchemes.getCountStepValidationSchema(translate),
    [translate],
  );

  // Synchronous validate for the entire count step
  const validateCountStep = useCallback(() => {
    const state = store.getState();
    const cycleCounts = getCountWorkflowEntities(state);
    const parsedSchema = countStepValidationSchema.safeParse(cycleCounts);
    dispatch(setErrors(parsedSchema.error?.format()));
    return parsedSchema.success;
  }, [countStepValidationSchema]);

  return {
    validateCountStep,
  };
};

export default useCountStepValidation;
