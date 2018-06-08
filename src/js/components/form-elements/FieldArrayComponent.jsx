import _ from 'lodash';
import React, { Component } from 'react';
import PropTypes from 'prop-types';

import { renderFormField } from '../../utils/form-utils';

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

    return (
      <div>
        <table className="table table-striped text-center">
          <thead>
            <tr>
              { _.map(fieldsConfig.fields, (config, name) =>
                <th key={name}>{config.label}</th>) }
            </tr>
          </thead>
          <tbody>
            {fields.map((field, index) => (
            // eslint-disable-next-line react/no-array-index-key
              <tr key={index}>
                { _.map(fieldsConfig.fields, (config, name) => (
                  <td key={`${field}.${name}`} className="align-middle">
                    { renderFormField(config, `${field}.${name}`, {
                    ...properties,
                    arrayField: true,
                    addRow,
                    removeRow: () => fields.remove(index),
                    rowIndex: index,
                  })}
                  </td>
              )) }
              </tr>))}
          </tbody>
        </table>
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
