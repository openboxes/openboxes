import notification from 'components/Layout/notifications/notification';
import NotificationType from 'consts/notificationTypes';
import translate from 'utils/Translate';

const useThrowError = ({
  condition, callWhenValid, errorMessageLabel, errorMessageDefault, translateData,
}) => {
  const verifyCondition = async () => {
    if (!condition) {
      notification(NotificationType.ERROR_OUTLINED)({
        message: translate({
          id: errorMessageLabel,
          defaultMessage: errorMessageDefault,
          data: translateData,
        }),
      });
      return;
    }
    await callWhenValid();
  };

  return {
    verifyCondition,
  };
};

export default useThrowError;
