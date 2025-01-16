import React, { useMemo } from 'react';

import Button from 'components/form-elements/Button';
import InboundHeader from 'components/stock-movement-wizard/inboundV2/InboundHeader';
import InboundV2AddItems
  from 'components/stock-movement-wizard/inboundV2/sections/InboundV2AddItems';
import InboundV2Create from 'components/stock-movement-wizard/inboundV2/sections/InboundV2Create';
import InboundV2Send from 'components/stock-movement-wizard/inboundV2/sections/InboundV2Send';
import WizardStepsV2 from 'components/wizard/v2/WizardStepsV2';
import inboundV2Step from 'consts/InboundV2Step';
import mockInboundV2AdditionalTitle from 'consts/MockInboundV2AdditionalTitle';
// eslint-disable-next-line import/no-named-as-default
import mockInboundV2Title from 'consts/MockInboundV2Title';
import useTranslate from 'hooks/useTranslate';
import useTranslation from 'hooks/useTranslation';
// eslint-disable-next-line import/no-extraneous-dependencies
import useWizard from 'hooks/useWizard';
import PageWrapper from 'wrappers/PageWrapper';

const InboundV2 = () => {
  useTranslation('stockMovement');
  const translate = useTranslate();

  const steps = useMemo(() => [
    {
      key: inboundV2Step.CREATE,
      title: translate('react.stockMovement.create.label', 'Create'),
      Component: () => (<InboundV2Create />),
    },
    {
      key: inboundV2Step.ADD_ITEMS,
      title: translate('react.stockMovement.addItems.label', 'Add Items'),
      Component: () => (<InboundV2AddItems />),
    },
    {
      key: inboundV2Step.SEND,
      title: translate('react.stockMovement.send.label', 'Send'),
      Component: () => (<InboundV2Send />),
    },
  ], [translate]);

  const stepsTitles = steps.map((step) => ({
    title: step.title,
    key: step.key,
  }));

  const [
    Step,
    {
      // navigateToStep,
      next,
      previous,
      is,
    },
  ] = useWizard({
    initialKey: InboundV2.DETAILS,
    steps,
  });

  // I need to check this
  const headerAdditionalTitle = is(inboundV2Step.SEND) ? mockInboundV2AdditionalTitle : undefined;
  // eslint-disable-next-line max-len
  const headerTitle = is(inboundV2Step.ADD_ITEMS) || is(inboundV2Step.SEND) ? mockInboundV2Title : undefined;

  return (
    <PageWrapper>
      <WizardStepsV2 steps={stepsTitles} currentStepKey={Step.key} />
      <InboundHeader
        title={headerTitle}
        additionalTitle={headerAdditionalTitle}
      />
      {/* <form> */}
      {is(inboundV2Step.CREATE) && (<Step.Component />)}
      {is(inboundV2Step.ADD_ITEMS) && (<Step.Component />)}
      {is(inboundV2Step.SEND) && (<Step.Component />)}
      {/* eslint-disable-next-line react/button-has-type */}
      <div className="d-flex gap-8 justify-content-between">
        <Button
          label="react.outboundImport.form.previous.label"
          defaultLabel="previous"
          variant="primary"
          className="fit-content align-self-end"
          onClick={previous}
        />
        <Button
          label="react.outboundImport.form.next.label"
          defaultLabel="Next"
          variant="primary"
          className="fit-content align-self-end"
          onClick={next}
        />
      </div>
      {/* <button onClick={() => previous()}>Previous</button> */}
      {/* /!* eslint-disable-next-line react/button-has-type *!/ */}
      {/* <button onClick={() => next()}>Next</button> */}
      {/* </form> */}
    </PageWrapper>
  );
};

export default InboundV2;
