import { useDispatch } from 'react-redux';

import { hideSpinner, showSpinner } from 'actions';

const useSpinner = () => {
  const dispatch = useDispatch();

  const show = () => dispatch(showSpinner());
  const hide = () => dispatch(hideSpinner());

  return { show, hide };
};

export default useSpinner;
