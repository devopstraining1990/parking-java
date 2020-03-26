pipeline{
agent any
stages{
stage('scm')
{
steps{
git ''
}
}
stage('QG')
{
steps{
sh ''mvn -Dmaven.test.failure.ignore clean verify'
}
}
}
}
