import { useEffect } from 'react';

import _ from 'lodash';
import queryString from 'query-string';
import { useHistory, useLocation } from 'react-router-dom';

import notification from 'components/Layout/notifications/notification';
import NotificationType from 'consts/notificationTypes';

const useDeleteNotificationListener = () => {
  const { search } = useLocation();
  const history = useHistory();
  const parsedQuerySearch = queryString.parse(search);
  const { notificationMessage, notificationType } = parsedQuerySearch;

  useEffect(() => {
    if (notificationMessage) {
      notification(notificationType?.toLowerCase() || NotificationType.SUCCESS)({
        message: notificationMessage,
      });
      const clearedQuerySearch = _.omit(parsedQuerySearch, ['notificationMessage', 'notificationType']);
      const parsedClearedQuerySearch = queryString.stringify(clearedQuerySearch);
      history.replace({
        search: parsedClearedQuerySearch,
      });
    }
  }, []);
};

export default useDeleteNotificationListener;
