<#
Generates the Play Console feature graphic from committed ClickDungeon art.

Run from the repository root:
  ./scripts/generate_feature_graphic.ps1

The output is a 1024x500 RGB PNG at docs/store_assets/feature_graphic.png.
#>

$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Drawing

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$outputPath = Join-Path $repoRoot "docs/store_assets/feature_graphic.png"
$texturePath = Join-Path $repoRoot "app/src/main/res/drawable-nodpi/crypt.png"
$knightPath = Join-Path $repoRoot "app/src/main/res/drawable-nodpi/icon_knight.png"
$wizardPath = Join-Path $repoRoot "app/src/main/res/drawable-nodpi/icon_wizard.png"

foreach ($path in @($texturePath, $knightPath, $wizardPath)) {
    if (-not (Test-Path $path)) {
        throw "Missing required source asset: $path"
    }
}

$canvasWidth = 1024
$canvasHeight = 500
$darkBackground = [System.Drawing.Color]::FromArgb(0x1A, 0x0A, 0x00)
$deepShadow = [System.Drawing.Color]::FromArgb(0x0D, 0x05, 0x00)
$gold = [System.Drawing.Color]::FromArgb(0xFF, 0xF3, 0xD5)

function New-ColorBrush([System.Drawing.Color] $color, [int] $alpha = 255) {
    return New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb($alpha, $color))
}

function Draw-ImageWithOpacity(
    [System.Drawing.Graphics] $graphics,
    [System.Drawing.Image] $image,
    [System.Drawing.Rectangle] $dest,
    [System.Drawing.Rectangle] $source,
    [float] $opacity
) {
    $matrix = New-Object System.Drawing.Imaging.ColorMatrix
    $matrix.Matrix33 = $opacity
    $attributes = New-Object System.Drawing.Imaging.ImageAttributes
    $attributes.SetColorMatrix($matrix, [System.Drawing.Imaging.ColorMatrixFlag]::Default, [System.Drawing.Imaging.ColorAdjustType]::Bitmap)
    $graphics.DrawImage($image, $dest, $source.X, $source.Y, $source.Width, $source.Height, [System.Drawing.GraphicsUnit]::Pixel, $attributes)
    $attributes.Dispose()
}

function Draw-CenteredText(
    [System.Drawing.Graphics] $graphics,
    [string] $text,
    [System.Drawing.FontFamily] $fontFamily,
    [float] $fontSize,
    [float] $top,
    [float] $maxWidth,
    [System.Drawing.FontStyle] $style,
    [System.Drawing.Color] $fillColor,
    [System.Drawing.Color] $shadowColor
) {
    $font = New-Object System.Drawing.Font ($fontFamily, $fontSize, $style, [System.Drawing.GraphicsUnit]::Pixel)
    try {
        while ($fontSize -gt 20) {
            $size = $graphics.MeasureString($text, $font)
            if ($size.Width -le $maxWidth) {
                break
            }
            $font.Dispose()
            $fontSize -= 2
            $font = New-Object System.Drawing.Font ($fontFamily, $fontSize, $style, [System.Drawing.GraphicsUnit]::Pixel)
        }

        $size = $graphics.MeasureString($text, $font)
        $x = [float](($script:canvasWidth - $size.Width) / 2)
        $shadowBrush = New-ColorBrush $shadowColor 120
        $fillBrush = New-ColorBrush $fillColor 255
        try {
            foreach ($dx in -2..2) {
                foreach ($dy in -2..2) {
                    if (($dx * $dx + $dy * $dy) -le 5) {
                        $graphics.DrawString($text, $font, $shadowBrush, $x + 4 + $dx, $top + 5 + $dy)
                    }
                }
            }
            $graphics.DrawString($text, $font, $fillBrush, $x, $top)
        }
        finally {
            $shadowBrush.Dispose()
            $fillBrush.Dispose()
        }
    }
    finally {
        $font.Dispose()
    }
}

function Get-FeatureFontFamily {
    $fontFiles = @(
        "C:/Windows/Fonts/Castle Bold_1.ttf",
        "C:/Windows/Fonts/OLDENG.TTF",
        "C:/Windows/Fonts/OldTown.TTF",
        "C:/Windows/Fonts/georgiab.ttf"
    )

    foreach ($fontFile in $fontFiles) {
        if (Test-Path $fontFile) {
            $collection = New-Object System.Drawing.Text.PrivateFontCollection
            $collection.AddFontFile($fontFile)
            if ($collection.Families.Count -gt 0) {
                return @{ Family = $collection.Families[0]; Collection = $collection }
            }
            $collection.Dispose()
        }
    }

    return @{ Family = [System.Drawing.FontFamily]::GenericSerif; Collection = $null }
}

