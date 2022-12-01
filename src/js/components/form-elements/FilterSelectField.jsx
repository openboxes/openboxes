import React from 'react';

import PropTypes from 'prop-types';
import { Overlay } from 'react-overlays';
import { components } from 'react-select';

import BaseField from 'components/form-elements/BaseField';
import Select from 'utils/Select';

import 'components/form-elements/FilterSelectField.scss';

const Dropdown = ({ children, style, width }) => (
  <div
    className="filter-select__dropdown"
    style={{
      ...style, width,
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
      <Dropdown width={(target.offsetWidth - 10).toString()}>
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

IndicatorsContainer.defaultProps = {
  width: undefined,
};

IndicatorsContainer.propTypes = {
  children: PropTypes.oneOfType([
    PropTypes.arrayOf(PropTypes.node),
    PropTypes.node,
  ]).isRequired,
  selectProps: PropTypes.shape({
    isMulti: PropTypes.bool.isRequired,
    value: PropTypes.oneOfType([
      PropTypes.array,
      PropTypes.shape({}),
    ]),
  }).isRequired,
  width: PropTypes.string,
};

const FilterSelectField = (props) => {
  const renderInput = ({ className, ...attributes }) => (
    <Select
      name={attributes.id}
      {...attributes}
      className={`filter-select ${className}`}
      classNamePrefix="filter-select"
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
