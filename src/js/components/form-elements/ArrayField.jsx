import _ from 'lodash';
import React from 'react';
import PropTypes from 'prop-types';
import { FieldArray } from 'redux-form';

import { renderFormField } from '../../utils/form-utils';

const ArrayField = (props) => {
  const { fieldName, fieldConfig, ...otherProps } = props;

  const renderFieldArray = ({ fieldsConfig, properties, fields }) => {
    const AddButton = fieldsConfig.addButton;
    const addRow = (row = {}) => fields.push(row);

    return (
      <div>
        <table className="table table-striped">
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
                  <td key={`${field}.${name}`}>
                    { renderFormField(config, `${field}.${name}`, {
                      ...properties,
                      arrayField: true,
                      addRow,
                      removeRow: () => fields.remove(index),
                    })}
                  </td>
              )) }
              </tr>))}
          </tbody>
        </table>
        <div className="text-center">
          {
            typeof AddButton === 'string' ?
              <button type="button" className="btn btn-outline-success margin-bottom-lg" onClick={() => addRow()}>
                {AddButton}
              </button>
              : <AddButton addRow={addRow} />
          }
        </div>
      </div>
    );
  };

  renderFieldArray.propTypes = {
    fieldsConfig: PropTypes.shape({}).isRequired,
    fields: PropTypes.oneOfType([
      PropTypes.shape({}),
      PropTypes.arrayOf(PropTypes.shape({})),
    ]).isRequired,
    properties: PropTypes.shape({}),
  };

  renderFieldArray.defaultProps = {
    properties: {},
  };

  return (
    <FieldArray
      key={fieldName}
      name={fieldName}
      component={renderFieldArray}
      fieldsConfig={fieldConfig}
      properties={otherProps}
    />
  );
};

export default ArrayField;

ArrayField.propTypes = {
  fieldName: PropTypes.string.isRequired,
  fieldConfig: PropTypes.shape({
    getDynamicAttr: PropTypes.func,
    fields: PropTypes.shape({}),
  }).isRequired,
};
