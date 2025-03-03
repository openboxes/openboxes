import { useEffect, useState } from 'react';

const useElementSize = (element) => {
  const [size, setSize] = useState({
    width: element.innerWidth,
    height: element.innerHeight,
  });

  const handleResize = () => {
    setSize({
      width: element.innerWidth,
      height: element.innerHeight,
    });
  };

  useEffect(() => {
    element.addEventListener('resize', handleResize);
    // Call handler right away so state gets updated with initial window size
    handleResize();
    return () => {
      element.removeEventListener('resize', handleResize);
    };
  }, []);
  return size;
};

export default useElementSize;
