import React from 'react';

import PropTypes from 'prop-types';

import Numbers from 'components/dashboard/Numbers';
import TableCard from 'components/dashboard/TableCard';

const NumbersTableCard = (props) => (
  <div className="numbers-table-card">
    <div className="numbers-left">
      <Numbers data={props.data.numbersIndicator} options={props.options} />
    </div>
    <div className="table-right">
      <TableCard
        data={props.data.tableData}
        columnsSize={props.options.columnsSize}
        truncationLength={props.options.truncationLength}
        disableTruncation={props.options.disableTruncation}
      />
    </div>
  </div>
);

NumbersTableCard.propTypes = {
  data: PropTypes.shape({
    numbersIndicator: PropTypes.shape({
      first: PropTypes.shape({
        link: PropTypes.string,
        value: PropTypes.number,
        subtitle: PropTypes.string,
      }).isRequired,
      second: PropTypes.shape({
        link: PropTypes.string,
        value: PropTypes.number,
        subtitle: PropTypes.string,
      }).isRequired,
      third: PropTypes.shape({
        link: PropTypes.string,
        value: PropTypes.number,
        subtitle: PropTypes.string,
      }).isRequired,
    }).isRequired,
    tableData: PropTypes.shape({
      number: PropTypes.string,
      name: PropTypes.string,
      value: PropTypes.string,
      body: PropTypes.arrayOf(PropTypes.shape({})),
    }).isRequired,
  }).isRequired,
  options: PropTypes.shape({
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
  }).isRequired,
};

export default NumbersTableCard;
