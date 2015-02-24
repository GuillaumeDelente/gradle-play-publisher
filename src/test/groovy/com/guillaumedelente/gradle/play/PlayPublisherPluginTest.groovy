package com.guillaumedelente.gradle.play

import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.internal.plugins.PluginApplicationException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

class PlayPublisherPluginTest {

    @Test(expected = PluginApplicationException.class)
    public void testThrowsOnLibraryProjects() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'android-library'
        project.apply plugin: 'play'
    }

    @Test
    public void testCreatesDefaultTask() {
        Project project = TestHelper.evaluatableProject()
        project.evaluate()

        assertNotNull(project.tasks.publishRelease)
        assertEquals(project.tasks.publishApkRelease.variant, project.android.applicationVariants[1])
    }

    @Test
    public void testCreatesFlavorTasks() {
        Project project = TestHelper.evaluatableProject()

        project.android.productFlavors {
            free
            paid
        }

        project.evaluate()

        assertNotNull(project.tasks.publishPaidRelease)
        assertNotNull(project.tasks.publishFreeRelease)

        assertEquals(project.tasks.publishApkFreeRelease.variant, project.android.applicationVariants[3])
        assertEquals(project.tasks.publishApkPaidRelease.variant, project.android.applicationVariants[1])
    }

    @Test
    public void testDefaultTrack() {
        Project project = TestHelper.evaluatableProject()
        project.evaluate()

        assertEquals('alpha', project.publishingConfigs.release.track)
    }

    @Test
    public void testProductionTrack() {
        Project project = TestHelper.evaluatableProject()

        project.publishingConfigs.create('release')
        project.publishingConfigs.release.track = 'production'

        project.evaluate()

        assertEquals('production', project.publishingConfigs.release.track)
    }

    @Test
    public void testRolloutTrack() {
        Project project = TestHelper.evaluatableProject()

        project.publishingConfigs.create('release')
        project.publishingConfigs.release.track = 'rollout'
        project.publishingConfigs.release.userFraction = 0.5

        project.evaluate()

        assertEquals('rollout', project.publishingConfigs.release.track)
        assertEquals(0.5D, (Double) project.publishingConfigs.release.userFraction, 0.01)
    }

    @Test(expected = ProjectConfigurationException.class)
    public void testRolloutMustHaveUserFraction() {
        Project project = TestHelper.evaluatableProject()

        project.publishingConfigs.create('release')
        project.publishingConfigs.release.track = 'rollout'

        project.evaluate()
    }

    @Test(expected = ProjectConfigurationException.class)
    public void testUserFractionIsOnlyWithRollout() {  	
        Project project = TestHelper.evaluatableProject()

        project.publishingConfigs.create('release')
        project.publishingConfigs.release.track = 'alpha'
        project.publishingConfigs.release.userFraction = 0.5

        project.evaluate()
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsOnInvalidUserFraction() {
        Project project = TestHelper.evaluatableProject()

        project.publishingConfigs.create('release')
        project.publishingConfigs.release.track = 'rollout'
        project.publishingConfigs.release.userFraction = 0
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsOnInvalidTrack() {
        Project project = TestHelper.evaluatableProject()

        project.publishingConfigs.create('release')
        project.publishingConfigs.release.track = 'gamma'
    }

    @Test
    public void testPublishListingTask() {
        Project project = TestHelper.evaluatableProject()

        project.android.productFlavors {
            free
            paid
        }

        project.evaluate()

        assertNotNull(project.tasks.publishListingFreeRelease)
        assertNotNull(project.tasks.publishListingPaidRelease)
    }

}
