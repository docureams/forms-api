package db.migration;

import java.io.InputStream;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class V2__Insert_BLOB_Files implements JdbcMigration {

    @Override
    public void migrate(Connection connection) throws Exception {
        loadFile(connection, "f1094b", "f1094b.pdf");
        loadFile(connection, "f1094c", "f1094c.pdf");
        loadFile(connection, "f1095b", "f1095b.pdf");
        loadFile(connection, "f1095b_page1", "f1095b_page1.pdf");
        loadFile(connection, "f1095c", "f1095c.pdf");
        loadFile(connection, "f1095c_page1", "f1095c_page1.pdf");
    }
    
    private void loadFile(Connection connection, String name, String filename) throws Exception {
        PreparedStatement statement = connection.prepareStatement("UPDATE form_type SET pdf_template = ? WHERE name = ?");
        InputStream stream = null;
        try {
            stream = this.getClass().getClassLoader().getResourceAsStream("assets/"+filename);
            statement.setBinaryStream(1, stream);
            statement.setString(2, name);
            statement.execute();
        } finally {
            statement.close();
            if (stream != null) stream.close();
        }        
    }
}
