package io.github.nilscoding.maven.githashprop;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Maven Mojo to load a Maven properties from current GIT commit hash.
 * @author NilsCoding
 */
@Mojo(name = "git-hash-property", defaultPhase = LifecyclePhase.INITIALIZE)
public class GitHashPropertyMojo extends AbstractMojo {

    /**
     * Length of short GIT hash.
     */
    public static final int SHORT_GIT_HASH_LEN = 7;

    /**
     * Length of "ref: " prefix.
     */
    protected static final int REF_PREFIX_LEN = 5;

    /**
     * Length of "refs/heads/" prefix.
     */
    protected static final int REFS_HEADS_PREFIX_LEN = 11;

    /**
     * Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * Property name.
     */
    @Parameter(property = "propertyName")
    private String propertyName;

    /**
     * Property prefix string.
     */
    @Parameter(property = "propertyPrefix", defaultValue = "")
    private String propertyPrefix;

    /**
     * Property suffix string.
     */
    @Parameter(property = "propertySuffix", defaultValue = "")
    private String propertySuffix;

    /**
     * Flag for short hash.
     */
    @Parameter(property = "shortHash", defaultValue = "{false}")
    private boolean shortHash;

    /**
     * Fallback value.
     */
    @Parameter(property = "fallbackValue")
    private String fallbackValue;

    /**
     * Nome of branch property.
     */
    @Parameter(property = "branchPropertyName")
    private String branchPropertyName;

    /**
     * Fallback value for branch value.
     */
    @Parameter(property = "fallbackBranchValue")
    private String fallbackBranchValue;

    /**
     * Executes the Maven Mojo.
     * @throws MojoExecutionException Mojo execution exception
     * @throws MojoFailureException   Mojo failure exception
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Log log = getLog();
        log.info("GitHashProperty Mojo at work...");

        boolean useFallbackValue = (this.fallbackValue != null);
        String gitHash = null;
        String branchName = null;

        // read head reference
        String headInfo = readFirstLineFromFile(".git" + File.separator + "HEAD");
        if (headInfo == null) {
            if (useFallbackValue == false) {
                log.info("HEAD info not found, so not setting property");
                return;
            } else {
                log.info("HEAD info not found, will use fallback value");
            }
        } else {
            if (headInfo.startsWith("ref: ") == false) {
                // not a ref entry in the file
                if (useFallbackValue == false) {
                    log.info("ref entry not found in HEAD file, so not setting property");
                    return;
                } else {
                    log.info("ref entry not found in HEAD file, will use fallback value");
                }
            } else {
                String refInfo = headInfo.substring(REF_PREFIX_LEN).trim();
                if (refInfo.isEmpty()) {
                    if (useFallbackValue == false) {
                        log.info("ref entry is empty in HEAD file, so not setting property");
                        return;
                    } else {
                        log.info("ref entry is empty in HEAD file, will use fallback value");
                    }
                } else {
                    if (refInfo.startsWith("refs/heads/")) {
                        branchName = refInfo.substring(REFS_HEADS_PREFIX_LEN);
                    }
                    String gitHashFile = ".git" + File.separator + refInfo;
                    gitHash = readFirstLineFromFile(gitHashFile);
                    if (gitHash == null) {
                        if (useFallbackValue == false) {
                            log.info("GIT hash not found in file '" + gitHashFile + "'");
                            return;
                        } else {
                            log.info("GIT hash not found in file '" + gitHashFile + "', so use fallback value");
                        }
                    }
                }
            }
        }
        if (gitHash == null) {
            gitHash = this.fallbackValue;
        }

        // short hash will only be used for real hash, not for fallback value
        if ((this.shortHash == true) && (gitHash.length() >= SHORT_GIT_HASH_LEN) && (useFallbackValue == false)) {
            gitHash = gitHash.substring(0, SHORT_GIT_HASH_LEN);
        }

        String propName = null;
        if ((this.propertyName == null) || (this.propertyName.trim().isEmpty())) {
            propName = "git_hash";
        } else {
            propName = this.propertyName.trim();
        }
        String propertyValue = gitHash;
        if ((this.propertyPrefix != null) && (this.propertyPrefix.isEmpty() == false)) {
            propertyValue = this.propertyPrefix + propertyValue;
        }
        if ((this.propertySuffix != null) && (this.propertySuffix.isEmpty() == false)) {
            propertyValue = propertyValue + this.propertySuffix;
        }
        this.project.getProperties().put(propName, propertyValue);
        log.info("GIT hash '" + gitHash + "' assigned to property '" + propName + "'");
        if ((this.branchPropertyName != null) && (this.branchPropertyName.isEmpty() == false)) {

            if ((branchName != null) && (branchName.isEmpty() == false)) {
                this.project.getProperties().put(this.branchPropertyName, branchName);
                log.info("GIT branch name '" + branchName + "' assigned "
                        + "to property '" + this.branchPropertyName + "'");
            } else if ((this.fallbackBranchValue != null) && (this.fallbackBranchValue.isEmpty() == false)) {
                this.project.getProperties().put(this.branchPropertyName, this.fallbackBranchValue);
                log.info("GIT branch fallback '" + branchName + "' assigned "
                        + "to property '" + this.branchPropertyName + "'");

            }
        }
    }

    /**
     * Reads the first content line to a string. Empty lines are ignored.
     * If a line starts with # then it also will be ignored.
     * @param filename filename
     * @return first content line of file or null if nothing useful was read
     */
    protected String readFirstLineFromFile(String filename) {
        if ((filename == null) || (filename.isEmpty())) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String contentStr = null;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                if (line.trim().startsWith("#")) {
                    continue;
                }
                contentStr = line;
                break;
            }
            if ((contentStr != null) && (contentStr.trim().isEmpty() == false)) {
                return contentStr;
            }
        } catch (Exception ex) {
            return null;
        }
        return null;
    }

}

