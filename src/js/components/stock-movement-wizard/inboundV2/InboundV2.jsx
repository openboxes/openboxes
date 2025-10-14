import React, { useMemo } from 'react';

import { useSelector } from 'react-redux';

import InboundHeader from 'components/stock-movement-wizard/inboundV2/InboundHeader';
import InboundV2AddItems from 'components/stock-movement-wizard/inboundV2/sections/InboundV2AddItems';
import InboundV2Create from 'components/stock-movement-wizard/inboundV2/sections/InboundV2Create';
import InboundV2Send from 'components/stock-movement-wizard/inboundV2/sections/InboundV2Send';
import WizardStepsV2 from 'components/wizard/v2/WizardStepsV2';
import inboundV2Step from 'consts/InboundV2Step';
import useTranslate from 'hooks/useTranslate';
import useTranslation from 'hooks/useTranslation';
import useWizard from 'hooks/useWizard';
import PageWrapper from 'wrappers/PageWrapper';

import 'utils/utils.scss';
import 'components/stock-movement-wizard/inboundV2/inboundV2.scss';
import 'react-confirm-alert/src/react-confirm-alert.css';

const InboundV2 = () => {
  useTranslation('stockMovement');
  const translate = useTranslate();
  const { locale } = useSelector((state) => state.session.activeLanguage);

  const steps = useMemo(() => [
    {
      key: inboundV2Step.CREATE,
      title: translate('react.stockMovement.create.label', 'Create'),
      Component: (props) => (<InboundV2Create {...props} />),
    },
    {
      key: inboundV2Step.ADD_ITEMS,
      title: translate('react.stockMovement.addItems.label', 'Add Items'),
      Component: (props) => (<InboundV2AddItems {...props} />),
    },
    {
      key: inboundV2Step.SEND,
      title: translate('react.stockMovement.send.label', 'Send'),
      Component: (props) => (<InboundV2Send {...props} />),
    },
  ], [locale]);

  const stepsTitles = steps.map((step) => ({
    title: step.title,
    key: step.key,
  }));

  const [
    Step,
    {
      next,
      previous,
      is,
    },
  ] = useWizard({
    initialKey: inboundV2Step.CREATE,
    steps,
  });

  return (
    <PageWrapper className="inbound-wrapper">
      <WizardStepsV2 steps={stepsTitles} currentStepKey={Step.key} />
      <InboundHeader showHeaderStatus={is(inboundV2Step.SEND)} />

      {is(inboundV2Step.CREATE) && (<Step.Component next={next} />)}

      {is(inboundV2Step.ADD_ITEMS) && (<Step.Component previous={previous} next={next} />)}

      {is(inboundV2Step.SEND) && (<Step.Component previous={previous} />)}
    </PageWrapper>
  );
};

export default InboundV2;
