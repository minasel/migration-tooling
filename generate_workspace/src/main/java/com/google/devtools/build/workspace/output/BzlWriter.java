// Copyright 2017 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtools.build.workspace.output;

import com.google.devtools.build.workspace.maven.Rule;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Writes WORKSPACE and BUILD file definitions to a .bzl file.
 */
public class BzlWriter extends AbstractWriter {

  private final static Logger logger = Logger.getLogger(
      MethodHandles.lookup().lookupClass().getName());

  private final String[] argv;
  private final Path generatedFile;

  public BzlWriter(String[] argv, String outputDirStr) {
    this.argv = argv;
    this.generatedFile = Paths.get(outputDirStr).resolve("generate_workspace.bzl");
  }

  @Override
  public void write(Collection<Rule> rules) {
    try (PrintStream outputStream = new PrintStream(generatedFile.toFile())) {
      writeBzl(outputStream, rules);
    } catch (IOException e) {
      logger.severe("Could not write " + generatedFile + ": " + e.getMessage());
      return;
    }
    System.out.println("Wrote " + generatedFile.toAbsolutePath());
  }

  private void writeBzl(PrintStream outputStream, Collection<Rule> rules) {
    writeHeader(outputStream, argv);
    outputStream.println("def generated_maven_jars():");
    if (rules.isEmpty()) {
      outputStream.println("  pass\n");
    }
    for (Rule rule : rules) {
      outputStream.println(formatMavenJar(rule, "native.maven_jar", "  "));
    }

    outputStream.append("\n\n");

    outputStream.println("def generated_java_libraries():");
    if (rules.isEmpty()) {
      outputStream.println("  pass\n");
    }
    for (Rule rule : rules) {
      outputStream.println(formatJavaLibrary(rule, "native.java_library", "  "));
    }
  }
}
