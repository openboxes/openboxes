import { useRef, useState } from 'react';

import actionItemType from 'consts/actionItemType';

const useContextMenu = () => {
  const popoverRef = useRef();
  const [isPopoverOpen, setIsPopoverOpen] = useState(false);

  const getActionItemType = (action) => {
    if (action.href) {
      if (action.reactLink) {
        return actionItemType.REACT_LINK;
      }
      return actionItemType.LINK;
    }
    return actionItemType.BUTTON;
  };

  const buildLink = (action, id) => {
    if (typeof action.href === 'string') {
      return `${action.href}/${id}`;
    }
    if (typeof action.href === 'function') {
      return action.href(id);
    }
    return null;
  };

  const getPositionClass = (dropdownPlacement) => {
    switch (dropdownPlacement) {
      case 'top':
        return 'dropup';
      case 'left':
        return 'dropleft';
      case 'right':
        return 'dropright';
      default:
        return '';
    }
  };

  return {
    getActionItemType,
    buildLink,
    getPositionClass,
    popoverRef,
    isPopoverOpen,
    setIsPopoverOpen,
  };
};

export default useContextMenu;
