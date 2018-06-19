import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { formValueSelector } from 'redux-form';
import PropTypes from 'prop-types';

const withValueSelector = ({
  fieldName,
  fieldConfig: {
    component: FieldComponent,
    componentConfig = {},
    label,
    getDynamicAttr,
    attributes = {},
  }, ...props
}) => {
  const dynamicAttr = getDynamicAttr ? getDynamicAttr(props) : {};
  const attr = { ...attributes, ...dynamicAttr };
  const { formName, field } = attr;
  const selector = formValueSelector(formName);

  return connect(state => ({
    fieldName,
    fieldConfig: { label, ...componentConfig },
    ...props,
    selectedValue: selector(state, field),
  }))(FieldComponent);
};

class ValueSelectorField extends Component {
  shouldComponentUpdate(nextProps) {
    return !_.isEqualWith(this.props, nextProps, (objValue, othValue) => {
      if (typeof objValue === 'function' || typeof othValue === 'function') {
        return true;
      }

      return undefined;
    });
  }

  render() {
    const SelectorComponent = withValueSelector(this.props);

    return (
      <SelectorComponent />
    );
  }
}

export default ValueSelectorField;

withValueSelector.propTypes = {
  fieldName: PropTypes.string.isRequired,
  fieldConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
};
