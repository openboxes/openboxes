import React, { useMemo } from 'react';

import FullOutboundImportConfirm from 'components/stock-movement-wizard/fullOutbound/FullOutboundImportConfirm';
import FullOutboundImportDetails from 'components/stock-movement-wizard/fullOutbound/FullOutboundImportDetails';
import WizardStepsV2 from 'components/wizard/v2/WizardStepsV2';
import useWizard from 'hooks/useWizard';

const FullOutboundImport = () => {
  const FullOutboundImportStep = {
    DETAILS: {
      key: 'DETAILS',
      // TODO: Make titles translatable in OBPIH-6329
      title: 'Create',
    },
    CONFIRM: {
      key: 'CONFIRM',
      title: 'Confirm',
    },
  };
  const steps = useMemo(() => [
    {
      key: FullOutboundImportStep.DETAILS.key,
      title: FullOutboundImportStep.DETAILS.title,
      Component: () => (<FullOutboundImportDetails />),
    },
    {
      key: FullOutboundImportStep.CONFIRM.key,
      title: FullOutboundImportStep.CONFIRM.title,
      Component: () => (<FullOutboundImportConfirm />),
    },
  ], []);

  const stepsTitles = steps.map((step) => ({
    title: step.title,
    key: step.key,
  }));

  const [
    Step,
    {
      next,
      previous,
    },
  ] = useWizard({ initialKey: FullOutboundImportStep.DETAILS.key, steps });

  return (
    <div>
      <WizardStepsV2 steps={stepsTitles} currentStepKey={Step.key} />
      <Step.Component />
      <button type="button" onClick={() => previous()}>previous</button>
      <button type="button" onClick={() => next()}>next</button>
    </div>
  );
};

export default FullOutboundImport;
