package org.pih.warehouse

class ScriptTagLib {

    Closure jquery = { Map attrs, Closure body ->
        StringBuilder script = new StringBuilder()
        script.append("<script src=\"https://code.jquery.com/jquery-3.3.1.slim.min.js\"")
        script.append(" integrity=\"sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo\"")
        script.append(" crossorigin=\"anonymous\">")
        script.append("</script>")
        out.println script.toString()
    }
}
