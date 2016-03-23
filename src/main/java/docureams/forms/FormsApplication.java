package docureams.forms;

import docureams.forms.db.FormDAO;
import docureams.forms.db.FormTypeDAO;
import docureams.forms.resources.FormTypesResource;
import docureams.forms.resources.FormsResource;
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayBundle;
import io.dropwizard.flyway.FlywayFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.flywaydb.core.Flyway;
import org.skife.jdbi.v2.DBI;

public class FormsApplication extends Application<FormsConfiguration> {

    public static void main(final String[] args) throws Exception {
        new FormsApplication().run(args);
    }

    @Override
    public String getName() {
        return "forms-api";
    }

    @Override
    public void initialize(final Bootstrap<FormsConfiguration> bootstrap) {
        bootstrap.addBundle(new FlywayBundle<FormsConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(FormsConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }

            @Override
            public FlywayFactory getFlywayFactory(FormsConfiguration configuration) {
                return configuration.getFlywayFactory();
            }
        });
    }

    @Override
    public void run(final FormsConfiguration config,
                    final Environment environment) throws ClassNotFoundException {
        final DBIFactory factory = new DBIFactory();
        final DBI jdbi = factory.build(environment, config.getDataSourceFactory(), "mysql");
        final FormTypeDAO formTypeDao = jdbi.onDemand(FormTypeDAO.class);
        final FormDAO formDao = jdbi.onDemand(FormDAO.class);
        environment.jersey().register(new FormTypesResource(formTypeDao));
        environment.jersey().register(new FormsResource(formDao));
        
        Flyway flyway = new Flyway();
        flyway.setDataSource(config.getDataSourceFactory().getUrl(), config.getDataSourceFactory().getUser(), config.getDataSourceFactory().getPassword());
        flyway.migrate();
    }
}
