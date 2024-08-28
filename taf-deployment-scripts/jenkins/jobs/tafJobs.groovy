import javaposse.jobdsl.dsl.views.jobfilter.MatchType
import javaposse.jobdsl.dsl.views.jobfilter.Status

def protocol = 'ssh://'
def groupId = 'com.ericsson.cifwk'
def repo = 'ERICtaf_util'
def gerritMaster = 'gerritMaster'
def gerritMirror = 'gerritmirror.lmera'
def path = '.ericsson.se:29418/OSS/' + groupId + '/' + repo
def GIT_URL = protocol + gerritMaster + path
def GIT_MIRROR_URL = protocol + gerritMirror + path

// Jenkins properties
def JDK = 'JDK 1.7.0_21'

/*
Full API documentation:
https://jenkinsci.github.io/job-dsl-plugin/

Job DSL playground:
http://job-dsl.herokuapp.com/

Groovy DSL in Intellij IDEA:
https://github.com/sheehan/job-dsl-gradle-example
*/

// Job names
def final UNIT_JOB = 'TAF_A_Unit'
def final INTEGRATION_JOB = 'TAF_B_Itest'
def final RELEASE_JOB = 'TAF_D_Build_Release'
def final CONFIGURE_ENM_VAPP_JOB = 'Configure_ENM_Ready_vApp_To_Execute_Taffit'

job(CONFIGURE_ENM_VAPP_JOB){
    description 'install git and add the ssh key to the known hosts file for gerrit on the gateway of the vApp'
    steps {
        shell('sudo yum -y install git;\n' +
                'ssh-keygen -R gerrit.ericsson.se;\n' +
                'ssh-keyscan -p 29418 gerrit.ericsson.se >> ~/.ssh/known_hosts;\n' +
                'ssh-keygen -R gerritmirror.lmera.ericsson.se;\n' +
                'ssh-keyscan -p 29418 gerritmirror.lmera.ericsson.se >> ~/.ssh/known_hosts;')
    }
    label('ENM_READY_VAPP')
    jdk('Default')
    logRotator(21, 30)
}

mavenJob(UNIT_JOB) {

    description('''Triggers when there is a change in master branch. Compiles code and runs Unit tests.<br/>
            Success triggers job for iTests''')
    logRotator(21, 30)
    keepDependencies(false)

    blockOn([INTEGRATION_JOB, RELEASE_JOB])

    scm {
        git {
            remote {
                name('gm')
                url(GIT_MIRROR_URL)
            }
            remote {
                name('gc')
                url(GIT_URL)
            }
            branch('master')
            clean()
        }
    }
    quietPeriod(30)
    checkoutRetryCount(10)
    jdk(JDK)

    triggers {
        gerrit {
            events {
                refUpdated()
            }
            project('ERICtaf_util', 'master')
        }
    }

    goals('-T 4 clean source:jar deploy -PskipTestRun')
    mavenOpts('-Xmx8g -XX:MaxPermSize=4g -Dmaven.test.failure.ignore=false')
    archivingDisabled(true)

    publishers {
        buildPipelineTrigger(INTEGRATION_JOB) {

        }
    }

    preBuildSteps {
        shell('export GIT_URL=${GIT_URL_1}')

        // cannot push back to gerrit mirror so need to set url to GC
        shell('repo=$(echo $GIT_URL | sed \'s#.*OSS/##g\')')
        shell('git remote set-url --push gc ssh://gerrit.ericsson.se:29418/OSS/${repo}')

        shell('git checkout master || git checkout -b master')
        shell('git reset --hard gm/master')
    }

    postBuildSteps {

    }

    // TODO: complete pushing to branch
//    configure { project ->
//        project / 'publishers' << 'hudson.plugins.git.GitPublisher' {
//            pushOnlyIfSuccess true
//            forcePush false
//            pushMerge false
//        }
//    }

}

// TAF View
listView('ERICtaf_util') {
    description('All TAF jobs')
    jobs {
        regex(/^TAF_.*/)
    }
    jobFilters {
        status {
            matchType(MatchType.EXCLUDE_MATCHED)
            status(Status.DISABLED)
        }
    }

    columns {
        status()
        lastBuildConsole()
        configureProject()
        buildButton()
        weather()
        name()
        lastDuration()
        robotResults()
        claim()
        lastBuildNode()
        lastSuccess()
    }
}