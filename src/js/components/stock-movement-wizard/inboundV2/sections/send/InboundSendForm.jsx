import React from 'react';

import PropTypes from 'prop-types';
import { Controller } from 'react-hook-form';

import DateFieldDateFns from 'components/form-elements/v2/DateFieldDateFns';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import Section from 'components/Layout/v2/Section';
import InboundSendFormHeader from 'components/stock-movement-wizard/inboundV2/sections/send/InboundSendFormHeader';
import InboundSendFormNavigation from 'components/stock-movement-wizard/inboundV2/sections/send/InboundSendNavigationButtons';
import requisitionStatus from 'consts/requisitionStatus';
import { DateFormatDateFns } from 'consts/timeFormat';
import useInboundSendForm from 'hooks/inboundV2/send/useInboundSendForm';

const InboundSendForm = ({ previous }) => {
  const {
    form: {
      control,
      errors,
      isValid,
      handleSubmit,
      trigger,
    },
    data: {
      statusCode,
      shipped,
      documents,
      shipmentTypesWithoutDefaultValue,
      debouncedDestinationLocationsFetch,
    },
    actions: {
      sendShipment,
      rollbackStockMovement,
      onSave,
      previousPage,
      saveAndExit,
    },
    files: {
      files,
      handleExportFile,
      handleDownloadFiles,
      handleRemoveFile,
    },
    permissions: { hasRoleAdmin },
  } = useInboundSendForm({ previous });

  // Check if shipment is dispatched (used for disabling buttons and enabling rollback)
  const isDispatched = statusCode === requisitionStatus.DISPATCHED;

  // Rollback button is visible only for admins when shipment has been dispatched
  const rollbackButtonVisible = hasRoleAdmin && shipped;

  // Rollback is enabled only when form is valid and status is dispatched
  const isRollbackEnabled = isValid && isDispatched;

  return (
    <>
      <InboundSendFormHeader
        saveAndExit={saveAndExit}
        onSave={onSave}
        statusCode={statusCode}
        isValid={isValid}
        documents={documents}
        handleExportFile={handleExportFile}
        handleDownloadFiles={handleDownloadFiles}
        files={files}
        handleRemoveFile={handleRemoveFile}
        isDispatched={isDispatched}
      />
      <form onSubmit={handleSubmit(sendShipment)}>
        <Section showTitle={false}>
          <div className="row">
            <div className="col-lg-3 col-md-6 px-2 pt-2">
              <Controller
                name="origin"
                control={control}
                disabled
                render={({ field }) => (
                  <SelectField
                    {...field}
                    title={{
                      id: 'react.stockMovement.origin.label',
                      defaultMessage: 'Origin',
                    }}
                    errorMessage={errors.origin?.message}
                    customTooltip
                    ariaLabel={{
                      id: 'react.stockMovement.origin.label',
                      defaultMessage: 'Origin',
                    }}
                  />
                )}
              />
            </div>
            <div className="col-lg-3 col-md-6 px-2 pt-2">
              <Controller
                name="destination"
                control={control}
                render={({ field }) => (
                  <SelectField
                    {...field}
                    title={{
                      id: 'react.stockMovement.destination.label',
                      defaultMessage: 'Destination',
                    }}
                    errorMessage={errors.destination?.message}
                    customTooltip
                    ariaLabel={{
                      id: 'react.stockMovement.destination.label',
                      defaultMessage: 'Destination',
                    }}
                    async
                    loadOptions={debouncedDestinationLocationsFetch}
                    required
                  />
                )}
              />
            </div>
            <div className="col-lg-3 col-md-6 px-2 pt-2">
              <Controller
                name="shipDate"
                control={control}
                render={({ field }) => (
                  <DateFieldDateFns
                    {...field}
                    title={{
                      id: 'react.stockMovement.shipDate.label',
                      defaultMessage: 'Ship date',
                    }}
                    errorMessage={errors.shipDate?.message}
                    showTimeSelect
                    required
                    disabled={isDispatched}
                    customDateFormat={DateFormatDateFns.DD_MMM_YYYY}
                    customTooltip
                    showCustomInput={false}
                    ariaLabel={{
                      id: 'react.stockMovement.shipDate.label',
                      defaultMessage: 'Ship date',
                    }}
                    onChange={async (value) => {
                      field.onChange(value);
                      await trigger(['shipDate', 'expectedDeliveryDate']);
                    }}
                  />
                )}
              />
            </div>
            <div className="col-lg-3 col-md-6 px-2 pt-2">
              <Controller
                name="shipmentType"
                control={control}
                render={({ field }) => (
                  <SelectField
                    {...field}
                    title={{
                      id: 'react.stockMovement.shipmentType.label',
                      defaultMessage: 'Shipment type',
                    }}
                    required
                    errorMessage={errors.shipmentType?.message}
                    options={shipmentTypesWithoutDefaultValue.map((item) => ({
                      id: item.id,
                      name: item.name,
                      label: item.displayName,
                      value: item.id,
                    }))}
                    customTooltip
                    ariaLabel={{
                      id: 'react.stockMovement.shipmentType.label',
                      defaultMessage: 'Shipment type',
                    }}
                  />
                )}
              />
            </div>
            <div className="col-lg-3 col-md-6 px-2 pt-2">
              <Controller
                name="trackingNumber"
                control={control}
                render={({ field }) => (
                  <TextInput
                    {...field}
                    title={{
                      id: 'react.stockMovement.trackingNumber.label',
                      defaultMessage: 'Tracking Number',
                    }}
                    errorMessage={errors.trackingNumber?.message}
                    customTooltip
                    ariaLabel={{
                      id: 'react.stockMovement.trackingNumber.label',
                      defaultMessage: 'Tracking Number',
                    }}
                  />
                )}
              />
            </div>
            <div className="col-lg-3 col-md-6 px-2 pt-2">
              <Controller
                name="driverName"
                control={control}
                render={({ field }) => (
                  <TextInput
                    {...field}
                    title={{
                      id: 'react.stockMovement.driverName.label',
                      defaultMessage: 'Driver Name',
                    }}
                    errorMessage={errors.driverName?.message}
                    customTooltip
                    ariaLabel={{
                      id: 'react.stockMovement.driverName.label',
                      defaultMessage: 'Driver Name',
                    }}
                  />
                )}
              />
            </div>
            <div className="col-lg-3 col-md-6 px-2 pt-2">
              <Controller
                name="comments"
                control={control}
                render={({ field }) => (
                  <TextInput
                    {...field}
                    title={{
                      id: 'react.stockMovement.comments.label',
                      defaultMessage: 'Comments',
                    }}
                    errorMessage={errors.comments?.message}
                    customTooltip
                    ariaLabel={{
                      id: 'react.stockMovement.comments.label',
                      defaultMessage: 'Comments',
                    }}
                  />
                )}
              />
            </div>
            <div className="col-lg-3 col-md-6 px-2 pt-2">
              <Controller
                name="expectedDeliveryDate"
                control={control}
                render={({ field }) => (
                  <DateFieldDateFns
                    {...field}
                    title={{
                      id: 'react.stockMovement.expectedDeliveryDate.label',
                      defaultMessage: 'Expected Delivery Date',
                    }}
                    errorMessage={errors.expectedDeliveryDate?.message}
                    required
                    customDateFormat={DateFormatDateFns.DD_MMM_YYYY}
                    customTooltip
                    showCustomInput={false}
                    ariaLabel={{
                      id: 'react.stockMovement.expectedDeliveryDate.label',
                      defaultMessage: 'Expected Delivery Date',
                    }}
                    onChange={async (value) => {
                      field.onChange(value);
                      await trigger(['shipDate', 'expectedDeliveryDate']);
                    }}
                  />
                )}
              />
            </div>
          </div>
        </Section>
        <InboundSendFormNavigation
          onPrevious={() => previousPage()}
          onRollback={() => rollbackStockMovement()}
          isDispatched={isDispatched}
          isRollbackEnabled={isRollbackEnabled}
          rollbackButtonVisible={rollbackButtonVisible}
        />
      </form>
    </>
  );
};

export default InboundSendForm;

InboundSendForm.propTypes = {
  previous: PropTypes.func.isRequired,
};
