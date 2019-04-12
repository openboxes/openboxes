import _ from 'lodash';
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { FieldArray } from 'react-final-form-arrays';

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
    const dynamicAttr = fieldsConfig.getDynamicRowAttr ?
      fieldsConfig.getDynamicRowAttr({ ...properties, index, rowValues }) : {};
    const { subfieldKey } = fieldsConfig;

    return (
      <div>
        <TableRow {...this.props} />
        { !dynamicAttr.hideSubfields &&
          <FieldArray
            name={`${field}.${subfieldKey}`}
            component={TableBody}
            fieldsConfig={fieldsConfig}
            properties={{
              ...properties,
              parentIndex: index,
              subfield: true,
            }}
          />
        }
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
  rowValues: PropTypes.shape({}),
};

TableRowWithSubfields.defaultProps = {
  rowValues: {},
};
