import React, { useState } from 'react';

import PropTypes from 'prop-types';
import { RiArrowDownSLine } from 'react-icons/ri';

import Translate from 'utils/Translate';

const Subsection = ({
  title,
  collapsable,
  children,
  expandedByDefault,
}) => {
  // If a subsection is not collapsable, it is always expanded
  // (collapsable: false --> expanded: true)
  // If a subsection is collapsable, it is not expanded by default
  // (collapsable: true --> expanded: false)
  const [expanded, setExpanded] = useState(expandedByDefault);

  const triggerCollapse = () => {
    if (collapsable) {
      setExpanded(!expanded);
    }
  };

  // `title` is either a ready JSX node (e.g. a badge) or a translate descriptor.
  const renderTitle = () => {
    if (React.isValidElement(title)) {
      return title;
    }
    if (title.label && title.defaultMessage) {
      return <Translate id={title.label} defaultMessage={title.defaultMessage} />;
    }
    return null;
  };

  return (
    <div className="v2-subsection">
      <div className="subsection-title-wrapper">
        <span
          role="button"
          tabIndex={0}
          onClick={collapsable ? () => triggerCollapse() : null}
          onKeyDown={collapsable ? () => triggerCollapse() : null}
          style={collapsable ? { cursor: 'pointer' } : { cursor: 'unset' }}
        >
          {renderTitle()}
          {collapsable
            && <RiArrowDownSLine className={`arrow-up ${expanded ? 'arrow-up--expanded' : ''}`} />}
        </span>
      </div>
      <div className={`subsection-body ${expanded ? 'subsection-body-expanded' : ''}`}>
        {children}
      </div>
    </div>
  );
};

export default Subsection;

Subsection.propTypes = {
  // Either a translate descriptor or a ready node (e.g. a badge) rendered as-is.
  title: PropTypes.oneOfType([
    PropTypes.node,
    PropTypes.shape({
      label: PropTypes.string.isRequired,
      defaultMessage: PropTypes.string.isRequired,
    }),
  ]),
  collapsable: PropTypes.bool,
  expandedByDefault: PropTypes.bool,
  children: PropTypes.node.isRequired,
};

Subsection.defaultProps = {
  collapsable: true,
  expandedByDefault: true,
  title: '',
};
