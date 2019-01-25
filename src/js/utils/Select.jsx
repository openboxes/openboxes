import _ from 'lodash';
import React, { Component } from 'react';
import ReactSelect, { Async } from 'react-select-plus';
import { Overlay } from 'react-overlays';
import PropTypes from 'prop-types';
import { Tooltip } from 'react-tippy';

import 'react-select-plus/dist/react-select-plus.css';
import 'react-tippy/dist/tippy.css';

// eslint-disable-next-line react/prop-types
const Dropdown = ({ children, style, width }) => (
  <div
    style={{
      ...style, position: 'absolute', zIndex: 9999, width,
    }}
  >
    {children}
  </div>
);

class Select extends Component {
  constructor(props) {
    super(props);

    this.state = {
      value: props.initialValue === undefined ? null : props.initialValue,
      id: _.uniqueId('select-id_'),
    };

    this.handleChange = this.handleChange.bind(this);
  }

  handleChange(value) {
    if (this.props.multi) {
      const values = _.map(value, val =>
        (this.props.objectValue ? JSON.parse(val.value) : val.value));
      this.setState({ value: values });
      this.props.onChange(values);
    } else if (value !== null && value !== undefined) {
      const val = this.props.objectValue ? JSON.parse(value.value) : value.value;
      this.props.onChange(val);
      this.setState({ label: value.label, value: val });
    } else {
      this.props.onChange(null);
      this.setState({ value: null, label: null });
    }
  }

  render() {
    const {
      options: selectOptions, value: selectValue = this.state.value,
      objectValue = false, multi = false, delimiter = ';', async = false, showValueTooltip,
      arrowLeft, arrowUp, arrowRight, arrowDown, fieldRef, onTabPress, onEnterPress, ...attributes
    } = this.props;

    const options = _.map(selectOptions, (value) => {
      if (typeof value === 'string') {
        return { value, label: value };
      } else if (objectValue) {
        return { ...value, value: JSON.stringify(value.value) };
      }

      return value;
    });

    let value = selectValue;

    if (objectValue) {
      value = multi ? _.map(selectValue, val => JSON.stringify(val))
        : JSON.stringify(selectValue);
    }

    const SelectType = async ? Async : ReactSelect;

    // eslint-disable-next-line react/prop-types
    const dropdownComponent = ({ children }) => {
      const target = document.getElementById(`${this.state.id}-container`);
      return (
        <Overlay
          show
          placement="bottom"
          target={target}
          container={document.getElementById('root')}
        >
          <Dropdown width={target.offsetWidth}>
            {children}
          </Dropdown>
        </Overlay>
      );
    };

    const selectValueDiv = document.querySelector(`#${this.state.id}-container div.Select-value`);
    return (
      <div id={`${this.state.id}-container`}>
        <Tooltip
          html={(<div>{this.state.label}</div>)}
          disabled={!showValueTooltip
            || _.isNull(selectValueDiv)
            || selectValueDiv.scrollWidth <= selectValueDiv.offsetWidth}
          theme="transparent"
          arrow="true"
          delay="150"
          duration="250"
          hideDelay="50"
          title=" "
        >
          <SelectType
            name={this.state.id}
            {...attributes}
            options={options}
            multi={multi}
            delimiter={delimiter}
            value={multi ? _.join(value, delimiter) : value}
            onChange={this.handleChange}
            dropdownComponent={dropdownComponent}
            ref={fieldRef}
            inputProps={{
              onKeyDown: (event) => {
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
              },
            }}
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
  objectValue: PropTypes.bool,
  multi: PropTypes.bool,
  async: PropTypes.bool,
  delimiter: PropTypes.string,
  showValueTooltip: PropTypes.bool,
  initialValue: PropTypes.oneOfType([PropTypes.string,
    PropTypes.shape({}), PropTypes.any]),
  arrowLeft: PropTypes.func,
  arrowUp: PropTypes.func,
  arrowRight: PropTypes.func,
  arrowDown: PropTypes.func,
  fieldRef: PropTypes.func,
  onTabPress: PropTypes.func,
  onEnterPress: PropTypes.func,
};

Select.defaultProps = {
  value: undefined,
  onChange: null,
  objectValue: false,
  multi: false,
  async: false,
  delimiter: ';',
  initialValue: null,
  showValueTooltip: false,
  arrowLeft: null,
  arrowUp: null,
  arrowRight: null,
  arrowDown: null,
  fieldRef: null,
  onTabPress: null,
  onEnterPress: null,
};
