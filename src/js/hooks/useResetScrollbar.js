const useResetScrollbar = ({ scrollableComponentClassName }) => {
  const resetScrollbar = () => {
    const element = document.getElementsByClassName(scrollableComponentClassName);
    element[0].scrollTop = 0;
  };

  return {
    resetScrollbar,
  };
};

export default useResetScrollbar;
