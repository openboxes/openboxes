import { useEffect, useMemo, useState } from 'react';

import moment from 'moment';
import queryString from 'query-string';
import { useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';
import { getCurrentLocation } from 'selectors';

import cycleCountReportingIndicators from 'consts/cycleCountReportingIndicators';
import { DateFormat } from 'consts/timeFormat';
import dateWithoutTimeZone from 'utils/dateUtils';
import {
  fetchIndicatorInventoryAccuracy,
  fetchIndicatorInventoryShrinkage,
  fetchIndicatorProductsInventoried,
} from 'utils/option-utils';

const useIndicatorsTab = ({
  filterParams,
  serializedParams,
}) => {
  const [loading, setLoading] = useState(false);
  // In tiles, we will store all the filtered data that we will render in IndicatorNumberCards
  const [tiles, setTiles] = useState([]);
  const history = useHistory();
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

  useEffect(() => {
    const currentQueryParams = queryString.parse(history.location.search);
    const dateParams = startDate && endDate
      ? {
        startDate: moment(startDate).format(DateFormat.DD_MMM_YYYY),
        endDate: moment(endDate).format(DateFormat.DD_MMM_YYYY),
      }
      : {};
    const queryFilterParams = queryString.stringify({
      ...currentQueryParams,
      ...dateParams,
    });
    const { pathname } = history.location;
    history.push({ pathname, search: queryFilterParams });
  }, [startDate, endDate]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const responses = await Promise.all([
        fetchIndicatorProductsInventoried(params),
        fetchIndicatorInventoryAccuracy(params),
        fetchIndicatorInventoryShrinkage(params),
      ]);

      const mergedTiles = responses
        .filter((item) => cycleCountReportingIndicators[item.name])
        .map((item) => ({
          ...cycleCountReportingIndicators[item.name],
          ...item,
        }));
      setTiles(mergedTiles);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (startDate && endDate) {
      fetchData();
    }
  }, [startDate, endDate, serializedParams]);

  return {
    loading,
    tiles,
  };
};

export default useIndicatorsTab;
