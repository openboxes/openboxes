import _ from 'lodash';
import React from 'react';
import PropTypes from 'prop-types';
import { formValueSelector } from 'redux-form';
import { connect } from 'react-redux';

import TableRow from './TableRow';

const LineItemsRowKeyFieldArrayComponent = (props) => {
  const { fieldsConfig, properties, fields } = props;
  const RowComponent = fieldsConfig.rowComponent || TableRow;
  return (
    <div>
      <table className="table table-striped text-center">
        <thead>
          <tr>
            {_.map(fieldsConfig.fields, (config, name) =>
              <th key={name}>{config.label}</th>)}
          </tr>
        </thead>
        <tbody>
          {fields.map((field, index) => (
            <RowComponent
              key={props.lineItems[index].rowKey}
              field={field}
              index={index}
              properties={properties}
              fieldsConfig={fieldsConfig}
              addRow={() => true}
              removeRow={() => fields.remove(index)}
            />))
              }
        </tbody>
      </table>
    </div>
  );
};

const selector = formValueSelector('stock-movement-wizard');

const mapStateToProps = state => ({ lineItems: selector(state, 'lineItems') });

LineItemsRowKeyFieldArrayComponent.propTypes = {
  fieldsConfig: PropTypes.shape({}).isRequired,
  lineItems: PropTypes.arrayOf(PropTypes.shape({ rowKey: PropTypes.string.isRequired })).isRequired,
  fields: PropTypes.oneOfType([
    PropTypes.shape({}),
    PropTypes.arrayOf(PropTypes.shape({})),
  ]).isRequired,
  properties: PropTypes.shape({}),
};

LineItemsRowKeyFieldArrayComponent.defaultProps = {
  properties: {},
};

export default connect(mapStateToProps, {})(LineItemsRowKeyFieldArrayComponent);
