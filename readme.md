# mvn-git-hash-property

**mvn-git-hash-property** is a custom Maven Mojo to extract the current GIT hash for the project from file system without using the `git` command.

The GIT hash is stored in a Maven build project property and can be used throughout the Maven build. If you don't specify a property name, the default name `git_hash` will be used.
Optionally, the hash value can be shortened to seven character, which is what most GIT tools and websites use for display.

If no GIT hash was found, e.g. when the project does not contain a `.git` folder, or the information about the GIT hash could not be retrieved for other reasons, a message is written to the build log and the property will not be assigned.

Please note that if no GIT hash was found and the property was not set, this can result in variables not being replaced, leaving a `${git_hash}` text in filtered resources. To prevent this, you can set the `fallbackValue` property and that value will be used then.

## pre-requisits
As **mvn-git-hash-property** is currently not available on Maven Central, you need to make sure that it is available to whatever system you're going to use it in your build.

### local builds
You need to clone the repository of **mvn-git-hash-property** and then install it locally by `mvn clean install`.

### server-side builds (with custom Maven hosting)
First, you need to clone and locally build **mvn-git-hash-property**, then you need to deploy it to your custom Maven hosting (e.g. Nexus).

While you might have configured your custom Maven hosting to be accessible via your Maven configuration, this might not include a custom Maven hosting location for plugins. So, make sure that `pluginRepositories` in your Maven configuration file includes your custom Maven hosting.


## usage

To execute the Mojo in your build process, it needs to be configured in the build/plugins section, like this:

```xml
<plugin>
    <groupId>io.github.nilscoding.maven</groupId>
    <artifactId>mvn-git-hash-property</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <phase>initialize</phase>
            <goals>
                <goal>git-hash-property</goal>
            </goals>
            <configuration>
                <propertyName>git_hash</propertyName>
                <shortHash>false</shortHash>
                <propertyPrefix></propertyPrefix>
                <propertySuffix></propertySuffix>
                <fallbackValue>unknown</fallbackValue>
                <branchPropertyName>gitBranch</branchPropertyName>
            </configuration>
        </execution>
    </executions>
</plugin>
```

The default phase is `initialize`.


### command line usage (e.g. for testing)

The Mojo can also be invoked via command line (needs to be installed in Maven first, see above):
```bash
mvn io.github.nilscoding.maven:mvn-git-hash-property:1.0.0:git-hash-property -DpropertyName=git_hash -DshortHash=false
```

The call must be done in a command shell and the current directory must contain a Maven project. All configuration options are also optional when using the command line.

Actually, this call does not do anything useful, it reads the GIT hash and displays it in the logging output.
The property will be set for the current Maven invocation, but this rather useless because there is no build running and therefore the property will not be used anywhere.
So, this call is only useful for testing purposes.



## configuration options

All configuration properties are optional and are only presented in the example for demonstration purposes.

### propertyName

The `propertyName` option specifies the name of the project property that the GIT hash will be stored in.

If no value is given, the default name `git_hash` will be used.

### propertyPrefix

The `propertyPrefix` option lets you add another string to the beginning of the resulting property value. E.g. this can be used to add a leading dot if you want to use the property as part of another property.

If no prefix is given (or if it's an empty string), nothing will be added to the resulting property.

### propertySuffix

The `propertySuffix` option lets you append another string at the end of the resulting property value.

If no suffix is given (or if it's an empty string), nothing will be appended to the resulting property.

### shortHash

If property `shortHash` evaluates to `true`, then the GIT hash will be shortened to seven characters, which is the common display length for GIT logs and websites.

### fallbackValue

When the target project does not contain any GIT information or the GIT hash cannot be determined for other reasons, you can optionally specify the `fallbackValue` property and then this value will be used for the property value.
This can be useful when the GIT hash is used in filtered resources because the non-existing variable would then not be replaced and a `${git_hash}` text can be present in the resource content.

### branchPropertyName

Since version 1.1.0 you can optionally specify `branchPropertyName` that will be filled with the branch name.
If no branch name was detected, the fallback value from `fallbackBranchValue` will be used if not empty itself.

### fallbackBranchValue

Since version 1.1.0 you can optionally define a fallback value using `fallbackBranchValue` for when no branch name was detected.


### copyright / license

**mvn-git-hash-property** is licensed under the MIT License, for more details see license.md
