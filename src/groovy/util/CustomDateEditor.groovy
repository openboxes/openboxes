package util

import org.springframework.util.StringUtils

import java.beans.PropertyEditorSupport;
import java.text.ParseException;
import java.text.SimpleDateFormat;

class CustomDateEditor extends PropertyEditorSupport {

    private final List<String> formats

    private final boolean allowEmpty

    CustomDateEditor(List formats, boolean allowEmpty) {
        List<String> formatList = new ArrayList<String>(formats.size());
        for (Object format : formats) {
            formatList.add(format.toString())
        }
        this.formats = Collections.unmodifiableList(formatList)
        this.allowEmpty = allowEmpty
    }

    @Override
	String getAsText() {
		Date value = (Date) value;
		return (value != null ? new SimpleDateFormat(this.formats[0]).format(value) : "");
	}

    @Override
    void setAsText(String text) throws IllegalArgumentException {

        if (allowEmpty && !StringUtils.hasText(text)) {
			setValue(null);
		}
        else if (text) {
            for (String format : formats) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                try {
                    setValue(dateFormat.parse(text));
                    return;
                } catch (ParseException e) {
                    if (format == formats.last()) {
                        throw new IllegalArgumentException("Could not parse date: " + e.message, e);
                    }
                }
            }
        }
    }
}
