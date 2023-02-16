import React from 'react';

import moment from 'moment';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import TableCell from 'components/DataTable/TableCell';

const DateCell = ({ tableDateFormat, tableDateDefaultValue, ...row }) => (
  <TableCell
    {...row}
    value={row.value && moment(row.value).format(tableDateFormat)}
    defaultValue={tableDateDefaultValue}
  />
);

const mapStateToProps = state => ({
  tableDateFormat: state.session.tableDateFormat,
  tableDateDefaultValue: state.session.tableDateDefaultValue,
});

export default connect(mapStateToProps)(DateCell);


DateCell.propTypes = {
  tableDateFormat: PropTypes.string.isRequired,
  tableDateDefaultValue: PropTypes.string.isRequired,
};
