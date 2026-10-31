<#
  Prueba el endpoint de la API REST que crea una Orden de Venta, igual que lo hace la app Android.

  Uso (desde la carpeta api):
    .\probar-orden-venta.ps1 -Usuario jzetinob -Nit 8888888-8 -Productos "9384-dfd:2,5d5d2d2ee:1" -Observaciones "Prueba desde PowerShell"

  - Pide la contraseña sin mostrarla en pantalla (no se guarda en ningún lado).
  - Toma la URL y la clave pública de la app móvil (movil/.../ConfigSupabase.java).
  - Productos: "codigo:cantidad" separados por comas.
#>
param(
    [Parameter(Mandatory)] [string]$Usuario,
    [Parameter(Mandatory)] [string]$Nit,
    [Parameter(Mandatory)] [string]$Productos,
    [string]$Observaciones = ""
)
$ErrorActionPreference = 'Stop'

$config = Get-Content (Join-Path $PSScriptRoot '..\movil\app\src\main\java\com\josue\ventas\movil\datos\ConfigSupabase.java') -Raw
$url = [regex]::Match($config, 'URL\s*=\s*"([^"]+)"').Groups[1].Value
$clavePublica = [regex]::Match($config, 'CLAVE_PUBLICA\s*=\s*"([^"]+)"').Groups[1].Value

# POST /rest/v1/rpc/<funcion> con cuerpo JSON; si la API responde un error, muestra su código y mensaje.
function Invocar([string]$funcion, [hashtable]$cuerpo) {
    $json = $cuerpo | ConvertTo-Json -Depth 5
    try {
        return Invoke-RestMethod -Method Post -Uri "$url/rest/v1/rpc/$funcion" -Headers @{ apikey = $clavePublica } `
            -ContentType 'application/json; charset=utf-8' -Body ([Text.Encoding]::UTF8.GetBytes($json))
    } catch {
        $codigo = [int]$_.Exception.Response.StatusCode
        $mensaje = try { ($_.ErrorDetails.Message | ConvertFrom-Json).message } catch { $_.Exception.Message }
        throw "La API respondió HTTP $codigo - $mensaje"
    }
}

$segura = Read-Host "Contraseña de $Usuario" -AsSecureString
$clave = [Net.NetworkCredential]::new('', $segura).Password
$sesion = Invocar 'app_login' @{ p_usuario = $Usuario; p_clave = $clave }
$clave = $null
Write-Host "Sesión iniciada: $($sesion.nombre) ($($sesion.rol))"

try {
    $detalles = @($Productos.Split(',') | ForEach-Object {
            $partes = $_.Split(':')
            @{ codigo = $partes[0].Trim(); cantidad = [int]$partes[1] }
        })
    Write-Host "POST $url/rest/v1/rpc/app_registrar_orden_venta"
    $orden = Invocar 'app_registrar_orden_venta' @{
        p_token = $sesion.token; p_nit = $Nit; p_observaciones = $Observaciones; p_detalles = $detalles
    }
    Write-Host ("Orden registrada: {0} · {1} · Total Q{2} · {3}" -f $orden.numero_orden, $orden.cliente, $orden.total, $orden.estado)
} finally {
    Invocar 'app_logout' @{ p_token = $sesion.token } | Out-Null
}
