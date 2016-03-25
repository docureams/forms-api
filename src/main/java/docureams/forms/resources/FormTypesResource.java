package docureams.forms.resources;

import docureams.forms.core.FormType;
import docureams.forms.db.FormTypeDAO;
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
    public FormType add(@Valid FormType formType) {
        long newId = formTypeDAO.insert(formType);
        return formType.setId(newId);
    }

    @POST
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    public FormType add(
            @FormParam("name") String name, 
            @FormParam("description") String description,
            @FormParam("pdfTemplate") String pdfTemplate,
            @FormParam("jsonMetadata") String jsonMetadata) {
        FormType formType = new FormType()
                .setName(name)
                .setDescription(description)
                .setPdfTemplate(pdfTemplate)
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
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    public FormType update(
            @PathParam("name") String name,
            @FormParam("description") String description,
            @FormParam("pdfTemplate") String pdfTemplate,
            @FormParam("jsonMetadata") String jsonMetadata) {
        FormType formType = new FormType()
                .setName(name)
                .setDescription(description)
                .setPdfTemplate(pdfTemplate)
                .setJsonMetadata(jsonMetadata);
        formTypeDAO.update(formType);
        return formType;
    }
    
    @Produces({MediaType.APPLICATION_OCTET_STREAM})
    @GET
    @Path("/{name}/asp")
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
            .header("content-disposition","attachment; filename = " + name.toUpperCase() + ".asp")
            .build();
    }

}