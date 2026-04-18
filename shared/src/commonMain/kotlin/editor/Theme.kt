package editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object SynapseColors {
    val Background = Color(0xFF0F0F0F)
    val Surface = Color(0xFF121212)
    val Panel = Color(0xFF0F0F0F)
    val Selection = Color(0xFF1E1E1E)
    val Primary = Color(0xFF1E88E5)
    val Success = Color(0xFF66BB6A)
    val Error = Color(0xFFEF5350)
    
    val PerspectiveCapture = Color(0xFF81D4FA)
    val PerspectiveReference = Color(0xFFA5D6A7)
    val PerspectiveSynthesis = Color(0xFFFFCC80)
    val PerspectiveMap = Color(0xFFCE93D8)
}

object SynapseDimensions {
    val LeftNavWidth = 260.dp
    val ContextPanelWidth = 300.dp
    val MaxEditorWidth = 800.dp
    val BreadcrumbHorizontalPadding = 24.dp
    val EditorHorizontalPadding = 32.dp
}

object SynapseTypography {
    val BrandLetterSpacing = 4.sp
    val BrandFontSize = 12.sp
}
