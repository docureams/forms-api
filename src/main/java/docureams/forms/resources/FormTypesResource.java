package docureams.forms.resources;

import docureams.forms.core.FormType;
import docureams.forms.db.FormTypeDAO;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataParam;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public class FormTypesResource {

    FormTypeDAO formTypeDAO;

    public FormTypesResource(FormTypeDAO formTypeDAO) {
        this.formTypeDAO = formTypeDAO;
    }

    @GET
    @Path("/formTypes")
    public List<FormType> getAll(){
        return formTypeDAO.getAll();
    }

    @GET
    @Path("/formTypes/{name}")
    public Response get(@PathParam("name") String name){
        FormType formType = formTypeDAO.findByName(name);
        if (formType == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(formType).build();
    }

    @POST
    @Path("/formTypes")
    @Consumes(MediaType.APPLICATION_JSON)
    public FormType create(@Valid FormType formType) {
        long newId = formTypeDAO.insert(formType);
        return formType.setId(newId);
    }

    @POST
    @Path("/formTypes")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public FormType create(
            @FormDataParam("name") String name, 
            @FormDataParam("description") String description,
            @FormDataParam("pdfTemplate") InputStream pdfStream,
            @FormDataParam("pageFilter") String pageFilter,
            @FormDataParam("jsonMetadata") String jsonMetadata) {
        FormType formType = new FormType()
                .setName(name)
                .setDescription(description)
                .setPdfTemplate(pdfStream)
                .setPageFilter(pageFilter)
                .setJsonMetadata(jsonMetadata);
        long newId = formTypeDAO.insert(formType);
        return formType.setId(newId);
    }

    @PUT
    @Path("/formTypes/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    public FormType update(
            @PathParam("name") String name, 
            @Valid FormType formType) {
        formType = formType.setName(name);
        formTypeDAO.update(formType);
        return formType;
    }

    @POST
    @Path("/formTypes/{name}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public FormType update(
            @PathParam("name") String name,
            @FormDataParam("description") String description,
            @FormDataParam("pdfTemplate") InputStream pdfStream,
            @FormDataParam("pageFilter") String pageFilter,
            @FormDataParam("jsonMetadata") String jsonMetadata) {
        FormType formType = new FormType()
                .setName(name)
                .setDescription(description)
                .setPdfTemplate(pdfStream)
                .setPageFilter(pageFilter)
                .setJsonMetadata(jsonMetadata);
        formTypeDAO.update(formType);
        return formType;
    }
    
    @POST
    @Path("/formTypes/pdf")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response parseMetadataFromPdf(
            @FormDataParam("pdfTemplate") InputStream pdfStream) {
        return Response
                .ok(FormType.parseMetadataFromPdf(pdfStream))
                .build();
    }

    @GET
    @Path("/formTypes/pdf/{name}")
    @Produces("application/pdf")
    public Response getPdfTemplate(@PathParam("name") String name) {
        FormType formType = formTypeDAO.findByName(name);
        if (formType == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response
            .ok(formType.getPdfTemplate())
            .header("content-disposition","attachment; filename=\"" + name + ".pdf\"")
            .build();
    }
    
    @GET
    @Path("/formTypes/asp/{name}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getClientSdkForAsp(@PathParam("name") String name) {
        FormType formType = formTypeDAO.findByName(name);
        if (formType == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        String contents = formType.generateClientSdkForAsp();
        if (contents == null) {
            return Response.serverError().build();
        }
        
        return Response
            .ok(contents.getBytes(StandardCharsets.UTF_8))
            .header("content-disposition","attachment; filename=\"" + name.toUpperCase() + ".asp\"")
            .build();
    }

}