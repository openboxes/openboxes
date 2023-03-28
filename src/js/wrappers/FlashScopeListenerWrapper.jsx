import useFlashScopeListener from 'hooks/useFlashScopeListener';

const FlashScopeListenerWrapper = ({ children }) => {
  useFlashScopeListener();

  return children;
};

export default FlashScopeListenerWrapper;
