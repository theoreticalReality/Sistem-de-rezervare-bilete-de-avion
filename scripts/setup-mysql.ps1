param(
    [string]$MysqlExe = "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe",
    [string]$HostName = "localhost",
    [int]$Port = 3306,
    [string]$RootUser = "root",
    [string]$Database = "airportdb",
    [string]$AppUser = "airport_user",
    [string]$AppPassword = "airport_pass"
)

if (-not (Test-Path -LiteralPath $MysqlExe)) {
    $found = Get-ChildItem "C:\Program Files\MySQL" -Recurse -Filter mysql.exe -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($null -eq $found) {
        throw "Nu am gasit mysql.exe. Instaleaza MySQL Server sau seteaza -MysqlExe cu calea corecta."
    }
    $MysqlExe = $found.FullName
}

$securePassword = Read-Host -Prompt "Parola pentru utilizatorul MySQL $RootUser" -AsSecureString
$passwordPtr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
$rootPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPtr)

try {
$sql = @"
CREATE DATABASE IF NOT EXISTS $Database CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '$AppUser'@'localhost' IDENTIFIED BY '$AppPassword';
CREATE USER IF NOT EXISTS '$AppUser'@'%' IDENTIFIED BY '$AppPassword';
GRANT ALL PRIVILEGES ON $Database.* TO '$AppUser'@'localhost';
GRANT ALL PRIVILEGES ON $Database.* TO '$AppUser'@'%';
FLUSH PRIVILEGES;
"@

    & $MysqlExe --protocol=TCP -h $HostName -P $Port -u $RootUser "--password=$rootPassword" -e $sql
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }

    Write-Host "MySQL este pregatit pentru aplicatie: baza '$Database', user '$AppUser'."
}
finally {
    if ($passwordPtr -ne [IntPtr]::Zero) {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPtr)
    }
}
