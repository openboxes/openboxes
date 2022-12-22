const useCheckRightSpace = (elementReference, isVisible) => {
  if (elementReference.current && isVisible) {
    const elementCoordinates = elementReference.current.getBoundingClientRect();
    return window.innerWidth > elementCoordinates.right + elementCoordinates.width;
  }
  return false;
};

export default useCheckRightSpace;
