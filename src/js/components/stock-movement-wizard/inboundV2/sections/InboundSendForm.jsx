import React from 'react';

import PropTypes from 'prop-types';
import { Controller } from 'react-hook-form';
import { RiArrowGoBackFill } from 'react-icons/ri';

import Button from 'components/form-elements/Button';
import DateField from 'components/form-elements/v2/DateField';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import Section from 'components/Layout/v2/Section';
import InboundSendFormHeader
  from 'components/stock-movement-wizard/inboundV2/sections/InboundSendFormHeader';
import requisitionStatus from 'consts/requisitionStatus';
import { DateFormat } from 'consts/timeFormat';
import useInboundSendForm from 'hooks/inboundV2/send/useInboundSendForm';

const InboundSendForm = ({ previous }) => {
  const {
    handleSubmit,
    control,
    errors,
    trigger,
    sendShipment,
    shipmentTypesWithoutDefaultValue,
    rollbackStockMovement,
    onSave,
    previousPage,
    saveAndExit,
    statusCode,
    hasRoleAdmin,
    shipped,
    matchesDestination,
    documents,
    handleExportFile,
    handleDownloadFiles,
    files,
    handleRemoveFile,
    isValid,
  } = useInboundSendForm({ previous });

  // Rollback button is visible only for admins when shipment has been dispatched
  const rollbackButtonVisible = hasRoleAdmin && shipped;

  // button is disabled when there are form errors
  // or when the shipment status is not dispatched
  // or selected destination doesn't match current location
  const shouldDisableRollbackButton =
    !isValid || statusCode !== requisitionStatus.DISPATCHED || !matchesDestination;

  // Disable navigation buttons if shipment is already dispatched
  // or selected destination doesn't match current location
  const navigationButtonDisabled =
    statusCode === requisitionStatus.DISPATCHED || !matchesDestination;

  return (
    <>
      <InboundSendFormHeader
        saveAndExit={saveAndExit}
        onSave={onSave}
        statusCode={statusCode}
        isValid={isValid}
        matchesDestination={matchesDestination}
        documents={documents}
        handleExportFile={handleExportFile}
        handleDownloadFiles={handleDownloadFiles}
        files={files}
        handleRemoveFile={handleRemoveFile}
      />
      <form onSubmit={handleSubmit(sendShipment)}>
        <Section title="Send Shipment">
          <div className="row">
            <div className="col-lg-3 col-md-6 px-2 pt-2">
              <Controller
                name="origin"
                control={control}
                disabled
                render={({ field }) => (
                  <SelectField
                    title={{
                      id: 'react.stockMovement.origin.label',
                      defaultMessage: 'Origin',
                    }}
                    errorMessage={errors.origin?.message}
                    {...field}
                  />
                )}
              />
            </div>
            <div className="col-lg-3 col-md-6 px-2 pt-2">
              <Controller
                name="destination"
                control={control}
                disabled
                render={({ field }) => (
                  <SelectField
                    title={{
                      id: 'react.stockMovement.destination.label',
                      defaultMessage: 'Destination',
                    }}
                    errorMessage={errors.destination?.message}
                    {...field}
                  />
                )}
              />
            </div>
            <div className="col-lg-3 col-md-6 px-2 pt-2">
              <Controller
                name="shipDate"
                control={control}
                disabled={!matchesDestination}
                render={({ field }) => (
                  <DateField
                    title={{
                      id: 'react.stockMovement.shipDate.label',
                      defaultMessage: 'Ship date',
                    }}
                    errorMessage={errors.shipDate?.message}
                    required
                    onChangeRaw={async (date) => {
                      field.onChange(date.format());
                      await trigger();
                    }}
                    customDateFormat={DateFormat.DD_MMM_YYYY}
                    {...field}
                  />
                )}
              />
            </div>
            <div className="col-lg-3 col-md-6 px-2 pt-2">
              <Controller
                name="shipmentType"
                control={control}
                disabled={!matchesDestination}
                render={({ field }) => (
                  <SelectField
                    title={{
                      id: 'react.stockMovement.shipmentType.label',
                      defaultMessage: 'ShipmentType',
                    }}
                    required
                    errorMessage={errors.shipmentType?.message}
                    options={shipmentTypesWithoutDefaultValue.map((item) => ({
                      id: item.id,
                      name: item.name,
                      label: item.displayName,
                      value: item.id,
                    }))}
                    {...field}
                  />
                )}
              />
            </div>
            <div className="col-lg-3 col-md-6 px-2 pt-2">
              <Controller
                name="trackingNumber"
                control={control}
                disabled={!matchesDestination}
                render={({ field }) => (
                  <TextInput
                    title={{
                      id: 'react.stockMovement.trackingNumber.label',
                      defaultMessage: 'Tracking Number',
                    }}
                    errorMessage={errors.trackingNumber?.message}
                    {...field}
                  />
                )}
              />
            </div>
            <div className="col-lg-3 col-md-6 px-2 pt-2">
              <Controller
                name="driverName"
                control={control}
                disabled={!matchesDestination}
                render={({ field }) => (
                  <TextInput
                    title={{
                      id: 'react.stockMovement.driverName.label',
                      defaultMessage: 'Driver Name',
                    }}
                    errorMessage={errors.driverName?.message}
                    {...field}
                  />
                )}
              />
            </div>
            <div className="col-lg-3 col-md-6 px-2 pt-2">
              <Controller
                name="comments"
                control={control}
                disabled={!matchesDestination}
                render={({ field }) => (
                  <TextInput
                    title={{
                      id: 'react.stockMovement.comments.label',
                      defaultMessage: 'Comments',
                    }}
                    errorMessage={errors.comments?.message}
                    {...field}
                  />
                )}
              />
            </div>
            <div className="col-lg-3 col-md-6 px-2 pt-2">
              <Controller
                name="expectedDeliveryDate"
                control={control}
                disabled={!matchesDestination}
                render={({ field }) => (
                  <DateField
                    title={{
                      id: 'react.stockMovement.expectedDeliveryDate.label',
                      defaultMessage: 'Expected Delivery Date',
                    }}
                    errorMessage={errors.expectedDeliveryDate?.message}
                    required
                    onChangeRaw={async (date) => {
                      field.onChange(date.format());
                      await trigger();
                    }}
                    customDateFormat={DateFormat.DD_MMM_YYYY}
                    {...field}
                  />
                )}
              />
            </div>
          </div>
        </Section>
        <div className="submit-buttons">
          <Button
            label="react.default.button.previous.label"
            defaultLabel="Previous"
            variant="primary"
            onClick={() => previousPage()}
            disabled={navigationButtonDisabled}
          />
          <div className="buttons-container">
            {rollbackButtonVisible && (
              <Button
                label="react.default.button.rollback.label"
                defaultLabel="Rollback"
                variant="primary-outline"
                onClick={() => rollbackStockMovement()}
                StartIcon={<RiArrowGoBackFill className="icon" />}
                disabled={shouldDisableRollbackButton}
              />
            )}
            <Button
              label="react.shipping.sendShipment.label"
              defaultLabel="Send shipment"
              variant="primary"
              type="submit"
              disabled={navigationButtonDisabled}
            />
          </div>
        </div>
      </form>
    </>
  );
};

export default InboundSendForm;

InboundSendForm.propTypes = {
  previous: PropTypes.func.isRequired,
};
