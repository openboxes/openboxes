import React, { useMemo } from 'react';

import InboundHeader from 'components/stock-movement-wizard/inboundV2/InboundHeader';
import InboundAddItems from 'components/stock-movement-wizard/inboundV2/sections/addItems/InboundAddItems';
import InboundCreate from 'components/stock-movement-wizard/inboundV2/sections/create/InboundCreate';
import InboundSend from 'components/stock-movement-wizard/inboundV2/sections/send/InboundSend';
import WizardStepsV2 from 'components/wizard/v2/WizardStepsV2';
import inboundStep from 'consts/InboundStep';
import useTranslate from 'hooks/useTranslate';
import useTranslation from 'hooks/useTranslation';
import useWizard from 'hooks/useWizard';
import PageWrapper from 'wrappers/PageWrapper';

import 'utils/utils.scss';
import 'components/stock-movement-wizard/inboundV2/inbound.scss';
import 'react-confirm-alert/src/react-confirm-alert.css';

const Inbound = () => {
  useTranslation('stockMovement');
  const translate = useTranslate();

  const steps = useMemo(() => [
    {
      key: inboundStep.CREATE,
      title: translate('react.stockMovement.create.label', 'Create'),
      Component: InboundCreate,
    },
    {
      key: inboundStep.ADD_ITEMS,
      title: translate('react.stockMovement.addItems.label', 'Add Items'),
      Component: InboundAddItems,
    },
    {
      key: inboundStep.SEND_SHIPMENT,
      title: translate('react.stockMovement.send.label', 'Send'),
      Component: InboundSend,
    },
  ], [translate]);

  const stepsTitles = useMemo(
    () => steps.map((step) => ({ title: step.title, key: step.key })),
    [steps],
  );

  const [
    Step,
    {
      next,
      previous,
      is,
    },
  ] = useWizard({
    initialKey: inboundStep.CREATE,
    steps,
  });

  return (
    <PageWrapper>
      <WizardStepsV2 steps={stepsTitles} currentStepKey={Step.key} />
      <InboundHeader showHeaderStatus={is(inboundStep.SEND_SHIPMENT)} />
      {is(inboundStep.CREATE) && (<Step.Component next={next} />)}
      {is(inboundStep.ADD_ITEMS) && (<Step.Component previous={previous} next={next} />)}
      {is(inboundStep.SEND_SHIPMENT) && (<Step.Component previous={previous} />)}
    </PageWrapper>
  );
};

export default Inbound;
