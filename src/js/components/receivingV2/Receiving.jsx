import React from 'react';

import ConfirmReceiptHeader from 'components/receivingV2/ConfirmReceiptHeader';
import WizardPageLayout from 'components/wizard/v2/WizardPageLayout';
import useReceivingHeader from 'hooks/receiving/v2/useReceivingHeader';
import useReceivingSteps from 'hooks/receiving/v2/useReceivingSteps';
import useTranslate from 'hooks/useTranslate';
import useTranslation from 'hooks/useTranslation';

const Receiving = () => {
  useTranslation('receiving');
  const translate = useTranslate();
  const { info } = useReceivingHeader();
  const {
    Step, stepsTitles, flushRef, isCheckStep, previous, onNext,
  } = useReceivingSteps();

  const title = {
    label: translate('react.receiving.receiving.label', 'Receiving'),
    info,
  };

  return (
    <WizardPageLayout
      title={title}
      wizard={{ steps: stepsTitles, currentStepKey: Step.key }}
      topSection={isCheckStep ? <ConfirmReceiptHeader onBackToReceive={previous} /> : undefined}
      buttons={isCheckStep
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
        : { next: { onClick: onNext } }}
    >
      <Step.Component flushRef={flushRef} />
    </WizardPageLayout>
  );
};

export default Receiving;
