node("rubber") {
   stage ('Run Hub-PackMan'){
       withEnv(["PATH=${tool 'jdk8'}/bin:${env.PATH}","JAVA_HOME=${tool 'jdk8'}"])
       {
           sh 'curl -o hub-packman.sh https://blackducksoftware.github.io/hub-packman/hub-packman.sh'
           sh 'bash ./hub-packman.sh'
       }
   }
}
