import { useState } from 'react';

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
