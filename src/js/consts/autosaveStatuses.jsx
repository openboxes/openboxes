import React from 'react';

import { RiCheckFill, RiErrorWarningFill, RiSubtractFill } from 'react-icons/ri';

export const AutosaveStatus = {
  SAVED: 'saved',
  DRAFT: 'draft',
  ERROR: 'error',
};

export const AutosaveStatusDescription = {
  SAVED: 'the line item is completed and saved.',
  DRAFT: 'the line item is not saved. Required fields must be filled in.',
  ERROR: 'the line item is not saved. Correct the errors.',
};

export const AutosaveIcon = {
  SAVED: <RiCheckFill />,
  DRAFT: <RiSubtractFill />,
  ERROR: <RiErrorWarningFill />,
};
