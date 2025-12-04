import { useEffect } from 'react';

const useHideScroll = ({ hide = false } = {}) => {
  useEffect(() => {
    if (hide) {
      document.body.style.overflowY = 'hidden';
    }

    return () => {
      document.body.style.overflowY = 'auto';
    };
  }, [hide]);
};

export default useHideScroll;
