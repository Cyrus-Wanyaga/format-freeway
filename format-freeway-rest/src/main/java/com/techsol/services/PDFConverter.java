package com.techsol.services;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.json.JSONObject;

import com.techsol.pdfconverter.Converter;

@Path("/pdfconverter")
public class PDFConverter {
    @Path("/pdftotext")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public String convertPDFToText(MultipartBody multipartBody) {
        JSONObject responseObject = new JSONObject();
        List<Attachment> attachments = multipartBody.getAllAttachments();
        
        for (Attachment attachment : attachments) {
            if (attachment == null) {
                responseObject.put("status", 400);
                responseObject.put("statusMessage", "No file found");
                return responseObject.toString();
            } else {
                try {
                    String paramName = attachment.getContentDisposition().getParameters().get("file");
                    if (paramName.equals("file")) {
                        InputStream fileInputStream = attachment.getObject(InputStream.class);
                        ContentDisposition contentDisposition = attachment.getContentDisposition();
                        String fileName = contentDisposition.getFilename();
                        Converter converter = new Converter(fileInputStream, fileName);
                        converter.convertPdfToText();
                        responseObject.put("fileContents", converter.getFileTextContents());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        responseObject.put("status", 200);
        responseObject.put("statusMessage", "Successfully converted pdf to text");
        return responseObject.toString();
    }

    @Path("/downloadText")
    @POST
    public String downloadText() {
        return "";
    }
}
