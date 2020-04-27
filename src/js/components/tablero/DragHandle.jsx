import React from 'react';
import { sortableHandle } from 'react-sortable-hoc';

const DragHandle = sortableHandle(() => (
  <span className="drag-handler">
    <i className="fa fa-ellipsis-v" />
  </span>
));

export default DragHandle;
