import { useEffect, useState } from 'react';

const useResolveStepHeader = ({
  id,
  initialDateRecounted,
  initialRecountedBy,
  initialDefaultRecountedBy,
  updateRecountedDate,
  assignRecountedBy,
}) => {
  const [dateRecounted, setDateRecounted] = useState(initialDateRecounted);
  const [recountedBy, setRecountedBy] = useState(initialRecountedBy);
  const [defaultRecountedBy, setDefaultRecountedBy] = useState(initialDefaultRecountedBy);

  useEffect(() => {
    if (dateRecounted !== initialDateRecounted) {
      setDateRecounted(initialDateRecounted);
    }
    if (recountedBy !== initialRecountedBy) {
      setRecountedBy(initialRecountedBy);
    }
    if (defaultRecountedBy !== initialDefaultRecountedBy) {
      setDefaultRecountedBy(initialDefaultRecountedBy);
    }
  }, [initialDateRecounted, initialRecountedBy, initialDefaultRecountedBy]);

  const handleDateRecountedChange = (date) => {
    setDateRecounted(date);
    updateRecountedDate(date);
  };

  const handleRecountedByChange = (person) => {
    setRecountedBy(person);
    setDefaultRecountedBy(person);
    assignRecountedBy(id)(person);
  };

  const recountedByMeta = recountedBy ? {
    id: recountedBy.id,
    value: recountedBy.id,
    label: recountedBy.label ?? `${recountedBy.firstName} ${recountedBy.lastName}`,
    name: `${recountedBy.firstName} ${recountedBy.lastName}`,
  } : null;

  const defaultRecountedByMeta = defaultRecountedBy ? {
    id: defaultRecountedBy.id,
    value: defaultRecountedBy.id,
    label: defaultRecountedBy.label ?? `${defaultRecountedBy.firstName} ${defaultRecountedBy.lastName}`,
    name: `${defaultRecountedBy.firstName} ${defaultRecountedBy.lastName}`,
  } : null;

  return {
    dateRecounted,
    recountedBy,
    defaultRecountedBy,
    recountedByMeta,
    defaultRecountedByMeta,
    handleDateRecountedChange,
    handleRecountedByChange,
  };
};

export default useResolveStepHeader;
