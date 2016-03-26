package docureams.forms.core.mapper;

import docureams.forms.core.FormType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

public class FormTypeMapper implements ResultSetMapper<FormType>
{
    @Override
    public FormType map(int index, ResultSet resultSet, StatementContext statementContext) throws SQLException
    {
        File pdfTemplate = new File(resultSet.getString("name")+".pdf");
        try (
            InputStream inputStream = resultSet.getBinaryStream("pdfTemplate");
            OutputStream outputStream = new FileOutputStream(pdfTemplate)
        ) {
            int bytesRead = -1;
            byte[] buffer = new byte[4096];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FormTypeMapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FormTypeMapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return new FormType()
                .setId(resultSet.getInt("id"))
                .setName(resultSet.getString("name"))
                .setDescription(resultSet.getString("description"))
                .setPdfTemplate(pdfTemplate)
                .setJsonMetadata(resultSet.getString("json_metadata"));
    }
}