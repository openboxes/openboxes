import { useEffect, useMemo, useState } from 'react';

import { useSelector } from 'react-redux';
import { getCurrentLocation } from 'selectors';

import cycleCountReportingIndicators from 'consts/cycleCountReportingIndicators';
import dateWithoutTimeZone from 'utils/dateUtils';
import {
  fetchIndicatorItemsCounted,
  fetchIndicatorNotFinishedItems,
  fetchIndicatorTargetProgress,
  fetchIndicatorTotalCount,
} from 'utils/option-utils';

const useIndicatorsTab = ({
  filterParams,
}) => {
  const [loading, setLoading] = useState(false);
  // In tiles, we will store all the filtered data that we will render in IndicatorNumberCards
  const [tiles, setTiles] = useState([]);
  const {
    currentLocation,
  } = useSelector((state) => ({
    currentLocation: getCurrentLocation(state),
  }));
  const { endDate, startDate } = filterParams;

  const params = useMemo(() => ({
    startDate: dateWithoutTimeZone({ date: startDate }),
    endDate: dateWithoutTimeZone({ date: endDate }),
    facility: currentLocation.id,
  }), [startDate, endDate, currentLocation?.id]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const responses = await Promise.all([
        fetchIndicatorTotalCount(params),
        fetchIndicatorItemsCounted(params),
        fetchIndicatorTargetProgress(params),
        fetchIndicatorNotFinishedItems(params),
      ]);

      const mergedNumberCards = responses
        .filter(item => cycleCountReportingIndicators[item.type])
        .map(item => ({
          ...cycleCountReportingIndicators[item.type],
          ...item,
        }));

      setTiles(mergedNumberCards);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (startDate && endDate) {
      fetchData();
    }
  }, [startDate, endDate, currentLocation?.id]);

  return {
    loading,
    tiles,
  };
};

export default useIndicatorsTab;
