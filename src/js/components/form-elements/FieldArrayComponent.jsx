import React, { Component } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import { Tooltip } from 'react-tippy';

import TableBody from 'components/form-elements/TableBody';
import TableBodyVirtualized from 'components/form-elements/TableBodyVirtualized';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-tippy/dist/tippy.css';

class FieldArrayComponent extends Component {
  constructor(props) {
    super(props);

    this.fieldRefs = [];
    this.focusField = this.focusField.bind(this);
    this.copyDown = this.copyDown.bind(this);
  }

  focusField(index, fieldName) {
    const field = _.get(this.fieldRefs, `[${index}].${fieldName}`);
    // 8 - the amount of rows shown in the table on 1360x786 resolution
    const fieldToScroll = _.get(this.fieldRefs, `[${index - 8 > 0 ? index - 8 : 0}].${fieldName}`);

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
    const {
      maxTableHeight, virtualized, overflowStyle = 'scroll', showRowSaveIndicator,
    } = fieldsConfig;
    const addRow = (row = {}, index = null, shouldScroll = true) => {
      if (index === null) {
        const table = document.querySelectorAll('[role="rowgroup"]')[0];
        // lines can also be added on modals and no scroll should be applied then
        if (table && shouldScroll) {
          table.scrollIntoView({ block: 'end' });
        }
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
        {/* Additional headers, that act as a 'colspan'. Dev should provide either
            fixedWith or flexWidth which will show which table columns should be
            grouped under a specific header. Total value of grouping headers width
            (fixed or flex) should be the same as it is on the table columns. */}
        {fieldsConfig.headerGroupings && (
          <div className="text-center border table-additional-header">
            <div className="d-flex flex-row border-bottom font-weight-bold">
              {_.map(fieldsConfig.headerGroupings, (config, name) => (
                <div
                  className="text-truncate font-size-xs"
                  key={name}
                  style={{
                    flex: config.fixedWidth ? `0 1 ${config.fixedWidth}` : `${config.flexWidth || '12'} 1 0`,
                    minWidth: 0,
                    textAlign: config.headerAlign ? config.headerAlign : 'center',
                  }}
                >
                  {config.label && <span className="w-100 mx-1"><Translate id={config.label} defaultMessage={config.defaultLabel} /></span>}
                </div>
              ))}
            </div>
          </div>
        )}
        <div className="text-center border table-header">
          <div className="d-flex flex-row border-bottom font-weight-bold">
            {_.map(fieldsConfig.fields, (config, name) => {
              const dynamicAttr = config.getDynamicAttr ? config.getDynamicAttr(properties) : {};
              const { hide, headerHtml } = dynamicAttr;
              const flexWidth = dynamicAttr.flexWidth || config.flexWidth;
              const fixedWidth = dynamicAttr.fixedWidth || config.fixedWidth;
              const headerLabel = dynamicAttr.label || config.label;
              const headerDefaultMessage = dynamicAttr.defaultMessage || config.defaultMessage;

              if (!hide) {
                return (
                  <div
                    key={name}
                    className={`${config.headerClassName ? config.headerClassName : ''}`}
                    style={{
                      flex: fixedWidth ? `0 1 ${fixedWidth}` : `${flexWidth || '12'} 1 0`,
                      minWidth: 0,
                      textAlign: config.headerAlign ? config.headerAlign : 'center',
                    }}
                  >
                    <Tooltip
                      html={config.headerTooltip ? (
                        <div>
                          {this.props.translate(config.headerTooltip, config.headerDefaultTooltip)}
                        </div>
                      ) : (headerLabel
                        && (
                        <div>
                          {this.props.translate(headerLabel, headerDefaultMessage)}
                        </div>
                        )
                      )}
                      theme="transparent"
                      arrow="true"
                      delay="150"
                      duration="250"
                      hideDelay="50"
                    >
                      <div
                        className={`mx-2 ${config.multilineHeader ? '' : 'text-truncate'} ${config.required ? 'arrayfield-header-required' : ''}`}
                        style={{
                          fontSize: fieldsConfig.headerFontSize ? fieldsConfig.headerFontSize : '0.875rem',
                        }}
                      >
                        { headerHtml && headerHtml() }
                        { headerLabel && !headerHtml
                          && <Translate id={headerLabel} defaultMessage={headerDefaultMessage} />}
                      </div>
                    </Tooltip>
                  </div>
                );
              }
              return null;
            })}
          </div>
        </div>
        <div
          className="text-center border mb-1 flex-grow-1 table-content"
          style={{ overflowX: 'hidden', overflowY: virtualized && isPaginated ? 'hidden' : overflowStyle, maxHeight: maxTableHeight }}
        >
          <TableBodyComponent
            fields={fields}
            properties={{
              ...properties,
              focusField: this.focusField,
              copyDown: this.copyDown,
              showRowSaveIndicator,
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
        { AddButton
          && (
          <div className="text-center add-button">
            {
              typeof AddButton === 'string'
                ? (
                  <button type="button" className="btn btn-outline-success btn-xs" onClick={() => addRow()}>
                    <span>
                      <i className="fa fa-plus pr-2" />
                      <Translate id={AddButton} />
                    </span>
                  </button>
                )
                : <AddButton {...properties} addRow={addRow} />
            }
          </div>
          )}
      </div>
    );
  }
}

const mapStateToProps = (state) => ({
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
