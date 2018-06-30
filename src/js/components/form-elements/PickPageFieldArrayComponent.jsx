import _ from 'lodash';
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { formValueSelector } from 'redux-form';
import { connect } from 'react-redux';

import { renderFormField } from '../../utils/form-utils';

class PickPageFieldArrayComponent extends Component {
  constructor(props) {
    super(props);

    this.state = { show: '' };
  }

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

    return (
      <div>
        <button
          type="button"
          className="btn btn-secondary p-2 mb-1 fa fa-expand d-print-none"
          onClick={() => {
            const elements = document.getElementsByClassName('collapse-all');
            if (!this.state.show) {
              _.forEach(elements, element => element.classList.add('show'));
              this.setState({ show: 'show' });
            } else {
              _.forEach(elements, element => element.classList.remove('show'));
              this.setState({ show: '' });
            }
          }}
        />
        <table className="table text-center border">
          <thead>
            <tr>
              { _.map(fieldsConfig.fields, (config, name) =>
                <th key={name}>{config.label}</th>) }
            </tr>
          </thead>
          <tbody>
            {fields.map((field, index) => {
            if (!this.props.pickPage[index].lot) {
              return (
                <tr
                  key={this.props.pickPage[index].rowKey}
                  className="bg-light clickable-row"
                  data-toggle="collapse"
                  data-target={`.collapse-${this.props.pickPage[index].product.code}`}
                  aria-expanded="false"
                >
                  { _.map(fieldsConfig.fields, (config, name) => (
                    <td key={`${field}.${name}`} className="align-middle">
                      { renderFormField(config, `${field}.${name}`, {
                        ...properties,
                        arrayField: true,
                        rowIndex: index,
                        fieldValue: _.get(fields.get(index), name),
                    })}
                    </td>
                )) }
                </tr>
              );
            }
            return (
              <tr
                key={this.props.pickPage[index].rowKey}
                className={`
                  bg-white 
                  collapse 
                  collapse-all 
                  collapse-${this.props.pickPage[index].product.code} 
                  ${this.props.pickPage[index].crossedOut ? 'crossed-out' : ''}
                `}
              >
                { _.map(fieldsConfig.fields, (config, name) => (
                  <td key={`${field}.${name}`} className="align-middle">
                    { (name !== 'buttonEditPick' && name !== 'buttonAdjustInventory') &&
                    renderFormField(config, `${field}.${name}`, {
                      ...properties,
                      arrayField: true,
                      rowIndex: index,
                      fieldValue: _.get(fields.get(index), name),
                    })}
                  </td>
                ))}
              </tr>
            );
            })}
          </tbody>
        </table>
      </div>
    );
  }
}

const selector = formValueSelector('stock-movement-wizard');

const mapStateToProps = state => ({ pickPage: selector(state, 'pickPage') });

PickPageFieldArrayComponent.propTypes = {
  fieldsConfig: PropTypes.shape({}).isRequired,
  fields: PropTypes.oneOfType([
    PropTypes.shape({}),
    PropTypes.arrayOf(PropTypes.shape({})),
  ]).isRequired,
  properties: PropTypes.shape({}),
  pickPage: PropTypes.arrayOf(PropTypes.shape({
    product: PropTypes.shape({
      code: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    }),
    lot: PropTypes.string,
    crossedOut: PropTypes.bool,
    rowKey: PropTypes.string,
  })),
};

PickPageFieldArrayComponent.defaultProps = {
  properties: {},
  pickPage: [],
};

export default connect(mapStateToProps, {})(PickPageFieldArrayComponent);
