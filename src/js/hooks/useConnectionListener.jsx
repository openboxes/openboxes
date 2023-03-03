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
  const { translate, online, browserConnectionTimeout } = useSelector(state => ({
    translate: translateWithDefaultMessage(getTranslate(state.localize)),
    online: state.connection.online,
    browserConnectionTimeout: state.session.browserConnectionTimeout,
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
    if (timeoutId.current) {
      clearTimeout(timeoutId.current);
      timeoutId.current = null;
    }

    if (online !== navigator.onLine) {
      if (navigator.onLine) {
        setOnlineStatus();
      } else {
        setOfflineStatus();
      }
    }
  };

  const onChangeStatusToOnline = () => {
    if (!timeoutId.current) {
      timeoutId.current = setTimeout(checkOnlineStatus, browserConnectionTimeout);
    }
  };

  const onChangeStatusToOffline = () => {
    checkOnlineStatus();
  };


  useEffect(() => {
    window.addEventListener('offline', onChangeStatusToOffline);
    window.addEventListener('online', onChangeStatusToOnline);
    return () => {
      window.removeEventListener('offline', onChangeStatusToOffline);
      window.removeEventListener('online', onChangeStatusToOnline);
      clearTimeout(timeoutId.current);
      timeoutId.current = null;
    };
  }, [online, translate]);
};

export default useConnectionListener;
