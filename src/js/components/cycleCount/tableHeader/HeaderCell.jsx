import React from 'react';

import PropTypes from 'prop-types';

import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import useTranslate from 'hooks/useTranslate';

const HeaderCell = ({ id, defaultMessage }) => {
  const translate = useTranslate();

  return (
    <TableHeaderCell className="rt-th-count-step">
      {translate(id, defaultMessage)}
    </TableHeaderCell>
  );
};

export default HeaderCell;

HeaderCell.propTypes = {
  id: PropTypes.string.isRequired,
  defaultMessage: PropTypes.string.isRequired,
};
