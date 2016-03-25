package docureams.forms.resources;

import docureams.forms.core.Form;
import docureams.forms.core.FormType;
import docureams.forms.db.FormDAO;
import docureams.forms.db.FormTypeDAO;
import java.io.File;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Response;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFMergerUtility;

@Path("/forms")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class FormsResource {

    FormDAO formDAO;
    FormTypeDAO formTypeDAO;

    public FormsResource(FormDAO formDAO, FormTypeDAO formTypeDAO) {
        this.formDAO = formDAO;
        this.formTypeDAO = formTypeDAO;
    }

    @GET
    public List<Form> getAll(){
        return formDAO.getAll();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") Integer id){
        Form form = formDAO.findById(id);
        if (form == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(form).build();
    }

    @POST
    public Form add(@Valid Form form) {
        long newId = formDAO.insert(form);
        return form.setId(newId);
    }

    @PUT
    @Path("/{id}")
    public Form update(@PathParam("id") Integer id, @Valid Form form) {
        form = form.setId(id);
        formDAO.update(form);
        return form;
    }

    @DELETE
    @Path("/{id}")
    public void delete(@PathParam("id") Integer id) {
        formDAO.deleteById(id);
    }
    
    @GET
    @Path("/{id}/pdf")
    @Produces("application/pdf")
    public Response mergeAsPdf(@QueryParam("ids") String ids, @QueryParam("filename") String filename) {
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
                .header("content-disposition","attachment; filename = " + filename != null ? filename : "form-"+ids+".pdf")
                .build();
        } catch (Exception ex) {
            Logger.getLogger(FormsResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().build();
        }
    }

    
}