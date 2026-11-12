$ErrorActionPreference = 'Stop'
$project = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$javac = Get-Command javac -ErrorAction Stop
$java = Get-Command java -ErrorAction Stop
$out = Join-Path ([IO.Path]::GetTempPath()) ('hometech-checks-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $out | Out-Null
try {
    $sources = @('DeliveryDateUtil','ResetTokenUtil','OrderRules','RateLimiter','VietQrUtil','FeatureConfig') | ForEach-Object { Join-Path $project ('src/java/util/' + $_ + '.java') }
    $sources += Join-Path $PSScriptRoot 'CoreChecks.java'
    $sources += Join-Path $PSScriptRoot 'WorkflowChecks.java'
    & $javac.Source -encoding UTF-8 -source 17 -target 17 -d $out @sources
    if ($LASTEXITCODE -ne 0) { throw 'Compilation failed.' }
    foreach ($name in @('CoreChecks','WorkflowChecks')) {
        & $java.Source -cp $out $name
        if ($LASTEXITCODE -ne 0) { throw "Check failed: $name" }
    }
} finally {
    Remove-Item -LiteralPath $out -Recurse -Force
}
