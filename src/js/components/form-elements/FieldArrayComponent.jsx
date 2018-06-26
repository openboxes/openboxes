import _ from 'lodash';
import React, { Component } from 'react';
import PropTypes from 'prop-types';

import PickPageFieldArrayComponent from './PickPageFieldArrayComponent';
import TableBody from './TableBody';
import LineItemsRowKeyFieldArrayComponent from './LineItemsRowKeyFieldArrayComponent';

class FieldArrayComponent extends Component {
  shouldComponentUpdate(nextProps) {
    if (this.props.fields.length !== nextProps.fields.length) {
      return true;
    }

    return !_.isEqualWith(this.props.properties, nextProps.properties, (objValue, othValue) => {
      if (typeof objValue === 'function' || typeof othValue === 'function') {
        return true;
      }

      return undefined;
    });
  }

  render() {
    const { fieldsConfig, properties, fields } = this.props;
    const AddButton = fieldsConfig.addButton;
    const addRow = (row = {}) => fields.push(row);

    if (fieldsConfig.pickPage) {
      return (
        <PickPageFieldArrayComponent
          fieldsConfig={fieldsConfig}
          properties={properties}
          fields={fields}
        />
      );
    }

    if (fieldsConfig.lineItemsRowKey) {
      return (
        <LineItemsRowKeyFieldArrayComponent
          fieldsConfig={fieldsConfig}
          properties={properties}
          fields={fields}
        />
      );
    }

    return (
      <div>
        <div className="text-center border">
          <div className="d-flex flex-row border-bottom font-weight-bold py-2">
            { _.map(fieldsConfig.fields, (config, name) =>
              <div key={name} className="mx-1" style={{ flex: '1 1 0' }}>{config.label}</div>) }
          </div>
          <TableBody
            fields={fields}
            properties={properties}
            addRow={addRow}
            fieldsConfig={fieldsConfig}
          />
        </div>
        { AddButton &&
        <div className="text-center">
          {
            typeof AddButton === 'string' ?
              <button type="button" className="btn btn-outline-success margin-bottom-lg" onClick={() => addRow()}>
                {AddButton}
              </button>
              : <AddButton addRow={addRow} />
          }
        </div>
        }
      </div>
    );
  }
}

FieldArrayComponent.propTypes = {
  fieldsConfig: PropTypes.shape({}).isRequired,
  fields: PropTypes.oneOfType([
    PropTypes.shape({}),
    PropTypes.arrayOf(PropTypes.shape({})),
  ]).isRequired,
  properties: PropTypes.shape({}),
};

FieldArrayComponent.defaultProps = {
  properties: {},
};

export default FieldArrayComponent;
