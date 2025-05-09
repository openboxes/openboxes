import { useDispatch, useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';

import { eraseDraft } from 'actions';
import { CYCLE_COUNT } from 'consts/applicationUrls';

const useDraftInfoBar = () => {
  const dispatch = useDispatch();
  const history = useHistory();
  const { currentLocation } = useSelector((state) => ({
    currentLocation: state.session.currentLocation,
  }));

  const discardDraft = () => {
    dispatch(eraseDraft(currentLocation));
  };

  const continueDraft = () => {
    history.push(CYCLE_COUNT.countStep());
  };

  return {
    discardDraft,
    continueDraft,
  };
};

export default useDraftInfoBar;
