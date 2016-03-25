package docureams.forms.db;

import docureams.forms.core.FormType;
import docureams.forms.core.mapper.FormTypeMapper;
import java.util.List;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(FormTypeMapper.class)
public interface FormTypeDAO {

    @SqlQuery("SELECT * FROM form_type")
    List<FormType> getAll();

    @SqlQuery("SELECT * FROM form_type WHERE name = :name")
    FormType findByName(@Bind("name") String name);

    @SqlUpdate("UPDATE form_type SET name = :name, description = :description, pdf_template = :pdfTemplate, json_metadata = :jsonMetadata WHERE id = :id")
    long update(@BindBean FormType formType);

    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO form_type (id, name, description, pdf_template, json_metadata) VALUES (:id, :name, :description, :pdfTemplate, :jsonMetadata)")
    long insert(@BindBean FormType formType);
}