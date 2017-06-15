package jp.skypencil.sonarqube;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

/**
 * <p>Generate rule.xml for Sonar (SonarQube).</p>
 */
public class RuleXmlGenerator {
    void generate(File findbugsFile, File messageFile, File output, String[] tags) throws IOException {
        SAXReader reader = new SAXReader();
        try {
            Document message = reader.read(messageFile);
            Document findbugs = reader.read(findbugsFile);

            @SuppressWarnings("unchecked")
            List<Node> bugPatterns = message.selectNodes("/MessageCollection/BugPattern");
            @SuppressWarnings("unchecked")
            List<Node> findbugsAbstract = findbugs.selectNodes("/FindbugsPlugin/BugPattern");

            writePatterns(findbugsAbstract, bugPatterns, output, tags);
        } catch (DocumentException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void writePatterns(List<Node> findbugsAbstract, List<Node> bugPatterns, File output, String[] tags) throws IOException {
        BufferedWriter writer = Files.newWriter(output, Charsets.UTF_8);
        boolean threw = true;
        try {
            writer.write("<rules>\n");
            for (Node bugPattern : bugPatterns) {
                writeBugPattern(bugPattern, findbugsAbstract, writer, tags);
            }
            writer.write("</rules>\n");
            threw = false;
        } finally {
            Closeables.close(writer, threw);
        }
    }

    private void writeBugPattern(Node bugPattern, List<Node> findbugsAbstract, BufferedWriter writer, String[] tags) throws IOException {
        String type = bugPattern.valueOf("@type");
        String description = bugPattern.selectSingleNode("ShortDescription").getText();

        Node data = findByType(findbugsAbstract, type);
        String abbrev = data.valueOf("@abbrev");
        String category = data.valueOf("@category");
        String priority = decidePriority(category);

        String line = String.format("  <rule key=\"%s\">\n" +
                "    <priority>%s</priority>\n" +
                "    <name><![CDATA[%s - %s]]></name>\n" +
                "    <description><![CDATA[[%s] %s]]></description>\n" +
                "    <configKey><![CDATA[%s]]></configKey>\n" +
                "    <tag>%s</tag>\n",
                type, priority, category, description, abbrev, description, type, category.toLowerCase().replace("_","-"));
        writer.write(line);
        for (String tag : tags) {
            writer.write(String.format("    <tag>%s</tag>\n", tag));
        }
        if (category.equals("PERFORMANCE") || category.equals("CORRECTNESS") || category.equals("MULTI-THREADING")) {
            writer.write("    <tag>bug</tag>\n");
        }
        writer.write("  </rule>\n");
    }

    private String decidePriority(String category) {
        if (category == null) {
            return "MAJOR";
        }

        switch (category) {
        case "STYLE":
        case "MALICIOUS_CODE":
        case "I18N":
        case "EXPERIMENTAL":
            return "INFO";
        default:
            return "MAJOR";
        }
    }

    private Node findByType(List<Node> findbugsAbstract, String type) {
        for (Node node : findbugsAbstract) {
            if (Objects.equal(type, node.valueOf("@type"))) {
                return node;
            }
        }
        throw new IllegalArgumentException("cannot find " + type);
    }

}
