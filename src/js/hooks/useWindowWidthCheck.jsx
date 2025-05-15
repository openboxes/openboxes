import { useEffect, useState } from 'react';

const useWindowWidthCheck = (width) => {
  const [isWider, setIsWider] = useState(false);

  useEffect(() => {
    const handleResize = () => {
      setIsWider(window.innerWidth > width);
    };
    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, [width]);

  return isWider;
};

export default useWindowWidthCheck;
