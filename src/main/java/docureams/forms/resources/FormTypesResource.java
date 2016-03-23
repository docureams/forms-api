package docureams.forms.resources;

import docureams.forms.core.FormType;
import docureams.forms.db.FormTypeDAO;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/formTypes")
@Consumes({MediaType.APPLICATION_JSON})
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
    public FormType get(@PathParam("name") String name){
        return formTypeDAO.findByName(name);
    }

    @POST
    public FormType add(@Valid FormType formType) {
        int newId = formTypeDAO.insert(formType);

        return formType.setId(newId);
    }

    @PUT
    @Path("/{name}")
    public FormType update(@PathParam("name") String name, @Valid FormType formType) {
        formType = formType.setName(name);
        formTypeDAO.update(formType);

        return formType;
    }
}