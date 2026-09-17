import {
  useCallback, useMemo, useRef, useState,
} from 'react';

import { useDispatch } from 'react-redux';

import { hideSpinner, showSpinner } from 'actions';
import CheckStep from 'components/receivingV2/CheckStep';
import ReceivingStep from 'components/receivingV2/ReceivingStep';
import useTranslate from 'hooks/useTranslate';
import useWizard from 'hooks/useWizard';

const ReceivingStepKey = {
  RECEIVING: 'RECEIVING',
  CHECK: 'CHECK',
};

/**
 * Steps of the receiving wizard and their transitions.
 *
 * @returns {{
 *   Step: Object,
 *   stepsTitles: Array,
 *   flushRef: Object,
 *   validateBeforeNextRef: Object,
 *   setNextDisabled: Function,
 *   isNextDisabled: boolean,
 *   completeReceiptRef: Object,
 *   isCheckStep: boolean,
 *   previous: Function,
 *   onNext: Function,
 *   onCompleteReceipt: Function,
 * }} `Step` - the current step ({ key, Component }).
 *   `stepsTitles` - step titles for the wizard header.
 *   `flushRef` - filled by the receiving step with its autosave flush.
 *   `validateBeforeNextRef` - filled by the receiving step with the validation guarding
 *     the transition.
 *   `setNextDisabled` - lets the current step disable the Next button.
 *   `isNextDisabled` - whether the Next button is disabled.
 *   `completeReceiptRef` - filled by the check step with its complete receipt handler.
 *   `isCheckStep` - true on the check (last) step.
 *   `previous` - goes back to the receiving step.
 *   `onNext` - validates the step, saves pending edits, then goes to the next step.
 *   `onCompleteReceipt` - runs the handler registered by the check step.
 */
const useReceivingSteps = () => {
  const translate = useTranslate();
  const dispatch = useDispatch();
  const flushRef = useRef(null);
  const validateBeforeNextRef = useRef(null);
  const completeReceiptRef = useRef(null);
  const [isNextDisabled, setNextDisabled] = useState(false);

  const steps = useMemo(() => [
    {
      key: ReceivingStepKey.RECEIVING,
      title: translate('react.receiving.receiving.label', 'Receiving'),
      Component: ReceivingStep,
    },
    {
      key: ReceivingStepKey.CHECK,
      title: translate('react.receiving.check.label', 'Check'),
      Component: CheckStep,
    },
  ], [translate]);

  const stepsTitles = useMemo(
    () => steps.map((step) => ({ title: step.title, key: step.key })),
    [steps],
  );

  const [Step, { next, previous, is }] = useWizard({
    initialKey: ReceivingStepKey.RECEIVING,
    steps,
  });

  // Pending edits are saved before the step transition. When the flush fails (some rows
  // could not be saved even after a retry), we stay on the step
  const onNext = useCallback(async () => {
    const canProceed = await validateBeforeNextRef.current?.();
    if (!canProceed) {
      return;
    }
    dispatch(showSpinner());
    try {
      await flushRef.current?.();
    } catch {
      return;
    } finally {
      dispatch(hideSpinner());
    }
    next();
  }, [next]);

  const onCompleteReceipt = useCallback(async () => {
    await completeReceiptRef.current?.();
  }, []);

  return {
    Step,
    stepsTitles,
    flushRef,
    validateBeforeNextRef,
    setNextDisabled,
    isNextDisabled,
    completeReceiptRef,
    isCheckStep: is(ReceivingStepKey.CHECK),
    previous,
    onNext,
    onCompleteReceipt,
  };
};

export default useReceivingSteps;
