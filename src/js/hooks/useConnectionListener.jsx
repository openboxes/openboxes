import React, { useEffect, useRef } from 'react';

import { RiWifiLine, RiWifiOffLine } from 'react-icons/ri';
import { getTranslate } from 'react-localize-redux';
import { useDispatch, useSelector } from 'react-redux';

import { setOffline, setOnline } from 'actions';
import notification from 'components/Layout/notifications/notification';
import NotificationType from 'consts/notificationTypes';
import { translateWithDefaultMessage } from 'utils/Translate';

const useConnectionListener = () => {
  const timeoutId = useRef(null);

  const dispatch = useDispatch();
  const { translate, online, connectionTimeout } = useSelector(state => ({
    translate: translateWithDefaultMessage(getTranslate(state.localize)),
    online: state.connection.online,
    connectionTimeout: state.session.connectionTimeout,
  }));
  const setOnlineStatus = () => {
    dispatch(setOnline());
    notification(NotificationType.SUCCESS)({
      message: translate(
        'react.notification.connectivity.online.label',
        'Connection restored.',
      ),
      details: translate(
        'react.notification.connectivity.online.message',
        'You are back online. You can now continue your work.',
      ),
      icon: <RiWifiLine />,
    });
  };
  const setOfflineStatus = () => {
    dispatch(setOffline());
    notification(NotificationType.INFO)({
      message: translate(
        'react.notification.connectivity.offline.label',
        'Lost Connection',
      ),
      details: translate(
        'react.notification.connectivity.offline.message',
        'You are now offline. Your changes will be saved when the connection is restored.',
      ),
      icon: <RiWifiOffLine />,
    });
  };

  const checkOnlineStatus = () => {
    timeoutId.current = null;

    if (online !== navigator.onLine) {
      if (navigator.onLine) {
        setOnlineStatus();
      } else {
        setOfflineStatus();
      }
    }
  };

  const onChangedOnlineStatus = () => {
    if (!timeoutId.current) {
      timeoutId.current = setTimeout(checkOnlineStatus, connectionTimeout);
    }
  };


  useEffect(() => {
    window.addEventListener('offline', onChangedOnlineStatus);
    window.addEventListener('online', onChangedOnlineStatus);
    return () => {
      window.removeEventListener('offline', onChangedOnlineStatus);
      window.removeEventListener('online', onChangedOnlineStatus);
      clearTimeout(timeoutId.current);
      timeoutId.current = null;
    };
  }, [online, translate]);
};

export default useConnectionListener;
