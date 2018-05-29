import _ from 'lodash';
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Field } from 'redux-form';

import { renderField } from '../../utils/form-utils';

class BaseField extends Component {
  constructor(props) {
    super(props);

    this.state = {
      touched: false,
    };
  }

  shouldComponentUpdate(nextProps, nextState) {
    if (this.state.touched !== nextState.touched) {
      return true;
    }

    return !_.isEqualWith(this.props, nextProps, (objValue, othValue) => {
      if (typeof objValue === 'function' || typeof othValue === 'function') {
        return true;
      }

      return undefined;
    });
  }

  render() {
    const {
      fieldName,
      fieldConfig: { label, getDynamicAttr, attributes = {} },
      renderInput,
      arrayField,
      ...otherProps
    } = this.props;
    const dynamicAttr = getDynamicAttr ? getDynamicAttr(otherProps) : {};

    return (
      <Field
        name={fieldName}
        component={renderField}
        renderInput={renderInput}
        attributes={{
          ...attributes,
          ...dynamicAttr,
          onBlur: () => this.setState({ touched: true }),
        }}
        label={label}
        touched={this.state.touched}
        arrayField={arrayField}
      />
    );
  }
}

export default BaseField;

BaseField.propTypes = {
  fieldName: PropTypes.string.isRequired,
  fieldConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
  renderInput: PropTypes.func.isRequired,
  arrayField: PropTypes.bool,
};

BaseField.defaultProps = {
  arrayField: false,
};
