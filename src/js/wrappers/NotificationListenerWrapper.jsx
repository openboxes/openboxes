import useNotificationListener from 'hooks/useNotificationListener';

const NotificationListenerWrapper = ({ children }) => {
  useNotificationListener();

  return children;
};

export default NotificationListenerWrapper;
