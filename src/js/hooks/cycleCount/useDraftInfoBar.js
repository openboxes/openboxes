import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';
import { getCurrentLocation } from 'selectors';

import { eraseDraft } from 'actions';
import { CYCLE_COUNT } from 'consts/applicationUrls';
import { TO_COUNT_TAB } from 'consts/cycleCount';

const useDraftInfoBar = (tab) => {
  const dispatch = useDispatch();
  const history = useHistory();
  const { currentLocation } = useSelector((state) => ({
    currentLocation: getCurrentLocation(state),
  }));

  const discardDraft = () => {
    dispatch(eraseDraft(currentLocation?.id, tab));
  };

  const continueDraft = () => {
    history.push(tab === TO_COUNT_TAB ? CYCLE_COUNT.countStep() : CYCLE_COUNT.resolveStep());
  };

  return {
    discardDraft,
    continueDraft,
  };
};

export default useDraftInfoBar;
