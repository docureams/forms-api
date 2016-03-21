package docureams.forms.db;

import docureams.forms.core.Form;
import docureams.forms.core.mapper.FormMapper;
import java.util.List;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(FormMapper.class)
public interface FormDAO {

    @SqlQuery("select * from FORM")
    List<Form> getAll();

    @SqlQuery("select * from FORM where ID = :id")
    Form findById(@Bind("id") int id);

    @SqlUpdate("delete from FORM where ID = :id")
    int deleteById(@Bind("id") int id);

    @SqlUpdate("update into FORM set NAME = :name where ID = :id")
    int update(@BindBean Form form);

    @SqlUpdate("insert into FORM (ID, NAME) values (:id, :name)")
    int insert(@BindBean Form form);
}