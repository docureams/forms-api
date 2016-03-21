package docureams.forms;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayFactory;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class FormsConfiguration extends Configuration {
    @Valid
    @NotNull
    @JsonProperty
    private final DataSourceFactory database = new DataSourceFactory();

    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @Valid
    @NotNull
    @JsonProperty
    private final FlywayFactory flyway = new FlywayFactory();

    public FlywayFactory getFlywayFactory() {
        return flyway;
    }
}
