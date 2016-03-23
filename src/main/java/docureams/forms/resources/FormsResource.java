package docureams.forms.resources;

import docureams.forms.core.Form;
import docureams.forms.db.FormDAO;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/forms")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public class FormsResource {

    FormDAO formDAO;

    public FormsResource(FormDAO formDAO) {
        this.formDAO = formDAO;
    }

    @GET
    public List<Form> getAll(){
        return formDAO.getAll();
    }

    @GET
    @Path("/{id}")
    public Form get(@PathParam("id") Integer id){
        return formDAO.findById(id);
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
}