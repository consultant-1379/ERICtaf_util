# Jenkins Job DSL Gradle Example 

An example [Job DSL](https://github.com/jenkinsci/job-dsl-plugin) project that uses Gradle for building and testing.
Check out [this presentation](https://www.youtube.com/watch?v=SSK_JaBacE0) for a walkthrough of this example (starts around 14:00).

For development please use [Job DSL Playground](http://job-dsl.herokuapp.com/) or your local Jenkins.


# Script Examples

* [Example 1](jobs/example1Jobs.groovy) - shows basic folder/job creation
* [Example 2](jobs/example2Jobs.groovy) - shows how to create a set of jobs for each github branch, each in its own folder
* [Example 3](jobs/example3Jobs.groovy) - shows how to use the configure block
* [Example 4](jobs/example4Jobs.groovy) - shows a way to reuse job definitions for jobs that differ only with a few properties
* [Example 5](jobs/example5Jobs.groovy) - shows how to pull out common components into static methods
* [Example 6](jobs/example6Jobs.groovy) - shows how to include script resources from the workspace
* [Example 7](jobs/example7Jobs.groovy) - shows how to create jobs using builders
