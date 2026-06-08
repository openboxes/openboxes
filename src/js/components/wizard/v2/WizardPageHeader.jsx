import React from 'react';

import * as locales from 'date-fns/locale';
import PropTypes from 'prop-types';
import { useSelector } from 'react-redux';
import { getCurrentLocale } from 'selectors';

import { DateFormatDateFns } from 'consts/timeFormat';
import { formatDateToString } from 'utils/dateUtils';

/**
 * Generic header bar for v2 wizard pages. Renders a prefix label followed by
 * colored, delimited info segments and an optional status badge.
 */
const WizardPageHeader = ({ label, info, status }) => {
  const currentLocale = useSelector(getCurrentLocale);

  return (
    <div className="wizard-page-header-title d-flex align-items-center justify-content-between w-100">
      <h5 className="wizard-page-header-content m-0">
        {info?.length > 0 && (
        <>
          {label && (
          <>
            <span>{label}</span>
            <span>{' | '}</span>
          </>
          )}
          {info.map((item) => (
            <React.Fragment key={item.text}>
              <span style={{ color: item.color }}>
                {item.isDate
                  ? formatDateToString({
                    date: item.text,
                    dateFormat: DateFormatDateFns.DD_MMM_YYYY,
                    options: { locale: locales[currentLocale] },
                  })
                  : item.text}
              </span>
              {item.delimeter && <span>{item.delimeter}</span>}
            </React.Fragment>
          ))}
        </>
        )}
      </h5>
      {status && (
      <span className="shipment-status">
        {status}
      </span>
      )}
    </div>
  );
};

export default WizardPageHeader;

WizardPageHeader.propTypes = {
  /** prefix label, e.g. "Receiving" */
  label: PropTypes.string,
  /** Colored title segments rendered after the label */
  info: PropTypes.arrayOf(
    PropTypes.shape({
      text: PropTypes.string,
      color: PropTypes.string,
      delimeter: PropTypes.string,
      isDate: PropTypes.bool,
    }),
  ),
  /** Optional status badge rendered on the right side of the header */
  status: PropTypes.string,
};

WizardPageHeader.defaultProps = {
  label: '',
  info: [],
  status: '',
};
