import { useCallback, useMemo, useRef } from 'react';

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
 *   completeReceiptRef: Object,
 *   isCheckStep: boolean,
 *   previous: Function,
 *   onNext: Function,
 *   onCompleteReceipt: Function,
 * }} `Step` - the current step ({ key, Component }).
 *   `stepsTitles` - step titles for the wizard header.
 *   `flushRef` - filled by the receiving step with its autosave flush.
 *   `completeReceiptRef` - filled by the check step with its complete receipt handler.
 *   `isCheckStep` - true on the check (last) step.
 *   `previous` - goes back to the receiving step.
 *   `onNext` - saves pending edits, then goes to the next step.
 *   `onCompleteReceipt` - runs the handler registered by the check step.
 */
const useReceivingSteps = () => {
  const translate = useTranslate();
  const dispatch = useDispatch();
  const flushRef = useRef(null);
  const completeReceiptRef = useRef(null);

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
    dispatch(showSpinner());
    try {
      await flushRef.current?.();
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
    completeReceiptRef,
    isCheckStep: is(ReceivingStepKey.CHECK),
    previous,
    onNext,
    onCompleteReceipt,
  };
};

export default useReceivingSteps;
