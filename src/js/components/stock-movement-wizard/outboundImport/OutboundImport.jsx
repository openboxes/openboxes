import React, { useMemo } from 'react';

import OutboundImportConfirm from 'components/stock-movement-wizard/outboundImport/OutboundImportConfirm';
import OutboundImportDetails from 'components/stock-movement-wizard/outboundImport/OutboundImportDetails';
import WizardStepsV2 from 'components/wizard/v2/WizardStepsV2';
import useWizard from 'hooks/useWizard';

const OutboundImport = () => {
  const OutboundImportStep = {
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
      key: OutboundImportStep.DETAILS.key,
      title: OutboundImportStep.DETAILS.title,
      Component: () => (<OutboundImportDetails />),
    },
    {
      key: OutboundImportStep.CONFIRM.key,
      title: OutboundImportStep.CONFIRM.title,
      Component: () => (<OutboundImportConfirm />),
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
  ] = useWizard({ initialKey: OutboundImportStep.DETAILS.key, steps });

  return (
    <div>
      <WizardStepsV2 steps={stepsTitles} currentStepKey={Step.key} />
      <Step.Component />
      <button type="button" onClick={() => previous()}>previous</button>
      <button type="button" onClick={() => next()}>next</button>
    </div>
  );
};

export default OutboundImport;
