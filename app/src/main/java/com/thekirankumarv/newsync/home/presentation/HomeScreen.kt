package com.thekirankumarv.newsync.home.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.LazyPagingItems
import com.thekirankumarv.newsync.R
import com.thekirankumarv.newsync.home.domain.model.Article
import com.thekirankumarv.newsync.home.presentation.common.ArticlesList
import com.thekirankumarv.newsync.home.presentation.common.SearchBar
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    articles: LazyPagingItems<Article>,
    navigateToSearch: () -> Unit,
    navigateToDetails: (Article) -> Unit,
    event: (HomeEvent) -> Unit,
    state: HomeState,
) {

    val titles by remember {
        derivedStateOf {
            if (articles.itemCount > 10) {
                articles.itemSnapshotList.items
                    .slice(IntRange(start = 0, endInclusive = 9))
                    .joinToString(separator = " \uD83D\uDFE5 ") { it.title }
            } else {
                ""
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
            .statusBarsPadding()
    ) {
        Image(
            painter = painterResource(id = R.drawable.breakingnews),
            contentDescription = null,
            modifier = Modifier
                .width(200.dp)
                .height(60.dp)
                .padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        SearchBar(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth(),
            text = "",
            readOnly = true,
            onValueChange = {},
            onSearch = {},
            onClick = {
                navigateToSearch()
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        val scrollState = rememberScrollState()

        LaunchedEffect(key1 = state.maxScrollingValue) {
            delay(500)
            if (state.maxScrollingValue > 0) {
                scrollState.animateScrollTo(
                    value = state.maxScrollingValue,
                    animationSpec = infiniteRepeatable(
                        tween(
                            durationMillis = (state.maxScrollingValue - state.scrollValue) * 50_000 / state.maxScrollingValue,
                            easing = LinearEasing,
                            delayMillis = 1000
                        )
                    )
                )
            }
        }
        // Update the maxScrollingValue
        LaunchedEffect(key1 = scrollState.maxValue) {
            event(HomeEvent.UpdateMaxScrollingValue(scrollState.maxValue))
        }
        // Save the state of the scrolling position
        LaunchedEffect(key1 = scrollState.value) {
            event(HomeEvent.UpdateScrollValue(scrollState.value))
        }

        Text(
            text = titles, modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp)
                .basicMarquee(), fontSize = 12.sp,
            color = colorResource(id = R.color.placeholder)
        )

        Spacer(modifier = Modifier.height(24.dp))

        ArticlesList(
            modifier = Modifier.padding(horizontal = 24.dp),
            articles = articles,
            onClick = {
                navigateToDetails(it)
            }
        )
    }
}

