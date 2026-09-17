# Embabel 教程一键脚本（Windows / PowerShell）
#
# 用法：
#   .\run.ps1 list                 列出所有模块与端口
#   .\run.ps1 docker               启动 Postgres + LiteLLM + Ollama，并拉取所需模型
#   .\run.ps1 chat                 启动指定模块（自动识别是否需要指向 LiteLLM）
#   .\run.ps1 vector-store -SkipBuild
#
# 为什么需要它：52 个模块、52 个端口、两种环境变量组合（DeepSeek 直连 / LiteLLM），
# 手工敲 mvn 与 export 很容易出错。
[CmdletBinding()]
param(
    [Parameter(Position = 0)]
    [string]$Module = "list",

    # 跳过 mvn package（已构建过时更快）
    [switch]$SkipBuild,

    # 额外 JVM 参数
    [string]$JvmArgs = ""
)

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot

# 需要嵌入/视觉的模块：必须走 LiteLLM（DeepSeek 没有 embedding 接口）
$LiteLlmModules = @(
    "embabel-embeddings", "embabel-vector-store", "embabel-document-ingest",
    "embabel-memory", "embabel-multimodal"
)

function Get-Modules {
    Get-ChildItem -Path $root -Directory |
        Where-Object { $_.Name -ne "docker" -and $_.Name -ne "target" } |
        ForEach-Object {
            $cat = $_.Name
            Get-ChildItem -Path $_.FullName -Directory |
                Where-Object { $_.Name -ne "target" } |
                ForEach-Object {
                    $yml = Join-Path $_.FullName "src/main/resources/application.yml"
                    $port = "?"
                    if (Test-Path $yml) {
                        $m = Select-String -Path $yml -Pattern "port:\s*(\d+)"
                        if ($m) { $port = $m.Matches[0].Groups[1].Value }
                    }
                    [pscustomobject]@{ Category = $cat; Name = $_.Name; Port = $port; Path = $_.FullName }
                }
        }
}

function Show-List {
    $modules = Get-Modules | Sort-Object Port
    $modules | Format-Table -AutoSize @{L="端口";E={$_.Port}}, @{L="模块";E={$_.Name}}, @{L="分类";E={$_.Category}}
    Write-Host ("共 {0} 个模块。用法：.\run.ps1 <模块名>" -f $modules.Count) -ForegroundColor Cyan
    Write-Host "需要 Docker 的模块：embeddings / vector-store / document-ingest / memory / multimodal / persistence" -ForegroundColor DarkGray
}

function Start-Docker {
    Push-Location (Join-Path $root "docker")
    try {
        if (-not (Test-Path ".env")) {
            Copy-Item ".env.example" ".env"
            Write-Host "已生成 docker/.env —— 请填入 DEEPSEEK_API_KEY 后重跑" -ForegroundColor Yellow
        }
        docker compose up -d
        Write-Host "拉取 Ollama 模型（已存在会跳过）..." -ForegroundColor Cyan
        docker compose exec -T ollama ollama pull nomic-embed-text
        docker compose exec -T ollama ollama pull qwen2.5vl:3b
        Write-Host "Docker 组件就绪：LiteLLM :4000 / Ollama :11434 / Postgres :5433" -ForegroundColor Green
    }
    finally { Pop-Location }
}

function Start-Module([string]$name) {
    $target = Get-Modules | Where-Object { $_.Name -eq $name }
    if (-not $target) {
        Write-Host "找不到模块 '$name'。用 .\run.ps1 list 查看全部。" -ForegroundColor Red
        exit 1
    }

    if ($LiteLlmModules -contains $name) {
        # Embabel 的环境变量优先于 yml，所以显式指向 LiteLLM
        $env:OPENAI_BASE_URL = "http://localhost:4000"
        $env:OPENAI_API_KEY = "sk-1234"
        Write-Host "已把 OPENAI_BASE_URL/OPENAI_API_KEY 指向 LiteLLM（本模块需要 embedding/视觉）" -ForegroundColor Yellow
    } else {
        if (-not $env:DEEPSEEK_API_KEY -and -not $env:OPENAI_API_KEY) {
            Write-Host "提示：未检测到 DEEPSEEK_API_KEY / OPENAI_API_KEY 环境变量。" -ForegroundColor Yellow
        }
    }

    if (-not $SkipBuild) {
        Push-Location $root
        try { mvn -q -pl ":$name" package -DskipTests } finally { Pop-Location }
    }

    $jar = Get-ChildItem -Path $target.Path -Filter "$name-*.jar" -Recurse |
        Where-Object { $_.FullName -match "target" -and $_.Name -notmatch "original" } |
        Select-Object -First 1
    if (-not $jar) {
        Write-Host "未找到 $name 的 jar，请先构建（去掉 -SkipBuild）。" -ForegroundColor Red
        exit 1
    }

    Write-Host ("启动 {0}（端口 {1}）…" -f $name, $target.Port) -ForegroundColor Green
    $args = @("-jar", $jar.FullName)
    if ($JvmArgs) { $args = $JvmArgs.Split(" ") + $args }
    & java @args
}

switch ($Module) {
    "list"   { Show-List }
    "docker" { Start-Docker }
    default  { Start-Module $Module }
}
