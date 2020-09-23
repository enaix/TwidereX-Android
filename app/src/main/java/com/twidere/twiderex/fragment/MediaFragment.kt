package com.twidere.twiderex.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.rememberZoomableController
import androidx.compose.foundation.gestures.zoomable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.drawLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.fragment.navArgs
import com.twidere.twiderex.component.NetworkImage
import com.twidere.twiderex.component.Pager
import com.twidere.twiderex.db.model.DbMedia
import com.twidere.twiderex.extensions.AmbientNavController
import com.twidere.twiderex.extensions.compose
import com.twidere.twiderex.ui.TwidereXTheme
import kotlin.math.max

class MediaFragment : ComposeFragment() {
    private val args by navArgs<MediaFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return compose {
            TwidereXTheme(darkTheme = true) {
                onCompose()
            }
        }
    }

    @Composable
    override fun onCompose() {
        var lockPager by remember { mutableStateOf(false) }
        val controlPanelColor = MaterialTheme.colors.surface.copy(alpha = 0.6f)
        Scaffold {
            Stack {
                Pager(
                    items = args.status.media.sortedBy { it.order },
                    startPage = args.selectedIndex,
                    enableDrag = !lockPager,
                ) {
                    MediaItemView(data) {
                        lockPager = it
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(color = controlPanelColor)
                ) {
                    Row {
                        Spacer(modifier = Modifier.weight(1f))
                        ActionButton(onClick = {}) {
                            Icon(asset = Icons.Default.Reply)
                        }
                        ActionButton(onClick = {}) {
                            Icon(asset = Icons.Default.Comment)
                        }
                        ActionButton(onClick = {}) {
                            Icon(asset = Icons.Default.Favorite)
                        }
                        ActionButton(onClick = {}) {
                            Icon(asset = Icons.Default.Share)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .clip(RoundedCornerShape(8.dp))
                            .background(color = controlPanelColor, shape = RoundedCornerShape(8.dp))
                            .clipToBounds()
                    ) {
                        val navController = AmbientNavController.current
                        IconButton(onClick = {
                            navController.popBackStack()
                        }) {
                            Icon(asset = Icons.Default.Close)
                        }
                    }
                }
            }
        }
    }

}

@Composable
private fun ActionButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(ButtonConstants.DefaultMinWidth),
        icon = icon,
    )
}

@Composable
fun MediaItemView(
    data: DbMedia,
    requestLock: (Boolean) -> Unit,
) {
    var scale by remember { mutableStateOf(1f) }
    val zoomableController = rememberZoomableController { scale = max(scale * it, 1F) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        Box(
            modifier = Modifier
                .zoomable(
                    zoomableController,
                    onZoomStarted = {
                        requestLock(true)
                    },
                    onZoomStopped = {
                        requestLock(false)
                    },
                )
                .clickable(
                    indication = null,
                    onDoubleClick = { zoomableController.smoothScaleBy(4f) },
                    onClick = {}
                )
                .fillMaxSize(),
            gravity = ContentGravity.Center,
        ) {
            data.mediaUrl?.let {
                NetworkImage(
                    url = it,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .drawLayer(scaleX = scale, scaleY = scale),
                    placeholder = {
                        CircularProgressIndicator()
                    }
                )
            }
        }
    }
}