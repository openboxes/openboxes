import { useDispatch } from 'react-redux';
import { useHistory } from 'react-router-dom';

import { eraseDraft } from 'actions';
import { CYCLE_COUNT } from 'consts/applicationUrls';

const useDraftInfoBar = () => {
  const dispatch = useDispatch();
  const history = useHistory();
  const discardDraft = () => {
    dispatch(eraseDraft());
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
