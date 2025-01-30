import React, { useMemo } from 'react';

import { useSelector } from 'react-redux';

import Button from 'components/form-elements/Button';
import InboundHeader from 'components/stock-movement-wizard/inboundV2/InboundHeader';
import InboundV2AddItems
  from 'components/stock-movement-wizard/inboundV2/sections/InboundV2AddItems';
import InboundV2Create from 'components/stock-movement-wizard/inboundV2/sections/InboundV2Create';
import InboundV2Send from 'components/stock-movement-wizard/inboundV2/sections/InboundV2Send';
import WizardStepsV2 from 'components/wizard/v2/WizardStepsV2';
import inboundV2Step from 'consts/InboundV2Step';
import mockInboundV2Status from 'consts/MockInboundV2Status';
import mockInboundV2Title from 'consts/MockInboundV2Title';
import useInboundForm from 'hooks/inboundV2/useInboundForm';
import useTranslate from 'hooks/useTranslate';
import useTranslation from 'hooks/useTranslation';
import useWizard from 'hooks/useWizard';
import PageWrapper from 'wrappers/PageWrapper';

import 'utils/utils.scss';

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
      Component: () => (<InboundV2AddItems />),
    },
    {
      key: inboundV2Step.SEND,
      title: translate('react.stockMovement.send.label', 'Send'),
      Component: () => (<InboundV2Send />),
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
    initialKey: InboundV2.DETAILS,
    steps,
  });

  const {
    errors,
    control,
    isValid,
    trigger,
    handleSubmit,
    onSubmitStockMovementDetails,
    stockLists,
  } = useInboundForm({ next });

  const detailsComponentProps = {
    control,
    errors,
    isValid,
    next,
    trigger,
    stockLists,
  };

  // this will still need to be improved in the future
  const headerStatus = is(inboundV2Step.SEND) ? mockInboundV2Status : undefined;
  const headerTitle = is(inboundV2Step.ADD_ITEMS)
  || is(inboundV2Step.SEND) ? mockInboundV2Title : undefined;

  return (
    <PageWrapper>
      <WizardStepsV2 steps={stepsTitles} currentStepKey={Step.key} />
      <InboundHeader
        title={headerTitle}
        status={headerStatus}
      />
      <form onSubmit={handleSubmit(onSubmitStockMovementDetails)}>
        {is(inboundV2Step.CREATE) && (<Step.Component {...detailsComponentProps} />)}
      </form>
      {is(inboundV2Step.ADD_ITEMS) && (<Step.Component />)}
      {is(inboundV2Step.SEND) && (<Step.Component />)}
      {/* this is for testing purposes */}
      <Button
        label="react.default.button.previous.label"
        defaultLabel="Previous"
        variant="primary"
        className="fit-content align-self-end"
        onClick={() => previous()}
      />
    </PageWrapper>
  );
};

export default InboundV2;
