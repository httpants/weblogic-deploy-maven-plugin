package au.com.cyberavenue.weblogic.deploy.plugin;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "updateDomain", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, requiresProject = false)
public class UpdateDomainMojo extends AbstractWeblogicDeployMojo {

    @Override
    String getWeblogicDeployCommand() {
        return "updateDomain";
    }

}
