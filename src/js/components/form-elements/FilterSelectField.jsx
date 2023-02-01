import React, { useRef } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { Overlay } from 'react-overlays';
import { components } from 'react-select';

import BaseField from 'components/form-elements/BaseField';
import Select from 'utils/Select';

import 'components/form-elements/FilterSelectField.scss';

const Dropdown = ({
  children, style, inputContainerRec,
}) => {
  const dropdownRef = useRef(null);
  // if current dropdown width is smaller than the input container
  // then change dropdown width to the same width as the input container
  const currentDropdownWidth = dropdownRef.current?.offsetWidth;
  const inputContainerWidth = inputContainerRec?.width;
  const dropdownWidth = inputContainerWidth > currentDropdownWidth
    ? inputContainerWidth
    : currentDropdownWidth;
  return (
    <div
      ref={dropdownRef}
      className="filter-select__dropdown"
      style={{
        ...style,
        width: dropdownWidth,
        left: inputContainerRec.left,
        top: inputContainerRec?.bottom,
      }}
    >
      {children}
    </div>
  );
};

Dropdown.propTypes = {
  children: PropTypes.element.isRequired,
  style: PropTypes.shape({}),
  inputContainerRec: PropTypes.shape({
    height: PropTypes.number.isRequired,
    width: PropTypes.number.isRequired,
    bottom: PropTypes.number.isRequired,
    top: PropTypes.number.isRequired,
    right: PropTypes.number.isRequired,
    left: PropTypes.number.isRequired,
    x: PropTypes.number.isRequired,
    y: PropTypes.number.isRequired,
  }).isRequired,
};

Dropdown.defaultProps = {
  style: {},
};

const Menu = (props) => {
  const inputContainer = document.getElementById(`${props.selectProps.id}-container`);
  return (
    <Overlay
      show
      placement="bottom"
      target={inputContainer}
      container={document.getElementById('root')}
    >
      <Dropdown inputContainerRec={inputContainer.getBoundingClientRect()} >
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
      <span className="option-label">
        {props.data.label}
      </span>
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
    {props.selectProps.isMulti && props.selectProps.value?.length > 0 &&
      <div className="d-flex flex-column justify-content-center align-items-center selected-count-indicator-container">
        <div className="selected-count-indicator-inner">
          { props.selectProps.value.length }
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
      className={`filter-select ${!_.isEmpty(attributes?.value) ? 'filter-select-has-value' : ''} ${className}`}
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
