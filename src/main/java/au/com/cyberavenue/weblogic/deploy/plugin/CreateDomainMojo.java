package au.com.cyberavenue.weblogic.deploy.plugin;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "createDomain", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, requiresProject = false)
public class CreateDomainMojo extends AbstractWeblogicDeployMojo {

    @Override
    String getWeblogicDeployCommand() {
        return "createDomain";
    }

}
