import React from 'react';
import PropTypes from 'prop-types';

/* global _ */
const TableCard = props => (
  <div className="table-card">
    <table>
      <thead>
        <tr>
          <th>{props.data.number}</th>
          { props.data.body.find(item => item.icon) ? <td /> : null }
          <th className="mid">{_.truncate(props.data.name, { length: 50 })}</th>
          <th>{_.truncate(props.data.value, { length: 50 })}</th>
        </tr>
      </thead>
      <tbody>
        {props.data.body.map(item => (
          <tr
            onClick={() => window.open(item.link, '_blank')}
            key={`item-${item.number}`}
            className="table-link"
          >
            <td>{item.number}</td>
            { item.icon ? <td><img alt="" src={item.icon} width="20" height="20" /></td> : null }
            <td className="mid">{_.truncate(item.name, { length: 80 })}</td>
            <td>{_.truncate(item.value, { length: 10 })}</td>
          </tr>
        ))}
      </tbody>
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
