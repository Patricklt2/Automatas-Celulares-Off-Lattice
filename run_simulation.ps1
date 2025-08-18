$SrcDir = ".\simulation\src"
$OutDir = ".\simulation\out"

if (-not (Test-Path $OutDir)) {
    New-Item -ItemType Directory -Path $OutDir
}

javac -d $OutDir $SrcDir\*.java

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful. Running program..."
    java -cp $OutDir FrontEndGui
} else {
    Write-Host "Compilation failed."
}