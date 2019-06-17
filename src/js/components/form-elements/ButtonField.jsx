import React from 'react';
import PropTypes from 'prop-types';
import { Tooltip } from 'react-tippy';

import Translate from '../../utils/Translate';

const ButtonField = (props) => {
  const {
    fieldName,
    fieldConfig: {
      buttonLabel: ButtonLabel, buttonDefaultMessage, getDynamicAttr, attributes = { className: 'btn-outline-primary' },
    },
  } = props;
  const dynamicAttr = getDynamicAttr ? getDynamicAttr(props) : {};
  const attr = { ...attributes, ...dynamicAttr };

  return (
    <button type="button" key={fieldName} {...attr} className={`text-truncate btn btn-xs ${attr.className}`} >
      {
        typeof ButtonLabel === 'string' ?
          <Tooltip
            html={(ButtonLabel &&
            <Translate id={ButtonLabel} defaultMessage={buttonDefaultMessage} />)}
            theme="transparent"
            arrow="true"
            delay="150"
            duration="250"
            hideDelay="50"
          > <Translate id={ButtonLabel} defaultMessage={buttonDefaultMessage} />
          </Tooltip>
          : <ButtonLabel />
      }
    </button>
  );
};

export default ButtonField;

ButtonField.propTypes = {
  fieldName: PropTypes.string.isRequired,
  fieldConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
};
