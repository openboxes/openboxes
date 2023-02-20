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
          const parsedNumber = this.value.replace(/(?![\d"${decimalSeparator}"])./g, "");
          this.value = sign + parsedNumber
        })
    });
</script>
