import React, { useMemo } from 'react';

import CheckStep from 'components/receivingV2/CheckStep';
import ConfirmReceiptHeader from 'components/receivingV2/ConfirmReceiptHeader';
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
      topSection={is(ReceivingStepKey.CHECK) ? <ConfirmReceiptHeader /> : undefined}
      buttons={is(ReceivingStepKey.CHECK)
        ? {
          previous: {
            onClick: previous,
            label: 'react.receiving.backToReceive.label',
            defaultLabel: 'Back to Receive',
            variant: 'primary-outline',
          },
          next: {
            // Completing the receipt is out of scope of OBPIH-7900 (UI only)
            onClick: () => {},
            label: 'react.receiving.completeReceipt.label',
            defaultLabel: 'Complete Receipt',
          },
        }
        : { next: { onClick: next } }}
    >
      <Step.Component />
    </WizardPageLayout>
  );
};

export default Receiving;
