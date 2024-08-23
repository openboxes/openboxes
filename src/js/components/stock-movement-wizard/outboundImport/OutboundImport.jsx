import React, { useMemo } from 'react';

import { HttpStatusCode } from 'axios';

import OutboundImportHeader from 'components/stock-movement-wizard/outboundImport/OutboundImportHeader';
import OutboundImportStep from 'components/stock-movement-wizard/outboundImport/OutboundImportStep';
import OutboundImportConfirm from 'components/stock-movement-wizard/outboundImport/sections/OutboundImportConfirm';
import OutboundImportDetails from 'components/stock-movement-wizard/outboundImport/sections/OutboundImportDetails';
import WizardStepsV2 from 'components/wizard/v2/WizardStepsV2';
import useOutboundImportForm from 'hooks/outboundImport/useOutboundImportForm';
import useTranslate from 'hooks/useTranslate';
import useTranslation from 'hooks/useTranslation';
import useWizard from 'hooks/useWizard';
import PageWrapper from 'wrappers/PageWrapper';

import 'utils/utils.scss';

const OutboundImport = () => {
  useTranslation('outboundImport', 'stockMovement');

  const translate = useTranslate();

  const steps = useMemo(() => [
    {
      key: OutboundImportStep.DETAILS,
      title: translate('react.outboundImport.steps.details.label', 'Create'),
      Component: (props) => (<OutboundImportDetails {...props} />),
    },
    {
      key: OutboundImportStep.CONFIRM,
      title: translate('react.outboundImport.steps.confirm.label', 'Confirm'),
      Component: (props) => (<OutboundImportConfirm {...props} />),
    },
  ], [translate]);

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
  ] = useWizard({ initialKey: OutboundImportStep.DETAILS, steps });

  const {
    lineItems,
    lineItemErrors,
    validateStatus,
    errors,
    control,
    isValid,
    handleSubmit,
    onSubmitStockMovementDetails,
    onConfirmImport,
    trigger,
  } = useOutboundImportForm({ next });

  const detailsComponentProps = {
    control,
    errors,
    isValid,
    next,
    trigger,
  };

  return (
    <PageWrapper>
      <WizardStepsV2 steps={stepsTitles} currentStepKey={Step.key} />
      <OutboundImportHeader />
      <form onSubmit={handleSubmit(onSubmitStockMovementDetails)}>
        {is(OutboundImportStep.DETAILS.key) && (<Step.Component {...detailsComponentProps} />)}
      </form>
      <form onSubmit={handleSubmit(onConfirmImport)}>
        {is(OutboundImportStep.CONFIRM.key)
          && (
          <Step.Component
            {...detailsComponentProps}
            previous={previous}
            data={lineItems}
            tableErrors={lineItemErrors}
            hasErrors={validateStatus === HttpStatusCode.BadRequest}
          />
          )}
      </form>
    </PageWrapper>
  );
};

export default OutboundImport;
