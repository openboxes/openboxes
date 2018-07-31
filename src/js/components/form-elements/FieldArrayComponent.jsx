import _ from 'lodash';
import React from 'react';
import PropTypes from 'prop-types';

import TableBody from './TableBody';
import TableBodyVirtualized from './TableBodyVirtualized';

const FieldArrayComponent = (props) => {
  const { fieldsConfig, properties, fields } = props;
  const AddButton = fieldsConfig.addButton;
  const addRow = (row = {}) => fields.push(row);
  const TableBodyComponent = fieldsConfig.disableVirtualization ? TableBody : TableBodyVirtualized;

  return (
    <div>
      <div className="text-center border mb-2">
        <div className="d-flex flex-row border-bottom font-weight-bold py-2">
          { _.map(fieldsConfig.fields, (config, name) => (
            <div
              key={name}
              className="mx-1 text-truncate"
              style={{ flex: config.fixedWidth ? `0 1 ${config.fixedWidth}` : `${config.flexWidth || '12'} 1 0`, minWidth: 0 }}
            >{config.label}
            </div>)) }
        </div>
        <TableBodyComponent
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
