import _ from 'lodash';
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { FieldArray } from 'redux-form';

import TableBody from './TableBody';
import TableRow from './TableRow';

class TableRowWithSubfields extends Component {
  constructor(props) {
    super(props);

    this.state = {
      fieldPreview: _.isNil(props.fieldPreview) ? false : props.fieldPreview,
    };
  }

  componentWillReceiveProps(nextProps) {
    if (!nextProps.fieldPreview && this.state.fieldPreview) {
      this.setState({ fieldPreview: nextProps.fieldPreview });
    }
  }

  shouldComponentUpdate(nextProps, nextState) {
    if (this.state.fieldPreview !== nextState.fieldPreview) {
      return true;
    }

    return !_.isEqualWith(_.omit(this.props, 'fieldPreview'), _.omit(nextProps, 'fieldPreview'), (objValue, othValue) => {
      if (typeof objValue === 'function' || typeof othValue === 'function') {
        return true;
      }

      return undefined;
    });
  }

  render() {
    const {
      fieldsConfig, index, field, properties, rowValues = {},
    } = this.props;
    const dynamicAttr = fieldsConfig.getDynamicRowAttr ?
      fieldsConfig.getDynamicRowAttr({
        ...properties, index, rowValues, fieldPreview: this.state.fieldPreview,
      }) : {};
    const { subfieldKey } = fieldsConfig;

    return (
      <div>
        <TableRow {...this.props} />
        { !dynamicAttr.hideSubfields && (!this.state.fieldPreview ?
          <FieldArray
            name={`${field}.${subfieldKey}`}
            component={TableBody}
            fieldsConfig={fieldsConfig}
            properties={{
              ...properties,
              parentIndex: index,
              subfield: true,
            }}
          /> :
          _.map(_.get(rowValues, subfieldKey), (subfield, subfieldIndex) => (
            <TableRow
              // eslint-disable-next-line react/no-array-index-key
              key={`${index}-${subfieldIndex}`}
              field={`${field}.${subfieldKey}[${subfieldIndex}]`}
              index={subfieldIndex}
              properties={{
                ...properties,
                parentIndex: index,
                subfield: true,
              }}
              fieldPreview={this.state.fieldPreview}
              addRow={() => {}}
              fieldsConfig={fieldsConfig}
              removeRow={() => {}}
              rowValues={subfield}
            />))) }
      </div>
    );
  }
}

export default TableRowWithSubfields;

TableRowWithSubfields.propTypes = {
  fieldsConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
  }).isRequired,
  index: PropTypes.number.isRequired,
  field: PropTypes.string.isRequired,
  addRow: PropTypes.func.isRequired,
  removeRow: PropTypes.func.isRequired,
  properties: PropTypes.shape({}).isRequired,
  fieldPreview: PropTypes.bool,
  rowValues: PropTypes.shape({}),
};

TableRowWithSubfields.defaultProps = {
  fieldPreview: false,
  rowValues: {},
};
