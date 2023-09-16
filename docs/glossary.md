<script>
fetch("glossary.json")
    .then(response => response.json())
    .then(data => {
        console.log(data.glossary.con.conTerm)
        document.querySelector("#conTerm").innerText = data.glossary.con.conTerm});
        console.log(data.glossary.con.conDef)
        document.querySelector("#conDef").innerText = data.glossary.con.conDef});
    .then(data => {
        console.log(data.glossary.pro.proTerm)
        document.querySelector("#proTerm").innerText = data.glossary.pro.proTerm});
        console.log(data.glossary.pro.proDef)
        document.querySelector("#proDef").innerText = data.glossary.pro.proDef});
        console.log(data.glossary.pro.html)
        document.querySelector("#proHTML").innerText = data.glossary.pro.proHTML});
    // ...
</script>


<!--            Help! -->
<script>
let proDef = data.glossary.pro.proDef
let proHTML = data.glossary.pro.proHTML
let proConcat = proDef.concat(" ", proHTML);
document.getElementById("pro").innerHTML = proConcat;
</script>

# Glossary

<html>
<dl>
  <dt id="conterm"></dt>
  <dd id="conDef"></dd>
  <br>
  <dt id="proTerm"></dt>
  <dd id="pro"></dd>         <!-- ???? -->
</dl>


</html>