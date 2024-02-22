import React from 'react';

import PropTypes from 'prop-types';
import { RiCheckboxCircleLine } from 'react-icons/all';
import { RiErrorWarningLine } from 'react-icons/ri';

import Button from 'components/form-elements/Button';

const InvalidItemsIndicator = ({ className, errorsCounter, setFilterInvalid }) => {
  const { Icon, wrapperClassName, variant } = errorsCounter ? {
    Icon: RiErrorWarningLine,
    variant: 'danger',
    wrapperClassName: 'is-invalid',
  } : {
    Icon: RiCheckboxCircleLine,
    variant: 'transparent',
    wrapperClassName: 'is-valid',
  };

  return (
    <Button
      variant={variant}
      className={`invalid-items-indicator ${wrapperClassName} ${className}`}
      label="react.productSupplier.form.invalidItemsIndicator.title"
      defaultLabel="Item(s) require attention"
      onClick={() => setFilterInvalid(prev => !prev)}
      StartIcon={(
        <>
          <Icon />
          {errorsCounter}
          {' '}
        </>
      )}
    />
  );
};

export default InvalidItemsIndicator;

InvalidItemsIndicator.propTypes = {
  className: PropTypes.string,
  errorsCounter: PropTypes.number,
};

InvalidItemsIndicator.defaultProps = {
  className: '',
  errorsCounter: 0,
};
