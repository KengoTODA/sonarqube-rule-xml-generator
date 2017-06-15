package jp.skypencil.sonarqube;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "generate", threadSafe = true, defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class GenerateMojo extends AbstractMojo {
    @Parameter(required = true, defaultValue = "${project.basedir}/src/main/resources/findbugs.xml")
    private File findbugs;

    @Parameter(required = true, defaultValue = "${project.basedir}/src/main/resources/messages.xml")
    private File messages;

    @Parameter(required = true, defaultValue = "${project.build.outputDirectory}/rule.xml")
    private File output;

    @Parameter(required = true, defaultValue = "false")
    private boolean skip;

    @Parameter(defaultValue = "")
    private String tags;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Skip generating rule XML for SonarQube");
            return;
        }

        if (!findbugs.isFile()) {
            throw new MojoExecutionException("findbug.xml not found at " + findbugs.getAbsolutePath());
        }
        if (!messages.isFile()) {
            throw new MojoExecutionException("messages.xml not found at " + messages.getAbsolutePath());
        }
        if (!output.getParentFile().mkdirs()) {
            throw new MojoExecutionException("failed to make directory at " + output.getParent());
        }

        try {
            getLog().info("Generating rule XML for SonarQube at " + output.getAbsolutePath());
            new RuleXmlGenerator().generate(findbugs, messages, output, tags.split(","));
        } catch (IOException e) {
            throw new MojoExecutionException("I/O problem occurred", e);
        }
    }
}
