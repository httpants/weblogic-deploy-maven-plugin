package au.com.cyberavenue.weblogic.deploy.plugin;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "discoverDomain", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, requiresProject = false)
public class DiscoverDomainMojo extends AbstractWeblogicDeployMojo {

    @Override
    String getWeblogicDeployCommand() {
        return "discoverDomain";
    }

}
