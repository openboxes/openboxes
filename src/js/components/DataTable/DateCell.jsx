import React from 'react';

import moment from 'moment';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import TableCell from 'components/DataTable/TableCell';

const DateCell = ({ displayDateFormat, displayDateDefaultValue, ...row }) => (
  <TableCell
    {...row}
    value={row.value && moment(row.value).format(displayDateFormat)}
    defaultValue={displayDateDefaultValue}
  />
);

const mapStateToProps = state => ({
  displayDateFormat: state.session.displayDateFormat,
  displayDateDefaultValue: state.session.displayDateDefaultValue,
});

export default connect(mapStateToProps)(DateCell);


DateCell.propTypes = {
  displayDateFormat: PropTypes.string.isRequired,
  displayDateDefaultValue: PropTypes.string.isRequired,
};
