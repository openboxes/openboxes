import React from 'react';

import {
  RiCheckboxCircleFill,
  RiCheckFill,
  RiErrorWarningFill,
  RiLoader4Line,
  RiSubtractFill,
} from 'react-icons/ri';

export const AutosaveStatus = {
  SAVED: 'saved',
  SAVING: 'saving',
  DRAFT: 'draft',
  ERROR: 'error',
};

export const AutosaveStatusDescription = {
  SAVED: 'the line item is completed and saved.',
  DRAFT: 'the line item is not saved. Required fields must be filled in.',
  ERROR: 'the line item is not saved. Correct the errors.',
};

// Config for autosave indicator used for the whole table, not only for rows
export const AutosaveConfig = {
  [AutosaveStatus.SAVED]: {
    icon: <RiCheckboxCircleFill size={17} />,
    label: 'react.receiving.autosave.saved.label',
    defaultLabel: 'Your work is auto-saved',
  },
  [AutosaveStatus.SAVING]: {
    icon: <RiLoader4Line size={17} className="autosave-indicator__spinner" />,
    label: 'react.receiving.autosave.saving.label',
    defaultLabel: 'Your work is being saved',
  },
  [AutosaveStatus.ERROR]: {
    icon: <RiErrorWarningFill size={17} />,
    label: 'react.receiving.autosave.error.label',
    defaultLabel: 'Failed to save your work',
  },
};

export const AutosaveIcon = {
  SAVED: <RiCheckFill />,
  DRAFT: <RiSubtractFill />,
  ERROR: <RiErrorWarningFill />,
};
