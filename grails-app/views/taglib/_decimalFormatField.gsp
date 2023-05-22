<input
    id="${attrs.id}"
    name="${attrs.name}"
    placeholder="${attrs.placeholder}"
    required="${attrs.required}"
    style="${attrs.style}"
    size="${attrs.size}"
    value="${attrs.value}"
    type="text"
    class="text"
/>
<script>
  $(document)
    .ready(function () {
      $("#${attrs.id}")
        .on("input", function () {
          const sign = this.value.substring(0, 1) === "-" ? "-" : ""
          // prevent user from inputting any other characters than: digits or decimal separator of the current locale
          let parsedNumber = this.value.replace(/(?![\d"${decimalSeparator}"])./g, "");

          // handle decimal points
          if ("${attrs.decimal}") {
            // attrs.decimal is a string, so we need to cast it to a number
            const decimalPoint = parseInt("${attrs.decimal}")
            // given we have a number "12,345"  with a decimal separator ","
            // we split the number string into two parts: number (12) and decimal (345)
            const [number, decimal] = parsedNumber?.split("${decimalSeparator}");
            if (decimalPoint > 0) {
              // if decimalPoint is > 0 (for example 2), then return  a number with fixed decimal points like: "12" + "," + "34"
              parsedNumber = number + "${decimalSeparator}" + decimal.substring(0, decimalPoint);
            } else {
              // if decimal point is <= 0 the just return the number and ignore the decimal part
              parsedNumber = number;
            }
          }
          this.value = sign + parsedNumber
        })
    });
</script>
