# scala-gradle-project-template

Basic scala gradle project template. Includes

* com.github.maiflai.scalatest - produces a nice output that includes stack traces, and in addition can run tests in parallel, without requiring the addition of `@RunWith(classOf[JUnitRunner])` (which creates a duplicate Run option in Intellij). Also means you can run individual tests in the suite, rather then the whole suite, from Intellij.