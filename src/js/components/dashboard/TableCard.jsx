import React from 'react';

import PropTypes from 'prop-types';

/* global _ */
const TableCard = (props) => {
  const { columnsSize, truncationLength } = props;

  return (
    <div className="table-card">
      <table>
        <thead>
          <tr>
            <th style={{ width: columnsSize?.number }}>
              {props.data.number}
            </th>
            { props.data.body.find((item) => item.icon) ? <td /> : null }
            <th style={{ width: columnsSize?.name }} className="mid">
              {_.truncate(props.data.name, { length: 50 })}
            </th>
            <th style={{ width: columnsSize?.value }}>
              {_.truncate(props.data.value, { length: 50 })}
            </th>
          </tr>
        </thead>
        <tbody>
          {props.data.body.map((item) => (
            <tr
              onClick={() => {
                if (item.link) {
                  window.open(item.link, '_blank');
                }
              }}
              key={`item-${item.number}`}
              className={item.link ? 'table-link' : ''}
            >
              <td style={{ width: columnsSize?.number }}>
                {_.truncate(item.number, { length: truncationLength?.number ?? 80 })}
              </td>
              { item.icon ? <td><img alt="" src={item.icon} width="20" height="20" /></td> : null }
              <td className="mid" style={{ width: columnsSize?.name }}>
                {_.truncate(item.name, { length: truncationLength?.name ?? 80 })}
              </td>
              <td style={{ width: columnsSize?.value }}>
                {_.truncate(item.value, { length: truncationLength?.value ?? 10 })}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

TableCard.propTypes = {
  data: PropTypes.shape({
    number: PropTypes.string,
    name: PropTypes.string,
    value: PropTypes.string,
    body: PropTypes.arrayOf(PropTypes.shape({})),
  }).isRequired,
  columnsSize: PropTypes.shape({
    name: PropTypes.string,
    number: PropTypes.string,
    value: PropTypes.string,
  }),
  truncationLength: PropTypes.shape({
    name: PropTypes.number,
    number: PropTypes.number,
    value: PropTypes.number,
  }),
};

TableCard.defaultProps = {
  columnsSize: {
    name: null,
    number: null,
    value: null,
  },
  truncationLength: {
    name: null,
    number: null,
    value: null,
  },
};

export default TableCard;
