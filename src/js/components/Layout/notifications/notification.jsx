import React from 'react';

import {
  RiAlertLine, RiCheckboxCircleLine, RiErrorWarningFill, RiErrorWarningLine, RiInformationLine,
} from 'react-icons/ri';
import { toast } from 'react-toastify';

import NotificationType from 'consts/notificationTypes';

const notification = (type) => ({
  message, details, icon, detailsArray,
}) => {
  const alertsProps = {
    customFields: {
      details,
      icon,
      detailsArray,
    },
  };
  switch (type) {
    case NotificationType.SUCCESS:
      return toast.success(message, {
        ...alertsProps,
        customFields: {
          ...alertsProps.customFields,
          icon: alertsProps.customFields.icon ?? <RiCheckboxCircleLine />,
        },
      });
    case NotificationType.WARNING:
      return toast.warning(message, {
        ...alertsProps,
        customFields: {
          ...alertsProps.customFields,
          icon: alertsProps.customFields.icon ?? <RiAlertLine />,
        },
      });
    case NotificationType.ERROR_OUTLINED:
      return toast.error(message, {
        ...alertsProps,
        customFields: {
          ...alertsProps.customFields,
          icon: alertsProps.customFields.icon ?? <RiErrorWarningLine />,
        },
      });
    case NotificationType.ERROR_FILLED:
      return toast.error(message, {
        ...alertsProps,
        customFields: {
          ...alertsProps.customFields,
          icon: alertsProps.customFields.icon ?? <RiErrorWarningFill />,
        },
      });
    default:
      return toast.info(message, {
        ...alertsProps,
        customFields: {
          ...alertsProps.customFields,
          icon: alertsProps.customFields.icon ?? <RiInformationLine />,
        },
      });
  }
};

export default notification;
