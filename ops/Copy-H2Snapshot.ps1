[CmdletBinding()]
param(
    [Parameter(Mandatory)][ValidateSet('Backup','Restore')][string]$Mode,
    [Parameter(Mandatory)][string]$Source,
    [Parameter(Mandatory)][string]$Destination,
    [ValidatePattern('^[a-fA-F0-9]{64}$')][string]$ExpectedSha256
)

$ErrorActionPreference = 'Stop'
if ($Mode -eq 'Restore' -and -not $ExpectedSha256) {
    throw 'Restore requires the SHA-256 recorded at backup time.'
}
$sourcePath = (Resolve-Path -LiteralPath $Source).ProviderPath
$destinationPath = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($Destination)
$stagingPath = "$destinationPath.$([Guid]::NewGuid()).partial"
$inputFile = [IO.File]::Open($sourcePath, 'Open', 'Read', 'None')
try {
    $stage = [IO.File]::Open($stagingPath, 'CreateNew', 'Write', 'None')
    try {
        $inputFile.CopyTo($stage)
        $stage.Flush($true)
    } finally {
        $stage.Dispose()
    }
    $digest = (Get-FileHash -LiteralPath $stagingPath -Algorithm SHA256).Hash
    if ($ExpectedSha256 -and $digest -ne $ExpectedSha256) {
        throw 'Snapshot checksum mismatch.'
    }
    [IO.File]::Move($stagingPath, $destinationPath)
    [PSCustomObject]@{ Operation = $Mode; Path = $destinationPath; SHA256 = $digest }
} finally {
    $inputFile.Dispose()
    if ([IO.File]::Exists($stagingPath)) { [IO.File]::Delete($stagingPath) }
}
