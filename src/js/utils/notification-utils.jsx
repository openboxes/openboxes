import queryString from 'query-string';

import notification from 'components/Layout/notifications/notification';
import NotificationType from 'consts/notificationTypes';
import translate from 'utils/Translate';

export default function triggerDeletionNotification(queryParams) {
  const parsedQueryParams = queryString.parse(queryParams);
  if (parsedQueryParams.deleted) {
    notification(NotificationType.SUCCESS)({
      message: translate({
        id: 'react.stockMovement.deleted.success.message.label',
        defaultMessage: 'Stock Movement has been deleted successfully',
      }),
    });
  }
}
