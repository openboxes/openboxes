import React from 'react';

import PropTypes from 'prop-types';

import Button from 'components/form-elements/Button';
import useDraftInfoBar from 'hooks/cycleCount/useDraftInfoBar';
import Translate from 'utils/Translate';

const CycleCountDraftInfoBar = ({ tab }) => {
  const {
    continueDraft,
    discardDraft,
  } = useDraftInfoBar(tab);
  return (
    <div className="d-flex justify-content-between align-items-center draft-modal">
      <div>
        <Translate
          id="react.cycleCount.draftInfoBar.label"
          defaultMessage="You have an unfinished count. Do you want to get back to the point where you left off?"
        />
      </div>
      <div className="d-flex gap-8">
        <Button
          label="react.cycleCount.draftInfoBar.discard.button.label"
          defaultLabel="Discard"
          variant="secondary"
          onClick={() => discardDraft()}
        />
        <Button
          label="react.cycleCount.draftInfoBar.continue.button.label"
          defaultLabel="Continue"
          variant="primary"
          onClick={() => continueDraft()}
        />
      </div>
    </div>
  );
};

export default CycleCountDraftInfoBar;

CycleCountDraftInfoBar.propTypes = {
  tab: PropTypes.string.isRequired,
};
