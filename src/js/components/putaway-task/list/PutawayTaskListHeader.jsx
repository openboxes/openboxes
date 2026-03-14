import React from 'react';

import Translate from 'utils/Translate';

const PutawayTaskListHeader = () => (
  <div className="d-flex list-page-header">
    <span className="d-flex align-self-center title">
      <Translate id="react.putawayTask.list.label" defaultMessage="List Putaway Tasks" />
    </span>
  </div>
);

export default PutawayTaskListHeader;
