import { useEffect, useMemo, useState } from 'react';

import { zodResolver } from '@hookform/resolvers/zod';
import _ from 'lodash';
import { useForm } from 'react-hook-form';
import { useDispatch, useSelector } from 'react-redux';
import {
  getCurrentLocale,
  getCurrentLocation,
  getShipmentTypes,
} from 'selectors';

import { fetchShipmentTypes, updateWorkflowHeader } from 'actions';
import stockMovementApi from 'api/services/StockMovementApi';
import notification from 'components/Layout/notifications/notification';
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import locationType from 'consts/locationType';
import NotificationType from 'consts/notificationTypes';
import requisitionStatus from 'consts/requisitionStatus';
import RoleType from 'consts/roleType';
import { DateFormat } from 'consts/timeFormat';
import { OutboundWorkflowState } from 'consts/WorkflowState';
import useInboundSendValidation from 'hooks/inboundV2/send/useInboundSendValidation';
import useQueryParams from 'hooks/useQueryParams';
import useSpinner from 'hooks/useSpinner';
import useTranslate from 'hooks/useTranslate';
import useUserHasPermissions from 'hooks/useUserHasPermissions';
import confirmationModal from 'utils/confirmationModalUtils';
import createInboundWorkflowHeader from 'utils/createInboundWorkflowHeader';
import dateWithoutTimeZone from 'utils/dateUtils';

