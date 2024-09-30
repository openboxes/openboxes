import { useEffect, useRef } from 'react';

import { useDispatch, useSelector } from 'react-redux';

import { setScrollToBottom } from 'actions';

const useScrollToBottom = () => {
  const {
    scrollToBottom,
  } = useSelector((state) => ({
    scrollToBottom: state.outboundImport.scrollToBottom,
  }));
  const dispatch = useDispatch();

  const nextButtonRef = useRef();

  useEffect(() => {
    if (scrollToBottom && nextButtonRef.current) {
      nextButtonRef.current.scrollIntoView();
      dispatch(setScrollToBottom(false));
    }
  }, [scrollToBottom, nextButtonRef.current]);

  return {
    nextButtonRef,
  };
};

export default useScrollToBottom;
