import React from 'react';

import { RiMore2Fill } from 'react-icons/ri';
import { sortableHandle } from 'react-sortable-hoc';

const DragHandle = sortableHandle(() => (
  <span className="drag-handler">
    <RiMore2Fill />
  </span>
));

export default DragHandle;
