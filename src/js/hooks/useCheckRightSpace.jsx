import { useRef, useState } from 'react';

const useCheckRightSpace = () => {
  const elementReference = useRef();
  const [isVisible, setIsVisible] = useState(false);

  const shouldAlignLeft = () => {
    if (elementReference.current && isVisible) {
      const elementCoordinates = elementReference.current.getBoundingClientRect();
      return window.innerWidth > elementCoordinates.right + elementCoordinates.width;
    }
    return false;
  };

  return { elementReference, setIsVisible, shouldAlignLeft };
};

export default useCheckRightSpace;
