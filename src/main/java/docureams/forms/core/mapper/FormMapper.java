package docureams.forms.core.mapper;

import docureams.forms.core.Form;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class FormMapper implements ResultSetMapper<Form>
{
    @Override
    public Form map(int index, ResultSet resultSet, StatementContext statementContext) throws SQLException
    {
        return new Form()
                .setId(resultSet.getInt("id"))
                .setName(resultSet.getString("name"));
    }
}