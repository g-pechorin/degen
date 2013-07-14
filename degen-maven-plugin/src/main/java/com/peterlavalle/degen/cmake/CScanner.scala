package com.peterlavalle.degen.cmake

import org.apache.maven.project.MavenProject
import com.google.inject.Inject

import scala.collection.JavaConversions._
import scala.Predef._
import java.io.{FileWriter, File}

class CScanner {

  @Inject
  val project: MavenProject = null

  val mode = CMakeMode.LIBRARY

  def apply(cmakeFile: File, absolutePathPattern: String = ".*\\.(c|cpp)$") = {

    cmakeFile.getParentFile.mkdirs()

    def flatten(file: File): Set[File] =
      if (file.isDirectory)
        file.listFiles().map((sub) => flatten(sub)).reduce(_ ++ _)
      else
        Set(file)


    val sources =
      project.getCompileSourceRoots.toList
        .map((str) => flatten(new File(str.toString)))
        .reduce(_ ++ _)
        .filter((file) => file.getAbsolutePath.matches(absolutePathPattern)).toArray


    val writer = new FileWriter(cmakeFile)

    new CMakeSourcesTemplate().render(writer, project, sources, mode)

    writer.close()
  }

}
