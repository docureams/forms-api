package docureams.forms;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class FormsApplication extends Application<FormsConfiguration> {

    public static void main(final String[] args) throws Exception {
        new FormsApplication().run(args);
    }

    @Override
    public String getName() {
        return "forms";
    }

    @Override
    public void initialize(final Bootstrap<FormsConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final FormsConfiguration configuration,
                    final Environment environment) {
        // TODO: implement application
    }

}
