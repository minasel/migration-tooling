package com.google.devtools.build.workspace.maven;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.google.devtools.build.workspace.maven.AetherUtils.MAVEN_CENTRAL_URL;
import static java.util.stream.Collectors.toList;

/**
 * A struct representing the fields of maven_jar to be written to the WORKSPACE file.
 */
//TODO(petros): refactor existing resolvers to use this class rather than Rule.
//TODO(petros): Implement the SHA1 and aliasing.
public class MavenJarRule implements Comparable<MavenJarRule> {

  private final DependencyNode node;
  private final Set<String> parents;
  private final Set<String> dependencies;

  public MavenJarRule(DependencyNode node) {
    this.node = node;
    this.parents = Sets.newHashSet();
    this.dependencies = Sets.newHashSet();
  }

  public void addParent(MavenJarRule parent) {
    addParent(parent.toMavenArtifactString());
  }

  public void addParent(String parent) {
    parents.add(parent);
  }

  public Set<String> getParents() {
    return parents;
  }

  public void addDependency(MavenJarRule dependency) {
    addDependency(dependency.name());
  }

  public void addDependency(String dependency) {
    dependencies.add(dependency);
  }

  public Set<String> getDependencies() {
    return dependencies;
  }

  private String artifactId() {
    return node.getArtifact().getArtifactId();
  }

  private String groupId() {
    return node.getArtifact().getGroupId();
  }

  public String version() {
    return node.getArtifact().getVersion();
  }

  /**
   * A unique name for this artifact to use in maven_jar's name attribute.
   */
  public String name() {
    return name(groupId(), artifactId());
  }

  /**
   * A unique name for this artifact to use in maven_jar's name attribute.
   */
  public static String name(String groupId, String artifactId) {
    return groupId.replaceAll("[.-]", "_") + "_" + artifactId.replaceAll("[.-]", "_");
  }

  public Artifact getArtifact() {
    return node.getArtifact();
  }

  public String toMavenArtifactString() {
    return groupId() + ":" + artifactId() + ":" + version();
  }

  /** Checks if the dependency node possesses a remote repository other than maven central */
  public boolean hasCustomRepository() {
    //TODO(petros): add a test for this.
    List<RemoteRepository> repositories = node.getRepositories();
    if (repositories == null || repositories.isEmpty() || repositories.size() > 1) {
      return false;
    }
    return !repositories.get(0).getUrl().equals(MAVEN_CENTRAL_URL);
  }

  public String getRepository() {
    return Joiner.on(',').join(
        node.getRepositories().stream().map(RemoteRepository::getUrl).collect(toList()));
  }

  @Override
  public String toString() {
    return node.getArtifact().toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    MavenJarRule rule = (MavenJarRule) o;

    return Objects.equals(groupId(), rule.groupId())
        && Objects.equals(artifactId(), rule.artifactId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(groupId(), artifactId());
  }

  @Override
  public int compareTo(MavenJarRule o) {
    return name().compareTo(o.name());
  }

  public boolean aliased() {
    //TODO(petros) implement this
    return false;
  }

  public String getSha1() {
    //TODO(petros) implement this
    return null;
  }
}
