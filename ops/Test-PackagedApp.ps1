[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Net.Http
$artifacts = @(Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot '../target') -Filter '*.jar' -File)
if ($artifacts.Count -ne 1) { throw 'Expected one application JAR. Run mvn clean verify first.' }

$work = New-Item -ItemType Directory -Path (Join-Path ([IO.Path]::GetTempPath()) ([guid]::NewGuid().ToString('N')))
$logPath = Join-Path $work.FullName 'startup.log'
$errorPath = Join-Path $work.FullName 'startup.err'
$handler = [Net.Http.HttpClientHandler]::new()
$handler.AllowAutoRedirect = $false
$client = [Net.Http.HttpClient]::new($handler)
$client.Timeout = [TimeSpan]::FromSeconds(10)
$app = $null
try {
    $arguments = @('-jar', ('"{0}"' -f $artifacts[0].FullName), '--server.address=127.0.0.1',
        '--server.port=0', '--spring.datasource.url=jdbc:h2:mem:packagecheck',
        '--logging.level.org.springframework.boot.web.embedded.tomcat.TomcatWebServer=INFO')
    $java = Join-Path $env:JAVA_HOME 'bin/java.exe'
    $app = Start-Process $java -ArgumentList $arguments -WorkingDirectory $work.FullName -WindowStyle Hidden -PassThru -RedirectStandardOutput $logPath -RedirectStandardError $errorPath
    $deadline = [DateTime]::UtcNow.AddSeconds(90)
    do {
        if ($app.HasExited) { throw "Application exited with code $($app.ExitCode)." }
        $started = Select-String -LiteralPath $logPath -Pattern 'Tomcat started on port (\d+)' | Select-Object -First 1
        if ($started) { break }
        Start-Sleep -Milliseconds 250
    } while ([DateTime]::UtcNow -lt $deadline)
    if (!$started) { throw 'Timed out waiting for Tomcat.' }
    $base = 'http://127.0.0.1:' + $started.Matches[0].Groups[1].Value
    $health = $client.GetStringAsync("$base/actuator/health").Result | ConvertFrom-Json
    if ($health.status -ne 'UP') { throw 'Application health check failed.' }
    $destination = 'https://example.org/package-check'
    $body = [Net.Http.StringContent]::new((@{originalUrl=$destination} | ConvertTo-Json), [Text.Encoding]::UTF8, 'application/json')
    $created = $client.PostAsync("$base/api/v1/urls", $body).Result
    if ([int]$created.StatusCode -ne 201) { throw "Create returned $($created.StatusCode)." }
    $link = $created.Content.ReadAsStringAsync().Result | ConvertFrom-Json
    $path = ([uri]$link.shortUrl).AbsolutePath
    $redirect = $client.GetAsync("$base$path").Result
    if ([int]$redirect.StatusCode -ne 302 -or $redirect.Headers.Location.AbsoluteUri -ne $destination) { throw 'Unexpected redirect.' }
    $details = $client.GetStringAsync("$base/api/v1/urls$path").Result | ConvertFrom-Json
    if ($details.clickCount -ne 1) { throw 'Expected one recorded click.' }
    Write-Output 'Packaged application passed health, creation, redirect and click-count checks.'
} catch {
    foreach ($path in @($logPath, $errorPath)) {
        if (Test-Path -LiteralPath $path) { Get-Content -LiteralPath $path -Tail 30 | Write-Warning }
    }
    throw
} finally {
    $client.Dispose()
    if ($app) {
        if (!$app.HasExited) { $app.Kill(); $app.WaitForExit() }
        $app.Dispose()
    }
    foreach ($path in @($logPath, $errorPath)) { [IO.File]::Delete($path) }
    if (!(Get-ChildItem -LiteralPath $work.FullName -Force)) { [IO.Directory]::Delete($work.FullName) }
}
