import React, { Component } from 'react';
import PropTypes from 'prop-types';

import BaseField from './BaseField';

class BaseReactField extends Component {
  constructor(props) {
    super(props);

    this.state = {
      touched: false,
    };

    this.renderInput = this.renderInput.bind(this);
  }

  renderInput(input, attr) {
    const onChange = (value) => {
      if (attr.onChange) {
        attr.onChange(value);
      }

      input.onChange(value);
    };
    const attributes = {
      ...attr,
      onChange,
      value: input.value,
      onBlur: () => this.setState({ touched: true }),
    };

    return this.props.renderInput(attributes);
  }

  render() {
    return (
      <BaseField
        {...this.props}
        renderInput={this.renderInput}
        touched={this.state.touched}
      />
    );
  }
}

export default BaseReactField;

BaseReactField.propTypes = {
  renderInput: PropTypes.func.isRequired,
};
