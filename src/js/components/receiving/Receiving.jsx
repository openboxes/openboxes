import React, { useMemo } from 'react';

import CheckStep from 'components/receiving/CheckStep';
import ReceivingStep from 'components/receiving/ReceivingStep';
import WizardPageLayout from 'components/wizard/v2/WizardPageLayout';
import useTranslate from 'hooks/useTranslate';
import useTranslation from 'hooks/useTranslation';
import useWizard from 'hooks/useWizard';

const ReceivingStepKey = {
  RECEIVING: 'RECEIVING',
  CHECK: 'CHECK',
};

// TODO: replace the hardcoded header info with the real shipment data
const HEADER_INFO = [
  { text: '158BFR', color: '#000000', delimeter: ' - ' },
  { text: 'Modise Transport T/A Yelloow Line Car Wash', color: '#004d40', delimeter: ' to ' },
  { text: 'Belladere Depot', color: '#01579b', delimeter: ', ' },
  { text: '01/31/2026', color: '#4a148c', delimeter: ', ' },
  { text: 'fdffdf', color: '#770838', delimeter: '' },
];

const Receiving = () => {
  useTranslation('partialReceiving');
  const translate = useTranslate();

  const steps = useMemo(() => [
    {
      key: ReceivingStepKey.RECEIVING,
      title: translate('react.partialReceiving.receiving.label', 'Receiving'),
      Component: ReceivingStep,
    },
    {
      key: ReceivingStepKey.CHECK,
      title: translate('react.partialReceiving.check.label', 'Check'),
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
    label: translate('react.partialReceiving.receiving.label', 'Receiving'),
    info: HEADER_INFO,
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
