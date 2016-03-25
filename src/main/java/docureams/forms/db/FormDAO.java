package docureams.forms.db;

import docureams.forms.core.Form;
import docureams.forms.core.mapper.FormMapper;
import java.util.List;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(FormMapper.class)
public interface FormDAO {

    @SqlQuery("SELECT * FROM form")
    List<Form> getAll();

    @SqlQuery("SELECT * FROM form WHERE id = :id")
    Form findById(@Bind("id") long id);

    @SqlUpdate("DELETE FROM form WHERE id = :id")
    long deleteById(@Bind("id") long id);

    @SqlUpdate("UPDATE form SET name = :name, json_data = :jsonData WHERE id = :id")
    long update(@BindBean Form form);

    @GetGeneratedKeys
    @SqlUpdate("INSERT INTO form (id, name, json_data) VALUES (:id, :name, :jsonData)")
    long insert(@BindBean Form form);
}