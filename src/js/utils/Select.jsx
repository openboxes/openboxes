import React, { Component } from 'react';
import ReactSelect from 'react-select';
import PropTypes from 'prop-types';

class Select extends Component {
  constructor(props) {
    super(props);

    this.state = {
      value: null,
    };

    this.handleChange = this.handleChange.bind(this);
  }

  handleChange(value) {
    this.setState({ value });

    if (this.props.onChange) {
      this.props.onChange(value);
    }
  }

  render() {
    return (
      <ReactSelect
        value={this.state.value}
        {...this.props}
        onChange={this.handleChange}
      />
    );
  }
}

export default Select;

Select.propTypes = {
  onChange: PropTypes.func,
};

Select.defaultProps = {
  onChange: null,
};
