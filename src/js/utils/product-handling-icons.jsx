import React from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import _ from 'lodash';

const getIcon = iconName => _.trimStart(iconName, ['fa', '-']);

const renderHandlingIcons = (handlingIcons) => {
  if (!handlingIcons || handlingIcons.length === 0) {
    return null;
  }

  return (
    <span className="d-flex align-items-center">
      {_.map(handlingIcons, (handlingIcon) => {
        if (!handlingIcon || !getIcon(handlingIcon.icon)) {
          return null;
        }
        return (
          <FontAwesomeIcon
            className="ml-1"
            icon={getIcon(handlingIcon.icon)}
            color={handlingIcon.color ? handlingIcon.color : 'inherit'}
          />
        );
      })}
    </span>
  );
};

export default renderHandlingIcons;
