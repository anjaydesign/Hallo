package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.ui.theme.StatusBahanBg
import com.example.ui.theme.StatusBahanBorder
import com.example.ui.theme.StatusBahanText
import com.example.ui.theme.StatusDoneBg
import com.example.ui.theme.StatusDoneBorder
import com.example.ui.theme.StatusDoneText
import com.example.ui.theme.StatusPaidBg
import com.example.ui.theme.StatusPaidBorder
import com.example.ui.theme.StatusPaidText
import com.example.ui.theme.StatusUnpaidBg
import com.example.ui.theme.StatusUnpaidBorder
import com.example.ui.theme.StatusUnpaidText

@Composable
fun PengerjaanBadge(status: String, modifier: Modifier = Modifier) {
    val isDone = status.equals(Customer.STATUS_DONE, ignoreCase = true)
    val bgColor = if (isDone) StatusDoneBg else StatusBahanBg
    val textColor = if (isDone) StatusDoneText else StatusBahanText
    val borderColor = if (isDone) StatusDoneBorder else StatusBahanBorder
    val label = if (isDone) "DONE" else "BAHAN"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun PembayaranBadge(status: String, modifier: Modifier = Modifier) {
    val isPaid = status.equals(Customer.STATUS_PAID, ignoreCase = true)
    val bgColor = if (isPaid) StatusPaidBg else StatusUnpaidBg
    val textColor = if (isPaid) StatusPaidText else StatusUnpaidText
    val borderColor = if (isPaid) StatusPaidBorder else StatusUnpaidBorder
    val label = if (isPaid) "PAID" else "UNPAID"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
