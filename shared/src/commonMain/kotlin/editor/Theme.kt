package editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object SynapseColors {
    val Background = Color(0xFFF9F9FB)
    val Surface = Color(0xFFF9F9FB)
    val Panel = Color(0xFFF3F3F5) // Surface Container Low
    val Selection = Color(0xFFEEEEF0) // Surface Container
    val Primary = Color(0xFF000000)
    val OnPrimary = Color(0xFFE2E2E2)
    val OnSurface = Color(0xFF1A1C1D)
    val OnSurfaceVariant = Color(0xFF474747)
    val Success = Color(0xFF4CAF50)
    val Error = Color(0xFFBA1A1A)
    
    val SurfaceContainerLowest = Color(0xFFFFFFFF)
    val SurfaceContainerLow = Color(0xFFF3F3F5)
    val SurfaceContainer = Color(0xFFEEEEF0)
    val SurfaceContainerHigh = Color(0xFFE8E8EA)
    val SurfaceContainerHighest = Color(0xFFE2E2E4)
    val OutlineVariant = Color(0xFFC6C6C6)

    val PerspectiveCapture = Color(0xFF000000) // The system is monochromatic
    val PerspectiveReference = Color(0xFF000000)
    val PerspectiveSynthesis = Color(0xFF000000)
    val PerspectiveMap = Color(0xFF000000)
}

object SynapseDimensions {
    val LeftNavWidth = 256.dp // 64 * 4
    val ContextPanelWidth = 288.dp // 72 * 4
    val MaxEditorWidth = 800.dp
    val BreadcrumbHorizontalPadding = 48.dp // 12 * 4
    val EditorHorizontalPadding = 48.dp // 12 * 4
}

object SynapseTypography {
    val BrandLetterSpacing = 1.2.sp
    val BrandFontSize = 20.sp
    val TagFontSize = 10.sp
    val MetadataFontSize = 11.sp
}
