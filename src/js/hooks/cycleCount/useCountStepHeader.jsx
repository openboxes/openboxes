import { useEffect, useState } from 'react';

const useCountStepHeader = ({
  id,
  initialDateCounted,
  initialCountedBy,
  initialDefaultCountedBy,
  updateDateCounted,
  assignCountedBy,
}) => {
  const [dateCounted, setDateCounted] = useState(initialDateCounted);
  const [countedBy, setCountedBy] = useState(initialCountedBy);
  const [defaultCountedBy, setDefaultCountedBy] = useState(initialDefaultCountedBy);

  // Sync state with initial props when they change
  useEffect(() => {
    if (dateCounted !== initialDateCounted) {
      setDateCounted(initialDateCounted);
    }
    if (countedBy !== initialCountedBy) {
      setCountedBy(initialCountedBy);
    }
    if (defaultCountedBy !== initialDefaultCountedBy) {
      setDefaultCountedBy(initialDefaultCountedBy);
    }
  }, [initialDateCounted, initialCountedBy, initialDefaultCountedBy]);

  const handleDateCountedChange = (date) => {
    setDateCounted(date.format());
    updateDateCounted(date);
  };

  const handleCountedByChange = (person) => {
    setCountedBy(person);
    setDefaultCountedBy(person);
    assignCountedBy(id)(person);
  };

  const countedByMeta = countedBy ? {
    id: countedBy.id,
    value: countedBy.id,
    label: countedBy.label ?? `${countedBy.firstName} ${countedBy.lastName}`,
    name: `${countedBy.firstName} ${countedBy.lastName}`,
  } : null;

  const defaultCountedByMeta = defaultCountedBy ? {
    id: defaultCountedBy.id,
    value: defaultCountedBy.id,
    label: defaultCountedBy.label ?? `${defaultCountedBy.firstName} ${defaultCountedBy.lastName}`,
    name: `${defaultCountedBy.firstName} ${defaultCountedBy.lastName}`,
  } : null;

  return {
    dateCounted,
    countedByMeta,
    defaultCountedByMeta,
    handleDateCountedChange,
    handleCountedByChange,
  };
};

export default useCountStepHeader;
