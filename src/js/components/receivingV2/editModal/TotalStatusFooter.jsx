import React from 'react';

import PropTypes from 'prop-types';

import useFormatNumber from 'hooks/useFormatNumber';
import useTranslate from 'hooks/useTranslate';
import getReceivingRowStatus from 'utils/receiving/getReceivingRowStatus';

// The footer which displays the total status of the receiving now table,
// based on the remaining quantity to receive.
const TotalStatusFooter = ({ remainingToReceive }) => {
  const translate = useTranslate();
  const formatNumber = useFormatNumber();

  const { className, value } = getReceivingRowStatus({
    quantityRemaining: remainingToReceive,
    translate,
    formatNumber,
  });

  return (
    <span>
      (
      <span className={className}>{value}</span>
      )
    </span>
  );
};

TotalStatusFooter.propTypes = {
  remainingToReceive: PropTypes.number.isRequired,
};

export default TotalStatusFooter;
