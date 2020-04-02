import React from 'react';
import PropTypes from 'prop-types';

/* global _ */
const TableCard = props => (
  <div className="tableCard">
    <table>
      <thead>
        <tr>
          {props.headItems.map(item => <th>{item}</th>)}
        </tr>
      </thead>
      {props.data.map(item => (
        <tbody key={`item-${Math.random()}`} className="tableLink">
          <tr onClick={() => window.open(`/openboxes/stockMovement/show/${item.requisition}`, '_blank')}>
            <td>{item.shipments}</td>
            <td>{_.truncate(item.name, { length: 50, omission: '...' })}</td>
            <td>{_.truncate(item.discrepancy, { length: 10, omission: '...' })}</td>
          </tr>
        </tbody>
      ))}
    </table>
  </div>
);

TableCard.propTypes = {
  data: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  headItems: PropTypes.arrayOf().isRequired,
};

TableCard.defaultProps = {
};

export default TableCard;
