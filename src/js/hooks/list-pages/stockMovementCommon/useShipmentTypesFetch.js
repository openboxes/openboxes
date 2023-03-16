import { useEffect } from 'react';

import { getTranslate } from 'react-localize-redux';
import { useDispatch, useSelector } from 'react-redux';

import { fetchShipmentTypes } from 'actions';
import { translateWithDefaultMessage } from 'utils/Translate';

const useShipmentTypesFetch = () => {
  const {
    shipmentTypesFetched,
    translate,
    fetchedTranslations,
    currentLocale,
  } = useSelector(state => ({
    shipmentTypesFetched: state.stockMovementCommon.shipmentTypesFetched,
    translate: translateWithDefaultMessage(getTranslate(state.localize)),
    fetchedTranslations: state.session.fetchedTranslations,
    currentLocale: state.session.activeLanguage,
  }));
  const dispatch = useDispatch();

  // Refetch the shipment types whenever we change the locale or translations are fetched
  useEffect(() => {
    if (translate && fetchedTranslations) {
      dispatch(fetchShipmentTypes(translate));
    }
  }, [currentLocale, fetchedTranslations]);


  // If translate function changes, refetch the shipmentTypes
  // shipmentTypesFetched flag is necessary to avoid situation,
  // when translations are fetched after shipmentTypes
  // which can result in shipment types not being translated
  useEffect(() => {
    // This if is needed to avoid infinite loop because of translate dependency
    if (!shipmentTypesFetched && translate) {
      dispatch(fetchShipmentTypes(translate));
    }
  }, [translate]);
};

export default useShipmentTypesFetch;
