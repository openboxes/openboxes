import _ from 'lodash';
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Tooltip } from 'react-tippy';
import { connect } from 'react-redux';
import { getTranslate } from 'react-localize-redux';

import 'react-tippy/dist/tippy.css';

import TableBody from './TableBody';
import TableBodyVirtualized from './TableBodyVirtualized';
import Translate, { translateWithDefaultMessage } from '../../utils/Translate';

class FieldArrayComponent extends Component {
  constructor(props) {
    super(props);

    this.fieldRefs = [];
    this.focusField = this.focusField.bind(this);
    this.copyDown = this.copyDown.bind(this);
  }

  focusField(index, fieldName) {
    const field = _.get(this.fieldRefs, `[${index}].${fieldName}`);
    const fieldToScroll = _.get(this.fieldRefs, `[${index - 15 > 0 ? index - 15 : 0}].${fieldName}`);

    if (field) {
      field.focus();
      if (fieldToScroll) {
        fieldToScroll.scrollIntoView();
      }
    }
  }

  copyDown(index, fieldName) {
    const field = _.get(this.fieldRefs, `[${index}].${fieldName}`);
    const fieldToScroll = _.get(this.fieldRefs, `[${index - 15 > 0 ? index - 15 : 0}].${fieldName}`);
    const valueToCopy = _.get(this.fieldRefs, `[${index - 1}].${fieldName}.value`);

    if (field && valueToCopy && !field.disabled) {
      field.value = valueToCopy;
      field.focus();
      if (fieldToScroll) {
        fieldToScroll.scrollIntoView();
      }
    }
  }

  render() {
    const {
      fieldsConfig, properties, fields, isPaginated,
    } = this.props;
    const AddButton = fieldsConfig.addButton;
    const { maxTableHeight = 'calc(100vh - 400px)', virtualized } = fieldsConfig;
    const addRow = (row = {}, index = null) => {
      if (index === null) {
        fields.push(row);
      } else if (typeof fields === 'object') {
        fields.insert(index + 1, row);
      } else {
        fields.splice(index + 1, 0, row);
      }
    };
    const TableBodyComponent = virtualized && isPaginated ? TableBodyVirtualized : TableBody;

    return (
      <div className="d-flex flex-column">
        <div className="text-center border">
          <div className="d-flex flex-row border-bottom font-weight-bold py-1 mr-3">
            {_.map(fieldsConfig.fields, (config, name) => {
              const dynamicAttr = config.getDynamicAttr ? config.getDynamicAttr(properties) : {};
              const { hide } = dynamicAttr;
              if (!hide) {
                return (
                  <div
                    key={name}
                    className="mx-1"
                    style={{
                      flex: config.fixedWidth ? `0 1 ${config.fixedWidth}` : `${config.flexWidth || '12'} 1 0`,
                      minWidth: 0,
                      textAlign: config.headerAlign ? config.headerAlign : 'center',
                    }}
                  >
                    <Tooltip
                      html={(config.label &&
                        <div>{this.props.translate(config.label, config.defaultMessage)}</div>)}
                      theme="transparent"
                      arrow="true"
                      delay="150"
                      duration="250"
                      hideDelay="50"
                    >
                      <div
                        className={`mx-1 text-truncate font-size-xs ${config.required ? 'required' : ''}`}
                      >{config.label &&
                      <Translate id={config.label} defaultMessage={config.defaultMessage} />}
                      </div>
                    </Tooltip>
                  </div>);
              }
              return null;
})}
          </div>
        </div>
        <div
          className="text-center border mb-1 flex-grow-1"
          style={{ overflowY: virtualized && isPaginated ? 'hidden' : 'scroll', maxHeight: virtualized && isPaginated ? window.innerHeight - 450 : maxTableHeight }}
        >
          <TableBodyComponent
            fields={fields}
            properties={{
              ...properties, focusField: this.focusField, copyDown: this.copyDown,
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

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  isPaginated: state.session.isPaginated,
});

FieldArrayComponent.propTypes = {
  fieldsConfig: PropTypes.shape({}).isRequired,
  fields: PropTypes.oneOfType([
    PropTypes.shape({}),
    PropTypes.arrayOf(PropTypes.shape({})),
  ]).isRequired,
  properties: PropTypes.shape({}),
  translate: PropTypes.func.isRequired,
  /** Return true if pagination is enabled */
  isPaginated: PropTypes.bool.isRequired,
};

FieldArrayComponent.defaultProps = {
  properties: {},
};

export default connect(mapStateToProps)(FieldArrayComponent);
