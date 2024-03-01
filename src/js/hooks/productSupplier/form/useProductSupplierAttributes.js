import { useEffect } from 'react';

import { useDispatch, useSelector } from 'react-redux';
import { z } from 'zod';

import { fetchAttributes } from 'actions';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import entityTypeCode from 'consts/entityTypeCode';
import useTranslate from 'hooks/useTranslate';

const useProductSupplierAttributes = () => {
  const dispatch = useDispatch();
  const translate = useTranslate();
  const { attributes } = useSelector((state) => ({
    attributes: state.productSupplier.attributes,
  }));

  useEffect(() => {
    dispatch(fetchAttributes({
      params: {
        entityType: entityTypeCode.PRODUCT_SUPPLIER,
      },
    }));
  }, []);

  // When we have at least one option, it has to be select type
  // (allowOther doesn't matter in this case)
  const isSelectType = (attribute) => attribute?.options.length;

  // When we don't have options, and allowOther is set to true, it is text input
  const isTextType = (attribute) => attribute?.allowOther && !attribute?.options.length;

  const inputTypeSchema = (attribute, errorProps) => (attribute.required
    ? z
      .string(errorProps)
      .max(255, `Max length of ${attribute?.name} is 255`)
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

  /**
   * Function for mapping attribute options to align them with attributes
   * validation schema
   */
  const mapAttributeOptions = (attribute, includeOther) => {
    const options = attribute?.options.map((option) =>
      ({ id: attribute?.id, value: option, label: option }));

    return includeOther
      ? [...options, {
        id: attribute?.id,
        value: 'Other',
        label: translate('react.productSupplier.form.selectOtherValue.label', 'Other'),
      }]
      : options;
  };

  /**
   * When the attribute has the allowOther property set to true and has no options, it should be
   * text input. When the attribute has the allowOther property set to true and has available
   * options, then it should be a select field, but with the additional option "Other". In all
   * other cases, it should be just a select field with the options fetched from the API.
   */
  const getAttributesWithInputTypes = (attributesToMap) =>
    attributesToMap.map((attribute) => {
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
          options: mapAttributeOptions(attribute, true),
        },
        Input: SelectField,
      };
    });

  return {
    attributes,
    attributesWithInputTypes: getAttributesWithInputTypes(attributes),
    inputTypeSchema,
    selectTypeSchema,
    isTextType,
    isSelectType,
  };
};

export default useProductSupplierAttributes;
