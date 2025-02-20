import React from 'react';

import Tippy from '@tippyjs/react';
import PropTypes from 'prop-types';

// eslint-disable-next-line import/no-extraneous-dependencies
import 'tippy.js/animations/shift-away.css';

const TooltipWrapper = ({
  children,
  content,
  className,
}) => (
  <Tippy
    content={<div className="tippy-tooltip-v2">{content}</div>}
    animation="shift-away"
  >
    <span className={className}>{children}</span>
  </Tippy>
);

export default TooltipWrapper;

TooltipWrapper.propTypes = {
  children: PropTypes.node.isRequired,
  content: PropTypes.node.isRequired,
  className: PropTypes.string,
};

TooltipWrapper.defaultProps = {
  className: '',
};
