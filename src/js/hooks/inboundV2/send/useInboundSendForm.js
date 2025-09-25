import { useEffect, useMemo } from 'react';

import { zodResolver } from '@hookform/resolvers/zod';
import _ from 'lodash';
import { useForm } from 'react-hook-form';
import { useDispatch, useSelector } from 'react-redux';
import { getCurrentLocale, getCurrentLocation, getShipmentTypes } from 'selectors';

import { fetchShipmentTypes, updateWorkflowHeader } from 'actions';
import stockMovementApi from 'api/services/StockMovementApi';
import notification from 'components/Layout/notifications/notification';
import locationType from 'consts/locationType';
import NotificationType from 'consts/notificationTypes';
import { OutboundWorkflowState } from 'consts/WorkflowState';
import useInboundSendValidation from 'hooks/inboundV2/send/useInboundSendValidation';
import useQueryParams from 'hooks/useQueryParams';
import useSpinner from 'hooks/useSpinner';
import useTranslate from 'hooks/useTranslate';
import createInboundWorkflowHeader from 'utils/createInboundWorkflowHeader';

const useInboundSendForm = () => {
  const {
    currentLocation,
    currentLocale,
    shipmentTypes,
  } = useSelector((state) => ({
    currentLocation: getCurrentLocation(state),
    currentLocale: getCurrentLocale(state),
    shipmentTypes: getShipmentTypes(state),
  }));

  const translate = useTranslate();
  const queryParams = useQueryParams();
  const dispatch = useDispatch();
  const spinner = useSpinner();
  const { validationSchema } = useInboundSendValidation();

  const defaultValues = useMemo(() => ({
    origin: null,
    destination: {
      id: currentLocation?.id,
      label: `${currentLocation?.name} [${currentLocation?.locationType?.description}]`,
      name: currentLocation?.name,
    },
    shipDate: null,
    shipmentType: null,
    trackingNumber: '',
    driverName: '',
    comments: '',
    expectedDeliveryDate: null,
  }), [currentLocation]);

  const {
    control,
    getValues,
    handleSubmit,
    formState: { errors },
    trigger,
    setValue,
    reset,
  } = useForm({
    mode: 'onBlur',
    defaultValues,
    resolver: zodResolver(validationSchema()),
  });

  const fetchStockMovementData = async () => {
    try {
      spinner.show();
      const response = await stockMovementApi.getStockMovementById(queryParams.id,
        { stepNumber: OutboundWorkflowState.SEND_SHIPMENT });

      const { data } = response.data;

      reset({
        origin: {
          id: data.origin.id,
          name: data.origin.name,
          label: `${data.origin.name} [${data.origin.locationType.description}]`,
        } || null,
        destination: {
          id: data.destination.id,
          name: data.destination.name,
          label: `${data.destination.name} [${data.destination.locationType ? data.destination.locationType.description : null}]`,
        } || null,
        shipDate: data.dateShipped || null,
        shipmentType: data.shipmentType
          ? {
            id: data.shipmentType.id,
            name: data.shipmentType.name,
            label: data.shipmentType.displayName,
            value: data.shipmentType.id,
          }
          : null,
        trackingNumber: data.trackingNumber || '',
        driverName: data.driverName || '',
        comments: data.comments || '',
        expectedDeliveryDate: data.expectedDeliveryDate || null,
      });

      dispatch(
        updateWorkflowHeader(
          createInboundWorkflowHeader(data),
          data.displayStatus.name,
        ),
      );
    } finally {
      spinner.hide();
    }
  };

  const onSubmit = async (values) => {
    console.log(values);

    if (
      (currentLocation?.id !== values.origin?.id) &&
      (values.origin?.type !== locationType.SUPPLIER && values.hasManageInventory)
    ) {
      notification(NotificationType.ERROR_FILLED)({
        message: 'Error',
        details: translate(
          'react.stockMovement.alert.sendStockMovement.label',
          'You are not able to send shipment from a location other than origin. Change your current location.',
        ),
      });
      return;
    }

    const defaultShipmentType = _.find(shipmentTypes, (shipmentType) => shipmentType.name === 'Default');

    // check if shipment type is default
    if (values.shipmentType?.id === defaultShipmentType?.id) {
      notification(NotificationType.ERROR_FILLED)({
        message: 'Error',
        details: translate(
          'react.stockMovement.alert.populateShipmentType.label',
          'Please populate shipment type before continuing',
        ),
      });
    }
  };

  useEffect(() => {
    dispatch(fetchShipmentTypes());
  }, [currentLocale]);

  useEffect(() => {
    fetchStockMovementData();
  }, []);

  return {
    control,
    getValues,
    setValue,
    handleSubmit,
    errors,
    trigger,
    onSubmit,
    shipmentTypes,
  };
};

export default useInboundSendForm;
