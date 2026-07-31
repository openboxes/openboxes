import React from 'react';

import { sortableHandle } from 'react-sortable-hoc';
import { RiMore2Fill } from 'react-icons/ri';

const DragHandle = sortableHandle(() => (
  <span className="drag-handler">
    <RiMore2Fill />
  </span>
));

export default DragHandle;
