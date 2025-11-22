import React from 'react';

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
