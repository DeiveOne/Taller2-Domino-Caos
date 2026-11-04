package com.udistrital.dominocaos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udistrital.dominocaos.ui.i18n.AppLanguage
import com.udistrital.dominocaos.ui.i18n.AppStrings
import com.udistrital.dominocaos.ui.i18n.LanguageManager

@Composable
fun SettingsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E0A38),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFF00E5FF)) },
        title = { Text(AppStrings.settings, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(AppStrings.language, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.SemiBold)

                AppLanguage.entries.forEach { lang ->
                    val isSelected = LanguageManager.currentLanguage == lang
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { LanguageManager.setLanguage(lang) },
                        color = if (isSelected) Color(0xFFE040FB).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        border = if (isSelected) BorderStroke(1.dp, Color(0xFFE040FB)) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${lang.flag}  ${lang.displayName}", color = Color.White, fontWeight = FontWeight.Bold)
                            RadioButton(
                                selected = isSelected,
                                onClick = { LanguageManager.setLanguage(lang) },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFE040FB), unselectedColor = Color.White.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(AppStrings.close, color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
            }
        }
    )
}
