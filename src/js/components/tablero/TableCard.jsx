import React from 'react';
import PropTypes from 'prop-types';

/* global _ */
const TableCard = props => (
  <div className="tableCard">
    <table>
      <thead>
        <tr>
          <th>{props.data.number}</th>
          <th>{_.truncate(props.data.name, { length: 50, omission: '...' })}</th>
          <th>{_.truncate(props.data.value, { length: 50, omission: '...' })}</th>
        </tr>
      </thead>
      {props.data.body.map(item => (
        <tbody key={`item-${Math.random()}`} className="tableLink">
          <tr onClick={() => window.open(item.link, '_blank')}>
            <td>{item.number}</td>
            <td>{_.truncate(item.name, { length: 50, omission: '...' })}</td>
            <td>{_.truncate(item.value, { length: 10, omission: '...' })}</td>
          </tr>
        </tbody>
      ))}
    </table>
  </div>
);

TableCard.propTypes = {
  data: PropTypes.shape({
    number: PropTypes.string,
    name: PropTypes.string,
    value: PropTypes.string,
    body: PropTypes.arrayOf(PropTypes.shape({})),
  }).isRequired,
};

TableCard.defaultProps = {
};

export default TableCard;
