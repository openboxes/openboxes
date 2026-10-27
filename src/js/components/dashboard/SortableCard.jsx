import React from 'react';

import { useSortable } from '@dnd-kit/react/sortable';
import PropTypes from 'prop-types';

const SortableCard = ({
  id, index, type, disabled, component: Card, ...cardProps
}) => {
  const { ref, handleRef } = useSortable({
    id,
    index,
    type,
    accept: type,
    group: type,
    disabled,
  });

  return <Card {...cardProps} sortableRef={ref} handleRef={handleRef} />;
};

export default SortableCard;

SortableCard.propTypes = {
  id: PropTypes.string.isRequired,
  index: PropTypes.number.isRequired,
  type: PropTypes.oneOf(['graph', 'number']).isRequired,
  disabled: PropTypes.bool,
  component: PropTypes.elementType.isRequired,
};

SortableCard.defaultProps = {
  disabled: false,
};
