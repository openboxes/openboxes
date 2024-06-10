import { useEffect, useState } from 'react';

import { zodResolver } from '@hookform/resolvers/zod';
import moment from 'moment/moment';
import { useForm } from 'react-hook-form';
import { useSelector } from 'react-redux';

import { DateFormat } from 'consts/timeFormat';
import useOutboundImportValidation from 'hooks/outboundImport/useOutboundImportValidation';

const testRow = {
  product: {
    id: 'someId',
    name: 'Some produc tname',
    productCode: '10002',
  },
  lotNumber: 'TE!11',
  expirationDate: '09/16/2027',
  quantityPicked: 2,
  binLocation: {
    id: 'someBinId',
    name: 'CUB1C',
    zone: {
      id: 'zoneId',
      name: 'someZone',
    },
  },
  recipient: {
    id: 'someuserId',
    firstName: 'first',
    lastName: 'last',
    username: 'someusername',
    name: 'first last',
  },
};

const otherData = [...Array(250).keys()].map(it => ({
  ...testRow,
  quantityPicked: it,
}));

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

  const [lineItems, setLineItems] = useState([]);

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
    setLineItems(otherData);
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
    lineItems,
  };
};

export default useOutboundImportForm;
