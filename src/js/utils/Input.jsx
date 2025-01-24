import React from 'react';

import PropTypes from 'prop-types';

const Input = ({
  fieldRef, onChange, className = '', arrowLeft, arrowUp, arrowRight, arrowDown, copyDown, onTabPress, isFormElement, ...props
}) => {
  const handleChange = (event) => {
    const { value } = event.target;

    if (onChange) {
      onChange(value);
    }

    return event;
  };

  const handleFocus = (event) => event.target.select();

  let keys = {};

  return (
    <input
      data-testid="input"
      type="text"
      ref={fieldRef}
      onKeyPress={(event) => {
        if (event.which === 13 /* Enter */) {
          event.preventDefault();
        }
      }}
      onKeyDown={(event) => {
        keys[event.keyCode] = true;
        if (keys[40] && keys[17]) { /* CTRL + arrow down */
          if (copyDown) {
            copyDown();
            event.preventDefault();
          }
        } else if (keys[37]) { /* Arrow left */
          if (arrowLeft) {
            arrowLeft();
            event.preventDefault();
          }
        } else if (keys[38]) { /* Arrow up */
          if (arrowUp) {
            arrowUp();
            event.preventDefault();
          }
        } else if (keys[39]) { /* Arrow right */
          if (arrowRight) {
            arrowRight();
            event.preventDefault();
          }
        } else if (keys[40]) { /* Arrow down */
          if (arrowDown) {
            arrowDown();
            event.preventDefault();
          }
        } else if (keys[9]) { /* Tab */
          if (onTabPress) {
            onTabPress(event);
          }
        }
      }}
      onKeyUp={(event) => {
        keys = {};
        const { value } = event.target;
        onChange(value);
      }}
      /* eslint-disable-next-line react/prop-types */
      className={`${isFormElement ? 'form-control form-control-xs' : ''} ${className} ${props.type === 'number' ? 'text-right mr-2' : ''}`}
      {...props}
      onChange={handleChange}
      onFocus={handleFocus}
      onDragStart={(event) => {
        event.preventDefault();
      }}
      onWheel={() => document.activeElement.blur()}
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
  copyDown: PropTypes.func,
  fieldRef: PropTypes.func,
  onTabPress: PropTypes.func,
  isFormElement: PropTypes.bool,
};

Input.defaultProps = {
  onChange: null,
  className: '',
  arrowLeft: null,
  arrowUp: null,
  arrowRight: null,
  arrowDown: null,
  copyDown: null,
  fieldRef: null,
  onTabPress: null,
  isFormElement: false,
};
