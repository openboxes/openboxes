Golden ZPL for `api.regression.obaf.labels.ZebraLabelIntegrationSpec`: OBAF's three bundled templates
(`grails-app/conf/templates/zpl-boarcodes-templates/*.zpl`) rendered by BeanPropertyTemplateService with the
spec's fixed fixtures (product code TEST-LBL-0001, container LPN-TEST-00001, bin TEST-BIN-A01).
Byte-exact, trailing newline included. A template change upstream fails the spec: review the new
output and replace the golden file in the same commit as the template change.
