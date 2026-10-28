[CmdletBinding()]
param([switch]$CheckOnly)

$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$Work = Join-Path $Root '.work'
New-Item -ItemType Directory -Force $Work | Out-Null

function Test-IsAdministrator {
    $identity = [Security.Principal.WindowsIdentity]::GetCurrent()
    $principal = [Security.Principal.WindowsPrincipal]$identity
    return $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
}

function Test-MySqlPort {
    $client = New-Object System.Net.Sockets.TcpClient
    try {
        $task = $client.ConnectAsync('127.0.0.1', 3306)
        if (-not $task.Wait(700)) { return $false }
        return $client.Connected
    } catch { return $false }
    finally { $client.Dispose() }
}

function Read-ProjectConfig([string]$name, [string]$fallback) {
    $configPath = Join-Path $Root 'config.properties'
    if (-not (Test-Path $configPath)) { return $fallback }
    $line = Get-Content $configPath | Where-Object { $_ -match "^$name=" } | Select-Object -First 1
    if ($line) { return ($line -split '=', 2)[1].Trim() }
    return $fallback
}

function Invoke-MySql([string[]]$arguments) {
    & $mysqlCli @arguments
    if ($LASTEXITCODE -ne 0) { throw "MySQL command failed with exit code $LASTEXITCODE." }
}

function Invoke-MySqlValue([string]$query) {
    $result = & $mysqlCli --protocol=tcp -h 127.0.0.1 -P 3306 -u $dbUser --batch --skip-column-names -e $query 2>&1
    if ($LASTEXITCODE -ne 0) { throw (($result | Out-String).Trim()) }
    return ($result | Select-Object -Last 1).ToString().Trim()
}

# Prefer XAMPP because this project uses its default root/no-password setup.
$mysqlCli = $null
$serverExe = $null
$serverArgs = @()
$xamppCli = 'C:\xampp\mysql\bin\mysql.exe'
$xamppServer = 'C:\xampp\mysql\bin\mariadbd.exe'
$xamppIni = 'C:\xampp\mysql\bin\my.ini'
$mysqlCliPath = 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'
$mysqlServerPath = 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe'

if (Test-Path $xamppCli) {
    $mysqlCli = $xamppCli
    $serverExe = $xamppServer
    $serverArgs = @("--defaults-file=$xamppIni", '--console')
} elseif (Test-Path $mysqlCliPath) {
    $mysqlCli = $mysqlCliPath
    $serverExe = $mysqlServerPath
    $serverArgs = @('--console')
} else {
    throw 'No MySQL or XAMPP installation was found. Install XAMPP or MySQL Server first.'
}

$dbUser = Read-ProjectConfig 'db.username' 'root'
$dbPassword = Read-ProjectConfig 'db.password' ''
$env:MYSQL_PWD = $dbPassword

try {
    if (-not (Test-MySqlPort)) {
        if (-not (Test-IsAdministrator)) {
            Write-Host '[ATM] Administrator permission is required to start XAMPP MySQL.' -ForegroundColor Yellow
            $args = "-NoProfile -ExecutionPolicy Bypass -File `"$($MyInvocation.MyCommand.Path)`""
            if ($CheckOnly) { $args += ' -CheckOnly' }
            Start-Process powershell.exe -Verb RunAs -ArgumentList $args -WorkingDirectory $Root | Out-Null
            exit 0
        }

        if (-not (Test-Path $serverExe)) { throw "The database server executable was not found: $serverExe" }
        $serviceName = 'ATM-MariaDB'
        $service = Get-Service -Name $serviceName -ErrorAction SilentlyContinue
        if (-not $service) {
            Write-Host '[ATM] Registering XAMPP MariaDB as a detached Windows service...' -ForegroundColor Cyan
            & $serverExe --install $serviceName "--defaults-file=$xamppIni" 2>&1 | Out-Host
            if ($LASTEXITCODE -ne 0) { throw 'Could not register the local MariaDB service.' }
        }
        Write-Host '[ATM] Starting XAMPP MariaDB service...' -ForegroundColor Cyan
        Start-Service -Name $serviceName -ErrorAction Stop

        $ready = $false
        for ($i = 1; $i -le 30; $i++) {
            Start-Sleep -Seconds 1
            if (Test-MySqlPort) { $ready = $true; break }
        }
        if (-not $ready) {
            throw 'MariaDB did not become ready on port 3306. Check the Windows service named ATM-MariaDB.'
        }
    }

    Write-Host '[ATM] MySQL is running.' -ForegroundColor Green
    if ($CheckOnly) { exit 0 }

    $schemaPath = Join-Path $Root 'database\schema.sql'
    $samplePath = Join-Path $Root 'database\sample_data.sql'
    $dbExists = Invoke-MySqlValue "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = 'atm_transaction_system';"
    if ($dbExists -eq '0') {
        Write-Host '[ATM] Creating database schema...' -ForegroundColor Cyan
        Get-Content -Raw $schemaPath | & $mysqlCli --protocol=tcp -h 127.0.0.1 -P 3306 -u $dbUser
        if ($LASTEXITCODE -ne 0) { throw 'Schema initialization failed.' }
    } else {
        $tableCount = Invoke-MySqlValue "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'atm_transaction_system';"
        if ([int]$tableCount -lt 7) {
            Write-Host '[ATM] Completing database schema...' -ForegroundColor Cyan
            Get-Content -Raw $schemaPath | & $mysqlCli --protocol=tcp -h 127.0.0.1 -P 3306 -u $dbUser
            if ($LASTEXITCODE -ne 0) { throw 'Schema initialization failed.' }
        } else {
            Write-Host '[ATM] Schema already exists; leaving it unchanged.' -ForegroundColor DarkGray
        }
    }

    $customerCount = Invoke-MySqlValue "SELECT COUNT(*) FROM atm_transaction_system.customers;"
    if ([int]$customerCount -eq 0) {
        Write-Host '[ATM] Loading synthetic sample data...' -ForegroundColor Cyan
        Get-Content -Raw $samplePath | & $mysqlCli --protocol=tcp -h 127.0.0.1 -P 3306 -u $dbUser
        if ($LASTEXITCODE -ne 0) { throw 'Sample data initialization failed.' }
    } else {
        Write-Host '[ATM] Existing customer data found; leaving sample data unchanged.' -ForegroundColor DarkGray
    }

    if (-not (Test-Path (Join-Path $Root 'bin\com\atm\ATMApplication.class'))) {
        Write-Host '[ATM] Project not built yet. Building now...' -ForegroundColor Cyan
        & (Join-Path $Root 'build.bat')
        if ($LASTEXITCODE -ne 0) { throw 'The Java build failed.' }
    }

    Write-Host '[ATM] Launching ATM Transaction System...' -ForegroundColor Green
    Push-Location $Root
    try { & java -cp 'bin;lib/*' com.atm.ATMApplication }
    finally { Pop-Location }
} catch {
    Write-Host "[ATM] $($_.Exception.Message)" -ForegroundColor Red
    Write-Host '[ATM] Check the Windows service ATM-MariaDB and the XAMPP MySQL error log if startup fails.' -ForegroundColor Yellow
    if (-not $CheckOnly) { Read-Host 'Press Enter to close' }
    exit 1
} finally {
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
}
