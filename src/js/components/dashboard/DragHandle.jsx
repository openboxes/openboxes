import React from 'react';

import { RiMore2Fill } from 'react-icons/ri';
import { sortableHandle } from 'react-sortable-hoc';

import { isUnifiedLayout } from 'utils/unifiedLayout';

const DragHandle = sortableHandle(() => (
  <span className="drag-handler">
    {isUnifiedLayout() ? <RiMore2Fill /> : <i className="fa fa-ellipsis-v" />}
  </span>
));

export default DragHandle;
