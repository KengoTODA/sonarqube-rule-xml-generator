# SonarQube rule.xml generator

This is a Maven plugin to generate `rule.xml` which is necessary to [create SonarQube plugin](https://docs.sonarqube.org/display/DEV/Build+Plugin).

## How to use

Create a multi module Maven project, which has a submodule for SpotBugs plugin and another submodule for SonarQube plugin.

In SonarQube plugin submodule, execute `generate` goal at `generate-resources` phase. For instance:

```xml
      <plugin>
        <groupId>jp.skypencil.sonarqube</groupId>
        <artifactId>rule-xml-maven-plugin</artifactId>
        <version>0.1.1</version>
        <configuration>
          <findbugs>../spotbugs-plugin/src/main/resources/findbugs.xml</findbugs>
          <messages>../spotbugs-plugin/src/main/resources/messages.xml</messages>
          <output>${project.build.outputDirectory}/rule.xml</output>
        </configuration>
        <executions>
          <execution>
            <phase>generate-resources</phase>
            <goals>
              <goal>generate</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
```

## Logic to decide severity

* If bug pattern's category is STYLE, MALICIOUS_CODE, I18N or EXPERIMENTAL, its severity is **INFO**
* Otherwise its severity is **MAJOR**

## Logic to decide tag

* If bug pattern's category is  PERFORMANCE, CORRECTNESS or MULTI-THREADING, it's tagged with **bug**
* You can specify comma-separated list of custom tag by `<tags>` parameter, e.g. `<tags>foo,bar</tags>`
