package docureams.forms.core.mapper;

import docureams.forms.core.FormType;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class FormTypeMapper implements ResultSetMapper<FormType>
{
    @Override
    public FormType map(int index, ResultSet resultSet, StatementContext statementContext) throws SQLException
    {
        return new FormType()
                .setId(resultSet.getInt("id"))
                .setName(resultSet.getString("name"))
                .setDescription(resultSet.getString("description"))
                .setPdfTemplate(resultSet.getString("pdf_template"))
                .setJsonMetadata(resultSet.getString("json_metadata"));
    }
}