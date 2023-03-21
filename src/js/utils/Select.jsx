import React, { Component } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { Overlay } from 'react-overlays';
import { connect } from 'react-redux';
import ReactSelect, { Async, components } from 'react-select';
import { Tooltip } from 'react-tippy';

import Translate, { translateWithDefaultMessage } from 'utils/Translate';

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
              <span><i className="fa fa-plus pr-2" /><Translate id={props.selectProps.createNewFromModalLabel} defaultMessage={props.selectProps.defaultMessage} /></span>
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
    defaultMessage: PropTypes.string.isRequired,
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
      sortedOptionsByChecked: null,
    };

    this.handleChange = this.handleChange.bind(this);
    this.getTooltipHtml = this.getTooltipHtml.bind(this);
  }
  getTooltipHtml() {
    const {
      multi, placeholder, showLabelTooltip, value, defaultPlaceholder, labelKey,
    } = this.props;

    if (value?.displayNames?.default || value?.displayName) {
      return (
        <div className="p-1">
          {value?.name}
        </div>);
    }

    if (showLabelTooltip) {
      const valueMapped = multi && value ?
        this.props.value.map(v => v?.[labelKey] ?? v?.label) : [];
      const valueLabel = multi ? valueMapped.join(', ') : (value.label || value.name);
      return (
        <div className="p-1">
          {`${this.props.translate(placeholder, defaultPlaceholder ?? placeholder)}${valueLabel ? `: ${valueLabel}` : ''}`}
        </div>
      );
    }

    return (value && <div className="p-1">{value.label ?? value?.name}</div>);
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

  sortOptionsByChecked(options, checkedValues) {
    const groupByChecked = () => {
      if (options?.length) {
        return options.reduce((acc, curr) => {
          // If checked values contain current option, add it to checked options
          if (checkedValues?.some(val => val.id === curr.id)) {
            return {
              ...acc,
              checked: [...acc.checked, curr],
            };
          }
          // If checked values don't contain current option, add it to unchecked
          return {
            ...acc,
            unchecked: [...acc.unchecked, curr],
          };
        }, { checked: [], unchecked: [] });
      }
      return { checked: [], unchecked: [] };
    };
    const { checked, unchecked } = groupByChecked();
    // Concat checked and unchecked options in order: checked, unchecked, so checked are at the top
    this.setState({
      sortedOptionsByChecked: [...checked, ...unchecked],
    });
  }


  render() {
    const {
      options: selectOptions, value: selectValue = this.state.value,
      multi = false, delimiter = ';', async = false, showValueTooltip, showLabelTooltip,
      clearable = true, arrowLeft, arrowUp, arrowRight, arrowDown, fieldRef, onTabPress,
      onEnterPress, customSelectComponents, optionRenderer, classNamePrefix,
      showSelectedOptionColor, ...attributes
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
          this.props.valueRenderer({ ...props.data, showSelectedOptionColor })
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

    const getPlaceholder = () => {
      if (attributes.placeholder) {
        return (
          <Translate
            id={attributes.placeholder}
            defaultMessage={attributes.defaultPlaceholder ?? attributes.placeholder}
          />
        );
      }
      return null;
    };


    /* We would like to see the tooltip when an item
      has translatedName or when the showLabelTooltip
      or showValueTooltip are truthy. We return false
      when at least one property is true, because we need
      an information when the tooltip should be disabled.
     */
    const isTooltipDisabled = () => {
      const { value: fieldValue } = this.props;

      if (fieldValue?.displayName || showLabelTooltip) {
        return false;
      }

      return !(showValueTooltip && fieldValue);
    };

    return (
      <div id={`${this.state.id}-container`}>
        <Tooltip
          html={this.getTooltipHtml()}
          disabled={isTooltipDisabled()}
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
            placeholder={getPlaceholder()}
            isDisabled={attributes.disabled}
            options={(value?.length && this.state.sortedOptionsByChecked) || options}
            isMulti={multi}
            isClearable={clearable}
            title=""
            delimiter={delimiter}
            onMenuClose={() => {
              if (multi) this.sortOptionsByChecked(options, value);
              if (this.props.onMenuClose) this.props.onMenuClose();
            }}
            value={value}
            onChange={this.handleChange}
            components={{
              ...customSelectComponents,
              Menu: customSelectComponents.Menu ?? Menu,
              Option: customSelectComponents.Option ?? Option,
              SingleValue: customSelectComponents.SingleValue ?? SingleValue,
            }}
            ref={fieldRef}
            classNamePrefix={classNamePrefix}
            loadingMessage={() => this.props.translate('react.default.loading.label', 'Loading...')}
            noOptionsMessage={() => (async ?
              this.props.translate('react.default.select.noResultsFound.label', 'No results found') :
              this.props.translate('react.default.select.typeToSearch.label', 'Type to search'))
          }
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

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});

export default connect(mapStateToProps)(Select);

Select.propTypes = {
  options: PropTypes.arrayOf(PropTypes.oneOfType([PropTypes.string,
    PropTypes.shape({})])).isRequired,
  value: PropTypes.oneOfType([PropTypes.string,
    PropTypes.shape({}), PropTypes.any]),
  onChange: PropTypes.func,
  onMenuClose: PropTypes.func,
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
  translate: PropTypes.func.isRequired,
  defaultPlaceholder: PropTypes.string,
  showSelectedOptionColor: PropTypes.bool,
  labelKey: PropTypes.string,
};

Select.defaultProps = {
  value: undefined,
  onChange: null,
  onMenuClose: null,
  multi: false,
  clearable: true,
  async: false,
  delimiter: ';',
  placeholder: '',
  defaultPlaceholder: '',
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
  showSelectedOptionColor: false,
  customSelectComponents: {},
  classNamePrefix: 'react-select',
  labelKey: null,
};
