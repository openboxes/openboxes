import React from 'react';

import {
  RiAlertLine, RiCheckboxCircleLine, RiErrorWarningFill, RiErrorWarningLine, RiInformationLine,
} from 'react-icons/ri';
import { toast } from 'react-toastify';

import NotificationContent from 'components/Layout/notifications/NotificationContent';
import NotificationType from 'consts/notificationTypes';

const notificationConfig = {
  [NotificationType.SUCCESS]: { showToast: toast.success, icon: <RiCheckboxCircleLine /> },
  [NotificationType.WARNING]: { showToast: toast.warning, icon: <RiAlertLine /> },
  [NotificationType.ERROR]: { showToast: toast.error, icon: <RiErrorWarningFill /> },
  [NotificationType.ERROR_OUTLINED]: { showToast: toast.error, icon: <RiErrorWarningLine /> },
  [NotificationType.ERROR_FILLED]: { showToast: toast.error, icon: <RiErrorWarningFill /> },
  [NotificationType.INFO]: { showToast: toast.info, icon: <RiInformationLine /> },
};

const notification = (type) => ({
  message, details, icon, detailsArray,
}) => {
  const config = notificationConfig[type] ?? notificationConfig[NotificationType.INFO];

  return config.showToast(NotificationContent, {
    data: { message, details, detailsArray },
    icon: icon ?? config.icon,
  });
};

export default notification;
