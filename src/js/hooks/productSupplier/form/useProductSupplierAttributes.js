import { useEffect, useState } from 'react';

import { useDispatch, useSelector } from 'react-redux';
import { z } from 'zod';

import { fetchAttributes } from 'actions';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import entityTypeCode from 'consts/entityTypeCode';
import useTranslate from 'hooks/useTranslate';

const useProductSupplierAttributes = () => {
  const [isAttributeModalOpen, setIsAttributeModalOpen] = useState(false);
  const [selectedAttribute, setSelectedAttribute] = useState(null);
  const dispatch = useDispatch();
  const translate = useTranslate();
  const { attributes } = useSelector((state) => ({
    attributes: state.productSupplier.attributes,
  }));

  const closeAttributeModal = () => setIsAttributeModalOpen(false);

  useEffect(() => {
    const controller = new AbortController();
    dispatch(fetchAttributes({
      signal: controller.signal,
      params: {
        entityType: entityTypeCode.PRODUCT_SUPPLIER,
      },
    }));

    return () => {
      controller.abort();
    };
  }, []);

  // When we have at least one option, it has to be select type
  // (allowOther doesn't matter in this case)
  const isSelectType = (attribute) => attribute?.options.length
    || (!attribute?.options.length && !attribute?.allowOther);

  // When we don't have options, and allowOther is set to true, it is text input
  const isTextType = (attribute) => attribute?.allowOther && !attribute?.options.length;

  const inputTypeSchema = (attribute, errorProps) => (attribute.required
    ? z
      .string(errorProps)
      .max(255, `Max length of ${attribute?.name} is 255`)
      .trim()
      .min(1, errorProps?.required_error)
    : z
      .string()
      .max(255, `Max length of ${attribute?.name} is 255`)
      .optional()
  );

  const selectTypeSchema = (attribute, errorProps) => (attribute?.required
    ? z.object({
      value: z.string(),
      label: z.string(),
    }, errorProps)
      .required()
    : z.object({
      value: z.string(),
      label: z.string(),
    })
      .nullish()
  );

  const selectFieldProps = (attribute) => ({
    createNewFromModalLabel: translate(
      'react.productSupplier.form.selectOtherValue.label',
      'Other',
    ),
    createNewFromModal: true,
    newOptionModalOpen: () => {
      setIsAttributeModalOpen(true);
      setSelectedAttribute(attribute);
    },
  });

  /**
   * Function for mapping attribute options to align them with attributes
   * validation schema
   */
  const mapAttributeOptions = (attribute) => attribute?.options.map((option) =>
    ({ id: attribute?.id, value: option, label: option }));

  /**
   * When the attribute has the allowOther property set to true and has no options, it should be
   * text input. When the attribute has the allowOther property set to true and has available
   * options, then it should be a select field, but with the additional option "Other". In all
   * other cases, it should be just a select field with the options fetched from the API.
   * When field doesn't have options and allowOther then we create a select with "other" option.
   */
  const getAttributesWithInputTypes = (attributesToMap) =>
    (attributesToMap || []).map((attribute) => {
      if (!attribute?.options.length && !attribute?.allowOther) {
        return {
          attribute,
          inputParams: selectFieldProps(attribute),
          Input: SelectField,
        };
      }

      if (!attribute?.allowOther) {
        return {
          attribute: { ...attribute, options: mapAttributeOptions(attribute) },
          Input: SelectField,
        };
      }

      if (!attribute?.options.length) {
        return {
          attribute,
          Input: TextInput,
        };
      }

      return {
        attribute: {
          ...attribute,
          options: mapAttributeOptions(attribute),
        },
        inputParams: selectFieldProps(attribute),
        Input: SelectField,
      };
    });

  const mapFetchedAttributes = (fetchedAttributes) => fetchedAttributes.reduce((acc, attribute) => {
    const field = attributes.find((currentAttribute) =>
      currentAttribute?.id === attribute?.attribute?.id);

    if (isSelectType(field)) {
      return {
        ...acc,
        [attribute?.attribute?.id]: {
          id: attribute?.attribute?.id,
          value: attribute?.value,
          label: attribute?.value,
        },
      };
    }

    return {
      ...acc,
      [attribute?.attribute?.id]: attribute?.value,
    };
  }, {});

  return {
    attributes,
    isAttributeModalOpen,
    selectedAttribute,
    closeAttributeModal,
    attributesWithInputTypes: getAttributesWithInputTypes(attributes),
    inputTypeSchema,
    selectTypeSchema,
    isTextType,
    isSelectType,
    mapFetchedAttributes,
  };
};

export default useProductSupplierAttributes;
