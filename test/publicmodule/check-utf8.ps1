param(
    [string]$BaseUrl,
    [string]$FixtureBaseUrl
)
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$strictUtf8 = [Text.UTF8Encoding]::new($false, $true)
$mojibakePattern = '(?:\u00E1[\u00BB\u00BA][\u0080-\u00BF\u2000-\u2122]|\u00C6[\u00B0\u00A1]|\uFFFD)'
$checked = 0
$roots = @((Join-Path $root 'src/java'), (Join-Path $root 'web'))
$files = Get-ChildItem $roots -Recurse -File | Where-Object { $_.Extension -in '.java','.jsp','.jspf','.css','.js','.svg' }
if (Test-Path -LiteralPath (Join-Path $root 'PRJ301_G4.sql')) {
    $files += Get-Item (Join-Path $root 'PRJ301_G4.sql')
}
foreach ($file in $files) {
    $source = $strictUtf8.GetString([IO.File]::ReadAllBytes($file.FullName))
    if ($source -match $mojibakePattern) { throw "Suspicious mojibake in source: $($file.FullName)" }
    if ($file.Extension -in '.jsp','.jspf') {
        if ($source -notmatch '<%@\s*page\b[^%]*pageEncoding\s*=\s*"UTF-8"') {
            throw "JSP/JSPF missing its OWN UTF-8 pageEncoding: $($file.FullName)"
        }
    }
    if ($file.Extension -eq '.jsp' -and $source -notmatch 'contentType\s*=\s*"text/html;\s*charset=UTF-8"') {
        throw "JSP missing UTF-8 contentType: $($file.FullName)"
    }
    $checked++
}
Write-Output "PASS: $checked source files decode strictly as UTF-8; every JSP/JSPF declares pageEncoding."
if (!$BaseUrl -and !$FixtureBaseUrl) { return }
Add-Type -AssemblyName System.Net.Http
$client = [Net.Http.HttpClient]::new()
$httpChecks = 0
function Assert-Page([string]$Url, [string[]]$Expected, [bool]$FullDocument = $true) {
    $response = $client.GetAsync($Url).GetAwaiter().GetResult()
    try {
        $charset = $response.Content.Headers.ContentType.CharSet
        if ($charset -ne 'UTF-8') { throw "Response charset is not UTF-8: $Url ($charset)" }
        if ([int]$response.StatusCode -eq 500) { throw "JSP compilation/runtime failure: $Url" }
        $bytes = $response.Content.ReadAsByteArrayAsync().GetAwaiter().GetResult()
        $html = $strictUtf8.GetString($bytes)
        if ($html -match $mojibakePattern) { throw "Mojibake in rendered response: $Url" }
        if ($FullDocument -and $html -notmatch '<meta charset="UTF-8">') { throw "HTML charset missing: $Url" }
        foreach ($phrase in $Expected) {
            if (!$html.Contains($phrase)) { throw "Expected UTF-8 text missing at $Url : $phrase" }
        }
        Write-Output "PASS HTTP $([int]$response.StatusCode): $Url"
        $script:httpChecks++
    } finally { $response.Dispose() }
}
try {
    if ($BaseUrl) {
        $BaseUrl = $BaseUrl.TrimEnd('/')
        Assert-Page "$BaseUrl/home" @('Nhà đẹp hơn.', 'Hỗ trợ khách hàng')
        Assert-Page "$BaseUrl/products" @('Khám phá sản phẩm', 'Hỗ trợ khách hàng')
        Assert-Page "$BaseUrl/product?id=abc" @('Không tìm thấy sản phẩm', 'Tiếp tục khám phá')
        Assert-Page "$BaseUrl/product?id=1" @('Hỗ trợ khách hàng')
        Assert-Page "$BaseUrl/views/common/header.jsp" @('Tìm theo tên sản phẩm hoặc SKU') $false
        Assert-Page "$BaseUrl/views/common/navbar.jsp" @('Khám phá sản phẩm') $false
        Assert-Page "$BaseUrl/views/common/footer.jsp" @('Hỗ trợ khách hàng') $false
        Assert-Page "$BaseUrl/views/error/404.jsp" @('Về trang chủ')
    }
    if ($FixtureBaseUrl) {
        $FixtureBaseUrl = $FixtureBaseUrl.TrimEnd('/')
        Assert-Page "$FixtureBaseUrl/fixture?view=home" @('Nổi bật', 'Xem chi tiết', 'Từ ')
        Assert-Page "$FixtureBaseUrl/fixture?view=products" @('Nổi bật', 'Xem chi tiết', 'Từ ')
        $search = [Uri]::EscapeDataString('nồi cơm điện Đà Nẵng')
        Assert-Page "$FixtureBaseUrl/fixture?view=products&q=$search" @('nồi cơm điện Đà Nẵng')
        Assert-Page "$FixtureBaseUrl/fixture?view=detail" @('Phiên bản &amp; màu sắc', 'Thông tin chi tiết', 'Tiêu chuẩn')
        Assert-Page "$FixtureBaseUrl/fixture?view=products&empty=true" @('Chưa tìm thấy sản phẩm')
        Assert-Page "$FixtureBaseUrl/fixture?view=detail&empty=true" @('Sản phẩm chưa có phiên bản khả dụng.')
    }
} finally { $client.Dispose() }
Write-Output "PASS: $httpChecks rendered UTF-8 checks. HTTP 503 means database unavailable, not a passed database test."

