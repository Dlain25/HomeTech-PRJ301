param(
    [Parameter(Mandatory=$true)][string]$TomcatHome,
    [switch]$Database
)
$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$classes = Join-Path $projectRoot 'build/web/WEB-INF/classes'
if (!(Test-Path -LiteralPath $classes)) { throw 'Build project with NetBeans/Ant first.' }
$destination = Join-Path $projectRoot 'build/public-tests'
New-Item -ItemType Directory -Force $destination | Out-Null
$classpath = "$classes;$(Join-Path $projectRoot 'web/WEB-INF/lib/*');$(Join-Path $TomcatHome 'lib/*')"
& javac -encoding UTF-8 --release 17 -cp $classpath -d $destination (Join-Path $PSScriptRoot 'PublicModuleChecks.java') (Join-Path $PSScriptRoot 'PublicDatabaseReadCheck.java') (Join-Path $PSScriptRoot 'PublicSqlAudit.java')
if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed.' }
& java -cp "$destination;$classpath" controller.PublicModuleChecks
if ($LASTEXITCODE -ne 0) { throw 'Offline checks failed.' }
if (Test-Path -LiteralPath (Join-Path $projectRoot 'PRJ301_G4.sql')) {
    & java -cp "$destination;$classpath" publicmodule.PublicSqlAudit $projectRoot
    if ($LASTEXITCODE -ne 0) { throw 'SQL/source schema audit failed.' }
} else {
    Write-Output 'SKIP: source SQL schema file was not supplied; use database integration checks against the existing schema.'
}
if ($Database) {
    & java -cp "$destination;$classpath" publicmodule.PublicDatabaseReadCheck
    if ($LASTEXITCODE -ne 0) { throw 'Database read checks failed; see connection/configuration or assertion output.' }
}

