// Vendored from org.grails.plugins:csv:1.0.1 (Apache-2.0); the plugin has no Grails 7 release.
// Source: repo.grails.org csv-1.0.1-sources.jar. Unmodified except this header.

package grails.plugins.csv
/**
 * Writes CSV content to a given writer, using a definition DSL.
 *
 * Examples:
 *
 * def sw = new StringWriter()
 * def b = new CSVBuilder(sw) {
 *   col1 { it.val1 }
 *   col2 { it.val2 }
 * }
 * b << [val1: 'a', val2: 'b']
 * b << [val1: 'c', val2: 'd']
 *
 * assert b.writer.toString() == '''"col1","col2"
 * "a",b"
 * "c","d"'''
 *
 * This class is NOT threadsafe.
 *
 * @author Luke Daley
 */
class CSVWriter {

	private columns = [:]

	final writer

	private cachedQuote
	private cachedQuoteEscape
	private cachedQuoteReplace
	private cachedValueSeperator
	private cachedRowSeperator

	private producers
	private lastProducer

	private headingsWritten = false

	CSVWriter(Writer writer, Closure definition) {
		this.writer = writer

		columns = CSVWriterColumnsBuilder.build(definition)

		// do these once incase subclasses are reading from config etc.
		cachedQuote = this.quote
		cachedQuoteEscape = this.quoteEscape
		cachedQuoteReplace = this.quoteEscape + this.quote
		cachedValueSeperator = this.valueSeperator
		cachedRowSeperator = this.rowSeperator

		producers = columns.values().toList()
		lastProducer = producers.last()
	}

	def leftShift(row) {
		write(row)
		this
	}

	def write(row) {
		if (!headingsWritten) {
			writeHeadings()
		}

		writer << this.@cachedRowSeperator
		for (producer in this.@producers) {
			writeValue(producer(row).toString())
			if (!producer.is(this.@lastProducer)) {
				writer << this.@cachedValueSeperator
			}
		}

		writer
	}

	def writeAll(Collection rows) {
		for (row in rows) {
			write(row)
		}
		writer
	}

	protected writeHeadings() {
		columns.eachWithIndex { column, i ->
			writeValue(column.key)
			if (i != (columns.size() - 1)) {
				writer << this.@cachedValueSeperator
			}
		}
		headingsWritten = true
	}

	protected writeValue(String value) {
		writer << this.@cachedQuote
		writer << value.replace(this.@cachedQuote, this.@cachedQuoteReplace)
		writer << this.@cachedQuote
	}

	protected getQuote() {
		'"'
	}

	protected getQuoteEscape() {
		'"'
	}

	protected getValueSeperator() {
		","
	}

	protected getRowSeperator() {
		"\n"
	}
}

class CSVWriterColumnsBuilder {

	final columns = [:]

	CSVWriterColumnsBuilder(Closure definition) {
		definition.delegate = this
		definition()
	}

	def methodMissing(String name, args) {
		if (args.size() == 1 && args[0] instanceof Closure) {
			columns[name] = args[0]
		} else {
			throw new IllegalArgumentException('Must have 1 closure argument')
		}
	}

	static build(Closure definition) {
		new CSVWriterColumnsBuilder(definition).columns
	}
}