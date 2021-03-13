package com.javampire.cad.services

import com.intellij.openapi.project.Project
import com.javampire.cad.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