const useInboundSendForm = ({ previous }) => {
  const [files, setFiles] = useState([]);
  const {
    currentLocation,
    currentLocale,
    shipmentTypes,
  } = useSelector((state) => ({
    currentLocation: getCurrentLocation(state),
    currentLocale: getCurrentLocale(state),
    shipmentTypes: getShipmentTypes(state),
  }));
  const hasRoleAdmin = useUserHasPermissions({
    minRequiredRole: RoleType.ROLE_ADMIN,
  });
  const translate = useTranslate();
  const { id: stockMovementId } = useQueryParams();
  const dispatch = useDispatch();
  const spinner = useSpinner();
  const { validationSchema } = useInboundSendValidation();

  // We don't want to allow users to select "Default" shipment type, so we filter it out
  const shipmentTypesWithoutDefaultValue = useMemo(
    () => shipmentTypes.filter((item) => item.name !== 'Default'),
    [shipmentTypes],
  );

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
    hasManageInventory: false,
    statusCode: '',
    shipped: false,
    documents: [],
  }), [currentLocation?.id]);

  const {
    control,
    getValues,
    handleSubmit,
    formState: { errors },
    trigger,
    reset,
  } = useForm({
    mode: 'onBlur',
    defaultValues,
    resolver: zodResolver(validationSchema()),
  });
  const {
    statusCode,
    shipped,
    destination,
    documents,
  } = getValues();

  // True when destination matches current location (valid case)
  // or when destination is not yet loaded from backend (to avoid showing incorrect UI state
  // during initial loading)
  const matchesDestination = currentLocation?.id && destination?.id
    ? currentLocation.id === destination.id
    : true;
  const hasErrors = Object.keys(errors).length > 0;

  const getShipmentPayload = () => {
    const {
      shipDate,
      shipmentType,
      trackingNumber,
      driverName,
      comments,
      expectedDeliveryDate,
    } = getValues();

    return {
      dateShipped: dateWithoutTimeZone({
        date: shipDate,
        outputDateFormat: DateFormat.MM_DD_YYYY,
      }),
      shipmentType: shipmentType.id,
      trackingNumber: trackingNumber ?? '',
      driverName: driverName ?? '',
      comments: comments ?? '',
      expectedDeliveryDate: dateWithoutTimeZone({
        date: expectedDeliveryDate,
        outputDateFormat: DateFormat.MM_DD_YYYY,
      }),
    };
  };

  const fetchStockMovementData = async () => {
    try {
      spinner.show();
      const response = await stockMovementApi.getStockMovementById(stockMovementId,
        { stepNumber: OutboundWorkflowState.SEND_SHIPMENT });

      const { data } = response.data;

      reset({
        origin: data.origin
          ? {
            id: data.origin.id,
            name: data.origin.name,
            label: `${data.origin.name} [${data.origin.locationType?.description ?? ''}]`,
          }
          : null,
        destination: data.destination
          ? {
            id: data.destination.id,
            name: data.destination.name,
            label: `${data.destination.name} [${data.destination.locationType?.description ?? ''}]`,
          }
          : null,
        shipDate: data.dateShipped ?? null,
        shipmentType: data.shipmentType && data.shipmentType.name !== 'Default'
          ? {
            id: data.shipmentType.id,
            name: data.shipmentType.name,
            label: data.shipmentType.displayName,
            value: data.shipmentType.id,
          }
          : null,
        trackingNumber: data.trackingNumber ?? '',
        driverName: data.driverName ?? '',
        comments: data.comments ?? '',
        expectedDeliveryDate: data.expectedDeliveryDate ?? null,
        statusCode: data.statusCode ?? '',
        hasManageInventory: data.hasManageInventory ?? false,
        shipped: data.shipped ?? false,
        documents: _.filter(data.associations.documents, (document) => document.stepNumber === 5),
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

  const sendFiles = async () => {
    const data = new FormData();

    files.forEach((file, idx) => {
      data.append(`filesContents[${idx}]`, file);
    });

    await stockMovementApi.uploadDocuments(stockMovementId, data);
    if (files.length > 1) {
      notification(NotificationType.SUCCESS)({
        message: translate(
          'react.stockMovement.alert.filesSuccess.label',
          'Files uploaded successfully!',
        ),
      });
      return;
    }
    notification(NotificationType.SUCCESS)({
      message: translate(
        'react.stockMovement.alert.fileSuccess.label',
        'File uploaded successfully!',
      ),
    });
  };

  // sends the whole stock movement
  const sendShipment = async (values) => {
    try {
      spinner.show();
      if (values.origin?.type !== locationType.SUPPLIER && values.hasManageInventory) {
        notification(NotificationType.ERROR_FILLED)({
          message: 'Error',
          details: translate(
            'react.stockMovement.alert.sendStockMovement.label',
            'You are not able to send shipment from a location other than origin. Change your current location.',
          ),
        });
        return;
      }
      // first we should send files if there are any uploaded by user
      if (files.length > 0) {
        await sendFiles();
      }

      await stockMovementApi.updateShipment(stockMovementId, getShipmentPayload());
      await stockMovementApi.updateStatus(stockMovementId,
        { status: requisitionStatus.DISPATCHED });
      window.location = STOCK_MOVEMENT_URL.show(stockMovementId);
    } finally {
      spinner.hide();
    }
  };

  // Rollback stock movement if it has been shipped
  const rollbackStockMovement = async () => {
    try {
      spinner.show();
      const { origin, hasManageInventory } = getValues();
      const matchesOrigin = currentLocation?.id === origin?.id;

      const canRollback = (hasManageInventory && matchesOrigin)
        || (!hasManageInventory && matchesDestination);

      if (!canRollback) {
        notification(NotificationType.ERROR_FILLED)({
          message: 'Error',
          details: translate(
            'react.stockMovement.alert.rollbackShipment.label',
            'You are not able to rollback shipment from your location.',
          ),
        });
        return;
      }

      await stockMovementApi.updateStatus(stockMovementId, { rollback: true });
      await fetchStockMovementData();
    } finally {
      spinner.hide();
    }
  };

  // save the whole stock movement
  const onSave = async ({ showNotification = true }) => {
    const isValid = await trigger();
    if (!isValid) {
      return;
    }
    try {
      spinner.show();
      await stockMovementApi.updateShipment(stockMovementId, getShipmentPayload());

      if (statusCode === requisitionStatus.DISPATCHED) {
        await fetchStockMovementData();
      }

      if (showNotification) {
        notification(NotificationType.SUCCESS)({
          message: translate(
            'react.stockMovement.alert.saveSuccess.label',
            'Changes saved successfully',
          ),
        });
      }
    } finally {
      spinner.hide();
    }
  };

  const confirmActionModal = ({
    titleId = 'react.stockMovement.message.confirmSave.label',
    titleDefault = 'Confirm save',
    messageId,
    messageDefault,
    buttons,
    onConfirm,
  }) => {
    const modalLabels = {
      title: { label: titleId, default: titleDefault },
      content: { label: messageId, default: messageDefault },
    };

    const modalButtons = (onClose) => {
      if (buttons) {
        return buttons(onClose);
      }
      return [
        {
          variant: 'transparent',
          defaultLabel: 'No',
          label: 'react.default.no.label',
          onClick: () => onClose(),
        },
        {
          variant: 'primary',
          defaultLabel: 'Yes',
          label: 'react.default.yes.label',
          onClick: () => {
            onConfirm?.();
            onClose();
          },
        },
      ];
    };

    confirmationModal({
      buttons: modalButtons,
      ...modalLabels,
    });
  };

  // Saves changes made by user in this step and go back to previous page
  const previousPage = async () => {
    const isValid = await trigger();
    if (isValid) {
      await stockMovementApi.updateShipment(stockMovementId, getShipmentPayload());
      return previous();
    }

    return confirmActionModal({
      titleId: 'react.stockMovement.confirmPreviousPage.label',
      titleDefault: 'Validation error',
      messageId: 'react.stockMovement.confirmPreviousPage.message.label',
      messageDefault: 'Cannot save due to validation error on page',
      buttons: (onClose) => [
        {
          variant: 'primary',
          defaultLabel: 'Correct error',
          label: 'react.stockMovement.confirmPreviousPage.correctError.label',
          onClick: () => onClose(),
        },
        {
          variant: 'danger',
          defaultLabel: 'Continue (lose unsaved work)',
          label: 'react.stockMovement.confirmPreviousPage.continue.label',
          onClick: async () => {
            previous();
            onClose();
          },
        },
      ],
    });
  };

  // Saves changes made by user in this step and redirects to the shipment view page
  const saveAndExit = async () => {
    const isValid = await trigger();

    if (isValid) {
      await stockMovementApi.updateShipment(stockMovementId, getShipmentPayload());
      window.location = STOCK_MOVEMENT_URL.show(stockMovementId);
      return;
    }

    confirmActionModal({
      messageId: 'react.stockMovement.confirmExit.message',
      messageDefault: 'Validation errors occurred. Are you sure you want to exit and lose unsaved data?',
      onConfirm: () => {
        window.location = STOCK_MOVEMENT_URL.show(stockMovementId);
      },
    });
  };

  const handleExportFile = async (doc) => {
    const isValid = await trigger();
    if (!isValid || !doc?.uri) {
      return;
    }
    await onSave({ showNotification: false });
    window.open(doc.uri, '_blank');
  };

  const handleDownloadFiles = (newFiles) => {
    setFiles((prevFiles) =>
      _.unionBy([...prevFiles, ...newFiles], 'name'));
  };

  const handleRemoveFile = (fileToRemove) => {
    setFiles((prevFiles) => prevFiles.filter((f) => f.name !== fileToRemove.name));
  };

  useEffect(() => {
    dispatch(fetchShipmentTypes());
  }, [currentLocale, stockMovementId]);

  useEffect(() => {
    fetchStockMovementData();
  }, [stockMovementId]);

  return {
    control,
    getValues,
    handleSubmit,
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
    hasErrors,
    matchesDestination,
    documents,
    handleExportFile,
    handleDownloadFiles,
    files,
    handleRemoveFile,
  };
};

export default useInboundSendForm;
