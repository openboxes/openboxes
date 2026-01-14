/**
 * Forces a UI update (repaint) before continuing the next steps
 */
const forceUIUpdate = () =>
  new Promise((resolve) => setTimeout(resolve, 0));

export default forceUIUpdate;
