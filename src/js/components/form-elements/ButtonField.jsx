import React from 'react';
import PropTypes from 'prop-types';

const ButtonField = (props) => {
  const {
    fieldName,
    fieldConfig: { buttonLabel: ButtonLabel, getDynamicAttr, attributes = {} },
  } = props;
  const dynamicAttr = getDynamicAttr ? getDynamicAttr(props) : {};
  const attr = { ...attributes, ...dynamicAttr };

  return (
    <button type="button" key={fieldName} className="btn btn-outline-primary" {...attr} >
      {
        typeof ButtonLabel === 'string' ? ButtonLabel : <ButtonLabel />
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
