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
    Step,
    stepsTitles,
    flushRef,
    validateBeforeNextRef,
    setNextDisabled,
    isNextDisabled,
    completeReceiptRef,
    isCheckStep,
    previous,
    onNext,
    onCompleteReceipt,
  } = useReceivingSteps();

  const title = {
    label: translate('react.receiving.receiving.label', 'Receiving'),
    info,
  };

  return (
    <WizardPageLayout
      title={title}
      wizard={{ steps: stepsTitles, currentStepKey: Step.key }}
      topSection={isCheckStep
        ? (
          <ConfirmReceiptHeader
            onBackToReceive={previous}
            onCompleteReceipt={onCompleteReceipt}
          />
        )
        : undefined}
      buttons={isCheckStep
        ? {
          previous: {
            onClick: previous,
            label: 'react.receiving.backToReceive.label',
            defaultLabel: 'Back to Receive',
            tooltipLabel: 'react.receiving.backToReceive.tooltip.label',
            defaultTooltipLabel: 'Edit previously-entered receiving information',
            variant: 'primary-outline',
          },
          next: {
            onClick: onCompleteReceipt,
            label: 'react.receiving.completeReceipt.label',
            defaultLabel: 'Complete Receipt',
            tooltipLabel: 'react.receiving.completeReceipt.tooltip.label',
            defaultTooltipLabel: 'Submit the receipt',
          },
        }

        : {
          next: {
            onClick: onNext,
            disabled: isNextDisabled,
            tooltipLabel: 'react.receiving.moveToCheck.tooltip.label',
            defaultTooltipLabel: 'Proceed to the review stage',
          },
        }}
    >
      <Step.Component
        flushRef={flushRef}
        validateBeforeNextRef={validateBeforeNextRef}
        setNextDisabled={setNextDisabled}
        completeReceiptRef={completeReceiptRef}
      />
    </WizardPageLayout>
  );
};

export default Receiving;
