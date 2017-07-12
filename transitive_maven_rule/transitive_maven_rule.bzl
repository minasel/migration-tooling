DEPS = ["mvn"]
MAVEN_CENTRAL = "https://repo1.maven.org/maven2/"
MAX_TIMEOUT = 0x7FFFFFFF

#TODO(petros): Obtain the binary in some other fashion
BINARY_URL = "https://github.com/petroseskinder/migration-tooling/releases/download/deploy/generate_workspace_deploy_latest.jar"

def _check_dependencies(repository_ctx):
  for dep in DEPS:
		if repository_ctx.which(dep) == None:
			fail("%s requires %s as a dependency. Please check your PATH" % (repository_ctx.name, dep))

def _validate_coordinate(coordinate):
  parts = coordinate.split(":")
  return len(parts) >= 2 and len(parts) <= 5

def _validate_coordinates(repository_ctx, coordinates):
  for coordinate in coordinates:
    if _validate_coordinate(coordinate) == False:
      fail("Invalid coordinate %s. Should be formatted as \"group:artifact:version\"" % coordinate)
  return True

def _create_coordinates(repository_ctx):
	coordinates = []
	for group in repository_ctx.attr.artifacts:
		for artifact in repository_ctx.attr.artifacts[group]:
			coordinates.append('%s:%s' % (group, artifact))
	return coordinates

def _execute(repository_ctx, command_string):
	return repository_ctx.execute(["bash", "-c", command_string], timeout=repository_ctx.attr.timeout)

def _transitive_maven_jar_impl(repository_ctx):
	_check_dependencies(repository_ctx)
	coordinates = _create_coordinates(repository_ctx)
	_validate_coordinates(repository_ctx, coordinates)
	arguments = ' '.join(['-a ' + coordinate for coordinate in coordinates])

	# obtain the generate workspace binary
	jar_path = repository_ctx.path('generate_workspace_deploy.jar')
	repository_ctx.download(BINARY_URL, output=jar_path)

	# execute the command
	result = _execute(repository_ctx, "java -jar %s %s" % (jar_path, arguments))
	print(result.stdout)

	repository_ctx.file('BUILD', '', False)


transitive_maven_jar = repository_rule(
	implementation = _transitive_maven_jar_impl,
	attrs = {
		"artifacts" : attr.string_list_dict(default = {}, mandatory=True),
		# TODO(petros): add support for private repositories in generate_workspace
		"repository" : attr.string(default=MAVEN_CENTRAL),
		"timeout" : attr.int(default=MAX_TIMEOUT)
	},
	local = False,
)
