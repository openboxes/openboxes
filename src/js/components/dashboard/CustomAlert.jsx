import React from 'react';

import PropTypes from 'prop-types';
import { RiAlertLine, RiCheckboxCircleLine, RiCloseLine, RiErrorWarningFill, RiInformationLine } from 'react-icons/ri';

import NotificationType from 'consts/notificationTypes';

const defaultStartIcon = {
  [NotificationType.SUCCESS]: <RiCheckboxCircleLine />,
  [NotificationType.INFO]: <RiInformationLine />,
  [NotificationType.ERROR]: <RiErrorWarningFill />,
  [NotificationType.WARNING]: <RiAlertLine />,
};

const CustomAlert = ({
  message, classNames, customFields, id, styles, condition, handleClose,
}) => {
  // Show default start icon for already existing Alerts,
  // which are not called by notification(), but Alert
  const getIcon = () => {
    if (customFields?.icon) {
      return customFields.icon;
    }
    return defaultStartIcon[condition ?? NotificationType.INFO];
  };

  return (
    <div className={`${classNames} ${!customFields?.details ? 'no-details' : ''}`} id={id} style={styles}>
      <div className="s-alert-box-inner">
        <div className="alert-start-icon">
          {getIcon()}
        </div>
        <span className="alert-title">{message}</span>
        {customFields?.detailsArray?.length ?
          (
            <div className="d-flex flex-column array-errors-wrapper">
              {customFields.detailsArray
                .filter(Boolean)
                .map(detail => (<p key={detail}>{detail}</p>))}
            </div>
          ) : null}
        {customFields?.details && <span className="alert-details">{customFields?.details}</span>}
        <div className="alert-close-icon">
          <span role="presentation" onClick={handleClose}>
            <RiCloseLine />
          </span>
        </div>
      </div>
    </div>
  );
};


export default CustomAlert;


CustomAlert.propTypes = {
  classNames: PropTypes.string.isRequired,
  /** Condition is a built-in prop in the Alert and the types of it can be:
    success, error, warning, info.
  */
  condition: PropTypes.string.isRequired,
  /** customFields can contain anything in the future - for now we use details and startIcon */
  customFields: PropTypes.shape({
    details: PropTypes.string.isRequired,
    icon: PropTypes.element.isRequired,
  }).isRequired,
  handleClose: PropTypes.func.isRequired,
  id: PropTypes.string.isRequired,
  /** message (title) is the first param passed to the Alert.(..) */
  message: PropTypes.string.isRequired,
  styles: PropTypes.shape({}).isRequired,
};
