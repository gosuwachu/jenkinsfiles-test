// ============================================
// Mobile CI/CD Pipeline - Job DSL Script
// ============================================

def pipelineFolder = 'mobile-pipeline'

// Create folder for pipeline jobs
folder(pipelineFolder) {
    description('Mobile CI/CD Pipeline')
}

// --------------------------------------------
// TRIGGER JOB - Entry point for the pipeline
// --------------------------------------------
job("${pipelineFolder}/trigger") {
    displayName('Pipeline Trigger')
    description('Triggers the entire mobile CI/CD pipeline')

    deliveryPipelineConfiguration('Trigger', 'Start Pipeline')

    steps {
        shell('echo "hello world"')
    }

    publishers {
        downstreamParameterized {
            trigger([
                "${pipelineFolder}/ios-build",
                "${pipelineFolder}/android-build",
                "${pipelineFolder}/ios-unit-tests",
                "${pipelineFolder}/android-unit-tests",
                "${pipelineFolder}/ios-linter",
                "${pipelineFolder}/android-linter"
            ].join(', ')) {
                condition('SUCCESS')
                triggerWithNoParameters()
            }
        }
    }
}

// --------------------------------------------
// iOS BUILD AND DEPLOY
// --------------------------------------------
job("${pipelineFolder}/ios-build") {
    displayName('iOS Build')
    description('Build iOS application')

    deliveryPipelineConfiguration('Build', 'iOS Build')

    steps {
        shell('echo "hello world"')
    }

    publishers {
        downstreamParameterized {
            trigger("${pipelineFolder}/ios-deploy") {
                condition('SUCCESS')
                triggerWithNoParameters()
            }
        }
    }
}

job("${pipelineFolder}/ios-deploy") {
    displayName('iOS Deploy')
    description('Deploy iOS application')

    deliveryPipelineConfiguration('Deploy', 'iOS Deploy')

    steps {
        shell('echo "hello world"')
    }
}

// --------------------------------------------
// ANDROID BUILD AND DEPLOY
// --------------------------------------------
job("${pipelineFolder}/android-build") {
    displayName('Android Build')
    description('Build Android application')

    deliveryPipelineConfiguration('Build', 'Android Build')

    steps {
        shell('echo "hello world"')
    }

    publishers {
        downstreamParameterized {
            trigger("${pipelineFolder}/android-deploy") {
                condition('SUCCESS')
                triggerWithNoParameters()
            }
        }
    }
}

job("${pipelineFolder}/android-deploy") {
    displayName('Android Deploy')
    description('Deploy Android application')

    deliveryPipelineConfiguration('Deploy', 'Android Deploy')

    steps {
        shell('echo "hello world"')
    }
}

// --------------------------------------------
// iOS UNIT TESTS
// --------------------------------------------
job("${pipelineFolder}/ios-unit-tests") {
    displayName('iOS Unit Tests')
    description('Run iOS unit tests')

    deliveryPipelineConfiguration('Test', 'iOS Unit Tests')

    steps {
        shell('echo "hello world"')
    }
}

// --------------------------------------------
// ANDROID UNIT TESTS
// --------------------------------------------
job("${pipelineFolder}/android-unit-tests") {
    displayName('Android Unit Tests')
    description('Run Android unit tests')

    deliveryPipelineConfiguration('Test', 'Android Unit Tests')

    steps {
        shell('echo "hello world"')
    }
}

// --------------------------------------------
// iOS LINTER
// --------------------------------------------
job("${pipelineFolder}/ios-linter") {
    displayName('iOS Linter')
    description('Run iOS linter')

    deliveryPipelineConfiguration('Quality', 'iOS Linter')

    steps {
        shell('echo "hello world"')
    }
}

// --------------------------------------------
// ANDROID LINTER
// --------------------------------------------
job("${pipelineFolder}/android-linter") {
    displayName('Android Linter')
    description('Run Android linter')

    deliveryPipelineConfiguration('Quality', 'Android Linter')

    steps {
        shell('echo "hello world"')
    }
}

// --------------------------------------------
// DELIVERY PIPELINE VIEW
// --------------------------------------------
deliveryPipelineView('Mobile Pipeline View') {
    description('Visualization of the Mobile CI/CD Pipeline')

    pipelineInstances(5)
    showAggregatedPipeline(false)
    columns(1)
    updateInterval(2)

    enableManualTriggers(true)
    allowPipelineStart(true)
    allowRebuild(true)

    showAvatars(true)
    showChangeLog(true)
    showTotalBuildTime(true)
    showDescription(true)

    pipelines {
        component('Mobile CI/CD', "${pipelineFolder}/trigger")
    }
}

// --------------------------------------------
// SEED JOB (for re-running this script)
// --------------------------------------------
job('seed-job') {
    displayName('Seed Job')
    description('Regenerates all pipeline jobs from DSL scripts')

    steps {
        shell('cp /var/jenkins_home/jobs-dsl/*.groovy .')
        jobDsl {
            targets('*.groovy')
            removedJobAction('DELETE')
            removedViewAction('DELETE')
            lookupStrategy('SEED_JOB')
            failOnMissingPlugin(true)
        }
    }
}
