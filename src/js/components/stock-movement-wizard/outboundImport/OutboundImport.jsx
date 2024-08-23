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
    getValues,
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

  /**
   * Skips validation to allow form submission after a page refresh,
   * where the required XLS file is lost. The file was already validated earlier,
   * so we bypass validation and submit the form directly to retain progress.
   *
   * Related ticket OBPIH-6627 (Keep filled form progress when refreshing the page)
   */
  const handleConfirmSubmitForm = (submitMethod) => (event) => {
    event.preventDefault();
    submitMethod(getValues());
  };

  return (
    <PageWrapper>
      <WizardStepsV2 steps={stepsTitles} currentStepKey={Step.key} />
      <OutboundImportHeader />
      <form onSubmit={handleSubmit(onSubmitStockMovementDetails)}>
        {is(OutboundImportStep.DETAILS) && (<Step.Component {...detailsComponentProps} />)}
      </form>
      <form onSubmit={handleConfirmSubmitForm(onConfirmImport)}>
        {is(OutboundImportStep.CONFIRM)
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
