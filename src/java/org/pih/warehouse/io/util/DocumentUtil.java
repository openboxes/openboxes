package org.pih.warehouse.io.util;

import java.io.*;
import org.apache.poi.hwpf.*;
import org.apache.poi.hwpf.usermodel.*;

public class DocumentUtil {

	public static void writeDocument() {
		try {

			InputStream fis = new FileInputStream("/home/jmiranda/Desktop/input.doc");
			HWPFDocument doc = new HWPFDocument(fis);
			//Range range = doc.getRange();
			//range.insertAfter("Hello World!!! HAHAHAHAHA I DID IT!!!");

			Range range = doc.getRange();
			System.out.println("range: " + range);
			Section section = range.getSection(0);
			System.out.println("section: " + section.getCharacterRun(0));
			Paragraph para = section.getParagraph(0);
			System.out.println("paragraph: " + para.getCharacterRun(0).text());
			String text = para.getCharacterRun(0).text();
			System.out.println("text: " + text);
			range.insertBefore("New ");
			OutputStream fos = new FileOutputStream("/home/jmiranda/Desktop/output.doc");
			doc.write(fos);
			
			/*
			Range range = new Range(insertionPoint, (insertionPoint + 2), daDoc);
			range.insertBefore(textToInsert);

			// we need to let the model re-calculate the Range before we
			// evaluate it
			range = daDoc.getRange();

			assertEquals(1, range.numSections());
			Section section = range.getSection(0);

			assertEquals(3, section.numParagraphs());
			Paragraph para = section.getParagraph(2);
			assertEquals((textToInsert + originalText), para.text());

			assertEquals(3, para.numCharacterRuns());
			String text = para.getCharacterRun(0).text() + para.getCharacterRun(1).text()
					+ para.getCharacterRun(2).text();

			OutputStream out = new FileOutputStream("/home/jmiranda/Desktop/output.doc");
			doc.write(out);

			out.flush();
			out.close();
			*/

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		DocumentUtil.writeDocument();
	}

}
