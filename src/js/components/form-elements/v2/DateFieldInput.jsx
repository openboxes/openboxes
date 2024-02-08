import React, { forwardRef } from 'react';

import PropTypes from 'prop-types';
import { RiCalendarLine, RiCloseLine } from 'react-icons/ri';

import './style.scss';

const DateFieldInput = forwardRef(({
  value,
  placeholder,
  onClear,
  ...props
}, ref) => (
  <div
    ref={ref}
    {...props}
  >
    <span>{value || placeholder}</span>
    <span className="form-element-icons-wrapper">{value ? <RiCloseLine onClick={onClear} /> : <RiCalendarLine />}</span>
  </div>
));

export default DateFieldInput;

DateFieldInput.propTypes = {
  value: PropTypes.string,
  placeholder: PropTypes.string,
  onClear: PropTypes.func.isRequired,
};

DateFieldInput.defaultProps = {
  value: null,
  placeholder: '',
};
