package docureams.forms.resources;

import docureams.forms.core.FormType;
import docureams.forms.db.FormTypeDAO;
import java.io.File;
import java.nio.charset.StandardCharsets;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import javax.ws.rs.core.Response;

@Path("/formTypes")
@Produces({MediaType.APPLICATION_JSON})
public class FormTypesResource {

    FormTypeDAO formTypeDAO;

    public FormTypesResource(FormTypeDAO formTypeDAO) {
        this.formTypeDAO = formTypeDAO;
    }

    @GET
    public List<FormType> getAll(){
        return formTypeDAO.getAll();
    }

    @GET
    @Path("/{name}")
    public Response get(@PathParam("name") String name){
        FormType formType = formTypeDAO.findByName(name);
        if (formType == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(formType).build();
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    public FormType create(@Valid FormType formType) {
        long newId = formTypeDAO.insert(formType);
        return formType.setId(newId);
    }

    @POST
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public FormType create(
            @FormParam("name") String name, 
            @FormParam("description") String description,
            @FormParam("pdfTemplate") File pdfTemplate,
            @FormParam("pageFilter") String pageFilter,
            @FormParam("jsonMetadata") String jsonMetadata) {
        FormType formType = new FormType()
                .setName(name)
                .setDescription(description)
                .setPdfTemplate(pdfTemplate)
                .setPageFilter(pageFilter)
                .setJsonMetadata(jsonMetadata);
        long newId = formTypeDAO.insert(formType);
        return formType.setId(newId);
    }

    @PUT
    @Path("/{name}")
    @Consumes({MediaType.APPLICATION_JSON})
    public FormType update(
            @PathParam("name") String name, 
            @Valid FormType formType) {
        formType = formType.setName(name);
        formTypeDAO.update(formType);
        return formType;
    }

    @PUT
    @Path("/{name}")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public FormType update(
            @PathParam("name") String name,
            @FormParam("description") String description,
            @FormParam("pdfTemplate") File pdfTemplate,
            @FormParam("pageFilter") String pageFilter,
            @FormParam("jsonMetadata") String jsonMetadata) {
        FormType formType = new FormType()
                .setName(name)
                .setDescription(description)
                .setPdfTemplate(pdfTemplate)
                .setPageFilter(pageFilter)
                .setJsonMetadata(jsonMetadata);
        formTypeDAO.update(formType);
        return formType;
    }
    
    @GET
    @Path("/pdf/{name}")
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
    @Path("/asp/{name}")
    @Produces({MediaType.APPLICATION_OCTET_STREAM})
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