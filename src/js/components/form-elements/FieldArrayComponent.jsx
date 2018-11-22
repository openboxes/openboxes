import _ from 'lodash';
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Tooltip } from 'react-tippy';

import 'react-tippy/dist/tippy.css';
import { Translate } from 'react-localize-redux';

import TableBody from './TableBody';
import TableBodyVirtualized from './TableBodyVirtualized';

class FieldArrayComponent extends Component {
  constructor(props) {
    super(props);

    this.fieldRefs = [];
    this.focusField = this.focusField.bind(this);
  }

  focusField(index, fieldName) {
    const field = _.get(this.fieldRefs, `[${index}].${fieldName}`);

    if (field) {
      field.focus();
    }
  }

  render() {
    const { fieldsConfig, properties, fields } = this.props;
    const AddButton = fieldsConfig.addButton;
    const { maxTableHeight = 'calc(100vh - 400px)', virtualize } = fieldsConfig;
    const addRow = (row = {}) => fields.push(row);
    const TableBodyComponent = virtualize ? TableBodyVirtualized : TableBody;

    return (
      <div className="d-flex flex-column">
        <div className="text-center border">
          <div className="d-flex flex-row border-bottom font-weight-bold py-1">
            {_.map(fieldsConfig.fields, (config, name) => (
              <Tooltip
                html={(<div>{config.label}</div>)}
                theme="transparent"
                arrow="true"
                delay="150"
                style={{
                  flex: config.fixedWidth ? `0 1 ${config.fixedWidth}` : `${config.flexWidth || '12'} 1 0`,
                  minWidth: 0,
                }}
                duration="250"
                hideDelay="50"
              >
                <div
                  key={name}
                  className="mx-1 text-truncate font-size-xs"
                ><Translate id={config.label} />
                </div>
              </Tooltip>))}
          </div>
        </div>
        <div
          className="text-center border mb-1 flex-grow-1"
          style={{ overflowY: virtualize ? 'hidden' : 'scroll', maxHeight: virtualize ? '450px' : maxTableHeight }}
        >
          <TableBodyComponent
            fields={fields}
            properties={{
              ...properties, rowCount: fields.length || 0, focusField: this.focusField,
            }}
            addRow={addRow}
            fieldsConfig={fieldsConfig}
            tableRef={(el, fieldName, index) => {
              if (!this.fieldRefs[index]) {
                this.fieldRefs[index] = {};
              }

              this.fieldRefs[index][fieldName] = el;
            }}
          />
        </div>
        { AddButton &&
          <div className="text-center">
            {
              typeof AddButton === 'string' ?
                <button type="button" className="btn btn-outline-success btn-xs" onClick={() => addRow()}>
                  <Translate id={AddButton} />
                </button>
                : <AddButton {...properties} addRow={addRow} />
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
