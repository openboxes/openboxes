import React from 'react';

import PropTypes from 'prop-types';

import Translate from 'utils/Translate';
import CustomTooltip from 'wrappers/CustomTooltip';

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
    <button type="button" key={fieldName} {...attr} className={`text-truncate btn btn-xs ${attr.className}`}>
      {
        typeof ButtonLabel === 'string'
          ? (
            <CustomTooltip
              content={(ButtonLabel
            && <Translate id={ButtonLabel} defaultMessage={buttonDefaultMessage} />)}
            >
              {' '}
              <Translate id={ButtonLabel} defaultMessage={buttonDefaultMessage} />
            </CustomTooltip>
          )
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