$bitmap = [System.Drawing.Bitmap]::new($canvasWidth, $canvasHeight, [System.Drawing.Imaging.PixelFormat]::Format24bppRgb)
$graphics = [System.Drawing.Graphics]::FromImage($bitmap)
$texture = [System.Drawing.Image]::FromFile($texturePath)
$knight = [System.Drawing.Image]::FromFile($knightPath)
$wizard = [System.Drawing.Image]::FromFile($wizardPath)
$fontInfo = Get-FeatureFontFamily

try {
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
    $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality

    $baseBrush = New-ColorBrush $darkBackground
    try {
        $graphics.FillRectangle($baseBrush, 0, 0, $canvasWidth, $canvasHeight)
    }
    finally {
        $baseBrush.Dispose()
    }

    $sourceCrop = New-Object System.Drawing.Rectangle (0, 170, 1024, 500)
    $canvasRect = [System.Drawing.Rectangle]::new(0, 0, $canvasWidth, $canvasHeight)
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    Draw-ImageWithOpacity $graphics $texture $canvasRect $sourceCrop 0.48

    $darkenBrush = New-ColorBrush $darkBackground 72
    try {
        $graphics.FillRectangle($darkenBrush, 0, 0, $canvasWidth, $canvasHeight)
    }
    finally {
        $darkenBrush.Dispose()
    }

    foreach ($edge in @(
        @{ Rect = [System.Drawing.Rectangle]::new(0, 0, $canvasWidth, 150); Angle = 90 },
        @{ Rect = [System.Drawing.Rectangle]::new(0, $canvasHeight - 160, $canvasWidth, 160); Angle = 270 },
        @{ Rect = [System.Drawing.Rectangle]::new(0, 0, 190, $canvasHeight); Angle = 0 },
        @{ Rect = [System.Drawing.Rectangle]::new($canvasWidth - 190, 0, 190, $canvasHeight); Angle = 180 }
    )) {
        $brush = New-Object System.Drawing.Drawing2D.LinearGradientBrush (
            $edge.Rect,
            [System.Drawing.Color]::FromArgb(170, $deepShadow),
            [System.Drawing.Color]::FromArgb(0, $deepShadow),
            [single]$edge.Angle
        )
        try {
            $graphics.FillRectangle($brush, $edge.Rect)
        }
        finally {
            $brush.Dispose()
        }
    }

    $iconSize = 180
    $iconY = [int](($canvasHeight - $iconSize) / 2)
    $iconSource = [System.Drawing.Rectangle]::new(0, 0, 1024, 1024)
    $leftIcon = [System.Drawing.Rectangle]::new(40, $iconY, $iconSize, $iconSize)
    $rightIcon = [System.Drawing.Rectangle]::new($canvasWidth - 40 - $iconSize, $iconY, $iconSize, $iconSize)

    $glowBrush = New-ColorBrush $gold 34
    $shadowBrush = New-ColorBrush $deepShadow 150
    try {
        foreach ($rect in @($leftIcon, $rightIcon)) {
            $graphics.FillEllipse($glowBrush, $rect.X - 16, $rect.Y - 16, $rect.Width + 32, $rect.Height + 32)
            $graphics.FillRectangle($shadowBrush, $rect.X + 8, $rect.Y + 8, $rect.Width, $rect.Height)
        }
    }
    finally {
        $glowBrush.Dispose()
        $shadowBrush.Dispose()
    }

    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
    $graphics.DrawImage($knight, $leftIcon, $iconSource, [System.Drawing.GraphicsUnit]::Pixel)
    $graphics.DrawImage($wizard, $rightIcon, $iconSource, [System.Drawing.GraphicsUnit]::Pixel)

    Draw-CenteredText $graphics "ClickDungeon" $fontInfo.Family 104 152 610 ([System.Drawing.FontStyle]::Bold) $gold $deepShadow
    Draw-CenteredText $graphics "Tap. Fight. Survive." $fontInfo.Family 40 406 520 ([System.Drawing.FontStyle]::Regular) $gold $deepShadow
}
finally {
    if ($fontInfo.Collection -ne $null) {
        $fontInfo.Collection.Dispose()
    }
    $wizard.Dispose()
    $knight.Dispose()
    $texture.Dispose()
    $graphics.Dispose()
}

$bitmap.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
$bitmap.Dispose()

Write-Host "Generated $outputPath"
