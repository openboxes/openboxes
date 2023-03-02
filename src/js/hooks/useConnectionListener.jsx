import { useEffect, useRef } from 'react';

import { getTranslate } from 'react-localize-redux';
import { useDispatch, useSelector } from 'react-redux';
import Alert from 'react-s-alert';

import { setOffline, setOnline } from 'actions';
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
    Alert.success(translate(
      'react.notification.connectivity.online.message',
      'You are back online. You can now continue your work.',
    ), { timeout: 10000 });
  };
  const setOfflineStatus = () => {
    dispatch(setOffline());
    Alert.warning(translate(
      'react.notification.connectivity.offline.message',
      'You are now offline. Your changes will be saved when the connection is restored.',
    ), { timeout: 10000 });
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
