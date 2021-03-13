package com.github.ncsaba.ideajavmpirecad.services

import com.github.ncsaba.ideajavmpirecad.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
