import React from 'react';

import { RiInformationLine } from 'react-icons/all';
import { RiAlertLine, RiCheckboxCircleLine, RiErrorWarningFill, RiErrorWarningLine } from 'react-icons/ri';
import Alert from 'react-s-alert';

import NotificationType from 'consts/notificationTypes';

const notification = type => ({ message, details, startIcon }) => {
  const alertsProps = {
    customFields: {
      details,
      startIcon,
    },
  };
  switch (type) {
    case NotificationType.SUCCESS:
      return Alert.success(message, {
        ...alertsProps,
        customFields: {
          ...alertsProps.customFields,
          startIcon: alertsProps.customFields.startIcon ?? <RiCheckboxCircleLine />,
        },
      });
    case NotificationType.WARNING:
      return Alert.warning(message, {
        ...alertsProps,
        customFields: {
          ...alertsProps.customFields,
          startIcon: alertsProps.customFields.startIcon ?? <RiAlertLine />,
        },
      });
    case NotificationType.ERROR_OUTLINED:
      return Alert.error(message, {
        ...alertsProps,
        customFields: {
          ...alertsProps.customFields,
          startIcon: alertsProps.customFields.startIcon ?? <RiErrorWarningLine />,
        },
      });
    case NotificationType.ERROR_FILLED:
      return Alert.error(message, {
        ...alertsProps,
        customFields: {
          ...alertsProps.customFields,
          startIcon: alertsProps.customFields.startIcon ?? <RiErrorWarningFill />,
        },
      });
    default:
      return Alert.info(message, {
        ...alertsProps,
        customFields: {
          ...alertsProps.customFields,
          startIcon: alertsProps.customFields.startIcon ?? <RiInformationLine />,
        },
      });
  }
};

export default notification;
