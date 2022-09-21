import React from 'react';

import PropTypes from 'prop-types';
import { Overlay } from 'react-overlays';
import { components } from 'react-select';

import BaseField from 'components/form-elements/BaseField';
import Select from 'utils/Select';

const Dropdown = ({ children, style, width }) => (
  <div
    style={{
      ...style, position: 'absolute', zIndex: 9999, width, backgroundColor: 'white', border: '1px solid hsl(0deg 0% 80%)',
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
      <Dropdown width={target.offsetWidth}>
        <div className="custom-option" {...props.innerProps}>
          {props.selectProps.createNewFromModal &&
            <div
              className="add-new-button"
              onClick={props.selectProps.newOptionModalOpen}
              onKeyPress={props.selectProps.newOptionModalOpen}
              role="button"
              tabIndex={0}
            >
              <span><i className="fa fa-plus pr-2" />{props.selectProps.createNewFromModalLabel}</span>
            </div>
          }
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
    createNewFromModal: PropTypes.bool.isRequired,
    newOptionModalOpen: PropTypes.func.isRequired,
    createNewFromModalLabel: PropTypes.string.isRequired,
  }).isRequired,
};


const Option = props => (
  <components.Option {...props}>
    {props.selectProps.optionRenderer ? (
      props.selectProps.optionRenderer(props.data)
    ) : (
      <div>{props.data.label}</div>
    )}
  </components.Option>
);

Option.propTypes = {
  data: PropTypes.shape({
    label: PropTypes.string.isRequired,
  }).isRequired,
  selectProps: PropTypes.shape({
    optionRenderer: PropTypes.func,
  }).isRequired,
};

const SelectField = (props) => {
  // eslint-disable-next-line react/prop-types
  const renderInput = ({ className, ...attributes }) => (
    <Select
      name={attributes.id}
      {...attributes}
      className={`select-xs ${className}`}
      classNamePrefix="react-select"
      customSelectComponents={{
        Menu,
        Option,
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

export default SelectField;
