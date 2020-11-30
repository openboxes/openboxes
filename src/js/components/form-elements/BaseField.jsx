import _ from 'lodash';
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Field } from 'react-final-form';

import { renderField } from '../../utils/form-utils';

class BaseField extends Component {
  constructor(props) {
    super(props);

    this.state = {
      touched: false,
    };

    this.renderInput = this.renderInput.bind(this);
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

  renderInput(input, attr) {
    const onChange = (value) => {
      if (attr.onChange) {
        attr.onChange(value);
      }

      input.onChange(value);
    };

    const onBlur = (value) => {
      this.setState({ touched: true });

      if (attr.onBlur) {
        attr.onBlur(value);
      }
    };

    const attributes = {
      ...attr,
      value: input.value,
      onChange,
      onBlur,
    };

    return this.props.renderInput(attributes);
  }

  render() {
    const {
      fieldName,
      fieldConfig: {
        label, defaultMessage, getDynamicAttr, attributes = {},
      },
      arrayField, fieldValue, fieldRef, focusThis, arrowsNavigation,
      ...otherProps
    } = this.props;
    const dynamicAttr = getDynamicAttr ? getDynamicAttr({ ...otherProps, fieldValue }) : {};
    let attr = { ...attributes, ...dynamicAttr, fieldRef };

    if (arrowsNavigation) {
      const focusLeft = attr.focusLeft || otherProps.focusLeft;
      const focusRight = attr.focusRight || otherProps.focusRight;

      const arrowLeft = attr.arrowLeft ? attr.arrowLeft : () => {
        if (focusLeft) {
          otherProps.focusField(otherProps.rowIndex, focusLeft);
          return true;
        }
        return false;
      };
      const arrowRight = attr.arrowRight ? attr.arrowRight : () => {
        if (focusRight) {
          otherProps.focusField(otherProps.rowIndex, focusRight);
          return true;
        }
        return false;
      };
      const arrowUp = attr.arrowUp ? attr.arrowUp : () => {
        if (otherProps.rowIndex > 0) {
          otherProps.focusField(otherProps.rowIndex - 1, focusThis);
          return true;
        }
        return false;
      };
      const arrowDown = attr.arrowDown ? attr.arrowDown : () => {
        if (otherProps.rowIndex < otherProps.rowCount - 1) {
          otherProps.focusField(otherProps.rowIndex + 1, focusThis);
          return true;
        }
        return false;
      };
      const copyDown = attr.copyDown ? attr.copyDown : () => {
        if (otherProps.rowIndex < otherProps.rowCount - 1) {
          otherProps.copyDown(otherProps.rowIndex + 1, focusThis);
          return true;
        }
        return false;
      };

      attr = {
        ...attr, arrowLeft, arrowRight, arrowUp, arrowDown, copyDown,
      };
    }

    return (
      <Field
        name={fieldName}
        component={renderField}
        renderInput={this.renderInput}
        attributes={attr}
        label={label}
        defaultMessage={defaultMessage}
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
  fieldValue: PropTypes.oneOfType([PropTypes.string,
    PropTypes.shape({}), PropTypes.any]),
  fieldRef: PropTypes.func,
  focusThis: PropTypes.string,
  arrowsNavigation: PropTypes.bool,
};

BaseField.defaultProps = {
  arrayField: false,
  fieldValue: undefined,
  fieldRef: null,
  focusThis: null,
  arrowsNavigation: false,
};
