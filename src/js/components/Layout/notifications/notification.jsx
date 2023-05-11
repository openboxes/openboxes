import React from 'react';

import { RiAlertLine, RiCheckboxCircleLine, RiErrorWarningFill, RiErrorWarningLine, RiInformationLine } from 'react-icons/ri';
import Alert from 'react-s-alert';

import NotificationType from 'consts/notificationTypes';

const notification = type => ({
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
      return Alert.success(message, {
        ...alertsProps,
        customFields: {
          ...alertsProps.customFields,
          icon: alertsProps.customFields.icon ?? <RiCheckboxCircleLine />,
        },
      });
    case NotificationType.WARNING:
      return Alert.warning(message, {
        ...alertsProps,
        customFields: {
          ...alertsProps.customFields,
          icon: alertsProps.customFields.icon ?? <RiAlertLine />,
        },
      });
    case NotificationType.ERROR_OUTLINED:
      return Alert.error(message, {
        ...alertsProps,
        customFields: {
          ...alertsProps.customFields,
          icon: alertsProps.customFields.icon ?? <RiErrorWarningLine />,
        },
      });
    case NotificationType.ERROR_FILLED:
      return Alert.error(message, {
        ...alertsProps,
        customFields: {
          ...alertsProps.customFields,
          icon: alertsProps.customFields.icon ?? <RiErrorWarningFill />,
        },
      });
    default:
      return Alert.info(message, {
        ...alertsProps,
        customFields: {
          ...alertsProps.customFields,
          icon: alertsProps.customFields.icon ?? <RiInformationLine />,
        },
      });
  }
};

export default notification;
