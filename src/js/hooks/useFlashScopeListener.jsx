import { useEffect } from 'react';

import _ from 'lodash';
import queryString from 'query-string';
import { useLocation, useNavigate } from 'react-router-dom';

import notification from 'components/Layout/notifications/notification';
import NotificationType from 'consts/notificationTypes';

const useFlashScopeListener = () => {
  const { search } = useLocation();
  const navigate = useNavigate();
  const parsedQuerySearch = queryString.parse(search);
  const { flash } = parsedQuerySearch;

  const clearQuerySearch = (...pathToOmit) => {
    const clearedQuerySearch = _.omit(parsedQuerySearch, pathToOmit);
    const parsedClearedQuerySearch = queryString.stringify(clearedQuerySearch);
    navigate({
      search: parsedClearedQuerySearch,
    }, { replace: true });
  };

  useEffect(() => {
    if (!flash) {
      return;
    }
    const { message, error } = JSON.parse(flash);
    if (message) {
      notification(NotificationType.SUCCESS)({ message });
    }
    if (error) {
      notification(NotificationType.ERROR)({ error });
    }
    clearQuerySearch('flash');
  }, []);
};

export default useFlashScopeListener;
