import React from 'react';
import PropTypes from 'prop-types';

const Input = ({
  fieldRef, onChange, className = '', arrowLeft, arrowUp, arrowRight, arrowDown, onTabPress, ...props
}) => {
  const handleChange = (event) => {
    const { value } = event.target;

    if (onChange) {
      onChange(value);
    }
  };

  return (
    <input
      type="text"
      ref={fieldRef}
      onKeyPress={(event) => {
        if (event.which === 13 /* Enter */) {
          event.preventDefault();
        }
      }}
      onKeyDown={(event) => {
        switch (event.keyCode) {
          case 37: /* arrow left */
            if (arrowLeft) {
              arrowLeft();
              event.preventDefault();
            }
            break;
          case 38: /* arrow up */
            if (arrowUp) {
              arrowUp();
              event.preventDefault();
            }
            break;
          case 39: /* arrow right */
            if (arrowRight) {
              arrowRight();
              event.preventDefault();
            }
            break;
          case 40: /* arrow down */
            if (arrowDown) {
              arrowDown();
              event.preventDefault();
            }
            break;
          case 9: /* Tab key */
            if (onTabPress) {
              onTabPress(event);
            }
            break;
          default:
        }
      }}
      className={`form-control form-control-xs ${className}`}
      {...props}
      onChange={handleChange}
    />
  );
};

export default Input;

Input.propTypes = {
  onChange: PropTypes.func,
  className: PropTypes.string,
  arrowLeft: PropTypes.func,
  arrowUp: PropTypes.func,
  arrowRight: PropTypes.func,
  arrowDown: PropTypes.func,
  fieldRef: PropTypes.func,
  onTabPress: PropTypes.func,
};

Input.defaultProps = {
  onChange: null,
  className: '',
  arrowLeft: null,
  arrowUp: null,
  arrowRight: null,
  arrowDown: null,
  fieldRef: null,
  onTabPress: null,
};
