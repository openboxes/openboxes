import React, { useState } from 'react';

import PropTypes from 'prop-types';
import { RiArrowDownSLine, RiArrowUpSLine } from 'react-icons/ri';

import Translate from 'utils/Translate';

const Subsection = ({
  title,
  collapsable,
  children,
}) => {
  // If a subsection is not collapsable, it is always expanded
  // (collapsable: false --> expanded: true)
  // If a subsection is collapsable, it is not expanded by default
  // (collapsable: true --> expanded: false)
  const [expanded, setExpanded] = useState(!collapsable);

  const triggerCollapse = () => {
    setExpanded(!expanded);
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
          <Translate id={title.label} defaultMessage={title.defaultMessage} />
          {collapsable
            && (expanded
              ? <RiArrowUpSLine className="arrow-up" />
              : <RiArrowDownSLine className="arrow-down" />)}
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
  title: PropTypes.shape({
    label: PropTypes.string.isRequired,
    defaultMessage: PropTypes.string.isRequired,
  }).isRequired,
  collapsable: PropTypes.bool,
  children: PropTypes.node.isRequired,
};

Subsection.defaultProps = {
  collapsable: true,
};
