import React from 'react';

import PropTypes from 'prop-types';
import { Tooltip } from 'react-tippy';

const CustomTooltip = ({
  children,
  content,
  className,
  show,
  disabled,
  icon: Icon,
}) => (
  // This div was added to ensure the tooltip works correctly with absolute positioning
  show ? (
    <div className={className} role="tooltip">
      <Tooltip
        delay={150}
        duration={250}
        hideDelay={50}
        disabled={disabled}
        // react-tippy has a quirk where it resets its non-styled tooltip to have value of `title`
        // when the tooltip is re-enabled after being disabled. This causes a non-styled "undefined"
        // tooltip to display. Setting a blank title hides the tooltip in this scenario.
        title=" "
        className="w-100"
        style={{ display: 'block' }}
        html={<div className={`p-2 tooltip-dark-blue ${!content && 'd-none'}`}>{content}</div>}
      >
        <div className="flex items-center">
          {Icon && <Icon className="mr-2" />}
          {children}
        </div>
      </Tooltip>
    </div>
  ) : children
);

export default CustomTooltip;

CustomTooltip.propTypes = {
  children: PropTypes.node.isRequired,
  content: PropTypes.node.isRequired,
  className: PropTypes.string,
  /**
   * If false, the {@link Tooltip} will not even be rendered, and so setting `disabled=false`
   * will have no impact.
   *
   * If you need to conditionally enable the tooltip without reloading the page, set `show`
   * to true and use `disabled` to dynamically control the state of the tooltip.
   */
  show: PropTypes.bool,
  /**
   * True if the {@link Tooltip} added by `show` should be disabled.
   *
   * Useful for cases where the tooltip will conditionally be active depending on user interaction
   * with `children` (for example, for displaying validation errors). We don't want the tooltip
   * to ever get unmounted, even if there is nothing for it to show, since unmounting it can alter
   * state, trigger a redraw, and cause a child element to lose focus.
   *
   * If `show` is false, `disabled` will have no effect.
   */
  disabled: PropTypes.bool,
  icon: PropTypes.elementType,
};

CustomTooltip.defaultProps = {
  className: '',
  icon: null,
  show: true,
  disabled: false,
};
