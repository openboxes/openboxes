/**
 * Custom hook for opening links in a browser tab. By default, links open in a new tab (`_blank`)
 * @returns {Object}
 */
const useWindowOpen = () => {
  /**
   * Opens a given URL in a browser window.
   * @param {string} url - The URL to open.
   * @param {string} [target='_blank'] - The target window. Defaults to '_blank'.
   */
  const openWindow = (url, target = '_blank') => {
    if (!url) {
      return;
    }
    window.open(url, target);
  };

  return { openWindow };
};

export default useWindowOpen;
