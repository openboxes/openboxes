import React from 'react';

import PropTypes from 'prop-types';
import { Overlay } from 'react-overlays';
import { components } from 'react-select';

import BaseField from 'components/form-elements/BaseField';
import Select from 'utils/Select';

const DROPDOWN_STYLE = {
  background: '#FFFFFF',
  'box-shadow': '0px 0px 0px 1px rgba(152, 161, 179, 0.1), 0px 15px 35px -5px rgba(17, 24, 38, 0.2), 0px 5px 15px rgba(0, 0, 0, 0.08)',
  'border-radius': '4px',
  position: 'absolute',
  zIndex: 3,
};

const Dropdown = ({ children, style, width }) => (
  <div
    style={{
      ...style, width, ...DROPDOWN_STYLE,
    }}
  >
    {children}
  </div>
);

Dropdown.propTypes = {
  children: PropTypes.element.isRequired,
  style: PropTypes.shape({}).isRequired,
  width: PropTypes.string.isRequired,
};

const Menu = (props) => {
  const target = document.getElementById(`${props.selectProps.id}-container`);
  return (
    <Overlay
      show
      placement="bottom"
      target={target}
      container={document.getElementById('root')}
    >
      <Dropdown width={target.offsetWidth - 10}>
        <div className="filter-select__custom-option" {...props.innerProps}>
          {props.children}
        </div>
      </Dropdown>
    </Overlay>
  );
};

Menu.propTypes = {
  children: PropTypes.element.isRequired,
  innerProps: PropTypes.shape({}).isRequired,
  selectProps: PropTypes.shape({
    id: PropTypes.string.isRequired,
  }).isRequired,
};

const Option = props => (
  <components.Option {...props}>
    <div className="d-flex flex-row align-items-center">
      {props.isMulti &&
        <input
          type="checkbox"
          checked={props.isSelected}
          className="mr-1"
        />
      }
      {props.data.label}
    </div>
  </components.Option>
);

Option.propTypes = {
  isMulti: PropTypes.bool.isRequired,
  isSelected: PropTypes.bool.isRequired,
  data: PropTypes.shape({
    label: PropTypes.string.isRequired,
  }).isRequired,
};

const IndicatorsContainer = ({ children, ...props }) => (
  <components.IndicatorsContainer {...props}>
    {props.selectProps.isMulti &&
      <div className="d-flex flex-column justify-content-center align-items-center selected-count-indicator-container">
        <div className="selected-count-indicator-inner">
          {props.selectProps.value ? props.selectProps.value.length : 0}
        </div>
      </div>
    }
    {children}
  </components.IndicatorsContainer>
);

IndicatorsContainer.propTypes = {
  children: PropTypes.element.isRequired,
  selectProps: PropTypes.shape({
    isMulti: PropTypes.bool.isRequired,
    value: PropTypes.array.isRequired,
  }).isRequired,
  width: PropTypes.string.isRequired,
};

const FilterSelectField = (props) => {
  const renderInput = ({ className, ...attributes }) => (
    <Select
      name={attributes.id}
      {...attributes}
      className={`filter-select ${className}`}
      classNamePrefix="filter-select"
      closeMenuOnSelect={false}
      hideSelectedOptions={false}
      controlShouldRenderValue={!attributes.multi}
      customSelectComponents={{
        Menu,
        Option,
        IndicatorsContainer,
      }}
    />
  );

  return (
    <BaseField
      {...props}
      renderInput={renderInput}
    />
  );
};

export default FilterSelectField;

FilterSelectField.propTypes = {
  className: PropTypes.string,
};

FilterSelectField.defaultProps = {
  className: '',
};
