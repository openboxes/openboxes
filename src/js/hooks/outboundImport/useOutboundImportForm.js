import { useEffect } from 'react';

import { zodResolver } from '@hookform/resolvers/zod';
import moment from 'moment/moment';
import { useForm } from 'react-hook-form';
import { useSelector } from 'react-redux';

import { DateFormat } from 'consts/timeFormat';
import useOutboundImportValidation from 'hooks/outboundImport/useOutboundImportValidation';

const useOutboundImportForm = ({ next }) => {
  const { validationSchema } = useOutboundImportValidation();
  const { currentLocation } = useSelector((state) => ({
    currentLocation: state.session.currentLocation,
  }));

  const getDefaultValues = () => ({
    description: undefined,
    origin: {
      id: currentLocation?.id,
      label: `${currentLocation?.name} [${currentLocation?.locationType?.description}]`,
    },
    destination: undefined,
    requestedBy: undefined,
    dateRequested: undefined,
    dateShipped: moment(new Date()).format(DateFormat.MMM_DD_YYYY_HH_MM_SS),
    shipmentType: undefined,
    trackingNumber: undefined,
    comments: undefined,
    expectedDeliveryDate: undefined,
    packingList: undefined,
  });

  const {
    control,
    getValues,
    handleSubmit,
    formState: { errors, isValid },
    trigger,
    setValue,
  } = useForm({
    mode: 'onBlur',
    defaultValues: getDefaultValues(),
    resolver: (values, context, options) =>
      zodResolver(validationSchema(values))(values, context, options),
  });

  const onSubmit = (values) => {
    // here distinguish whether the onSubmit happens from detalis step or confirm page.
    // if it happens from details step, send an endpoint to validate the data,
    // if from confirm page - save & validate
    console.log(values);
    next();
  };

  useEffect(() => {
    if (currentLocation) {
      setValue('origin', {
        id: currentLocation?.id,
        label: `${currentLocation?.name} [${currentLocation?.locationType?.description}]`,
      });
    }
  }, [currentLocation?.id]);

  return {
    control,
    getValues,
    handleSubmit,
    errors,
    isValid,
    onSubmit,
    trigger,
  };
};

export default useOutboundImportForm;
