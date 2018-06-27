import _ from 'lodash';
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { FieldArray } from 'redux-form';

import TableBody from './TableBody';
import TableRow from './TableRow';

class TableRowWithSubfields extends Component {
  shouldComponentUpdate(nextProps) {
    return !_.isEqualWith(this.props, nextProps, (objValue, othValue) => {
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
    const { subfieldKey } = fieldsConfig;

    return (
      <div>
        <TableRow {...this.props} />
        { !properties.fieldPreview ?
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
              addRow={() => {}}
              fieldsConfig={fieldsConfig}
              removeRow={() => {}}
              rowValues={subfield}
            />)) }
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
