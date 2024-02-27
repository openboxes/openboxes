import React from 'react';

import PropTypes from 'prop-types';
import { RiCheckboxCircleLine } from 'react-icons/all';
import { RiErrorWarningLine } from 'react-icons/ri';

import Button from 'components/form-elements/Button';

const invalidLinesButton = {
  Icon: RiErrorWarningLine,
  variant: 'danger',
  wrapperClassName: 'is-invalid',
};

const validLinesButton = {
  Icon: RiCheckboxCircleLine,
  variant: 'transparent',
  wrapperClassName: 'is-valid',
};

const getButtonVariant = ({
  buttonVariant,
  isFiltered,
}) => ({
  ...buttonVariant,
  variant: isFiltered ? 'active' : buttonVariant.variant,
  wrapperClassName: isFiltered ? '' : buttonVariant.wrapperClassName,
});

const InvalidItemsIndicator = ({
  className,
  errorsCounter,
  setIsFiltered,
  isFiltered,
  triggerValidation,
}) => {
  const { Icon, variant, wrapperClassName } = getButtonVariant({
    buttonVariant: errorsCounter ? invalidLinesButton : validLinesButton,
    isFiltered,
  });

  const handleOnFilterButtonClick = () => {
    triggerValidation('productSupplierPreferences');
    setIsFiltered((value) => !value);
  };

  return (
    <Button
      variant={variant}
      className={`invalid-items-indicator ${wrapperClassName} ${className} active`}
      label="react.productSupplier.form.invalidItemsIndicator.title"
      defaultLabel="Item(s) require attention"
      onClick={handleOnFilterButtonClick}
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
  setIsFiltered: PropTypes.func,
  isFiltered: PropTypes.bool,
  triggerValidation: PropTypes.func.isRequired,
};

InvalidItemsIndicator.defaultProps = {
  className: '',
  errorsCounter: 0,
  setIsFiltered: () => {},
  isFiltered: false,
};
