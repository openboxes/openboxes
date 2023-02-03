<input
    id="${attrs.id}-field"
    placeholder="${attrs.placeholder}"
    required="${attrs.required}"
    style="${attrs.style}"
    type="text"
    class="text"
/>
<input id="${attrs.id}" type="hidden" name="${attrs.name}"  />

<script language="javascript">
    $(document).ready(function() {

      $("#${attrs.id}-field")
        .on('input', function () {
        // prevent user from inputting any characters that are not digits, dot or a comma
        this.value = this.value.replace(/(?![\d,.])./g,'');

        // there is currently no easy way in javascript to reverse a localized number string
        // so we are reverse engineering this by localizing a test number eg. 1234.5
        // and extracting the thousand and decimal separators
        const [thousandSeparator, decimalSeparator] = (1234.5).toLocaleString("${attrs.locale ?: 'en'}").match(/(\D+)/g);
        const delocalizedNumberString = this.value
          .replaceAll(thousandSeparator, "")
          .replaceAll(decimalSeparator, ".")

        $("#${attrs.id}").val(parseFloat(delocalizedNumberString))
      })

    });
</script>
