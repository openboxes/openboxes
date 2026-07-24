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
 *   isCheckStep: boolean,
 *   previous: Function,
 *   onNext: Function,
 * }} `Step` - the current step ({ key, Component }).
 *   `stepsTitles` - step titles for the wizard header.
 *   `flushRef` - filled by the receiving step with its autosave flush.
 *   `isCheckStep` - true on the check (last) step.
 *   `previous` - goes back to the receiving step.
 *   `onNext` - saves pending edits, then goes to the next step.
 */
const useReceivingSteps = () => {
  const translate = useTranslate();
  const dispatch = useDispatch();
  const flushRef = useRef(null);

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

  return {
    Step,
    stepsTitles,
    flushRef,
    isCheckStep: is(ReceivingStepKey.CHECK),
    previous,
    onNext,
  };
};

export default useReceivingSteps;
