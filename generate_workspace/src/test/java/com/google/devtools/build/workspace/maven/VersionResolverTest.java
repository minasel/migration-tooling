package com.google.devtools.build.workspace.maven;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;

/**
 * Tests for {@link VersionResolver}.
 */
//TODO(petros): Test this actually selects highest version. VersionRangeResult is final class so Mockito cannot mock it.
@RunWith(JUnit4.class)
public class VersionResolverTest {

  /**
   * Ensures that an exception is thrown if there is a version range resolution exception.
   * This occurs when aether is unable to resolve the version.
   */
  @Test
  public void failsOnResolutionException() {
    RepositorySystem system = Mockito.mock(RepositorySystem.class);
    try {
      Mockito.when(system.resolveVersionRange(anySession(), anyRangeRequest()))
          .thenThrow(new VersionRangeResolutionException(any()));

      VersionResolver resolver = VersionResolver.builder().systemSession(anySession(), system).build();
      resolver.resolveVersion("something", "something", "1.0");
      fail();
    } catch (VersionRangeResolutionException e) {
      // This should be caught by the VersionResolver
      fail();
    } catch (ArtifactBuilder.InvalidArtifactCoordinateException e) {
      assertThat(e.getMessage()).isEqualTo("Unable to find a version for something:something:1.0");
    }
  }

  /**
   * Ensures that an exception is thrown if there is an invalid version range. An invalid version range
   * is one which is either (1) equal to null or (2) returns null when asked for highest version.
   */
  @Test
  public void failsOnInvalidVersionRange() {
    RepositorySystem system = Mockito.mock(RepositorySystem.class);

    try {

      // Using `anyRangeResult()` will ensure that rangeResult.highestVersion() == null.
      Mockito.when(system.resolveVersionRange(anySession(), anyRangeRequest())).thenReturn(anyRangeResult());

      VersionResolver resolver = VersionResolver.builder().systemSession(anySession(), system).build();
      resolver.resolveVersion("something", "something", "1.0");
      fail();
    } catch (VersionRangeResolutionException e) {
      // This should be caught by the VersionResolver
      fail();
    } catch (ArtifactBuilder.InvalidArtifactCoordinateException e) {
      assertThat(e.getMessage()).isEqualTo("Unable to find a version for something:something:1.0");
    }
  }

  /**
   * Asserts that given a soft pinned version specification, it selects that version,
   * and does not get the highest version. "3.4" is an example of a soft pinned version specification.
   */
  //TODO(petros): implicitly using Maven central for resolving version. This is sketchy.
  @Test
  public void softPinnedVersions() {
    try {
      String version =
          VersionResolver.defaultResolver().resolveVersion("something", "something", "3.4");
      assertThat(version).isEqualTo("3.4");

    } catch (ArtifactBuilder.InvalidArtifactCoordinateException e) {
      fail();
    }
  }

  private VersionRangeResult anyRangeResult() {
    return any(VersionRangeResult.class);
  }

  private RepositorySystemSession anySession() {
    return any(RepositorySystemSession.class);
  }

  private VersionRangeRequest anyRangeRequest() {
    return any(VersionRangeRequest.class);
  }
}