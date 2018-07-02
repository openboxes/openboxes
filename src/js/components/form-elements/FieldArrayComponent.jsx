import _ from 'lodash';
import React from 'react';
import PropTypes from 'prop-types';

import PickPageFieldArrayComponent from './PickPageFieldArrayComponent';
import TableBody from './TableBody';

const FieldArrayComponent = (props) => {
  const { fieldsConfig, properties, fields } = props;
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
};

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
