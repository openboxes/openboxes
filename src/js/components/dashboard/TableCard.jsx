import React from 'react';

import PropTypes from 'prop-types';

/* global _ */
const TableCard = (props) => {
  const {
    columnsSize,
    truncationLength,
    disableTruncation,
    data,
  } = props;

  const displayItemData = ({
    truncate,
    item,
    link,
    defaultTruncationLength,
  }) => (
    <a
      href={link}
      className={link ? 'indicator-item-href' : 'disabled-indicator-item-href'}
      rel="noreferrer"
      target="_blank"
    >
      {!truncate ? item : _.truncate(item, {
        length: truncationLength ?? defaultTruncationLength,
      })}
    </a>
  );

  const displayListItemsData = (items, links) =>
    items?.map((item, index) => (
      <a
        href={links[index]}
        className="indicator-item-href"
        rel="noreferrer"
        target="_blank"
      >
        {item}
        {' '}
      </a>
    ));

  return (
    <div className="table-card">
      <table>
        <thead>
          <tr>
            <th style={{ width: columnsSize?.number }}>
              {data.number}
            </th>
            {data.body.find((item) => item.icon) ? <td /> : null }
            <th style={{ width: columnsSize?.name }} className="mid">
              {_.truncate(data.name, { length: 50 })}
            </th>
            <th style={{ width: columnsSize?.value }}>
              {_.truncate(data.value, { length: 50 })}
            </th>
          </tr>
        </thead>
        <tbody>
          {data.body.map((item) => (
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
                {displayItemData({
                  truncate: disableTruncation?.number,
                  item: item.number,
                  link: item?.numberLink,
                  defaultTruncationLength: 80,
                })}
              </td>
              { item.icon ? <td><img alt="" src={item.icon} width="20" height="20" /></td> : null }
              <td className="mid" style={{ width: columnsSize?.name }}>
                {item.name
                  ? displayItemData({
                    truncate: disableTruncation?.name,
                    item: item.name,
                    link: item?.nameLink,
                    defaultTruncationLength: 80,
                  })
                  : displayListItemsData(item.nameDataList, item.nameLinksList)}
              </td>
              <td className="last" style={{ width: columnsSize?.value }}>
                {displayItemData({
                  truncate: disableTruncation?.value,
                  item: item.value,
                  link: item?.valueLink,
                  defaultTruncationLength: 10,
                })}
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
    numberLink: PropTypes.string,
    valueLink: PropTypes.string,
    nameLink: PropTypes.string,
    nameDataList: PropTypes.arrayOf(PropTypes.string),
    nameLinksList: PropTypes.arrayOf(PropTypes.string),
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
  disableTruncation: PropTypes.shape({
    name: PropTypes.bool,
    number: PropTypes.bool,
    value: PropTypes.bool,
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
  disableTruncation: {
    name: undefined,
    number: undefined,
    value: undefined,
  },
};

export default TableCard;
