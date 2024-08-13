import { useEffect, useState } from 'react';

import { zodResolver } from '@hookform/resolvers/zod';
import _ from 'lodash';
import moment from 'moment/moment';
import { useForm } from 'react-hook-form';
import { useSelector } from 'react-redux';

import notification from 'components/Layout/notifications/notification';
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import NotificationType from 'consts/notificationTypes';
import { DateFormat } from 'consts/timeFormat';
import useOutboundImportValidation from 'hooks/outboundImport/useOutboundImportValidation';
import useTranslate from 'hooks/useTranslate';
import apiClient from 'utils/apiClient';

// TODO: Remove this before feature is finished
// TODO: Remove this before feature is finished

const useOutboundImportForm = () => {
  const translate = useTranslate();
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

  const [lineItems] = useState([]);
  const [lineItemErrors] = useState({});
  const [headerDetailsData] = useState({});

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

  // TODO: implement data validation request
  // TODO: implement data validation request
  const onSubmitStockMovementDetails = (values) => {
    // here distinguish whether the onSubmit happens from detalis step or confirm page.
    // if it happens from details step, send an endpoint to validate the data,
    // if from confirm page - save & validate
    console.log(values.packingList);
    const formData = new FormData();
    formData.append('importFile', values.packingList);
    const config = {
      headers: {
        'content-type': 'multipart/form-data',
      },
    };
    apiClient.post('/packingList/importPackingList', formData, config).then((res) => {
      console.log(res);
      const basicDetails = {
        ..._.omit(values, 'shipmentType', 'trackingNumber', 'dateShipped', 'expectedDeliveryDate', 'packingList'),
        dateRequested: moment(values.dateRequested).format(DateFormat.MM_DD_YYYY),
      };
      const sendingOptions = {
        ..._.pick(values, ['shipmentType', 'trackingNumber']),
        expectedShippingDate: moment(values.dateShipped).format(DateFormat.MM_DD_YYYY),
        expectedDeliveryDate: moment(values.expectedDeliveryDate).format(DateFormat.MM_DD_YYYY),
      };

      const items = res.data.data.map((item) => ({
        origin: basicDetails.origin.id,
        ...item,
        product: item.productCode,
        rowId: _.uniqueId(),
      }));

      apiClient.post('/api/fulfillments/validate', {
        fulfillmentDetails: basicDetails,
        packingList: items,
        sendingOptions,
      }).then(() => apiClient.post('/api/fulfillments', {
        fulfillmentDetails: basicDetails,
        packingList: items,
        sendingOptions,
      })).then((response) => {
        window.location = STOCK_MOVEMENT_URL.show(response.data.data.id);
      });
    });
  };

  // TODO: implement confirm import logic
  const onConfirmImport = (values) => {
    // here distinguish whether the onSubmit happens from details step or confirm page.
    // if it happens from details step, send an endpoint to validate the data,
    // if from confirm page - save & validate
    console.log('Sending values for saving import', values, lineItems);
    notification(NotificationType.SUCCESS)({
      message: translate('react.outboundImport.form.created.success.label', 'Stock Movement has been created successfully'),
    });
    // TODO: redirect to created stockMovement show page
  };

  useEffect(() => {
    if (currentLocation) {
      setValue('origin', {
        id: currentLocation?.id,
        name: currentLocation?.name,
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
    onSubmitStockMovementDetails,
    onConfirmImport,
    headerDetailsData,
    trigger,
    lineItemErrors,
    lineItems,
  };
};

export default useOutboundImportForm;
