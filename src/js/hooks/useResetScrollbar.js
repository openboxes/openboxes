const useResetScrollbar = ({ selector }) => {
  const resetScrollbar = () => {
    const element = document.querySelector(selector);
    element.scrollTop = 0;
  };

  return {
    resetScrollbar,
  };
};

export default useResetScrollbar;
