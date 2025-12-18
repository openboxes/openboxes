import { useState } from 'react';

/**
 * @deprecated Remove after performance fixes for cycle counts
 */
const useForceRender = () => {
  const [, setValue] = useState({});

  const forceRerender = () => {
    setValue({});
  };

  return {
    forceRerender,
  };
};

export default useForceRender;
