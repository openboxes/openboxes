import React from 'react';

import moment from 'moment';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import TableCell from 'components/DataTable/TableCell';
import DateFormat from 'consts/dateFormat';
import { formatDate } from 'utils/translation-utils';

const DateCell = ({
  displayDateFormat,
  displayDateDefaultValue,
  defaultValue,
  localizeDate,
  formatLocalizedDate,
  formatLocalizedDateToDisplay,
  ...row
}) => {
  const getValue = () => {
    if (!row.value) {
      return null;
    }

    if (localizeDate) {
      return formatLocalizedDateToDisplay(row.value, formatLocalizedDate);
    }

    return moment(row.value).format(displayDateFormat);
  };

  return (
    <TableCell
      {...row}
      value={getValue()}
      defaultValue={defaultValue ?? displayDateDefaultValue}
    />
  );
};

const mapStateToProps = (state) => ({
  displayDateFormat: state.session.displayDateFormat,
  displayDateDefaultValue: state.session.displayDateDefaultValue,
  formatLocalizedDateToDisplay: formatDate(state.localize),
});

export default connect(mapStateToProps)(DateCell);

DateCell.defaultProps = {
  localizeDate: false,
  formatLocalizedDate: DateFormat.COMMON,
  defaultValue: undefined,
};

DateCell.propTypes = {
  displayDateFormat: PropTypes.string.isRequired,
  displayDateDefaultValue: PropTypes.string.isRequired,
  defaultValue: PropTypes.string,
  localizeDate: PropTypes.bool,
  formatLocalizedDate: PropTypes.string,
  formatLocalizedDateToDisplay: PropTypes.func.isRequired,
};
