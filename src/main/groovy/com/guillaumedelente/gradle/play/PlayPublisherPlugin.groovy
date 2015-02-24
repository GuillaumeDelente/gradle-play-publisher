package com.guillaumedelente.gradle.play
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class PlayPublisherPlugin implements Plugin<Project> {

    public static final String PLAY_STORE_GROUP = "Play Store"

    @Override
    void apply(Project project) {
        def log = project.logger

        def hasAppPlugin = project.plugins.find { p -> p instanceof AppPlugin }
        if (!hasAppPlugin) {
            throw new IllegalStateException("The 'com.android.application' plugin is required.")
        }

        //def extension = project.extensions.create('publishingConfigs', PlayPublisherPluginExtension)

        def publishingConfigs = project.container(PublishingConfig)
        project.extensions.publishingConfigs = publishingConfigs
        log.warn 'PublishingConfigs parsing success'
        project.android.applicationVariants.all { variant ->
            if (variant.buildType.isDebuggable()) {
                log.debug("Skipping debuggable build type ${variant.buildType.name}.")
                return
            }
            project.publishingConfigs.all { publishingConfig ->
                checkTrack(project, publishingConfig)
            }
            def buildTypeName = variant.buildType.name.capitalize()

            def productFlavorNames = variant.productFlavors.collect { it.name.capitalize() }
            if (productFlavorNames.isEmpty()) {
                productFlavorNames = [""]
            }
            def productFlavorName = productFlavorNames.join('')
            def flavor = productFlavorName.toLowerCase()

            def variationName = "${productFlavorName}${buildTypeName}"
            def variantApplicationId = variant.applicationId

            def bootstrapTaskName = "bootstrap${variationName}PlayResources"
            def playResourcesTaskName = "generate${variationName}PlayResources"
            def publishApkTaskName = "publishApk${variationName}"
            def publishListingTaskName = "publishListing${variationName}"
            def publishTaskName = "publish${variationName}"

            def outputData = variant.outputs[0]
            def zipAlignTask = outputData.zipAlign
            def assembleTask = outputData.assemble

            def variantData = variant.variantData
            if (!zipAlignTask || !variantData.zipAlignEnabled) {
                log.warn("Could not find ZipAlign task. Did you specify a signingConfig for the variation ${variationName}?")
                return
            }
            def publishingConfigName = variationName[0].toLowerCase() + variationName.substring(1)
            project.publishingConfigs.maybeCreate(publishingConfigName)
            PublishingConfig publishingConfig = publishingConfigs.findByName(publishingConfigName)

            // Create and configure bootstrap task for this variant.
            def bootstrapTask = project.tasks.create(bootstrapTaskName, BootstrapTask)
            bootstrapTask.extension = publishingConfig
            bootstrapTask.applicationId = variantApplicationId
            if (flavor) {
                bootstrapTask.outputFolder = new File(project.getProjectDir(), "src/${flavor}/play")
            } else {
                bootstrapTask.outputFolder = new File(project.getProjectDir(), "src/${variant.buildType.name}/play")
            }
            bootstrapTask.description = "Downloads the play store listing for the ${variationName} build. No download of image resources. See #18."
            bootstrapTask.group = PLAY_STORE_GROUP

            // Create and configure task to collect the play store resources.
            def playResourcesTask = project.tasks.create(playResourcesTaskName, GeneratePlayResourcesTask)

            playResourcesTask.inputs.file(new File(project.getProjectDir(), "src/main/play"))
            if (flavor) {
                playResourcesTask.inputs.file(new File(project.getProjectDir(), "src/${flavor}/play"))
            }
            playResourcesTask.inputs.file(new File(project.getProjectDir(), "src/${variant.buildType.name}/play"))
            if (flavor) {
                playResourcesTask.inputs.file(new File(project.getProjectDir(), "src/${variant.name}/play"))
            }

            playResourcesTask.outputFolder = new File(project.getProjectDir(), "build/outputs/play/${variant.name}")
            playResourcesTask.description = "Collects play store resources for the ${variationName} build"
            playResourcesTask.group = PLAY_STORE_GROUP

            // Create and configure publisher apk task for this variant.
            def publishApkTask = project.tasks.create(publishApkTaskName, PlayPublishApkTask)
            publishApkTask.extension = publishingConfig
            publishApkTask.variant = variant
            publishApkTask.applicationId = variantApplicationId
            publishApkTask.inputFolder = playResourcesTask.outputFolder
            publishApkTask.description = "Uploads the APK for the ${variationName}"
            publishApkTask.group = PLAY_STORE_GROUP

            // Create and configure publisher meta task for this variant
            def publishListingTask = project.tasks.create(publishListingTaskName, PlayPublishListingTask)
            publishListingTask.extension = publishingConfig
            publishListingTask.applicationId = variantApplicationId
            publishListingTask.inputFolder = playResourcesTask.outputFolder
            publishListingTask.description = "Updates the play store listing for the ${variationName} build"
            publishListingTask.group = PLAY_STORE_GROUP

            def publishTask = project.tasks.create(publishTaskName)
            publishTask.description = "Updates APK and play store listing for the ${variationName} build"
            publishTask.group = PLAY_STORE_GROUP

            // Attach tasks to task graph.
            publishTask.dependsOn publishApkTask
            publishTask.dependsOn publishListingTask
            publishListingTask.dependsOn playResourcesTask
            publishApkTask.dependsOn playResourcesTask
            publishApkTask.dependsOn assembleTask
        }
    }

    def checkTrack(Project project, PublishingConfig extension) {
        if (extension.track == "rollout" && extension.userFraction == null) {
            throw new IllegalStateException("When publishing on rollout track, you must " +
                    "declare a userFraction in your build.gradle")
        } else if (extension.track != "rollout" && extension.userFraction != null) {
            throw new IllegalStateException("You cannot declare a userFraction without" +
                    "having a rollout track")
        }
    }

}
