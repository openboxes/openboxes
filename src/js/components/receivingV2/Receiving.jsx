import React, { useMemo } from 'react';

import CheckStep from 'components/receivingV2/CheckStep';
import ReceivingStep from 'components/receivingV2/ReceivingStep';
import WizardPageLayout from 'components/wizard/v2/WizardPageLayout';
import useReceivingHeader from 'hooks/receiving/v2/useReceivingHeader';
import useTranslate from 'hooks/useTranslate';
import useTranslation from 'hooks/useTranslation';
import useWizard from 'hooks/useWizard';

const ReceivingStepKey = {
  RECEIVING: 'RECEIVING',
  CHECK: 'CHECK',
};

const Receiving = () => {
  useTranslation('receiving');
  const translate = useTranslate();
  const { info } = useReceivingHeader();

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

  const title = {
    label: translate('react.receiving.receiving.label', 'Receiving'),
    info,
  };

  return (
    <WizardPageLayout
      title={title}
      wizard={{ steps: stepsTitles, currentStepKey: Step.key }}
      buttons={{
        onPrevious: is(ReceivingStepKey.RECEIVING) ? undefined : previous,
        onNext: is(ReceivingStepKey.CHECK) ? undefined : next,
      }}
    >
      <Step.Component />
    </WizardPageLayout>
  );
};

export default Receiving;
