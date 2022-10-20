import React, { Component } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { Overlay } from 'react-overlays';
import ReactSelect, { Async, components } from 'react-select';
import { Tooltip } from 'react-tippy';

import 'react-tippy/dist/tippy.css';

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
      <Dropdown width={target.offsetWidth.toString()}>
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

class Select extends Component {
  constructor(props) {
    super(props);

    this.state = {
      value: props.initialValue === undefined ? null : props.initialValue,
      id: _.uniqueId('select-id_'),
    };

    this.handleChange = this.handleChange.bind(this);
    this.getTooltipHtml = this.getTooltipHtml.bind(this);
  }

  getTooltipHtml() {
    const {
      multi, placeholder, showLabelTooltip, value,
    } = this.props;

    if (showLabelTooltip) {
      const valueMapped = multi && value ? this.props.value.map(v => v && v.label) : [];
      const valueLabel = multi ? valueMapped.join(', ') : (value.label || value.name);
      return (
        <div className="p-1">
          {`${placeholder}${valueLabel ? `: ${valueLabel}` : ''}`}
        </div>
      );
    }

    return (value && <div className="p-1">{value.label}</div>);
  }

  handleChange(value) {
    if (value !== null && value !== undefined) {
      this.props.onChange(value);
      this.setState({ value });
    } else {
      this.props.onChange(null);
      this.setState({ value: null });
    }
  }

  render() {
    const {
      options: selectOptions, value: selectValue = this.state.value,
      multi = false, delimiter = ';', async = false, showValueTooltip, showLabelTooltip,
      clearable = true, arrowLeft, arrowUp, arrowRight, arrowDown, fieldRef, onTabPress,
      onEnterPress, customSelectComponents, optionRenderer, classNamePrefix, ...attributes
    } = this.props;
    const { formatValue, className, showLabel = false } = attributes;

    const mapOptions = vals => (_.map(vals, (value) => {
      let option = value;

      if (typeof value === 'string') {
        return { value, label: value };
      }

      if (value && attributes.valueKey && !option.value) {
        option = { ...option, value: option[attributes.valueKey] };
      }

      if (value && attributes.labelKey && !option.label) {
        option = { ...option, label: option[attributes.labelKey] };
      }

      if (option.options) {
        option = { ...option, options: mapOptions(option.options) };
      }

      return option;
    }));

    const options = mapOptions(selectOptions);

    let value = selectValue || null;

    if (selectValue && typeof selectValue === 'string') {
      const selectedOption = _.find(options, o => o.value === selectValue);
      value = { value: selectValue, label: selectedOption ? selectedOption.label : '' };
    }

    if (!multi) {
      if (value && attributes.valueKey && !value.value) {
        value = { ...value, value: value[attributes.valueKey] };
      }

      if (value && attributes.labelKey && !value.label) {
        value = { ...value, label: value[attributes.labelKey] };
      }
    }

    const SelectType = async ? Async : ReactSelect;

    const SingleValue = props => (
      <components.SingleValue {...props}>
        {this.props.valueRenderer ? (
          this.props.valueRenderer(props.data)
        ) : (
          <div>{props.data.label}</div>
        )}
      </components.SingleValue>
    );

    if (attributes.disabled && this.props.value && showLabel) {
      // eslint-disable-next-line no-nested-ternary
      const formattedValue = formatValue ? formatValue(this.props.value) :
        (this.props.value.label ? this.props.value.label : this.props.value);
      return (
        <div id={`${this.state.id}-container`}>
          <Tooltip
            html={this.props.value.label}
            disabled={!showValueTooltip}
            theme="transparent"
            delay="150"
            duration="250"
            hideDelay="50"
          >
            <div title="" className={`font-size-xs text-truncate ${className}`}>
              {formattedValue}
            </div>
          </Tooltip>
        </div>
      );
    }
    return (
      <div id={`${this.state.id}-container`}>
        <Tooltip
          html={this.getTooltipHtml()}
          disabled={!showLabelTooltip || (showValueTooltip && this.props.value)}
          theme="transparent"
          arrow="true"
          delay="150"
          duration="250"
          hideDelay="50"
          title=" "
          classes=""
        >
          <SelectType
            {...attributes}
            isDisabled={attributes.disabled}
            options={options}
            isMulti={multi}
            isClearable={clearable}
            title=""
            delimiter={delimiter}
            value={value}
            onChange={this.handleChange}
            components={{
              ...customSelectComponents,
              Menu: customSelectComponents.Menu ?? Menu,
              Option: customSelectComponents.Option ?? Option,
              SingleValue,
            }}
            ref={fieldRef}
            classNamePrefix={classNamePrefix}
            noOptionsMessage={() => (async ? 'Type to search' : 'No results found')}
            onKeyDown={(event) => {
              switch (event.keyCode) {
                case 37: /* arrow left */
                  if (arrowLeft) {
                    arrowLeft();
                    event.preventDefault();
                    event.stopPropagation();
                  }
                  break;
                case 38: /* arrow up */
                  if (arrowUp) {
                    arrowUp();
                    event.preventDefault();
                    event.stopPropagation();
                  }
                  break;
                case 39: /* arrow right */
                  if (arrowRight) {
                    arrowRight();
                    event.preventDefault();
                    event.stopPropagation();
                  }
                  break;
                case 40: /* arrow down */
                  if (arrowDown) {
                    arrowDown();
                    event.preventDefault();
                    event.stopPropagation();
                  }
                  break;
                case 9: /* Tab key */
                  if (onTabPress) {
                    onTabPress(event);
                  }
                  break;
                case 13: /* Enter key */
                  if (onEnterPress) {
                    onEnterPress(event);
                  }
                  break;
                default:
              }
            }}
            name={this.state.id}
            id={this.state.id}
            optionRenderer={optionRenderer}
          />
        </Tooltip>
      </div>
    );
  }
}

export default Select;

Select.propTypes = {
  options: PropTypes.arrayOf(PropTypes.oneOfType([PropTypes.string,
    PropTypes.shape({})])).isRequired,
  value: PropTypes.oneOfType([PropTypes.string,
    PropTypes.shape({}), PropTypes.any]),
  onChange: PropTypes.func,
  multi: PropTypes.bool,
  clearable: PropTypes.bool,
  async: PropTypes.bool,
  delimiter: PropTypes.string,
  showValueTooltip: PropTypes.bool,
  showLabelTooltip: PropTypes.bool,
  placeholder: PropTypes.string,
  initialValue: PropTypes.oneOfType([PropTypes.string,
    PropTypes.shape({}), PropTypes.any]),
  arrowLeft: PropTypes.func,
  arrowUp: PropTypes.func,
  arrowRight: PropTypes.func,
  arrowDown: PropTypes.func,
  fieldRef: PropTypes.func,
  onTabPress: PropTypes.func,
  onEnterPress: PropTypes.func,
  optionRenderer: PropTypes.func,
  valueRenderer: PropTypes.func,
  customSelectComponents: PropTypes.shape({}),
  classNamePrefix: PropTypes.string,
};

Select.defaultProps = {
  value: undefined,
  onChange: null,
  multi: false,
  clearable: true,
  async: false,
  delimiter: ';',
  placeholder: '',
  initialValue: null,
  showValueTooltip: false,
  showLabelTooltip: false,
  arrowLeft: null,
  arrowUp: null,
  arrowRight: null,
  arrowDown: null,
  fieldRef: null,
  onTabPress: null,
  onEnterPress: null,
  optionRenderer: null,
  valueRenderer: null,
  customSelectComponents: {},
  classNamePrefix: 'react-select',
};
