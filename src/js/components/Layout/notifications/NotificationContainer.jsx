import React from 'react';

import PropTypes from 'prop-types';
import {
  RiAlertLine,
  RiCheckboxCircleLine,
  RiCloseLine,
  RiErrorWarningFill,
  RiInformationLine,
} from 'react-icons/ri';
import { useSelector } from 'react-redux';
import { ToastContainer } from 'react-toastify';
import { getNotificationAutohideDelay } from 'selectors';

import 'react-toastify/dist/ReactToastify.css';

const iconByType = {
  success: <RiCheckboxCircleLine />,
  warning: <RiAlertLine />,
  error: <RiErrorWarningFill />,
};

const NotificationIcon = ({ type }) => iconByType[type] ?? <RiInformationLine />;

const NotificationCloseButton = ({ closeToast }) => (
  <span role="presentation" className="notification-close-icon" onClick={closeToast}>
    <RiCloseLine />
  </span>
);

const NotificationContainer = () => {
  const notificationAutohideDelay = useSelector(getNotificationAutohideDelay);

  return (
    <ToastContainer
      autoClose={notificationAutohideDelay}
      limit={3}
      position="top-right"
      hideProgressBar
      icon={NotificationIcon}
      closeButton={NotificationCloseButton}
    />
  );
};

export default NotificationContainer;

NotificationIcon.propTypes = {
  type: PropTypes.string.isRequired,
};

NotificationCloseButton.propTypes = {
  closeToast: PropTypes.func.isRequired,
};
