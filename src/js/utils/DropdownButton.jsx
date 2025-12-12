import React, { memo } from 'react';

import PropTypes from 'prop-types';
import { RiDownload2Line } from 'react-icons/ri';

import Button from 'components/form-elements/Button';
import Translate from 'utils/Translate';

const DropdownButton = ({
  actions,
  disabled,
  buttonLabel,
  buttonDefaultLabel,
  variant,
}) => (
  <>
    <Button
      isDropdown
      disabled={disabled}
      defaultLabel={buttonDefaultLabel}
      label={buttonLabel}
      variant={variant}
      EndIcon={<RiDownload2Line />}
    />
    <div
      className="dropdown-menu dropdown-menu-right nav-item padding-8"
      aria-labelledby="dropdownMenuButton"
    >
      {actions.map((action) => (
        <a
          key={action.label}
          href="#"
          className="dropdown-item"
          onClick={action.onClick}
          role="button"
          tabIndex={0}
        >
          <Translate
            id={action.label}
            defaultMessage={action.defaultLabel}
          />
        </a>
      ))}
    </div>
  </>
);

export default memo(DropdownButton);

DropdownButton.propTypes = {
  actions: PropTypes.arrayOf(PropTypes.shape({
    label: PropTypes.string.isRequired,
    defaultLabel: PropTypes.string.isRequired,
    onClick: PropTypes.func.isRequired,
  })),
  buttonLabel: PropTypes.string.isRequired,
  buttonDefaultLabel: PropTypes.string.isRequired,
  disabled: PropTypes.bool,
  variant: PropTypes.string,
};

DropdownButton.defaultProps = {
  actions: [],
  disabled: false,
  variant: 'secondary',
};
