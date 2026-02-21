// ============================================
// Mobile CI/CD Pipeline - Job DSL Script
// All Architecture Options for Comparison
// ============================================

def repoUrl = 'file:///var/jenkins_home/repo'
def branch = '*/master'

// ============================================
// OPTION 0: Current - Job DSL Free-Style Jobs
// Folder: mobile-pipeline
// ============================================

def folder0 = 'mobile-pipeline'

folder(folder0) {
    description('Option 0: Current - Job DSL free-style jobs with triggers')
    authorization {
        userPermissions('dev1', [
            'hudson.model.Item.Discover',
            'hudson.model.Item.Read',
            'hudson.model.Item.Build',
            'hudson.model.Item.Workspace'
        ])
    }
}

job("${folder0}/trigger") {
    displayName('Pipeline Trigger')
    description('Triggers the entire mobile CI/CD pipeline')
    deliveryPipelineConfiguration('Trigger', 'Start Pipeline')
    steps {
        shell('echo "hello world"')
    }
    publishers {
        downstreamParameterized {
            trigger([
                "${folder0}/ios-build",
                "${folder0}/android-build",
                "${folder0}/ios-unit-tests",
                "${folder0}/android-unit-tests",
                "${folder0}/ios-linter",
                "${folder0}/android-linter"
            ].join(', ')) {
                condition('SUCCESS')
                triggerWithNoParameters()
            }
        }
    }
}

job("${folder0}/ios-build") {
    displayName('iOS Build')
    deliveryPipelineConfiguration('Build', 'iOS Build')
    steps { shell('echo "hello world"') }
    publishers {
        downstreamParameterized {
            trigger("${folder0}/ios-deploy") {
                condition('SUCCESS')
                triggerWithNoParameters()
            }
        }
    }
}

job("${folder0}/ios-deploy") {
    displayName('iOS Deploy')
    deliveryPipelineConfiguration('Deploy', 'iOS Deploy')
    steps { shell('echo "hello world"') }
}

job("${folder0}/android-build") {
    displayName('Android Build')
    deliveryPipelineConfiguration('Build', 'Android Build')
    steps { shell('echo "hello world"') }
    publishers {
        downstreamParameterized {
            trigger("${folder0}/android-deploy") {
                condition('SUCCESS')
                triggerWithNoParameters()
            }
        }
    }
}

job("${folder0}/android-deploy") {
    displayName('Android Deploy')
    deliveryPipelineConfiguration('Deploy', 'Android Deploy')
    steps { shell('echo "hello world"') }
}

job("${folder0}/ios-unit-tests") {
    displayName('iOS Unit Tests')
    deliveryPipelineConfiguration('Test', 'iOS Unit Tests')
    steps { shell('echo "hello world"') }
}

job("${folder0}/android-unit-tests") {
    displayName('Android Unit Tests')
    deliveryPipelineConfiguration('Test', 'Android Unit Tests')
    steps { shell('echo "hello world"') }
}

job("${folder0}/ios-linter") {
    displayName('iOS Linter')
    deliveryPipelineConfiguration('Quality', 'iOS Linter')
    steps { shell('echo "hello world"') }
}

job("${folder0}/android-linter") {
    displayName('Android Linter')
    deliveryPipelineConfiguration('Quality', 'Android Linter')
    steps { shell('echo "hello world"') }
}

deliveryPipelineView('Option 0 - Mobile Pipeline View') {
    description('Option 0: Job DSL free-style jobs')
    pipelineInstances(5)
    columns(1)
    updateInterval(2)
    enableManualTriggers(true)
    allowPipelineStart(true)
    allowRebuild(true)
    pipelines {
        component('Mobile CI/CD', "${folder0}/trigger")
    }
}

// ============================================
// OPTION 1: Hybrid - Jenkinsfile orchestrates Free-Style Jobs
// Folder: pipeline-1-hybrid
// ============================================

def folder1 = 'pipeline-1-hybrid'

folder(folder1) {
    description('Option 1: Hybrid - Jenkinsfile orchestrates free-style jobs')
    authorization {
        userPermissions('dev1', [
            'hudson.model.Item.Discover',
            'hudson.model.Item.Read',
            'hudson.model.Item.Build',
            'hudson.model.Item.Workspace'
        ])
    }
}

// Orchestrator pipeline job
pipelineJob("${folder1}/orchestrator") {
    displayName('Pipeline Orchestrator')
    description('Reads Jenkinsfile.1-hybrid and orchestrates free-style jobs')
    definition {
        cpsScm {
            scm {
                git {
                    remote { url(repoUrl) }
                    branches(branch)
                }
            }
            scriptPath('Jenkinsfile.1-hybrid')
        }
    }
}

// Free-style jobs (no triggers - orchestrator handles flow)
job("${folder1}/ios-build") {
    displayName('iOS Build')
    steps { shell('echo "hello world"') }
}

job("${folder1}/ios-deploy") {
    displayName('iOS Deploy')
    steps { shell('echo "hello world"') }
}

job("${folder1}/android-build") {
    displayName('Android Build')
    steps { shell('echo "hello world"') }
}

job("${folder1}/android-deploy") {
    displayName('Android Deploy')
    steps { shell('echo "hello world"') }
}

job("${folder1}/ios-unit-tests") {
    displayName('iOS Unit Tests')
    steps { shell('echo "hello world"') }
}

job("${folder1}/android-unit-tests") {
    displayName('Android Unit Tests')
    steps { shell('echo "hello world"') }
}

