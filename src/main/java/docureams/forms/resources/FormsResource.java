package docureams.forms.resources;

import docureams.forms.core.Form;
import docureams.forms.core.FormType;
import docureams.forms.db.FormDAO;
import docureams.forms.db.FormTypeDAO;
import java.io.File;
import java.io.InputStream;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Response;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.glassfish.jersey.media.multipart.FormDataParam;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public class FormsResource {

    FormDAO formDAO;
    FormTypeDAO formTypeDAO;

    public FormsResource(FormDAO formDAO, FormTypeDAO formTypeDAO) {
        this.formDAO = formDAO;
        this.formTypeDAO = formTypeDAO;
    }

    @GET
    @Path("/forms")
    public List<Form> getAll(){
        return formDAO.getAll();
    }

    @GET
    @Path("/forms/{id}")
    public Response get(@PathParam("id") Integer id){
        Form form = formDAO.findById(id);
        if (form == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(form).build();
    }

    @POST
    @Path("/forms")
    @Consumes(MediaType.APPLICATION_JSON)
    public Form create(@Valid Form form) {
        long newId = formDAO.insert(form);
        return form.setId(newId);
    }

    @POST
    @Path("/forms")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response create(
            @FormParam("name") String name, 
            @FormParam("jsonData") String jsonData) {
        FormType formType = formTypeDAO.findByName(name);
        if (formType == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        Form form = new Form()
                .setName(name)
                .setJsonData(jsonData);
        long newId = formDAO.insert(form);
        return Response.ok(form.setId(newId)).build();
    }

    @POST
    @Path("/forms/pdf")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response createFromPdf(
            @FormDataParam("name") String name, 
            @FormDataParam("pdfFile") InputStream pdfStream) {
        FormType formType = formTypeDAO.findByName(name);
        if (formType == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        Form form = new Form()
                .setName(name)
                .setJsonData(formType.parsePdf(pdfStream));
        long newId = formDAO.insert(form);
        return Response.ok(form.setId(newId)).build();
    }

    @PUT
    @Path("/forms/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Form update(
            @PathParam("id") Integer id, 
            @Valid Form form) {
        form.setId(id);
        formDAO.update(form);
        return form;
    }

    @POST
    @Path("/forms/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Form update(
            @PathParam("id") Integer id, 
            @FormParam("name") String name, 
            @FormParam("jsonData") String jsonData) {
        Form form = new Form()
                .setId(id)
                .setName(name)
                .setJsonData(jsonData);
        formDAO.update(form);
        return form;
    }

    @POST
    @Path("/forms/pdf/{id}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateFromPdf(
            @PathParam("id") Long id, 
            @FormDataParam("pdfFile") InputStream pdfStream) {
        Form form = formDAO.findById(id);
        if (form == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        FormType formType = formTypeDAO.findByName(form.getName());
        if (formType == null) {
            return Response.serverError().build();
        }
        form.setJsonData(formType.parsePdf(pdfStream));
        formDAO.update(form);
        return Response.ok(form).build();
    }

    @DELETE
    @Path("/forms/{id}")
    public void delete(@PathParam("id") Integer id) {
        formDAO.deleteById(id);
    }
    
    @GET
    @Path("/forms/pdf")
    @Produces("application/pdf")
    public Response mergeAsPdf(
            @QueryParam("ids") String ids, 
            @QueryParam("filename") String filename) {
        try {
            PDDocument destination = null;
            PDFMergerUtility merger = new PDFMergerUtility();
            for (String id : ids.split(",")) {
                Form form = formDAO.findById(Long.parseLong(id));
                if (form == null) {
                    return Response.serverError().build();
                }
                FormType formType = formTypeDAO.findByName(form.getName());
                if (formType == null) {
                    return Response.serverError().build();
                }
                PDDocument document = formType.generatePdf(form.getJsonData());
                if (document == null) {
                    return Response.serverError().build();
                }
                document.getDocumentCatalog().getAcroForm().flatten();
                if (destination != null) {
                    merger.appendDocument(destination, document);
                    document.close();
                } else {
                    destination = document;
                }
            }
            File tempFile = File.createTempFile("form", ".pdf");
            tempFile.deleteOnExit();
            destination.save(tempFile.getCanonicalPath());
            destination.close();
            
            return Response
                .ok(tempFile)
                .header("content-type", "application/pdf")
                .header("content-disposition","attachment; filename=\"" + (filename != null ? filename : "form-"+ids+".pdf") + "\"")
                .build();
        } catch (Exception ex) {
            Logger.getLogger(FormsResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().build();
        }
    }

}