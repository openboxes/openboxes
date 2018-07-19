import _ from 'lodash';
import React, { Component } from 'react';
import ReactSelect, { Async } from 'react-select-plus';
import { Overlay } from 'react-overlays';
import PropTypes from 'prop-types';

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
      this.setState({ value: val });
    } else {
      this.props.onChange(null);
      this.setState({ value: null });
    }
  }

  render() {
    const {
      options: selectOptions, value: selectValue = this.state.value,
      objectValue = false, multi = false, delimiter = ';', async = false, ...attributes
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

    return (
      <div id={`${this.state.id}-container`}>
        <SelectType
          name={this.state.id}
          {...attributes}
          options={options}
          delimiter={delimiter}
          value={multi ? _.join(value, delimiter) : value}
          onChange={this.handleChange}
          dropdownComponent={dropdownComponent}
        />
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
  initialValue: PropTypes.oneOfType([PropTypes.string,
    PropTypes.shape({}), PropTypes.any]),
};

Select.defaultProps = {
  value: undefined,
  onChange: null,
  objectValue: false,
  multi: false,
  async: false,
  delimiter: ';',
  initialValue: null,
};
