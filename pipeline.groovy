node("integrations") {
   stage ('Run Hub-PackMan'){
       withEnv(["PATH=${tool 'jdk8'}/bin:${env.PATH}","JAVA_HOME=${tool 'jdk8'}"])
       {
           sh 'curl -o hub-packman.sh $HUB_DETECT_URL'
           sh 'bash ./hub-packman.sh'
       }
   }
}
