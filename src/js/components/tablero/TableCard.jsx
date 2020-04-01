import React from 'react';
import PropTypes from 'prop-types';

/* global _ */
const TableCard = props => (
  <div>
    <table>
      <thead>
        <tr>
          <th><span className="small-text">Nº of<br /></span> Shipment</th>
          <th><span className="small-text"><br /></span>Name</th>
          <th><span className="small-text">Nº of<br /></span> Discrepancy</th>
        </tr>
      </thead>
      {props.data.map(item => (
        <tbody key={`item-${item.shipments}`}>
          <tr>
            <td>{_.truncate(item.shipments, { length: 10, omission: '...' })}</td>
            <td>{item.name}</td>
            <td>{_.truncate(item.discrepancy, { length: 10, omission: '...' })}</td>
          </tr>
        </tbody>
      ))}
    </table>
  </div>
);

TableCard.propTypes = {
  data: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
};

TableCard.defaultProps = {
};

export default TableCard;
