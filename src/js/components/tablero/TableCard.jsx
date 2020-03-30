import React from 'react';
import PropTypes from 'prop-types';

const TableCard = props => (
  <div>
    <table>
      <thead>
        <tr>
          <th><span className="small-text">Nº of<br /></span> Shipments</th>
          <th><span className="small-text"><br /></span>Name</th>
          <th><span className="small-text">Nº of<br /></span> Discrepancy</th>
        </tr>
      </thead>
      {props.data.map(item => (
        <tbody key={`item-${item.shipments}`}>
          <tr>
            <td>{item.shipments}</td>
            <td>{item.name}</td>
            <td>{item.discrepancy}</td>
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
