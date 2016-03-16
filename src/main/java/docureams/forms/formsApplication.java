package docureams.forms;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class formsApplication extends Application<formsConfiguration> {

    public static void main(final String[] args) throws Exception {
        new formsApplication().run(args);
    }

    @Override
    public String getName() {
        return "forms";
    }

    @Override
    public void initialize(final Bootstrap<formsConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final formsConfiguration configuration,
                    final Environment environment) {
        // TODO: implement application
    }

}
