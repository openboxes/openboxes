package org.liquibase.grails;

import liquibase.xml.XmlWriter;
import org.w3c.dom.Document;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;

import java.io.OutputStream;
import java.io.IOException;

public class GrailsXmlWriter implements XmlWriter {

    public void write(Document doc, OutputStream outputStream) throws IOException {
        OutputFormat format = new OutputFormat(doc);
        format.setIndenting(true);
        XMLSerializer serializer = new XMLSerializer(outputStream, format);
        serializer.asDOMSerializer();
        serializer.serialize(doc);

    }
}
