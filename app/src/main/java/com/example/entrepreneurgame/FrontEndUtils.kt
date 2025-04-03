package com.example.entrepreneurgame

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.entrepreneurgame.ui.theme.CartoonGreen
import com.example.entrepreneurgame.ui.theme.CartoonRed
import com.example.entrepreneurgame.ui.theme.CartoonTextPrimary
import kotlinx.coroutines.launch

@Composable
fun MyHeading(
    @StringRes activity: Int,
)
{
    Box (
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val playerName by remember { mutableStateOf(currentPlayer.name) }
            Text(
                text = stringResource(activity),
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = playerName,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
fun MyAnimatedButton(@StringRes label: Int, onClick: () -> Unit) {
    val scale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope() // Create a coroutine scope

    Button(
        onClick = {
            coroutineScope.launch {
                scale.animateTo(0.9f, animationSpec = tween(50))
                scale.animateTo(1f, animationSpec = tween(50))
            }
            onClick()
        },
        modifier = Modifier
            .padding(8.dp)
            .graphicsLayer(scaleX = scale.value, scaleY = scale.value),
        colors = ButtonDefaults.buttonColors(
            containerColor = CartoonRed,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = stringResource(label), fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}


@Composable
fun MyCenteredButton(
    @StringRes label: Int,
    onValueChange: () -> Unit,
    modifier: Modifier = Modifier,
    buttonColor: Color = CartoonRed,
    textColor: Color = Color.White
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onValueChange,
            modifier = Modifier.padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                contentColor = textColor
            ),
            shape = RoundedCornerShape(8.dp), // Rounded corners
        ) {
            Text(text = stringResource(label))
        }
    }
}

@Composable
fun ClickableSmallImage(
    imagePainter: Painter,
    onClick: () -> Unit
) {
    Image(
        painter = imagePainter,
        contentDescription = "Clickable Image",
        modifier = Modifier
            .size(30.dp) // Set the size of the image
            .clickable { onClick() }, // Handle the click event
        colorFilter = ColorFilter.tint(androidx.compose.ui.graphics.Color.Black) // Optional, for tint
    )
}

@Composable
fun PlayerSelector(title: String, count: Int, onDecrease: () -> Unit, onIncrease: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = CartoonTextPrimary
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onDecrease) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    modifier = Modifier.size(40.dp),
                    tint = CartoonRed
                )
            }

            Text(
                text = count.toString(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onIncrease) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    modifier = Modifier.size(40.dp),
                    tint = CartoonGreen
                )
            }
        }
    }
}
