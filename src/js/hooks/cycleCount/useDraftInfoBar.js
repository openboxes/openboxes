import { useDispatch } from 'react-redux';
import { useHistory } from 'react-router-dom';

import { eraseDraft } from 'actions';
import { CYCLE_COUNT } from 'consts/applicationUrls';

const useDraftInfoBar = (tab) => {
  const dispatch = useDispatch();
  const history = useHistory();
  const discardDraft = () => {
    console.log('Discarding draft...', tab);
    dispatch(eraseDraft(tab));
  };

  const continueDraft = () => {
    history.push(tab === 'count' ? CYCLE_COUNT.countStep() : CYCLE_COUNT.resolveStep());
  };

  return {
    discardDraft,
    continueDraft,
  };
};

export default useDraftInfoBar;
