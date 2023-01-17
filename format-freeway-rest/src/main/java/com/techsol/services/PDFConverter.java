package com.techsol.services;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import javax.print.attribute.standard.Media;
import javax.servlet.ServletContext;
import javax.servlet.annotation.MultipartConfig;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.json.JSONObject;

import com.techsol.config.Constants;
import com.techsol.pdfconverter.Converter;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 1,
    maxFileSize = 1024 * 1024 * 10,
    maxRequestSize = 1024 * 1024 * 100
)
@Path("/pdfconverter")
public class PDFConverter {
    @Context
    ServletContext context;

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
                    String paramName = attachment.getContentDisposition().getParameters().get("name");
                    if (paramName.equals("file")) {
                        InputStream fileInputStream = attachment.getObject(InputStream.class);
                        ContentDisposition contentDisposition = attachment.getContentDisposition();
                        String fileName = contentDisposition.getFilename();
                        String[] splitFileName = fileName.split(".pdf");
                        if (splitFileName.length == 0) {
                            responseObject.put("status", 400);
                            responseObject.put("statusMessages", "Please upload a PDF file");
                            return responseObject.toString();
                        }
                        String outputFileName = context.getRealPath("/") + Constants.PDFConverterDirectory + fileName.replace(".pdf", ".txt");
                        Converter converter = new Converter(fileInputStream, outputFileName);
                        if (!converter.checkIfFileExists()) { 
                            System.out.println("File does not exist");
                            converter.createFile();
                        }
                        converter.convertPdfToText();
                        responseObject.put("fileContents", converter.getFileTextContents());
                    }
                } catch (Exception e) {
                    responseObject.put("status", 500);
                    responseObject.put("statusMessage", "A problem occurred");
                    e.printStackTrace();
                    return responseObject.toString();
                }
            }
        }

        responseObject.put("status", 200);
        responseObject.put("statusMessage", "Successfully converted pdf to text");
        return responseObject.toString();
    }

    @Path("/downloadText")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadText(String payload) {
        JSONObject payloadObject = new JSONObject(payload);
        JSONObject responseObject = new JSONObject();

        try {
            if (!payloadObject.has("fileName")) {
                responseObject.put("status", 404);
                responseObject.put("statusMessage", "The file name is missing");
                return Response.status(400).entity(responseObject.toString()).build();
            } 
    
            String fileName = payloadObject.getString("fileName");
    
            if (fileName == null || fileName.length() == 0) {
                responseObject.put("status", 404);
                responseObject.put("statusMessage", "The file name is missing");
                return Response.status(400).entity(responseObject.toString()).build();
            }
    
            File file = new File(context.getRealPath("/") + Constants.PDFConverterDirectory + fileName.replace(".pdf", ".txt"));
    
            if (file.exists()) {
                return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                .build();
            } else {
                responseObject.put("status", 404);
                responseObject.put("statusMessage", "File does not exist");
                return Response.status(404).entity(responseObject.toString()).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseObject.put("status", 500);
            responseObject.put("statusMessage", "An exception occurred");
            return Response.status(500).entity(responseObject.toString()).build();
        }
    }
}                   
