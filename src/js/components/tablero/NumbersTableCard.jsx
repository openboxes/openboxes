import React from 'react';
import PropTypes from 'prop-types';
import Numbers from './Numbers';
import TableCard from './TableCard';

const NumbersTableCard = props => (
  <div className="numbers-table-card">
    <div className="numbers-left">
      <Numbers data={props.data.numberIndicator} options={props.options} />
    </div>
    <div className="table-right">
      <TableCard data={props.data.tableData} />
    </div>
  </div>
);

NumbersTableCard.propTypes = {
  data: PropTypes.shape({
    numberIndicator: PropTypes.shape({
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
  options: PropTypes.shape({}).isRequired,
};

export default NumbersTableCard;
