package au.com.cyberavenue.weblogic.deploy.plugin;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.attribute;
import static org.twdata.maven.mojoexecutor.MojoExecutor.attributes;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;

public abstract class AbstractWeblogicDeployMojo extends AbstractMojo {

    @Parameter(property = "weblogic-deploy.groupId", defaultValue = "com.oracle.weblogic.lifecycle")
    private String weblogicDeployGroupId;

    @Parameter(property = "weblogic-deploy.artifactId", defaultValue = "weblogic-deploy-installer")
    private String weblogicDeployArtifactId;

    @Parameter(property = "weblogic-deploy.version", defaultValue = "1.9.19-SNAPSHOT")
    private String weblogicDeployVersion;

    @Parameter(property = "oracle_home", defaultValue = "${env.ORACLE_HOME}")
    private String oracleHome;

    @Parameter(property = "wdt_custom_config", defaultValue = "${env.WDT_CUSTOM_CONFIG}")
    private String wdtCustomConfig;

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

    @Parameter(property = "rcu_db")
    private String rcuDb;

    @Parameter(property = "rcu_prefix")
    private String rcuPrefix;

    @Parameter(property = "rcu_db_user")
    private String rcuDbUser;

    @Parameter(property = "use_encryption", defaultValue = "false")
    private Boolean useEncryption;

    @Parameter(property = "run_rcu", defaultValue = "false")
    private Boolean runRCU;

    @Parameter(property = "downloadDirectory", defaultValue = "target")
    private String downloadDirectory;

    @Parameter(property = "admin_url")
    private String adminUrl;

    @Parameter(property = "admin_user")
    private String adminUser;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

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
                element(name("outputDirectory"), downloadDirectory),
                element(name("markersDirectory"), downloadDirectory + "/.markers")),
            executionEnvironment(
                mavenSession,
                pluginManager));

        String commandLineArguments = buildCommandLineArgs();
        getLog().info("Executing " + getWeblogicDeployCommand() + ".cmd " + commandLineArguments);

        System.setProperty("org.slf4j.simpleLogger.log.org.codehaus.mojo.exec.ExecMojo", "OFF");

        executeWeblogicDeployScript();
//        antRunWeblogicDeployScript();
    }

    abstract String getWeblogicDeployCommand();

    protected void executeWeblogicDeployScript() throws MojoExecutionException {
        executeMojo(
            plugin(
                groupId("org.codehaus.mojo"),
                artifactId("exec-maven-plugin"),
                version("3.0.0")),
            goal("exec"),
            buildExecCmdConfiguration(),
            executionEnvironment(
                mavenSession,
                pluginManager));
    }

    private String buildCommandLineArgs() {
        return String.join(" ", buildArguments());
    }

    protected Xpp3Dom buildExecCmdConfiguration() {
        return configuration(element(name("executable"), "cmd"),
            element(name("arguments"), buildArgumentElements()),
            element(name("environmentVariables"),
                element("ORACLE_HOME", oracleHome),
                element("WDT_CUSTOM_CONFIG", wdtCustomConfig)));
    }

    private Element[] buildArgumentElements() {
        return Stream
            .concat(
                Stream.of("/C", downloadDirectory + "\\weblogic-deploy\\bin\\" + getWeblogicDeployCommand() + ".cmd"),
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

        if (StringUtils.isNotBlank(rcuDb)) {
            arguments.add("-rcu_db");
            arguments.add(rcuDb);
        }

        if (StringUtils.isNotBlank(rcuPrefix)) {
            arguments.add("-rcu_prefix");
            arguments.add(rcuPrefix);
        }

        if (StringUtils.isNotBlank(rcuDbUser)) {
            arguments.add("-rcu_db_user");
            arguments.add(rcuDbUser);
        }

        if (StringUtils.isNotBlank(adminUrl)) {
            arguments.add("-admin_url");
            arguments.add(adminUrl);
        }

        if (StringUtils.isNotBlank(adminUser)) {
            arguments.add("-admin_user");
            arguments.add(adminUser);
        }

        return arguments;
    }

    protected void antRunWeblogicDeployScript() throws MojoExecutionException {
        executeMojo(
            plugin(
                groupId("org.apache.maven.plugins"),
                artifactId("maven-antrun-plugin"),
                version("3.0.0")),
            goal("run"),
            buildAntRunConfiguration(),
            executionEnvironment(
                mavenSession,
                pluginManager));
    }

    protected Xpp3Dom buildAntRunConfiguration() {
        return configuration(element("target",
            element("exec",
                attributes(
                    attribute("executable", "cmd"),
                    attribute("spawn", "false")),
                buildAntRunExecChildElements())));
    }

    private Element[] buildAntRunExecChildElements() {
        return Stream.concat(buildAntRunExecEnvElements().stream(),
            buildAntRunExecArgElements().stream())
            .toArray(Element[]::new);

//        return buildAntRunExecEnvElements().stream()
//            .toArray(Element[]::new);
    }

    private List<Element> buildAntRunExecArgElements() {
        return Stream
            .concat(
                Stream.of("/C", downloadDirectory + "\\weblogic-deploy\\bin\\" + getWeblogicDeployCommand() + ".cmd"),
                buildArguments().stream())
            .map(s -> element("arg", attribute("value", s)))
            .collect(Collectors.toList());
    }

    private List<Element> buildAntRunExecEnvElements() {
        List<Element> elements = new ArrayList<>();

        if (StringUtils.isNotBlank(oracleHome)) {
            elements.add(element("env", attributes(attribute("key", "ORACLE_HOME"), attribute("value", oracleHome))));
        }

        if (StringUtils.isNotBlank(wdtCustomConfig)) {
            elements.add(
                element("env", attributes(attribute("key", "WDT_CUSTOM_CONFIG"), attribute("value", wdtCustomConfig))));
        }

        return elements;
    }

}
