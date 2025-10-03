const useWindowOpen = () => {
  const openWindow = (url, target = '_blank') => {
    if (!url) return;
    window.open(url, target);
  };

  return { openWindow };
};

export default useWindowOpen;
