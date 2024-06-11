import React, { useMemo } from 'react';

import fileDownload from 'js-file-download';
import { useDispatch } from 'react-redux';

import { hideSpinner, showSpinner } from 'actions';
import stockMovementApi from 'api/services/StockMovementApi';
import Button from 'components/form-elements/Button';
import OutboundImportConfirm from 'components/stock-movement-wizard/outboundImport/OutboundImportConfirm';
import OutboundImportHeader from 'components/stock-movement-wizard/outboundImport/OutboundImportHeader';
import OutboundImportDetails from 'components/stock-movement-wizard/outboundImport/sections/OutboundImportDetails';
import WizardStepsV2 from 'components/wizard/v2/WizardStepsV2';
import useOutboundImportForm from 'hooks/outboundImport/useOutboundImportForm';
import useTranslate from 'hooks/useTranslate';
import useTranslation from 'hooks/useTranslation';
import useWizard from 'hooks/useWizard';
import PageWrapper from 'wrappers/PageWrapper';
import FileFormat from 'consts/fileFormat';

import 'utils/utils.scss';

const OutboundImport = () => {
  const dispatch = useDispatch();
  useTranslation('outboundImport');
  const translate = useTranslate();

  const OutboundImportStep = {
    DETAILS: {
      key: 'DETAILS',
      title: translate('react.outboundImport.steps.details.label', 'Create'),
    },
    CONFIRM: {
      key: 'CONFIRM',
      title: translate('react.outboundImport.steps.confirm.label', 'Confirm'),
    },
  };

  const steps = useMemo(() => [
    {
      key: OutboundImportStep.DETAILS.key,
      title: OutboundImportStep.DETAILS.title,
      Component: (props) => (<OutboundImportDetails {...props} />),
    },
    {
      key: OutboundImportStep.CONFIRM.key,
      title: OutboundImportStep.CONFIRM.title,
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
  ] = useWizard({ initialKey: OutboundImportStep.DETAILS.key, steps });

  const {
    errors,
    control,
    isValid,
    handleSubmit,
    onSubmit,
  } = useOutboundImportForm({ next });

  const detailsComponentProps = {
    control,
    errors,
    isValid,
    next,
  };

  const downloadPackingListTemplate = async () => {
    try {
      dispatch(showSpinner());
      await exportFileFromAPI({
        url: PACKING_LIST_TEMPLATE,
        format: FileFormat.XLS,
        filename: 'Completed Packing List',
      });
    } finally {
      dispatch(hideSpinner());
    }
  };

  return (
    <PageWrapper>
      <WizardStepsV2 steps={stepsTitles} currentStepKey={Step.key} />
      <OutboundImportHeader />
      <form onSubmit={handleSubmit(onSubmit)}>
        {is(OutboundImportStep.DETAILS.key) && (<Step.Component {...detailsComponentProps} />)}
        {is(OutboundImportStep.CONFIRM.key) && (<Step.Component previous={previous} />)}
      </form>
      <Button
        defaultLabel="Export Template"
        label="react.default.button.exportTemplate.label"
        onClick={downloadPackingListTemplate}
      />
    </PageWrapper>
  );
};

export default OutboundImport;
