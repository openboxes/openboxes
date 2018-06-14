import _ from 'lodash';
import React, { Component } from 'react';
import PropTypes from 'prop-types';

import PickPageFieldArrayComponent from './PickPageFieldArrayComponent';
import TableRow from './TableRow';
import TableRowWithSelector from './TableRowWithSelector';

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
            {fields.map((field, index) => {
              if (fieldsConfig.rowWithSelector) {
                return (
                  <TableRowWithSelector
                    // eslint-disable-next-line react/no-array-index-key
                    key={index}
                    field={field}
                    index={index}
                    properties={properties}
                    addRow={addRow}
                    fieldsConfig={fieldsConfig}
                    removeRow={() => fields.remove(index)}
                  />
                );
              }
              return (
                <TableRow
                  // eslint-disable-next-line react/no-array-index-key
                  key={index}
                  field={field}
                  index={index}
                  properties={properties}
                  addRow={addRow}
                  fieldsConfig={fieldsConfig}
                  removeRow={() => fields.remove(index)}
                />
              );
            })}
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
