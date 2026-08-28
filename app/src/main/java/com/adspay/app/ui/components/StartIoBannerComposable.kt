package com.adspay.app.ui.components

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.adspay.app.ui.theme.*
import com.startapp.sdk.ads.banner.Banner
import com.startapp.sdk.ads.banner.BannerListener

@Composable
fun StartIoBannerComposable(
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    if (!isEnabled) return

    val context = LocalContext.current
    var isAdLoaded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceWhite)
            .border(1.dp, PurpleLighter, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = PurplePrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "SPONSORED ADVERTISEMENT (START.IO)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )
            }

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                factory = { ctx ->
                    val container = FrameLayout(ctx).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }

                    try {
                        val banner = Banner(ctx, object : BannerListener {
                            override fun onReceiveAd(bannerView: View?) {
                                Log.d("StartIoBanner", "Banner ad loaded successfully")
                                isAdLoaded = true
                            }

                            override fun onFailedToReceiveAd(bannerView: View?) {
                                Log.w("StartIoBanner", "Banner ad failed to load")
                            }

                            override fun onClick(bannerView: View?) {
                                Log.d("StartIoBanner", "Banner ad clicked")
                            }

                            override fun onImpression(bannerView: View?) {
                                Log.d("StartIoBanner", "Banner ad impression logged")
                            }
                        })

                        banner.layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        container.addView(banner)
                        banner.loadAd()
                    } catch (e: Throwable) {
                        Log.e("StartIoBanner", "Banner instantiation error: ${e.message}", e)
                    }

                    container
                }
            )
        }
    }
}
