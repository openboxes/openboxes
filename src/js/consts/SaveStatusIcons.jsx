import React from 'react';

import { RiCheckFill, RiErrorWarningFill, RiSubtractFill } from 'react-icons/ri';

import Spinner from 'components/spinner/Spinner';
import RowSaveStatus from 'consts/rowSaveStatus';

const SaveStatusIcons = {
  [RowSaveStatus.SAVED]: {
    icon: <RiCheckFill />,
    id: 'react.stockMovement.tooltip.itemSaved.label',
    defaultMessage: 'Item saved',
  },
  [RowSaveStatus.PENDING]: {
    icon: <RiSubtractFill />,
    id: 'react.stockMovement.tooltip.itemNotSaved.label',
    defaultMessage: 'Item not saved. Fill in the line item details',
  },
  [RowSaveStatus.ERROR]: {
    icon: <RiErrorWarningFill />,
    id: 'react.stockMovement.tooltip.saveError.label',
    defaultMessage: 'Item not saved. Correct the error',
  },
  [RowSaveStatus.SAVING]: {
    icon: <Spinner />,
    id: 'react.stockMovement.tooltip.pendingSave.label',
    defaultMessage: 'Saving in progress',
  },
};

export default SaveStatusIcons;
