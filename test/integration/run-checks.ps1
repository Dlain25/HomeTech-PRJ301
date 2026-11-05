param([Parameter(Mandatory=$true)][string]$TomcatHome)
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$classes = Join-Path $root 'build/web/WEB-INF/classes'
if (!(Test-Path -LiteralPath $classes)) { throw 'Build the project first.' }
$destination = Join-Path $root 'build/integration-tests'
New-Item -ItemType Directory -Force $destination | Out-Null
$classpath = "$classes;$root/web/WEB-INF/lib/*;$TomcatHome/lib/*"
& javac -encoding UTF-8 --release 17 -cp $classpath -d $destination (Join-Path $PSScriptRoot 'DatabaseIntegrationChecks.java')
if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed.' }
& java -cp "$destination;$classpath" integration.DatabaseIntegrationChecks
if ($LASTEXITCODE -ne 0) { throw 'Database integration checks failed.' }
