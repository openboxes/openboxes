import { useEffect, useRef } from 'react';

import { getTranslate } from 'react-localize-redux';
import { useDispatch, useSelector } from 'react-redux';
import Alert from 'react-s-alert';

import { setOffline, setOnline } from 'actions';
import { translateWithDefaultMessage } from 'utils/Translate';

const useConnectionListener = () => {
  const timeoutId = useRef(null);
  const previousOnlineStatus = useRef(navigator.onLine);

  const dispatch = useDispatch();
  const { translate } = useSelector(state => ({
    translate: translateWithDefaultMessage(getTranslate(state.localize)),
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
    if (previousOnlineStatus.current !== navigator.onLine) {
      if (navigator.onLine) {
        setOnlineStatus();
      } else {
        setOfflineStatus();
      }
    }
    timeoutId.current = null;
    previousOnlineStatus.current = navigator.onLine;
  };

  const onChangedOnlineStatus = () => {
    if (!timeoutId.current) {
      timeoutId.current = setTimeout(checkOnlineStatus, 5000);
    }
  };


  useEffect(() => {
    window.addEventListener('offline', onChangedOnlineStatus);
    window.addEventListener('online', onChangedOnlineStatus);

    // cleanup if we unmount
    return () => {
      window.removeEventListener('offline', onChangedOnlineStatus);
      window.removeEventListener('online', onChangedOnlineStatus);
      clearTimeout(timeoutId.current);
    };
  }, []);
};

export default useConnectionListener;
