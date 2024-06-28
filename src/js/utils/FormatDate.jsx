import React from 'react';

import PropTypes from 'prop-types';
import { useSelector } from 'react-redux';

import DateFormat from 'consts/dateFormat';
import { formatDate } from 'utils/translation-utils';

const FormatDate = ({ date, formatName }) => {
  const { translateDate } = useSelector((state) => ({
    translateDate: formatDate(state.localize),
  }));

  return (
    <>
      {translateDate(date, formatName)}
    </>
  );
};

export default FormatDate;

FormatDate.propTypes = {
  date: PropTypes.instanceOf(Date),
  formatName: PropTypes.string,
};

FormatDate.defaultProps = {
  date: new Date(),
  formatName: DateFormat.DEFAULT,
};
