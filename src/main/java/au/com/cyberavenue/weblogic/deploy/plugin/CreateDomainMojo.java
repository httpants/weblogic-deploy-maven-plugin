package au.com.cyberavenue.weblogic.deploy.plugin;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;

@Mojo(name = "createDomain", requiresProject = false)
public class CreateDomainMojo extends AbstractMojo {

    @Parameter(property = "weblogic-deploy.groupId", defaultValue = "com.oracle.weblogic.lifecycle")
    private String weblogicDeployGroupId;

    @Parameter(property = "weblogic-deploy.artifactId", defaultValue = "weblogic-deploy-installer")
    private String weblogicDeployArtifactId;

    @Parameter(property = "weblogic-deploy.version", defaultValue = "1.9.19-SNAPSHOT")
    private String weblogicDeployVersion;

    @Parameter(property = "oracle_home", defaultValue = "${env.ORACLE_HOME}")
    private String oracleHome;

    @Parameter(property = "domain_parent")
    private String domainParent;

    @Parameter(property = "domain_home")
    private String domainHome;

    @Parameter(property = "domain_type")
    private String domainType;

    @Parameter(property = "java_home")
    private String javaHome;

    @Parameter(property = "archive_file")
    private String archiveFile;

    @Parameter(property = "model_file")
    private String modelFile;

    @Parameter(property = "variable_file")
    private String variableFile;

    @Parameter(property = "passphrase_env")
    private String passphraseEnv;

    @Parameter(property = "passphrase_file")
    private String passphraseFile;

    @Parameter(property = "opss_wallet_passphrase_env")
    private String opssWalletpassphraseEnv;

    @Parameter(property = "opss_wallet_passphrase_file")
    private String opssWalletpassphraseFile;

    @Parameter(property = "wlst_path")
    private String wlstPath;

    @Parameter(property = "rcu_database")
    private String rcuDatabase;

    @Parameter(property = "rcu_prefix")
    private String rcuPrefix;

    @Parameter(property = "rcu_db_user")
    private String rcuDbUser;

    @Parameter(property = "use_encryption", defaultValue = "false")
    private Boolean useEncryption;

    @Parameter(property = "run_rcu", defaultValue = "false")
    private Boolean runRCU;

    @Component
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("oracleHome = " + oracleHome);

        // download weblogic-deploy-installer
        executeMojo(
            plugin(
                groupId("org.apache.maven.plugins"),
                artifactId("maven-dependency-plugin"),
                version("3.2.0")),
            goal("unpack"),
            configuration(
                element(name("artifact"),
                    weblogicDeployGroupId + ":" + weblogicDeployArtifactId + ":" + weblogicDeployVersion + ":zip"),
                element(name("outputDirectory"), "target"),
                element(name("markersDirectory"), "target/.markers")),
            executionEnvironment(
                mavenSession,
                pluginManager));

        String commandLineArguments = buildCommandLineArgs();
        getLog().info("Executing createDomain.cmd " + commandLineArguments + "...");

        System.setProperty("org.slf4j.simpleLogger.log.org.codehaus.mojo.exec.ExecMojo", "OFF");

        // run the createDomain script
        executeMojo(
            plugin(
                groupId("org.codehaus.mojo"),
                artifactId("exec-maven-plugin"),
                version("3.0.0")),
            goal("exec"),
            configuration(
                element(name("executable"), "cmd"),
                element(name("arguments"), buildArgumentElements()),
                element(name("environmentVariables"),
                    element("ORACLE_HOME", oracleHome))),
            executionEnvironment(
                mavenSession,
                pluginManager));
    }

    private String buildCommandLineArgs() {
        return String.join(" ", buildArguments());
    }

    private Element[] buildArgumentElements() {
        return Stream
            .concat(Stream.of("/C", "target\\weblogic-deploy\\bin\\createDomain.cmd"),
                buildArguments().stream())
            .map(s -> element("argument", s))
            .toArray(Element[]::new);
    }

    private List<String> buildArguments() {

        List<String> arguments = new ArrayList<>();

        if (useEncryption) {
            arguments.add("-use_encryption");
        }

        if (runRCU) {
            arguments.add("-run_rcu");
        }

//        if (StringUtils.isNotBlank(oracleHome)) {
//            arguments.add("-oracle_home");
//            arguments.add(oracleHome);
//        }

        if (StringUtils.isNotBlank(domainParent)) {
            arguments.add("-domain_parent");
            arguments.add(domainParent);
        }

        if (StringUtils.isNotBlank(domainHome)) {
            arguments.add("-domain_home");
            arguments.add(domainHome);
        }

        if (StringUtils.isNotBlank(domainType)) {
            arguments.add("-domain_type");
            arguments.add(domainType);
        }

        if (StringUtils.isNotBlank(javaHome)) {
            arguments.add("-java_home");
            arguments.add(javaHome);
        }

        if (StringUtils.isNotBlank(archiveFile)) {
            arguments.add("-archive_file");
            arguments.add(archiveFile);
        }

        if (StringUtils.isNotBlank(modelFile)) {
            arguments.add("-model_file");
            arguments.add(modelFile);
        }

        if (StringUtils.isNotBlank(variableFile)) {
            arguments.add("-variable_file");
            arguments.add(variableFile);
        }

        if (StringUtils.isNotBlank(passphraseEnv)) {
            arguments.add("-passphrase_env");
            arguments.add(passphraseEnv);
        }

        if (StringUtils.isNotBlank(passphraseFile)) {
            arguments.add("-passphrase_file");
            arguments.add(passphraseFile);
        }

        if (StringUtils.isNotBlank(opssWalletpassphraseEnv)) {
            arguments.add("-opss_wallet_passphrase_env");
            arguments.add(opssWalletpassphraseEnv);
        }

        if (StringUtils.isNotBlank(opssWalletpassphraseFile)) {
            arguments.add("-opss_wallet_passphrase_file");
            arguments.add(opssWalletpassphraseFile);
        }

        if (StringUtils.isNotBlank(wlstPath)) {
            arguments.add("-wlst_path");
            arguments.add(wlstPath);
        }

        if (StringUtils.isNotBlank(rcuDatabase)) {
            arguments.add("-rcu_db");
            arguments.add(rcuDatabase);
        }

        if (StringUtils.isNotBlank(rcuPrefix)) {
            arguments.add("-rcu_prefix");
            arguments.add(rcuPrefix);
        }

        if (StringUtils.isNotBlank(rcuDbUser)) {
            arguments.add("-rcu_db_user");
            arguments.add(rcuDbUser);
        }

        return arguments;
    }

}
