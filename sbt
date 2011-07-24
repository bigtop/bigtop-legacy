java -Xmx768M -Xss2M -XX:+CMSClassUnloadingEnabled -XX:PermSize=64M -XX:MaxPermSize=128M -jar `dirname $0`/sbt-launcher.jar "$@"


