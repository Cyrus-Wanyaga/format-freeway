package com.techsol.pdfconverter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;

public class Converter {
    private InputStream input;
    private String outputFileName;

    public Converter() {
    };

    public Converter(InputStream input, String outputFileName) {
        this.input = input;
        this.outputFileName = outputFileName;
    }

    public InputStream getInput() {
        return input;
    }

    public void setInput(InputStream input) {
        this.input = input;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public void convertPdfToText() {
        try {
            OutputStream output = new FileOutputStream(new File(outputFileName));
            BodyContentHandler bodyContentHandler = new BodyContentHandler(output);
            Metadata metadata = new Metadata();
            ParseContext parseContext = new ParseContext();

            PDFParser parser = new PDFParser();
            parser.parse(input, bodyContentHandler, metadata, parseContext);

            output.close();
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getFileTextContents() {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(this.outputFileName));
            char[] buffer = new char[1024];
            int numRead;
            while ((numRead = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, numRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return builder.toString();
    }
}
