import _ from 'lodash';
import React from 'react';
import PropTypes from 'prop-types';

import TableBody from './TableBody';
import TableBodyVirtualized from './TableBodyVirtualized';

const FieldArrayComponent = (props) => {
  const { fieldsConfig, properties, fields } = props;
  const AddButton = fieldsConfig.addButton;
  const { maxTableHeight = 'calc(100vh - 450px)', virtualize } = fieldsConfig;
  const addRow = (row = {}) => fields.push(row);
  const TableBodyComponent = virtualize ? TableBodyVirtualized : TableBody;

  return (
    <div className="d-flex flex-column">
      <div className="text-center border">
        <div className="d-flex flex-row border-bottom font-weight-bold py-2">
          { _.map(fieldsConfig.fields, (config, name) => (
            <div
              key={name}
              className="mx-1 text-truncate"
              style={{ flex: config.fixedWidth ? `0 1 ${config.fixedWidth}` : `${config.flexWidth || '12'} 1 0`, minWidth: 0 }}
            >{config.label}
            </div>)) }
        </div>
      </div>
      <div className="text-center border mb-2 flex-grow-1" style={{ overflowY: virtualize ? 'hidden' : 'scroll', maxHeight: virtualize ? '450px' : maxTableHeight }}>
        <TableBodyComponent
          fields={fields}
          properties={{ ...properties, rowCount: fields.length || 0 }}
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
              : <AddButton {...properties} addRow={addRow} />
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
