package docureams.forms.db;

import docureams.forms.core.FormType;
import docureams.forms.core.mapper.FormTypeMapper;
import java.util.List;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

@RegisterMapper(FormTypeMapper.class)
public interface FormTypeDAO {

    @SqlQuery("select * from FORMTYPE")
    List<FormType> getAll();

    @SqlQuery("select * from FORMTYPE where NAME = :name")
    FormType findByName(@Bind("name") String name);

    @SqlUpdate("update into FORMTYPE set NAME = :name where ID = :id")
    int update(@BindBean FormType formType);

    @SqlUpdate("insert into FORMTYPE (ID, NAME) values (:id, :name)")
    int insert(@BindBean FormType formType);
}