job("${folder1}/ios-linter") {
    displayName('iOS Linter')
    steps { shell('echo "hello world"') }
}

job("${folder1}/android-linter") {
    displayName('Android Linter')
    steps { shell('echo "hello world"') }
}

// ============================================
// OPTION 2: Blue Ocean - Single Jenkinsfile with restart from stage
// Folder: pipeline-2-blueocean
// ============================================

def folder2 = 'pipeline-2-blueocean'

folder(folder2) {
    description('Option 2: Blue Ocean - Single Jenkinsfile, restart from stage in Blue Ocean UI')
    authorization {
        userPermissions('dev1', [
            'hudson.model.Item.Discover',
            'hudson.model.Item.Read',
            'hudson.model.Item.Build',
            'hudson.model.Item.Workspace'
        ])
    }
}

pipelineJob("${folder2}/pipeline") {
    displayName('Mobile Pipeline')
    description('Single Jenkinsfile - use Blue Ocean UI to restart from stage')
    definition {
        cpsScm {
            scm {
                git {
                    remote { url(repoUrl) }
                    branches(branch)
                }
            }
            scriptPath('Jenkinsfile.2-blueocean')
        }
    }
}

// ============================================
// OPTION 3: Skip Params - Single Jenkinsfile with skip parameters
// Folder: pipeline-3-skip-params
// ============================================

def folder3 = 'pipeline-3-skip-params'

folder(folder3) {
    description('Option 3: Skip Params - Single Jenkinsfile with SKIP_* parameters')
    authorization {
        userPermissions('dev2', [
            'hudson.model.Item.Discover',
            'hudson.model.Item.Read',
            'hudson.model.Item.Build',
            'hudson.model.Item.Workspace'
        ])
    }
}

pipelineJob("${folder3}/pipeline") {
    displayName('Mobile Pipeline')
    description('Single Jenkinsfile with skip parameters - re-run with SKIP_* flags to resume')
    definition {
        cpsScm {
            scm {
                git {
                    remote { url(repoUrl) }
                    branches(branch)
                }
            }
            scriptPath('Jenkinsfile.3-skip-params')
        }
    }
}

// ============================================
// OPTION 4A: Job DSL + Pipeline Jobs with Jenkinsfiles
// Folder: pipeline-4a
// Note: pipelineJobs don't support publishers{}, so each job is standalone
// ============================================

def folder4a = 'pipeline-4a'

folder(folder4a) {
    description('Option 4A: pipelineJobs reading Jenkinsfiles (each job standalone)')
    authorization {
        userPermissions('dev2', [
            'hudson.model.Item.Discover',
            'hudson.model.Item.Read',
            'hudson.model.Item.Build',
            'hudson.model.Item.Workspace'
        ])
    }
}

pipelineJob("${folder4a}/trigger") {
    displayName('Pipeline Trigger')
    definition {
        cpsScm {
            scm {
                git {
                    remote { url(repoUrl) }
                    branches(branch)
                }
            }
            scriptPath('ci/trigger.Jenkinsfile')
        }
    }
}

pipelineJob("${folder4a}/ios-build") {
    displayName('iOS Build')
    definition {
        cpsScm {
            scm {
                git {
                    remote { url(repoUrl) }
                    branches(branch)
                }
            }
            scriptPath('ci/ios-build.Jenkinsfile')
        }
    }
}

pipelineJob("${folder4a}/ios-deploy") {
    displayName('iOS Deploy')
    definition {
        cpsScm {
            scm {
                git {
                    remote { url(repoUrl) }
                    branches(branch)
                }
            }
            scriptPath('ci/ios-deploy.Jenkinsfile')
        }
    }
}

pipelineJob("${folder4a}/android-build") {
    displayName('Android Build')
    definition {
        cpsScm {
            scm {
                git {
                    remote { url(repoUrl) }
                    branches(branch)
                }
            }
            scriptPath('ci/android-build.Jenkinsfile')
        }
    }
}

pipelineJob("${folder4a}/android-deploy") {
    displayName('Android Deploy')
    definition {
        cpsScm {
            scm {
                git {
                    remote { url(repoUrl) }
                    branches(branch)
                }
            }
            scriptPath('ci/android-deploy.Jenkinsfile')
        }
    }
}

pipelineJob("${folder4a}/ios-unit-tests") {
    displayName('iOS Unit Tests')
    definition {
        cpsScm {
            scm {
                git {
                    remote { url(repoUrl) }
                    branches(branch)
                }
            }
            scriptPath('ci/ios-unit-tests.Jenkinsfile')
        }
    }
}

pipelineJob("${folder4a}/android-unit-tests") {
    displayName('Android Unit Tests')
    definition {
        cpsScm {
            scm {
                git {
                    remote { url(repoUrl) }
                    branches(branch)
                }
            }
            scriptPath('ci/android-unit-tests.Jenkinsfile')
        }
    }
}

pipelineJob("${folder4a}/ios-linter") {
    displayName('iOS Linter')
    definition {
        cpsScm {
            scm {
                git {
                    remote { url(repoUrl) }
                    branches(branch)
                }
            }
            scriptPath('ci/ios-linter.Jenkinsfile')
        }
    }
}

pipelineJob("${folder4a}/android-linter") {
    displayName('Android Linter')
    definition {
        cpsScm {
            scm {
                git {
                    remote { url(repoUrl) }
                    branches(branch)
                }
            }
            scriptPath('ci/android-linter.Jenkinsfile')
        }
    }
}

// ============================================
// SEED JOB
// ============================================

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
