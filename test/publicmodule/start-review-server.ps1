param(
    [Parameter(Mandatory=$true)][string]$TomcatHome,
    [ValidateRange(1024,65535)][int]$Port = 18080,
    [switch]$Fixtures
)
$ErrorActionPreference = 'Stop'
if (Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue) {
    throw "Port $Port is already in use. Stop the existing review server before restarting."
}
$root = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$war = Join-Path $root 'dist/Group04.war'
if (!(Test-Path -LiteralPath $war)) { throw 'Clean and Build project first.' }
$serverHome = (Resolve-Path -LiteralPath $TomcatHome).Path
# Keep Tomcat's open jars and deployed WAR outside NetBeans build/dist.
# A Clean and Build must not try to delete files locked by this review server.
$base = Join-Path $root ".local/review-$Port"
foreach ($name in @('conf','logs','temp','webapps','work')) {
    New-Item -ItemType Directory -Force (Join-Path $base $name) | Out-Null
}
foreach ($name in @('web.xml','catalina.properties','logging.properties')) {
    Copy-Item -LiteralPath (Join-Path $serverHome "conf/$name") -Destination (Join-Path $base "conf/$name") -Force
}
$runtimeWar = Join-Path $base 'Group04.war'
Copy-Item -LiteralPath $war -Destination $runtimeWar -Force
$docBase = [Security.SecurityElement]::Escape($runtimeWar.Replace('\','/'))
$contexts = '<Context path="/Group04" docBase="' + $docBase + '"/><Context path="/renamed-shop" docBase="' + $docBase + '"/>'
if ($Fixtures) {
    $preview = Join-Path $base 'public-ui-check'
    New-Item -ItemType Directory -Force $preview | Out-Null
    Copy-Item -Path (Join-Path $root 'build/web/*') -Destination $preview -Recurse -Force
    $classpath = "$(Join-Path $root 'build/web/WEB-INF/classes');$(Join-Path $serverHome 'lib/*')"
    & javac -encoding UTF-8 --release 17 -cp $classpath -d (Join-Path $preview 'WEB-INF/classes') (Join-Path $PSScriptRoot 'PublicUiFixtureServlet.java')
    if ($LASTEXITCODE -ne 0) { throw 'UI fixture compilation failed.' }
    $webXml = Join-Path $preview 'WEB-INF/web.xml'
    $xml = [IO.File]::ReadAllText($webXml).Replace('</web-app>', '<servlet><servlet-name>UIFixture</servlet-name><servlet-class>publicmodule.PublicUiFixtureServlet</servlet-class></servlet><servlet-mapping><servlet-name>UIFixture</servlet-name><url-pattern>/fixture</url-pattern></servlet-mapping></web-app>')
    [IO.File]::WriteAllText($webXml, $xml, [Text.UTF8Encoding]::new($false))
    $previewXml = [Security.SecurityElement]::Escape($preview.Replace('\','/'))
    $contexts += '<Context path="/ui-check" docBase="' + $previewXml + '"/>'
    Write-Output 'UI fixtures enabled ONLY at /ui-check/fixture. These are not HomeTechDB data.'
}
$serverXml = '<Server port="-1"><Service name="PublicReview"><Connector address="127.0.0.1" port="' + $Port + '" protocol="HTTP/1.1" URIEncoding="UTF-8"/><Engine name="PublicReview" defaultHost="localhost"><Host name="localhost" appBase="webapps" autoDeploy="false">' + $contexts + '</Host></Engine></Service></Server>'
[IO.File]::WriteAllText((Join-Path $base 'conf/server.xml'), $serverXml, [Text.UTF8Encoding]::new($false))
Write-Output "Review server: http://127.0.0.1:$Port/Group04/ (runtime: $base; stop/restart to load a new build)"
& java "-Dcatalina.home=$serverHome" "-Dcatalina.base=$base" -cp "$(Join-Path $serverHome 'bin/bootstrap.jar');$(Join-Path $serverHome 'bin/tomcat-juli.jar')" org.apache.catalina.startup.Bootstrap start
if ($LASTEXITCODE -ne 0) { throw 'Review server stopped or failed; inspect console output.' }

