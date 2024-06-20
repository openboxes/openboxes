import React from 'react';

import PropTypes from 'prop-types';
import { useSelector } from 'react-redux';

import DateFormatName from 'consts/dateFormatName';
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
  formatName: DateFormatName.DEFAULT,
};
