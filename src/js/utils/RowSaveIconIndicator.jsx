import React from 'react';

import PropTypes from 'prop-types';
import { getTranslate } from 'react-localize-redux';
import { useSelector } from 'react-redux';

import RowSaveStatus from 'consts/rowSaveStatus';
import SaveStatusIcons from 'consts/saveStatusIcons';
import { translateWithDefaultMessage } from 'utils/Translate';
import CustomTooltip from 'wrappers/CustomTooltip';

const RowSaveIconIndicator = ({ lineItemSaveStatus }) => {
  const { icon, id, defaultMessage } = SaveStatusIcons[lineItemSaveStatus];
  const translate = useSelector((state) =>
    translateWithDefaultMessage(getTranslate(state.localize)));

  return (
    <CustomTooltip content={<span className="p-1">{translate(id, defaultMessage)}</span>}>
      <div className={`${lineItemSaveStatus?.toLowerCase()} line-item-icon-save-indicator`}>{icon}</div>
    </CustomTooltip>
  );
};

export default RowSaveIconIndicator;

RowSaveIconIndicator.propTypes = {
  lineItemSaveStatus: PropTypes.string,
};

RowSaveIconIndicator.defaultProps = {
  lineItemSaveStatus: RowSaveStatus.SAVED,
};
