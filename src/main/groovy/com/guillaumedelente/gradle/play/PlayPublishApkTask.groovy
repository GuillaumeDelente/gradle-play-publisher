package com.guillaumedelente.gradle.play

import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.ApplicationVariant
import com.google.api.client.http.FileContent
import com.google.api.services.androidpublisher.model.Apk
import com.google.api.services.androidpublisher.model.ApkListing
import com.google.api.services.androidpublisher.model.Track
import org.gradle.api.tasks.TaskAction

class PlayPublishApkTask extends PlayPublishTask {

    static def MAX_CHARACTER_LENGTH_FOR_WHATS_NEW_TEXT = 500
    static def FILE_NAME_FOR_WHATS_NEW_TEXT = "whatsnew"

    ApplicationVariant variant

    File inputFolder

    @TaskAction
    publishApk() {
        super.publish()
        logger.warn("Getting extension file ${extension.pk12File} for variant ${variant.name}")
        def apkOutput = variant.outputs.find { variantOutput -> variantOutput instanceof ApkVariantOutput }
        FileContent newApkFile = new FileContent(AndroidPublisherHelper.MIME_TYPE_APK, apkOutput.outputFile)

        Apk apk = edits.apks()
                .upload(applicationId, editId, newApkFile)
                .execute()
        Track newTrack = new Track().setVersionCodes([apk.getVersionCode()])
        if (extension.userFraction != null) {
            newTrack.setUserFraction(extension.userFraction)
        }
        edits.tracks()
                .update(applicationId, editId, extension.track, newTrack)
                .execute()

        if (inputFolder.exists()) {

            // Matches if locale have the correct naming e.g. en-US for play store
            inputFolder.eachDirMatch(matcher) { dir ->
                File whatsNewFile = new File(dir, FILE_NAME_FOR_WHATS_NEW_TEXT + "-" + extension.track)

                if (!whatsNewFile.exists()) {
                    whatsNewFile = new File(dir, FILE_NAME_FOR_WHATS_NEW_TEXT)
                }

                if (whatsNewFile.exists()) {
                    def whatsNewText = TaskHelper.readAndTrimFile(whatsNewFile, MAX_CHARACTER_LENGTH_FOR_WHATS_NEW_TEXT)
                    def locale = dir.getName()

                    ApkListing newApkListing = new ApkListing().setRecentChanges(whatsNewText)
                    edits.apklistings()
                            .update(applicationId, editId, apk.getVersionCode(), locale, newApkListing)
                            .execute()
                }
            }

        }

        edits.commit(applicationId, editId).execute()
    }
}
