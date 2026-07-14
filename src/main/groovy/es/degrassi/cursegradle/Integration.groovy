package es.degrassi.cursegradle

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class Integration {

    private static final Logger log = Logging.getLogger(Integration)

    static void checkJava(Project project, CurseProject curseProject) {
        try {
            if (project.plugins.hasPlugin('java')) {
                log.info 'Java plugin detected, adding integration...'
                if (curseProject.mainArtifact == null) {
                    Task jarTask = project.tasks.getByName('jar')
                    log.info "Setting main artifact for CurseForge Project $curseProject.id to the java jar"
                    CurseArtifact artifact = new CurseArtifact()
                    artifact.artifact = jarTask
                    curseProject.mainArtifact = artifact
                    curseProject.uploadTask.dependsOn jarTask
                }
            }
        } catch (Throwable t) {
            log.warn("Failed Java integration", t)
        }
    }

    static void checkForgeGradle(Project project, CurseProject curseProject) {
        if (project.hasProperty('minecraft')) {
            log.info "ForgeGradle plugin detected, adding integration..."

            // FG 3+ doesn't set MC_VERSION until afterEvaluate
            project.gradle.taskGraph.whenReady {
                try {
                    if (project.minecraft.hasProperty('version')) {
                        log.info 'Found Minecraft version in FG < 3'
                        curseProject.addGameVersion(project.minecraft.version)
                        curseProject.addGameVersion('Forge')
                    } else if (project.getExtensions().getExtraProperties().has('MC_VERSION')) {
                        log.info 'Found Minecraft version in FG >= 3'
                        curseProject.addGameVersion(project.getExtensions().getExtraProperties().get('MC_VERSION'))
                        curseProject.addGameVersion('Forge')
                    } else {
                        log.warn 'Unable to extract Minecraft version from ForgeGradle'
                    }
                } catch (Throwable t) {
                    log.warn('Failed ForgeGradle integration', t)
                }
            }
        }
    }
}
