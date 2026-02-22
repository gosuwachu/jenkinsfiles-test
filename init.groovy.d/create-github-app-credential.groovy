import jenkins.model.Jenkins
import com.cloudbees.plugins.credentials.CredentialsScope
import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import org.jenkinsci.plugins.github_branch_source.GitHubAppCredentials

def jenkins = Jenkins.get()
def domain = Domain.global()
def store = jenkins.getExtensionList(SystemCredentialsProvider.class)[0].getStore()

// Check if credential already exists
def existing = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
    com.cloudbees.plugins.credentials.common.StandardCredentials.class,
    jenkins,
    null,
    null
).find { it.id == 'github-app' }

if (existing) {
    println "GitHub App credential 'github-app' already exists, skipping creation"
    return
}

def pemFile = new File('/var/jenkins_home/github-app-key.pem')
if (!pemFile.exists()) {
    println "WARNING: /var/jenkins_home/github-app-key.pem not found, skipping GitHub App credential creation"
    return
}

def appId = System.getenv('GITHUB_APP_ID')
if (!appId) {
    println "WARNING: GITHUB_APP_ID env var not set, skipping GitHub App credential creation"
    return
}

def privateKey = hudson.util.Secret.fromString(pemFile.text)

def credential = new GitHubAppCredentials(
    CredentialsScope.GLOBAL,
    'github-app',
    'GitHub App for Checks API',
    appId,
    privateKey
)

store.addCredentials(domain, credential)
println "GitHub App credential 'github-app' created successfully (App ID: ${appId})"
