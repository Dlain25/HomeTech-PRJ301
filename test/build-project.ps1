param(
    [Parameter(Mandatory=$true)][string]$TomcatHome,
    [string]$NetBeansHome = "$env:ProgramFiles/NetBeans-17/netbeans",
    [switch]$CheckJsps
)
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$server = (Resolve-Path -LiteralPath $TomcatHome).Path
$ant = Join-Path $NetBeansHome 'extide/ant/bin/ant.bat'
$copyLibs = Join-Path $NetBeansHome 'java/ant/extra/org-netbeans-modules-java-j2seproject-copylibstask.jar'
if (!(Test-Path -LiteralPath $ant)) { throw 'Pass -NetBeansHome pointing to the netbeans directory.' }
$platform = "$server/lib/servlet-api.jar;$server/lib/jsp-api.jar;$server/lib/el-api.jar"
# Avoid inheriting unrelated machine settings when building from a terminal.
$local = Join-Path $root '.local'
New-Item -ItemType Directory -Force $local | Out-Null
$empty = Join-Path $local 'build.properties'
if (!(Test-Path -LiteralPath $empty)) { [IO.File]::WriteAllText($empty, '') }
& $ant -q -f (Join-Path $root 'build.xml') "-Duser.properties.file=$empty" "-Dj2ee.server.home=$server" "-Dj2ee.platform.classpath=$platform" "-Dlibs.CopyLibs.classpath=$copyLibs" dist
if ($LASTEXITCODE -ne 0) { throw 'NetBeans Ant build failed.' }
if ($CheckJsps) {
    $cp = "$server/bin/tomcat-juli.jar;$server/lib/*;$NetBeansHome/extide/ant/lib/*;$root/web/WEB-INF/lib/*;$root/build/web/WEB-INF/classes"
    & java -cp $cp org.apache.jasper.JspC -uriroot "$root/build/web" -d "$root/build/jsp-check" -compile -source 17 -target 17
    if ($LASTEXITCODE -ne 0) { throw 'JSP compilation failed.' }
}
