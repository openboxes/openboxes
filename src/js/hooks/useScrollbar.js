const useScrollbar = ({ selector }) => {
  const scrollToTop = () => {
    const element = document.querySelector(selector);
    if (element) {
      element.scrollTop = 0;
    }
  };

  const scrollToBottom = () => {
    const element = document.querySelector(selector);
    if (element) {
      element.scrollTop = element.scrollHeight;
    }
  };

  return {
    scrollToBottom,
    scrollToTop,
  };
};

export default useScrollbar;
